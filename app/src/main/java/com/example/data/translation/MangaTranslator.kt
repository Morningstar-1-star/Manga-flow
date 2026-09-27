package com.example.data.translation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.data.local.KotatsuDatabase
import com.example.data.local.TranslationCacheEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

enum class TranslationMode(val displayName: String) {
    ENGLISH_TYPESETTING("English Overlay"),
    ORIGINAL_JAPANESE("Original OCR Text"),
    BILINGUAL("Bilingual Comparison")
}

data class TranslatedBubble(
    val xRatio: Float,        // 0.0 to 1.0 (X position percentage on image)
    val yRatio: Float,        // 0.0 to 1.0 (Y position percentage on image)
    val widthRatio: Float,    // Width percentage
    val heightRatio: Float,   // Height percentage
    val originalText: String,
    val translatedText: String,
    val language: String = "ja",
    val confidence: Float = 0.95f
)

data class PageTranslationResult(
    val pageIndex: Int,
    val sourceLanguage: String,
    val targetLanguage: String,
    val bubbles: List<TranslatedBubble>,
    val fullTranslationSummary: String
)

data class ChapterTranslationProgress(
    val chapterId: String,
    val totalPages: Int,
    val completedPages: Int,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false
)

/**
 * Pluggable OCR & Translation Engine interface
 */
interface MangaOcrTranslationEngine {
    val name: String
    suspend fun translatePage(
        imageBytes: ByteArray,
        pageIndex: Int,
        sourceLang: String,
        targetLang: String,
        glossary: Map<String, String>,
        apiKey: String
    ): Result<List<TranslatedBubble>>
}

/**
 * Gemini Vision Multimodal Manga OCR & Translation Engine
 * Passes actual image bytes directly via inlineData for vision detection,
 * speech bubble localization, vertical/horizontal Japanese OCR, and contextual translation.
 */
class GeminiVisionTranslationEngine(
    private val httpClient: OkHttpClient
) : MangaOcrTranslationEngine {

    override val name: String = "Gemini Vision Multimodal OCR"

    override suspend fun translatePage(
        imageBytes: ByteArray,
        pageIndex: Int,
        sourceLang: String,
        targetLang: String,
        glossary: Map<String, String>,
        apiKey: String
    ): Result<List<TranslatedBubble>> = withContext(Dispatchers.IO) {
        runCatching {
            if (apiKey.isBlank()) {
                throw IOException("Gemini API key is not configured. Enter an API key in Settings or Secrets panel.")
            }

            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val glossaryContext = glossary.entries.joinToString(", ") { "${it.key}=${it.value}" }

            val promptText = buildString {
                append("You are a professional manga OCR, localization, and typesetting engine. ")
                append("Detect all dialogue bubbles, narration boxes, sound effects, and text regions in this comic page image. ")
                append("Source language: $sourceLang, target translation language: $targetLang. ")
                if (glossaryContext.isNotEmpty()) {
                    append("Manga Glossary: [$glossaryContext]. ")
                }
                append("For each detected text bubble or region:\n")
                append("1. Locate its normalized bounding box: xmin (0.0-1.0), ymin (0.0-1.0), xmax (0.0-1.0), ymax (0.0-1.0).\n")
                append("2. Transcribe the original text accurately (handle vertical and horizontal comic script).\n")
                append("3. Translate into natural $targetLang.\n")
                append("Return ONLY a valid JSON array of objects with keys: \"xmin\", \"ymin\", \"xmax\", \"ymax\", \"originalText\", \"translatedText\". If no text is found, return [].")
            }

            val jsonBody = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Image)
                        })
                    })
                    put(JSONObject().apply {
                        put("text", promptText)
                    })
                }
                put("contents", JSONArray().put(JSONObject().apply {
                    put("parts", parts)
                }))
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: ""
                throw IOException("Gemini Vision API error (HTTP ${response.code}): $err")
            }

            val body = response.body?.string() ?: throw IOException("Empty response from translation engine")
            parseGeminiVisionResponse(body, targetLang)
        }
    }

    private fun parseGeminiVisionResponse(response: String, targetLang: String): List<TranslatedBubble> {
        val list = mutableListOf<TranslatedBubble>()
        val jsonStart = response.indexOf("[")
        val jsonEnd = response.lastIndexOf("]")
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            val jsonArr = JSONArray(response.substring(jsonStart, jsonEnd + 1))
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val xmin = obj.optDouble("xmin", 0.1).toFloat().coerceIn(0f, 1f)
                val ymin = obj.optDouble("ymin", 0.1).toFloat().coerceIn(0f, 1f)
                val xmax = obj.optDouble("xmax", (xmin + 0.3).toDouble()).toFloat().coerceIn(0f, 1f)
                val ymax = obj.optDouble("ymax", (ymin + 0.15).toDouble()).toFloat().coerceIn(0f, 1f)

                val width = (xmax - xmin).coerceAtLeast(0.05f)
                val height = (ymax - ymin).coerceAtLeast(0.04f)

                val orig = obj.optString("originalText", "")
                val trans = obj.optString("translatedText", "")

                if (orig.isNotBlank() || trans.isNotBlank()) {
                    list.add(
                        TranslatedBubble(
                            xRatio = xmin,
                            yRatio = ymin,
                            widthRatio = width,
                            heightRatio = height,
                            originalText = orig,
                            translatedText = trans,
                            language = targetLang,
                            confidence = 0.95f
                        )
                    )
                }
            }
        }
        return list
    }
}

/**
 * MangaTranslator
 * Manages translation pipeline, image byte loading, multimodal OCR,
 * persistent Room caching, and chapter translation tracking.
 */
object MangaTranslator {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private val defaultEngine: MangaOcrTranslationEngine = GeminiVisionTranslationEngine(httpClient)

    // In-memory Translation Cache
    private val memoryCache = ConcurrentHashMap<String, PageTranslationResult>()

    // Custom Japanese -> English Glossary
    private val glossary = ConcurrentHashMap<String, String>().apply {
        put("センパイ", "Senpai (Senior)")
        put("先生", "Sensei (Teacher)")
        put("仲間", "Comrades / Friends")
        put("魔王", "Demon King")
        put("勇者", "Hero")
        put("ギルド", "Guild")
        put("魔法", "Magic")
    }

    private val _chapterProgress = MutableStateFlow<ChapterTranslationProgress?>(null)
    val chapterProgress: StateFlow<ChapterTranslationProgress?> = _chapterProgress.asStateFlow()

    val supportedLanguages = listOf(
        "ja" to "Japanese (日本語)",
        "ko" to "Korean (한국어)",
        "zh" to "Chinese (中文)",
        "vi" to "Vietnamese (Tiếng Việt)",
        "es" to "Spanish (Español)",
        "fr" to "French (Français)",
        "de" to "German (Deutsch)",
        "auto" to "Auto Detect Language"
    )

    val targetLanguages = listOf(
        "en" to "English 🇺🇸",
        "es" to "Spanish 🇪🇸",
        "fr" to "French 🇫🇷",
        "de" to "German 🇩🇪",
        "vi" to "Vietnamese 🇻🇳",
        "id" to "Indonesian 🇮🇩",
        "pt" to "Portuguese 🇧🇷",
        "ru" to "Russian 🇷🇺"
    )

    fun getGlossary(): Map<String, String> = glossary.toMap()

    fun addGlossaryTerm(term: String, translation: String) {
        glossary[term] = translation
    }

    fun removeGlossaryTerm(term: String) {
        glossary.remove(term)
    }

    fun clearCache() {
        memoryCache.clear()
    }

    /**
     * Translates a comic page by fetching actual image bytes,
     * executing real multimodal vision OCR and translation.
     * Persists results in Room database and in-memory cache.
     * NEVER fabricates fake bubbles if API or network fails.
     */
    suspend fun translateComicPage(
        context: Context?,
        pageIndex: Int,
        imageUrl: String,
        sourceLang: String = "auto",
        targetLang: String = "en",
        forceRetranslate: Boolean = false,
        apiKey: String = ""
    ): Result<PageTranslationResult> = withContext(Dispatchers.IO) {
        val cacheKey = "$imageUrl|$sourceLang|$targetLang"

        // 1. Check memory cache
        if (!forceRetranslate) {
            val cached = memoryCache[cacheKey]
            if (cached != null) {
                return@withContext Result.success(cached)
            }
        }

        // 2. Check Room database cache
        if (!forceRetranslate && context != null) {
            try {
                val db = KotatsuDatabase.getInstance(context)
                val dbCached = db.translationCacheDao().getTranslation(cacheKey)
                if (dbCached != null) {
                    val bubbles = parseBubblesJson(dbCached.bubblesJson)
                    val result = PageTranslationResult(
                        pageIndex = dbCached.pageIndex,
                        sourceLanguage = dbCached.sourceLanguage,
                        targetLanguage = dbCached.targetLanguage,
                        bubbles = bubbles,
                        fullTranslationSummary = dbCached.fullSummary
                    )
                    memoryCache[cacheKey] = result
                    return@withContext Result.success(result)
                }
            } catch (_: Exception) { }
        }

        // 3. Load actual image bytes (from local file or remote URL)
        val imageBytesResult = loadImageBytes(imageUrl)
        if (imageBytesResult.isFailure) {
            return@withContext Result.failure(
                imageBytesResult.exceptionOrNull() ?: IOException("Failed to load image bytes for translation.")
            )
        }
        val imageBytes = imageBytesResult.getOrThrow()

        // 4. Resolve effective Gemini API Key
        val effectiveKey = if (apiKey.isNotBlank()) {
            apiKey
        } else {
            runCatching { com.example.BuildConfig.GEMINI_API_KEY }.getOrDefault("")
        }

        // 5. Execute real OCR & Translation
        val bubblesResult = defaultEngine.translatePage(
            imageBytes = imageBytes,
            pageIndex = pageIndex,
            sourceLang = sourceLang,
            targetLang = targetLang,
            glossary = glossary.toMap(),
            apiKey = effectiveKey
        )

        if (bubblesResult.isFailure) {
            return@withContext Result.failure(bubblesResult.exceptionOrNull()!!)
        }

        val bubbles = bubblesResult.getOrThrow()
        val summary = if (bubbles.isEmpty()) {
            "No text bubbles detected on page ${pageIndex + 1}"
        } else {
            "Page ${pageIndex + 1} translated (${bubbles.size} bubbles localized)"
        }

        val pageResult = PageTranslationResult(
            pageIndex = pageIndex,
            sourceLanguage = sourceLang,
            targetLanguage = targetLang,
            bubbles = bubbles,
            fullTranslationSummary = summary
        )

        // 6. Save in memory cache
        memoryCache[cacheKey] = pageResult

        // 7. Save in Room database cache
        if (context != null) {
            try {
                val db = KotatsuDatabase.getInstance(context)
                val bubblesJson = serializeBubbles(bubbles)
                db.translationCacheDao().insertTranslation(
                    TranslationCacheEntity(
                        cacheKey = cacheKey,
                        pageIndex = pageIndex,
                        sourceLanguage = sourceLang,
                        targetLanguage = targetLang,
                        bubblesJson = bubblesJson,
                        fullSummary = summary
                    )
                )
            } catch (_: Exception) { }
        }

        Result.success(pageResult)
    }

    private suspend fun loadImageBytes(imageUrl: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            if (imageUrl.startsWith("file://")) {
                val filePath = imageUrl.removePrefix("file://").substringBefore("#")
                val file = File(filePath)
                if (!file.exists()) throw IOException("Local image file not found: $filePath")
                file.readBytes()
            } else {
                val request = Request.Builder()
                    .url(imageUrl)
                    .header("User-Agent", "MangaFlow/2.0 (Kotatsu)")
                    .build()
                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code} downloading image for OCR")
                }
                response.body?.bytes() ?: throw IOException("Empty image response body")
            }
        }
    }

    private fun serializeBubbles(bubbles: List<TranslatedBubble>): String {
        val arr = JSONArray()
        for (b in bubbles) {
            val obj = JSONObject().apply {
                put("xRatio", b.xRatio.toDouble())
                put("yRatio", b.yRatio.toDouble())
                put("widthRatio", b.widthRatio.toDouble())
                put("heightRatio", b.heightRatio.toDouble())
                put("originalText", b.originalText)
                put("translatedText", b.translatedText)
                put("language", b.language)
                put("confidence", b.confidence.toDouble())
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseBubblesJson(json: String): List<TranslatedBubble> {
        val list = mutableListOf<TranslatedBubble>()
        runCatching {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    TranslatedBubble(
                        xRatio = obj.optDouble("xRatio", 0.0).toFloat(),
                        yRatio = obj.optDouble("yRatio", 0.0).toFloat(),
                        widthRatio = obj.optDouble("widthRatio", 0.0).toFloat(),
                        heightRatio = obj.optDouble("heightRatio", 0.0).toFloat(),
                        originalText = obj.optString("originalText", ""),
                        translatedText = obj.optString("translatedText", ""),
                        language = obj.optString("language", "en"),
                        confidence = obj.optDouble("confidence", 0.95).toFloat()
                    )
                )
            }
        }
        return list
    }

    suspend fun translateEntireChapter(
        context: Context?,
        chapterId: String,
        pages: List<String>,
        sourceLang: String = "auto",
        targetLang: String = "en",
        onPageTranslated: (Int, PageTranslationResult) -> Unit
    ) = withContext(Dispatchers.IO) {
        _chapterProgress.value = ChapterTranslationProgress(
            chapterId = chapterId,
            totalPages = pages.size,
            completedPages = 0,
            isRunning = true
        )

        for ((index, url) in pages.withIndex()) {
            if (_chapterProgress.value?.isRunning != true) break

            val pageResult = translateComicPage(
                context = context,
                pageIndex = index,
                imageUrl = url,
                sourceLang = sourceLang,
                targetLang = targetLang
            )
            pageResult.getOrNull()?.let {
                onPageTranslated(index, it)
            }

            _chapterProgress.value = _chapterProgress.value?.copy(
                completedPages = index + 1
            )
        }

        _chapterProgress.value = _chapterProgress.value?.copy(isRunning = false)
    }

    fun stopChapterTranslation() {
        _chapterProgress.value = _chapterProgress.value?.copy(isRunning = false)
    }
}
