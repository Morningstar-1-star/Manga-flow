package com.example.data.model

data class AppSettings(
    // Appearance
    val colorScheme: String = "Totoro", // Totoro, Black & White, Manga Yellow, Pink, Silver, Expressive
    val themeMode: String = "Dark", // Dark, Light, Follow system
    val isAmoledBlack: Boolean = true,
    val appLanguage: String = "Follow system",
    val isGridMode: Boolean = true,
    val gridSizePercent: Int = 100,
    val showQuickFilters: Boolean = true,
    val showReadingProgress: Boolean = true,
    val badgesInLists: String = "Saved manga, Favourites",
    val showFloatingContinue: Boolean = true,
    val showNavLabels: Boolean = true,
    val floatingNavBar: Boolean = true,
    val pinNavUi: Boolean = false,
    val exitConfirmation: Boolean = false,
    val showRecentShortcuts: Boolean = true,
    val hideNsfwShortcuts: Boolean = true,
    val protectApp: Boolean = false,
    val appPinCode: String = "",

    // Liquid Glass Navigation (Abdullajon1881/LiquidGlass)
    val liquidGlassNavEnabled: Boolean = true,
    val liquidGlassIntensity: String = "Balanced", // Subtle, Balanced, Strong
    val liquidGlassBlurDp: Float = 10f, // 0..30 dp (crystal-clear iOS style)
    val liquidGlassRefractionDp: Float = 18f, // 0..24 dp (strong curved edge lensing)
    val liquidGlassChromaticAberration: String = "Medium", // OFF, Low, Medium
    val liquidGlassRimHighlight: Boolean = true,
    val liquidGlassGelPress: Boolean = true,
    val liquidGlassTintOption: String = "System", // System, Custom
    val liquidGlassCustomTintColor: Long = 0xFF14B8A6,
    val liquidGlassTransparency: Float = 0.08f, // 0.0 to 1.0 (crystal-clear glass sheen)
    val liquidGlassReducedTransparency: Boolean = false,
    val liquidGlassPerformanceMode: String = "Auto", // Auto, Battery Saver, Maximum quality

    // Manga Sources
    val sourcesSortingOrder: String = "Name",
    val sourcesGridView: Boolean = false,
    val enableAllSources: Boolean = false,
    val disableNsfw: Boolean = false,
    val incognitoNsfw: Boolean = false,
    val chooseMirrorAuto: Boolean = true,
    val handleExternalLinks: Boolean = true,

    // Reader Settings
    val defaultReaderMode: String = "Webtoon", // Webtoon, Standard, Right-to-Left, Vertical
    val autodetectReaderMode: Boolean = true,
    val scaleMode: String = "Fit center", // Fit center, Fit width, Fit height, Stretch
    val showZoomButtons: Boolean = false,
    val webtoonZoom: Boolean = true,
    val defaultWebtoonZoomOut: Int = 0,
    val gapsInWebtoonMode: Boolean = true,
    val readerControlsInBottomBar: Boolean = true,
    val volumeKeysPaging: Boolean = true,
    val cropPageBorders: Boolean = false,
    val pageAnimation: String = "Default",
    val eInkFlashOnPageChange: Boolean = false,
    val eInkFlashDurationMs: Int = 300,
    val eInkFlashEveryPages: Int = 1,
    val readerBackground: String = "Black", // Black, Dark Gray, White
    val showInfoBar: Boolean = true,
    val transparentInfoBar: Boolean = true,
    val showChapterChangePopup: Boolean = true,
    val keepScreenOn: Boolean = true,
    val fullscreenMode: Boolean = true,

    // Storage and Network
    val savedMangaMb: Float = 219.9f,
    val pagesCacheMb: Float = 199.6f,
    val otherCacheMb: Float = 43.9f,
    val availableSpaceGb: Float = 85.0f,
    val contentPreloading: String = "3 pages",
    val preloadPagesWifiOnly: Boolean = true,
    val proxyEnabled: Boolean = false,
    val dohEnabled: Boolean = false,
    val ignoreSslErrors: Boolean = false,
    val disableConnectivityCheck: Boolean = false,

    // Downloads
    val downloadsFolder: String = "Internal shared storage",
    val preferredDownloadFormat: String = "Automatic",
    val downloadOnlyWifi: Boolean = true,
    val cellularDownloadAllowed: Boolean = true,
    val askDestinationDirEveryTime: Boolean = false,

    // Services & Tracking
    val syncEnabled: Boolean = true,
    val communityEnabled: Boolean = true,
    val suggestionsEnabled: Boolean = true,
    val relatedMangaEnabled: Boolean = true,
    val readingStatsEnabled: Boolean = true,
    val showEstimatedReadingTime: Boolean = true,
    val aniListConnected: Boolean = false,
    val kitsuConnected: Boolean = false,
    val myAnimeListConnected: Boolean = false,
    val shikimoriConnected: Boolean = false,
    val discordPresenceConnected: Boolean = false,

    // Backup & Restore
    val periodicBackups: String = "Off", // Off, Daily, Weekly

    // About
    val appVersion: String = "9.8.4",
    val allowUnstableUpdates: Boolean = false
)
