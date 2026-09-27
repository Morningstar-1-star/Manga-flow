package com.example.data.repository

import com.example.data.local.BookmarkEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.DownloadEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.KotatsuDatabase
import com.example.data.local.MangaEntity
import com.example.data.model.Bookmark
import com.example.data.model.Chapter
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTask
import com.example.data.model.HistoryItem
import com.example.data.model.Manga
import com.example.data.model.MangaPage
import com.example.data.model.MangaSource
import com.example.data.sources.SourceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MangaRepository(
    private val database: KotatsuDatabase,
    val sourceManager: SourceManager
) {
    private val mangaDao = database.mangaDao()
    private val chapterDao = database.chapterDao()
    private val historyDao = database.historyDao()
    private val bookmarkDao = database.bookmarkDao()
    private val downloadDao = database.downloadDao()

    fun getSources(): Flow<List<MangaSource>> = sourceManager.getSourcesFlow()

    fun getLibraryManga(category: String? = null): Flow<List<Manga>> {
        val flow = if (category == null || category == "All") {
            mangaDao.getLibraryManga()
        } else {
            mangaDao.getLibraryMangaByCategory(category)
        }
        return flow.map { list -> list.map { it.toManga() } }
    }

    fun getHistory(): Flow<List<HistoryItem>> {
        return historyDao.getHistory().map { list ->
            list.map {
                HistoryItem(
                    mangaId = it.mangaId,
                    title = it.title,
                    coverUrl = it.coverUrl,
                    chapterId = it.chapterId,
                    chapterName = it.chapterName,
                    readPage = it.readPage,
                    totalPages = it.totalPages,
                    timestamp = it.timestamp,
                    progressPercent = it.progressPercent,
                    isCompleted = it.isCompleted
                )
            }
        }
    }

    fun getBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { list ->
            list.map {
                Bookmark(
                    id = it.id,
                    mangaId = it.mangaId,
                    mangaTitle = it.mangaTitle,
                    chapterId = it.chapterId,
                    chapterName = it.chapterName,
                    coverUrl = it.coverUrl,
                    pageNumber = it.pageNumber,
                    timestamp = it.timestamp
                )
            }
        }
    }

    fun getDownloads(): Flow<List<DownloadTask>> {
        return downloadDao.getAllDownloads().map { list ->
            list.map {
                DownloadTask(
                    id = it.id,
                    mangaId = it.mangaId,
                    chapterId = it.chapterId,
                    mangaTitle = it.mangaTitle,
                    chapterName = it.chapterName,
                    coverUrl = it.coverUrl,
                    progress = it.progress,
                    status = try { DownloadStatus.valueOf(it.status) } catch (e: Exception) { DownloadStatus.DOWNLOADING },
                    speedText = it.speedText,
                    etaText = it.etaText,
                    downloadedChapters = it.downloadedChapters
                )
            }
        }
    }

    fun getChapters(mangaId: String, language: String? = null): Flow<List<Chapter>> {
        val flow = if (language == null || language == "all") {
            chapterDao.getChaptersForManga(mangaId)
        } else {
            chapterDao.getChaptersForMangaByLanguage(mangaId, language)
        }
        return flow.map { list -> list.map { it.toChapter() } }
    }

    fun getMangaDetails(mangaId: String): Flow<Manga?> {
        return mangaDao.getMangaById(mangaId).map { it?.toManga() }
    }

    suspend fun fetchAndStoreMangaDetails(mangaId: String, sourceId: String): Manga {
        val parser = sourceManager.getParser(sourceId)
        val onlineManga = parser.getMangaDetails(mangaId).getOrNull()
        val local = mangaDao.getMangaByIdSync(mangaId)

        val merged = if (onlineManga != null) {
            val entity = onlineManga.toEntity().copy(
                inLibrary = local?.inLibrary ?: false,
                category = local?.category ?: "None",
                lastReadChapterId = local?.lastReadChapterId,
                lastReadChapterName = local?.lastReadChapterName,
                lastReadPage = local?.lastReadPage ?: 0,
                lastReadTime = local?.lastReadTime ?: 0L,
                readProgressPercent = local?.readProgressPercent ?: 0
            )
            mangaDao.insertOrUpdate(entity)
            entity.toManga()
        } else {
            local?.toManga() ?: parser.getPopularManga().getOrThrow().first()
        }

        // Fetch online chapters
        val onlineChapters = parser.getChapters(mangaId).getOrDefault(emptyList())
        if (onlineChapters.isNotEmpty()) {
            chapterDao.insertChapters(onlineChapters.map { it.toEntity() })
        }
        return merged
    }

    suspend fun getPages(sourceId: String, chapterId: String): List<MangaPage> {
        val parser = sourceManager.getParser(sourceId)
        return parser.getPages(chapterId).getOrDefault(emptyList())
    }

    suspend fun updateReadingProgress(
        mangaId: String,
        mangaTitle: String,
        coverUrl: String,
        chapterId: String,
        chapterName: String,
        page: Int,
        totalPages: Int
    ) {
        val percent = if (totalPages > 0) ((page.toFloat() / totalPages) * 100).toInt() else 0
        val isCompleted = page >= totalPages
        val now = System.currentTimeMillis()

        mangaDao.updateReadingProgress(mangaId, chapterId, chapterName, page, now, percent)
        chapterDao.markChapterRead(chapterId, isCompleted)

        historyDao.upsertHistory(
            HistoryEntity(
                mangaId = mangaId,
                title = mangaTitle,
                coverUrl = coverUrl,
                chapterId = chapterId,
                chapterName = chapterName,
                readPage = page,
                totalPages = totalPages,
                timestamp = now,
                progressPercent = percent,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun updateFavorite(mangaId: String, inLibrary: Boolean, category: String) {
        mangaDao.updateFavoriteStatus(mangaId, inLibrary, category)
    }

    suspend fun toggleChapterBookmark(chapterId: String, isBookmarked: Boolean, mangaId: String, mangaTitle: String, coverUrl: String, chapterName: String, page: Int) {
        chapterDao.toggleChapterBookmark(chapterId, isBookmarked)
        if (isBookmarked) {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    id = "${mangaId}_${chapterId}_$page",
                    mangaId = mangaId,
                    mangaTitle = mangaTitle,
                    chapterId = chapterId,
                    chapterName = chapterName,
                    coverUrl = coverUrl,
                    pageNumber = page
                )
            )
        } else {
            bookmarkDao.deleteBookmark("${mangaId}_${chapterId}_$page")
        }
    }

    suspend fun enqueueDownload(
        mangaId: String,
        chapterId: String,
        mangaTitle: String,
        chapterName: String,
        coverUrl: String
    ) {
        downloadDao.insertOrUpdate(
            DownloadEntity(
                id = "${mangaId}_$chapterId",
                mangaId = mangaId,
                chapterId = chapterId,
                mangaTitle = mangaTitle,
                chapterName = chapterName,
                coverUrl = coverUrl,
                progress = 0.1f,
                status = DownloadStatus.DOWNLOADING.name,
                speedText = "2.8 MB/s",
                etaText = "In 2 minutes"
            )
        )
        chapterDao.updateDownloadStatus(chapterId, isDownloaded = false, progress = 10)
    }

    suspend fun pauseAllDownloads() = downloadDao.pauseAll()
    suspend fun resumeAllDownloads() = downloadDao.resumeAll()
    suspend fun cancelDownload(id: String) = downloadDao.deleteDownload(id)

    suspend fun clearHistory() = historyDao.clearHistory()
    suspend fun clearLibrary() = mangaDao.clearLibrary()

    suspend fun getAllLibraryMangaSync(): List<MangaEntity> = mangaDao.getAllLibraryMangaSync()
    suspend fun getAllHistorySync(): List<HistoryEntity> = historyDao.getAllHistorySync()
    suspend fun getAllBookmarksSync(): List<BookmarkEntity> = bookmarkDao.getAllBookmarksSync()

    suspend fun insertMangaEntities(entities: List<MangaEntity>) {
        entities.forEach { mangaDao.insertOrUpdate(it) }
    }

    suspend fun insertHistoryEntities(entities: List<HistoryEntity>) {
        entities.forEach { historyDao.upsertHistory(it) }
    }

    suspend fun insertBookmarkEntities(entities: List<BookmarkEntity>) {
        entities.forEach { bookmarkDao.insertBookmark(it) }
    }

    private fun MangaEntity.toManga(): Manga = Manga(
        id = id,
        sourceId = sourceId,
        title = title,
        altTitle = altTitle,
        author = author,
        artist = artist,
        description = description,
        coverUrl = coverUrl,
        status = status,
        rating = rating,
        isNsfw = isNsfw,
        genres = if (genresString.isNotEmpty()) genresString.split(",") else emptyList(),
        category = category,
        inLibrary = inLibrary,
        lastReadChapterId = lastReadChapterId,
        lastReadChapterName = lastReadChapterName,
        lastReadPage = lastReadPage,
        lastReadTime = lastReadTime,
        totalChapters = totalChapters,
        readProgressPercent = readProgressPercent
    )

    private fun Manga.toEntity(): MangaEntity = MangaEntity(
        id = id,
        sourceId = sourceId,
        title = title,
        altTitle = altTitle,
        author = author,
        artist = artist,
        description = description,
        coverUrl = coverUrl,
        status = status,
        rating = rating,
        isNsfw = isNsfw,
        genresString = genres.joinToString(","),
        category = category,
        inLibrary = inLibrary,
        lastReadChapterId = lastReadChapterId,
        lastReadChapterName = lastReadChapterName,
        lastReadPage = lastReadPage,
        lastReadTime = lastReadTime,
        totalChapters = totalChapters,
        readProgressPercent = readProgressPercent
    )

    private fun ChapterEntity.toChapter(): Chapter = Chapter(
        id = id,
        mangaId = mangaId,
        sourceId = sourceId,
        name = name,
        number = number,
        volume = volume,
        scanlator = scanlator,
        dateUpload = dateUpload,
        language = language,
        isRead = isRead,
        isBookmarked = isBookmarked,
        isDownloaded = isDownloaded,
        downloadProgress = downloadProgress,
        pageCount = pageCount,
        lastPageRead = lastPageRead
    )

    private fun Chapter.toEntity(): ChapterEntity = ChapterEntity(
        id = id,
        mangaId = mangaId,
        sourceId = sourceId,
        name = name,
        number = number,
        volume = volume,
        scanlator = scanlator,
        dateUpload = dateUpload,
        language = language,
        isRead = isRead,
        isBookmarked = isBookmarked,
        isDownloaded = isDownloaded,
        downloadProgress = downloadProgress,
        pageCount = pageCount,
        lastPageRead = lastPageRead
    )
}
