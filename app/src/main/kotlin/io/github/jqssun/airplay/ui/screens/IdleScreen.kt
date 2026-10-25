package io.github.jqssun.airplay.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.DesktopMac
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.TabletMac
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jqssun.airplay.AllowedDevice
import io.github.jqssun.airplay.DeviceKind
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.deviceKind
import io.github.jqssun.airplay.service.AirPlayService.ServerState
import io.github.jqssun.airplay.ui.design.ChipKind
import io.github.jqssun.airplay.ui.design.GlassButton
import io.github.jqssun.airplay.ui.design.GlassPanel
import io.github.jqssun.airplay.ui.design.MediaCard
import io.github.jqssun.airplay.ui.design.SafeArea
import io.github.jqssun.airplay.ui.design.StatusChip
import io.github.jqssun.airplay.ui.design.Wall
import io.github.jqssun.airplay.ui.design.Wordmark
import io.github.jqssun.airplay.ui.requestFocusUntilLanded
import io.github.jqssun.airplay.ui.theme.Jd
import io.github.jqssun.airplay.ui.theme.JdType
import io.github.jqssun.airplay.viewmodel.LocalNetworkAccess

fun deviceIcon(model: String): ImageVector = when (deviceKind(model)) {
    DeviceKind.PHONE -> Icons.Rounded.PhoneIphone
    DeviceKind.TABLET -> Icons.Rounded.TabletMac
    DeviceKind.LAPTOP -> Icons.Rounded.Laptop
    DeviceKind.DESKTOP -> Icons.Rounded.DesktopMac
    DeviceKind.OTHER -> Icons.Rounded.Devices
}

@Composable
fun TopBar(chipText: String, chipKind: ChipKind, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Wordmark()
        StatusChip(chipText, chipKind, Modifier.padding(start = Jd.Space4))
    }
}

@Composable
fun IdleScreen(
    state: ServerState,
    localNetwork: LocalNetworkAccess,
    serverName: String,
    clientName: String,
    connections: Int,
    recentDevices: List<AllowedDevice>,
    onStart: () -> Unit,
    onLocalNetwork: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDevices: () -> Unit,
) {
    val needsPermission = state != ServerState.RUNNING && localNetwork != LocalNetworkAccess.GRANTED
    val sender = clientName.ifBlank { stringResource(R.string.device_generic) }
    val (chipText, chipKind) = when {
        needsPermission -> stringResource(R.string.status_permission) to ChipKind.ERROR
        state == ServerState.ERROR -> stringResource(R.string.status_error) to ChipKind.ERROR
        state == ServerState.STOPPED -> stringResource(R.string.status_stopped) to ChipKind.IDLE
        connections > 0 -> stringResource(R.string.status_connecting, sender) to ChipKind.OK
        else -> stringResource(R.string.status_waiting) to ChipKind.IDLE
    }
    val (title, hint) = when {
        needsPermission -> R.string.idle_title_permission to R.string.local_network_desc
        state == ServerState.ERROR -> R.string.idle_title_error to R.string.idle_hint_error
        state == ServerState.STOPPED -> R.string.idle_title_stopped to R.string.idle_hint_stopped
        connections > 0 -> R.string.idle_title_connecting to R.string.waiting_for_playback
        else -> R.string.idle_title to R.string.idle_hint
    }
    val action: Pair<String, () -> Unit>? = when {
        needsPermission -> stringResource(
            if (localNetwork == LocalNetworkAccess.SETTINGS) R.string.btn_open_settings else R.string.btn_allow
        ) to onLocalNetwork
        state != ServerState.RUNNING -> stringResource(R.string.btn_start) to onStart
        else -> null
    }
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(action != null) { firstFocus.requestFocusUntilLanded() }

    Wall {
        SafeArea {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TopBar(chipText, chipKind)
                    Spacer(Modifier.height(36.dp))
                    GlassPanel(Modifier.widthIn(max = 560.dp)) {
                        Column(
                            modifier = Modifier.padding(horizontal = 56.dp, vertical = Jd.Space8),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Jd.Space3),
                        ) {
                            Text(stringResource(title), style = JdType.Display, textAlign = TextAlign.Center)
                            Text(
                                stringResource(hint),
                                style = JdType.Body,
                                color = Jd.InkMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(max = 440.dp),
                            )
                            Text(
                                serverName,
                                style = JdType.Headline,
                                color = Jd.Signal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = Jd.Space1),
                            )
                            if (action != null) {
                                GlassButton(
                                    text = action.first,
                                    onClick = action.second,
                                    modifier = Modifier.padding(top = Jd.Space2).focusRequester(firstFocus),
                                )
                            }
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Jd.Space6),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(start = Jd.Space3, end = Jd.Space3, top = Jd.Space8, bottom = Jd.Space6),
                ) {
                    recentDevices.take(2).forEachIndexed { i, d ->
                        MediaCard(
                            icon = deviceIcon(d.model),
                            title = d.name,
                            onClick = onOpenDevices,
                            modifier = if (i == 0 && action == null) Modifier.focusRequester(firstFocus) else Modifier,
                        )
                    }
                    MediaCard(
                        icon = Icons.Rounded.Tune,
                        title = stringResource(R.string.card_settings),
                        onClick = onOpenSettings,
                        modifier = if (recentDevices.isEmpty() && action == null) Modifier.focusRequester(firstFocus) else Modifier,
                    )
                }
            }
        }
    }
}
