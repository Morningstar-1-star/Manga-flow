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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MangaCard
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTealContainer
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

@Composable
fun SuggestionsScreen(
    viewModel: MangaViewModel,
    onNavigateToMangaDetails: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val genres = listOf("All", "Romance", "Action", "Comedy", "Fantasy", "Drama", "Slice of Life", "Isekai", "Sci-Fi", "Mystery", "Horror", "Superpowers")
    var selectedGenre by remember { mutableStateOf("All") }
    var pageCount by remember { mutableIntStateOf(1) }

    val filteredManga = remember(searchResults, selectedGenre, pageCount) {
        if (selectedGenre == "All") {
            searchResults
        } else {
            searchResults.filter { manga -> manga.genres.any { it.equals(selectedGenre, ignoreCase = true) } }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KotatsuDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = KotatsuTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Smart Recommendations",
                    color = KotatsuTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = {
                viewModel.searchManga(selectedGenre.takeIf { it != "All" } ?: "")
            }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh suggestions",
                    tint = KotatsuTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Genre Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            genres.forEach { genre ->
                val isSelected = genre == selectedGenre
                Surface(
                    modifier = Modifier.clickable {
                        selectedGenre = genre
                        viewModel.searchManga(if (genre == "All") "" else genre)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) KotatsuTealContainer else KotatsuDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) KotatsuTeal else KotatsuCardBorder
                    )
                ) {
                    Text(
                        text = genre,
                        color = if (isSelected) KotatsuTeal else KotatsuTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "Trending Now",
                    color = KotatsuTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    filteredManga.take(8).forEach { manga ->
                        MangaCard(
                            manga = manga,
                            onClick = { onNavigateToMangaDetails(manga.id, manga.sourceId) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Popular in $selectedGenre",
                    color = KotatsuTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    filteredManga.drop(2).take(8).forEach { manga ->
                        MangaCard(
                            manga = manga,
                            onClick = { onNavigateToMangaDetails(manga.id, manga.sourceId) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "All Discovered Titles",
                    color = KotatsuTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredManga) { manga ->
                        MangaCard(
                            manga = manga,
                            onClick = { onNavigateToMangaDetails(manga.id, manga.sourceId) }
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            pageCount++
                            viewModel.searchManga(if (selectedGenre == "All") "page $pageCount" else "$selectedGenre page $pageCount")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KotatsuDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuTeal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Load Unlimited Suggestions", color = KotatsuTeal)
                    }
                }
            }
        }
    }
}
