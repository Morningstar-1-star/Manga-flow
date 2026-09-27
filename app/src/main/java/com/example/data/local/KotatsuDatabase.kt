package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        SourceConfigEntity::class,
        PageEntity::class,
        TranslationCacheEntity::class,
        ReadingProgressEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KotatsuDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun chapterDao(): ChapterDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun downloadDao(): DownloadDao
    abstract fun sourceConfigDao(): SourceConfigDao
    abstract fun pageDao(): PageDao
    abstract fun translationCacheDao(): TranslationCacheDao
    abstract fun readingProgressDao(): ReadingProgressDao

    companion object {
        @Volatile
        private var INSTANCE: KotatsuDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pages` (
                        `chapterId` TEXT NOT NULL,
                        `pageIndex` INTEGER NOT NULL,
                        `imageUrl` TEXT NOT NULL,
                        `headersJson` TEXT NOT NULL DEFAULT '',
                        `localFilePath` TEXT,
                        PRIMARY KEY(`chapterId`, `pageIndex`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `translation_cache` (
                        `cacheKey` TEXT NOT NULL PRIMARY KEY,
                        `pageIndex` INTEGER NOT NULL,
                        `sourceLanguage` TEXT NOT NULL,
                        `targetLanguage` TEXT NOT NULL,
                        `bubblesJson` TEXT NOT NULL,
                        `fullSummary` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reading_progress` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `mangaId` TEXT NOT NULL,
                        `chapterId` TEXT NOT NULL,
                        `lastPageRead` INTEGER NOT NULL,
                        `totalPages` INTEGER NOT NULL,
                        `progressPercent` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): KotatsuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KotatsuDatabase::class.java,
                    "kotatsu_manga.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialSources(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialSources(database: KotatsuDatabase) {
            // Seed genuine active sources
            val sources = listOf(
                SourceConfigEntity("mangadex", "MangaDex", isPinned = true, isEnabled = true, language = "en"),
                SourceConfigEntity("comick", "ComicK", isPinned = true, isEnabled = true, language = "en"),
                SourceConfigEntity("guya", "Guya.moe", isPinned = true, isEnabled = true, language = "en"),
                SourceConfigEntity("manganato", "MangaNato", isPinned = true, isEnabled = true, language = "en"),
                SourceConfigEntity("cuutruyen", "Cứu Truyện", isPinned = true, isEnabled = true, language = "vi")
            )
            sources.forEach { database.sourceConfigDao().setConfig(it) }
        }
    }
}
