package com.example.data.sources

import com.example.data.model.Chapter
import com.example.data.model.Manga
import com.example.data.model.MangaPage
import com.example.data.model.MangaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ComickSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) : SourceAdapter {

    override val sourceType: SourceType = SourceType.API_SOURCE

    override val source = MangaSource(
        id = "comick",
        name = "ComicK",
        domain = "comick.io",
        isPinned = true,
        isEnabled = true,
        language = "all",
        isNsfw = false,
        iconEmoji = "🦄",
        brandColorHex = 0xFF38BDF8,
        reliability = 0.97f
    )

    private val baseUrl = "https://api.comick.fun"

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$baseUrl/top?type=trending&page=$page&limit=20"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("ComicK API error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty body from ComicK")
            val array = JSONArray(body)
            parseComickArray(array)
        }
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$baseUrl/top?type=new&page=$page&limit=20"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("ComicK API error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty body from ComicK")
            val array = JSONArray(body)
            parseComickArray(array)
        }
    }

    override suspend fun searchManga(
        query: String,
        page: Int,
        genres: List<String>,
        author: String?
    ): Result<List<Manga>> = withContext(Dispatchers.IO) {
        runCatching {
            val encodedQuery = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val url = "$baseUrl/v1.0/search?q=$encodedQuery&page=$page&limit=20"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("ComicK search error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty body from ComicK")
            val array = JSONArray(body)
            parseComickArray(array)
        }
    }

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> = withContext(Dispatchers.IO) {
        runCatching {
            val slug = mangaId.removePrefix("comick_")
            val url = "$baseUrl/comic/$slug"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("ComicK detail error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty body from ComicK")
            val json = JSONObject(body).getJSONObject("comic")
            parseComickDetailJson(json)
        }
    }

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        runCatching {
            val slug = mangaId.removePrefix("comick_")
            val lang = if (language == "all" || language == null) "en" else language
            val url = "$baseUrl/comic/$slug/chapters?lang=$lang&limit=100"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("ComicK chapters error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty body from ComicK")
            val json = JSONObject(body)
            val chaptersArray = json.getJSONArray("chapters")
            val chapters = mutableListOf<Chapter>()

            for (i in 0 until chaptersArray.length()) {
                val item = chaptersArray.getJSONObject(i)
                val hid = item.getString("hid")
                val chapStr = item.optString("chap", (i + 1).toString())
                val chapNum = chapStr.toFloatOrNull() ?: (i + 1).toFloat()
                val vol = item.optString("vol", "")
                val title = item.optString("title", "")
                val date = item.optString("created_at", "Recent").take(10)

                val name = buildString {
                    if (vol.isNotEmpty()) append("Vol. $vol ")
                    append("Chapter $chapStr")
                    if (title.isNotEmpty()) append(": $title")
                }

                chapters.add(
                    Chapter(
                        id = hid,
                        mangaId = mangaId,
                        sourceId = source.id,
                        name = name,
                        number = chapNum,
                        volume = if (vol.isNotEmpty()) "Volume $vol" else "",
                        scanlator = item.optJSONArray("group_name")?.optString(0) ?: "Official",
                        dateUpload = date,
                        language = lang,
                        pageCount = 0
                    )
                )
            }
            if (chapters.isEmpty()) throw IOException("No chapters found on ComicK for language: $language")
            chapters
        }
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$baseUrl/chapter/$chapterId"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("ComicK page server error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty body from ComicK")
            val json = JSONObject(body).getJSONObject("chapter")
            val imagesArray = json.getJSONArray("images")
            val pages = mutableListOf<MangaPage>()

            for (i in 0 until imagesArray.length()) {
                val img = imagesArray.getJSONObject(i)
                val imgUrl = img.optString("url", "")
                val b2key = img.optString("b2key", "")
                val resolvedUrl = when {
                    imgUrl.isNotEmpty() -> imgUrl
                    b2key.isNotEmpty() -> "https://meo.comick.pictures/$b2key"
                    else -> ""
                }
                if (resolvedUrl.isNotEmpty()) {
                    pages.add(
                        MangaPage(
                            index = i + 1,
                            imageUrl = resolvedUrl,
                            headers = mapOf("Referer" to "https://comick.io/")
                        )
                    )
                }
            }
            if (pages.isEmpty()) throw IOException("No pages found in ComicK chapter")
            pages
        }
    }

    private fun parseComickArray(array: JSONArray): List<Manga> {
        val result = mutableListOf<Manga>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val slug = obj.optString("slug", "")
            val title = obj.optString("title", "Untitled")
            val mdCovers = obj.optJSONArray("md_covers")
            val b2key = mdCovers?.optJSONObject(0)?.optString("b2key") ?: ""
            val coverUrl = if (b2key.isNotEmpty()) "https://meo.comick.pictures/$b2key" else ""

            val genresList = mutableListOf<String>()
            val mdGenres = obj.optJSONArray("md_comic_md_genres")
            if (mdGenres != null) {
                for (j in 0 until mdGenres.length()) {
                    val gName = mdGenres.getJSONObject(j).optJSONObject("md_genres")?.optString("name", "") ?: ""
                    if (gName.isNotEmpty()) genresList.add(gName)
                }
            }

            result.add(
                Manga(
                    id = "comick_$slug",
                    sourceId = source.id,
                    title = title,
                    coverUrl = coverUrl,
                    rating = (obj.optDouble("rating", 9.0) * 1.0).toFloat(),
                    genres = if (genresList.isNotEmpty()) genresList else listOf("Manga"),
                    status = if (obj.optInt("status", 1) == 1) "Ongoing" else "Completed"
                )
            )
        }
        return result
    }

    private fun parseComickDetailJson(json: JSONObject): Manga {
        val slug = json.optString("slug", "")
        val title = json.optString("title", "Webtoon")
        val desc = json.optString("desc", "")
        val mdCovers = json.optJSONArray("md_covers")
        val b2key = mdCovers?.optJSONObject(0)?.optString("b2key") ?: ""
        val coverUrl = if (b2key.isNotEmpty()) "https://meo.comick.pictures/$b2key" else ""

        return Manga(
            id = "comick_$slug",
            sourceId = source.id,
            title = title,
            description = desc,
            coverUrl = coverUrl,
            rating = (json.optDouble("bayesian_rating", 9.0)).toFloat(),
            genres = listOf("Webtoon", "ComicK")
        )
    }
}
