package io.github.jqssun.airplay.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.ui.design.ChipKind
import io.github.jqssun.airplay.ui.design.GlassIconButton
import io.github.jqssun.airplay.ui.design.SafeArea
import io.github.jqssun.airplay.ui.design.TrackBar
import io.github.jqssun.airplay.ui.design.Wall
import io.github.jqssun.airplay.ui.design.glass
import io.github.jqssun.airplay.ui.design.isWide
import io.github.jqssun.airplay.ui.dpadAdjust
import io.github.jqssun.airplay.ui.formatVideoTime
import io.github.jqssun.airplay.ui.gestures.ServerVolumeState
import io.github.jqssun.airplay.ui.isTv
import io.github.jqssun.airplay.ui.requestFocusUntilLanded
import io.github.jqssun.airplay.ui.theme.Jd
import io.github.jqssun.airplay.ui.theme.JdType
import io.github.jqssun.airplay.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun AudioScreen(viewModel: MainViewModel, clientName: String, inputTick: Int) {
    val track by viewModel.trackInfo.collectAsState()
    val player = viewModel.dacpPlayer
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var playing by remember { mutableStateOf(true) }
    LaunchedEffect(player) {
        while (player != null) {
            positionMs = player.currentPosition.coerceAtLeast(0)
            durationMs = player.duration.takeIf { it > 0 } ?: track.durationMs
            playing = player.playWhenReady
            delay(500)
        }
    }
    var dimmed by remember { mutableStateOf(false) }
    LaunchedEffect(inputTick) {
        dimmed = false
        delay(DIM_AFTER_MS)
        dimmed = true
    }
    val playFocus = remember { FocusRequester() }
    LaunchedEffect(player != null) { playFocus.requestFocusUntilLanded() }
    val sender = clientName.ifBlank { stringResource(R.string.device_generic) }
    val tv = isTv()

    Wall {
        SafeArea {
            val wide = isWide()
            Column(Modifier.fillMaxSize()) {
                TopBar(stringResource(R.string.chip_audio), ChipKind.LIVE)
                val info: @Composable ColumnScope.() -> Unit = {
                    Text(stringResource(R.string.source_from, sender), style = JdType.CaptionStrong, color = Jd.InkMuted)
                    Text(
                        track.title.ifEmpty { stringResource(R.string.unknown_track) },
                        style = JdType.Display,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtitle = listOf(track.artist, track.album).filter { it.isNotEmpty() }.joinToString(" · ")
                    if (subtitle.isNotEmpty()) {
                        Text(subtitle, style = JdType.Headline, color = Jd.InkMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(
                        modifier = Modifier.padding(top = 28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Jd.Space4),
                    ) {
                        Text(formatVideoTime(positionMs), style = JdType.CaptionStrong, color = Jd.InkMuted)
                        TrackBar(
                            if (durationMs > 0) positionMs / durationMs.toFloat() else 0f,
                            Modifier.weight(1f),
                        )
                        Text(
                            "−" + formatVideoTime((durationMs - positionMs).coerceAtLeast(0)),
                            style = JdType.CaptionStrong,
                            color = Jd.InkMuted,
                        )
                    }
                    if (player != null) {
                        Row(
                            modifier = Modifier.padding(top = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(Jd.Space4),
                        ) {
                            GlassIconButton(Icons.Rounded.SkipPrevious, stringResource(R.string.cd_previous), { player.seekToPrevious() })
                            GlassIconButton(
                                if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                stringResource(R.string.cd_play_pause),
                                {
                                    if (playing) player.pause() else player.play()
                                    playing = !playing
                                },
                                modifier = Modifier.focusRequester(playFocus),
                            )
                            GlassIconButton(Icons.Rounded.SkipNext, stringResource(R.string.cd_next), { player.seekToNext() })
                        }
                    }
                    if (!tv) VolumeControls(viewModel)
                }
                if (wide) {
                    Row(
                        modifier = Modifier.weight(1f).padding(horizontal = Jd.Space6),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Jd.Space12),
                    ) {
                        Artwork(track.coverArt, Modifier.size(280.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp), content = info)
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = Jd.Space6),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Artwork(track.coverArt, Modifier.widthIn(max = 360.dp).fillMaxWidth().align(Alignment.CenterHorizontally))
                        info()
                    }
                }
                if (tv) Text(stringResource(R.string.audio_dim_note), style = JdType.Caption, color = Jd.InkMuted)
            }
        }
        val scrim by animateColorAsState(
            if (dimmed && tv) Color.Black.copy(alpha = 0.7f) else Color.Transparent,
            tween(1_200),
            label = "dim",
        )
        Box(Modifier.fillMaxSize().background(scrim))
    }
}

@Composable
private fun Artwork(cover: Bitmap?, modifier: Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(24.dp, Jd.ShapeLg, ambientColor = Color.Black, spotColor = Color.Black)
            .glass(Jd.ShapeLg),
        contentAlignment = Alignment.Center,
    ) {
        if (cover != null) {
            Image(
                bitmap = cover.asImageBitmap(),
                contentDescription = stringResource(R.string.cd_cover_art),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(Jd.ShapeLg),
            )
        } else {
            Icon(Icons.Rounded.MusicNote, null, tint = Jd.InkMuted, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun VolumeControls(viewModel: MainViewModel) {
    val context = LocalContext.current
    val serverVolume = remember { ServerVolumeState(context) }
    DisposableEffect(serverVolume) { serverVolume.handleLifecycle(this) }
    val clientVolume by viewModel.clientVolume.collectAsState()
    val clientAdjustable by viewModel.clientVolumeAdjustable.collectAsState()
    var clientDrag by remember { mutableStateOf<Float?>(null) }
    val clientShown = clientDrag ?: clientVolume
    Column(Modifier.padding(top = Jd.Space6), verticalArrangement = Arrangement.spacedBy(Jd.Space2)) {
        VolumeRow(
            icon = Icons.AutoMirrored.Rounded.VolumeUp,
            label = stringResource(R.string.volume_server),
            value = serverVolume.percentage / 100f,
            onValueChange = { serverVolume.update((it * 100).roundToInt()) },
            onStep = { serverVolume.adjust(it > 0) },
        )
        VolumeRow(
            icon = Icons.Rounded.PhoneIphone,
            label = stringResource(R.string.volume_client),
            value = clientShown,
            enabled = clientAdjustable,
            onValueChange = { clientDrag = it; viewModel.setClientVolume(it) },
            onValueChangeFinished = { clientDrag = null },
            onStep = { viewModel.setClientVolume((clientShown + it * CLIENT_VOLUME_STEP).coerceIn(0f, 1f)) },
        )
    }
}

@Composable
private fun VolumeRow(
    icon: ImageVector,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onStep: (Int) -> Unit,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Jd.Space3)) {
        Icon(icon, label, tint = Jd.InkMuted, modifier = Modifier.size(20.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            onValueChangeFinished = onValueChangeFinished,
            colors = SliderDefaults.colors(
                thumbColor = Jd.Ink,
                activeTrackColor = Jd.Ink,
                inactiveTrackColor = Color.White.copy(alpha = 0.22f),
            ),
            modifier = Modifier.weight(1f).dpadAdjust(onLeft = { onStep(-1) }, onRight = { onStep(1) }),
        )
        Text(stringResource(R.string.volume_value, (value * 100).roundToInt()), style = JdType.CaptionStrong, color = Jd.InkMuted)
    }
}

private const val DIM_AFTER_MS = 120_000L
private const val CLIENT_VOLUME_STEP = 1f / 16
