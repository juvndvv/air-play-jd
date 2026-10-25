package io.github.jqssun.airplay.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jqssun.airplay.R

object Jd {
    val Bg = Color(0xFF0B0A0D)
    val Surface1 = Color(0xFF1C1C1E)
    val Surface3 = Color(0xFF3A3A3C)
    val Ambient1 = Color(0xFFFC5858)
    val Ambient2 = Color(0xFF8C2A2E)
    val Ambient3 = Color(0xFF3D1013)
    val Glass = Color.White.copy(alpha = 0.10f)
    val GlassTint = Color(0xFF0A0A0C).copy(alpha = 0.5f)
    val GlassStrong = Color.White.copy(alpha = 0.18f)
    val GlassEdge = Color.White.copy(alpha = 0.42f)
    val GlassEdgeLow = Color.White.copy(alpha = 0.12f)
    val GlassFocus = Color(0xFFFAFAFC).copy(alpha = 0.92f)
    val GlassFallback = Color(0xFF1C1C1E).copy(alpha = 0.92f)
    val Ink = Color(0xFFF5F5F7)
    val InkMuted = Color(0xFFC7C7CC)
    val InkFaint = Color(0xFF7C7C82)
    val OnFocus = Color(0xFF1D1D1F)
    val OnFocusMuted = Color(0xFF5C5C62)
    val Signal = Color(0xFFFC5858)
    val OnSignal = Color(0xFF1A0606)
    val SignalSoft = Color(0xFF3A1414)

    val Space1 = 4.dp
    val Space2 = 8.dp
    val Space3 = 12.dp
    val Space4 = 16.dp
    val Space6 = 24.dp
    val Space8 = 32.dp
    val Space12 = 48.dp
    val SafeX = 48.dp
    val SafeY = 27.dp

    val RadiusSm = 12.dp
    val RadiusMd = 20.dp
    val RadiusLg = 32.dp
    val ShapeSm = RoundedCornerShape(RadiusSm)
    val ShapeMd = RoundedCornerShape(RadiusMd)
    val ShapeLg = RoundedCornerShape(RadiusLg)
    val ShapePill = RoundedCornerShape(percent = 50)

    const val ScaleFocus = 1.1f
    const val ScaleRow = 1.03f
    const val ScalePressed = 0.97f
    val BlurGlass = 24.dp
    val BlurStrong = 40.dp
    const val AmbientStrength = 0.45f
}

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

object JdType {
    private fun style(size: Int, line: Int, weight: FontWeight, tracking: Float = 0f) = TextStyle(
        fontFamily = Inter,
        fontSize = size.sp,
        lineHeight = line.sp,
        fontWeight = weight,
        letterSpacing = tracking.sp,
    )

    val Display = style(40, 46, FontWeight.Bold, -0.8f)
    val Title = style(28, 34, FontWeight.Bold, -0.4f)
    val Headline = style(22, 28, FontWeight.SemiBold)
    val PlayerTitle = style(22, 28, FontWeight.SemiBold, -0.2f)
    val Body = style(18, 24, FontWeight.Normal)
    val BodyStrong = style(18, 24, FontWeight.SemiBold)
    val Callout = style(16, 22, FontWeight.Normal)
    val CardTitle = style(16, 22, FontWeight.SemiBold)
    val Caption = style(14, 18, FontWeight.Normal)
    val CaptionStrong = style(14, 18, FontWeight.Medium)
    val Button = style(18, 24, FontWeight.SemiBold)
    val Pin = style(72, 80, FontWeight.SemiBold, 8f)
    val Clock = style(56, 60, FontWeight.SemiBold, -1.5f)
}
