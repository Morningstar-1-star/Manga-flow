package com.example.data.sources

import com.example.data.local.SourceConfigDao
import com.example.data.local.SourceConfigEntity
import com.example.data.model.Manga
import com.example.data.model.MangaSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
    private val mangaDex = MangaDexSource()
    private val comick = ComickSource()
    private val cuuTruyen = CuuTruyenSource()
    private val truyenGG = TruyenGGSource()
    private val docTruyen3Q = DocTruyen3QSource()
    private val batoTo = BatoToSource()
    private val localStorage = LocalStorageSource()
    val komga = KomgaSourceAdapter()
    val opds = OpdsSourceAdapter()

    // Complete list of core parser engines
    val activeParsers = listOf(
        comick,
        mangaDex,
        cuuTruyen,
        truyenGG,
        docTruyen3Q,
        batoTo,
        localStorage,
        komga,
        opds
    )

    private val dynamicParsersMap = ConcurrentHashMap<String, MangaSourceParser>(
        activeParsers.associateBy { it.source.id }
    )
    private val customSourcesMap = ConcurrentHashMap<String, MangaSource>()

    // Bounded concurrency semaphore for multi-source requests (avoids flooding networks)
    private val searchSemaphore = Semaphore(6)

    // Full catalog list from authentic definitions
    val baseCatalogSourcesList: List<MangaSource> by lazy {
        CatalogSourcesData.getAllCatalogSources().map { raw ->
            MangaSource(
                id = raw.id,
                name = raw.name,
                domain = raw.domain,
                isPinned = raw.id in listOf("mangadex", "asurascans", "comick", "webtoons", "flamecomics", "reaperscans", "manganato", "tapastic", "cuutruyen", "truyengg", "doctruyen3q", "batoto"),
                isEnabled = true,
                language = raw.language,
                category = raw.category,
                isNsfw = raw.isNsfw,
                iconEmoji = raw.iconEmoji,
                brandColorHex = raw.brandColorHex,
                reliability = raw.reliability
            )
        } + listOf(komga.source, opds.source)
    }

    fun getSourcesFlow(): Flow<List<MangaSource>> {
        return combine(
            sourceConfigDao.getSourceConfigs(),
            SourceHealthManager.healthFlow
        ) { configs, healthMap ->
            val configMap = configs.associateBy { it.id }
            val allList = baseCatalogSourcesList + customSourcesMap.values.toList()

            // Filter out deleted sources
            val activeList = allList.filter { src ->
                val cfg = configMap[src.id]
                cfg?.isEnabled != false || src.isEnabled
            }

            activeList.map { source ->
                val cfg = configMap[source.id]
                val health = healthMap[source.id]
                val isOnline = health?.status != SourceHealthStatus.BROKEN
                val statusText = when (health?.status) {
                    SourceHealthStatus.HEALTHY -> "Working"
                    SourceHealthStatus.INTERMITTENT -> "Intermittent"
                    SourceHealthStatus.BROKEN -> "Offline"
                    SourceHealthStatus.DISABLED -> "Disabled"
                    null -> if (isOnline) "Working" else "Offline"
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

    fun getParser(sourceId: String): MangaSourceParser {
        return dynamicParsersMap.getOrPut(sourceId) {
            when (sourceId) {
                komga.source.id -> komga
                opds.source.id -> opds
                else -> {
                    val src = (baseCatalogSourcesList + customSourcesMap.values).find { it.id == sourceId }
                        ?: MangaSource(id = sourceId, name = sourceId, domain = "$sourceId.com")
                    GenericMangaSourceParser(src)
                }
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

    suspend fun deleteSource(sourceId: String) {
        customSourcesMap.remove(sourceId)
        sourceConfigDao.deleteConfig(sourceId)
        sourceConfigDao.setConfig(
            SourceConfigEntity(
                id = sourceId,
                name = sourceId,
                isPinned = false,
                isEnabled = false,
                language = "en"
            )
        )
    }

    suspend fun pingAllSources(): Map<String, Boolean> = coroutineScope {
        val allSources = (baseCatalogSourcesList + customSourcesMap.values).distinctBy { it.id }
        val results = allSources.map { src ->
            async {
                val isWorking = try {
                    val res = SourceHealthManager.executeWithHealth(src.id) {
                        val parser = getParser(src.id)
                        parser.getPopularManga(1)
                    }
                    res.isSuccess && (res.getOrNull()?.isNotEmpty() == true)
                } catch (e: Exception) {
                    false
                }
                src.id to isWorking
            }
        }.awaitAll().toMap()
        results
    }

    suspend fun togglePin(sourceId: String, isPinned: Boolean) {
        val src = (baseCatalogSourcesList + customSourcesMap.values).find { it.id == sourceId } ?: return
        sourceConfigDao.setConfig(
            SourceConfigEntity(
                id = sourceId,
                name = src.name,
                isPinned = isPinned,
                isEnabled = src.isEnabled,
                language = src.language
            )
        )
    }

    suspend fun toggleEnable(sourceId: String, isEnabled: Boolean) {
        val src = (baseCatalogSourcesList + customSourcesMap.values).find { it.id == sourceId } ?: return
        sourceConfigDao.setConfig(
            SourceConfigEntity(
                id = sourceId,
                name = src.name,
                isPinned = src.isPinned,
                isEnabled = isEnabled,
                language = src.language
            )
        )
    }

    suspend fun enableAllSources(enable: Boolean) {
        baseCatalogSourcesList.forEach { src ->
            sourceConfigDao.setConfig(
                SourceConfigEntity(
                    id = src.id,
                    name = src.name,
                    isPinned = src.isPinned,
                    isEnabled = enable,
                    language = src.language
                )
            )
        }
    }

    /**
     * Parallel multi-source search with bounded concurrency, retry, and health monitoring
     */
    suspend fun searchAcrossSources(
        query: String,
        activeSources: List<MangaSource> = (baseCatalogSourcesList + customSourcesMap.values).distinctBy { it.id }
    ): List<Manga> = coroutineScope {
        val enabledSources = activeSources.filter { it.isEnabled }
        val deferredResults = enabledSources.map { src ->
            async {
                searchSemaphore.withPermit {
                    try {
                        SourceHealthManager.executeWithHealth(src.id) {
                            val parser = getParser(src.id)
                            parser.searchManga(query, page = 1)
                        }.getOrDefault(emptyList())
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
        }
        deferredResults.awaitAll().flatten()
    }

    /**
     * Aggregated search:
     * Parallel search -> Bounded concurrency -> Deduplicate titles -> Show source availability
     * e.g. One Piece: MangaDex ✓, Comick ✓, Asura Scans ✓
     */
    suspend fun searchAndAggregate(
        query: String,
        activeSources: List<MangaSource> = (baseCatalogSourcesList + customSourcesMap.values).distinctBy { it.id }
    ): List<AggregatedSearchResult> {
        val flatResults = searchAcrossSources(query, activeSources)
        if (flatResults.isEmpty()) return emptyList()

        // Normalize title for deduplication (strip spaces, punctuation, lowercase)
        fun normalize(title: String): String =
            title.lowercase().replace(Regex("[^a-z0-9]"), "")

        val grouped = flatResults.groupBy { normalize(it.title) }

        return grouped.values.map { mangasInGroup ->
            val primary = mangasInGroup.first()
            val availableSourcesList = activeSources.filter { it.isEnabled }.map { src ->
                val matching = mangasInGroup.find { it.sourceId == src.id }
                SourceAvailability(
                    sourceId = src.id,
                    sourceName = src.name,
                    isAvailable = matching != null,
                    mangaId = matching?.id
                )
            }

            AggregatedSearchResult(
                title = primary.title,
                primaryManga = primary,
                availableSources = availableSourcesList,
                allMatches = mangasInGroup
            )
        }
    }
}
