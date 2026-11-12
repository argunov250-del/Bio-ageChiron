package com.bioage.alpha

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Major 1 design tokens: cinematic dark navy, restrained glass, alpine mint accent. */
object M1 {
    val Bg = Color(0xFF061521)
    val Surface = Color(0xFF102638)
    val Primary = Color(0xFF67E6CD)
    val Ink = Color(0xFFF2FAFC)
    val Muted = Color(0xFFA7C2CF)
    val Warning = Color(0xFFF6BD75)
    val Track = Color(0xFF23404F)
    val Line = Color(0x26CFF3F7)
    val HeroShadow = TextStyle(shadow = Shadow(color = Color(0xCC03101A), offset = Offset(0f, 2f), blurRadius = 14f))
}

fun m1Colors(): ColorScheme = darkColorScheme(
    primary = M1.Primary, onPrimary = M1.Bg,
    primaryContainer = Color(0xFF14394A), onPrimaryContainer = M1.Ink,
    secondary = M1.Primary, onSecondary = M1.Bg,
    secondaryContainer = Color(0xFF1A3D4E), onSecondaryContainer = M1.Ink,
    tertiary = M1.Warning, onTertiary = M1.Bg,
    tertiaryContainer = Color(0xFF3A3122), onTertiaryContainer = M1.Ink,
    background = M1.Bg, onBackground = M1.Ink,
    surface = M1.Surface, onSurface = M1.Ink,
    surfaceVariant = Color(0xFF1B3547), onSurfaceVariant = M1.Muted,
    surfaceContainerLowest = M1.Bg, surfaceContainerLow = Color(0xFF0C1F2E),
    surfaceContainer = M1.Surface, surfaceContainerHigh = Color(0xFF16303F),
    surfaceContainerHighest = Color(0xFF1B3547),
    outline = Color(0xFF5E8291), outlineVariant = Color(0xFF2A4658)
)

/** Translucent panel with a fine 1dp highlight. No blur: cheap on mid-range phones. */
@Composable
fun M1Card(modifier: Modifier = Modifier, padding: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xC81B3A52), Color(0xD00E2434))))
            .border(1.dp, Brush.verticalGradient(listOf(Color(0x59CFF3F7), Color(0x12CFF3F7))), shape)
            .padding(padding),
        content = content
    )
}

@Composable
fun M1SectionTitle(text: String) {
    Text(text, color = M1.Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
}

enum class M1Glyph { Home, Pulse, Mountain, Person, Heart, Moon, Bolt, Steps, Chevron, ChevronDown, Check, Lock, Box, Info, Leaf }

@Composable
fun M1Icon(glyph: M1Glyph, tint: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) { drawGlyph(glyph, tint) }
}

private fun DrawScope.drawGlyph(glyph: M1Glyph, tint: Color) {
    val u = size.minDimension / 24f
    val stroke = Stroke(width = 1.9f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun path(build: Path.() -> Unit) = drawPath(Path().apply(build), tint, style = stroke)
    when (glyph) {
        M1Glyph.Home -> path {
            moveTo(4f * u, 11f * u); lineTo(12f * u, 4f * u); lineTo(20f * u, 11f * u); lineTo(20f * u, 20f * u)
            lineTo(14.5f * u, 20f * u); lineTo(14.5f * u, 14f * u); lineTo(9.5f * u, 14f * u); lineTo(9.5f * u, 20f * u)
            lineTo(4f * u, 20f * u); close()
        }
        M1Glyph.Pulse -> path {
            moveTo(3f * u, 12f * u); lineTo(8f * u, 12f * u); lineTo(10.5f * u, 6f * u); lineTo(14f * u, 18f * u)
            lineTo(16f * u, 12f * u); lineTo(21f * u, 12f * u)
        }
        M1Glyph.Mountain -> path {
            moveTo(3f * u, 19f * u); lineTo(9f * u, 7f * u); lineTo(12.5f * u, 13f * u); lineTo(15.5f * u, 9.5f * u)
            lineTo(21f * u, 19f * u); close()
        }
        M1Glyph.Person -> {
            drawCircle(tint, radius = 3.6f * u, center = Offset(12f * u, 8f * u), style = stroke)
            path { moveTo(5f * u, 20f * u); cubicTo(5f * u, 14.5f * u, 19f * u, 14.5f * u, 19f * u, 20f * u) }
        }
        M1Glyph.Heart -> path {
            moveTo(12f * u, 20f * u); cubicTo(3f * u, 14f * u, 3.5f * u, 5.5f * u, 8.5f * u, 5.5f * u)
            cubicTo(10.5f * u, 5.5f * u, 12f * u, 7.5f * u, 12f * u, 8.5f * u)
            cubicTo(12f * u, 7.5f * u, 13.5f * u, 5.5f * u, 15.5f * u, 5.5f * u)
            cubicTo(20.5f * u, 5.5f * u, 21f * u, 14f * u, 12f * u, 20f * u)
        }
        M1Glyph.Moon -> path {
            moveTo(18.5f * u, 14.5f * u); cubicTo(12f * u, 16.5f * u, 7.5f * u, 11.5f * u, 10f * u, 5f * u)
            cubicTo(4.5f * u, 7f * u, 3.5f * u, 14.5f * u, 8.5f * u, 18f * u)
            cubicTo(12.5f * u, 20.5f * u, 17.5f * u, 18.5f * u, 18.5f * u, 14.5f * u)
        }
        M1Glyph.Bolt -> path {
            moveTo(13f * u, 3f * u); lineTo(6f * u, 13.5f * u); lineTo(11.5f * u, 13.5f * u); lineTo(10.5f * u, 21f * u)
            lineTo(18f * u, 10.5f * u); lineTo(12.5f * u, 10.5f * u); close()
        }
        M1Glyph.Steps -> path {
            moveTo(3.5f * u, 17.5f * u); lineTo(20.5f * u, 17.5f * u); lineTo(20.5f * u, 15f * u)
            cubicTo(17f * u, 14.5f * u, 14.5f * u, 12.5f * u, 13f * u, 8.5f * u)
            lineTo(9.5f * u, 10.5f * u); lineTo(8.5f * u, 8f * u); lineTo(3.5f * u, 9.5f * u); close()
        }
        M1Glyph.Chevron -> path { moveTo(9f * u, 6f * u); lineTo(15f * u, 12f * u); lineTo(9f * u, 18f * u) }
        M1Glyph.ChevronDown -> path { moveTo(6f * u, 9f * u); lineTo(12f * u, 15f * u); lineTo(18f * u, 9f * u) }
        M1Glyph.Check -> path { moveTo(5f * u, 12.5f * u); lineTo(10f * u, 17.5f * u); lineTo(19f * u, 7f * u) }
        M1Glyph.Lock -> {
            path {
                moveTo(6f * u, 11f * u); lineTo(18f * u, 11f * u); lineTo(18f * u, 20f * u); lineTo(6f * u, 20f * u); close()
            }
            path { moveTo(8.5f * u, 11f * u); lineTo(8.5f * u, 8f * u); cubicTo(8.5f * u, 3.5f * u, 15.5f * u, 3.5f * u, 15.5f * u, 8f * u); lineTo(15.5f * u, 11f * u) }
        }
        M1Glyph.Box -> {
            path { moveTo(4f * u, 8f * u); lineTo(20f * u, 8f * u); lineTo(20f * u, 19.5f * u); lineTo(4f * u, 19.5f * u); close() }
            path { moveTo(9f * u, 8f * u); lineTo(9f * u, 4.5f * u); lineTo(15f * u, 4.5f * u); lineTo(15f * u, 8f * u) }
            path { moveTo(9.5f * u, 13.5f * u); lineTo(14.5f * u, 13.5f * u) }
        }
        M1Glyph.Info -> {
            drawCircle(tint, radius = 8.5f * u, center = Offset(12f * u, 12f * u), style = stroke)
            path { moveTo(12f * u, 11f * u); lineTo(12f * u, 16.5f * u) }
            drawCircle(tint, radius = 1.1f * u, center = Offset(12f * u, 7.8f * u))
        }
        M1Glyph.Leaf -> {
            path { moveTo(5f * u, 19f * u); cubicTo(5f * u, 9f * u, 11f * u, 5f * u, 19f * u, 5f * u); cubicTo(19f * u, 13f * u, 15f * u, 19f * u, 5f * u, 19f * u) }
            path { moveTo(5f * u, 19f * u); lineTo(13f * u, 11f * u) }
        }
    }
}

/** Progress ring. With value == null only the track is drawn: nothing is invented. */
@Composable
fun M1Ring(value: Float?, diameter: Dp, strokeWidth: Dp = 9.dp, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = strokeWidth.toPx()
            val inset = sw / 2f
            val arcSize = Size(size.width - sw, size.height - sw)
            drawArc(M1.Track, 0f, 360f, false, topLeft = Offset(inset, inset), size = arcSize, style = Stroke(sw))
            if (value != null) {
                drawArc(M1.Primary, -90f, 360f * value.coerceIn(0f, 1f), false, topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(sw, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

/** Small non-human glowing orb: entry point to the existing Chiron dialogue. */
@Composable
fun M1Orb(diameter: Dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "m1-orb")
    val pulse by transition.animateFloat(0.86f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse), label = "m1-orb-pulse")
    Canvas(modifier.size(diameter)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f
        drawCircle(Brush.radialGradient(listOf(M1.Primary.copy(alpha = .45f), Color.Transparent), center = c, radius = r * pulse), radius = r * pulse, center = c)
        drawCircle(Brush.radialGradient(listOf(Color.White, M1.Primary, Color(0xFF1E8FA6)), center = c, radius = r * .5f), radius = r * .5f * pulse, center = c)
        drawCircle(M1.Primary.copy(alpha = .55f), radius = r * .74f, center = c, style = Stroke(1.2.dp.toPx()))
    }
}

/** 7 / 30 / 90 day selector. Each segment is at least 48dp tall. */
@Composable
fun M1Segmented(options: List<Int>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color(0xB30E2434))
            .border(1.dp, M1.Line, RoundedCornerShape(26.dp)).padding(4.dp)
    ) {
        options.forEach { option ->
            val active = option == selected
            Box(
                Modifier.weight(1f).heightIn(min = 48.dp).clip(RoundedCornerShape(22.dp))
                    .background(if (active) M1.Primary else Color.Transparent)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text("$option дней", color = if (active) M1.Bg else M1.Ink, fontSize = 14.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
            }
        }
    }
}

/** List row with icon, title, subtitle and optional trailing element. Min height 56dp. */
@Composable
fun M1Row(
    glyph: M1Glyph, title: String, subtitle: String?, modifier: Modifier = Modifier,
    enabled: Boolean = true, onClick: (() -> Unit)? = null, trailing: @Composable () -> Unit = {}
) {
    val base = modifier.fillMaxWidth().heightIn(min = 56.dp)
    Row(
        if (onClick != null && enabled) base.clip(RoundedCornerShape(14.dp)).clickable { onClick() } else base,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0x3367E6CD)), contentAlignment = Alignment.Center) {
            M1Icon(glyph, if (enabled) M1.Primary else M1.Muted, 20.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            Text(title, color = if (enabled) M1.Ink else M1.Muted, fontSize = 15.sp)
            if (subtitle != null) Text(subtitle, color = M1.Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}

@Composable
fun M1Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(M1.Line))
}

/** Compact metric tile for 2-column grids. Null value renders an em dash and «Нет данных». */
@Composable
fun M1Tile(glyph: M1Glyph, label: String, value: String?, unit: String, note: String?, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(Color(0x8C0C2130)).border(1.dp, M1.Line, RoundedCornerShape(16.dp)).padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            M1Icon(glyph, M1.Primary, 16.dp)
            Spacer(Modifier.width(6.dp))
            Text(label, color = M1.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value ?: "—", color = M1.Ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            if (value != null) {
                Spacer(Modifier.width(4.dp))
                Text(unit, color = M1.Muted, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
        Text(note ?: "Нет данных", color = M1.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
