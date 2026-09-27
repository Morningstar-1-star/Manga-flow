package com.example.data.sources

import com.example.data.local.SourceConfigDao
import com.example.data.local.SourceConfigEntity
import com.example.data.model.Manga
import com.example.data.model.MangaSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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

    // Complete list of parser engines
    val activeParsers = listOf(
        comick,
        mangaDex,
        cuuTruyen,
        truyenGG,
        docTruyen3Q,
        batoTo,
        localStorage
    )

    private val activeParserMap = activeParsers.associateBy { it.source.id }

    // Bounded concurrency semaphore for multi-source requests
    private val searchSemaphore = Semaphore(4)

    // Full catalog list (1200+ sources)
    val catalogSourcesList: List<MangaSource> by lazy {
        CatalogSourcesData.getAllCatalogSources().map { raw ->
            MangaSource(
                id = raw.id,
                name = raw.name,
                domain = raw.domain,
                isPinned = raw.id in listOf("comick", "mangadex", "cuutruyen", "truyengg", "doctruyen3q"),
                isEnabled = raw.id in listOf("comick", "mangadex", "cuutruyen", "truyengg", "doctruyen3q", "batoto", "local"),
                language = raw.language,
                category = raw.category,
                isNsfw = raw.isNsfw,
                iconEmoji = raw.iconEmoji,
                brandColorHex = raw.brandColorHex,
                reliability = raw.reliability
            )
        }
    }

    fun getSourcesFlow(): Flow<List<MangaSource>> {
        return sourceConfigDao.getSourceConfigs().map { configs ->
            val configMap = configs.associateBy { it.id }
            catalogSourcesList.map { source ->
                val cfg = configMap[source.id]
                source.copy(
                    isPinned = cfg?.isPinned ?: source.isPinned,
                    isEnabled = cfg?.isEnabled ?: source.isEnabled
                )
            }.sortedWith(
                compareByDescending<MangaSource> { it.isPinned }
                    .thenByDescending { it.isEnabled }
                    .thenBy { it.name }
            )
        }
    }

    fun getParser(sourceId: String): MangaSourceParser {
        return activeParserMap[sourceId] ?: mangaDex
    }

    suspend fun togglePin(sourceId: String, isPinned: Boolean) {
        val src = catalogSourcesList.find { it.id == sourceId } ?: return
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
        val src = catalogSourcesList.find { it.id == sourceId } ?: return
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
        catalogSourcesList.forEach { src ->
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

    suspend fun searchAcrossSources(
        query: String,
        activeSources: List<MangaSource> = activeParsers.map { it.source }
    ): List<Manga> = coroutineScope {
        val enabledSources = activeSources.filter { it.isEnabled }
        val deferredResults = enabledSources.map { src ->
            async {
                searchSemaphore.withPermit {
                    try {
                        val parser = getParser(src.id)
                        parser.searchManga(query, page = 1).getOrDefault(emptyList())
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
        }
        deferredResults.awaitAll().flatten()
    }
}

