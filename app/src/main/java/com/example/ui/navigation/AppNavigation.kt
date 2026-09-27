package com.example.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.LiquidGlassNavigationBar
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FavouritesScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LocalStorageScreen
import com.example.ui.screens.MangaDetailsScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SourceCatalogScreen
import com.example.ui.screens.SuggestionsScreen
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuRose
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel
import dev.liquidglass.compose.liquidGlassProvider
import dev.liquidglass.compose.rememberLiquidGlassProviderState

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null,
    val activeColor: Color = KotatsuTeal
)

@Composable
fun AppNavigation(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val settings by viewModel.appSettings.collectAsState()

    val glassState = rememberLiquidGlassProviderState()

    // Bottom Navigation items matching Screenshots 1 & 3:
    // History, Favourites, Explore, Feed (badge 6), Suggestions
    val navItems = listOf(
        BottomNavItem(
            title = "History",
            route = Screen.History.route,
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Outlined.History,
            activeColor = KotatsuRose // Red/rose active indicator matching Screenshot 1!
        ),
        BottomNavItem(
            title = "Favourites",
            route = Screen.Favourites.route,
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder,
            activeColor = KotatsuTeal
        ),
        BottomNavItem(
            title = "Explore",
            route = Screen.Explore.route,
            selectedIcon = Icons.Filled.Explore,
            unselectedIcon = Icons.Outlined.Explore,
            activeColor = KotatsuTeal // Teal active pill matching Screenshot 3!
        ),
        BottomNavItem(
            title = "Feed",
            route = Screen.Feed.route,
            selectedIcon = Icons.Filled.RssFeed,
            unselectedIcon = Icons.Outlined.RssFeed,
            badgeCount = 6, // Notification badge '6' matching Screenshot 3!
            activeColor = KotatsuTeal
        ),
        BottomNavItem(
            title = "Suggestions",
            route = Screen.Suggestions.route,
            selectedIcon = Icons.Filled.Lightbulb,
            unselectedIcon = Icons.Outlined.Lightbulb,
            activeColor = KotatsuTeal
        )
    )

    val isMainTab = navItems.any { it.route == currentRoute }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 680.dp

        if (isWideScreen && isMainTab) {
            // Tablet / Landscape with Navigation Rail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(44.dp)
                                .background(KotatsuTeal.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Kotatsu",
                                tint = KotatsuTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                ) {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (item.badgeCount != null) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = KotatsuRose) {
                                            Text(text = item.badgeCount.toString(), color = Color.White)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            },
                            label = { Text(text = item.title, fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = item.activeColor,
                                unselectedIconColor = KotatsuTextSecondary,
                                unselectedTextColor = KotatsuTextSecondary
                            ),
                            modifier = Modifier.testTag("rail_nav_${item.title.lowercase()}")
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AppNavHost(navController = navController, viewModel = viewModel)
                }
            }
        } else {
            // Mobile Portrait Layout with Liquid Glass Bottom Navigation Bar
            // Architecture: Content wrapped with liquidGlassProvider, with LiquidGlassNavigationBar as sibling ABOVE it!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // 1. Screen content wrapped inside liquidGlassProvider
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .liquidGlassProvider(glassState)
                ) {
                    AppNavHost(
                        navController = navController,
                        viewModel = viewModel
                    )
                }

                // 2. Liquid Glass Bottom Navigation Bar - sibling ABOVE provider!
                if (isMainTab) {
                    LiquidGlassNavigationBar(
                        glassState = glassState,
                        settings = settings,
                        navItems = navItems,
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Explore.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Explore.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            ExploreScreen(
                viewModel = viewModel,
                onNavigateToLocalStorage = { navController.navigate(Screen.LocalStorage.route) },
                onNavigateToBookmarks = { navController.navigate(Screen.Bookmarks.route) },
                onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                onNavigateToCatalog = { navController.navigate(Screen.SourceCatalog.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Favourites.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            FavouritesScreen(
                viewModel = viewModel,
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.History.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Feed.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            FeedScreen(
                viewModel = viewModel,
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                }
            )
        }

        composable(
            route = Screen.Suggestions.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            SuggestionsScreen(
                viewModel = viewModel,
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                }
            )
        }

        composable(
            route = Screen.Search.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
        ) {
            SearchScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                }
            )
        }

        composable(
            route = Screen.Bookmarks.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
        ) {
            BookmarksScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onReadChapter = { mangaId, chapterId, sourceId ->
                    navController.navigate(Screen.Reader.createRoute(mangaId, chapterId, sourceId))
                }
            )
        }

        composable(
            route = Screen.Downloads.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
        ) {
            DownloadsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                }
            )
        }

        composable(
            route = Screen.LocalStorage.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
        ) {
            LocalStorageScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onReadCbz = { mangaId, chapterId, sourceId ->
                    navController.navigate(Screen.Reader.createRoute(mangaId, chapterId, sourceId))
                }
            )
        }

        composable(
            route = Screen.MangaDetails.route,
            arguments = listOf(
                navArgument("mangaId") { type = NavType.StringType },
                navArgument("sourceId") { type = NavType.StringType }
            ),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
            val sourceId = backStackEntry.arguments?.getString("sourceId") ?: "mangadex"

            MangaDetailsScreen(
                mangaId = mangaId,
                sourceId = sourceId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onReadChapter = { selectedMangaId, chapterId, selectedSourceId ->
                    navController.navigate(Screen.Reader.createRoute(selectedMangaId, chapterId, selectedSourceId))
                }
            )
        }

        composable(
            route = Screen.Reader.route,
            arguments = listOf(
                navArgument("mangaId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("sourceId") { type = NavType.StringType }
            ),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            val sourceId = backStackEntry.arguments?.getString("sourceId") ?: "mangadex"

            ReaderScreen(
                mangaId = mangaId,
                chapterId = chapterId,
                sourceId = sourceId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.SourceCatalog.route) {
            SourceCatalogScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onSourceClick = { sourceId ->
                    viewModel.selectSourceFeed(sourceId)
                    navController.navigate(Screen.Search.route)
                }
            )
        }
    }
}
