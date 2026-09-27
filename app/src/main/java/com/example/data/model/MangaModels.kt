package com.example.data.model

data class Manga(
    val id: String,
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
    val genres: List<String> = emptyList(),
    val category: String = "None", // None, Reading, Completed, On-hold, Plan to read, Dropped
    val inLibrary: Boolean = false,
    val lastReadChapterId: String? = null,
    val lastReadChapterName: String? = null,
    val lastReadPage: Int = 0,
    val lastReadTime: Long = 0L,
    val totalChapters: Int = 0,
    val readProgressPercent: Int = 0,
    val unreadCount: Int = 0
)

data class Chapter(
    val id: String,
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

data class MangaPage(
    val index: Int,
    val imageUrl: String,
    val localUri: String? = null,
    val headers: Map<String, String> = emptyMap()
)

data class MangaSource(
    val id: String,
    val name: String,
    val domain: String,
    val isPinned: Boolean = false,
    val isEnabled: Boolean = true,
    val language: String = "en",
    val category: String = "Manga",
    val isNsfw: Boolean = false,
    val iconEmoji: String = "📖",
    val brandColorHex: Long = 0xFF2DD4BF,
    val reliability: Float = 0.98f,
    val isOnline: Boolean = true,
    val statusText: String = "Online",
    val isCustom: Boolean = false
)

enum class ReadMode(val displayName: String) {
    STANDARD("Standard"),
    RTL("Right-to-left"),
    VERTICAL("Vertical"),
    WEBTOON("Webtoon")
}

enum class DownloadStatus {
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    CANCELED,
    FAILED
}

data class DownloadTask(
    val id: String,
    val mangaId: String,
    val chapterId: String,
    val mangaTitle: String,
    val chapterName: String,
    val coverUrl: String,
    val progress: Float = 0f,
    val status: DownloadStatus = DownloadStatus.DOWNLOADING,
    val speedText: String = "2.3 MB/s",
    val etaText: String = "In 3 minutes",
    val downloadedChapters: Int = 1
)

data class Bookmark(
    val id: String,
    val mangaId: String,
    val mangaTitle: String,
    val chapterId: String,
    val chapterName: String,
    val coverUrl: String,
    val pageNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class HistoryItem(
    val mangaId: String,
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
