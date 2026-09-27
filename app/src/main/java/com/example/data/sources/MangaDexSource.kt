package com.example.data.sources

import com.example.data.model.Chapter
import com.example.data.model.Manga
import com.example.data.model.MangaPage
import com.example.data.model.MangaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MangaDexSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) : SourceAdapter {

    override val sourceType: SourceType = SourceType.API_SOURCE

    override val source = MangaSource(
        id = "mangadex",
        name = "MangaDex",
        domain = "mangadex.org",
        isPinned = true,
        isEnabled = true,
        language = "all",
        isNsfw = false,
        iconEmoji = "🐱",
        brandColorHex = 0xFFFF6740,
        reliability = 0.99f
    )

    private val baseUrl = "https://api.mangadex.org"

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        runCatching {
            val offset = (page - 1) * 25
            val url = "$baseUrl/manga?limit=25&offset=$offset&includes[]=cover_art&includes[]=author&order[followedCount]=desc&contentRating[]=safe&contentRating[]=suggestive"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("MangaDex API error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty response from MangaDex")
            parseMangaList(body)
        }
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        runCatching {
            val offset = (page - 1) * 25
            val url = "$baseUrl/manga?limit=25&offset=$offset&includes[]=cover_art&includes[]=author&order[latestUploadedChapter]=desc&contentRating[]=safe&contentRating[]=suggestive"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("MangaDex API error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty response from MangaDex")
            parseMangaList(body)
        }
    }

    override suspend fun searchManga(
        query: String,
        page: Int,
        genres: List<String>,
        author: String?
    ): Result<List<Manga>> = withContext(Dispatchers.IO) {
        runCatching {
            val offset = (page - 1) * 25
            val encodedQuery = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val url = if (encodedQuery.isBlank()) {
                "$baseUrl/manga?limit=25&offset=$offset&includes[]=cover_art&includes[]=author&order[followedCount]=desc&contentRating[]=safe&contentRating[]=suggestive"
            } else {
                "$baseUrl/manga?title=$encodedQuery&limit=25&offset=$offset&includes[]=cover_art&includes[]=author&contentRating[]=safe&contentRating[]=suggestive"
            }
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("MangaDex search error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty response from MangaDex")
            parseMangaList(body)
        }
    }

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanId = mangaId.removePrefix("mangadex_")
            val url = "$baseUrl/manga/$cleanId?includes[]=cover_art&includes[]=author&includes[]=artist"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("MangaDex detail error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty response from MangaDex")
            val json = JSONObject(body).getJSONObject("data")
            parseMangaJson(json)
        }
    }

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanId = mangaId.removePrefix("mangadex_")
            val langParam = if (!language.isNullOrBlank() && language != "all") {
                "&translatedLanguage[]=$language"
            } else {
                "&translatedLanguage[]=en"
            }
            val url = "$baseUrl/manga/$cleanId/feed?limit=100&order[chapter]=asc$langParam"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("MangaDex chapters error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty response from MangaDex")
            val json = JSONObject(body)
            val dataArray = json.optJSONArray("data") ?: org.json.JSONArray()
            val chapters = mutableListOf<Chapter>()

            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val id = item.getString("id")
                val attr = item.getJSONObject("attributes")
                val chNumStr = attr.optString("chapter", (i + 1).toString())
                val chNum = chNumStr.toFloatOrNull() ?: (i + 1).toFloat()
                val vol = attr.optString("volume", "")
                val title = attr.optString("title", "")
                val lang = attr.optString("translatedLanguage", "en")
                val date = attr.optString("publishAt", "Recent").take(10)

                val formattedName = buildString {
                    if (vol.isNotEmpty()) append("Vol. $vol ")
                    append("Ch. $chNumStr")
                    if (title.isNotEmpty()) append(": $title")
                }

                chapters.add(
                    Chapter(
                        id = id,
                        mangaId = mangaId,
                        sourceId = source.id,
                        name = formattedName,
                        number = chNum,
                        volume = if (vol.isNotEmpty()) "Volume $vol" else "",
                        scanlator = "MangaDex",
                        dateUpload = date,
                        language = lang,
                        pageCount = attr.optInt("pages", 0)
                    )
                )
            }
            if (chapters.isEmpty()) {
                // If specific language is empty, try fallback to all languages
                val fallbackUrl = "$baseUrl/manga/$cleanId/feed?limit=100&order[chapter]=asc"
                val fallbackReq = Request.Builder().url(fallbackUrl).header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)").build()
                val fallbackResp = client.newCall(fallbackReq).execute()
                if (fallbackResp.isSuccessful) {
                    val fBody = fallbackResp.body?.string()
                    if (fBody != null) {
                        val fData = JSONObject(fBody).optJSONArray("data")
                        if (fData != null) {
                            for (i in 0 until fData.length()) {
                                val item = fData.getJSONObject(i)
                                val id = item.getString("id")
                                val attr = item.getJSONObject("attributes")
                                val chNumStr = attr.optString("chapter", (i + 1).toString())
                                val chNum = chNumStr.toFloatOrNull() ?: (i + 1).toFloat()
                                val title = attr.optString("title", "")
                                chapters.add(
                                    Chapter(
                                        id = id,
                                        mangaId = mangaId,
                                        sourceId = source.id,
                                        name = if (title.isNotEmpty()) "Ch. $chNumStr: $title" else "Chapter $chNumStr",
                                        number = chNum,
                                        scanlator = "MangaDex",
                                        dateUpload = attr.optString("publishAt", "Recent").take(10),
                                        language = attr.optString("translatedLanguage", "en")
                                    )
                                )
                            }
                        }
                    }
                }
            }
            if (chapters.isEmpty()) throw IOException("No chapters found on MangaDex for this title")
            chapters
        }
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$baseUrl/at-home/server/$chapterId"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MangaFlow/2.0 (Android; Kotatsu-Reader)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("MangaDex page server error: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("Empty response from MangaDex server")
            val json = JSONObject(body)
            val base = json.getString("baseUrl")
            val chapterObj = json.getJSONObject("chapter")
            val hash = chapterObj.getString("hash")
            val dataArray = chapterObj.getJSONArray("data")

            val pages = mutableListOf<MangaPage>()
            for (i in 0 until dataArray.length()) {
                val filename = dataArray.getString(i)
                val imgUrl = "$base/data/$hash/$filename"
                pages.add(
                    MangaPage(
                        index = i + 1,
                        imageUrl = imgUrl,
                        headers = mapOf("Referer" to "https://mangadex.org/")
                    )
                )
            }
            if (pages.isEmpty()) throw IOException("No pages returned from MangaDex")
            pages
        }
    }

    private fun parseMangaList(jsonString: String): List<Manga> {
        val list = mutableListOf<Manga>()
        val json = JSONObject(jsonString)
        val dataArray = json.optJSONArray("data") ?: return emptyList()

        for (i in 0 until dataArray.length()) {
            val item = dataArray.getJSONObject(i)
            list.add(parseMangaJson(item))
        }
        return list
    }

    private fun parseMangaJson(item: JSONObject): Manga {
        val id = item.getString("id")
        val attr = item.getJSONObject("attributes")

        val titleMap = attr.optJSONObject("title")
        val title = if (titleMap != null) {
            titleMap.optString("en",
                titleMap.optString("ja-ro",
                    titleMap.optString("en-us",
                        titleMap.optString("ko-ro",
                            titleMap.optString("zh-ro",
                                titleMap.optString("ja",
                                    titleMap.optString("ko",
                                        titleMap.optString("zh",
                                            titleMap.keys().asSequence().firstOrNull()?.let { titleMap.optString(it) } ?: "Manga Title"
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        } else {
            "Manga Title"
        }

        var altTitle = ""
        val altTitlesArray = attr.optJSONArray("altTitles")
        if (altTitlesArray != null && altTitlesArray.length() > 0) {
            val firstAlt = altTitlesArray.getJSONObject(0)
            altTitle = firstAlt.optString("en", firstAlt.optString("ja", firstAlt.optString("ja-ro", "")))
        }

        val descMap = attr.optJSONObject("description")
        val description = if (descMap != null) {
            descMap.optString("en",
                descMap.optString("ja",
                    descMap.keys().asSequence().firstOrNull()?.let { descMap.optString(it) } ?: "Popular manga series on MangaDex."
                )
            )
        } else {
            "Popular manga series on MangaDex."
        }

        val status = attr.optString("status", "Ongoing").replaceFirstChar { it.uppercase() }

        var coverFileName = ""
        var authorName = "MangaDex Artist"
        val rels = item.optJSONArray("relationships")
        if (rels != null) {
            for (j in 0 until rels.length()) {
                val rel = rels.getJSONObject(j)
                val type = rel.optString("type")
                if (type == "cover_art") {
                    coverFileName = rel.optJSONObject("attributes")?.optString("fileName", "") ?: ""
                } else if (type == "author") {
                    val auth = rel.optJSONObject("attributes")?.optString("name", "") ?: ""
                    if (auth.isNotEmpty()) authorName = auth
                }
            }
        }

        val coverUrl = if (coverFileName.isNotEmpty()) {
            "https://uploads.mangadex.org/covers/$id/$coverFileName.256.jpg"
        } else {
            ""
        }

        val tagsArray = attr.optJSONArray("tags")
        val genres = mutableListOf<String>()
        if (tagsArray != null) {
            for (k in 0 until tagsArray.length()) {
                val tag = tagsArray.getJSONObject(k)
                val tagName = tag.optJSONObject("attributes")?.optJSONObject("name")?.optString("en", "") ?: ""
                if (tagName.isNotEmpty()) genres.add(tagName)
            }
        }

        return Manga(
            id = id,
            sourceId = source.id,
            title = title,
            altTitle = altTitle,
            author = authorName,
            description = description,
            coverUrl = coverUrl,
            status = status,
            rating = 8.8f,
            genres = if (genres.isNotEmpty()) genres else listOf("Manga", "Web Comic"),
            totalChapters = 0
        )
    }
}
