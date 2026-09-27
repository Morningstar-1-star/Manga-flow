package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Manga
import com.example.data.model.MangaSource
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuDarkSurfaceHigh
import com.example.ui.theme.KotatsuDarkSurfaceVariant
import com.example.ui.theme.KotatsuProgressRemaining
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTealContainer
import com.example.ui.theme.KotatsuTextMuted
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary

@Composable
fun MangaCard(
    manga: Manga,
    modifier: Modifier = Modifier,
    progressPercent: Int? = if (manga.readProgressPercent > 0) manga.readProgressPercent else null,
    isCompleted: Boolean = manga.readProgressPercent >= 100,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_press_scale"
    )

    Column(
        modifier = modifier
            .width(135.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("manga_card_${manga.id}"),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(16.dp))
                .background(KotatsuDarkSurfaceVariant)
                .border(1.dp, KotatsuCardBorder, RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = manga.coverUrl,
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Source Badge Overlay (Top Left)
            if (manga.sourceId.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Text(
                        text = when (val s = manga.sourceId.lowercase()) {
                            "comick" -> "🦄 ComicK"
                            "mangadex" -> "🐱 MangaDex"
                            "asurascans", "asura" -> "🗡️ Asura"
                            "flamecomics", "flame" -> "🔥 Flame"
                            "reaperscans", "reaper" -> "💀 Reaper"
                            "webtoons", "webtoon" -> "🟢 Webtoon"
                            "tapastic", "tapas" -> "🟡 Tapas"
                            "manganato" -> "🍃 Nato"
                            "mangakakalot" -> "🍏 Kakalot"
                            "mangapark" -> "🅿️ Park"
                            "mangasee" -> "👁️ See"
                            "cuutruyen" -> "🐬 Cứu"
                            "truyengg" -> "🦊 TruyenGG"
                            "doctruyen3q" -> "🅰️ 3Q"
                            "batoto" -> "🅱️ Bato"
                            "tumangaonline" -> "🇲 TMO"
                            "shonenjumpplus" -> "🎌 Jump+"
                            "pixivcomic" -> "🎨 Pixiv"
                            else -> s.replaceFirstChar { it.uppercase() }.take(10)
                        },
                        color = KotatsuTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Circular progress or completed check badge on bottom right (as in Screenshot 1!)
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xD90D9488))
                        .border(1.5.dp, Color(0xFF2DD4BF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else if (progressPercent != null && progressPercent > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC0F172A))
                        .border(1.5.dp, KotatsuTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$progressPercent%",
                        color = KotatsuTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = manga.title,
            color = KotatsuTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun TopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search manga",
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        color = KotatsuDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = KotatsuTextSecondary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = KotatsuTextSecondary,
                        fontSize = 15.sp
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = KotatsuTextPrimary,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input")
                )
            }

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = KotatsuTextSecondary
                )
            }
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick)
            .testTag("quick_action_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = KotatsuTeal,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = KotatsuTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FilterChipRow(
    chips: List<String>,
    selectedChip: String,
    onChipSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            val isSelected = chip == selectedChip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onChipSelected(chip) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) KotatsuTealContainer else KotatsuDarkSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) KotatsuTeal else KotatsuCardBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
                        text = chip,
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
fun SourceGridItem(
    source: MangaSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(82.dp)
            .clickable(onClick = onClick)
            .testTag("source_card_${source.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(source.brandColorHex).copy(alpha = 0.25f))
                .border(1.5.dp, Color(source.brandColorHex).copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = source.iconEmoji,
                fontSize = 32.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (source.isPinned) {
                Text(
                    text = "📌",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
            Text(
                text = source.name,
                color = KotatsuTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MangaInfoCard(
    sourceName: String,
    author: String,
    status: String,
    chaptersText: String,
    progressPercent: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(label = "Source", value = "🐱 $sourceName")
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "Author", value = author)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "State", value = status)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(label = "Chapters", value = chaptersText)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    color = KotatsuTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.width(72.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(KotatsuDarkSurfaceHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercent / 100f)
                            .height(6.dp)
                            .background(KotatsuTeal)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$progressPercent%",
                    color = KotatsuTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = KotatsuTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            color = KotatsuTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ShimmerMangaCardPlaceholder(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column(
        modifier = modifier.width(135.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(16.dp))
                .background(KotatsuDarkSurfaceVariant.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(KotatsuDarkSurfaceVariant.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(KotatsuDarkSurfaceVariant.copy(alpha = alpha))
        )
    }
}
