package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.ViewStream
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReadMode
import com.example.ui.theme.KotatsuCardBorder
import com.example.ui.theme.KotatsuDarkSurface
import com.example.ui.theme.KotatsuDarkSurfaceHigh
import com.example.ui.theme.KotatsuDarkSurfaceVariant
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTealContainer
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary

/**
 * In-reader overlay controls bottom sheet.
 * Exactly reproduces Screenshot 5!
 */
@Composable
fun ReaderControlsSheet(
    currentMode: ReadMode,
    onModeSelected: (ReadMode) -> Unit,
    useTwoPageLayout: Boolean,
    onTwoPageLayoutChange: (Boolean) -> Unit,
    autoScroll: Boolean,
    onAutoScrollChange: (Boolean) -> Unit,
    isTranslationActive: Boolean = false,
    onTranslationActiveChange: (Boolean) -> Unit = {},
    targetLanguage: String = "en",
    onLanguageChange: (String) -> Unit = {},
    onSavePage: () -> Unit,
    onRotateScreen: () -> Unit,
    onColorCorrection: () -> Unit,
    onImageQualityClick: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(KotatsuDarkSurface)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Drag handle bar
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(KotatsuTextSecondary.copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save page action row
        SheetActionRow(
            icon = Icons.Default.Save,
            title = "Save page",
            onClick = onSavePage
        )

        // Rotate screen action row
        SheetActionRow(
            icon = Icons.Default.ScreenRotation,
            title = "Rotate screen",
            onClick = onRotateScreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Read mode section
        Text(
            text = "Read mode",
            color = KotatsuTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4 Read Mode Segment buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(KotatsuDarkSurfaceVariant)
                .border(1.dp, KotatsuCardBorder, RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeSegmentButton(
                mode = ReadMode.STANDARD,
                icon = Icons.Outlined.Book,
                isSelected = currentMode == ReadMode.STANDARD,
                onClick = { onModeSelected(ReadMode.STANDARD) },
                modifier = Modifier.weight(1f)
            )
            ModeSegmentButton(
                mode = ReadMode.RTL,
                icon = Icons.Outlined.MenuBook,
                isSelected = currentMode == ReadMode.RTL,
                onClick = { onModeSelected(ReadMode.RTL) },
                modifier = Modifier.weight(1f)
            )
            ModeSegmentButton(
                mode = ReadMode.VERTICAL,
                icon = Icons.Outlined.ViewDay,
                isSelected = currentMode == ReadMode.VERTICAL,
                onClick = { onModeSelected(ReadMode.VERTICAL) },
                modifier = Modifier.weight(1f)
            )
            ModeSegmentButton(
                mode = ReadMode.WEBTOON,
                icon = Icons.Outlined.ViewStream,
                isSelected = currentMode == ReadMode.WEBTOON,
                onClick = { onModeSelected(ReadMode.WEBTOON) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "The chosen configuration will be remembered for this manga",
            color = KotatsuTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Switch: Use two pages layout on landscape orientation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                tint = KotatsuTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Use two pages layout on landscape ori...",
                color = KotatsuTextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = useTwoPageLayout,
                onCheckedChange = onTwoPageLayoutChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = KotatsuTeal,
                    uncheckedThumbColor = KotatsuTextSecondary,
                    uncheckedTrackColor = KotatsuDarkSurfaceHigh
                )
            )
        }

        // Switch: Automatic scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = KotatsuTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Automatic scroll",
                color = KotatsuTextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = autoScroll,
                onCheckedChange = onAutoScrollChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = KotatsuTeal,
                    uncheckedThumbColor = KotatsuTextSecondary,
                    uncheckedTrackColor = KotatsuDarkSurfaceHigh
                )
            )
        }

        // Switch: AI Manga Text Translator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = KotatsuTeal,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Comic Translator",
                    color = KotatsuTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Translate speech bubbles to ${targetLanguage.uppercase()}",
                    color = KotatsuTextSecondary,
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = isTranslationActive,
                onCheckedChange = onTranslationActiveChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = KotatsuTeal,
                    uncheckedThumbColor = KotatsuTextSecondary,
                    uncheckedTrackColor = KotatsuDarkSurfaceHigh
                )
            )
        }

        if (isTranslationActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("en" to "English 🇺🇸", "es" to "Spanish 🇪🇸", "fr" to "French 🇫🇷", "vi" to "Vietnamese 🇻🇳").forEach { (code, label) ->
                    val selected = targetLanguage == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) KotatsuTealContainer else KotatsuDarkSurfaceHigh)
                            .border(1.dp, if (selected) KotatsuTeal else Color.Transparent, RoundedCornerShape(16.dp))
                            .clickable { onLanguageChange(code) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (selected) KotatsuTeal else KotatsuTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Color correction
        SheetActionRow(
            icon = Icons.Default.AutoAwesome,
            title = "Color correction",
            onClick = onColorCorrection
        )

        // Preferred image server: Original quality
        SheetActionRow(
            icon = Icons.Default.Image,
            title = "Preferred image server: Original quality",
            onClick = onImageQualityClick
        )

        // Settings
        SheetActionRow(
            icon = Icons.Default.Settings,
            title = "Settings",
            onClick = onOpenSettings
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ModeSegmentButton(
    mode: ReadMode,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) KotatsuTealContainer else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) KotatsuTeal else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = mode.displayName,
            tint = if (isSelected) KotatsuTeal else KotatsuTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = mode.displayName,
            color = if (isSelected) KotatsuTeal else KotatsuTextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = KotatsuTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = KotatsuTextPrimary,
            fontSize = 14.sp
        )
    }
}
