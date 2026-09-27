package com.example

import com.example.data.model.Chapter
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTask
import com.example.data.model.Manga
import com.example.data.model.MangaSource
import com.example.data.model.ReadMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MangaReaderTest {

    @Test
    fun testMangaModelCreation() {
        val manga = Manga(
            id = "mangadex_non_milk_coffee",
            sourceId = "mangadex",
            title = "Non Milk-Milk Coffee Webcomic",
            altTitle = "Bạc xỉu không sữa",
            author = "Senukin",
            status = "Finished",
            readProgressPercent = 72,
            totalChapters = 18
        )

        assertEquals("Non Milk-Milk Coffee Webcomic", manga.title)
        assertEquals("Bạc xỉu không sữa", manga.altTitle)
        assertEquals("Senukin", manga.author)
        assertEquals(72, manga.readProgressPercent)
        assertEquals(18, manga.totalChapters)
    }

    @Test
    fun testChapterModelCreation() {
        val chapter = Chapter(
            id = "ch_13",
            mangaId = "mangadex_non_milk_coffee",
            sourceId = "mangadex",
            name = "Ngày 13",
            number = 13f,
            isRead = true,
            isBookmarked = false
        )

        assertEquals(13f, chapter.number)
        assertTrue(chapter.isRead)
        assertEquals("Ngày 13", chapter.name)
    }

    @Test
    fun testReadModes() {
        assertEquals("Webtoon", ReadMode.WEBTOON.displayName)
        assertEquals("Standard", ReadMode.STANDARD.displayName)
        assertEquals("Right-to-left", ReadMode.RTL.displayName)
        assertEquals("Vertical", ReadMode.VERTICAL.displayName)
    }

    @Test
    fun testDownloadTask() {
        val task = DownloadTask(
            id = "dl_1",
            mangaId = "comick_brainrot",
            chapterId = "ch_1",
            mangaTitle = "Brainrot Girlfriend",
            chapterName = "Chapter 1",
            coverUrl = "https://example.com/cover.jpg",
            progress = 0.209f,
            status = DownloadStatus.DOWNLOADING,
            etaText = "In 3 minutes"
        )

        assertEquals(DownloadStatus.DOWNLOADING, task.status)
        assertEquals(0.209f, task.progress, 0.001f)
        assertEquals("In 3 minutes", task.etaText)
    }
}
