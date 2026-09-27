package com.example.data.sources

import com.example.data.local.SourceConfigDao
import com.example.data.local.SourceConfigEntity
import com.example.data.model.Chapter
import com.example.data.model.Manga
import com.example.data.model.MangaPage
import com.example.data.model.MangaSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

data class SourceAvailability(
    val sourceId: String,
    val sourceName: String,
    val isAvailable: Boolean,
    val mangaId: String? = null
)

data class AggregatedSearchResult(
    val title: String,
    val primaryManga: Manga,
    val availableSources: List<SourceAvailability>,
    val allMatches: List<Manga>
)

class SourceManager(
    private val sourceConfigDao: SourceConfigDao
) {
    val mangaDex = MangaDexSource()
    val guya = GuyaCubariSource()
    val mangaPill = MangaPillSource()
    val weebCentral = WeebCentralSource()
    val cuuTruyen = CuuTruyenSource()
    val localStorage = LocalStorageSource()
    val komga = KomgaSourceAdapter()
    val opds = OpdsSourceAdapter()

    // Complete list of real core parser engines
    val activeParsers = listOf(
        mangaDex,
        guya,
        mangaPill,
        weebCentral,
        cuuTruyen,
        localStorage,
        komga,
        opds
    )

    private val dynamicParsersMap = ConcurrentHashMap<String, SourceAdapter>(
        activeParsers.associateBy { it.source.id }
    )
    private val customSourcesMap = ConcurrentHashMap<String, MangaSource>()

    // Bounded concurrency semaphore for multi-source search (avoids flooding networks)
    private val searchSemaphore = Semaphore(4)

    val baseCatalogSourcesList: List<MangaSource> by lazy {
        val rawList = CatalogSourcesData.getAllCatalogSources()
        val customMap = activeParsers.associateBy { it.source.id }

        rawList.map { raw ->
            val active = customMap[raw.id]
            if (active != null) {
                active.source
            } else {
                MangaSource(
                    id = raw.id,
                    name = raw.name,
                    domain = raw.domain,
                    isPinned = false,
                    isEnabled = true,
                    language = raw.language,
                    category = raw.category,
                    isNsfw = raw.isNsfw,
                    iconEmoji = raw.iconEmoji,
                    brandColorHex = raw.brandColorHex,
                    reliability = raw.reliability
                )
            }
        }
    }

    fun getSourcesFlow(): Flow<List<MangaSource>> {
        return combine(
            sourceConfigDao.getSourceConfigs(),
            SourceHealthManager.healthFlow
        ) { configs, healthMap ->
            val configMap = configs.associateBy { it.id }
            val allList = baseCatalogSourcesList + customSourcesMap.values.toList()

            allList.map { source ->
                val cfg = configMap[source.id]
                val health = healthMap[source.id]
                val isOnline = health?.status != SourceHealthStatus.BROKEN
                val statusText = when (health?.status) {
                    SourceHealthStatus.HEALTHY -> "Working"
                    SourceHealthStatus.INTERMITTENT -> "Intermittent"
                    SourceHealthStatus.BROKEN -> "Offline"
                    SourceHealthStatus.DISABLED -> "Disabled"
                    null -> if (source.isEnabled) "Working" else "Offline"
                }

                source.copy(
                    isPinned = cfg?.isPinned ?: source.isPinned,
                    isEnabled = cfg?.isEnabled ?: source.isEnabled,
                    isOnline = isOnline,
                    statusText = statusText
                )
            }.sortedWith(
                compareByDescending<MangaSource> { it.isPinned }
                    .thenByDescending { it.isEnabled }
                    .thenBy { it.name }
            )
        }
    }

    fun getParser(sourceId: String): SourceAdapter {
        return dynamicParsersMap.getOrPut(sourceId) {
            val matchingSource = (baseCatalogSourcesList + customSourcesMap.values).find { it.id == sourceId }
                ?: MangaSource(id = sourceId, name = sourceId, domain = "$sourceId.com")

            when {
                sourceId == "mangadex" -> mangaDex
                sourceId == "guya" -> guya
                sourceId == "mangapill" -> mangaPill
                sourceId == "weebcentral" -> weebCentral
                sourceId == "cuutruyen" -> cuuTruyen
                sourceId == "localStorage" -> localStorage
                sourceId == komga.source.id -> komga
                sourceId == opds.source.id -> opds

                // Shonen / English Scraper mappings
                sourceId in listOf("manganato", "mangakakalot", "mangapark", "mangasee", "readm", "ninemanga") ->
                    DelegatedSourceAdapter(mangaPill, matchingSource)

                // Manhwa / Webtoons Scraper mappings
                sourceId in listOf("weebcentral", "asurascans", "flamecomics", "reaperscans", "luminousscans", "zero_scans", "realmscans", "drakescans", "batoto", "webtoons", "tapastic") ->
                    DelegatedSourceAdapter(weebCentral, matchingSource)

                // Vietnamese Scraper mappings
                sourceId in listOf("truyengg", "doctruyen3q", "cmanga", "nettruyen", "blogtruyen", "truyenqq", "hamtruyen") ->
                    DelegatedSourceAdapter(cuuTruyen, matchingSource)

                // Spanish Scraper mappings
                sourceId in listOf("tumangaonline", "inmanga", "mangasmanhua", "lectormanga") ->
                    DelegatedSourceAdapter(mangaDex, matchingSource)

                // French Scraper mappings
                sourceId in listOf("japscan", "scanmanga", "furyosquad") ->
                    DelegatedSourceAdapter(mangaDex, matchingSource)

                // Japanese Scraper mappings
                sourceId in listOf("shonenjumpplus", "pixivcomic", "alphapolis") ->
                    DelegatedSourceAdapter(mangaDex, matchingSource)

                else -> DelegatedSourceAdapter(mangaPill, matchingSource)
            }
        }
    }

    suspend fun addCustomSource(
        name: String,
        domain: String,
        language: String,
        category: String
    ) {
        val cleanDomain = domain.replace("https://", "").replace("http://", "").trim()
        val id = "custom_${cleanDomain.replace(".", "_")}_${System.currentTimeMillis()}"
        val newSource = MangaSource(
            id = id,
            name = name.ifBlank { cleanDomain },
            domain = cleanDomain,
            isPinned = true,
            isEnabled = true,
            language = language,
            category = category,
            iconEmoji = "🌐",
            brandColorHex = 0xFF8B5CF6,
            isOnline = true,
            statusText = "Working",
            isCustom = true
        )
        customSourcesMap[id] = newSource
        sourceConfigDao.setConfig(
            SourceConfigEntity(
                id = id,
                name = newSource.name,
                isPinned = true,
                isEnabled = true,
                language = language
            )
        )
    }

    suspend fun togglePin(sourceId: String, isPinned: Boolean) {
        sourceConfigDao.togglePin(sourceId, isPinned)
    }

    suspend fun toggleEnable(sourceId: String, isEnabled: Boolean) {
        sourceConfigDao.toggleEnable(sourceId, isEnabled)
    }

    suspend fun deleteSource(sourceId: String) {
        customSourcesMap.remove(sourceId)
        sourceConfigDao.deleteConfig(sourceId)
    }

    suspend fun pingAllSources() {
        for (parser in activeParsers) {
            SourceHealthManager.runDiagnostics(parser.source.id) {
                val res = parser.getPopularManga(1)
                if (res.isSuccess) Result.success(res.getOrNull()?.size ?: 0)
                else Result.failure(res.exceptionOrNull() ?: IOException("Failed to ping source"))
            }
        }
    }

    /**
     * Parallel global search across active enabled sources with bounded concurrency.
     * Groups and deduplicates results to show per-source availability.
     */
    suspend fun searchMangaAcrossSources(
        query: String,
        enabledSourceIds: List<String> = emptyList(),
        genres: List<String> = emptyList()
    ): List<AggregatedSearchResult> = coroutineScope {
        if (query.isBlank()) return@coroutineScope emptyList()

        val sourcesToSearch = if (enabledSourceIds.isNotEmpty()) {
            activeParsers.filter { it.source.id in enabledSourceIds }
        } else {
            listOf(mangaDex, mangaPill, weebCentral, guya)
        }

        val deferredResults = sourcesToSearch.map { parser ->
            async {
                searchSemaphore.withPermit {
                    SourceHealthManager.executeWithHealth(parser.source.id, timeoutMs = 12000L) {
                        parser.searchManga(query = query, genres = genres)
                    }.getOrDefault(emptyList())
                }
            }
        }

        val allResults = deferredResults.awaitAll().flatten()
        aggregateSearchResults(allResults, sourcesToSearch.map { it.source })
    }

    private fun aggregateSearchResults(
        mangaList: List<Manga>,
        allQueriedSources: List<MangaSource>
    ): List<AggregatedSearchResult> {
        val grouped = mangaList.groupBy { normalizeTitle(it.title) }

        return grouped.map { (_, matches) ->
            val primary = matches.maxByOrNull { it.totalChapters } ?: matches.first()
            val availableSources = allQueriedSources.map { src ->
                val matchInSource = matches.find { it.sourceId == src.id }
                SourceAvailability(
                    sourceId = src.id,
                    sourceName = src.name,
                    isAvailable = matchInSource != null,
                    mangaId = matchInSource?.id
                )
            }

            AggregatedSearchResult(
                title = primary.title,
                primaryManga = primary,
                availableSources = availableSources,
                allMatches = matches
            )
        }.sortedByDescending { agg ->
            agg.availableSources.count { it.isAvailable }
        }
    }

    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }
}
