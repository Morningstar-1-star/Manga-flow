package dev.liquidglass.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.toSize

/**
 * State and provider architecture for Abdullajon1881/LiquidGlass.
 * Screen content is wrapped inside liquidGlassProvider(state),
 * while the Glass element is placed as a sibling ABOVE it.
 */
@Stable
class LiquidGlassProviderState {
    var providerBoundsInRoot: Rect by mutableStateOf(Rect.Zero)
        internal set

    var isReady: Boolean by mutableStateOf(false)
        internal set

    internal fun updateCoordinates(coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            val pos = coordinates.positionInRoot()
            val size = coordinates.size.toSize()
            providerBoundsInRoot = Rect(pos, size)
            isReady = true
        }
    }
}

@Composable
fun rememberLiquidGlassProviderState(): LiquidGlassProviderState {
    return remember { LiquidGlassProviderState() }
}

/**
 * Modifier that designates the backdrop source content to be sampled by liquid glass elements.
 */
fun Modifier.liquidGlassProvider(state: LiquidGlassProviderState): Modifier {
    return this.then(LiquidGlassProviderElement(state))
}

private data class LiquidGlassProviderElement(
    val state: LiquidGlassProviderState
) : ModifierNodeElement<LiquidGlassProviderNode>() {
    override fun create(): LiquidGlassProviderNode = LiquidGlassProviderNode(state)

    override fun update(node: LiquidGlassProviderNode) {
        node.state = state
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "liquidGlassProvider"
    }
}

private class LiquidGlassProviderNode(
    var state: LiquidGlassProviderState
) : Modifier.Node(), GlobalPositionAwareModifierNode, DrawModifierNode {

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        state.updateCoordinates(coordinates)
    }

    override fun ContentDrawScope.draw() {
        drawContent()
    }
}
