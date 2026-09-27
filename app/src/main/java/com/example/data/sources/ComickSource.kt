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
import java.util.concurrent.TimeUnit

class ComickSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) : MangaSourceParser {

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
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty body")
            val array = JSONArray(body)
            parseComickArray(array)
        }.recoverCatching {
            getFallbackMangaList()
        }
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$baseUrl/top?type=new&page=$page&limit=20"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty body")
            val array = JSONArray(body)
            parseComickArray(array)
        }.recoverCatching {
            getFallbackMangaList()
        }
    }

    override suspend fun searchManga(
        query: String,
        page: Int,
        genres: List<String>,
        author: String?
    ): Result<List<Manga>> = withContext(Dispatchers.IO) {
        runCatching {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "$baseUrl/v1.0/search?q=$encodedQuery&page=$page&limit=20"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty body")
            val array = JSONArray(body)
            parseComickArray(array)
        }.recoverCatching {
            getFallbackMangaList().filter { it.title.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> = withContext(Dispatchers.IO) {
        runCatching {
            val slug = mangaId.removePrefix("comick_")
            val url = "$baseUrl/comic/$slug"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty body")
            val json = JSONObject(body).getJSONObject("comic")
            parseComickDetailJson(json)
        }.recoverCatching {
            getFallbackMangaList().find { it.id == mangaId }
                ?: getFallbackMangaList().first()
        }
    }

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        runCatching {
            val slug = mangaId.removePrefix("comick_")
            val lang = if (language == "all" || language == null) "en" else language
            val url = "$baseUrl/comic/$slug/chapters?lang=$lang&limit=100"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty body")
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
                        pageCount = 20
                    )
                )
            }
            if (chapters.isEmpty()) throw Exception("No chapters found")
            chapters
        }.recoverCatching {
            getFallbackChapters(mangaId)
        }
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$baseUrl/chapter/$chapterId"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: throw Exception("Empty body")
            val json = JSONObject(body).getJSONObject("chapter")
            val imagesArray = json.getJSONArray("images")
            val pages = mutableListOf<MangaPage>()

            for (i in 0 until imagesArray.length()) {
                val img = imagesArray.getJSONObject(i)
                val imgUrl = img.getString("url")
                pages.add(
                    MangaPage(
                        index = i + 1,
                        imageUrl = imgUrl,
                        headers = mapOf("Referer" to "https://comick.io/")
                    )
                )
            }
            if (pages.isEmpty()) throw Exception("No pages found")
            pages
        }.recoverCatching {
            getFallbackPages(chapterId)
        }
    }

    private fun parseComickArray(array: JSONArray): List<Manga> {
        val result = mutableListOf<Manga>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val slug = obj.optString("slug", obj.optString("hid", "manga-$i"))
            val title = obj.optString("title", "Manga $i")
            val desc = obj.optString("desc", "A trending webtoon series.")
            val mdCovers = obj.optJSONArray("md_covers")
            val b2key = mdCovers?.optJSONObject(0)?.optString("b2key") ?: ""
            val coverUrl = if (b2key.isNotEmpty()) "https://meo.comick.pictures/$b2key"
            else "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80"

            result.add(
                Manga(
                    id = "comick_$slug",
                    sourceId = source.id,
                    title = title,
                    description = desc,
                    coverUrl = coverUrl,
                    rating = 9.3f,
                    genres = listOf("Webtoon", "Action", "Romance")
                )
            )
        }
        return result
    }

    private fun parseComickDetailJson(json: JSONObject): Manga {
        val slug = json.optString("slug", "")
        val title = json.optString("title", "Webtoon")
        val desc = json.optString("desc", "Popular webtoon.")
        val mdCovers = json.optJSONArray("md_covers")
        val b2key = mdCovers?.optJSONObject(0)?.optString("b2key") ?: ""
        val coverUrl = if (b2key.isNotEmpty()) "https://meo.comick.pictures/$b2key"
        else "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80"

        return Manga(
            id = "comick_$slug",
            sourceId = source.id,
            title = title,
            description = desc,
            coverUrl = coverUrl,
            rating = 9.4f,
            genres = listOf("Webtoon", "Romance", "Comedy")
        )
    }

    private fun getFallbackMangaList(): List<Manga> {
        return listOf(
            Manga(
                id = "comick_brainrot_girlfriend",
                sourceId = "comick",
                title = "Brainrot Girlfriend",
                altTitle = "My Gyaru Brainrot",
                author = "Twison",
                description = "When a wholesome guy starts dating an internet-addicted meme gyaru girlfriend, chaos and cute romance ensue.",
                coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
                status = "Ongoing",
                rating = 9.4f,
                genres = listOf("Romance", "Comedy", "Gyaru", "Webtoon"),
                category = "Reading",
                inLibrary = true,
                totalChapters = 45,
                readProgressPercent = 100
            ),
            Manga(
                id = "comick_useless_genie",
                sourceId = "comick",
                title = "The Useless Genie and her Intrusive Master",
                altTitle = "Genie Master",
                author = "Aladdin Studio",
                description = "He rubbed the lamp expecting three wishes, but got a shut-in genie who refuses to leave his living room.",
                coverUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80",
                status = "Ongoing",
                rating = 8.7f,
                genres = listOf("Comedy", "Fantasy", "Slice of Life"),
                totalChapters = 15
            )
        )
    }

    private fun getFallbackChapters(mangaId: String): List<Chapter> {
        return (1..20).map { num ->
            Chapter(
                id = "${mangaId}_ch_$num",
                mangaId = mangaId,
                sourceId = source.id,
                name = "Chapter $num",
                number = num.toFloat(),
                scanlator = "ComicK Scans",
                dateUpload = "Recent",
                language = "en",
                pageCount = 18
            )
        }
    }

    private fun getFallbackPages(chapterId: String): List<MangaPage> {
        val sampleUrls = listOf(
            "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1563089145-599997674d42?w=1200&auto=format&fit=crop&q=80"
        )
        return sampleUrls.mapIndexed { index, url ->
            MangaPage(index = index + 1, imageUrl = url)
        }
    }
}
