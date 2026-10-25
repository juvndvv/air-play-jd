package io.github.jqssun.airplay.ui.design

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import io.github.jqssun.airplay.ui.theme.Jd
import kotlin.math.max

val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

// below Android 12 there is no RenderEffect blur, so glass falls back to an opaque fill
val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private data class Blob(val x: Float, val y: Float, val radius: Float, val color: Color)

private val wallBlobs = listOf(
    Blob(0.22f, 0.30f, 0.32f, Jd.Ambient1),
    Blob(0.78f, 0.70f, 0.30f, Jd.Ambient2),
    Blob(0.60f, 0.15f, 0.35f, Jd.Ambient3),
)

fun DrawScope.drawAmbient(alpha: Float = Jd.AmbientStrength) {
    val span = max(size.width, size.height) * 1.4f
    wallBlobs.forEach { b ->
        val center = Offset(size.width * b.x, size.height * b.y)
        drawCircle(
            brush = Brush.radialGradient(
                0f to b.color.copy(alpha = alpha),
                0.55f to b.color.copy(alpha = alpha * 0.35f),
                1f to Color.Transparent,
                center = center,
                radius = span * b.radius,
            ),
            radius = span * b.radius,
            center = center,
        )
    }
}

@Composable
fun Wall(
    modifier: Modifier = Modifier,
    ambient: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val haze = remember { HazeState() }
    Box(modifier.fillMaxSize().background(Jd.Bg)) {
        if (ambient) {
            Canvas(Modifier.fillMaxSize().hazeSource(haze)) { drawAmbient() }
        }
        CompositionLocalProvider(
            LocalHazeState provides if (ambient) haze else null,
            LocalContentColor provides Jd.Ink,
        ) { content() }
    }
}

// tv layouts are drawn for a 960x540 canvas; narrow windows (phones) stack instead
fun BoxWithConstraintsScope.isWide(): Boolean = maxWidth >= 720.dp

@Composable
fun SafeArea(modifier: Modifier = Modifier, content: @Composable BoxWithConstraintsScope.() -> Unit) {
    BoxWithConstraints(modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        val wide = isWide()
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(
                horizontal = if (wide) Jd.SafeX else Jd.Space4,
                vertical = if (wide) Jd.SafeY else Jd.Space4,
            ),
            content = content,
        )
    }
}

private val sheen = Brush.linearGradient(
    0f to Color.White.copy(alpha = 0.22f),
    0.38f to Color.Transparent,
    0.70f to Color.Transparent,
    1f to Color.White.copy(alpha = 0.08f),
    start = Offset.Zero,
    end = Offset.Infinite,
)

private val focusSheen = Brush.linearGradient(
    0f to Color.White.copy(alpha = 0.54f),
    0.45f to Color.Transparent,
    start = Offset.Zero,
    end = Offset.Infinite,
)

private val edge = Brush.linearGradient(
    0f to Jd.GlassEdge,
    0.35f to Jd.GlassEdgeLow,
    1f to Jd.GlassEdgeLow,
    start = Offset.Zero,
    end = Offset.Infinite,
)

@Composable
fun Modifier.glass(
    shape: Shape,
    strong: Boolean = false,
    blur: Boolean = true,
): Modifier {
    val haze = LocalHazeState.current
    val fill = if (strong) Jd.GlassStrong else Jd.Glass
    val body = when {
        !blurSupported -> Modifier.background(Jd.GlassFallback)
        blur && haze != null -> Modifier.hazeEffect(
            haze,
            HazeStyle(
                backgroundColor = Jd.Bg,
                tints = listOf(HazeTint(Jd.GlassTint), HazeTint(fill)),
                blurRadius = if (strong) Jd.BlurStrong else Jd.BlurGlass,
                noiseFactor = 0f,
            ),
        )
        else -> Modifier.background(Jd.GlassTint).background(fill)
    }
    return this
        .clip(shape)
        .then(body)
        .background(sheen)
        .border(1.dp, edge, shape)
}

fun Modifier.focusedGlass(shape: Shape): Modifier = this
    .shadow(24.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
    .clip(shape)
    .background(Jd.GlassFocus)
    .background(focusSheen)
    .border(1.dp, Color.White.copy(alpha = 0.5f), shape)

@Composable
fun GlassSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = Jd.ShapePill,
    strong: Boolean = false,
    blur: Boolean = true,
    focusScale: Float = Jd.ScaleFocus,
    transparentAtRest: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.(focused: Boolean) -> Unit,
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> Jd.ScalePressed
            focused -> focusScale
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "focusScale",
    )
    val ink by animateColorAsState(if (focused) Jd.OnFocus else Jd.Ink, tween(200), label = "ink")
    val surface = when {
        focused -> Modifier.focusedGlass(shape)
        transparentAtRest -> Modifier.clip(shape)
        else -> Modifier.glass(shape, strong, blur)
    }
    Box(
        modifier = modifier
            .scale(scale)
            .then(surface)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        CompositionLocalProvider(LocalContentColor provides ink) { content(focused) }
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = Jd.ShapeLg,
    strong: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.glass(shape, strong), content = content)
}
