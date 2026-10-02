package dev.liquidglass.core

/**
 * AGSL source for the Liquid Glass shader.
 *
 * Implements Apple-style Liquid Glass optics:
 * 1. **Scene SDF & Smooth Union** — polynomial smooth-min (`smin`) of rounded
 *    rectangle shapes up to [GlassUniforms.MAX_SHAPES], enabling organic liquid
 *    metaball merging between navigation shapes and gliding indicator bubbles.
 * 2. **3D Lens Refraction & Magnification** — continuous convex 3D dome profile
 *    and rim refraction that displaces the backdrop along surface normal vectors,
 *    bending and magnifying scrolling content.
 * 3. **Elastic Gel Bulge** — Gaussian displacement around touch points.
 * 4. **Prismatic Chromatic Dispersion** — splits RGB channels along normal vectors
 *    to produce realistic rainbow light dispersion along glass contours.
 * 5. **Polished Specular Rim & Gloss** — angle-dependent rim light, top-light key catch,
 *    tint blending, and anti-banding dither noise.
 */
public object LiquidGlassShaders {

    private const val MAX_SHAPES: Int = GlassUniforms.MAX_SHAPES

    /** The complete AGSL program for the [GlassRenderTier.SHADER] tier. */
    public val LIQUID_GLASS: String = """
uniform shader ${GlassUniforms.CONTENT};
uniform float4 ${GlassUniforms.SHAPES}[$MAX_SHAPES];
uniform float ${GlassUniforms.SHAPE_RADII}[$MAX_SHAPES];
uniform float ${GlassUniforms.MERGE_SMOOTHING};
uniform float ${GlassUniforms.REFRACTION_HEIGHT};
uniform float ${GlassUniforms.REFRACTION_AMOUNT};
uniform float ${GlassUniforms.CHROMATIC_ABERRATION};
uniform float4 ${GlassUniforms.TINT};
uniform float ${GlassUniforms.NOISE_ALPHA};
uniform float2 ${GlassUniforms.LIGHT_DIRECTION};
uniform float ${GlassUniforms.HIGHLIGHT_ALPHA};
uniform float ${GlassUniforms.HIGHLIGHT_WIDTH};
uniform float ${GlassUniforms.PRESS_AMOUNT};
uniform float2 ${GlassUniforms.PRESS_POINT};

float sdRoundedBox(float2 p, float2 halfSize, float radius) {
    float2 q = abs(p) - halfSize + radius;
    return min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0))) - radius;
}

float smin(float a, float b, float k) {
    float kk = max(k, 0.0001);
    float h = clamp(0.5 + 0.5 * (b - a) / kk, 0.0, 1.0);
    return mix(b, a, h) - kk * h * (1.0 - h);
}

float sceneSd(float2 p) {
    float d = 1000000.0;
    for (int i = 0; i < $MAX_SHAPES; i++) {
        float4 s = ${GlassUniforms.SHAPES}[i];
        d = smin(d, sdRoundedBox(p - s.xy, s.zw, ${GlassUniforms.SHAPE_RADII}[i]), ${GlassUniforms.MERGE_SMOOTHING});
    }
    return d;
}

float2 sceneNormal(float2 p) {
    float2 e = float2(1.0, 0.0);
    float gx = sceneSd(p + e.xy) - sceneSd(p - e.xy);
    float gy = sceneSd(p + e.yx) - sceneSd(p - e.yx);
    float2 g = float2(gx, gy);
    float len = length(g);
    if (len < 0.0001) {
        return float2(0.0);
    }
    return g / len;
}

half4 main(float2 fragCoord) {
    float2 p = fragCoord;
    float sd = sceneSd(p);
    
    // Smooth anti-aliased edge mask
    float mask = 1.0 - smoothstep(-1.2, 0.8, sd);
    if (mask <= 0.001) {
        return half4(0.0);
    }

    float2 normal = sceneNormal(p);
    float edgeDist = max(-sd, 0.0);

    // Continuous 3D Convex Lens Profile: refracts and magnifies across full surface
    float refHeight = max(${GlassUniforms.REFRACTION_HEIGHT}, 1.0);
    float normDist = clamp(edgeDist / refHeight, 0.0, 1.0);
    float lensDome = sin(normDist * 1.5707963); // 0..1 smooth dome
    float rimFactor = 1.0 - normDist; // peak at rim
    
    // Total displacement along surface normal
    float refractionStrength = ${GlassUniforms.REFRACTION_AMOUNT} * (0.35 * lensDome + 0.65 * rimFactor);
    float2 sampleCoord = p - normal * refractionStrength;

    // Elastic Gel Press Bulge Interaction
    if (${GlassUniforms.PRESS_AMOUNT} > 0.001) {
        float2 toPress = p - ${GlassUniforms.PRESS_POINT};
        float distSq = dot(toPress, toPress);
        float falloff = exp(-distSq / 7500.0);
        sampleCoord -= toPress * (${GlassUniforms.PRESS_AMOUNT} * 0.12 * falloff);
    }

    // Prismatic Chromatic Aberration (RGB Channel Splitting along Normal)
    float caShift = ${GlassUniforms.CHROMATIC_ABERRATION} * (0.25 + 0.75 * rimFactor) * abs(${GlassUniforms.REFRACTION_AMOUNT}) * 0.22;
    float4 color;
    if (caShift > 0.001) {
        float2 caOffset = normal * caShift;
        float cr = float4(${GlassUniforms.CONTENT}.eval(sampleCoord - caOffset)).r;
        float4 cg = float4(${GlassUniforms.CONTENT}.eval(sampleCoord));
        float cb = float4(${GlassUniforms.CONTENT}.eval(sampleCoord + caOffset)).b;
        color = float4(cr, cg.g, cb, cg.a);
    } else {
        color = float4(${GlassUniforms.CONTENT}.eval(sampleCoord));
    }

    // Luminous Surface Tint Blending
    color.rgb = mix(color.rgb, ${GlassUniforms.TINT}.rgb, ${GlassUniforms.TINT}.a);

    // Polished Specular Rim Catch & Key Light
    float rimWidth = max(${GlassUniforms.HIGHLIGHT_WIDTH}, 0.0001);
    float rim = 1.0 - smoothstep(0.0, rimWidth, edgeDist);
    float facing = pow(max(0.0, dot(normal, ${GlassUniforms.LIGHT_DIRECTION})), 1.2);
    float topLight = pow(max(0.0, -normal.y), 2.0) * 0.35; // Glossy top edge sheen
    
    float pressBoost = 1.0 + ${GlassUniforms.PRESS_AMOUNT} * 0.8;
    float specularTotal = (rim * facing * ${GlassUniforms.HIGHLIGHT_ALPHA} + rim * topLight) * pressBoost;
    color.rgb += float3(specularTotal);

    // Dither Noise (Prevents gradient banding)
    float noise = fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453) - 0.5;
    color.rgb += float3(noise * ${GlassUniforms.NOISE_ALPHA});

    color.rgb = clamp(color.rgb, 0.0, 1.0);
    return half4(color * mask);
}
""".trimIndent()
}
