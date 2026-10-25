package io.github.jqssun.airplay.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.ui.design.Mark
import io.github.jqssun.airplay.ui.design.Wall
import io.github.jqssun.airplay.ui.design.glass
import io.github.jqssun.airplay.ui.requestFocusUntilLanded
import io.github.jqssun.airplay.ui.theme.Jd
import io.github.jqssun.airplay.ui.theme.JdType
import kotlinx.coroutines.delay

private fun DrawScope.blob(color: Color, alpha: Float, sizeDp: Float, leftDp: Float, topDp: Float, scale: Float, density: Float) {
    val radius = sizeDp / 2f * scale * density
    val center = Offset((leftDp + sizeDp / 2f) * density, (topDp + sizeDp / 2f) * density)
    drawCircle(
        brush = Brush.radialGradient(
            0f to color.copy(alpha = alpha),
            0.6f to color.copy(alpha = alpha * 0.4f),
            1f to Color.Transparent,
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

private val driftPath = listOf(0f to 0f, 520f to 40f, 500f to 300f, -10f to 280f, 0f to 0f)

private fun drift(t: Float): Pair<Float, Float> {
    val segment = (t * 4).toInt().coerceIn(0, 3)
    val local = t * 4 - segment
    val (x0, y0) = driftPath[segment]
    val (x1, y1) = driftPath[segment + 1]
    return (x0 + (x1 - x0) * local) to (y0 + (y1 - y0) * local)
}

@Composable
fun Screensaver(serverName: String) {
    val transition = rememberInfiniteTransition(label = "screensaver")
    val b1 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(46_000), RepeatMode.Reverse), label = "b1")
    val b2 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(58_000), RepeatMode.Reverse), label = "b2")
    val b3 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(70_000), RepeatMode.Reverse), label = "b3")
    val card by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(240_000, easing = LinearEasing)), label = "card")
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }
    val density = LocalDensity.current.density
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocusUntilLanded() }

    Wall(ambient = false) {
        BoxWithConstraints(Modifier.fillMaxSize().background(Jd.Bg).focusRequester(focus).focusable()) {
            val sx = maxWidth.value / 960f
            val sy = maxHeight.value / 540f
            Canvas(Modifier.fillMaxSize()) {
                blob(Jd.Ambient1, 0.42f, 520f * sx, -120f * sx + 380f * sx * b1, -160f * sy + 240f * sy * b1, 1f + 0.15f * b1, density)
                blob(Jd.Ambient2, 0.42f, 460f * sx, 640f * sx - 420f * sx * b2, 260f * sy - 200f * sy * b2, 1f - 0.1f * b2, density)
                blob(Jd.Ambient1, 0.22f, 360f * sx, 420f * sx - 300f * sx * b3, 260f * sy - 220f * sy * b3, 1f + 0.25f * b3, density)
            }
            val (dx, dy) = drift(card)
            Box(Modifier.offset(x = (72f + dx * sx).dp, y = (72f + dy * sy).dp)) {
                Column(
                    modifier = Modifier.alpha(0.85f).glass(Jd.ShapeLg, blur = false).padding(horizontal = 28.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(DateFormat.getTimeFormat(context).format(now), style = JdType.Clock, color = Jd.Ink)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Mark(Modifier.size(22.dp))
                        Text(stringResource(R.string.screensaver_status, serverName), style = JdType.Callout, color = Jd.InkMuted)
                    }
                }
            }
        }
    }
}
