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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
    val activeSourceId by viewModel.selectedSourceId.collectAsState()
    val allSources by viewModel.sources.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMorePages by viewModel.hasMorePages.collectAsState()
    val selectedFilterChip by viewModel.selectedFilterChip.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()

    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember(gridState) {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isSearching && !isLoadingMore && hasMorePages) {
            viewModel.loadNextPage()
        }
    }

    var showFilterBottomSheet by remember { mutableStateOf(false) }

    val quickFilterChips = listOf("All", "Completed", "Ongoing", "Romance", "Action", "Comedy", "Webtoon")
    
    // Dynamic source list matching all enabled sources
    val sourceFilterChips = remember(allSources) {
        listOf("All Sources") + allSources.filter { it.isEnabled }.map { it.name }
    }

    val currentChipName = remember(activeSourceId, allSources) {
        if (activeSourceId == null) {
            "All Sources"
        } else {
            allSources.find { it.id.equals(activeSourceId, ignoreCase = true) }?.name
                ?: activeSourceId ?: "All Sources"
        }
    }

    val filteredResults = remember(results, searchFilter, selectedFilterChip, activeSourceId) {
        results.filter { manga ->
            val matchesSource = activeSourceId == null || manga.sourceId.equals(activeSourceId, ignoreCase = true)

            val matchesStatus = searchFilter.selectedStatus == "All" || manga.status.equals(searchFilter.selectedStatus, ignoreCase = true)
            val matchesChip = selectedFilterChip == "All" ||
                    (selectedFilterChip == "Completed" && manga.status.equals("Completed", ignoreCase = true)) ||
                    (selectedFilterChip == "Ongoing" && manga.status.equals("Ongoing", ignoreCase = true)) ||
                    manga.genres.any { it.equals(selectedFilterChip, ignoreCase = true) }
            val matchesGenres = searchFilter.selectedGenres.isEmpty() ||
                    searchFilter.selectedGenres.all { genre -> manga.genres.any { it.equals(genre, ignoreCase = true) } }

            matchesSource && matchesStatus && matchesChip && matchesGenres
        }
    }

    val appBgColor = MaterialTheme.colorScheme.background
    val appSurfaceColor = MaterialTheme.colorScheme.surface

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
                                focusedContainerColor = appSurfaceColor,
                                unfocusedContainerColor = appSurfaceColor,
                                focusedTextColor = KotatsuTextPrimary,
                                unfocusedTextColor = KotatsuTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Dedicated Filter Button
                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clickable { showFilterBottomSheet = true },
                            shape = CircleShape,
                            color = appSurfaceColor,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appBgColor)
            )
        },
        containerColor = appBgColor,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Source filter bar with ALL sources!
            FilterChipRow(
                chips = sourceFilterChips,
                selectedChip = currentChipName,
                onChipSelected = { chip ->
                    if (chip == "All Sources") {
                        viewModel.selectSourceFeed(null)
                    } else {
                        val found = allSources.find { it.name.equals(chip, ignoreCase = true) }
                        viewModel.selectSourceFeed(found?.id)
                    }
                },
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            // Genre & Status filter bar
            FilterChipRow(
                chips = quickFilterChips,
                selectedChip = selectedFilterChip,
                onChipSelected = { viewModel.onFilterChipSelected(it) },
                modifier = Modifier.padding(bottom = 8.dp)
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = if (query.isNotBlank()) "No manga found for \"$query\"" else "No manga loaded",
                            color = KotatsuTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (activeSourceId != null) "Source '$currentChipName' returned no items for this query" else "Try searching across popular titles or reload",
                            color = KotatsuTextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (query.isNotBlank()) viewModel.searchManga(query)
                                    else viewModel.loadSourceManga(activeSourceId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KotatsuTeal, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reload Feed", fontWeight = FontWeight.SemiBold)
                            }
                            if (activeSourceId != null) {
                                OutlinedButton(
                                    onClick = { viewModel.selectSourceFeed(null) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("All Sources", color = KotatsuTeal)
                                }
                            }
                        }
                    }
                }
            } else if (activeSourceId == null && query.isNotEmpty()) {
                // Multi-source search with clear separation by source!
                val groupedBySource = remember(filteredResults) {
                    filteredResults.groupBy { it.sourceId }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    groupedBySource.forEach { (sourceId, sourceMangaList) ->
                        val sourceObj = allSources.find { it.id.equals(sourceId, ignoreCase = true) }
                        val srcDisplayName = sourceObj?.name ?: sourceId
                        val srcEmoji = sourceObj?.iconEmoji ?: "📖"

                        item(key = "header_$sourceId") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(srcEmoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = srcDisplayName,
                                        color = KotatsuTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = KotatsuTealContainer
                                    ) {
                                        Text(
                                            text = "${sourceMangaList.size}",
                                            color = KotatsuTeal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Focus Source →",
                                    color = KotatsuTeal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable { viewModel.selectSourceFeed(sourceId) }
                                        .padding(4.dp)
                                )
                            }
                        }

                        item(key = "row_$sourceId") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(sourceMangaList) { manga ->
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
            } else {
                LazyVerticalGrid(
                    state = gridState,
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

                    if (isLoadingMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = KotatsuTeal,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Loading more titles...",
                                        color = KotatsuTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else if (hasMorePages && filteredResults.size >= 15) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.loadNextPage() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KotatsuTeal)
                                ) {
                                    Text("Load More Manga (${filteredResults.size} loaded)", fontSize = 13.sp)
                                }
                            }
                        }
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
