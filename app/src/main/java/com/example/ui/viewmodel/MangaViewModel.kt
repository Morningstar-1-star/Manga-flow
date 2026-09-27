package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KotatsuDatabase
import com.example.data.model.AppSettings
import com.example.data.model.Bookmark
import com.example.data.model.Chapter
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTask
import com.example.data.model.HistoryItem
import com.example.data.model.Manga
import com.example.data.model.MangaPage
import com.example.data.model.MangaSource
import com.example.data.model.ReadMode
import com.example.data.model.SearchFilter
import com.example.data.repository.MangaRepository
import com.example.data.sources.SourceManager
import com.example.data.translation.MangaTranslator
import com.example.data.translation.PageTranslationResult
import com.example.data.translation.TranslatedBubble
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MangaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = KotatsuDatabase.getInstance(application)
    private val sourceManager = SourceManager(database.sourceConfigDao())
    val repository = MangaRepository(database, sourceManager)

    // Settings State
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    // Advanced Search Filter State
    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    // Sources list
    val sources: StateFlow<List<MangaSource>> = repository.getSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Library State
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val libraryManga: StateFlow<List<Manga>> = repository.getLibraryManga(null)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History State
    val historyItems: StateFlow<List<HistoryItem>> = repository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookmarks
    val bookmarks: StateFlow<List<Bookmark>> = repository.getBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Downloads
    val downloads: StateFlow<List<DownloadTask>> = repository.getDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilterChip = MutableStateFlow("All")
    val selectedFilterChip: StateFlow<String> = _selectedFilterChip.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Manga>>(emptyList())
    val searchResults: StateFlow<List<Manga>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Details State
    private val _selectedManga = MutableStateFlow<Manga?>(null)
    val selectedManga: StateFlow<Manga?> = _selectedManga.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("vi")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _mangaChapters = MutableStateFlow<List<Chapter>>(emptyList())
    val mangaChapters: StateFlow<List<Chapter>> = _mangaChapters.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _showBookmarksOnly = MutableStateFlow(false)
    val showBookmarksOnly: StateFlow<Boolean> = _showBookmarksOnly.asStateFlow()

    // Reader State
    private val _currentReaderMode = MutableStateFlow(ReadMode.WEBTOON)
    val currentReaderMode: StateFlow<ReadMode> = _currentReaderMode.asStateFlow()

    private val _readerPages = MutableStateFlow<List<MangaPage>>(emptyList())
    val readerPages: StateFlow<List<MangaPage>> = _readerPages.asStateFlow()

    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

    private val _isReaderControlsVisible = MutableStateFlow(false)
    val isReaderControlsVisible: StateFlow<Boolean> = _isReaderControlsVisible.asStateFlow()

    private val _useTwoPageLayout = MutableStateFlow(false)
    val useTwoPageLayout: StateFlow<Boolean> = _useTwoPageLayout.asStateFlow()

    private val _autoScroll = MutableStateFlow(false)
    val autoScroll: StateFlow<Boolean> = _autoScroll.asStateFlow()

    // Manga Translator State
    private val _isTranslationActive = MutableStateFlow(false)
    val isTranslationActive: StateFlow<Boolean> = _isTranslationActive.asStateFlow()

    private val _isTranslatingPage = MutableStateFlow(false)
    val isTranslatingPage: StateFlow<Boolean> = _isTranslatingPage.asStateFlow()

    private val _translationTargetLanguage = MutableStateFlow("en")
    val translationTargetLanguage: StateFlow<String> = _translationTargetLanguage.asStateFlow()

    private val _translatedBubblesMap = MutableStateFlow<Map<Int, List<TranslatedBubble>>>(emptyMap())
    val translatedBubblesMap: StateFlow<Map<Int, List<TranslatedBubble>>> = _translatedBubblesMap.asStateFlow()

    private val _readerColorFilter = MutableStateFlow("None")
    val readerColorFilter: StateFlow<String> = _readerColorFilter.asStateFlow()

    private val _readerBrightness = MutableStateFlow(1.0f)
    val readerBrightness: StateFlow<Float> = _readerBrightness.asStateFlow()

    private val _translationMode = MutableStateFlow(com.example.data.translation.TranslationMode.ENGLISH_TYPESETTING)
    val translationMode: StateFlow<com.example.data.translation.TranslationMode> = _translationMode.asStateFlow()

    // Mihon Extensions State
    val mihonExtensions = com.example.data.sources.MihonExtensionManager.extensionsList

    // Sync Manager
    val syncManager = com.example.data.sync.SyncManager(database)
    val syncConfig = syncManager.syncConfig

    private val _diagnosticResults = MutableStateFlow<Map<String, com.example.data.sources.DiagnosticResult>>(emptyMap())
    val diagnosticResults: StateFlow<Map<String, com.example.data.sources.DiagnosticResult>> = _diagnosticResults.asStateFlow()

    private val _selectedSourceId = MutableStateFlow<String?>(null)
    val selectedSourceId: StateFlow<String?> = _selectedSourceId.asStateFlow()

    init {
        // Load initial explore search results across sources
        loadSourceManga(null)
    }

    fun selectSourceFeed(sourceId: String?) {
        _selectedSourceId.value = sourceId
        _searchQuery.value = ""
        loadSourceManga(sourceId)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchManga(query)
    }

    fun onFilterChipSelected(chip: String) {
        _selectedFilterChip.value = chip
        searchManga(_searchQuery.value)
    }

    fun loadSourceManga(sourceId: String?) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                if (sourceId != null) {
                    val parser = sourceManager.getParser(sourceId)
                    _searchResults.value = parser.getPopularManga().getOrDefault(emptyList())
                } else {
                    val activeList = listOf(
                        "asurascans", "mangadex", "comick", "webtoons", "flamecomics",
                        "reaperscans", "manganato", "tapastic", "cuutruyen", "truyengg",
                        "doctruyen3q", "batoto", "tumangaonline", "shonenjumpplus"
                    )
                    val combined = activeList.flatMap { id ->
                        try {
                            sourceManager.getParser(id).getPopularManga().getOrDefault(emptyList())
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    _searchResults.value = combined.distinctBy { it.id }
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun searchManga(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                if (query.isBlank()) {
                    loadSourceManga(_selectedSourceId.value)
                } else {
                    val currentSource = _selectedSourceId.value
                    if (currentSource != null) {
                        val parser = sourceManager.getParser(currentSource)
                        _searchResults.value = parser.searchManga(query).getOrDefault(emptyList())
                    } else {
                        _searchResults.value = sourceManager.searchAcrossSources(query)
                    }
                }
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun loadMangaDetails(mangaId: String, sourceId: String) {
        viewModelScope.launch {
            try {
                val manga = repository.fetchAndStoreMangaDetails(mangaId, sourceId)
                _selectedManga.value = manga
                loadChapters(mangaId, _selectedLanguage.value)
            } catch (e: Exception) {
                // Keep local or fallback
            }
        }
    }

    fun selectLanguage(lang: String) {
        _selectedLanguage.value = lang
        _selectedManga.value?.let { loadChapters(it.id, lang) }
    }

    private fun loadChapters(mangaId: String, language: String) {
        viewModelScope.launch {
            repository.getChapters(mangaId, language).collect {
                _mangaChapters.value = it
            }
        }
    }

    fun toggleFavorite(manga: Manga, category: String = "Reading") {
        viewModelScope.launch {
            val newInLibrary = !manga.inLibrary
            val newCat = if (newInLibrary) category else "None"
            repository.updateFavorite(manga.id, newInLibrary, newCat)
            _selectedManga.value = _selectedManga.value?.copy(inLibrary = newInLibrary, category = newCat)
        }
    }

    fun toggleChapterBookmark(chapter: Chapter, manga: Manga) {
        viewModelScope.launch {
            repository.toggleChapterBookmark(
                chapterId = chapter.id,
                isBookmarked = !chapter.isBookmarked,
                mangaId = manga.id,
                mangaTitle = manga.title,
                coverUrl = manga.coverUrl,
                chapterName = chapter.name,
                page = 1
            )
        }
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun toggleBookmarksOnly() {
        _showBookmarksOnly.value = !_showBookmarksOnly.value
    }

    fun togglePinSource(sourceId: String, isPinned: Boolean) {
        viewModelScope.launch {
            sourceManager.togglePin(sourceId, isPinned)
        }
    }

    fun toggleEnableSource(sourceId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            sourceManager.toggleEnable(sourceId, isEnabled)
        }
    }

    fun addCustomSource(name: String, domain: String, language: String, category: String) {
        viewModelScope.launch {
            sourceManager.addCustomSource(name, domain, language, category)
        }
    }

    fun deleteSource(sourceId: String) {
        viewModelScope.launch {
            sourceManager.deleteSource(sourceId)
        }
    }

    fun pingAllSources() {
        viewModelScope.launch {
            sourceManager.pingAllSources()
        }
    }

    // Reader Functions
    fun openReader(mangaId: String, chapterId: String, sourceId: String) {
        viewModelScope.launch {
            val pages = repository.getPages(sourceId, chapterId)
            _readerPages.value = pages
            _currentPageIndex.value = 0
            _isReaderControlsVisible.value = false

            // Update reading progress in history
            val manga = _selectedManga.value
            if (manga != null) {
                val chapter = _mangaChapters.value.find { it.id == chapterId }
                repository.updateReadingProgress(
                    mangaId = manga.id,
                    mangaTitle = manga.title,
                    coverUrl = manga.coverUrl,
                    chapterId = chapterId,
                    chapterName = chapter?.name ?: "Chapter",
                    page = 1,
                    totalPages = pages.size
                )
            }
        }
    }

    fun setReadMode(mode: ReadMode) {
        _currentReaderMode.value = mode
    }

    fun toggleReaderControls() {
        _isReaderControlsVisible.value = !_isReaderControlsVisible.value
    }

    fun hideReaderControls() {
        _isReaderControlsVisible.value = false
    }

    fun setPageIndex(index: Int) {
        _currentPageIndex.value = index
        _selectedManga.value?.let { manga ->
            val pages = _readerPages.value
            val currentChId = manga.lastReadChapterId ?: ""
            if (pages.isNotEmpty()) {
                viewModelScope.launch {
                    repository.updateReadingProgress(
                        mangaId = manga.id,
                        mangaTitle = manga.title,
                        coverUrl = manga.coverUrl,
                        chapterId = currentChId,
                        chapterName = manga.lastReadChapterName ?: "Chapter",
                        page = index + 1,
                        totalPages = pages.size
                    )
                }
            }
        }
    }

    fun toggleTwoPageLayout(enabled: Boolean) {
        _useTwoPageLayout.value = enabled
    }

    fun toggleAutoScroll(enabled: Boolean) {
        _autoScroll.value = enabled
    }

    // Download actions
    fun downloadChapter(manga: Manga, chapter: Chapter) {
        viewModelScope.launch {
            repository.enqueueDownload(
                mangaId = manga.id,
                chapterId = chapter.id,
                mangaTitle = manga.title,
                chapterName = chapter.name,
                coverUrl = manga.coverUrl
            )
        }
    }

    fun pauseAllDownloads() {
        viewModelScope.launch { repository.pauseAllDownloads() }
    }

    fun resumeAllDownloads() {
        viewModelScope.launch { repository.resumeAllDownloads() }
    }

    fun cancelDownload(id: String) {
        viewModelScope.launch { repository.cancelDownload(id) }
    }

    // Settings actions
    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        _appSettings.value = transform(_appSettings.value)
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    fun clearLibrary() {
        viewModelScope.launch { repository.clearLibrary() }
    }

    fun enableAllSources(enable: Boolean) {
        viewModelScope.launch {
            sourceManager.enableAllSources(enable)
            _appSettings.value = _appSettings.value.copy(enableAllSources = enable)
        }
    }

    fun applySearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
        searchManga(filter.query)
    }

    // Translation methods
    fun toggleTranslationActive(active: Boolean? = null) {
        val newState = active ?: !_isTranslationActive.value
        _isTranslationActive.value = newState
        if (newState && _translatedBubblesMap.value.isEmpty()) {
            translatePage(_currentPageIndex.value)
        }
    }

    fun setTranslationTargetLanguage(lang: String) {
        _translationTargetLanguage.value = lang
        // Re-translate current page with new target language
        translatePage(_currentPageIndex.value)
    }

    fun translatePage(pageIndex: Int) {
        viewModelScope.launch {
            _isTranslatingPage.value = true
            val pages = _readerPages.value
            val imageUrl = if (pageIndex in pages.indices) pages[pageIndex].imageUrl else ""
            val result = MangaTranslator.translateComicPage(
                pageIndex = pageIndex,
                imageUrl = imageUrl,
                sourceLang = "auto",
                targetLang = _translationTargetLanguage.value
            )
            result.getOrNull()?.let { res ->
                val currentMap = _translatedBubblesMap.value.toMutableMap()
                currentMap[pageIndex] = res.bubbles
                _translatedBubblesMap.value = currentMap
            }
            _isTranslatingPage.value = false
        }
    }

    fun setReaderColorFilter(filterName: String) {
        _readerColorFilter.value = filterName
    }

    fun setReaderBrightness(brightness: Float) {
        _readerBrightness.value = brightness.coerceIn(0.2f, 1.0f)
    }

    fun setTranslationMode(mode: com.example.data.translation.TranslationMode) {
        _translationMode.value = mode
    }

    fun runSourceDiagnostics(sourceId: String) {
        viewModelScope.launch {
            val result = com.example.data.sources.SourceHealthManager.runDiagnostics(sourceId) {
                val parser = sourceManager.getParser(sourceId)
                val res = parser.getPopularManga(1)
                if (res.isSuccess) Result.success(res.getOrNull()?.size ?: 0)
                else Result.failure(res.exceptionOrNull() ?: Exception("Unknown diagnostic failure"))
            }
            val currentMap = _diagnosticResults.value.toMutableMap()
            currentMap[sourceId] = result
            _diagnosticResults.value = currentMap
        }
    }

    fun refreshMihonExtensions() {
        viewModelScope.launch {
            com.example.data.sources.MihonExtensionManager.refreshExtensions()
        }
    }

    fun toggleInstallMihonExtension(pkgName: String, install: Boolean) {
        com.example.data.sources.MihonExtensionManager.toggleInstallExtension(pkgName, install)
    }

    fun toggleEnableMihonExtension(pkgName: String, enable: Boolean) {
        com.example.data.sources.MihonExtensionManager.toggleEnableExtension(pkgName, enable)
    }

    fun performSync(onComplete: (com.example.data.sync.SyncResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = syncManager.performSync()
            onComplete(result)
        }
    }

    fun updateSyncConfig(url: String, token: String, autoSync: Boolean) {
        syncManager.updateConfig(url, token, autoSync)
    }
}
