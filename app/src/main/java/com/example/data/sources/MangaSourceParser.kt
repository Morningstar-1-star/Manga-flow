package com.example.data.sources

import com.example.data.model.Chapter
import com.example.data.model.Manga
import com.example.data.model.MangaPage
import com.example.data.model.MangaSource

interface MangaSourceParser {
    val source: MangaSource

    suspend fun getPopularManga(page: Int = 1): Result<List<Manga>>

    suspend fun getLatestManga(page: Int = 1): Result<List<Manga>>

    suspend fun searchManga(
        query: String,
        page: Int = 1,
        genres: List<String> = emptyList(),
        author: String? = null
    ): Result<List<Manga>>

    suspend fun getMangaDetails(mangaId: String): Result<Manga>

    suspend fun getChapters(mangaId: String, language: String? = null): Result<List<Chapter>>

    suspend fun getPages(chapterId: String): Result<List<MangaPage>>
}
