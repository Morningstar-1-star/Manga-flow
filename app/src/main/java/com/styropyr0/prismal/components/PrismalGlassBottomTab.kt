package com.styropyr0.prismal.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KotatsuRose
import com.example.ui.theme.KotatsuTextSecondary
import dev.liquidglass.compose.GlassShape
import dev.liquidglass.compose.container.LiquidGlassContainerState
import dev.liquidglass.compose.container.glassEffect

@Composable
fun RowScope.PrismalGlassBottomTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector = icon,
    isSelected: Boolean,
    activeColor: Color,
    badgeCount: Int? = null,
    showLabel: Boolean = true,
    containerState: LiquidGlassContainerState? = null,
    tabKey: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isSelected -> 1.15f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 450f),
        label = "tabScale"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) activeColor else KotatsuTextSecondary.copy(alpha = 0.90f),
        animationSpec = spring(dampingRatio = 0.8f),
        label = "iconTint"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else KotatsuTextSecondary.copy(alpha = 0.80f),
        animationSpec = spring(dampingRatio = 0.8f),
        label = "labelColor"
    )

    val baseModifier = modifier
        .weight(1f)
        .then(
            if (containerState != null && tabKey != null) {
                Modifier.glassEffect(
                    state = containerState,
                    id = tabKey,
                    shape = GlassShape.Capsule,
                    interactive = true
                )
            } else {
                Modifier.clip(CircleShape)
            }
        )
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = false, radius = 28.dp, color = activeColor.copy(alpha = 0.35f)),
            role = Role.Tab,
            onClick = onClick
        )
        .scale(scale)

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (badgeCount != null) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = KotatsuRose,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = badgeCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isSelected) selectedIcon else icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (isSelected) selectedIcon else icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            if (showLabel) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    color = labelColor,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
