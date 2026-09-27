package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chapter
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuDarkSurfaceVariant
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTealContainer
import com.example.ui.theme.KotatsuTextMuted
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary

@Composable
fun ChapterControlsHeader(
    isGridView: Boolean,
    onToggleGridView: () -> Unit,
    showBookmarksOnly: Boolean,
    onToggleBookmarksOnly: () -> Unit,
    continueButtonText: String = "Continue",
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // List View toggle
        IconButton(
            onClick = { if (isGridView) onToggleGridView() },
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (!isGridView) KotatsuDarkSurfaceVariant else Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.ViewList,
                contentDescription = "List View",
                tint = if (!isGridView) KotatsuTeal else KotatsuTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Grid View toggle
        IconButton(
            onClick = { if (!isGridView) onToggleGridView() },
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isGridView) KotatsuDarkSurfaceVariant else Color.Transparent)
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Grid View",
                tint = if (isGridView) KotatsuTeal else KotatsuTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Bookmark filter toggle
        IconButton(
            onClick = onToggleBookmarksOnly,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (showBookmarksOnly) KotatsuDarkSurfaceVariant else Color.Transparent)
        ) {
            Icon(
                imageVector = if (showBookmarksOnly) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Bookmarks filter",
                tint = if (showBookmarksOnly) KotatsuTeal else KotatsuTextSecondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Big Teal "Continue ▾" Button (matches Screenshot 2 & 6!)
        Surface(
            modifier = Modifier
                .height(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .clickable(onClick = onContinueClick)
                .testTag("continue_reading_button"),
            shape = RoundedCornerShape(21.dp),
            color = KotatsuTeal
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = continueButtonText,
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun LanguageFilterTabs(
    languages: List<String>,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        languages.forEach { lang ->
            val isSelected = lang == selectedLanguage
            val displayName = when (lang) {
                "en" -> "English"
                "vi" -> "Tiếng Việt"
                "es" -> "Español"
                "fr" -> "Français"
                "ja" -> "Japanese"
                "all" -> "All"
                else -> lang.replaceFirstChar { it.uppercase() }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onLanguageSelected(lang) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) KotatsuTealContainer else KotatsuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) KotatsuTeal else KotatsuCardBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = KotatsuTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = displayName,
                        color = if (isSelected) KotatsuTeal else KotatsuTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun ChapterListItem(
    chapter: Chapter,
    isCurrentReading: Boolean = false,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("chapter_item_${chapter.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active reading indicator play icon ▶ (matches Screenshot 2!)
        if (isCurrentReading) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Currently reading",
                tint = KotatsuTeal,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chapter.name,
                color = if (isCurrentReading) KotatsuTeal else if (chapter.isRead) KotatsuTextMuted else KotatsuTextPrimary,
                fontSize = 15.sp,
                fontWeight = if (isCurrentReading) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            val subtext = buildString {
                append("#${chapter.number.toInt()}")
                if (chapter.dateUpload.isNotEmpty()) append(" • ${chapter.dateUpload}")
                if (chapter.scanlator.isNotEmpty()) append(" • ${chapter.scanlator}")
            }

            Text(
                text = subtext,
                color = KotatsuTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Bookmark action
        IconButton(
            onClick = onBookmarkClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (chapter.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = if (chapter.isBookmarked) KotatsuTeal else KotatsuTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        // Download action
        IconButton(
            onClick = onDownloadClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (chapter.isDownloaded) Icons.Default.FileDownloadDone else Icons.Default.Download,
                contentDescription = "Download",
                tint = if (chapter.isDownloaded) KotatsuTeal else KotatsuTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
