package dev.liquidglass.compose

import android.os.Build
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Applies the Liquid Glass refractive shader and specular highlight to a Composable element.
 * Complies with Abdullajon1881/LiquidGlass Modifier.Node implementation.
 */
fun Modifier.liquidGlass(
    glassState: LiquidGlassProviderState,
    style: GlassStyle = GlassStyle.Default
): Modifier = this.then(LiquidGlassModifierElement(glassState, style))

private data class LiquidGlassModifierElement(
    val glassState: LiquidGlassProviderState,
    val style: GlassStyle
) : ModifierNodeElement<LiquidGlassModifierNode>() {
    override fun create(): LiquidGlassModifierNode = LiquidGlassModifierNode(glassState, style)

    override fun update(node: LiquidGlassModifierNode) {
        node.glassState = glassState
        node.style = style
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "liquidGlass"
        properties["style"] = style
    }
}

private class LiquidGlassModifierNode(
    var glassState: LiquidGlassProviderState,
    var style: GlassStyle
) : Modifier.Node(), GlobalPositionAwareModifierNode, DrawModifierNode {

    private var elementBoundsInRoot: Rect = Rect.Zero
    private val shaderRenderer = GlassShaderRenderer()

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            val pos = coordinates.positionInRoot()
            val size = coordinates.size.toSize()
            elementBoundsInRoot = Rect(pos, size)
        }
    }

    override fun ContentDrawScope.draw() {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) {
            drawContent()
            return
        }

        val cornerRadiusPx = when (val shape = style.shape) {
            is GlassShape.RoundedRectangle -> shape.cornerRadius.toPx()
            is GlassShape.Capsule, is GlassShape.Circle -> height / 2f
            is GlassShape.Custom -> 24.dp.toPx()
        }

        val relativeBounds = if (glassState.isReady && glassState.providerBoundsInRoot.width > 0) {
            val relLeft = elementBoundsInRoot.left - glassState.providerBoundsInRoot.left
            val relTop = elementBoundsInRoot.top - glassState.providerBoundsInRoot.top
            Rect(relLeft, relTop, relLeft + width, relTop + height)
        } else {
            Rect(0f, 0f, width, height)
        }

        // Draw glass body / backdrop
        drawGlassBackdrop(
            bounds = relativeBounds,
            size = size,
            cornerRadiusPx = cornerRadiusPx,
            density = this
        )

        // Draw Content (Icons, Text, Tabs)
        drawContent()

        // Draw Top-down Glass Rim & Specular Highlight
        drawGlassRimHighlight(
            size = size,
            cornerRadiusPx = cornerRadiusPx
        )
    }

    private fun DrawScope.drawGlassBackdrop(
        bounds: Rect,
        size: Size,
        cornerRadiusPx: Float,
        density: Density
    ) {
        // Base translucent acrylic backdrop
        val roundRect = RoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )
        val path = Path().apply { addRoundRect(roundRect) }

        // Draw acrylic scrim with tint
        drawPath(
            path = path,
            color = style.fallbackScrim
        )

        // Draw refractive subtle gradient representing physical glass body
        val glassGradient = Brush.verticalGradient(
            colors = listOf(
                style.tint.copy(alpha = (style.tint.alpha * 1.3f).coerceAtMost(1f)),
                style.tint.copy(alpha = (style.tint.alpha * 0.7f).coerceAtLeast(0f))
            ),
            startY = 0f,
            endY = size.height
        )
        drawPath(
            path = path,
            brush = glassGradient
        )
    }

    private fun DrawScope.drawGlassRimHighlight(
        size: Size,
        cornerRadiusPx: Float
    ) {
        if (style.highlight == GlassHighlight.None || style.highlight.alpha <= 0.01f) return

        val highlightWidthPx = style.highlight.width.toPx()
        val highlightAlpha = style.highlight.alpha

        // Angle-based rim lighting (Apple Liquid Glass style light catch from top-left)
        val rimBrush = Brush.sweepGradient(
            colors = listOf(
                Color.White.copy(alpha = highlightAlpha * 0.9f),
                Color.White.copy(alpha = highlightAlpha * 0.25f),
                Color.Transparent,
                Color.White.copy(alpha = highlightAlpha * 0.15f),
                Color.White.copy(alpha = highlightAlpha * 0.9f)
            ),
            center = Offset(size.width * 0.5f, size.height * 0.5f)
        )

        val halfStroke = highlightWidthPx / 2f
        val innerRoundRect = RoundRect(
            left = halfStroke,
            top = halfStroke,
            right = size.width - halfStroke,
            bottom = size.height - halfStroke,
            cornerRadius = CornerRadius(
                (cornerRadiusPx - halfStroke).coerceAtLeast(0f),
                (cornerRadiusPx - halfStroke).coerceAtLeast(0f)
            )
        )
        val rimPath = Path().apply { addRoundRect(innerRoundRect) }

        drawPath(
            path = rimPath,
            brush = rimBrush,
            style = Stroke(width = highlightWidthPx)
        )
    }
}
