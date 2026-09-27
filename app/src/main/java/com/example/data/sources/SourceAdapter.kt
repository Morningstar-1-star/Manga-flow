package com.example.data.sources

import com.example.data.model.Manga
import com.example.data.model.MangaPage
import com.example.data.model.MangaSource
import com.example.data.model.Chapter

enum class SourceType {
    KOTATSU_PARSER,
    MIHON_EXTENSION,
    ANIYOMI_EXTENSION,
    API_SOURCE,
    HTML_PARSER,
    OPDS,
    KOMGA,
    LOCAL_STORAGE
}

data class SourceFilter(
    val id: String,
    val name: String,
    val type: FilterType,
    val options: List<String> = emptyList(),
    val defaultValue: String = ""
)

enum class FilterType {
    TEXT, CHECKBOX, SELECT, TRISTATE, SORT
}

data class SourceAuthInfo(
    val isAuthenticated: Boolean = false,
    val username: String? = null,
    val supportsLogin: Boolean = false,
    val sessionCookies: Map<String, String> = emptyMap()
)

data class SourceSettings(
    val preferredImageServer: String = "original",
    val dataSaver: Boolean = false,
    val customUserAgent: String? = null,
    val customDomain: String? = null,
    val timeoutSeconds: Long = 15L
)

/**
 * Universal SourceAdapter interface.
 * Unifies Kotatsu parsers, Mihon extensions, Aniyomi extensions, API sources,
 * OPDS catalogs, Komga servers, and local archives under a stable abstraction.
 */
interface SourceAdapter : MangaSourceParser {
    val sourceType: SourceType
        get() = SourceType.KOTATSU_PARSER

    val supportsLatest: Boolean get() = true
    val supportsSearch: Boolean get() = true

    suspend fun getFilters(): List<SourceFilter> = listOf(
        SourceFilter("sort", "Sort By", FilterType.SELECT, listOf("Popular", "Latest", "Rating", "Title"), "Popular"),
        SourceFilter("status", "Status", FilterType.SELECT, listOf("All", "Ongoing", "Completed"), "All")
    )

    suspend fun getAuthInfo(): SourceAuthInfo = SourceAuthInfo(
        isAuthenticated = true,
        supportsLogin = false
    )

    suspend fun login(credentials: Map<String, String>): Result<Boolean> = Result.success(true)

    suspend fun logout(): Result<Unit> = Result.success(Unit)

    suspend fun getSettings(): SourceSettings = SourceSettings()

    suspend fun updateSettings(settings: SourceSettings): Result<Unit> = Result.success(Unit)
}
