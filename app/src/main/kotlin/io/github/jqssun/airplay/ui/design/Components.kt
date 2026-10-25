package io.github.jqssun.airplay.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.ui.theme.Jd
import io.github.jqssun.airplay.ui.theme.JdType

@Composable
fun Wordmark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.logo_wordmark),
        contentDescription = "Air Play JD",
        modifier = modifier.height(28.dp).aspectRatio(372f / 96f),
    )
}

@Composable
fun Mark(modifier: Modifier = Modifier, white: Boolean = false) {
    Image(
        painter = painterResource(if (white) R.drawable.logo_mark_white else R.drawable.logo_mark),
        contentDescription = null,
        modifier = modifier,
    )
}

enum class ChipKind { IDLE, OK, LIVE, ERROR }

@Composable
fun StatusChip(text: String, kind: ChipKind, modifier: Modifier = Modifier) {
    val content = when (kind) {
        ChipKind.LIVE -> Jd.OnSignal
        ChipKind.ERROR -> Jd.Signal
        ChipKind.IDLE -> Jd.InkMuted
        ChipKind.OK -> Jd.Ink
    }
    val shape = Jd.ShapePill
    val surface = if (kind == ChipKind.LIVE) {
        Modifier.shadow(12.dp, shape).clip(shape).background(Jd.Signal)
    } else {
        Modifier.glass(shape, blur = false)
    }
    Row(
        modifier = modifier.height(34.dp).then(surface).padding(start = Jd.Space3, end = Jd.Space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Jd.Space2),
    ) {
        if (kind == ChipKind.ERROR) {
            Icon(Icons.Rounded.Warning, null, tint = content, modifier = Modifier.size(14.dp))
        } else {
            val pulse = if (kind == ChipKind.LIVE) {
                rememberInfiniteTransition(label = "pulse").animateFloat(
                    initialValue = 1f,
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "pulseAlpha",
                ).value
            } else 1f
            Box(
                Modifier.size(10.dp).alpha(pulse).clip(Jd.ShapePill)
                    .background(if (kind == ChipKind.OK) Jd.Ink else content)
            )
        }
        Text(text, style = JdType.CaptionStrong, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    GlassSurface(onClick = onClick, modifier = modifier.height(52.dp)) {
        Row(
            modifier = Modifier.fillMaxHeight().padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Jd.Space2),
        ) {
            if (icon != null) Icon(icon, null, modifier = Modifier.size(20.dp))
            Text(text, style = JdType.Button, color = LocalContentColor.current)
        }
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    blur: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: (@Composable () -> Unit)? = null,
) {
    GlassSurface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        blur = blur,
        interactionSource = interactionSource,
    ) {
        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
            content?.invoke() ?: Icon(icon, contentDescription, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun MediaCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    meta: String? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val lift by animateFloatAsState(if (focused) 1f else 0f, label = "lift")
    val titleColor by animateColorAsState(if (focused) Jd.Ink else Jd.InkMuted, label = "cardTitle")
    Column(modifier.width(150.dp), verticalArrangement = Arrangement.spacedBy(Jd.Space3)) {
        GlassSurface(
            onClick = onClick,
            shape = Jd.ShapeMd,
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).offset(y = (-4).dp * lift),
        ) {
            Icon(icon, null, modifier = Modifier.size(40.dp).align(Alignment.Center))
        }
        Column(Modifier.offset(y = 8.dp * lift).padding(horizontal = Jd.Space1)) {
            Text(title, style = JdType.CardTitle, color = titleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (meta != null) Text(meta, style = JdType.Caption, color = Jd.InkMuted, maxLines = 1)
        }
    }
}

@Composable
fun GlassList(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.glass(RoundedCornerShape(Jd.RadiusMd + Jd.Space2)).padding(Jd.Space2),
        verticalArrangement = Arrangement.spacedBy(Jd.Space2),
        content = content,
    )
}

@Composable
fun GlassRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    value: String? = null,
    trailing: (@Composable RowScope.(focused: Boolean) -> Unit)? = null,
) {
    GlassSurface(
        onClick = onClick,
        shape = Jd.ShapeMd,
        focusScale = Jd.ScaleRow,
        transparentAtRest = true,
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
    ) { focused ->
        val muted = if (focused) Jd.OnFocusMuted else Jd.InkMuted
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 20.dp, vertical = Jd.Space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Jd.Space4),
        ) {
            if (icon != null) {
                Icon(icon, null, tint = if (focused) Jd.OnFocus else Jd.InkMuted, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = JdType.BodyStrong, color = LocalContentColor.current)
                if (subtitle != null) {
                    Text(subtitle, style = JdType.Caption, color = muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            if (value != null) {
                Text(value, style = JdType.Callout, color = muted, maxLines = 1, textAlign = TextAlign.End)
            }
            trailing?.invoke(this, focused)
        }
    }
}

@Composable
fun JdSwitch(checked: Boolean, focused: Boolean) {
    val track by animateColorAsState(
        when {
            checked -> Jd.Signal
            focused -> Jd.OnFocusMuted
            else -> Color.White.copy(alpha = 0.2f)
        },
        label = "switchTrack",
    )
    val knob by animateFloatAsState(if (checked) 1f else 0f, label = "switchKnob")
    Box(Modifier.size(50.dp, 30.dp).clip(Jd.ShapePill).background(track)) {
        Box(
            Modifier.padding(2.dp).offset(x = 20.dp * knob).size(26.dp)
                .shadow(3.dp, Jd.ShapePill).clip(Jd.ShapePill).background(Jd.Ink)
        )
    }
}

@Composable
fun TrackBar(fraction: Float, modifier: Modifier = Modifier, height: androidx.compose.ui.unit.Dp = 6.dp) {
    Box(modifier.height(height).clip(Jd.ShapePill).background(Color.White.copy(alpha = 0.22f))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).clip(Jd.ShapePill).background(Jd.Ink))
    }
}

