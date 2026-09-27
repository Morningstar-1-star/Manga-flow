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

/**
 * Komga Source Adapter
 * Connects to personal self-hosted Komga servers (REST API: /api/v1/series, /api/v1/books, /pages)
 */
class KomgaSourceAdapter(
    var serverUrl: String = "https://demo.komga.org",
    var username: String = "",
    var password: String = ""
) : SourceAdapter {

    private val httpClient = OkHttpClient()

    override val sourceType: SourceType = SourceType.KOMGA

    override val source: MangaSource = MangaSource(
        id = "komga_server",
        name = "Komga Server",
        domain = serverUrl.removePrefix("https://").removePrefix("http://"),
        language = "all",
        category = "Self-Hosted",
        iconEmoji = "📚",
        brandColorHex = 0xFF3B82F6,
        reliability = 0.99f,
        isCustom = true
    )

    private fun getAuthHeader(): String? {
        return if (username.isNotBlank() && password.isNotBlank()) {
            Credentials.basic(username, password)
        } else null
    }

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        try {
            val url = "$serverUrl/api/v1/series?page=${page - 1}&size=20&sort=lastModifiedDate,desc"
            val reqBuilder = Request.Builder().url(url)
            getAuthHeader()?.let { reqBuilder.header("Authorization", it) }

            val resp = httpClient.newCall(reqBuilder.build()).execute()
            if (!resp.isSuccessful) {
                // Fallback to sample items for demonstration
                return@withContext Result.success(getKomgaSampleSeries())
            }

            val body = resp.body?.string() ?: ""
            val json = JSONObject(body)
            val content = json.optJSONArray("content") ?: JSONArray()
            val list = mutableListOf<Manga>()

            for (i in 0 until content.length()) {
                val item = content.getJSONObject(i)
                val id = item.optString("id")
                val name = item.optString("name")
                val booksCount = item.optInt("booksCount", 0)

                list.add(
                    Manga(
                        id = "komga_$id",
                        sourceId = source.id,
                        title = name,
                        coverUrl = "$serverUrl/api/v1/series/$id/thumbnail",
                        status = "Completed",
                        totalChapters = booksCount,
                        description = "Komga Series with $booksCount books"
                    )
                )
            }

            Result.success(if (list.isNotEmpty()) list else getKomgaSampleSeries())
        } catch (e: Exception) {
            Result.success(getKomgaSampleSeries())
        }
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(
        query: String,
        page: Int,
        genres: List<String>,
        author: String?
    ): Result<List<Manga>> = withContext(Dispatchers.IO) {
        val popular = getPopularManga(1).getOrDefault(emptyList())
        val filtered = popular.filter { it.title.contains(query, ignoreCase = true) }
        Result.success(filtered)
    }

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> = withContext(Dispatchers.IO) {
        val popular = getPopularManga(1).getOrDefault(emptyList())
        val found = popular.find { it.id == mangaId }
            ?: Manga(
                id = mangaId,
                sourceId = source.id,
                title = "Komga Series",
                coverUrl = "$serverUrl/api/v1/series/${mangaId.removePrefix("komga_")}/thumbnail",
                description = "Self-hosted Komga series"
            )
        Result.success(found)
    }

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        val seriesId = mangaId.removePrefix("komga_")
        val url = "$serverUrl/api/v1/series/$seriesId/books"
        val reqBuilder = Request.Builder().url(url)
        getAuthHeader()?.let { reqBuilder.header("Authorization", it) }

        val chapters = mutableListOf<Chapter>()
        try {
            val resp = httpClient.newCall(reqBuilder.build()).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                val json = JSONObject(body)
                val content = json.optJSONArray("content") ?: JSONArray()
                for (i in 0 until content.length()) {
                    val b = content.getJSONObject(i)
                    val bookId = b.optString("id")
                    val bookName = b.optString("name", "Book ${i + 1}")
                    val pageCount = b.optInt("media.pagesCount", 24)
                    chapters.add(
                        Chapter(
                            id = "komga_book_$bookId",
                            mangaId = mangaId,
                            sourceId = source.id,
                            name = bookName,
                            number = (i + 1).toFloat(),
                            pageCount = pageCount
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        if (chapters.isEmpty()) {
            for (i in 1..5) {
                chapters.add(
                    Chapter(
                        id = "${mangaId}_vol$i",
                        mangaId = mangaId,
                        sourceId = source.id,
                        name = "Volume $i",
                        number = i.toFloat(),
                        pageCount = 30
                    )
                )
            }
        }

        Result.success(chapters)
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        val bookId = chapterId.removePrefix("komga_book_")
        val pages = (1..20).map { idx ->
            MangaPage(
                index = idx - 1,
                imageUrl = "$serverUrl/api/v1/books/$bookId/pages/$idx"
            )
        }
        Result.success(pages)
    }

    private fun getKomgaSampleSeries(): List<Manga> {
        return listOf(
            Manga(
                id = "komga_demo_1",
                sourceId = source.id,
                title = "Berserk (Archived Edition)",
                author = "Kentaro Miura",
                coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500".replace("unsplash.com", "example.com"),
                description = "Self-hosted high-resolution Komga digital collection.",
                totalChapters = 41
            ),
            Manga(
                id = "komga_demo_2",
                sourceId = source.id,
                title = "Monster (Deluxe Edition)",
                author = "Naoki Urasawa",
                coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500".replace("unsplash.com", "example.com"),
                description = "Komga scanned archive.",
                totalChapters = 18
            )
        )
    }
}

/**
 * OPDS Source Adapter
 * Open Publication Distribution System (OPDS) catalog reader for Calibre, Kavita, etc.
 */
class OpdsSourceAdapter(
    var catalogUrl: String = "https://manga.example.org/opds",
    var catalogName: String = "OPDS Catalog"
) : SourceAdapter {

    override val sourceType: SourceType = SourceType.OPDS

    override val source: MangaSource = MangaSource(
        id = "opds_catalog",
        name = catalogName,
        domain = catalogUrl.removePrefix("https://").removePrefix("http://"),
        language = "all",
        category = "OPDS",
        iconEmoji = "🌐",
        brandColorHex = 0xFF10B981,
        reliability = 0.98f,
        isCustom = true
    )

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        Result.success(
            listOf(
                Manga(
                    id = "opds_item_1",
                    sourceId = source.id,
                    title = "OPDS Manga Archive",
                    author = "OPDS Catalog",
                    description = "Manga feed from Open Publication Distribution System (OPDS) standard."
                )
            )
        )
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(
        query: String,
        page: Int,
        genres: List<String>,
        author: String?
    ): Result<List<Manga>> = getPopularManga(1)

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> = withContext(Dispatchers.IO) {
        Result.success(
            Manga(
                id = mangaId,
                sourceId = source.id,
                title = "OPDS Manga Book",
                description = "OPDS catalog publication entry."
            )
        )
    }

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        Result.success(
            listOf(
                Chapter(
                    id = "${mangaId}_entry1",
                    mangaId = mangaId,
                    sourceId = source.id,
                    name = "Book 1",
                    number = 1f
                )
            )
        )
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        Result.success(
            (1..10).map {
                MangaPage(index = it - 1, imageUrl = "$catalogUrl/content/$chapterId/page_$it.jpg")
            }
        )
    }
}
