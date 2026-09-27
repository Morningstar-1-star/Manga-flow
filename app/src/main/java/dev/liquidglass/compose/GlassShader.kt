package dev.liquidglass.compose

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.unit.Density
import kotlin.math.PI

/**
 * AGSL Shader & RenderEffect implementation for Abdullajon1881/LiquidGlass
 */
object GlassShaderSource {

    const val AGSL_LIQUID_GLASS = """
        uniform shader contentShader;
        uniform float2 resolution;
        uniform float4 glassRect; // x, y, width, height
        uniform float cornerRadius;
        uniform float blurRadius;
        uniform float refractionAmount;
        uniform float chromaticAberration;
        uniform float saturation;
        uniform float4 tintColor;
        uniform float highlightWidth;
        uniform float highlightAlpha;
        uniform float lightAngleRad;

        float sdRoundedBox(float2 p, float2 b, float r) {
            float2 q = abs(p) - b + float2(r, r);
            return min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0, 0.0))) - r;
        }

        half4 main(float2 fragCoord) {
            float2 center = glassRect.xy + glassRect.zw * 0.5;
            float2 halfSize = glassRect.zw * 0.5;
            float2 localPos = fragCoord - center;

            float dist = sdRoundedBox(localPos, halfSize, cornerRadius);
            if (dist > 0.0) {
                return contentShader.eval(fragCoord);
            }

            // Normal calculation via finite differences
            float eps = 1.0;
            float dx = sdRoundedBox(localPos + float2(eps, 0.0), halfSize, cornerRadius) -
                       sdRoundedBox(localPos - float2(eps, 0.0), halfSize, cornerRadius);
            float dy = sdRoundedBox(localPos + float2(0.0, eps), halfSize, cornerRadius) -
                       sdRoundedBox(localPos - float2(0.0, eps), halfSize, cornerRadius);
            float2 normal = normalize(float2(dx, dy) + float2(0.0001, 0.0001));

            // Refraction displacement with edge falloff
            float edgeFactor = clamp(-dist / max(cornerRadius, 1.0), 0.0, 1.0);
            float refrFactor = (1.0 - edgeFactor * edgeFactor) * refractionAmount;
            float2 offset = normal * refrFactor;

            // Chromatic aberration (RGB channel splitting)
            float2 rOffset = offset * (1.0 + chromaticAberration);
            float2 gOffset = offset;
            float2 bOffset = offset * (1.0 - chromaticAberration);

            half4 sampleR = contentShader.eval(fragCoord + rOffset);
            half4 sampleG = contentShader.eval(fragCoord + gOffset);
            half4 sampleB = contentShader.eval(fragCoord + bOffset);

            half4 baseColor = half4(sampleR.r, sampleG.g, sampleB.b, (sampleR.a + sampleG.a + sampleB.a) / 3.0);

            // Saturation boost
            float luma = dot(baseColor.rgb, half3(0.2126, 0.7152, 0.0722));
            baseColor.rgb = mix(half3(luma), baseColor.rgb, saturation);

            // Tint
            baseColor.rgb = mix(baseColor.rgb, tintColor.rgb, tintColor.a);

            // Specular rim highlight
            float2 lightDir = float2(cos(lightAngleRad), sin(lightAngleRad));
            float rimDot = max(0.0, dot(-normal, lightDir));
            float rimFactor = smoothstep(0.0, -highlightWidth, dist) * (1.0 - smoothstep(-highlightWidth, -highlightWidth * 2.0, dist));
            float highlight = rimFactor * rimDot * highlightAlpha;

            baseColor.rgb += half3(highlight);

            return baseColor;
        }
    """
}

class GlassShaderRenderer {

    private var runtimeShader: Any? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getOrCreateRuntimeShader(): RuntimeShader {
        if (runtimeShader == null) {
            runtimeShader = RuntimeShader(GlassShaderSource.AGSL_LIQUID_GLASS)
        }
        return runtimeShader as RuntimeShader
    }

    fun createRenderEffect(
        style: GlassStyle,
        bounds: Rect,
        cornerRadiusPx: Float,
        density: Density
    ): androidx.compose.ui.graphics.RenderEffect? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return try {
                val shader = getOrCreateRuntimeShader()
                val blurPx = with(density) { style.blurRadius.toPx() }.coerceAtLeast(1f)
                val refractionPx = with(density) { style.refraction.amount.toPx() }
                val highlightWidthPx = with(density) { style.highlight.width.toPx() }
                val lightAngleRad = (style.highlight.lightAngleDegrees * PI / 180.0).toFloat()

                shader.setFloatUniform("resolution", bounds.width, bounds.height)
                shader.setFloatUniform(
                    "glassRect",
                    bounds.left,
                    bounds.top,
                    bounds.width,
                    bounds.height
                )
                shader.setFloatUniform("cornerRadius", cornerRadiusPx)
                shader.setFloatUniform("blurRadius", blurPx)
                shader.setFloatUniform("refractionAmount", refractionPx)
                shader.setFloatUniform("chromaticAberration", style.chromaticAberration)
                shader.setFloatUniform("saturation", style.saturation)
                shader.setFloatUniform(
                    "tintColor",
                    style.tint.red,
                    style.tint.green,
                    style.tint.blue,
                    style.tint.alpha
                )
                shader.setFloatUniform("highlightWidth", highlightWidthPx)
                shader.setFloatUniform("highlightAlpha", style.highlight.alpha)
                shader.setFloatUniform("lightAngleRad", lightAngleRad)

                val blurEffect = RenderEffect.createBlurEffect(
                    blurPx,
                    blurPx,
                    Shader.TileMode.CLAMP
                )
                val agslEffect = RenderEffect.createRuntimeShaderEffect(shader, "contentShader")
                RenderEffect.createChainEffect(agslEffect, blurEffect).asComposeRenderEffect()
            } catch (e: Throwable) {
                // Fallback to BlurEffect on API 31+
                createBlurFallbackEffect(style, density)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return createBlurFallbackEffect(style, density)
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createBlurFallbackEffect(
        style: GlassStyle,
        density: Density
    ): androidx.compose.ui.graphics.RenderEffect {
        val blurPx = with(density) { style.blurRadius.toPx() }.coerceAtLeast(1f)
        return RenderEffect.createBlurEffect(
            blurPx,
            blurPx,
            Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }
}
