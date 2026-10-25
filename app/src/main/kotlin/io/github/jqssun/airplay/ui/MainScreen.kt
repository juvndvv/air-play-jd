package io.github.jqssun.airplay.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.jqssun.airplay.Prefs
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.service.AirPlayService.ServerState
import io.github.jqssun.airplay.ui.gestures.BrightnessState
import io.github.jqssun.airplay.ui.gestures.DoubleTapIndicator
import io.github.jqssun.airplay.ui.gestures.GestureInfoText
import io.github.jqssun.airplay.ui.gestures.SeekGestureState
import io.github.jqssun.airplay.ui.gestures.ServerVolumeState
import io.github.jqssun.airplay.ui.gestures.TapGestureState
import io.github.jqssun.airplay.ui.gestures.VerticalGesture
import io.github.jqssun.airplay.ui.gestures.VerticalProgressIndicator
import io.github.jqssun.airplay.ui.gestures.VideoPlayerGestures
import io.github.jqssun.airplay.ui.gestures.VolumeAndBrightnessGestureState
import io.github.jqssun.airplay.ui.gestures.ZoomState
import io.github.jqssun.airplay.ui.screens.AdvancedSettingsScreen
import io.github.jqssun.airplay.ui.screens.AudioScreen
import io.github.jqssun.airplay.ui.screens.ConnectionLostOverlay
import io.github.jqssun.airplay.ui.screens.DevicesScreen
import io.github.jqssun.airplay.ui.screens.IdleScreen
import io.github.jqssun.airplay.ui.screens.LogsRoute
import io.github.jqssun.airplay.ui.screens.MirroringOverlay
import io.github.jqssun.airplay.ui.screens.NoNetworkScreen
import io.github.jqssun.airplay.ui.screens.PinScreen
import io.github.jqssun.airplay.ui.screens.RequestScreen
import io.github.jqssun.airplay.ui.screens.Screensaver
import io.github.jqssun.airplay.ui.screens.SettingsHome
import io.github.jqssun.airplay.ui.screens.rememberLanAvailable
import io.github.jqssun.airplay.ui.theme.Jd
import io.github.jqssun.airplay.viewmodel.DebugInfo
import io.github.jqssun.airplay.viewmodel.MainViewModel
import kotlin.math.abs
import kotlinx.coroutines.delay

private enum class Route { HOME, SETTINGS, DEVICES, ADVANCED, LOGS }

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    isInPip: Boolean = false,
    onSurfaceAvailable: (android.view.Surface) -> Unit,
    onSurfaceDestroyed: (android.view.Surface) -> Unit,
    onPip: () -> Unit = {},
    onLocalNetwork: () -> Unit = {}
) {
    val pin by viewModel.pinCode.collectAsState()
    val connections by viewModel.connectionCount.collectAsState()
    val audioOnly by viewModel.audioOnly.collectAsState()
    val videoPlaybackActive by viewModel.videoPlaybackActive.collectAsState()
    val videoSessionPending by viewModel.videoSessionPending.collectAsState()
    val mirroringActive by viewModel.mirroringActive.collectAsState()
    val connectionLost by viewModel.connectionLost.collectAsState()
    val clientName by viewModel.clientName.collectAsState()

    // don't use movableContentOf: moving AndroidView across subcomposition boundaries makes it crash on reparent
    val video: @Composable () -> Unit = {
        val aspect by viewModel.videoAspect.collectAsState()
        VideoSurfaceView(
            onSurfaceAvailable = onSurfaceAvailable,
            onSurfaceDestroyed = onSurfaceDestroyed,
            aspectRatio = aspect
        )
    }

    val activity = LocalContext.current as? Activity
    val videoScreen = videoPlaybackActive || videoSessionPending
    val immersive = videoScreen || mirroringActive || connectionLost != null
    LaunchedEffect(immersive, isInPip) {
        val window = activity?.window ?: return@LaunchedEffect
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (immersive) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    if (videoScreen) {
        val videoPlaybackAspect by viewModel.videoPlaybackAspect.collectAsState()
        val playback: @Composable () -> Unit = {
            VideoSurfaceView(
                onSurfaceAvailable = { viewModel.onVideoPlaybackSurfaceAvailable(it) },
                onSurfaceDestroyed = { viewModel.onVideoPlaybackSurfaceDestroyed(it) },
                aspectRatio = videoPlaybackAspect
            )
        }
        if (isInPip) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                playback()
            }
            return
        }
        val overlayTick by viewModel.videoOverlayTick.collectAsState()
        val playing by viewModel.videoPlaying.collectAsState()
        val positionMs by viewModel.videoPositionMs.collectAsState()
        val durationMs by viewModel.videoDurationMs.collectAsState()
        val scrubPositionMs by viewModel.videoScrubPositionMs.collectAsState()
        val downloadProgress by viewModel.videoDownloadProgress.collectAsState()
        val scrubbing = scrubPositionMs != null
        val downloading = downloadProgress != null
        var overlayVisible by remember { mutableStateOf(false) }
        LaunchedEffect(overlayTick, playing, scrubbing, downloading, videoPlaybackActive) {
            // tick 0 = fresh session; a pending session or a running download pins the overlay
            if (!videoPlaybackActive) {
                overlayVisible = true
            } else if (overlayTick == 0L) {
                overlayVisible = false
            } else {
                overlayVisible = true
                if (playing && !scrubbing && !downloading) {
                    delay(VIDEO_OVERLAY_HIDE_MS)
                    overlayVisible = false
                }
            }
        }
        val gestureScope = rememberCoroutineScope()
        val tapGestureState = remember(viewModel) { TapGestureState(viewModel, gestureScope) }
        val seekGestureState = remember(viewModel) { SeekGestureState(viewModel) }
        val context = LocalContext.current
        val serverVolume = remember { ServerVolumeState(context) }
        val brightnessState = remember(activity) { activity?.window?.let { BrightnessState(it) } }
        val volumeAndBrightnessGestureState = remember(serverVolume, brightnessState) {
            VolumeAndBrightnessGestureState(serverVolume, brightnessState, gestureScope)
        }
        val zoomState = remember(viewModel) { ZoomState(viewModel, gestureScope) }
        var controlsLocked by remember { mutableStateOf(false) }
        DisposableEffect(serverVolume) { serverVolume.handleLifecycle(this) }
        LaunchedEffect(overlayTick) {
            if (overlayTick == 0L) {
                zoomState.reset()
                controlsLocked = false
            }
        }
        DisposableEffect(brightnessState) {
            onDispose { brightnessState?.clearOverride() }
        }
        // orientation follows the video aspect; manual rotate holds until the next video
        LaunchedEffect(videoPlaybackAspect) {
            activity?.requestedOrientation = if (videoPlaybackAspect < 1f) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
        DisposableEffect(activity) {
            onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
        }
        val videoPlaybackSize by viewModel.videoPlaybackSize.collectAsState()
        val buffering by viewModel.videoBuffering.collectAsState()
        val videoTitle by viewModel.videoTitle.collectAsState()
        val videoLocation by viewModel.videoLocation.collectAsState()
        val speed by viewModel.videoSpeed.collectAsState()
        val isPipSupported = remember {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        }
        var showSpeedSelector by remember { mutableStateOf(false) }
        BackHandler { viewModel.stopVideoPlayback() }

        val rootFocusRequester = remember { FocusRequester() }
        val playPauseFocusRequester = remember { FocusRequester() }
        val unlockFocusRequester = remember { FocusRequester() }
        var isPlayPauseFocused by remember { mutableStateOf(false) }
        var isUnlockFocused by remember { mutableStateOf(false) }
        LaunchedEffect(overlayVisible, controlsLocked, showSpeedSelector) {
            if (showSpeedSelector) return@LaunchedEffect
            if (!overlayVisible) {
                runCatching { rootFocusRequester.requestFocus() }
                return@LaunchedEffect
            }
            val locked = controlsLocked
            val target = if (locked) unlockFocusRequester else playPauseFocusRequester
            target.requestFocusUntilLanded(attempts = 20) { if (locked) isUnlockFocused else isPlayPauseFocused }
        }

        // dpad seeking (controls hidden): accumulate the skipped amount and briefly show it
        var dpadSeekOffsetMs by remember { mutableLongStateOf(0L) }
        var dpadSeekTargetMs by remember { mutableLongStateOf(0L) }
        var dpadSeekActive by remember { mutableStateOf(false) }
        var dpadSeekTick by remember { mutableIntStateOf(0) }
        LaunchedEffect(dpadSeekTick) {
            if (!dpadSeekActive) return@LaunchedEffect
            delay(1000)
            dpadSeekActive = false
        }
        val showDpadSeekFeedback: (Long) -> Unit = { deltaMs ->
            if (!dpadSeekActive) dpadSeekOffsetMs = 0L
            dpadSeekOffsetMs += deltaMs
            dpadSeekTargetMs = (dpadSeekTargetMs.takeIf { dpadSeekActive } ?: positionMs).plus(deltaMs)
                .coerceIn(0L, if (durationMs > 0) durationMs else Long.MAX_VALUE)
            dpadSeekActive = true
            dpadSeekTick++
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .focusRequester(rootFocusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (showSpeedSelector) {
                        false
                    } else {
                        handlePlayerKeyEvent(
                            keyEvent = keyEvent,
                            controlsVisible = overlayVisible,
                            controlsLocked = controlsLocked,
                            isPlayPauseFocused = isPlayPauseFocused,
                            seekIncrementMs = TapGestureState.SEEK_INCREMENT_MS,
                            viewModel = viewModel,
                            showControls = { viewModel.showVideoOverlay() },
                            unlockControls = {
                                controlsLocked = false
                                viewModel.showVideoOverlay()
                            },
                            onDpadSeek = showDpadSeekFeedback,
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            VideoContentFrame(
                aspect = videoPlaybackAspect,
                videoSizePx = videoPlaybackSize,
                contentScale = zoomState.contentScale,
                zoom = zoomState.zoom
            ) { sizeModifier ->
                VideoSurfaceView(
                    onSurfaceAvailable = { viewModel.onVideoPlaybackSurfaceAvailable(it) },
                    onSurfaceDestroyed = { viewModel.onVideoPlaybackSurfaceDestroyed(it) },
                    applyAspectRatio = false,
                    modifier = sizeModifier
                )
            }
            VideoPlayerGestures(
                enabled = videoPlaybackActive,
                locked = controlsLocked,
                onTap = {
                    // a pending session pins the overlay; taps must not unpin it
                    if (!videoPlaybackActive) return@VideoPlayerGestures
                    if (overlayVisible) overlayVisible = false else viewModel.showVideoOverlay()
                },
                tapGestureState = tapGestureState,
                seekGestureState = seekGestureState,
                volumeAndBrightnessGestureState = volumeAndBrightnessGestureState,
                zoomState = zoomState
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = overlayVisible && videoPlaybackActive && !controlsLocked,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            }
            if (buffering) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(72.dp))
            }
            DoubleTapIndicator(tapGestureState = tapGestureState)
            val seekAmountMs = seekGestureState.seekAmountMs
            when {
                seekAmountMs != null -> GestureInfoText(
                    info = "${if (seekAmountMs < 0) "-" else "+"}${formatVideoTime(abs(seekAmountMs))}\n" +
                        "[${formatVideoTime(seekGestureState.targetPositionMs ?: 0)}]",
                    modifier = Modifier.align(Alignment.Center)
                )
                zoomState.isZooming -> GestureInfoText(
                    info = "${(zoomState.zoom * 100).toInt()}%",
                    modifier = Modifier.align(Alignment.Center)
                )
                zoomState.showContentScaleIndicator -> GestureInfoText(
                    info = stringResource(zoomState.contentScale.nameRes()),
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> Unit
            }
            DpadSeekIndicator(
                visible = dpadSeekActive && dpadSeekOffsetMs != 0L,
                offsetMs = dpadSeekOffsetMs,
                positionMs = dpadSeekTargetMs
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = volumeAndBrightnessGestureState.activeGesture == VerticalGesture.VOLUME,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart).padding(24.dp)
            ) {
                VerticalProgressIndicator(value = serverVolume.percentage, icon = Icons.AutoMirrored.Rounded.VolumeUp)
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = volumeAndBrightnessGestureState.activeGesture == VerticalGesture.BRIGHTNESS,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd).padding(24.dp)
            ) {
                VerticalProgressIndicator(value = brightnessState?.percentage ?: 0, icon = Icons.Rounded.BrightnessHigh)
            }
            if (controlsLocked) {
                if (overlayVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                            .padding(top = 24.dp)
                    ) {
                        UnlockButton(
                            onClick = {
                                controlsLocked = false
                                viewModel.showVideoOverlay()
                            },
                            modifier = Modifier
                                .focusRequester(unlockFocusRequester)
                                .onFocusChanged { isUnlockFocused = it.hasFocus }
                        )
                    }
                }
            } else {
                androidx.compose.animation.AnimatedVisibility(
                    visible = overlayVisible,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    val sender = clientName.ifBlank { stringResource(R.string.device_generic) }
                    val quality = videoPlaybackSize?.let { (w, h) -> "${minOf(w, h)}p" }
                    VideoPanel(
                        title = videoTitle.ifBlank { stringResource(R.string.video_untitled) },
                        source = if (quality != null) stringResource(R.string.source_from_detail, sender, quality)
                            else stringResource(R.string.source_from, sender),
                        playing = playing,
                        positionMs = scrubPositionMs ?: positionMs,
                        durationMs = durationMs,
                        videoUrl = videoLocation,
                        downloadProgress = downloadProgress,
                        showDownload = durationMs > 0,
                        contentScale = zoomState.contentScale,
                        isPipSupported = isPipSupported,
                        showRotate = !isTv(),
                        onPlayPause = { viewModel.toggleVideoPlayPause() },
                        onStop = { viewModel.stopVideoPlayback() },
                        onSpeedClick = {
                            overlayVisible = false
                            showSpeedSelector = true
                        },
                        onDownloadClick = { viewModel.toggleVideoDownload() },
                        onLockClick = {
                            viewModel.showVideoOverlay()
                            controlsLocked = true
                        },
                        onContentScaleClick = {
                            viewModel.showVideoOverlay()
                            zoomState.switchToNextContentScale()
                        },
                        onPipClick = onPip,
                        onRotateClick = {
                            activity?.let {
                                it.requestedOrientation =
                                    if (it.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                                    } else {
                                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                    }
                            }
                        },
                        onSeek = { seekGestureState.onSeek(it) },
                        onSeekEnd = { seekGestureState.onSeekEnd() },
                        playPauseModifier = Modifier
                            .focusRequester(playPauseFocusRequester)
                            .onFocusChanged { isPlayPauseFocused = it.hasFocus },
                        seekBarModifier = Modifier.dpadAdjust(
                            onLeft = {
                                viewModel.seekVideoBy(-TapGestureState.SEEK_INCREMENT_MS)
                                viewModel.showVideoOverlay()
                            },
                            onRight = {
                                viewModel.seekVideoBy(TapGestureState.SEEK_INCREMENT_MS)
                                viewModel.showVideoOverlay()
                            }
                        )
                    )
                }
            }
            val skipSilence by viewModel.videoSkipSilence.collectAsState()
            PlaybackSpeedSelector(
                show = showSpeedSelector,
                speed = speed,
                skipSilence = skipSilence,
                onSpeedChange = { viewModel.setVideoSpeed(it) },
                onSkipSilenceChange = { viewModel.setVideoSkipSilence(it) },
                onDismiss = { showSpeedSelector = false }
            )
        }
        return
    }

    if (isInPip) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            video()
        }
        return
    }

    val state by viewModel.serverState.collectAsState()
    val localNetwork by viewModel.localNetwork.collectAsState()
    val serverName by viewModel.pref(Prefs.SERVER_NAME).collectAsState()
    val request by viewModel.clientRequest.collectAsState()
    val devices by viewModel.allowedDevices.collectAsState()
    val debugEnabled by viewModel.pref(Prefs.DEBUG_ENABLED).collectAsState()
    val debugInfo by viewModel.debugInfo.collectAsState()
    val lanAvailable = rememberLanAvailable()
    val context = LocalContext.current
    var route by rememberSaveable { mutableStateOf(Route.HOME) }
    var inputTick by remember { mutableIntStateOf(0) }
    var screensaver by remember { mutableStateOf(false) }

    val mirroring = state == ServerState.RUNNING && mirroringActive && !audioOnly
    val lost = connectionLost.takeUnless { mirroring }
    val audio = audioOnly && connections > 0
    val home = route == Route.HOME && request == null && pin == null && !mirroring && lost == null && !audio

    LaunchedEffect(home, inputTick) {
        screensaver = false
        if (home) {
            delay(SCREENSAVER_AFTER_MS)
            screensaver = true
        }
    }

    BackHandler(enabled = route != Route.HOME) {
        route = when (route) {
            Route.DEVICES, Route.ADVANCED, Route.LOGS -> Route.SETTINGS
            else -> Route.HOME
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Jd.Bg)
            .onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                inputTick++
                if (screensaver) {
                    screensaver = false
                    true
                } else false
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        inputTick++
                    }
                }
            }
    ) {
        if (mirroring || lost != null) {
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) { video() }
            if (debugEnabled && mirroring) {
                DebugOverlay(debugInfo, Modifier.align(Alignment.TopStart).safeDrawingPadding().padding(Jd.Space4))
            }
        }
        val pending = request
        when {
            pending != null -> RequestScreen(pending, onAnswer = viewModel::answerClientRequest)
            pin != null -> PinScreen(pin!!, clientName, onCancel = viewModel::dismissPin)
            lost != null -> ConnectionLostOverlay(lost, onHome = viewModel::dismissConnectionLost)
            mirroring -> MirroringOverlay(clientName, onStop = viewModel::disconnectSenders)
            audio -> AudioScreen(viewModel, clientName, inputTick)
            route == Route.SETTINGS -> SettingsHome(
                viewModel,
                onOpenDevices = { route = Route.DEVICES },
                onOpenAdvanced = { route = Route.ADVANCED },
                onOpenLogs = { route = Route.LOGS },
            )
            route == Route.DEVICES -> DevicesScreen(
                devices,
                onRemove = viewModel::removeAllowedDevice,
                onRemoveAll = viewModel::clearAllowedDevices,
                onBack = { route = Route.SETTINGS },
            )
            route == Route.ADVANCED -> AdvancedSettingsScreen(viewModel, onBack = { route = Route.SETTINGS })
            route == Route.LOGS -> LogsRoute(viewModel)
            !lanAvailable && connections == 0 -> NoNetworkScreen(
                onOpenNetworkSettings = { context.openNetworkSettings() },
                onRetry = { if (state != ServerState.RUNNING) viewModel.startServer() },
            )
            screensaver -> Screensaver(serverName)
            else -> IdleScreen(
                state = state,
                localNetwork = localNetwork,
                serverName = serverName,
                clientName = clientName,
                connections = connections,
                recentDevices = devices,
                onStart = viewModel::startServer,
                onLocalNetwork = onLocalNetwork,
                onOpenSettings = { route = Route.SETTINGS },
                onOpenDevices = { route = Route.DEVICES },
            )
        }
    }
}

private fun Context.openNetworkSettings() {
    val wifi = Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(wifi)
    } catch (_: ActivityNotFoundException) {
        startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private const val SCREENSAVER_AFTER_MS = 120_000L

private const val VIDEO_OVERLAY_HIDE_MS = 4000L

@Composable
private fun DebugOverlay(info: DebugInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val style = MaterialTheme.typography.labelSmall
        val headingStyle = style.copy(fontWeight = FontWeight.Bold)
        val color = Color.White.copy(alpha = 0.9f)
        val headingColor = Color(0xFF80D8FF) // cyan
        val context = LocalContext.current
        debugOverlaySections(context, info).forEachIndexed { i, section ->
            if (i > 0) Spacer(Modifier.height(6.dp))
            Text(section.title.uppercase(), style = headingStyle, color = headingColor)
            section.lines.forEach { Text(it, style = style, color = color) }
        }
    }
}

private data class DebugSection(val title: String, val lines: List<String>)

private fun debugOverlaySections(context: Context, info: DebugInfo): List<DebugSection> = buildList {
    buildList {
        if (info.videoCodec.isNotEmpty()) {
            add(context.getString(R.string.debug_video, info.videoCodec, info.videoRes))
            add(context.getString(R.string.debug_fps_bitrate, info.videoFps, info.bitrateStr))
            add(context.getString(R.string.debug_frames_drops, info.videoFrames, info.droppedFrames))
            add(context.getString(R.string.debug_jitter, info.jitterStr))
        }
    }.takeIf { it.isNotEmpty() }?.let { add(DebugSection(context.getString(R.string.debug_section_video), it)) }

    buildList {
        if (info.audioCodec.isNotEmpty()) add(context.getString(R.string.debug_audio, info.audioCodec, info.serverVolume, info.clientVolume))
        info.audio?.let { a ->
            add(context.getString(R.string.debug_audio_buffer, a.backlogMs, a.tunedCushionMs))
            add(context.getString(R.string.debug_audio_glitch, a.trims, a.drops, a.silences, a.underruns, a.xrun))
            add(context.getString(R.string.debug_audio_decode, formatDecode(a.decodeMeanUs, a.decodeMaxUs, a.decodeHeld, a.decodeErrors)))
        }
    }.takeIf { it.isNotEmpty() }?.let { add(DebugSection(context.getString(R.string.debug_section_audio), it)) }

    add(DebugSection(context.getString(R.string.debug_section_network), listOf(context.getString(R.string.debug_clients, info.connections))))
}

// "mean/max ms held=N", or "held=N" before first latency window lands
private fun formatDecode(meanUs: Int, maxUs: Int, held: Int, errors: Int): String =
    (if (meanUs == 0) "held=$held"
    else "%.1f/%.1f ms held=%d".format(meanUs / 1000.0, maxUs / 1000.0, held)) + " errs=$errors"
