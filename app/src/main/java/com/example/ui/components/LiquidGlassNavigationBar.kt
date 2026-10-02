package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.ui.navigation.BottomNavItem
import com.styropyr0.prismal.components.PrismalGlassBottomTab
import dev.liquidglass.compose.GlassHighlight
import dev.liquidglass.compose.GlassRefraction
import dev.liquidglass.compose.GlassShape
import dev.liquidglass.compose.GlassStyle
import dev.liquidglass.compose.LiquidGlassProviderState
import dev.liquidglass.compose.container.LiquidGlassContainer
import dev.liquidglass.compose.container.glassEffect
import dev.liquidglass.compose.container.rememberLiquidGlassContainerState
import dev.liquidglass.core.GlassRenderTier

/**
 * Authentic PrismalAGSL / Apple iOS Liquid Glass Navigation Bar.
 *
 * Implements:
 * - Real 3D Convex Spherical Lens Refraction (Snell's Law)
 * - Prismatic Chromatic Dispersion (RGB Channel Splitting along Surface Normals)
 * - Dual Blinn-Phong Specular Rim Catch & Top Sheen
 * - Gliding 3D Liquid Lens Bubble that pulls fluid metaball bridges across tabs
 * - High-definition optical glass contrast over scrolling manga art
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
    // Force AGSL SHADER rendering tier
    LaunchedEffect(settings.liquidGlassReducedTransparency, settings.liquidGlassPerformanceMode) {
        glassState.requestedTier = when {
            settings.liquidGlassReducedTransparency -> GlassRenderTier.SCRIM
            settings.liquidGlassPerformanceMode == "Battery Saver" -> GlassRenderTier.BLUR
            else -> GlassRenderTier.SHADER
        }
    }

    val isGlassEnabled = settings.liquidGlassNavEnabled &&
            !settings.liquidGlassReducedTransparency &&
            settings.liquidGlassPerformanceMode != "Battery Saver"

    val glassStyle = remember(settings) {
        val blurRadius = settings.liquidGlassBlurDp.coerceIn(6f, 30f).dp
        val refractionAmount = settings.liquidGlassRefractionDp.coerceIn(6f, 28f).dp

        val refraction = GlassRefraction(
            height = (refractionAmount * 0.95f).coerceAtLeast(12.dp),
            amount = refractionAmount
        )

        val chromaticAberration = when (settings.liquidGlassChromaticAberration) {
            "OFF" -> 0f
            "Low" -> 0.22f
            "Medium" -> 0.45f
            else -> 0.38f
        }

        val highlight = GlassHighlight(
            width = 2.4.dp,
            alpha = 0.92f,
            lightAngleDegrees = 245f
        )

        val saturation = when (settings.liquidGlassIntensity) {
            "Subtle" -> 1.30f
            "Balanced" -> 1.55f
            "Strong" -> 1.80f
            else -> 1.55f
        }

        val tintColor = when (settings.liquidGlassTintOption) {
            "Custom" -> Color(settings.liquidGlassCustomTintColor).copy(
                alpha = settings.liquidGlassTransparency.coerceIn(0.15f, 0.75f)
            )
            else -> Color(0xFF111A2E).copy(
                alpha = settings.liquidGlassTransparency.coerceIn(0.25f, 0.60f)
            )
        }

        GlassStyle(
            shape = GlassShape.Capsule,
            blurRadius = blurRadius,
            refraction = refraction,
            saturation = saturation,
            tint = tintColor,
            highlight = highlight,
            noiseAlpha = 0.015f,
            chromaticAberration = chromaticAberration,
            isInteractive = settings.liquidGlassGelPress,
            fallbackScrim = Color(0xEE0B1120)
        )
    }

    val containerState = rememberLiquidGlassContainerState(glassState)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val dockShape = RoundedCornerShape(34.dp)

        if (isGlassEnabled) {
            Box(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(elevation = 18.dp, shape = dockShape, spotColor = Color.Black)
                    .clip(dockShape)
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.50f),
                                Color.White.copy(alpha = 0.15f),
                                Color(0x33000000)
                            )
                        ),
                        shape = dockShape
                    )
                    .testTag("liquid_glass_bottom_bar")
            ) {
                LiquidGlassContainer(
                    state = containerState,
                    style = glassStyle,
                    spacing = 28.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Base Outer Dock Body Shape
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .glassEffect(
                                state = containerState,
                                id = "prismal_dock_base_shape",
                                shape = GlassShape.Capsule,
                                interactive = false
                            )
                    ) {
                        // Top Specular Sheen Line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 24.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.55f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            val totalWidth = maxWidth
                            val tabCount = navItems.size.coerceAtLeast(1)
                            val tabWidth = totalWidth / tabCount

                            val selectedIndex = navItems.indexOfFirst { it.route == currentRoute }
                                .takeIf { it >= 0 } ?: 0

                            val animatedIndex by animateFloatAsState(
                                targetValue = selectedIndex.toFloat(),
                                animationSpec = spring(
                                    dampingRatio = 0.65f,
                                    stiffness = 320f
                                ),
                                label = "prismalActiveLensSpring"
                            )

                            val activeItem = navItems.getOrNull(selectedIndex) ?: navItems[0]
                            val activeColor = activeItem.activeColor

                            // 3D Liquid Lens Bubble over active tab: SDF-merges with dock and adjacent tabs
                            Box(
                                modifier = Modifier
                                    .offset(x = tabWidth * animatedIndex + (tabWidth - 58.dp) / 2)
                                    .width(58.dp)
                                    .fillMaxHeight()
                                    .glassEffect(
                                        state = containerState,
                                        id = "prismal_active_lens_bubble",
                                        shape = GlassShape.Capsule,
                                        interactive = false
                                    )
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                activeColor.copy(alpha = 0.42f),
                                                activeColor.copy(alpha = 0.18f)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.2.dp,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                activeColor.copy(alpha = 0.85f),
                                                activeColor.copy(alpha = 0.30f)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )

                            // 5 Nav Tabs with interactive glassEffect shapes
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                navItems.forEachIndexed { index, item ->
                                    val isSelected = currentRoute == item.route
                                    PrismalGlassBottomTab(
                                        title = item.title,
                                        icon = item.unselectedIcon,
                                        selectedIcon = item.selectedIcon,
                                        isSelected = isSelected,
                                        activeColor = item.activeColor,
                                        badgeCount = item.badgeCount,
                                        showLabel = settings.showNavLabels,
                                        containerState = containerState,
                                        tabKey = "tab_item_$index",
                                        onClick = { onNavigate(item.route) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Fallback
            Row(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("liquid_glass_bottom_bar"),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = currentRoute == item.route
                    PrismalGlassBottomTab(
                        title = item.title,
                        icon = item.unselectedIcon,
                        selectedIcon = item.selectedIcon,
                        isSelected = isSelected,
                        activeColor = item.activeColor,
                        badgeCount = item.badgeCount,
                        showLabel = settings.showNavLabels,
                        containerState = null,
                        tabKey = null,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}
