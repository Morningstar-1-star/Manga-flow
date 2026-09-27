package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FilterChipRow
import com.example.ui.components.MangaCard
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuDarkSurfaceVariant
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTealContainer
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    onNavigateToMangaDetails: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedFilterChip by viewModel.selectedFilterChip.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()

    var showFilterBottomSheet by remember { mutableStateOf(false) }

    val quickFilterChips = listOf("All", "Completed", "Ongoing", "Romance", "Action", "Comedy", "Webtoon")

    val filteredResults = remember(results, searchFilter, selectedFilterChip) {
        results.filter { manga ->
            val matchesStatus = searchFilter.selectedStatus == "All" || manga.status.equals(searchFilter.selectedStatus, ignoreCase = true)
            val matchesChip = selectedFilterChip == "All" ||
                    (selectedFilterChip == "Completed" && manga.status.equals("Completed", ignoreCase = true)) ||
                    (selectedFilterChip == "Ongoing" && manga.status.equals("Ongoing", ignoreCase = true)) ||
                    manga.genres.any { it.equals(selectedFilterChip, ignoreCase = true) }
            val matchesGenres = searchFilter.selectedGenres.isEmpty() ||
                    searchFilter.selectedGenres.all { genre -> manga.genres.any { it.equals(genre, ignoreCase = true) } }

            matchesStatus && matchesChip && matchesGenres
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search manga across sources...", color = KotatsuTextSecondary, fontSize = 14.sp) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = KotatsuTeal)
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = KotatsuTextSecondary)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KotatsuTeal,
                                unfocusedBorderColor = KotatsuCardBorder,
                                focusedContainerColor = KotatsuDarkSurface,
                                unfocusedContainerColor = KotatsuDarkSurface,
                                focusedTextColor = KotatsuTextPrimary,
                                unfocusedTextColor = KotatsuTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Dedicated Filter Button (Requirement 4)
                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clickable { showFilterBottomSheet = true },
                            shape = CircleShape,
                            color = KotatsuDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuTeal)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Search Filter",
                                    tint = KotatsuTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = KotatsuTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KotatsuDarkBg)
            )
        },
        containerColor = KotatsuDarkBg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            FilterChipRow(
                chips = quickFilterChips,
                selectedChip = selectedFilterChip,
                onChipSelected = { viewModel.onFilterChipSelected(it) },
                modifier = Modifier.padding(vertical = 10.dp)
            )

            if (isSearching) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = KotatsuTeal)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Searching across all sources...", color = KotatsuTextSecondary, fontSize = 13.sp)
                    }
                }
            } else if (filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No manga found",
                            color = KotatsuTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search terms or filters",
                            color = KotatsuTeal,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 115.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredResults) { manga ->
                        MangaCard(
                            manga = manga,
                            progressPercent = if (manga.readProgressPercent > 0) manga.readProgressPercent else null,
                            onClick = { onNavigateToMangaDetails(manga.id, manga.sourceId) }
                        )
                    }
                }
            }
        }
    }

    // Advanced Search Filter Bottom Sheet (Requirement 4)
    if (showFilterBottomSheet) {
        val sheetState = rememberModalBottomSheetState()

        var tempStatus by remember { mutableStateOf(searchFilter.selectedStatus) }
        var tempContentRating by remember { mutableStateOf(searchFilter.selectedContentRating) }
        var tempSortBy by remember { mutableStateOf(searchFilter.sortBy) }
        var tempGenres by remember { mutableStateOf(searchFilter.selectedGenres) }

        val allGenres = listOf("Action", "Romance", "Fantasy", "Comedy", "Drama", "Slice of Life", "School Life", "Seinen", "Shoujo", "Shounen", "Isekai", "Webtoon", "Medical", "Mystery")

        ModalBottomSheet(
            onDismissRequest = { showFilterBottomSheet = false },
            sheetState = sheetState,
            containerColor = KotatsuDarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("Search Filters", color = KotatsuTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                // Status Filter
                Text("Publication Status", color = KotatsuTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Ongoing", "Completed").forEach { status ->
                        val isSel = status == tempStatus
                        Surface(
                            modifier = Modifier.clickable { tempStatus = status },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) KotatsuTealContainer else KotatsuDarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) KotatsuTeal else KotatsuCardBorder)
                        ) {
                            Text(status, color = if (isSel) KotatsuTeal else KotatsuTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Genres Filter
                Text("Filter by Genres", color = KotatsuTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allGenres.forEach { genre ->
                        val isSel = tempGenres.contains(genre)
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                tempGenres = if (isSel) tempGenres - genre else tempGenres + genre
                            },
                            label = { Text(genre, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KotatsuTealContainer,
                                selectedLabelColor = KotatsuTeal,
                                containerColor = KotatsuDarkSurfaceVariant,
                                labelColor = KotatsuTextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            tempStatus = "All"
                            tempContentRating = "All"
                            tempSortBy = "Popularity"
                            tempGenres = emptySet()
                            viewModel.applySearchFilter(searchFilter.copy(selectedStatus = "All", selectedGenres = emptySet()))
                            showFilterBottomSheet = false
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset All", color = KotatsuTextSecondary)
                    }

                    Button(
                        onClick = {
                            viewModel.applySearchFilter(
                                searchFilter.copy(
                                    selectedStatus = tempStatus,
                                    selectedContentRating = tempContentRating,
                                    sortBy = tempSortBy,
                                    selectedGenres = tempGenres
                                )
                            )
                            showFilterBottomSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KotatsuTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply Filters", color = Color.White)
                    }
                }
            }
        }
    }
}
