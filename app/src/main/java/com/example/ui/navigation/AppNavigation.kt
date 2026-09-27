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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
            // Tablet / Landscape with Navigation Rail (Screenshot 1 & 2!)
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = KotatsuDarkSurface,
                    header = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(44.dp)
                                .background(KotatsuTeal.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                            contentAlignment = androidx.compose.ui.Alignment.Center
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
            // Mobile Portrait Layout with Bottom Navigation Bar (Screenshot 3!)
            Scaffold(
                bottomBar = {
                    if (isMainTab) {
                        NavigationBar(
                            containerColor = KotatsuDarkSurface,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("bottom_nav_bar")
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
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
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        indicatorColor = item.activeColor,
                                        unselectedIconColor = KotatsuTextSecondary,
                                        unselectedTextColor = KotatsuTextSecondary
                                    ),
                                    modifier = Modifier.testTag("bottom_nav_${item.title.lowercase()}")
                                )
                            }
                        }
                    }
                },
                containerColor = KotatsuDarkBg
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AppNavHost(navController = navController, viewModel = viewModel)
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
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(280)) + slideInHorizontally(initialOffsetX = { it / 6 }, animationSpec = tween(280)) },
        exitTransition = { fadeOut(animationSpec = tween(280)) + slideOutHorizontally(targetOffsetX = { -it / 6 }, animationSpec = tween(280)) },
        popEnterTransition = { fadeIn(animationSpec = tween(280)) + slideInHorizontally(initialOffsetX = { -it / 6 }, animationSpec = tween(280)) },
        popExitTransition = { fadeOut(animationSpec = tween(280)) + slideOutHorizontally(targetOffsetX = { it / 6 }, animationSpec = tween(280)) }
    ) {
        composable(Screen.Explore.route) {
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

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Favourites.route) {
            FavouritesScreen(
                viewModel = viewModel,
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Feed.route) {
            FeedScreen(
                viewModel = viewModel,
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                }
            )
        }

        composable(Screen.Suggestions.route) {
            SuggestionsScreen(
                viewModel = viewModel,
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                }
            )
        }

        composable(
            route = Screen.MangaDetails.route,
            arguments = listOf(
                navArgument("mangaId") { type = NavType.StringType },
                navArgument("sourceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
            val sourceId = backStackEntry.arguments?.getString("sourceId") ?: "mangadex"

            MangaDetailsScreen(
                mangaId = mangaId,
                sourceId = sourceId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onReadChapter = { mId, chId, srcId ->
                    navController.navigate(Screen.Reader.createRoute(mId, chId, srcId))
                }
            )
        }

        composable(
            route = Screen.Reader.route,
            arguments = listOf(
                navArgument("mangaId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("sourceId") { type = NavType.StringType }
            )
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

        composable(Screen.Downloads.route) {
            DownloadsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                }
            )
        }

        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onReadChapter = { mId, chId, srcId ->
                    navController.navigate(Screen.Reader.createRoute(mId, chId, srcId))
                }
            )
        }

        composable(Screen.LocalStorage.route) {
            LocalStorageScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onReadCbz = { mId, chId, srcId ->
                    navController.navigate(Screen.Reader.createRoute(mId, chId, srcId))
                }
            )
        }

        composable(Screen.SourceCatalog.route) {
            SourceCatalogScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToMangaDetails = { mangaId, sourceId ->
                    navController.navigate(Screen.MangaDetails.createRoute(mangaId, sourceId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
