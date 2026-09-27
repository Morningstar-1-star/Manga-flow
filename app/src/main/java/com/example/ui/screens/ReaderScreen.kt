package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import com.example.data.translation.TranslatedBubble
import com.example.ui.components.ComicTranslationOverlay
import com.example.ui.components.ReaderGridSheet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.MangaPage
import com.example.data.model.ReadMode
import com.example.ui.components.ReaderControlsSheet
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reader Screen - Replicating Screenshot 5!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    mangaId: String,
    chapterId: String,
    sourceId: String,
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Keep screen on while reading
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(chapterId) {
        viewModel.openReader(mangaId, chapterId, sourceId)
    }

    val manga by viewModel.selectedManga.collectAsState()
    val chapters by viewModel.mangaChapters.collectAsState()
    val pages by viewModel.readerPages.collectAsState()
    val readMode by viewModel.currentReaderMode.collectAsState()
    val useTwoPageLayout by viewModel.useTwoPageLayout.collectAsState()
    val autoScroll by viewModel.autoScroll.collectAsState()

    val isTranslationActive by viewModel.isTranslationActive.collectAsState()
    val isTranslatingPage by viewModel.isTranslatingPage.collectAsState()
    val targetLanguage by viewModel.translationTargetLanguage.collectAsState()
    val translatedBubblesMap by viewModel.translatedBubblesMap.collectAsState()
    val readerColorFilter by viewModel.readerColorFilter.collectAsState()
    val currentPageIndex by viewModel.currentPageIndex.collectAsState()

    var showControls by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    var showGridSheet by remember { mutableStateOf(false) }

    val currentChapter = remember(chapters, chapterId) {
        chapters.find { it.id == chapterId }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    }
                )
            }
            .testTag("reader_viewport")
    ) {
        if (pages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = KotatsuTeal)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading chapter pages...",
                        color = KotatsuTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            when (readMode) {
                ReadMode.WEBTOON -> {
                    WebtoonReader(
                        pages = pages,
                        autoScroll = autoScroll,
                        isTranslationActive = isTranslationActive,
                        translatedBubblesMap = translatedBubblesMap,
                        onPageChange = { viewModel.setPageIndex(it) }
                    )
                }
                ReadMode.STANDARD -> {
                    PagedHorizontalReader(
                        pages = pages,
                        isRtl = false,
                        isTranslationActive = isTranslationActive,
                        translatedBubblesMap = translatedBubblesMap,
                        onPageChange = { viewModel.setPageIndex(it) }
                    )
                }
                ReadMode.RTL -> {
                    PagedHorizontalReader(
                        pages = pages,
                        isRtl = true,
                        isTranslationActive = isTranslationActive,
                        translatedBubblesMap = translatedBubblesMap,
                        onPageChange = { viewModel.setPageIndex(it) }
                    )
                }
                ReadMode.VERTICAL -> {
                    PagedVerticalReader(
                        pages = pages,
                        isTranslationActive = isTranslationActive,
                        translatedBubblesMap = translatedBubblesMap,
                        onPageChange = { viewModel.setPageIndex(it) }
                    )
                }
            }
        }

        // Top App Bar (shown when tapped, matching Screenshot 5 top bar!)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = manga?.title ?: "Manga Reader",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentChapter?.name ?: "Chapter",
                            color = KotatsuTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Bookmark toggle
                    IconButton(onClick = {
                        if (currentChapter != null && manga != null) {
                            viewModel.toggleChapterBookmark(currentChapter, manga!!)
                        }
                    }) {
                        Icon(
                            imageVector = if (currentChapter?.isBookmarked == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (currentChapter?.isBookmarked == true) KotatsuTeal else Color.White
                        )
                    }

                    // AI Translate Page Toggle
                    IconButton(onClick = {
                        viewModel.toggleTranslationActive()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate Page",
                            tint = if (isTranslationActive) KotatsuTeal else Color.White
                        )
                    }

                    // Page Grid Thumbnail Sheet Toggle
                    IconButton(onClick = { showGridSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Page Grid",
                            tint = Color.White
                        )
                    }

                    // Open Controls Bottom Sheet
                    IconButton(onClick = { showSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Controls",
                            tint = KotatsuTeal
                        )
                    }
                }

                if (isTranslationActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KotatsuTeal.copy(alpha = 0.9f))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isTranslatingPage) "Translating comic page..." else "AI Translation Active (${targetLanguage.uppercase()})",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Controls Bottom Sheet (Matches Screenshot 5)
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                containerColor = KotatsuDarkSurface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ReaderControlsSheet(
                    currentMode = readMode,
                    onModeSelected = {
                        viewModel.setReadMode(it)
                    },
                    useTwoPageLayout = useTwoPageLayout,
                    onTwoPageLayoutChange = { viewModel.toggleTwoPageLayout(it) },
                    autoScroll = autoScroll,
                    onAutoScrollChange = { viewModel.toggleAutoScroll(it) },
                    isTranslationActive = isTranslationActive,
                    onTranslationActiveChange = { viewModel.toggleTranslationActive(it) },
                    targetLanguage = targetLanguage,
                    onLanguageChange = { viewModel.setTranslationTargetLanguage(it) },
                    onSavePage = {
                        showSheet = false
                    },
                    onRotateScreen = {
                        val act = context as? Activity
                        val currentOrientation = act?.requestedOrientation
                        act?.requestedOrientation = if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        } else {
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }
                    },
                    onColorCorrection = { },
                    onImageQualityClick = { },
                    onOpenSettings = {
                        showSheet = false
                        onOpenSettings()
                    }
                )
            }
        }

        // Page Grid Thumbnail Sheet (Matches Screenshot 3!)
        if (showGridSheet) {
            ModalBottomSheet(
                onDismissRequest = { showGridSheet = false },
                containerColor = KotatsuDarkSurface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ReaderGridSheet(
                    pages = pages,
                    currentPageIndex = currentPageIndex,
                    chapterTitle = currentChapter?.name ?: "Chapter",
                    onPageSelected = { idx ->
                        viewModel.setPageIndex(idx)
                    },
                    onDismiss = { showGridSheet = false }
                )
            }
        }
    }
}

/**
 * Continuous Webtoon Vertical Scrolling Mode
 */
@Composable
private fun WebtoonReader(
    pages: List<MangaPage>,
    autoScroll: Boolean,
    isTranslationActive: Boolean,
    translatedBubblesMap: Map<Int, List<TranslatedBubble>>,
    onPageChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    // Smooth auto scroll loop if enabled
    LaunchedEffect(autoScroll) {
        if (autoScroll) {
            while (true) {
                delay(40)
                listState.scrollBy(3f)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            onPageChange(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(pages) { index, page ->
            ZoomablePageImage(
                imageUrl = page.imageUrl,
                pageNumber = index + 1,
                isTranslationActive = isTranslationActive,
                translatedBubbles = translatedBubblesMap[index]
            )
        }
    }
}

/**
 * Horizontal Paged Reader (LTR and RTL)
 */
@Composable
private fun PagedHorizontalReader(
    pages: List<MangaPage>,
    isRtl: Boolean,
    isTranslationActive: Boolean,
    translatedBubblesMap: Map<Int, List<TranslatedBubble>>,
    onPageChange: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(pagerState.currentPage) {
        val actualIndex = if (isRtl) pages.size - 1 - pagerState.currentPage else pagerState.currentPage
        onPageChange(actualIndex)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        reverseLayout = isRtl
    ) { pageIndex ->
        val actualIdx = if (isRtl) pages.size - 1 - pageIndex else pageIndex
        val actualPage = pages[actualIdx]
        ZoomablePageImage(
            imageUrl = actualPage.imageUrl,
            pageNumber = actualPage.index,
            isTranslationActive = isTranslationActive,
            translatedBubbles = translatedBubblesMap[actualIdx]
        )
    }
}

/**
 * Vertical Paged Reader
 */
@Composable
private fun PagedVerticalReader(
    pages: List<MangaPage>,
    isTranslationActive: Boolean,
    translatedBubblesMap: Map<Int, List<TranslatedBubble>>,
    onPageChange: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        ZoomablePageImage(
            imageUrl = pages[pageIndex].imageUrl,
            pageNumber = pages[pageIndex].index,
            isTranslationActive = isTranslationActive,
            translatedBubbles = translatedBubblesMap[pageIndex]
        )
    }
}

@Composable
private fun ZoomablePageImage(
    imageUrl: String,
    pageNumber: Int,
    isTranslationActive: Boolean = false,
    translatedBubbles: List<TranslatedBubble>? = null
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 3.5f)
        offset = if (scale > 1f) offset + offsetChange else Offset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .transformable(state = transformState)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            ),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Page $pageNumber",
            contentScale = ContentScale.FillWidth,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .background(Color(0xFF14161B)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = KotatsuTeal,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFF1F1D24)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Page $pageNumber",
                        color = KotatsuTextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Translation Overlay
        if (isTranslationActive && !translatedBubbles.isNullOrEmpty()) {
            ComicTranslationOverlay(
                bubbles = translatedBubbles,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}
