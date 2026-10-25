package io.github.jqssun.airplay.ui.screens

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.service.AirPlayService
import io.github.jqssun.airplay.ui.design.ChipKind
import io.github.jqssun.airplay.ui.design.GlassButton
import io.github.jqssun.airplay.ui.design.GlassPanel
import io.github.jqssun.airplay.ui.design.SafeArea
import io.github.jqssun.airplay.ui.design.StatusChip
import io.github.jqssun.airplay.ui.design.TrackBar
import io.github.jqssun.airplay.ui.design.Wall
import io.github.jqssun.airplay.ui.design.glass
import io.github.jqssun.airplay.ui.requestFocusUntilLanded
import io.github.jqssun.airplay.ui.theme.Jd
import io.github.jqssun.airplay.ui.theme.JdType
import kotlinx.coroutines.delay

@Composable
private fun rememberCountdown(deadline: Long): Long {
    var remaining by remember(deadline) { mutableLongStateOf(deadline - SystemClock.elapsedRealtime()) }
    LaunchedEffect(deadline) {
        while (remaining > 0) {
            delay(250)
            remaining = (deadline - SystemClock.elapsedRealtime()).coerceAtLeast(0)
        }
    }
    return remaining
}

@Composable
private fun CenteredPanel(width: Int, content: @Composable () -> Unit) {
    SafeArea {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            GlassPanel(Modifier.widthIn(max = width.dp).fillMaxWidth(), strong = true) { content() }
        }
    }
}

@Composable
fun RequestScreen(request: AirPlayService.ClientRequest, onAnswer: (Boolean) -> Unit) {
    val remaining = rememberCountdown(request.deadline)
    val allowFocus = remember { FocusRequester() }
    LaunchedEffect(request) { allowFocus.requestFocusUntilLanded() }
    BackHandler { onAnswer(false) }
    val name = request.name.ifBlank { stringResource(R.string.device_generic) }
    Wall {
        CenteredPanel(width = 480) {
            Column(
                modifier = Modifier.padding(start = 40.dp, end = 40.dp, top = 36.dp, bottom = Jd.Space8),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Jd.Space4),
            ) {
                Box(Modifier.size(72.dp).glass(Jd.ShapeMd, blur = false), contentAlignment = Alignment.Center) {
                    Icon(deviceIcon(request.model), null, tint = Jd.Ink, modifier = Modifier.size(36.dp))
                }
                Text(stringResource(R.string.request_title, name), style = JdType.Title, textAlign = TextAlign.Center)
                Text(
                    stringResource(R.string.request_desc),
                    style = JdType.Callout,
                    color = Jd.InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 380.dp),
                )
                Row(
                    modifier = Modifier.padding(top = Jd.Space2),
                    horizontalArrangement = Arrangement.spacedBy(Jd.Space4),
                ) {
                    GlassButton(
                        stringResource(R.string.btn_allow_device),
                        onClick = { onAnswer(true) },
                        modifier = Modifier.focusRequester(allowFocus),
                    )
                    GlassButton(stringResource(R.string.btn_reject), onClick = { onAnswer(false) })
                }
                Text(
                    stringResource(R.string.request_note),
                    style = JdType.Caption,
                    color = Jd.InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Jd.Space1),
                )
                Text(
                    stringResource(R.string.request_timeout, (remaining / 1000).toInt()),
                    style = JdType.Caption,
                    color = Jd.InkFaint,
                )
            }
        }
    }
}

@Composable
fun PinScreen(pin: String, clientName: String, onCancel: () -> Unit) {
    val deadline = remember(pin) { SystemClock.elapsedRealtime() + PIN_SCREEN_MS }
    val remaining = rememberCountdown(deadline)
    LaunchedEffect(remaining) { if (remaining <= 0) onCancel() }
    val cancelFocus = remember { FocusRequester() }
    LaunchedEffect(pin) { cancelFocus.requestFocusUntilLanded() }
    BackHandler(onBack = onCancel)
    val sender = clientName.ifBlank { stringResource(R.string.device_generic) }
    Wall {
        SafeArea {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TopBar(stringResource(R.string.pin_chip, sender), ChipKind.IDLE)
                    Spacer(Modifier.height(52.dp))
                    GlassPanel(strong = true) {
                        Column(
                            modifier = Modifier.padding(horizontal = 56.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Jd.Space4),
                        ) {
                            Text(
                                stringResource(R.string.pin_label),
                                style = JdType.Body,
                                color = Jd.InkMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 340.dp),
                            )
                            Text(pin, style = JdType.Pin, color = Jd.Signal)
                            TrackBar(remaining / PIN_SCREEN_MS.toFloat(), Modifier.width(200.dp), height = 4.dp)
                            Text(
                                stringResource(R.string.pin_timer, (remaining / 1000).toInt()),
                                style = JdType.Caption,
                                color = Jd.InkMuted,
                            )
                        }
                    }
                }
                GlassButton(
                    stringResource(R.string.btn_cancel),
                    onClick = onCancel,
                    modifier = Modifier.padding(top = Jd.Space6, bottom = Jd.Space2).focusRequester(cancelFocus),
                )
            }
        }
    }
}

@Composable
fun NoNetworkScreen(onOpenNetworkSettings: () -> Unit, onRetry: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocusUntilLanded() }
    Wall {
        SafeArea {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TopBar(stringResource(R.string.chip_no_network), ChipKind.ERROR)
                Spacer(Modifier.height(40.dp))
                GlassPanel(Modifier.widthIn(max = 560.dp).fillMaxWidth(), strong = true) {
                    Column(
                        modifier = Modifier.padding(start = Jd.Space12, end = Jd.Space12, top = 36.dp, bottom = Jd.Space8),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Icon(Icons.Rounded.WifiOff, null, tint = Jd.Signal, modifier = Modifier.size(48.dp))
                        Text(stringResource(R.string.no_network_title), style = JdType.Title, textAlign = TextAlign.Center)
                        Text(
                            stringResource(R.string.no_network_desc),
                            style = JdType.Body,
                            color = Jd.InkMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 440.dp),
                        )
                        Row(
                            modifier = Modifier.padding(top = Jd.Space2),
                            horizontalArrangement = Arrangement.spacedBy(Jd.Space4),
                        ) {
                            GlassButton(
                                stringResource(R.string.btn_network_settings),
                                onClick = onOpenNetworkSettings,
                                modifier = Modifier.focusRequester(focus),
                            )
                            GlassButton(stringResource(R.string.btn_retry), onClick = onRetry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionLostOverlay(senderName: String, onHome: () -> Unit) {
    val deadline = remember { SystemClock.elapsedRealtime() + LOST_WAIT_MS }
    val remaining = rememberCountdown(deadline)
    LaunchedEffect(remaining) { if (remaining <= 0) onHome() }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocusUntilLanded() }
    BackHandler(onBack = onHome)
    val sender = senderName.ifBlank { stringResource(R.string.device_generic) }
    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0C).copy(alpha = 0.55f))) {
        CenteredPanel(width = 520) {
            Column(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = Jd.Space8),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                StatusChip(stringResource(R.string.chip_lost), ChipKind.ERROR)
                Text(stringResource(R.string.lost_title, sender), style = JdType.Title, textAlign = TextAlign.Center)
                Text(
                    stringResource(R.string.lost_desc, ((remaining + 999) / 1000).toInt()),
                    style = JdType.Body,
                    color = Jd.InkMuted,
                    textAlign = TextAlign.Center,
                )
                GlassButton(
                    stringResource(R.string.btn_home),
                    onClick = onHome,
                    modifier = Modifier.padding(top = Jd.Space2).focusRequester(focus),
                )
            }
        }
    }
}

private const val PIN_SCREEN_MS = 60_000L
private const val LOST_WAIT_MS = 10_000L
