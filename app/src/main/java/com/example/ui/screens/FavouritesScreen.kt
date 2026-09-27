package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MangaCard
import com.example.ui.components.TopSearchBar
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuRose
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTealContainer
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

@Composable
fun FavouritesScreen(
    viewModel: MangaViewModel,
    onNavigateToMangaDetails: (String, String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val libraryManga by viewModel.libraryManga.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val categories = listOf("All", "Reading", "Completed", "On-hold", "Plan to read", "Dropped")
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredManga = remember(libraryManga, selectedCategory) {
        if (selectedCategory == "All") {
            libraryManga
        } else {
            libraryManga.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

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
                    placeholder = "Filter in favourites"
                )
            }

            if (libraryManga.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.clearLibrary() }) {
                    Icon(
                        imageVector = Icons.Default.BookmarkRemove,
                        contentDescription = "Clear Favourites",
                        tint = KotatsuRose
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    modifier = Modifier.clickable { selectedCategory = cat },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) KotatsuTealContainer else KotatsuDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) KotatsuTeal else KotatsuCardBorder
                    )
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) KotatsuTeal else KotatsuTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredManga.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = KotatsuRose,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No favourites saved yet",
                        color = KotatsuTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Favourite this' on any manga to save it to your library",
                        color = KotatsuTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = KotatsuTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Discover Manga", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredManga) { manga ->
                    MangaCard(
                        manga = manga,
                        progressPercent = if (manga.readProgressPercent > 0) manga.readProgressPercent else null,
                        isCompleted = manga.readProgressPercent >= 100,
                        onClick = {
                            onNavigateToMangaDetails(manga.id, manga.sourceId)
                        }
                    )
                }
            }
        }
    }
}
