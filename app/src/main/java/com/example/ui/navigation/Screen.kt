package com.example.ui.navigation

sealed class Screen(val route: String) {
    object History : Screen("history")
    object Favourites : Screen("favourites")
    object Explore : Screen("explore")
    object Feed : Screen("feed")
    object Suggestions : Screen("suggestions")

    object MangaDetails : Screen("details/{mangaId}/{sourceId}") {
        fun createRoute(mangaId: String, sourceId: String) = "details/$mangaId/$sourceId"
    }

    object Reader : Screen("reader/{mangaId}/{chapterId}/{sourceId}") {
        fun createRoute(mangaId: String, chapterId: String, sourceId: String) =
            "reader/$mangaId/$chapterId/$sourceId"
    }

    object Downloads : Screen("downloads")
    object Bookmarks : Screen("bookmarks")
    object LocalStorage : Screen("local_storage")
    object SourceCatalog : Screen("source_catalog")
    object Search : Screen("search")
    object Settings : Screen("settings")
}
