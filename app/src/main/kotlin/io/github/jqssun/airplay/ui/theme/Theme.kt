package io.github.jqssun.airplay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JdColors = darkColorScheme(
    primary = Jd.Signal,
    onPrimary = Jd.OnSignal,
    primaryContainer = Jd.SignalSoft,
    onPrimaryContainer = Jd.Ink,
    secondary = Jd.InkMuted,
    onSecondary = Jd.OnFocus,
    secondaryContainer = Jd.Surface3,
    onSecondaryContainer = Jd.Ink,
    tertiary = Jd.Signal,
    background = Jd.Bg,
    onBackground = Jd.Ink,
    surface = Jd.Bg,
    onSurface = Jd.Ink,
    surfaceVariant = Jd.Surface1,
    onSurfaceVariant = Jd.InkMuted,
    surfaceContainerLowest = Jd.Bg,
    surfaceContainerLow = Jd.Surface1,
    surfaceContainer = Jd.Surface1,
    surfaceContainerHigh = Jd.Surface1,
    surfaceContainerHighest = Jd.Surface3,
    outline = Jd.InkFaint,
    outlineVariant = Jd.Surface3,
    error = Jd.Signal,
    onError = Jd.OnSignal,
)

private val JdTypography = Typography().let { t ->
    Typography(
        displayLarge = t.displayLarge.copy(fontFamily = Inter),
        displayMedium = t.displayMedium.copy(fontFamily = Inter),
        displaySmall = t.displaySmall.copy(fontFamily = Inter),
        headlineLarge = t.headlineLarge.copy(fontFamily = Inter),
        headlineMedium = t.headlineMedium.copy(fontFamily = Inter),
        headlineSmall = t.headlineSmall.copy(fontFamily = Inter),
        titleLarge = t.titleLarge.copy(fontFamily = Inter),
        titleMedium = t.titleMedium.copy(fontFamily = Inter),
        titleSmall = t.titleSmall.copy(fontFamily = Inter),
        bodyLarge = t.bodyLarge.copy(fontFamily = Inter),
        bodyMedium = t.bodyMedium.copy(fontFamily = Inter),
        bodySmall = t.bodySmall.copy(fontFamily = Inter),
        labelLarge = t.labelLarge.copy(fontFamily = Inter),
        labelMedium = t.labelMedium.copy(fontFamily = Inter),
        labelSmall = t.labelSmall.copy(fontFamily = Inter),
    )
}

@Composable
fun AirPlayTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = JdColors, typography = JdTypography, content = content)
}
