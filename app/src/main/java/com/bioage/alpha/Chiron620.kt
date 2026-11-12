package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin

private val cyan620 = Color(0xFFAAEFE5)
private val white620 = Color(0xFFF0F8FA)
private val soft620 = Color(0xFFB6CFD7)

/** Abstract energy being: no human avatar, no gestures, no network dependencies. */
@Composable
fun ChironEnergy620(modifier: Modifier = Modifier) {
    val animation = rememberInfiniteTransition(label = "chiron-energy")
    val breath by animation.animateFloat(0.87f, 1.12f,
        infiniteRepeatable(tween(3600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breath")
    val phase by animation.animateFloat(0f, 6.28318f,
        infiniteRepeatable(tween(22000, easing = LinearEasing)), label = "orbit")
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension * 0.20f * breath
        drawCircle(brush = Brush.radialGradient(listOf(cyan620.copy(alpha = .37f), cyan620.copy(alpha = .06f), Color.Transparent), center = c, radius = r * 2.7f), center = c, radius = r * 2.7f)
        drawCircle(brush = Brush.radialGradient(listOf(Color.White, cyan620, Color(0xFF268DA2).copy(alpha = .65f), Color.Transparent), center = c, radius = r), center = c, radius = r)
        for (i in 0 until 3) {
            val radius = r * (1.35f + i * .29f)
            drawCircle(color = cyan620.copy(alpha = .26f - i * .065f), radius = radius, center = c, style = Stroke(width = (2.5f - i * .5f).dp.toPx()))
        }
        for (i in 0 until 22) {
            val angle = phase * (if (i % 2 == 0) 1 else -1) + i * 2.39996f
            val dist = r * (1.35f + (i % 5) * .22f)
            val p = Offset(c.x + cos(angle) * dist, c.y + sin(angle) * dist * .72f)
            drawCircle(color = cyan620.copy(alpha = .22f + (i % 4) * .13f), radius = (1.1f + i % 3).dp.toPx(), center = p)
        }
    }
}

private fun numericSleep620(prefs: SharedPreferences): Double? =
    prefs.getString("sleep", "")?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..24.0 }

private fun numericSteps620(prefs: SharedPreferences): Int? =
    prefs.getString("steps", "")?.toIntOrNull()?.takeIf { it in 0..150000 }

/** Never substitutes missing heart rate, HRV, or a WHOOP score with invented values. */
@Composable
fun Wellness620(prefs: SharedPreferences, refresh: Int) {
    val sleep = remember(refresh) { numericSleep620(prefs) }
    val steps = remember(refresh) { numericSteps620(prefs) }
    val date = LocalDate.now().toString()
    val habits = (0..2).count { prefs.getBoolean("day_${date}_habit_$it", false) }
    Column(Modifier.fillMaxWidth().background(Color(0xA51A394D), RoundedCornerShape(22.dp)).padding(16.dp)) {
        Text("СОСТОЯНИЕ СЕГОДНЯ", color = cyan620, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Сон", color = soft620, fontSize = 12.sp)
                Text(sleep?.let { "$it ч" } ?: "Нет данных", color = white620, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text("Шаги", color = soft620, fontSize = 12.sp)
                Text(steps?.toString() ?: "Нет данных", color = white620, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f)) {
                Text("Привычки", color = soft620, fontSize = 12.sp)
                Text("$habits/3", color = white620, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Восстановление и нагрузка WHOOP-типа появятся после подключения проверенных источников пульса и HRV. Без них баллы не рассчитываются.", color = soft620, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
fun ChironPresence620(prefs: SharedPreferences, refresh: Int) {
    val sleep = remember(refresh) { numericSleep620(prefs) }
    val steps = remember(refresh) { numericSteps620(prefs) }
    val advice = when {
        sleep != null && sleep < 6.0 -> "По записи сон был коротким. Сегодня лучше выбрать посильную нагрузку и оставить время на отдых."
        sleep != null && sleep >= 7.0 && steps != null -> "Есть записи сна и движения. Продолжай наблюдать динамику — один день ещё не показывает тенденцию."
        steps != null -> "Записано $steps шагов. Сравним их с другими днями, когда появится история."
        else -> "Я рядом. Добавь сон, шаги или самочувствие, и мы начнём отслеживать изменения."
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        ChironEnergy620(Modifier.fillMaxWidth().height(210.dp))
        Text("ХИРОН", color = cyan620, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Spacer(Modifier.height(8.dp))
        Text("Твой спокойный проводник", color = soft620, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth().background(Color(0xC3133448), RoundedCornerShape(20.dp)).padding(16.dp)) {
            Text(advice, color = white620, lineHeight = 23.sp)
            Spacer(Modifier.height(8.dp))
            Text("Совет по локальным записям, не медицинский вывод.", color = soft620, fontSize = 11.sp)
        }
    }
}
