package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Manga
import com.example.ui.components.QuickActionTile
import com.example.ui.components.SourceGridItem
import com.example.ui.components.TopSearchBar
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuDarkSurfaceVariant
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTextMuted
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

/**
 * Explore Screen - Replicating Screenshot 3!
 */
@Composable
fun ExploreScreen(
    viewModel: MangaViewModel,
    onNavigateToLocalStorage: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToMangaDetails: (String, String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sources by viewModel.sources.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KotatsuDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Search Bar (Clicking opens search or performs instant search)
        TopSearchBar(
            query = searchQuery,
            onQueryChange = {
                viewModel.onSearchQueryChanged(it)
                if (it.isNotEmpty()) onNavigateToSearch()
            },
            onMenuClick = onOpenSettings,
            placeholder = "Search manga"
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 4 Quick Action Cards in 2x2 grid (Screenshot 3)
            item(span = { GridItemSpan(2) }) {
                QuickActionTile(
                    title = "Local storage",
                    icon = Icons.Default.SdStorage,
                    onClick = onNavigateToLocalStorage
                )
            }
            item(span = { GridItemSpan(2) }) {
                QuickActionTile(
                    title = "Bookmarks",
                    icon = Icons.Outlined.BookmarkBorder,
                    onClick = onNavigateToBookmarks
                )
            }
            item(span = { GridItemSpan(2) }) {
                QuickActionTile(
                    title = "Random",
                    icon = Icons.Default.Casino,
                    onClick = {
                        val popular = viewModel.searchResults.value
                        if (popular.isNotEmpty()) {
                            val random = popular.random()
                            onNavigateToMangaDetails(random.id, random.sourceId)
                        }
                    }
                )
            }
            item(span = { GridItemSpan(2) }) {
                QuickActionTile(
                    title = "Downloads",
                    icon = Icons.Default.FileDownload,
                    onClick = onNavigateToDownloads
                )
            }

            // Suggestions Section Header
            item(span = { GridItemSpan(4) }) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suggestions",
                        color = KotatsuTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "More",
                        color = KotatsuTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onNavigateToSearch() }
                            .padding(4.dp)
                    )
                }
            }

            // Suggestions Featured Card (Screenshot 3: "Machikado Mazoku")
            item(span = { GridItemSpan(4) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNavigateToMangaDetails("mangadex_non_milk_coffee", "mangadex")
                        }
                        .testTag("featured_suggestion_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
                            contentDescription = "Machikado Mazoku",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KotatsuDarkSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Machikado Mazoku",
                                color = KotatsuTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Subtext, Slice of Life, Demon, Comedy, Mahou Shoujo, 4-koma, Anime",
                                color = KotatsuTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    // Pager dot indicators
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(KotatsuTeal)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(KotatsuTextMuted)
                        )
                    }
                }
            }

            // Manga sources Section Header (Screenshot 3)
            item(span = { GridItemSpan(4) }) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manga sources",
                        color = KotatsuTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Catalog",
                        color = KotatsuTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToCatalog)
                            .padding(4.dp)
                    )
                }
            }

            // Sources Grid (4 columns, with pin 📌 on pinned sources!)
            items(sources) { source ->
                SourceGridItem(
                    source = source,
                    onClick = {
                        viewModel.onSearchQueryChanged("")
                        onNavigateToSearch()
                    }
                )
            }
        }
    }
}
