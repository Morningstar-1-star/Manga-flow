package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuDarkSurfaceHigh
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTealContainer
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceCatalogScreen(
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sources by viewModel.sources.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchInput by remember { mutableStateOf(false) }

    val languages = listOf(
        "All" to "All Languages",
        "en" to "English",
        "vi" to "Vietnamese",
        "es" to "Spanish",
        "fr" to "French",
        "ja" to "Japanese",
        "de" to "German",
        "it" to "Italian",
        "ru" to "Russian",
        "id" to "Indonesian",
        "zh" to "Chinese",
        "pt" to "Portuguese"
    )
    var selectedLanguageCode by remember { mutableStateOf("en") }
    var languageDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("All", "New", "Manga", "Manhwa", "Manhua", "Hentai", "Comics", "Webtoons")
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredSources = remember(sources, searchQuery, selectedLanguageCode, selectedCategory) {
        sources.filter { source ->
            val matchesQuery = searchQuery.isEmpty() ||
                    source.name.contains(searchQuery, ignoreCase = true) ||
                    source.domain.contains(searchQuery, ignoreCase = true)

            val matchesLanguage = selectedLanguageCode == "All" ||
                    source.language.equals(selectedLanguageCode, ignoreCase = true) ||
                    source.language == "all"

            val matchesCategory = selectedCategory == "All" ||
                    (selectedCategory == "New" && source.isPinned) ||
                    source.category.equals(selectedCategory, ignoreCase = true)

            matchesQuery && matchesLanguage && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchInput) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search catalog...", color = KotatsuTextSecondary, fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.9f),
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
                    } else {
                        Column {
                            Text(
                                text = "Sources catalog",
                                color = KotatsuTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Available: ${sources.size}",
                                color = KotatsuTextSecondary,
                                fontSize = 12.sp
                            )
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
                actions = {
                    IconButton(onClick = { showSearchInput = !showSearchInput }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search sources",
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
            // Language & Category Filter Bar (Matching Screenshot 14)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Dropdown Chip
                Box {
                    Surface(
                        modifier = Modifier.clickable { languageDropdownExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = KotatsuDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuTeal)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = KotatsuTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = languages.find { it.first == selectedLanguageCode }?.second ?: "English",
                                color = KotatsuTeal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "▼", color = KotatsuTeal, fontSize = 10.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = languageDropdownExpanded,
                        onDismissRequest = { languageDropdownExpanded = false },
                        modifier = Modifier.background(KotatsuDarkSurface)
                    ) {
                        languages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = name,
                                        color = if (selectedLanguageCode == code) KotatsuTeal else KotatsuTextPrimary
                                    )
                                },
                                onClick = {
                                    selectedLanguageCode = code
                                    languageDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category Chips
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        modifier = Modifier.clickable { selectedCategory = cat },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) KotatsuTealContainer else KotatsuDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) KotatsuTeal else KotatsuCardBorder
                        )
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) KotatsuTeal else KotatsuTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List of Filtered Sources (Screenshot 14 & 15 style)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSources) { source ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Box
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(source.brandColorHex).copy(alpha = 0.2f))
                                    .border(1.dp, Color(source.brandColorHex).copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = source.iconEmoji, fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Name & Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = source.name,
                                    color = KotatsuTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${source.category}, ${languages.find { it.first == source.language }?.second ?: source.language.uppercase()}",
                                    color = KotatsuTextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            // Pin Button
                            IconButton(onClick = { viewModel.togglePinSource(source.id, !source.isPinned) }) {
                                Icon(
                                    imageVector = if (source.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (source.isPinned) KotatsuTeal else KotatsuTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Enable / Add Button (Screenshot 14 show `+` icon on right side)
                            IconButton(
                                onClick = { viewModel.toggleEnableSource(source.id, !source.isEnabled) }
                            ) {
                                Icon(
                                    imageVector = if (source.isEnabled) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = "Enable",
                                    tint = if (source.isEnabled) KotatsuTeal else KotatsuTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
