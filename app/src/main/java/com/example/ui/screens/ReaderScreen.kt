package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.model.MangaPage
import com.example.data.model.ReadMode
import com.example.data.translation.TranslatedBubble
import com.example.data.translation.TranslationMode
import com.example.ui.components.ComicTranslationOverlay
import com.example.ui.components.ReaderControlsSheet
import com.example.ui.components.ReaderGridSheet
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel
import kotlinx.coroutines.delay

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
    val translationMode by viewModel.translationMode.collectAsState()
    val readerColorFilter by viewModel.readerColorFilter.collectAsState()
    val readerBrightness by viewModel.readerBrightness.collectAsState()
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
                        colorFilterName = readerColorFilter,
                        isTranslationActive = isTranslationActive,
                        translationMode = translationMode,
                        translatedBubblesMap = translatedBubblesMap,
                        onPageChange = { viewModel.setPageIndex(it) }
                    )
                }
                ReadMode.STANDARD -> {
                    PagedHorizontalReader(
                        pages = pages,
                        isRtl = false,
                        useTwoPageLayout = useTwoPageLayout,
                        colorFilterName = readerColorFilter,
                        isTranslationActive = isTranslationActive,
                        translationMode = translationMode,
                        translatedBubblesMap = translatedBubblesMap,
                        onPageChange = { viewModel.setPageIndex(it) }
                    )
                }
                ReadMode.RTL -> {
                    PagedHorizontalReader(
                        pages = pages,
                        isRtl = true,
                        useTwoPageLayout = useTwoPageLayout,
                        colorFilterName = readerColorFilter,
                        isTranslationActive = isTranslationActive,
                        translationMode = translationMode,
                        translatedBubblesMap = translatedBubblesMap,
                        onPageChange = { viewModel.setPageIndex(it) }
                    )
                }
                ReadMode.VERTICAL -> {
                    PagedVerticalReader(
                        pages = pages,
                        colorFilterName = readerColorFilter,
                        isTranslationActive = isTranslationActive,
                        translationMode = translationMode,
                        translatedBubblesMap = translatedBubblesMap,
                        onPageChange = { viewModel.setPageIndex(it) }
                    )
                }
            }
        }

        // Screen Brightness Overlay
        if (readerBrightness < 0.98f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = (1f - readerBrightness).coerceIn(0f, 0.8f)))
            )
        }

        // Top App Bar
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
                            text = if (isTranslatingPage) "Translating comic page..." else "AI Translation Active (${targetLanguage.uppercase()} • ${translationMode.displayName})",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Controls Bottom Sheet
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
                    currentColorFilter = readerColorFilter,
                    onColorFilterChange = { viewModel.setReaderColorFilter(it) },
                    brightness = readerBrightness,
                    onBrightnessChange = { viewModel.setReaderBrightness(it) },
                    isTranslationActive = isTranslationActive,
                    onTranslationActiveChange = { viewModel.toggleTranslationActive(it) },
                    targetLanguage = targetLanguage,
                    onLanguageChange = { viewModel.setTranslationTargetLanguage(it) },
                    translationMode = translationMode,
                    onTranslationModeChange = { viewModel.setTranslationMode(it) },
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

        // Page Grid Thumbnail Sheet
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

@Composable
private fun WebtoonReader(
    pages: List<MangaPage>,
    autoScroll: Boolean,
    colorFilterName: String,
    isTranslationActive: Boolean,
    translationMode: TranslationMode,
    translatedBubblesMap: Map<Int, List<TranslatedBubble>>,
    onPageChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val imageLoader = context.imageLoader

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
            for (step in 1..3) {
                val nextIdx = index + step
                if (nextIdx in pages.indices) {
                    val req = ImageRequest.Builder(context)
                        .data(pages[nextIdx].imageUrl)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                    imageLoader.enqueue(req)
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(pages, key = { _, page -> "${page.imageUrl}_${page.index}" }) { index, page ->
            ZoomablePageImage(
                imageUrl = page.imageUrl,
                pageNumber = page.index,
                colorFilterName = colorFilterName,
                isTranslationActive = isTranslationActive,
                translationMode = translationMode,
                translatedBubbles = translatedBubblesMap[index]
            )
        }
    }
}

@Composable
private fun PagedHorizontalReader(
    pages: List<MangaPage>,
    isRtl: Boolean,
    useTwoPageLayout: Boolean,
    colorFilterName: String,
    isTranslationActive: Boolean,
    translationMode: TranslationMode,
    translatedBubblesMap: Map<Int, List<TranslatedBubble>>,
    onPageChange: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    LaunchedEffect(pagerState.currentPage) {
        val actualIndex = if (isRtl) pages.size - 1 - pagerState.currentPage else pagerState.currentPage
        onPageChange(actualIndex)

        for (step in 1..3) {
            val nextIdx = actualIndex + step
            if (nextIdx in pages.indices) {
                val req = ImageRequest.Builder(context)
                    .data(pages[nextIdx].imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(req)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 2,
        modifier = Modifier.fillMaxSize(),
        reverseLayout = isRtl
    ) { pageIndex ->
        val actualIdx = if (isRtl) pages.size - 1 - pageIndex else pageIndex
        val actualPage = pages[actualIdx]

        if (useTwoPageLayout && actualIdx + 1 < pages.size) {
            val secondPage = pages[actualIdx + 1]
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    ZoomablePageImage(
                        imageUrl = actualPage.imageUrl,
                        pageNumber = actualPage.index,
                        colorFilterName = colorFilterName,
                        isTranslationActive = isTranslationActive,
                        translationMode = translationMode,
                        translatedBubbles = translatedBubblesMap[actualIdx]
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ZoomablePageImage(
                        imageUrl = secondPage.imageUrl,
                        pageNumber = secondPage.index,
                        colorFilterName = colorFilterName,
                        isTranslationActive = isTranslationActive,
                        translationMode = translationMode,
                        translatedBubbles = translatedBubblesMap[actualIdx + 1]
                    )
                }
            }
        } else {
            ZoomablePageImage(
                imageUrl = actualPage.imageUrl,
                pageNumber = actualPage.index,
                colorFilterName = colorFilterName,
                isTranslationActive = isTranslationActive,
                translationMode = translationMode,
                translatedBubbles = translatedBubblesMap[actualIdx]
            )
        }
    }
}

@Composable
private fun PagedVerticalReader(
    pages: List<MangaPage>,
    colorFilterName: String,
    isTranslationActive: Boolean,
    translationMode: TranslationMode,
    translatedBubblesMap: Map<Int, List<TranslatedBubble>>,
    onPageChange: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    LaunchedEffect(pagerState.currentPage) {
        val index = pagerState.currentPage
        onPageChange(index)

        for (step in 1..3) {
            val nextIdx = index + step
            if (nextIdx in pages.indices) {
                val req = ImageRequest.Builder(context)
                    .data(pages[nextIdx].imageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(req)
            }
        }
    }

    VerticalPager(
        state = pagerState,
        beyondViewportPageCount = 2,
        modifier = Modifier.fillMaxSize()
    ) { pageIndex ->
        ZoomablePageImage(
            imageUrl = pages[pageIndex].imageUrl,
            pageNumber = pages[pageIndex].index,
            colorFilterName = colorFilterName,
            isTranslationActive = isTranslationActive,
            translationMode = translationMode,
            translatedBubbles = translatedBubblesMap[pageIndex]
        )
    }
}

@Composable
private fun ZoomablePageImage(
    imageUrl: String,
    pageNumber: Int,
    colorFilterName: String = "None",
    isTranslationActive: Boolean = false,
    translationMode: TranslationMode = TranslationMode.ENGLISH_TYPESETTING,
    translatedBubbles: List<TranslatedBubble>? = null
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var reloadCount by remember { mutableIntStateOf(0) }

    val colorFilter = remember(colorFilterName) {
        when (colorFilterName) {
            "Invert" -> ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "Grayscale" -> ColorFilter.colorMatrix(
                ColorMatrix().apply { setToSaturation(0f) }
            )
            "Sepia" -> ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            "High Contrast" -> ColorFilter.colorMatrix(
                ColorMatrix(
                    floatArrayOf(
                        1.4f, 0f, 0f, 0f, -30f,
                        0f, 1.4f, 0f, 0f, -30f,
                        0f, 0f, 1.4f, 0f, -30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
            else -> null
        }
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5.0f)
        offset = if (scale > 1f) offset + offsetChange else Offset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.2f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    }
                )
            }
            .transformable(state = transformState)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            ),
        contentAlignment = Alignment.Center
    ) {
        val imageModel = remember(imageUrl, reloadCount) {
            ImageRequest.Builder(context)
                .data(imageUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .setParameter("reload", reloadCount)
                .build()
        }

        SubcomposeAsyncImage(
            model = imageModel,
            contentDescription = "Page $pageNumber",
            contentScale = ContentScale.FillWidth,
            colorFilter = colorFilter,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color(0xFF1F1D24)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Page $pageNumber",
                        color = KotatsuTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { reloadCount++ },
                        shape = CircleShape,
                        border = BorderStroke(1.dp, KotatsuTeal)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = KotatsuTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry loading", color = KotatsuTeal, fontSize = 12.sp)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Translation Overlay
        if (isTranslationActive && !translatedBubbles.isNullOrEmpty()) {
            ComicTranslationOverlay(
                bubbles = translatedBubbles,
                mode = translationMode,
                modifier = Modifier.matchParentSize()
            )
        }

        // Floating Zoom Controls Bar
        if (scale > 1.05f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.82f),
                border = BorderStroke(1.dp, KotatsuTeal)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        color = KotatsuTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { scale = (scale + 0.5f).coerceAtMost(5.0f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = {
                            scale = (scale - 0.5f).coerceAtLeast(1.0f)
                            if (scale <= 1.0f) offset = Offset.Zero
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("-", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = {
                            scale = 1.0f
                            offset = Offset.Zero
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("1:1", color = KotatsuTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
