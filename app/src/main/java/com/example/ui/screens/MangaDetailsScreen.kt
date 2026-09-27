package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Chapter
import com.example.data.model.Manga
import com.example.ui.components.ChapterControlsHeader
import com.example.ui.components.ChapterListItem
import com.example.ui.components.LanguageFilterTabs
import com.example.ui.components.MangaInfoCard
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuDarkSurfaceHigh
import com.example.ui.theme.KotatsuDarkSurfaceVariant
import com.example.ui.theme.KotatsuRose
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTealContainer
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

/**
 * Manga Details Screen - Replicating Screenshots 2 (Tablet/Landscape) & 6 (Phone)!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailsScreen(
    mangaId: String,
    sourceId: String,
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    onReadChapter: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(mangaId) {
        viewModel.loadMangaDetails(mangaId, sourceId)
    }

    val manga by viewModel.selectedManga.collectAsState()
    val chapters by viewModel.mangaChapters.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val showBookmarksOnly by viewModel.showBookmarksOnly.collectAsState()

    var showCategoryDialog by remember { mutableStateOf(false) }

    val filteredChapters = remember(chapters, showBookmarksOnly) {
        if (showBookmarksOnly) chapters.filter { it.isBookmarked } else chapters
    }

    val currentManga = manga ?: return

    val continueChapter = remember(chapters, currentManga) {
        chapters.find { it.id == currentManga.lastReadChapterId }
            ?: chapters.firstOrNull { !it.isRead }
            ?: chapters.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = KotatsuTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Reading ${currentManga.title} on Kotatsu!")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Manga"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = KotatsuTextPrimary
                        )
                    }
                    IconButton(onClick = {
                        // Download next 3 unread chapters
                        val toDownload = chapters.filter { !it.isRead && !it.isDownloaded }.take(3)
                        toDownload.forEach { ch ->
                            viewModel.downloadChapter(currentManga, ch)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download All",
                            tint = KotatsuTextPrimary
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = KotatsuTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTabletOrLandscape = maxWidth > 680.dp

            if (isTabletOrLandscape) {
                // Two-Pane Tablet Layout (Screenshot 2)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Pane: Metadata & Info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        MangaHeaderSection(
                            manga = currentManga,
                            onFavoriteClick = { showCategoryDialog = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        MangaInfoCard(
                            sourceName = currentManga.sourceId.replaceFirstChar { it.uppercase() },
                            author = currentManga.author,
                            status = currentManga.status,
                            chaptersText = "${currentManga.lastReadChapterName ?: "Chapter 1"} of ${currentManga.totalChapters} (${currentManga.totalChapters * 3} m)",
                            progressPercent = currentManga.readProgressPercent
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        MangaDescriptionSection(description = currentManga.description)

                        Spacer(modifier = Modifier.height(14.dp))

                        MangaTagsSection(genres = currentManga.genres)
                    }

                    // Right Pane: Chapters (Screenshot 2)
                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .padding(top = 8.dp, bottom = 16.dp, end = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ChapterControlsHeader(
                                isGridView = isGridView,
                                onToggleGridView = { viewModel.toggleGridView() },
                                showBookmarksOnly = showBookmarksOnly,
                                onToggleBookmarksOnly = { viewModel.toggleBookmarksOnly() },
                                continueButtonText = if (currentManga.readProgressPercent > 0) "Continue" else "Read",
                                onContinueClick = {
                                    continueChapter?.let {
                                        onReadChapter(currentManga.id, it.id, currentManga.sourceId)
                                    }
                                }
                            )

                            LanguageFilterTabs(
                                languages = listOf("all", "vi", "en", "es", "fr"),
                                selectedLanguage = selectedLanguage,
                                onLanguageSelected = { viewModel.selectLanguage(it) }
                            )

                            ChapterListContent(
                                chapters = filteredChapters,
                                currentChapterId = currentManga.lastReadChapterId,
                                isGridView = isGridView,
                                onChapterClick = { ch ->
                                    onReadChapter(currentManga.id, ch.id, currentManga.sourceId)
                                },
                                onBookmarkClick = { ch ->
                                    viewModel.toggleChapterBookmark(ch, currentManga)
                                },
                                onDownloadClick = { ch ->
                                    viewModel.downloadChapter(currentManga, ch)
                                }
                            )
                        }
                    }
                }
            } else {
                // Mobile Vertical Layout (Screenshot 6)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            MangaHeaderSection(
                                manga = currentManga,
                                onFavoriteClick = { showCategoryDialog = true }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            MangaInfoCard(
                                sourceName = currentManga.sourceId.replaceFirstChar { it.uppercase() },
                                author = currentManga.author,
                                status = currentManga.status,
                                chaptersText = "${currentManga.lastReadChapterName ?: "Chapter 13"} of ${currentManga.totalChapters} (5 m)",
                                progressPercent = currentManga.readProgressPercent
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            MangaDescriptionSection(description = currentManga.description)

                            Spacer(modifier = Modifier.height(14.dp))

                            MangaTagsSection(genres = currentManga.genres)

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Bottom Sheet / Panel styled Chapter List (Screenshot 6)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .width(36.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(KotatsuTextSecondary.copy(alpha = 0.4f))
                                )

                                ChapterControlsHeader(
                                    isGridView = isGridView,
                                    onToggleGridView = { viewModel.toggleGridView() },
                                    showBookmarksOnly = showBookmarksOnly,
                                    onToggleBookmarksOnly = { viewModel.toggleBookmarksOnly() },
                                    continueButtonText = if (currentManga.readProgressPercent > 0) "Continue" else "Read",
                                    onContinueClick = {
                                        continueChapter?.let {
                                            onReadChapter(currentManga.id, it.id, currentManga.sourceId)
                                        }
                                    }
                                )

                                LanguageFilterTabs(
                                    languages = listOf("all", "vi", "en", "es", "fr"),
                                    selectedLanguage = selectedLanguage,
                                    onLanguageSelected = { viewModel.selectLanguage(it) }
                                )
                            }
                        }
                    }

                    items(filteredChapters) { chapter ->
                        Surface(color = KotatsuDarkSurface) {
                            ChapterListItem(
                                chapter = chapter,
                                isCurrentReading = chapter.id == currentManga.lastReadChapterId,
                                onClick = {
                                    onReadChapter(currentManga.id, chapter.id, currentManga.sourceId)
                                },
                                onBookmarkClick = {
                                    viewModel.toggleChapterBookmark(chapter, currentManga)
                                },
                                onDownloadClick = {
                                    viewModel.downloadChapter(currentManga, chapter)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Category Selector Bottom Sheet
    if (showCategoryDialog) {
        val categories = listOf("Reading", "Completed", "On-hold", "Plan to read", "Dropped")
        ModalBottomSheet(
            onDismissRequest = { showCategoryDialog = false },
            containerColor = KotatsuDarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Select Category",
                    color = KotatsuTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                categories.forEach { cat ->
                    val isSelected = currentManga.category == cat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.toggleFavorite(currentManga, cat)
                                showCategoryDialog = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) KotatsuTeal else KotatsuTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Text(text = "✓", color = KotatsuTeal, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Remove from library option
                if (currentManga.inLibrary) {
                    Text(
                        text = "Remove from library",
                        color = Color(0xFFEF4444),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.toggleFavorite(currentManga)
                                showCategoryDialog = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MangaHeaderSection(
    manga: Manga,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Cover Image (rounded corners, matches Screenshot 2 & 6!)
        AsyncImage(
            model = manga.coverUrl,
            contentDescription = manga.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(115.dp)
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(16.dp))
                .background(KotatsuDarkSurfaceVariant)
                .border(1.dp, KotatsuCardBorder, RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = manga.title,
                color = KotatsuTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            if (manga.altTitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = manga.altTitle,
                    color = KotatsuTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "Favourite this ▾" Pill Button (matches Screenshot 2 & 6!)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onFavoriteClick)
                    .testTag("favourite_button"),
                shape = RoundedCornerShape(18.dp),
                color = if (manga.inLibrary) KotatsuTeal.copy(alpha = 0.2f) else KotatsuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (manga.inLibrary) KotatsuTeal else KotatsuCardBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (manga.inLibrary) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (manga.inLibrary) KotatsuRose else KotatsuTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (manga.inLibrary) manga.category else "Favourite this",
                        color = KotatsuTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = KotatsuTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MangaDescriptionSection(description: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Description",
                color = KotatsuTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (expanded) "Less" else "More",
                color = KotatsuTeal,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            color = KotatsuTextSecondary,
            fontSize = 13.sp,
            maxLines = if (expanded) 100 else 3,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun MangaTagsSection(genres: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.forEach { genre ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = KotatsuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
            ) {
                Text(
                    text = genre,
                    color = KotatsuTextPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ChapterListContent(
    chapters: List<Chapter>,
    currentChapterId: String?,
    isGridView: Boolean,
    onChapterClick: (Chapter) -> Unit,
    onBookmarkClick: (Chapter) -> Unit,
    onDownloadClick: (Chapter) -> Unit
) {
    if (isGridView) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(chapters) { ch ->
                val isSelected = ch.id == currentChapterId
                Card(
                    modifier = Modifier
                        .clickable { onChapterClick(ch) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) KotatsuTealContainer else KotatsuDarkSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) KotatsuTeal else KotatsuCardBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ch. ${ch.number.toInt()}",
                            color = if (isSelected) KotatsuTeal else KotatsuTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(chapters) { ch ->
                ChapterListItem(
                    chapter = ch,
                    isCurrentReading = ch.id == currentChapterId,
                    onClick = { onChapterClick(ch) },
                    onBookmarkClick = { onBookmarkClick(ch) },
                    onDownloadClick = { onDownloadClick(ch) }
                )
            }
        }
    }
}
