package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DownloadStatus

@Entity(tableName = "manga")
data class MangaEntity(
    @PrimaryKey val id: String,
    val sourceId: String,
    val title: String,
    val altTitle: String = "",
    val author: String = "Unknown",
    val artist: String = "",
    val description: String = "",
    val coverUrl: String = "",
    val status: String = "Ongoing",
    val rating: Float = 0f,
    val isNsfw: Boolean = false,
    val genresString: String = "", // Comma-separated
    val category: String = "None", // Reading, Completed, On-hold, Plan to read, Dropped
    val inLibrary: Boolean = false,
    val lastReadChapterId: String? = null,
    val lastReadChapterName: String? = null,
    val lastReadPage: Int = 0,
    val lastReadTime: Long = 0L,
    val totalChapters: Int = 0,
    val readProgressPercent: Int = 0,
    val preferredReadMode: String = "WEBTOON"
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val sourceId: String,
    val name: String,
    val number: Float = 0f,
    val volume: String = "",
    val scanlator: String = "",
    val dateUpload: String = "",
    val language: String = "en",
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadProgress: Int = 0,
    val pageCount: Int = 0,
    val lastPageRead: Int = 0
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val mangaId: String,
    val title: String,
    val coverUrl: String,
    val chapterId: String,
    val chapterName: String,
    val readPage: Int,
    val totalPages: Int,
    val timestamp: Long,
    val progressPercent: Int,
    val isCompleted: Boolean = false
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val mangaTitle: String,
    val chapterId: String,
    val chapterName: String,
    val coverUrl: String,
    val pageNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val chapterId: String,
    val mangaTitle: String,
    val chapterName: String,
    val coverUrl: String,
    val progress: Float = 0f,
    val status: String = DownloadStatus.DOWNLOADING.name,
    val speedText: String = "2.3 MB/s",
    val etaText: String = "In 3 minutes",
    val downloadedChapters: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "source_configs")
data class SourceConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isPinned: Boolean = false,
    val isEnabled: Boolean = true,
    val language: String = "en"
)

@Entity(tableName = "pages", primaryKeys = ["chapterId", "pageIndex"])
data class PageEntity(
    val chapterId: String,
    val pageIndex: Int,
    val imageUrl: String,
    val headersJson: String = "",
    val localFilePath: String? = null
)

@Entity(tableName = "translation_cache")
data class TranslationCacheEntity(
    @PrimaryKey val cacheKey: String,
    val pageIndex: Int,
    val sourceLanguage: String,
    val targetLanguage: String,
    val bubblesJson: String,
    val fullSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val chapterId: String,
    val lastPageRead: Int,
    val totalPages: Int,
    val progressPercent: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
