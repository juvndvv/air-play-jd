package io.github.jqssun.airplay.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.jqssun.airplay.AllowedDevice
import io.github.jqssun.airplay.Prefs
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.service.AirPlayService.ServerState
import io.github.jqssun.airplay.ui.LogsScreen
import io.github.jqssun.airplay.ui.SettingsScreen
import io.github.jqssun.airplay.ui.design.GlassButton
import io.github.jqssun.airplay.ui.design.GlassList
import io.github.jqssun.airplay.ui.design.GlassRow
import io.github.jqssun.airplay.ui.design.JdSwitch
import io.github.jqssun.airplay.ui.design.Mark
import io.github.jqssun.airplay.ui.design.SafeArea
import io.github.jqssun.airplay.ui.design.Wall
import io.github.jqssun.airplay.ui.design.isWide
import io.github.jqssun.airplay.ui.requestFocusUntilLanded
import io.github.jqssun.airplay.ui.theme.Jd
import io.github.jqssun.airplay.ui.theme.JdType
import io.github.jqssun.airplay.viewmodel.MainViewModel
import java.util.Calendar

private enum class Quality(val label: Int, val resolution: String, val fps: Int) {
    AUTO(R.string.quality_auto, Prefs.AUTO, 60),
    HD(R.string.quality_720, "1280x720", 60),
    FULL_HD(R.string.quality_1080, "1920x1080", 60),
    UHD(R.string.quality_4k, "3840x2160", 60),
}

@Composable
private fun SplitLayout(
    scope: BoxWithConstraintsScope,
    header: @Composable ColumnScope.() -> Unit,
    list: @Composable ColumnScope.() -> Unit,
) {
    if (scope.isWide()) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(Jd.Space12)) {
            Column(
                modifier = Modifier.width(280.dp).fillMaxHeight().padding(top = Jd.Space6),
                verticalArrangement = Arrangement.spacedBy(Jd.Space3),
                content = header,
            )
            Column(
                modifier = Modifier.widthIn(max = 540.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                content = list,
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Jd.Space3),
        ) {
            header()
            Spacer(Modifier.height(Jd.Space4))
            list()
        }
    }
}

@Composable
private fun ScrollingGlassList(wide: Boolean, content: @Composable ColumnScope.() -> Unit) {
    GlassList(Modifier.fillMaxWidth()) {
        Column(
            modifier = if (wide) Modifier.verticalScroll(rememberScrollState()) else Modifier,
            verticalArrangement = Arrangement.spacedBy(Jd.Space2),
            content = content,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = JdType.CaptionStrong,
        color = Jd.InkFaint,
        modifier = Modifier.padding(start = 20.dp, top = Jd.Space3, bottom = Jd.Space1),
    )
}

@Composable
private fun BackLink(onBack: () -> Unit) {
    Text(
        stringResource(R.string.back_to_settings),
        style = JdType.Caption,
        color = Jd.InkMuted,
        modifier = Modifier.clickable(onClick = onBack),
    )
}

@Composable
fun SettingsHome(
    viewModel: MainViewModel,
    onOpenDevices: () -> Unit,
    onOpenAdvanced: () -> Unit,
    onOpenLogs: () -> Unit,
) {
    val serverState by viewModel.serverState.collectAsState()
    val serverName by viewModel.pref(Prefs.SERVER_NAME).collectAsState()
    val requirePin by viewModel.pref(Prefs.REQUIRE_PIN).collectAsState()
    val requestNew by viewModel.pref(Prefs.REQUEST_NEW_DEVICES).collectAsState()
    val resolution by viewModel.pref(Prefs.RESOLUTION).collectAsState()
    val maxFps by viewModel.pref(Prefs.MAX_FPS).collectAsState()
    val debug by viewModel.pref(Prefs.DEBUG_ENABLED).collectAsState()
    val devices by viewModel.allowedDevices.collectAsState()
    val quality = Quality.entries.firstOrNull { it.resolution == resolution && it.fps == maxFps }
    var editingName by remember { mutableStateOf(false) }
    val running = serverState == ServerState.RUNNING
    val context = LocalContext.current
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }
    val ip = remember(serverState) { localIpv4() }
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.requestFocusUntilLanded() }

    Wall {
        SafeArea {
            val wide = isWide()
            SplitLayout(
                scope = this,
                header = {
                    Text(stringResource(R.string.settings_title), style = JdType.Title)
                    Text(stringResource(R.string.settings_desc), style = JdType.Callout, color = Jd.InkMuted)
                    if (wide) Spacer(Modifier.weight(1f))
                    Mark(Modifier.size(72.dp))
                    Text(
                        if (ip != null) stringResource(R.string.settings_footer, version, ip)
                        else stringResource(R.string.settings_footer_offline, version),
                        style = JdType.Caption,
                        color = Jd.InkMuted,
                    )
                },
                list = {
                    ScrollingGlassList(wide) {
                        GlassRow(
                            title = stringResource(R.string.row_server),
                            subtitle = stringResource(if (running) R.string.row_server_running else R.string.row_server_stopped),
                            icon = Icons.Rounded.PowerSettingsNew,
                            onClick = { if (running) viewModel.stopServer() else viewModel.startServer() },
                            modifier = Modifier.focusRequester(firstFocus),
                        ) { focused -> JdSwitch(running, focused) }
                        GlassRow(
                            title = stringResource(R.string.row_name),
                            subtitle = stringResource(R.string.row_name_sub),
                            value = serverName,
                            icon = Icons.Rounded.Tv,
                            onClick = { editingName = true },
                        )
                        GlassRow(
                            title = stringResource(R.string.row_pin),
                            subtitle = stringResource(R.string.row_pin_sub),
                            icon = Icons.Rounded.Lock,
                            onClick = { viewModel.set(Prefs.REQUIRE_PIN, !requirePin) },
                        ) { focused -> JdSwitch(requirePin, focused) }
                        GlassRow(
                            title = stringResource(R.string.row_request),
                            subtitle = stringResource(R.string.row_request_sub),
                            icon = Icons.Rounded.VerifiedUser,
                            onClick = { viewModel.set(Prefs.REQUEST_NEW_DEVICES, !requestNew) },
                        ) { focused -> JdSwitch(requestNew, focused) }
                        GlassRow(
                            title = stringResource(R.string.row_devices),
                            subtitle = devices.takeIf { it.isNotEmpty() }?.joinToString { it.name }
                                ?: stringResource(R.string.row_devices_none),
                            value = devices.size.toString(),
                            icon = Icons.Rounded.Devices,
                            onClick = onOpenDevices,
                        )
                        GlassRow(
                            title = stringResource(R.string.row_quality),
                            subtitle = stringResource(R.string.row_quality_sub),
                            value = stringResource(quality?.label ?: R.string.quality_custom),
                            icon = Icons.Rounded.HighQuality,
                            onClick = {
                                val next = Quality.entries[((quality?.ordinal ?: -1) + 1) % Quality.entries.size]
                                viewModel.set(Prefs.RESOLUTION, next.resolution)
                                viewModel.set(Prefs.MAX_FPS, next.fps)
                            },
                        )
                        GlassRow(
                            title = stringResource(R.string.row_debug),
                            subtitle = stringResource(R.string.row_debug_sub),
                            icon = Icons.Rounded.Info,
                            onClick = { viewModel.set(Prefs.DEBUG_ENABLED, !debug) },
                        ) { focused -> JdSwitch(debug, focused) }
                        SectionLabel(stringResource(R.string.section_advanced))
                        GlassRow(
                            title = stringResource(R.string.row_advanced),
                            subtitle = stringResource(R.string.row_advanced_sub),
                            icon = Icons.Rounded.Settings,
                            onClick = onOpenAdvanced,
                        )
                        GlassRow(
                            title = stringResource(R.string.row_logs),
                            subtitle = stringResource(R.string.row_logs_sub),
                            icon = Icons.AutoMirrored.Rounded.Article,
                            onClick = onOpenLogs,
                        )
                    }
                },
            )
        }
    }

    if (editingName) {
        NameDialog(
            current = serverName,
            onDismiss = { editingName = false },
            onSave = {
                viewModel.set(Prefs.SERVER_NAME, it)
                editingName = false
            },
        )
    }
}

@Composable
private fun NameDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(current) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocusUntilLanded() }
    val save = { text.trim().takeIf { it.isNotEmpty() }?.let(onSave) ?: onDismiss() }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Jd.Surface1,
        title = { Text(stringResource(R.string.row_name), style = JdType.Headline) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(48) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
        },
        confirmButton = { TextButton(onClick = { save() }) { Text(stringResource(R.string.btn_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) } },
    )
}

private fun startOfDay(ms: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ms
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

@Composable
private fun lastSeenText(ms: Long): String {
    val context = LocalContext.current
    val days = ((startOfDay(System.currentTimeMillis()) - startOfDay(ms)) / 3_600_000.0 / 24).let { Math.round(it).toInt() }
    return when {
        days <= 0 -> stringResource(R.string.last_seen_today, DateFormat.getTimeFormat(context).format(ms))
        days == 1 -> stringResource(R.string.last_seen_yesterday)
        else -> pluralStringResource(R.plurals.last_seen_days, days, days)
    }
}

@Composable
fun DevicesScreen(
    devices: List<AllowedDevice>,
    onRemove: (String) -> Unit,
    onRemoveAll: () -> Unit,
    onBack: () -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(devices.isEmpty()) { firstFocus.requestFocusUntilLanded() }
    Wall {
        SafeArea {
            val wide = isWide()
            SplitLayout(
                scope = this,
                header = {
                    BackLink(onBack)
                    Text(stringResource(R.string.devices_title), style = JdType.Title)
                    Text(stringResource(R.string.devices_desc), style = JdType.Callout, color = Jd.InkMuted)
                    if (wide) Spacer(Modifier.weight(1f))
                    if (devices.isNotEmpty()) {
                        GlassButton(stringResource(R.string.btn_remove_all), onClick = onRemoveAll)
                    }
                },
                list = {
                    if (devices.isEmpty()) {
                        GlassList(Modifier.fillMaxWidth()) {
                            GlassRow(
                                title = stringResource(R.string.devices_empty),
                                icon = Icons.Rounded.Devices,
                                onClick = onBack,
                                modifier = Modifier.focusRequester(firstFocus),
                            )
                        }
                    } else {
                        ScrollingGlassList(wide) {
                            devices.forEachIndexed { i, d ->
                                GlassRow(
                                    title = d.name,
                                    subtitle = stringResource(R.string.last_seen, lastSeenText(d.lastSeenMs)),
                                    value = stringResource(R.string.devices_remove),
                                    icon = deviceIcon(d.model),
                                    onClick = { onRemove(d.id) },
                                    modifier = if (i == 0) Modifier.focusRequester(firstFocus) else Modifier,
                                )
                            }
                        }
                        Text(
                            stringResource(R.string.devices_hint),
                            style = JdType.Caption,
                            color = Jd.InkMuted,
                            modifier = Modifier.padding(start = Jd.Space2, top = Jd.Space3),
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun AdvancedSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    Wall(ambient = false) {
        SafeArea {
            Column(verticalArrangement = Arrangement.spacedBy(Jd.Space3)) {
                BackLink(onBack)
                Text(stringResource(R.string.row_advanced), style = JdType.Title)
                Box(Modifier.weight(1f)) { SettingsScreen(viewModel) }
            }
        }
    }
}

@Composable
fun LogsRoute(viewModel: MainViewModel) {
    Wall(ambient = false) {
        Box(Modifier.fillMaxSize()) { LogsScreen(viewModel) }
    }
}
