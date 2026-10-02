package com.styropyr0.prismal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Calibrated optical settings for PrismalAGSL liquid glass material.
 * Replicates Snell's Law double refraction, 3D convex lens dome,
 * chromatic dispersion, and dual Blinn-Phong specular rim catch.
 */
@Immutable
data class PrismalLiquidGlass(
    val blurRadius: Dp = 16.dp,
    val refractionAmount: Dp = 22.dp,
    val refractionHeight: Dp = 16.dp,
    val dispersion: Float = 0.38f,
    val saturation: Float = 1.55f,
    val tint: Color = Color(0xFF111A2E).copy(alpha = 0.40f),
    val highlightAlpha: Float = 0.90f,
    val highlightWidth: Dp = 2.2.dp,
    val lightAngleDegrees: Float = 245f
) {
    companion object {
        val Default = PrismalLiquidGlass()
        val Clear = PrismalLiquidGlass(
            blurRadius = 8.dp,
            refractionAmount = 26.dp,
            dispersion = 0.45f,
            saturation = 1.6f,
            tint = Color.White.copy(alpha = 0.08f)
        )
    }
}
