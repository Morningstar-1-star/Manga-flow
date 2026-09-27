package com.example.data.sources

import com.example.data.model.Chapter
import com.example.data.model.Manga
import com.example.data.model.MangaPage
import com.example.data.model.MangaSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CuuTruyenSource : MangaSourceParser {
    override val source = MangaSource(
        id = "cuutruyen",
        name = "Cứu Truyện",
        domain = "cuutruyen.net",
        isPinned = true,
        isEnabled = true,
        language = "vi",
        iconEmoji = "🐬",
        brandColorHex = 0xFF0284C7,
        reliability = 0.99f
    )

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        Result.success(SourceCatalogDataProvider.getMangaForSource(source.id, source.name, "Manga", "vi"))
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(query: String, page: Int, genres: List<String>, author: String?): Result<List<Manga>> =
        Result.success(getPopularManga(page).getOrDefault(emptyList()).filter { it.title.contains(query, ignoreCase = true) })

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> =
        Result.success(getPopularManga().getOrDefault(emptyList()).firstOrNull { it.id == mangaId } ?: getPopularManga().getOrThrow().first())

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        Result.success(
            (1..25).map { num ->
                Chapter(
                    id = "${mangaId}_ch_$num",
                    mangaId = mangaId,
                    sourceId = source.id,
                    name = "Chương $num",
                    number = num.toFloat(),
                    scanlator = "Cứu Truyện Team",
                    dateUpload = "2024-05-12",
                    language = "vi",
                    pageCount = 22
                )
            }
        )
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        val sampleUrls = listOf(
            "https://cdn.myanimelist.net/images/manga/2/258237.jpg",
            "https://cdn.myanimelist.net/images/manga/1/259070.jpg",
            "https://cdn.myanimelist.net/images/manga/3/54525.jpg"
        )
        Result.success(sampleUrls.mapIndexed { idx, url -> MangaPage(idx + 1, url) })
    }
}

class TruyenGGSource : MangaSourceParser {
    override val source = MangaSource(
        id = "truyengg",
        name = "TruyenGG",
        domain = "truyengg.com",
        isPinned = true,
        isEnabled = true,
        language = "vi",
        iconEmoji = "🦊",
        brandColorHex = 0xFFF97316,
        reliability = 0.95f
    )

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        Result.success(SourceCatalogDataProvider.getMangaForSource(source.id, source.name, "Manga", "vi"))
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(query: String, page: Int, genres: List<String>, author: String?): Result<List<Manga>> =
        Result.success(getPopularManga(page).getOrDefault(emptyList()).filter { it.title.contains(query, ignoreCase = true) })

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> =
        Result.success(getPopularManga().getOrDefault(emptyList()).first())

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        Result.success(
            (1..30).map { num ->
                Chapter(
                    id = "${mangaId}_ch_$num",
                    mangaId = mangaId,
                    sourceId = source.id,
                    name = "Chapter $num",
                    number = num.toFloat(),
                    scanlator = "TruyenGG Scans",
                    dateUpload = "2024-06-18",
                    language = "vi",
                    pageCount = 25
                )
            }
        )
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        val sampleUrls = listOf(
            "https://cdn.myanimelist.net/images/manga/3/232056.jpg",
            "https://cdn.myanimelist.net/images/manga/3/258224.jpg"
        )
        Result.success(sampleUrls.mapIndexed { idx, url -> MangaPage(idx + 1, url) })
    }
}

class DocTruyen3QSource : MangaSourceParser {
    override val source = MangaSource(
        id = "doctruyen3q",
        name = "DocTruyen3Q",
        domain = "doctruyen3q.com",
        isPinned = true,
        isEnabled = true,
        language = "vi",
        iconEmoji = "🅰️",
        brandColorHex = 0xFFF59E0B,
        reliability = 0.94f
    )

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        Result.success(SourceCatalogDataProvider.getMangaForSource(source.id, source.name, "Manga", "vi"))
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(query: String, page: Int, genres: List<String>, author: String?): Result<List<Manga>> =
        Result.success(getPopularManga(page).getOrDefault(emptyList()).filter { it.title.contains(query, ignoreCase = true) })

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> =
        Result.success(getPopularManga().getOrDefault(emptyList()).first())

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        Result.success(
            (1..20).map { num ->
                Chapter(
                    id = "${mangaId}_ch_$num",
                    mangaId = mangaId,
                    sourceId = source.id,
                    name = "Chương $num",
                    number = num.toFloat(),
                    scanlator = "DocTruyen3Q",
                    dateUpload = "2024-04-10",
                    language = "vi",
                    pageCount = 20
                )
            }
        )
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        val sampleUrls = listOf(
            "https://cdn.myanimelist.net/images/manga/3/222299.jpg",
            "https://cdn.myanimelist.net/images/manga/2/253119.jpg"
        )
        Result.success(sampleUrls.mapIndexed { idx, url -> MangaPage(idx + 1, url) })
    }
}

class BatoToSource : MangaSourceParser {
    override val source = MangaSource(
        id = "batoto",
        name = "Bato.To",
        domain = "bato.to",
        isPinned = false,
        isEnabled = true,
        language = "all",
        iconEmoji = "🅱️",
        brandColorHex = 0xFF0D9488,
        reliability = 0.96f
    )

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        Result.success(SourceCatalogDataProvider.getMangaForSource(source.id, source.name, "Manga", "en"))
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(query: String, page: Int, genres: List<String>, author: String?): Result<List<Manga>> =
        Result.success(getPopularManga(page).getOrDefault(emptyList()).filter { it.title.contains(query, ignoreCase = true) })

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> =
        Result.success(getPopularManga().getOrDefault(emptyList()).first())

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        Result.success(
            (1..20).map { num ->
                Chapter(
                    id = "${mangaId}_ch_$num",
                    mangaId = mangaId,
                    sourceId = source.id,
                    name = "Chapter $num",
                    number = num.toFloat(),
                    scanlator = "Bato Scans",
                    dateUpload = "2024-03-15",
                    language = "en",
                    pageCount = 22
                )
            }
        )
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        val sampleUrls = listOf(
            "https://cdn.myanimelist.net/images/manga/3/188896.jpg",
            "https://cdn.myanimelist.net/images/manga/2/258237.jpg"
        )
        Result.success(sampleUrls.mapIndexed { idx, url -> MangaPage(idx + 1, url) })
    }
}

class LocalStorageSource : MangaSourceParser {
    override val source = MangaSource(
        id = "local",
        name = "Local Storage",
        domain = "local.device",
        isPinned = false,
        isEnabled = true,
        language = "local",
        iconEmoji = "💾",
        brandColorHex = 0xFF10B981,
        reliability = 1.0f
    )

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        Result.success(
            listOf(
                Manga(
                    id = "local_imported_comic",
                    sourceId = source.id,
                    title = "Offline Comic Archive",
                    altTitle = "Local Comic Book",
                    author = "Local",
                    description = "Imported CBZ and ZIP comic archives stored locally on device.",
                    coverUrl = "https://cdn.myanimelist.net/images/manga/1/259070.jpg",
                    status = "Finished",
                    rating = 10f,
                    genres = listOf("CBZ", "Local", "Offline")
                )
            )
        )
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(query: String, page: Int, genres: List<String>, author: String?): Result<List<Manga>> =
        Result.success(getPopularManga(page).getOrDefault(emptyList()).filter { it.title.contains(query, ignoreCase = true) })

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> =
        Result.success(getPopularManga().getOrDefault(emptyList()).first())

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        Result.success(
            listOf(
                Chapter(
                    id = "local_ch_1",
                    mangaId = mangaId,
                    sourceId = source.id,
                    name = "Volume 1 Archive",
                    number = 1f,
                    scanlator = "Local File",
                    dateUpload = "Today",
                    language = "en",
                    pageCount = 15,
                    isDownloaded = true
                )
            )
        )
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        val sampleUrls = listOf(
            "https://cdn.myanimelist.net/images/manga/3/54525.jpg",
            "https://cdn.myanimelist.net/images/manga/3/232056.jpg",
            "https://cdn.myanimelist.net/images/manga/3/258224.jpg"
        )
        Result.success(sampleUrls.mapIndexed { idx, url -> MangaPage(idx + 1, url) })
    }
}

class GenericMangaSourceParser(override val source: MangaSource) : MangaSourceParser {

    override suspend fun getPopularManga(page: Int): Result<List<Manga>> = withContext(Dispatchers.IO) {
        val items = SourceCatalogDataProvider.getMangaForSource(
            sourceId = source.id,
            sourceName = source.name,
            category = source.category,
            language = source.language
        )
        Result.success(items)
    }

    override suspend fun getLatestManga(page: Int): Result<List<Manga>> = getPopularManga(page)

    override suspend fun searchManga(query: String, page: Int, genres: List<String>, author: String?): Result<List<Manga>> = withContext(Dispatchers.IO) {
        val catalog = getPopularManga(page).getOrDefault(emptyList())
        val filtered = if (query.isBlank()) {
            catalog
        } else {
            catalog.filter { manga ->
                manga.title.contains(query, ignoreCase = true) ||
                manga.altTitle.contains(query, ignoreCase = true) ||
                manga.author.contains(query, ignoreCase = true) ||
                manga.genres.any { it.contains(query, ignoreCase = true) }
            }
        }
        Result.success(filtered)
    }

    override suspend fun getMangaDetails(mangaId: String): Result<Manga> = withContext(Dispatchers.IO) {
        val items = getPopularManga().getOrDefault(emptyList())
        val found = items.firstOrNull { it.id == mangaId }
            ?: items.firstOrNull()
            ?: Manga(
                id = mangaId,
                sourceId = source.id,
                title = mangaId.substringAfterLast("_").replace("_", " ").replaceFirstChar { it.uppercase() },
                author = source.name,
                description = "Manga from ${source.name}",
                coverUrl = "https://cdn.myanimelist.net/images/manga/3/222299.jpg"
            )
        Result.success(found)
    }

    override suspend fun getChapters(mangaId: String, language: String?): Result<List<Chapter>> = withContext(Dispatchers.IO) {
        val chapters = SourceCatalogDataProvider.getChaptersForManga(
            mangaId = mangaId,
            sourceId = source.id,
            sourceName = source.name,
            language = language ?: source.language
        )
        Result.success(chapters)
    }

    override suspend fun getPages(chapterId: String): Result<List<MangaPage>> = withContext(Dispatchers.IO) {
        val pages = SourceCatalogDataProvider.getPagesForChapter(chapterId)
        Result.success(pages)
    }
}
