package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga WHERE inLibrary = 1 ORDER BY lastReadTime DESC")
    fun getLibraryManga(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga WHERE inLibrary = 1 ORDER BY lastReadTime DESC")
    suspend fun getAllLibraryMangaSync(): List<MangaEntity>

    @Query("SELECT * FROM manga WHERE inLibrary = 1 AND category = :category ORDER BY lastReadTime DESC")
    fun getLibraryMangaByCategory(category: String): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga WHERE id = :id LIMIT 1")
    fun getMangaById(id: String): Flow<MangaEntity?>

    @Query("SELECT * FROM manga WHERE id = :id LIMIT 1")
    suspend fun getMangaByIdSync(id: String): MangaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(manga: MangaEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(mangaList: List<MangaEntity>)

    @Update
    suspend fun update(manga: MangaEntity)

    @Query("UPDATE manga SET inLibrary = :inLibrary, category = :category WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, inLibrary: Boolean, category: String)

    @Query("UPDATE manga SET lastReadChapterId = :chapterId, lastReadChapterName = :chapterName, lastReadPage = :page, lastReadTime = :time, readProgressPercent = :progress WHERE id = :mangaId")
    suspend fun updateReadingProgress(mangaId: String, chapterId: String, chapterName: String, page: Int, time: Long, progress: Int)

    @Query("UPDATE manga SET inLibrary = 0, category = 'None'")
    suspend fun clearLibrary()
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId ORDER BY number ASC")
    fun getChaptersForManga(mangaId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId AND language = :language ORDER BY number ASC")
    fun getChaptersForMangaByLanguage(mangaId: String, language: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("UPDATE chapters SET isRead = :isRead WHERE id = :chapterId")
    suspend fun markChapterRead(chapterId: String, isRead: Boolean)

    @Query("UPDATE chapters SET isRead = 1 WHERE mangaId = :mangaId AND number <= :number")
    suspend fun markPreviousChaptersRead(mangaId: String, number: Float)

    @Query("UPDATE chapters SET isBookmarked = :isBookmarked WHERE id = :chapterId")
    suspend fun toggleChapterBookmark(chapterId: String, isBookmarked: Boolean)

    @Query("UPDATE chapters SET isDownloaded = :isDownloaded, downloadProgress = :progress WHERE id = :chapterId")
    suspend fun updateDownloadStatus(chapterId: String, isDownloaded: Boolean, progress: Int)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    suspend fun getAllHistorySync(): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE mangaId = :mangaId")
    suspend fun deleteHistory(mangaId: String)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    suspend fun getAllBookmarksSync(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE mangaId = :mangaId ORDER BY pageNumber ASC")
    fun getBookmarksForManga(mangaId: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(task: DownloadEntity)

    @Query("UPDATE downloads SET status = :status, progress = :progress, speedText = :speed, etaText = :eta WHERE id = :id")
    suspend fun updateProgress(id: String, status: String, progress: Float, speed: String, eta: String)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("UPDATE downloads SET status = 'PAUSED' WHERE status = 'DOWNLOADING'")
    suspend fun pauseAll()

    @Query("UPDATE downloads SET status = 'DOWNLOADING' WHERE status = 'PAUSED'")
    suspend fun resumeAll()
}

@Dao
interface SourceConfigDao {
    @Query("SELECT * FROM source_configs")
    fun getSourceConfigs(): Flow<List<SourceConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: SourceConfigEntity)

    @Query("UPDATE source_configs SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(id: String, isPinned: Boolean)

    @Query("UPDATE source_configs SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleEnable(id: String, isEnabled: Boolean)

    @Query("DELETE FROM source_configs WHERE id = :id")
    suspend fun deleteConfig(id: String)
}
