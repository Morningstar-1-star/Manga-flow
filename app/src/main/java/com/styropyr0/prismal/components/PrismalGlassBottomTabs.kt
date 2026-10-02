package com.styropyr0.prismal.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.liquidglass.compose.GlassShape
import dev.liquidglass.compose.GlassStyle
import dev.liquidglass.compose.LiquidGlassProviderState
import dev.liquidglass.compose.container.LiquidGlassContainer
import dev.liquidglass.compose.container.glassEffect
import dev.liquidglass.compose.container.rememberLiquidGlassContainerState

/**
 * PrismalAGSL Liquid Glass Bottom Tabs Bar.
 * Renders a floating optical glass dock with an active 3D convex liquid lens bubble
 * that glides smoothly across tabs, pulling fluid metaball bridges.
 */
@Composable
fun PrismalGlassBottomTabs(
    glassState: LiquidGlassProviderState,
    selectedIndex: Int,
    itemCount: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF38BDF8),
    style: GlassStyle = GlassStyle.Regular,
    content: @Composable RowScope.() -> Unit
) {
    val containerState = rememberLiquidGlassContainerState(glassState)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp),
        contentAlignment = Alignment.Center
    ) {
        val dockShape = RoundedCornerShape(34.dp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 16.dp, shape = dockShape, spotColor = Color.Black)
                .clip(dockShape)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.12f),
                            Color(0x33000000)
                        )
                    ),
                    shape = dockShape
                )
        ) {
            LiquidGlassContainer(
                state = containerState,
                style = style,
                spacing = 28.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                // Base Outer Liquid Dock Body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .glassEffect(
                            state = containerState,
                            id = "prismal_dock_base",
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
                        val count = itemCount.coerceAtLeast(1)
                        val tabWidth = totalWidth / count

                        val animatedIndex by animateFloatAsState(
                            targetValue = selectedIndex.toFloat(),
                            animationSpec = spring(
                                dampingRatio = 0.65f,
                                stiffness = 320f
                            ),
                            label = "prismalActiveLensSpring"
                        )

                        // 3D Liquid Lens Bubble over active tab
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

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            content = content
                        )
                    }
                }
            }
        }
    }
}
