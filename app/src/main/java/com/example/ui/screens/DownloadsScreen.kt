package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadStatus
import com.example.ui.components.ActiveDownloadCard
import com.example.ui.components.DownloadedItemCard
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

/**
 * Downloads Screen - Replicating Screenshot 4!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    onNavigateToMangaDetails: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.downloads.collectAsState()

    val activeTask = downloads.find { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PAUSED }
    val completedTasks = downloads.filter { it.id != activeTask?.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Downloads",
                        color = KotatsuTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                actions = {
                    IconButton(onClick = { viewModel.pauseAllDownloads() }) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause all",
                            tint = KotatsuTextPrimary
                        )
                    }
                    IconButton(onClick = { viewModel.resumeAllDownloads() }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume all",
                            tint = KotatsuTextPrimary
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Active Download Card (Screenshot 4)
            if (activeTask != null) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    ActiveDownloadCard(
                        task = activeTask,
                        onPauseClick = {
                            if (activeTask.status == DownloadStatus.PAUSED) {
                                viewModel.resumeAllDownloads()
                            } else {
                                viewModel.pauseAllDownloads()
                            }
                        },
                        onCancelClick = {
                            viewModel.cancelDownload(activeTask.id)
                        },
                        onClick = {
                            onNavigateToMangaDetails(activeTask.mangaId, "comick")
                        }
                    )
                }
            }

            // Section "Just now" (Screenshot 4)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Just now",
                    color = KotatsuTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Completed / Canceled items
            items(completedTasks) { task ->
                DownloadedItemCard(
                    task = task,
                    onClick = {
                        onNavigateToMangaDetails(task.mangaId, "mangadex")
                    }
                )
            }
        }
    }
}
