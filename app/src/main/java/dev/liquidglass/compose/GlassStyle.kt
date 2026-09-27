package dev.liquidglass.compose

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Configuration data structures for Abdullajon1881/LiquidGlass
 */

@Immutable
sealed interface GlassShape {
    val composeShape: Shape

    data class RoundedRectangle(val cornerRadius: Dp) : GlassShape {
        override val composeShape: Shape = RoundedCornerShape(cornerRadius)
    }

    object Capsule : GlassShape {
        override val composeShape: Shape = CircleShape
    }

    object Circle : GlassShape {
        override val composeShape: Shape = CircleShape
    }

    data class Custom(val shape: Shape) : GlassShape {
        override val composeShape: Shape = shape
    }
}

@Immutable
data class GlassRefraction(
    val height: Dp = 10.dp,
    val amount: Dp = 12.dp
) {
    companion object {
        val None = GlassRefraction(0.dp, 0.dp)
        val Default = GlassRefraction(10.dp, 12.dp)
    }
}

@Immutable
data class GlassHighlight(
    val width: Dp = 1.5.dp,
    val alpha: Float = 0.55f,
    val lightAngleDegrees: Float = 245f
) {
    companion object {
        val None = GlassHighlight(0.dp, 0f, 0f)
        val Default = GlassHighlight(1.5.dp, 0.55f, 245f)
    }
}

enum class GlassIntensity {
    Subtle,
    Balanced,
    Strong
}

@Immutable
data class GlassStyle(
    val shape: GlassShape = GlassShape.RoundedRectangle(26.dp),
    val blurRadius: Dp = 18.dp,
    val refraction: GlassRefraction = GlassRefraction.Default,
    val saturation: Float = 1.35f,
    val tint: Color = Color(0x331E293B),
    val highlight: GlassHighlight = GlassHighlight.Default,
    val chromaticAberration: Float = 0.18f,
    val isInteractive: Boolean = true,
    val fallbackScrim: Color = Color(0xEE121824)
) {
    companion object {
        val Default = GlassStyle()
        val Subtle = GlassStyle(
            blurRadius = 14.dp,
            refraction = GlassRefraction(8.dp, 8.dp),
            saturation = 1.18f,
            chromaticAberration = 0.10f
        )
        val Strong = GlassStyle(
            blurRadius = 24.dp,
            refraction = GlassRefraction(14.dp, 18.dp),
            saturation = 1.55f,
            chromaticAberration = 0.35f
        )
    }
}
