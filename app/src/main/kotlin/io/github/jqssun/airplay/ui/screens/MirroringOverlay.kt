package io.github.jqssun.airplay.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.ui.design.ChipKind
import io.github.jqssun.airplay.ui.design.GlassButton
import io.github.jqssun.airplay.ui.design.SafeArea
import io.github.jqssun.airplay.ui.design.StatusChip
import io.github.jqssun.airplay.ui.design.glass
import io.github.jqssun.airplay.ui.requestFocusUntilLanded
import io.github.jqssun.airplay.ui.theme.Jd
import io.github.jqssun.airplay.ui.theme.JdType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MirroringOverlay(clientName: String, onStop: () -> Unit) {
    var showTick by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(showTick) {
        visible = true
        delay(OVERLAY_HIDE_MS)
        visible = false
    }
    val rootFocus = remember { FocusRequester() }
    val stopFocus = remember { FocusRequester() }
    LaunchedEffect(visible) {
        if (visible) stopFocus.requestFocusUntilLanded() else rootFocus.requestFocusUntilLanded()
    }
    BackHandler(onBack = onStop)
    val sender = clientName.ifBlank { stringResource(R.string.device_generic) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(rootFocus)
            .focusable()
            .onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown || e.key !in revealKeys) return@onPreviewKeyEvent false
                showTick++
                !visible
            }
            .pointerInput(Unit) { detectTapGestures { showTick++ } },
    ) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            SafeArea {
                StatusChip(
                    stringResource(R.string.chip_mirroring, sender),
                    ChipKind.LIVE,
                    Modifier.align(Alignment.TopEnd),
                )
                Box(
                    Modifier.align(Alignment.BottomStart).glass(Jd.ShapePill, blur = false)
                        .padding(horizontal = 20.dp, vertical = Jd.Space3)
                ) {
                    Text(stringResource(R.string.mirroring_help), style = JdType.Caption, color = Jd.InkMuted)
                }
                GlassButton(
                    text = stringResource(R.string.btn_stop_mirroring),
                    icon = Icons.Rounded.Stop,
                    onClick = onStop,
                    modifier = Modifier.align(Alignment.BottomEnd).focusRequester(stopFocus),
                )
            }
        }
    }
}

private val revealKeys = setOf(
    Key.DirectionCenter, Key.Enter, Key.NumPadEnter,
    Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight,
)

private const val OVERLAY_HIDE_MS = 4_000L
