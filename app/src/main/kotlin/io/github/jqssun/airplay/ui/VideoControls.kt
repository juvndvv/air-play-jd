package io.github.jqssun.airplay.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CropLandscape
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FitScreen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.ui.design.ChipKind
import io.github.jqssun.airplay.ui.design.GlassIconButton
import io.github.jqssun.airplay.ui.design.StatusChip
import io.github.jqssun.airplay.ui.design.TrackBar
import io.github.jqssun.airplay.ui.design.glass
import io.github.jqssun.airplay.ui.design.isWide
import io.github.jqssun.airplay.ui.theme.Jd
import io.github.jqssun.airplay.ui.theme.JdType
import io.github.jqssun.airplay.ui.gestures.VideoContentScale
import io.github.jqssun.airplay.viewmodel.MainViewModel
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

// white content, strong white ripple; long-press detected via interactions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    containerColor: Color = Color.Transparent,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(interactionSource) {
        var longPressed = false
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    longPressed = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    onLongClick?.let {
                        longPressed = true
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        it.invoke()
                    }
                }
                is PressInteraction.Release -> if (!longPressed) onClick()
                else -> Unit
            }
        }
    }

    CompositionLocalProvider(
        LocalContentColor provides Color.White,
        LocalRippleConfiguration provides RippleConfiguration(
            color = Color.White,
            rippleAlpha = RippleAlpha(
                draggedAlpha = 0.5f,
                focusedAlpha = 0.5f,
                hoveredAlpha = 0.5f,
                pressedAlpha = 0.5f
            )
        )
    ) {
        IconButton(
            onClick = {},
            modifier = modifier.focusRing(),
            interactionSource = interactionSource,
            colors = IconButtonDefaults.iconButtonColors().copy(containerColor = containerColor),
            content = content,
        )
    }
}

@Composable
fun VideoPanel(
    title: String,
    source: String,
    playing: Boolean,
    positionMs: Long,
    durationMs: Long,
    videoUrl: String?,
    downloadProgress: Int?,
    showDownload: Boolean,
    contentScale: VideoContentScale,
    isPipSupported: Boolean,
    showRotate: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSpeedClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onLockClick: () -> Unit,
    onContentScaleClick: () -> Unit,
    onPipClick: () -> Unit,
    onRotateClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekEnd: () -> Unit,
    modifier: Modifier = Modifier,
    playPauseModifier: Modifier = Modifier,
    seekBarModifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing)) {
        val wide = isWide()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (wide) Jd.SafeX else Jd.Space4,
                    end = if (wide) Jd.SafeX else Jd.Space4,
                    bottom = if (wide) Jd.SafeY else Jd.Space4,
                )
                .glass(Jd.ShapeLg, strong = true, blur = false)
                .padding(start = Jd.Space8, end = Jd.Space8, top = 20.dp, bottom = Jd.Space6),
            verticalArrangement = Arrangement.spacedBy(Jd.Space3),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Jd.Space6)) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = JdType.PlayerTitle, color = Jd.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(source, style = JdType.Callout, color = Jd.InkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                StatusChip(stringResource(R.string.chip_streaming), ChipKind.LIVE)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Jd.Space4)) {
                GlassIconButton(
                    icon = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(R.string.cd_play_pause),
                    onClick = onPlayPause,
                    modifier = playPauseModifier,
                )
                Text(formatVideoTime(positionMs), style = JdType.CaptionStrong, color = Jd.InkMuted)
                PlayerSeekbar(
                    positionMs = positionMs,
                    durationMs = durationMs,
                    onSeek = onSeek,
                    onSeekEnd = onSeekEnd,
                    modifier = seekBarModifier.weight(1f),
                )
                Text(
                    "−" + formatVideoTime((durationMs - positionMs).coerceAtLeast(0)),
                    style = JdType.CaptionStrong,
                    color = Jd.InkMuted,
                )
                GlassIconButton(Icons.Rounded.Stop, stringResource(R.string.cd_stop), onStop)
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Jd.Space2),
            ) {
                GlassIconButton(Icons.Rounded.Speed, stringResource(R.string.select_playback_speed), onSpeedClick)
                GlassIconButton(contentScale.icon(), stringResource(contentScale.nameRes()), onContentScaleClick)
                GlassIconButton(Icons.Rounded.LockOpen, stringResource(R.string.cd_lock_controls), onLockClick)
                if (videoUrl != null) {
                    val clipboard = LocalClipboardManager.current
                    GlassIconButton(
                        Icons.Rounded.ContentCopy,
                        stringResource(R.string.cd_copy_url),
                        { clipboard.setText(AnnotatedString(videoUrl)) },
                    )
                }
                if (showDownload) {
                    GlassIconButton(Icons.Rounded.Download, stringResource(R.string.cd_download), onDownloadClick) {
                        when {
                            downloadProgress == null -> Icon(
                                Icons.Rounded.Download,
                                contentDescription = stringResource(R.string.cd_download),
                                modifier = Modifier.size(24.dp),
                            )
                            downloadProgress < 0 -> CircularProgressIndicator(
                                color = LocalContentColor.current,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp),
                            )
                            else -> CircularProgressIndicator(
                                progress = { downloadProgress / 100f },
                                color = LocalContentColor.current,
                                trackColor = LocalContentColor.current.copy(alpha = 0.3f),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
                if (isPipSupported) {
                    GlassIconButton(Icons.Rounded.PictureInPictureAlt, stringResource(R.string.cd_pip), onPipClick)
                }
                if (showRotate) {
                    GlassIconButton(Icons.Rounded.ScreenRotation, stringResource(R.string.cd_rotate), onRotateClick)
                }
            }
        }
    }
}

@Composable
fun UnlockButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PlayerButton(
        modifier = modifier,
        containerColor = Color.Black.copy(0.5f),
        onClick = onClick,
    ) {
        Icon(Icons.Rounded.Lock, contentDescription = stringResource(R.string.cd_unlock_controls))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSeekbar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    onSeekEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val thumbSize by animateDpAsState(if (isFocused) 22.dp else 16.dp, label = "thumbSize")
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Slider(
            value = positionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
            valueRange = 0f..durationMs.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            onValueChangeFinished = onSeekEnd,
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .height(24.dp),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .shadow(4.dp, CircleShape)
                        .background(Jd.Ink)
                        .then(if (isFocused) Modifier.border(3.dp, Jd.Signal, CircleShape) else Modifier),
                )
            },
            track = {
                TrackBar(
                    fraction = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        )
    }
}

// sizes the surface per content scale; requiredSize lets crop/100% exceed the container
@Composable
fun VideoContentFrame(
    aspect: Float,
    videoSizePx: Pair<Int, Int>?,
    contentScale: VideoContentScale,
    zoom: Float,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val w = maxWidth
        val h = maxHeight
        val fit = if (w / h > aspect) Modifier.requiredSize(h * aspect, h) else Modifier.requiredSize(w, w / aspect)
        val sizeModifier = when (contentScale) {
            VideoContentScale.BEST_FIT -> fit
            VideoContentScale.STRETCH -> Modifier.requiredSize(w, h)
            VideoContentScale.CROP ->
                if (w / h > aspect) Modifier.requiredSize(w, w / aspect) else Modifier.requiredSize(h * aspect, h)
            VideoContentScale.HUNDRED_PERCENT -> videoSizePx?.let { (pxW, pxH) ->
                with(LocalDensity.current) { Modifier.requiredSize(pxW.toDp(), pxH.toDp()) }
            } ?: fit
        }
        content(
            sizeModifier.graphicsLayer {
                scaleX = zoom
                scaleY = zoom
            }
        )
    }
}

// key decision table: visible controls hand dpad to focus navigation,
// hidden controls make dpad seek, locked controls only unlock via center
fun handlePlayerKeyEvent(
    keyEvent: KeyEvent,
    controlsVisible: Boolean,
    controlsLocked: Boolean,
    isPlayPauseFocused: Boolean,
    seekIncrementMs: Long,
    viewModel: MainViewModel,
    showControls: () -> Unit,
    unlockControls: () -> Unit,
    onDpadSeek: (deltaMs: Long) -> Unit,
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false
    if (controlsLocked) {
        return when (keyEvent.key) {
            Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                if (controlsVisible) unlockControls() else showControls()
                true
            }
            else -> {
                showControls()
                false
            }
        }
    }

    return when (keyEvent.key) {
        Key.MediaPlayPause, Key.Spacebar -> { viewModel.toggleVideoPlayPause(); showControls(); true }
        Key.MediaPlay -> { viewModel.setVideoPlaying(true); showControls(); true }
        Key.MediaPause -> { viewModel.setVideoPlaying(false); showControls(); true }
        Key.MediaFastForward -> { viewModel.seekVideoBy(seekIncrementMs); showControls(); true }
        Key.MediaRewind -> { viewModel.seekVideoBy(-seekIncrementMs); showControls(); true }
        Key.MediaStop -> { viewModel.stopVideoPlayback(); true }
        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
            when {
                !controlsVisible -> {
                    showControls()
                    true
                }
                isPlayPauseFocused -> {
                    viewModel.toggleVideoPlayPause()
                    showControls()
                    true
                }
                else -> false
            }
        }
        Key.DirectionLeft -> {
            if (!controlsVisible) {
                viewModel.seekVideoBy(-seekIncrementMs)
                onDpadSeek(-seekIncrementMs)
                true
            } else {
                showControls()
                false
            }
        }
        Key.DirectionRight -> {
            if (!controlsVisible) {
                viewModel.seekVideoBy(seekIncrementMs)
                onDpadSeek(seekIncrementMs)
                true
            } else {
                showControls()
                false
            }
        }
        Key.DirectionUp, Key.DirectionDown -> {
            showControls()
            !controlsVisible
        }
        else -> false
    }
}

// cumulative amount skipped by repeated dpad seeks while controls are hidden
@Composable
fun BoxScope.DpadSeekIndicator(
    visible: Boolean,
    offsetMs: Long,
    positionMs: Long,
) {
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.Center),
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.6f),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.FastForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(if (offsetMs < 0) 180f else 0f),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (if (offsetMs >= 0) "+" else "-") +
                            stringResource(R.string.seconds_short, abs(offsetMs) / 1000),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = formatVideoTime(positionMs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

fun VideoContentScale.nameRes(): Int = when (this) {
    VideoContentScale.BEST_FIT -> R.string.scale_best_fit
    VideoContentScale.STRETCH -> R.string.scale_stretch
    VideoContentScale.CROP -> R.string.scale_crop
    VideoContentScale.HUNDRED_PERCENT -> R.string.scale_hundred_percent
}

fun VideoContentScale.icon(): ImageVector = when (this) {
    VideoContentScale.BEST_FIT -> Icons.Rounded.FitScreen
    VideoContentScale.STRETCH -> Icons.Rounded.AspectRatio
    VideoContentScale.CROP -> Icons.Rounded.CropLandscape
    VideoContentScale.HUNDRED_PERCENT -> Icons.Rounded.PhotoSizeSelectActual
}

internal fun formatVideoTime(ms: Long): String {
    val s = (ms / 1000).toInt().coerceAtLeast(0)
    return if (s >= 3600) "%d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
    else "%d:%02d".format(s / 60, s % 60)
}
