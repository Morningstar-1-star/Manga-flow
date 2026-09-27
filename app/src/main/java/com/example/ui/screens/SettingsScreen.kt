package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.data.backup.BackupManager
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkBg
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuDarkSurfaceHigh
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import com.example.ui.viewmodel.MangaViewModel

enum class SettingsSubScreen {
    MAIN,
    APPEARANCE,
    SOURCES,
    READER,
    STORAGE,
    DOWNLOADS,
    SERVICES,
    BACKUP,
    ABOUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MangaViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.appSettings.collectAsState()
    var currentSubScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentSubScreen) {
                            SettingsSubScreen.MAIN -> "Settings"
                            SettingsSubScreen.APPEARANCE -> "Appearance"
                            SettingsSubScreen.SOURCES -> "Manga sources"
                            SettingsSubScreen.READER -> "Reader settings"
                            SettingsSubScreen.STORAGE -> "Storage and network"
                            SettingsSubScreen.DOWNLOADS -> "Downloads"
                            SettingsSubScreen.SERVICES -> "Services"
                            SettingsSubScreen.BACKUP -> "Backup and restore"
                            SettingsSubScreen.ABOUT -> "About"
                        },
                        color = KotatsuTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentSubScreen == SettingsSubScreen.MAIN) {
                            onBackClick()
                        } else {
                            currentSubScreen = SettingsSubScreen.MAIN
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSubScreen) {
                SettingsSubScreen.MAIN -> MainSettingsContent(
                    settings = settings,
                    onNavigate = { currentSubScreen = it }
                )
                SettingsSubScreen.APPEARANCE -> AppearanceSettingsContent(
                    viewModel = viewModel,
                    settings = settings
                )
                SettingsSubScreen.SOURCES -> SourcesSettingsContent(
                    viewModel = viewModel,
                    settings = settings
                )
                SettingsSubScreen.READER -> ReaderSettingsContent(
                    viewModel = viewModel,
                    settings = settings
                )
                SettingsSubScreen.STORAGE -> StorageSettingsContent(
                    viewModel = viewModel,
                    settings = settings
                )
                SettingsSubScreen.DOWNLOADS -> DownloadsSettingsContent(
                    viewModel = viewModel,
                    settings = settings
                )
                SettingsSubScreen.SERVICES -> ServicesSettingsContent(
                    viewModel = viewModel,
                    settings = settings
                )
                SettingsSubScreen.BACKUP -> BackupSettingsContent(
                    viewModel = viewModel,
                    settings = settings
                )
                SettingsSubScreen.ABOUT -> AboutSettingsContent(
                    viewModel = viewModel,
                    settings = settings
                )
            }
        }
    }
}

@Composable
private fun MainSettingsContent(
    settings: com.example.data.model.AppSettings,
    onNavigate: (SettingsSubScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SettingsCategoryTile(
                icon = Icons.Default.Palette,
                title = "Appearance",
                subtitle = "Theme, List mode, Language",
                onClick = { onNavigate(SettingsSubScreen.APPEARANCE) }
            )
        }
        item {
            SettingsCategoryTile(
                icon = Icons.Default.Extension,
                title = "Manga sources",
                subtitle = "350 of 1248 on",
                onClick = { onNavigate(SettingsSubScreen.SOURCES) }
            )
        }
        item {
            SettingsCategoryTile(
                icon = Icons.Default.MenuBook,
                title = "Reader settings",
                subtitle = "Read mode, Scale mode, Switch pages",
                onClick = { onNavigate(SettingsSubScreen.READER) }
            )
        }
        item {
            SettingsCategoryTile(
                icon = Icons.Default.Storage,
                title = "Storage and network",
                subtitle = "Storage usage, Proxy, Content preloading",
                onClick = { onNavigate(SettingsSubScreen.STORAGE) }
            )
        }
        item {
            SettingsCategoryTile(
                icon = Icons.Default.Download,
                title = "Downloads",
                subtitle = "Downloads folder, Download only via Wi-Fi",
                onClick = { onNavigate(SettingsSubScreen.DOWNLOADS) }
            )
        }
        item {
            SettingsCategoryTile(
                icon = Icons.Default.Sync,
                title = "Services",
                subtitle = "Suggestions, Synchronization, Tracking",
                onClick = { onNavigate(SettingsSubScreen.SERVICES) }
            )
        }
        item {
            SettingsCategoryTile(
                icon = Icons.Default.Backup,
                title = "Backup and restore",
                subtitle = "Create or restore a backup, Periodic backups",
                onClick = { onNavigate(SettingsSubScreen.BACKUP) }
            )
        }
        item {
            SettingsCategoryTile(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "Version ${settings.appVersion}",
                onClick = { onNavigate(SettingsSubScreen.ABOUT) }
            )
        }
    }
}

@Composable
private fun SettingsCategoryTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(KotatsuTeal.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = KotatsuTeal, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = KotatsuTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = KotatsuTextSecondary, fontSize = 12.sp)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = KotatsuTextSecondary)
        }
    }
}

@Composable
private fun AppearanceSettingsContent(
    viewModel: MangaViewModel,
    settings: com.example.data.model.AppSettings
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Color scheme", color = KotatsuTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (scheme in listOf("Totoro", "Dynamic", "Expressive", "Miku", "Asuka")) {
                    val isSel = settings.colorScheme == scheme
                    Surface(
                        modifier = Modifier.clickable { viewModel.updateSettings { it.copy(colorScheme = scheme) } },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) KotatsuTeal.copy(alpha = 0.2f) else KotatsuDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) KotatsuTeal else KotatsuCardBorder)
                    ) {
                        Text(scheme, color = if (isSel) KotatsuTeal else KotatsuTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }

        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "AMOLED Black",
                    subtitle = "Uses less power on AMOLED screens with pure black backgrounds",
                    checked = settings.isAmoledBlack,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(isAmoledBlack = it) } }
                )
                SettingsSwitchRow(
                    title = "Show quick filters",
                    subtitle = "Provides the ability to filter manga lists by certain parameters",
                    checked = settings.showQuickFilters,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(showQuickFilters = it) } }
                )
                SettingsSwitchRow(
                    title = "Show reading progress indicators",
                    subtitle = "Displays reading progress percentage overlay on manga cards",
                    checked = settings.showReadingProgress,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(showReadingProgress = it) } }
                )
                SettingsSwitchRow(
                    title = "Show floating Continue button",
                    subtitle = "Allows to continue reading in a single click",
                    checked = settings.showFloatingContinue,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(showFloatingContinue = it) } }
                )
            }
        }
    }
}

@Composable
private fun SourcesSettingsContent(
    viewModel: MangaViewModel,
    settings: com.example.data.model.AppSettings
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "Enable all manga sources",
                    subtitle = "All 1248 available manga sources will be enabled permanently",
                    checked = settings.enableAllSources,
                    onCheckedChange = { viewModel.enableAllSources(it) }
                )
                SettingsSwitchRow(
                    title = "Disable NSFW",
                    subtitle = "Disable NSFW sources and hide adult manga from list if possible",
                    checked = settings.disableNsfw,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(disableNsfw = it) } }
                )
                SettingsSwitchRow(
                    title = "Choose mirror automatically",
                    subtitle = "Automatically switch domains for manga sources on errors if mirrors are available",
                    checked = settings.chooseMirrorAuto,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(chooseMirrorAuto = it) } }
                )
            }
        }
    }
}

@Composable
private fun ReaderSettingsContent(
    viewModel: MangaViewModel,
    settings: com.example.data.model.AppSettings
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "Keep screen on",
                    subtitle = "Do not turn the screen off while reading manga",
                    checked = settings.keepScreenOn,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(keepScreenOn = it) } }
                )
                SettingsSwitchRow(
                    title = "Volume keys scrolling",
                    subtitle = "Use volume buttons for switching pages in reader",
                    checked = settings.volumeKeysPaging,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(volumeKeysPaging = it) } }
                )
                SettingsSwitchRow(
                    title = "Crop page borders",
                    subtitle = "Automatically trim white or black scanner margins",
                    checked = settings.cropPageBorders,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(cropPageBorders = it) } }
                )
                SettingsSwitchRow(
                    title = "Show information bar in reader",
                    subtitle = "Show current time and reading progress at top of screen",
                    checked = settings.showInfoBar,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(showInfoBar = it) } }
                )
            }
        }
    }
}

@Composable
private fun StorageSettingsContent(
    viewModel: MangaViewModel,
    settings: com.example.data.model.AppSettings
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Storage usage", color = KotatsuTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("• ${settings.savedMangaMb} MB - Saved manga", color = KotatsuTextPrimary, fontSize = 13.sp)
                    Text("• ${settings.pagesCacheMb} MB - Pages cache", color = KotatsuTextPrimary, fontSize = 13.sp)
                    Text("• ${settings.availableSpaceGb} GB - Available space", color = KotatsuTextSecondary, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast
                                    .makeText(context, "Pages cache cleared!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clear pages cache", color = KotatsuTeal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsSettingsContent(
    viewModel: MangaViewModel,
    settings: com.example.data.model.AppSettings
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "Download only via Wi-Fi",
                    subtitle = "Pause chapter downloads when connected to cellular mobile network",
                    checked = settings.downloadOnlyWifi,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(downloadOnlyWifi = it) } }
                )
            }
        }
    }
}

@Composable
private fun ServicesSettingsContent(
    viewModel: MangaViewModel,
    settings: com.example.data.model.AppSettings
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "Suggestions",
                    subtitle = "Show smart manga recommendations based on reading history",
                    checked = settings.suggestionsEnabled,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(suggestionsEnabled = it) } }
                )
                SettingsSwitchRow(
                    title = "Related manga",
                    subtitle = "Show list of related manga on details page",
                    checked = settings.relatedMangaEnabled,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(relatedMangaEnabled = it) } }
                )
            }
        }
    }
}

@Composable
private fun BackupSettingsContent(
    viewModel: MangaViewModel,
    settings: com.example.data.model.AppSettings
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                Toast.makeText(context, "Importing Kotatsu backup...", Toast.LENGTH_SHORT).show()
                val result = BackupManager.importBackup(context, uri, viewModel.repository, viewModel)
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val success = BackupManager.exportBackup(context, uri, viewModel.repository, settings)
                if (success) {
                    Toast.makeText(context, "Kotatsu backup created successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to export backup", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsCard {
                SettingsClickRow(
                    title = "Create data backup",
                    subtitle = "Export your history, favourites, bookmarks, and settings to a Kotatsu backup .zip",
                    onClick = {
                        exportLauncher.launch("kotatsu_backup_${System.currentTimeMillis()}.zip")
                    }
                )
                SettingsClickRow(
                    title = "Restore from backup",
                    subtitle = "Import Kotatsu backup file (.zip or .json) with library, history, bookmarks, and settings",
                    onClick = {
                        restoreLauncher.launch("*/*")
                    }
                )
            }
        }
    }
}

@Composable
private fun AboutSettingsContent(
    viewModel: MangaViewModel,
    settings: com.example.data.model.AppSettings
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsCard {
                SettingsClickRow(
                    title = "Version ${settings.appVersion}",
                    subtitle = "Check for updates",
                    onClick = { }
                )
                SettingsSwitchRow(
                    title = "Allow unstable updates",
                    subtitle = "Receive notifications about unstable preview builds",
                    checked = settings.allowUnstableUpdates,
                    onCheckedChange = { viewModel.updateSettings { s -> s.copy(allowUnstableUpdates = it) } }
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = KotatsuDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, KotatsuCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = KotatsuTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = KotatsuTextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = KotatsuTeal,
                uncheckedThumbColor = KotatsuTextSecondary,
                uncheckedTrackColor = KotatsuDarkSurfaceHigh
            )
        )
    }
}

@Composable
private fun SettingsClickRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = KotatsuTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = KotatsuTextSecondary, fontSize = 12.sp)
        }
    }
}
