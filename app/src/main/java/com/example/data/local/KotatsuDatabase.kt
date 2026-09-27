package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MangaEntity::class,
        ChapterEntity::class,
        HistoryEntity::class,
        BookmarkEntity::class,
        DownloadEntity::class,
        SourceConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KotatsuDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun chapterDao(): ChapterDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun downloadDao(): DownloadDao
    abstract fun sourceConfigDao(): SourceConfigDao

    companion object {
        @Volatile
        private var INSTANCE: KotatsuDatabase? = null

        fun getInstance(context: Context): KotatsuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KotatsuDatabase::class.java,
                    "kotatsu_manga.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: KotatsuDatabase) {
            // Seed initial Manga entities for explore catalog discovery
            val initialManga = listOf(
                MangaEntity(
                    id = "mangadex_non_milk_coffee",
                    sourceId = "mangadex",
                    title = "Non Milk-Milk Coffee Webcomic",
                    altTitle = "Bạc xỉu không sữa",
                    author = "Senukin",
                    artist = "Senukin",
                    description = "A male office worker falls in love with the owner of a small coffee shop in a corner of the big city.",
                    coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
                    status = "Finished",
                    rating = 8.9f,
                    genresString = "Web Comic,Self-Published,Romance,Slice of Life,Office Workers,Comedy",
                    category = "None",
                    inLibrary = false,
                    lastReadChapterId = null,
                    lastReadChapterName = null,
                    lastReadPage = 0,
                    lastReadTime = 0L,
                    totalChapters = 18,
                    readProgressPercent = 0,
                    preferredReadMode = "WEBTOON"
                ),
                MangaEntity(
                    id = "mangadex_say_hello_to_black_jack",
                    sourceId = "mangadex",
                    title = "Say Hello to Black Jack",
                    altTitle = "Give My Regards to Black Jack",
                    author = "Shuho Sato",
                    artist = "Shuho Sato",
                    description = "Saitou is a young doctor who just graduated. Starting his career as a doctor he finds there is a lot more to this profession than one would think. An intense drama about the dark side of the medical world.",
                    coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80",
                    status = "Finished",
                    rating = 9.1f,
                    genresString = "Drama,Medical,Slice of Life,Seinen",
                    category = "None",
                    inLibrary = false,
                    lastReadChapterId = null,
                    lastReadChapterName = null,
                    lastReadPage = 0,
                    lastReadTime = 0L,
                    totalChapters = 127,
                    readProgressPercent = 0,
                    preferredReadMode = "RTL"
                ),
                MangaEntity(
                    id = "comick_brainrot_girlfriend",
                    sourceId = "comick",
                    title = "Brainrot Girlfriend",
                    altTitle = "My Gyaru Brainrot",
                    author = "Twison",
                    artist = "Twison",
                    description = "When a wholesome guy starts dating an internet-addicted meme gyaru girlfriend, chaos and cute romance ensue.",
                    coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
                    status = "Ongoing",
                    rating = 9.4f,
                    genresString = "Romance,Comedy,Gyaru,Webtoon",
                    category = "None",
                    inLibrary = false,
                    lastReadChapterId = null,
                    lastReadChapterName = null,
                    lastReadPage = 0,
                    lastReadTime = 0L,
                    totalChapters = 45,
                    readProgressPercent = 0,
                    preferredReadMode = "WEBTOON"
                ),
                MangaEntity(
                    id = "mangadex_self_destruction_girl",
                    sourceId = "mangadex",
                    title = "Self-destruction Girl",
                    altTitle = "Jikai Shoujo",
                    author = "Kuroba",
                    artist = "Kuroba",
                    description = "A comedy about a girl whose overthinking leads to the most hilarious self-inflicted dilemmas.",
                    coverUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
                    status = "Ongoing",
                    rating = 8.6f,
                    genresString = "Comedy,School Life,Romance",
                    category = "None",
                    inLibrary = false,
                    lastReadChapterId = null,
                    lastReadChapterName = null,
                    lastReadPage = 0,
                    lastReadTime = 0L,
                    totalChapters = 20,
                    readProgressPercent = 0,
                    preferredReadMode = "STANDARD"
                )
            )

            database.mangaDao().insertAll(initialManga)

            // Seed Source Configs
            val sources = listOf(
                SourceConfigEntity("comick", "ComicK", isPinned = true, isEnabled = true, language = "en"),
                SourceConfigEntity("mangadex", "MangaDex", isPinned = true, isEnabled = true, language = "en"),
                SourceConfigEntity("cuutruyen", "Cứu Truyện", isPinned = true, isEnabled = true, language = "vi"),
                SourceConfigEntity("truyengg", "TruyenGG", isPinned = true, isEnabled = true, language = "vi"),
                SourceConfigEntity("doctruyen3q", "DocTruyen3Q", isPinned = true, isEnabled = true, language = "vi"),
                SourceConfigEntity("cmanga", "CManga", isPinned = true, isEnabled = true, language = "vi"),
                SourceConfigEntity("batoto", "Bato.To", isPinned = false, isEnabled = true, language = "en")
            )
            sources.forEach { database.sourceConfigDao().setConfig(it) }
        }
    }
}
