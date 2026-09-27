package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.ui.navigation.BottomNavItem
import com.example.ui.theme.KotatsuRose
import com.example.ui.theme.KotatsuTeal
import com.example.ui.theme.KotatsuTextPrimary
import com.example.ui.theme.KotatsuTextSecondary
import dev.liquidglass.compose.GlassHighlight
import dev.liquidglass.compose.GlassRefraction
import dev.liquidglass.compose.GlassShape
import dev.liquidglass.compose.GlassStyle
import dev.liquidglass.compose.LiquidGlassProviderState
import dev.liquidglass.compose.liquidGlass

/**
 * Premium Apple-style Liquid Glass Navigation Bar
 * Built with Abdullajon1881/LiquidGlass AGSL shader & refractive physics.
 */
@Composable
fun LiquidGlassNavigationBar(
    glassState: LiquidGlassProviderState,
    settings: AppSettings,
    navItems: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isGlassEnabled = settings.liquidGlassNavEnabled &&
            !settings.liquidGlassReducedTransparency &&
            settings.liquidGlassPerformanceMode != "Battery Saver"

    // Construct GlassStyle based on user customization
    val glassStyle = remember(settings) {
        val blurRadius = settings.liquidGlassBlurDp.coerceIn(0f, 30f).dp
        val refractionAmount = settings.liquidGlassRefractionDp.coerceIn(0f, 24f).dp

        val refraction = if (refractionAmount > 0.dp) {
            GlassRefraction(
                height = (refractionAmount * 0.85f).coerceAtLeast(4.dp),
                amount = refractionAmount
            )
        } else {
            GlassRefraction.None
        }

        val chromaticAberration = when (settings.liquidGlassChromaticAberration) {
            "OFF" -> 0f
            "Low" -> 0.18f
            "Medium" -> 0.38f
            else -> 0.18f
        }

        val highlight = if (settings.liquidGlassRimHighlight) {
            GlassHighlight(
                width = 2.dp,
                alpha = 0.55f,
                lightAngleDegrees = 245f
            )
        } else {
            GlassHighlight.None
        }

        val saturation = when (settings.liquidGlassIntensity) {
            "Subtle" -> 1.18f
            "Balanced" -> 1.35f
            "Strong" -> 1.55f
            else -> 1.35f
        }

        val tintColor = when (settings.liquidGlassTintOption) {
            "Custom" -> Color(settings.liquidGlassCustomTintColor).copy(alpha = settings.liquidGlassTransparency.coerceIn(0f, 1f))
            else -> Color(0xFF1E293B).copy(alpha = settings.liquidGlassTransparency.coerceIn(0f, 1f))
        }

        GlassStyle(
            shape = GlassShape.RoundedRectangle(26.dp),
            blurRadius = blurRadius,
            refraction = refraction,
            saturation = saturation,
            tint = tintColor,
            highlight = highlight,
            chromaticAberration = chromaticAberration,
            isInteractive = settings.liquidGlassGelPress,
            fallbackScrim = Color(0xDD121824)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // The Liquid Glass Bar Container
        Row(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp)
                .then(
                    if (isGlassEnabled) {
                        Modifier.liquidGlass(glassState, glassStyle)
                    } else {
                        Modifier
                            .clip(RoundedCornerShape(26.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                    }
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("liquid_glass_bottom_bar"),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route
                LiquidGlassNavItem(
                    item = item,
                    isSelected = isSelected,
                    showLabel = settings.showNavLabels,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    showLabel: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "iconScale"
    )

    val activePillAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.22f else 0.0f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "pillAlpha"
    )

    val activeColor = item.activeColor
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) activeColor else KotatsuTextSecondary,
        label = "iconTint"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else KotatsuTextSecondary,
        label = "labelColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = activeColor),
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("liquid_nav_${item.title.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        // Active indicator pill
        if (activePillAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(activeColor.copy(alpha = activePillAlpha))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .scale(iconScale),
                contentAlignment = Alignment.Center
            ) {
                if (item.badgeCount != null) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = KotatsuRose,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = item.badgeCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = iconTint,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        tint = iconTint,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            if (showLabel) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.title,
                    color = labelColor,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
