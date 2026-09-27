package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.MangaSource
import com.example.data.sources.MihonExtension
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuGreen
import com.example.ui.theme.KotatsuRed
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
    onSourceClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Kotatsu Sources, 1: Mihon Extensions, 2: Health
    val sources by viewModel.sources.collectAsState()
    val mihonExtensions by viewModel.mihonExtensions.collectAsState()
    val diagnosticResults by viewModel.diagnosticResults.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSearchInput by remember { mutableStateOf(false) }
    var showAddSourceDialog by remember { mutableStateOf(false) }

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
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = KotatsuTextPrimary,
                                unfocusedTextColor = KotatsuTextPrimary
                            )
                        )
                    } else {
                        Column {
                            Text(
                                text = "Sources & Extensions",
                                color = KotatsuTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Kotatsu & Mihon Ecosystem (${sources.size} sources)",
                                color = KotatsuTextSecondary,
                                fontSize = 11.sp
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
                    IconButton(onClick = { viewModel.pingAllSources() }) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = "Ping sources",
                            tint = KotatsuTeal
                        )
                    }
                    IconButton(onClick = { showSearchInput = !showSearchInput }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search sources",
                            tint = KotatsuTextPrimary
                        )
                    }
                    IconButton(onClick = { showAddSourceDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add custom source",
                            tint = KotatsuTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSourceDialog = true },
                containerColor = KotatsuTeal,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Source")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Three Tabs: Kotatsu Parsers | Keiyoushi Extensions | Health Diagnostics
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = KotatsuTeal,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = KotatsuTeal
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Sources (${sources.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) KotatsuTeal else KotatsuTextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Mihon/Keiyoushi (${mihonExtensions.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) KotatsuTeal else KotatsuTextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Health & Ping",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) KotatsuTeal else KotatsuTextSecondary
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                0 -> {
                    // Kotatsu & Native Sources Tab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Language Dropdown Chip
                        Box {
                            Surface(
                                modifier = Modifier.clickable { languageDropdownExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, KotatsuTeal)
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
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                languages.forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(name, color = KotatsuTextPrimary)
                                                if (code == selectedLanguageCode) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = KotatsuTeal,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
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
                        categories.forEach { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                modifier = Modifier.clickable { selectedCategory = category },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) KotatsuTealContainer else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) KotatsuTeal else KotatsuCardBorder)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) KotatsuTeal else KotatsuTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredSources, key = { it.id }) { source ->
                            SourceCatalogItem(
                                source = source,
                                onClick = { onSourceClick(source.id) },
                                onPinClick = { viewModel.togglePinSource(source.id, !source.isPinned) },
                                onToggleEnabled = { viewModel.toggleEnableSource(source.id, it) },
                                onDeleteClick = { viewModel.deleteSource(source.id) },
                                onTestClick = { viewModel.runSourceDiagnostics(source.id) }
                            )
                        }
                    }
                }

                1 -> {
                    // Mihon / Keiyoushi Extensions Tab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Repository: Keiyoushi & Aniyomi",
                            color = KotatsuTextSecondary,
                            fontSize = 12.sp
                        )
                        OutlinedButton(
                            onClick = { viewModel.refreshMihonExtensions() },
                            shape = CircleShape,
                            border = BorderStroke(1.dp, KotatsuTeal),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = KotatsuTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Refresh Repo", color = KotatsuTeal, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(mihonExtensions, key = { it.pkgName }) { ext ->
                            MihonExtensionItem(
                                extension = ext,
                                onInstallToggle = { viewModel.toggleInstallMihonExtension(ext.pkgName, it) },
                                onEnableToggle = { viewModel.toggleEnableMihonExtension(ext.pkgName, it) }
                            )
                        }
                    }
                }

                2 -> {
                    // Source Health & Ping Diagnostics Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sources, key = { "health_${it.id}" }) { src ->
                            val diagnostic = diagnosticResults[src.id]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, KotatsuCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(if (src.isOnline) "🟢" else "🔴", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = src.name,
                                                color = KotatsuTextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Button(
                                            onClick = { viewModel.runSourceDiagnostics(src.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = KotatsuTealContainer),
                                            shape = CircleShape,
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = KotatsuTeal, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Test", color = KotatsuTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (diagnostic != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (diagnostic.isSuccess) Color(0xFF064E3B).copy(alpha = 0.3f) else Color(0xFF7F1D1D).copy(alpha = 0.3f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = diagnostic.message,
                                                color = if (diagnostic.isSuccess) KotatsuGreen else KotatsuRed,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Custom Source Dialog
    if (showAddSourceDialog) {
        var sourceName by remember { mutableStateOf("") }
        var sourceDomain by remember { mutableStateOf("") }
        var sourceLang by remember { mutableStateOf("en") }
        var sourceCat by remember { mutableStateOf("Manga") }

        AlertDialog(
            onDismissRequest = { showAddSourceDialog = false },
            title = {
                Text(
                    text = "Add Custom Source",
                    color = KotatsuTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Add an external parser, Komga, or OPDS feed domain.",
                        color = KotatsuTextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = sourceName,
                        onValueChange = { sourceName = it },
                        label = { Text("Source / Server Name") },
                        placeholder = { Text("e.g. My Komga Server") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sourceDomain,
                        onValueChange = { sourceDomain = it },
                        label = { Text("Domain / URL") },
                        placeholder = { Text("e.g. demo.komga.org") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sourceDomain.isNotBlank()) {
                            viewModel.addCustomSource(
                                name = sourceName,
                                domain = sourceDomain,
                                language = sourceLang,
                                category = sourceCat
                            )
                            showAddSourceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KotatsuTeal)
                ) {
                    Text("Add Source", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddSourceDialog = false }) {
                    Text("Cancel", color = KotatsuTextSecondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun SourceCatalogItem(
    source: MangaSource,
    onClick: () -> Unit = {},
    onPinClick: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onTestClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KotatsuCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(source.brandColorHex).copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = source.iconEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = source.name,
                            color = KotatsuTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (source.isOnline) "🟢" else "🔴",
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = "${source.domain} • ${source.language.uppercase()} • ${source.category}",
                        color = KotatsuTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPinClick) {
                    Icon(
                        imageVector = if (source.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin Source",
                        tint = if (source.isPinned) KotatsuTeal else KotatsuTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Source",
                        tint = KotatsuRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Switch(
                    checked = source.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = KotatsuTeal,
                        uncheckedThumbColor = KotatsuTextSecondary,
                        uncheckedTrackColor = KotatsuCardBorder
                    )
                )
            }
        }
    }
}

@Composable
private fun MihonExtensionItem(
    extension: MihonExtension,
    onInstallToggle: (Boolean) -> Unit,
    onEnableToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, KotatsuCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KotatsuTealContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Extension, contentDescription = null, tint = KotatsuTeal)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = extension.name,
                        color = KotatsuTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "v${extension.versionName} • ${extension.lang.uppercase()} • Keiyoushi",
                        color = KotatsuTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (extension.isInstalled) {
                    Switch(
                        checked = extension.isEnabled,
                        onCheckedChange = onEnableToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = KotatsuTeal
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onInstallToggle(false) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Uninstall", color = KotatsuRed, fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = { onInstallToggle(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = KotatsuTeal),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Install", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
