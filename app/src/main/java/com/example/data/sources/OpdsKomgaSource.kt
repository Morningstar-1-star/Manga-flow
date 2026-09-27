package com.example.data.sources

import com.example.data.model.Chapter
import com.example.data.model.Manga
import com.example.data.model.MangaPage
import com.example.data.model.MangaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Komga Source Adapter
 * Connects to a self-hosted Komga manga/comic server via its official REST API.
 */
class KomgaSourceAdapter(
    var serverUrl: String = "",
    var username: String = "",
    var password: String = ""
) : SourceAdapter {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    override val sourceType: SourceType = SourceType.KOMGA

    override val source: MangaSource = MangaSource(
        id = "komga_server",
        name = "Komga Server",
        domain = "komga.local",
        language = "all",
        category = "Komga",
        iconEmoji = "📚",
        brandColorHex = 0xFF6366F1,
        reliability = 1.0f,
        isCustom = true
    )

    private fun getAuthHeader(): String? {
        return if (username.isNotBlank() && password.isNotBlank()) {
            Credentials.basic(username, password)
        } else null
    }

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        if (serverUrl.isBlank()) {
            return@withContext Result.failure(IOException("Komga server URL is not configured. Configure it in Settings."))
        }

        runCatching {
            val pageIndex = (page - 1).coerceAtLeast(0)
            val requestBuilder = Request.Builder()
                .url("$serverUrl/api/v1/series?page=$pageIndex&size=20&sort=metadata.titleSort,asc")

            getAuthHeader()?.let { requestBuilder.header("Authorization", it) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) throw IOException("Komga API error: HTTP ${response.code}")

            val body = response.body?.string() ?: throw IOException("Empty response from Komga")
            val json = JSONObject(body)
            val content = json.getJSONArray("content")
            val list = mutableListOf<Manga>()

            for (i in 0 until content.length()) {
                val item = content.getJSONObject(i)
                val id = item.getString("id")
                val metadata = item.optJSONObject("metadata")
                val title = metadata?.optString("title", item.optString("name", "Komga Series")) ?: "Series"
                val summary = metadata?.optString("summary", "") ?: ""
                val status = metadata?.optString("status", "ONGOING") ?: "ONGOING"
                val booksCount = item.optInt("booksCount", 0)

                list.add(
                    Manga(
                        id = "komga_$id",
                        sourceId = source.id,
                        title = title,
                        author = metadata?.optJSONArray("authors")?.optJSONObject(0)?.optString("name", "Unknown") ?: "Unknown",
                        description = summary,
                        coverUrl = "$serverUrl/api/v1/series/$id/thumbnail",
                        status = status.lowercase().replaceFirstChar { it.uppercase() },
                        totalChapters = booksCount,
                        genres = listOf("Komga", "Self-Hosted")
                    )
                )
            }
            list
        }
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(
        query: String,
        page: Int,
        genres: List<String>,
        author: String?
    ): Result<List<Manga>> = withContext(Dispatchers.IO) {
        if (serverUrl.isBlank()) {
            return@withContext Result.failure(IOException("Komga server URL not configured."))
        }
        runCatching {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val requestBuilder = Request.Builder()
                .url("$serverUrl/api/v1/series?search=$encoded&page=0&size=20")
            getAuthHeader()?.let { requestBuilder.header("Authorization", it) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) throw IOException("Komga search failed: HTTP ${response.code}")
            val body = response.body?.string() ?: throw IOException("Empty body")
            val content = JSONObject(body).getJSONArray("content")
            val list = mutableListOf<Manga>()
            for (i in 0 until content.length()) {
                val item = content.getJSONObject(i)
                val id = item.getString("id")
                val metadata = item.optJSONObject("metadata")
                val title = metadata?.optString("title", item.optString("name", "Komga Series")) ?: "Series"

                list.add(
                    Manga(
                        id = "komga_$id",
                        sourceId = source.id,
                        title = title,
                        coverUrl = "$serverUrl/api/v1/series/$id/thumbnail",
                        genres = listOf("Komga")
                    )
                )
            }
            list
        }
    }

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> = withContext(Dispatchers.IO) {
        if (serverUrl.isBlank()) return@withContext Result.failure(IOException("Komga server URL not configured."))
        runCatching {
            val seriesId = mangaId.removePrefix("komga_")
            val requestBuilder = Request.Builder()
                .url("$serverUrl/api/v1/series/$seriesId")
            getAuthHeader()?.let { requestBuilder.header("Authorization", it) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) throw IOException("Komga details failed: HTTP ${response.code}")
            val body = response.body?.string() ?: throw IOException("Empty body")
            val item = JSONObject(body)
            val metadata = item.optJSONObject("metadata")
            val title = metadata?.optString("title", item.optString("name", "Komga Series")) ?: "Series"
            val summary = metadata?.optString("summary", "") ?: ""

            Manga(
                id = mangaId,
                sourceId = source.id,
                title = title,
                description = summary,
                coverUrl = "$serverUrl/api/v1/series/$seriesId/thumbnail",
                genres = listOf("Komga")
            )
        }
    }

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        if (serverUrl.isBlank()) return@withContext Result.failure(IOException("Komga server URL not configured."))
        runCatching {
            val seriesId = mangaId.removePrefix("komga_")
            val requestBuilder = Request.Builder()
                .url("$serverUrl/api/v1/series/$seriesId/books?size=500&sort=metadata.numberSort,asc")
            getAuthHeader()?.let { requestBuilder.header("Authorization", it) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) throw IOException("Komga books failed: HTTP ${response.code}")
            val body = response.body?.string() ?: throw IOException("Empty body")
            val content = JSONObject(body).getJSONArray("content")
            val chapters = mutableListOf<Chapter>()

            for (i in 0 until content.length()) {
                val book = content.getJSONObject(i)
                val bookId = book.getString("id")
                val name = book.optString("name", "Book ${i + 1}")
                val number = book.optJSONObject("metadata")?.optDouble("numberSort", (i + 1).toDouble())?.toFloat() ?: (i + 1).toFloat()
                val pageCount = book.optInt("media_pagesCount", book.optInt("mediaPagesCount", 0))

                chapters.add(
                    Chapter(
                        id = bookId,
                        mangaId = mangaId,
                        sourceId = source.id,
                        name = name,
                        number = number,
                        pageCount = pageCount,
                        scanlator = "Komga"
                    )
                )
            }
            chapters
        }
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        if (serverUrl.isBlank()) return@withContext Result.failure(IOException("Komga server URL not configured."))
        runCatching {
            val requestBuilder = Request.Builder()
                .url("$serverUrl/api/v1/books/$chapterId/pages")
            getAuthHeader()?.let { requestBuilder.header("Authorization", it) }
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) throw IOException("Komga pages failed: HTTP ${response.code}")
            val body = response.body?.string() ?: throw IOException("Empty body")
            val pagesArray = JSONArray(body)
            val pages = mutableListOf<MangaPage>()

            val authMap = getAuthHeader()?.let { mapOf("Authorization" to it) } ?: emptyMap()

            for (i in 0 until pagesArray.length()) {
                val pageObj = pagesArray.getJSONObject(i)
                val pageNumber = pageObj.optInt("number", i + 1)
                pages.add(
                    MangaPage(
                        index = pageNumber,
                        imageUrl = "$serverUrl/api/v1/books/$chapterId/pages/$pageNumber",
                        headers = authMap
                    )
                )
            }
            pages
        }
    }
}

/**
 * OPDS Source Adapter
 * Open Publication Distribution System (OPDS) catalog reader for Calibre, Kavita, etc.
 */
class OpdsSourceAdapter(
    var catalogUrl: String = "",
    var catalogName: String = "OPDS Catalog"
) : SourceAdapter {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    override val sourceType: SourceType = SourceType.OPDS

    override val source: MangaSource = MangaSource(
        id = "opds_catalog",
        name = catalogName,
        domain = if (catalogUrl.isNotBlank()) catalogUrl.removePrefix("https://").removePrefix("http://") else "opds.local",
        language = "all",
        category = "OPDS",
        iconEmoji = "🌐",
        brandColorHex = 0xFF10B981,
        reliability = 0.98f,
        isCustom = true
    )

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        if (catalogUrl.isBlank()) {
            return@withContext Result.failure(IOException("OPDS feed URL is not configured. Configure it in Settings."))
        }
        runCatching {
            val request = Request.Builder().url(catalogUrl).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("OPDS error: HTTP ${response.code}")
            val xml = response.body?.string() ?: throw IOException("Empty OPDS body")
            val doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser())
            val entries = doc.select("entry")
            val list = mutableListOf<Manga>()

            for (entry in entries) {
                val id = entry.selectFirst("id")?.text() ?: entry.selectFirst("title")?.text() ?: "entry"
                val title = entry.selectFirst("title")?.text() ?: "Untitled"
                val author = entry.selectFirst("author name")?.text() ?: "Unknown"
                val summary = entry.selectFirst("summary, content")?.text() ?: ""
                val coverLink = entry.selectFirst("link[rel*='thumbnail'], link[rel*='image']")?.attr("href") ?: ""

                list.add(
                    Manga(
                        id = "opds_${id.hashCode()}",
                        sourceId = source.id,
                        title = title,
                        author = author,
                        description = summary,
                        coverUrl = coverLink,
                        genres = listOf("OPDS")
                    )
                )
            }
            list
        }
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(
        query: String,
        page: Int,
        genres: List<String>,
        author: String?
    ): Result<List<Manga>> = getPopularManga(page)

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> = withContext(Dispatchers.IO) {
        Result.success(
            Manga(
                id = mangaId,
                sourceId = source.id,
                title = "OPDS Publication",
                description = "OPDS catalog publication entry.",
                genres = listOf("OPDS")
            )
        )
    }

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        Result.success(
            listOf(
                Chapter(
                    id = "${mangaId}_ch1",
                    mangaId = mangaId,
                    sourceId = source.id,
                    name = "Complete Volume",
                    number = 1f,
                    scanlator = "OPDS"
                )
            )
        )
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        Result.failure(IOException("Reading OPDS entry pages requires direct acquisition link downloading."))
    }
}
