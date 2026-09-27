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
        Result.success(
            listOf(
                Manga(
                    id = "cuutruyen_frieren",
                    sourceId = source.id,
                    title = "Sousou no Frieren",
                    altTitle = "Pháp Sư Tiễn Táng",
                    author = "Yamada Kanehito",
                    description = "Câu chuyện về cuộc hành trình của pháp sư elf Frieren sau khi đánh bại Ma Vương.",
                    coverUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80",
                    status = "Ongoing",
                    rating = 9.8f,
                    genres = listOf("Fantasy", "Adventure", "Drama")
                ),
                Manga(
                    id = "cuutruyen_dungeon_meshi",
                    sourceId = source.id,
                    title = "Dungeon Meshi",
                    altTitle = "Mỹ Vị Hầm Ngục",
                    author = "Kui Ryoko",
                    description = "Nấu ăn trong hầm ngục để cứu em gái khỏi bụng rồng đỏ.",
                    coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80",
                    status = "Finished",
                    rating = 9.6f,
                    genres = listOf("Cooking", "Fantasy", "Comedy")
                )
            )
        )
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
            "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1563089145-599997674d42?w=1200&auto=format&fit=crop&q=80"
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
        Result.success(
            listOf(
                Manga(
                    id = "truyengg_solo_leveling",
                    sourceId = source.id,
                    title = "Solo Leveling: Ragnarok",
                    altTitle = "Thăng Cấp Một Mình",
                    author = "Chugong",
                    description = "Hành trình của con trai Sung Jin-Woo bước vào thế giới thợ săn mới.",
                    coverUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
                    status = "Ongoing",
                    rating = 9.7f,
                    genres = listOf("Action", "Fantasy", "Manhwa", "Webtoon")
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
            "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=1200&auto=format&fit=crop&q=80"
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
        Result.success(
            listOf(
                Manga(
                    id = "dt3q_omniscent",
                    sourceId = source.id,
                    title = "Omniscient Reader's Viewpoint",
                    altTitle = "Toàn Trí Độc Giả",
                    author = "Sing Shong",
                    description = "Thế giới đột nhiên biến thành tiểu thuyết mà chỉ có Kim Dokja đọc đến chương cuối.",
                    coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
                    status = "Ongoing",
                    rating = 9.9f,
                    genres = listOf("Action", "Apocalypse", "Fantasy", "Webtoon")
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
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1563089145-599997674d42?w=1200&auto=format&fit=crop&q=80"
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
        Result.success(
            listOf(
                Manga(
                    id = "batoto_villainess",
                    sourceId = source.id,
                    title = "Death Is the Only Ending for the Villainess",
                    altTitle = "Akuyaku Reijou",
                    author = "Gwon Gyeoeul",
                    description = "Reincarnated into an otome game as the doomed villainess Penelope Eckart.",
                    coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&auto=format&fit=crop&q=80",
                    status = "Ongoing",
                    rating = 9.8f,
                    genres = listOf("Otome", "Romance", "Fantasy", "Drama")
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
            "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=1200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200&auto=format&fit=crop&q=80"
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
                    id = "local_cbz_sample",
                    sourceId = source.id,
                    title = "Offline Archive (CBZ/ZIP)",
                    altTitle = "Local Comic Book",
                    author = "Local",
                    description = "Imported CBZ and ZIP comic archives stored locally on device.",
                    coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
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
            "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=1200&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1200&auto=format&fit=crop&q=80"
        )
        Result.success(sampleUrls.mapIndexed { idx, url -> MangaPage(idx + 1, url) })
    }
}
