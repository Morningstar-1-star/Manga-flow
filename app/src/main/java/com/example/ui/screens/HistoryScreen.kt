package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoryItem
import com.example.data.model.Manga
import com.example.ui.components.FilterChipRow
import com.example.ui.components.MangaCard
import com.example.ui.components.TopSearchBar
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuRose
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

@Composable
fun HistoryScreen(
    viewModel: MangaViewModel,
    onNavigateToMangaDetails: (String, String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val historyItems by viewModel.historyItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilterChip by viewModel.selectedFilterChip.collectAsState()

    val filterChips = listOf(
        "On device",
        "New chapters",
        "Completed",
        "Favourites",
        "Not in favourites",
        "18+ NSFW",
        "Self-Published"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KotatsuDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TopSearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        viewModel.onSearchQueryChanged(it)
                        if (it.isNotEmpty()) onNavigateToSearch()
                    },
                    onMenuClick = onOpenSettings,
                    placeholder = "Search history..."
                )
            }

            if (historyItems.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = KotatsuRose
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilterChipRow(
            chips = filterChips,
            selectedChip = selectedFilterChip,
            onChipSelected = { viewModel.onFilterChipSelected(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = KotatsuTextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your reading history is empty",
                        color = KotatsuTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Read chapters to automatically track reading progress here",
                        color = KotatsuTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = KotatsuTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Browse Catalog", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        } else {
        if (historyItems.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Text(
                            text = "Today",
                            color = KotatsuTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            historyItems.forEach { item ->
                                MangaCard(
                                    manga = Manga(
                                        id = item.mangaId,
                                        sourceId = "mangadex",
                                        title = item.title,
                                        coverUrl = item.coverUrl,
                                        readProgressPercent = item.progressPercent
                                    ),
                                    progressPercent = item.progressPercent,
                                    isCompleted = item.isCompleted,
                                    onClick = {
                                        onNavigateToMangaDetails(item.mangaId, "mangadex")
                                    }
                                )
                            }
                        }
                    }
                }

                // Bottom Floating "Continue" Button (Screenshot 6)
                val latest = historyItems.firstOrNull()
                if (latest != null) {
                    Button(
                        onClick = {
                            onNavigateToMangaDetails(latest.mangaId, "mangadex")
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 90.dp, end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KotatsuDarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuTeal)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = KotatsuTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue",
                                color = KotatsuTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
