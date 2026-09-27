package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuDarkSurfaceVariant
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTextMuted
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

data class FeedUpdateItem(
    val mangaId: String,
    val sourceId: String,
    val mangaTitle: String,
    val coverUrl: String,
    val newChapterName: String,
    val timeAgo: String
)

@Composable
fun FeedScreen(
    viewModel: MangaViewModel,
    onNavigateToMangaDetails: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val libraryManga by viewModel.libraryManga.collectAsState()

    val updates = listOf(
        FeedUpdateItem(
            mangaId = "comick_brainrot_girlfriend",
            sourceId = "comick",
            mangaTitle = "Brainrot Girlfriend",
            coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
            newChapterName = "Chapter 46: Brainrot Forever",
            timeAgo = "15 minutes ago"
        ),
        FeedUpdateItem(
            mangaId = "mangadex_non_milk_coffee",
            sourceId = "mangadex",
            mangaTitle = "Non Milk-Milk Coffee Webcomic",
            coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            newChapterName = "Ngày 19: Special Afterword",
            timeAgo = "2 hours ago"
        ),
        FeedUpdateItem(
            mangaId = "mangadex_self_destruction_girl",
            sourceId = "mangadex",
            mangaTitle = "Self-destruction Girl",
            coverUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
            newChapterName = "Chapter 21: A New Crisis",
            timeAgo = "4 hours ago"
        ),
        FeedUpdateItem(
            mangaId = "comick_useless_genie",
            sourceId = "comick",
            mangaTitle = "The Useless Genie and her Intrusive Master",
            coverUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=80",
            newChapterName = "Chapter 16: Summer Beach Wish",
            timeAgo = "Yesterday"
        ),
        FeedUpdateItem(
            mangaId = "mangadex_rotten_petal",
            sourceId = "mangadex",
            mangaTitle = "Rotten Petal",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
            newChapterName = "Chapter 31: Blood Lily",
            timeAgo = "2 days ago"
        ),
        FeedUpdateItem(
            mangaId = "mangadex_roommate_pretty",
            sourceId = "mangadex",
            mangaTitle = "I Guess My Roommate Is Pretty?",
            coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80",
            newChapterName = "Chapter 26: Anniversary Dinner",
            timeAgo = "3 days ago"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KotatsuDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RssFeed,
                    contentDescription = null,
                    tint = KotatsuTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Updates Feed",
                    color = KotatsuTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Mark all read",
                    tint = KotatsuTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(updates) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMangaDetails(item.mangaId, item.sourceId) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.coverUrl,
                            contentDescription = item.mangaTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KotatsuDarkSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.mangaTitle,
                                color = KotatsuTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.newChapterName,
                                color = KotatsuTeal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.timeAgo,
                                color = KotatsuTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download chapter",
                                tint = KotatsuTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
