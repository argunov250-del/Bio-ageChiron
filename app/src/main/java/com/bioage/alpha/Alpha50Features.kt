package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

private val Frost50 = Color(0xFFB5F0E7)
private val Soft50 = Color(0xFFB7CDD5)
private val White50 = Color(0xFFF1F8FA)

private fun marks50(prefs: SharedPreferences, day: LocalDate): Int =
    (0..2).count { prefs.getBoolean("day_${day}_habit_$it", false) }

private fun total50(prefs: SharedPreferences): Int =
    (0L..89L).sumOf { marks50(prefs, LocalDate.now().minusDays(it)) }

@Composable
fun HomeSummit50(prefs: SharedPreferences, refresh: Int) {
    val total = remember(refresh) { total50(prefs) }
    val fraction = (total / 270f).coerceIn(0f, 1f)
    Spacer(Modifier.height(14.dp))
    Text("ВОСХОЖДЕНИЕ · ${(fraction * 100).roundToInt()}%", color = Frost50, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(5.dp))
    LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(5.dp), color = Frost50)
    Text("$total отметок за 90 дней · игровой прогресс", color = Soft50, fontSize = 11.sp)
}

@Composable
fun HealthSummary50(prefs: SharedPreferences, refresh: Int) {
    val days = remember(refresh) { (0L..6L).map { LocalDate.now().minusDays(it) } }
    val stepValues = days.mapNotNull { prefs.getString("health_${it}_steps", null)?.toIntOrNull()?.takeIf { n -> n in 0..1000000 } }
    val sleepValues = days.mapNotNull { prefs.getString("health_${it}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { n -> n in 0.0..24.0 } }
    Spacer(Modifier.height(16.dp))
    Text("Неделя в цифрах", color = White50, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Text("Сон: ${if (sleepValues.isEmpty()) "нет записей" else String.format(Locale.getDefault(), "%.1f ч", sleepValues.average())} · ${sleepValues.size} дн.", color = White50)
    Text("Шаги: ${if (stepValues.isEmpty()) "нет записей" else stepValues.average().roundToInt().toString()} · ${stepValues.size} дн.", color = White50)
    Text("Средние рассчитаны только по дням с записями.", color = Soft50, fontSize = 12.sp)
}

@Composable
fun SummitMilestones50(prefs: SharedPreferences, refresh: Int) {
    val total = remember(refresh) { total50(prefs) }
    val levels = listOf(0 to "Начало пути", 30 to "Первый привал", 90 to "Горная тропа", 180 to "Высокий лагерь", 270 to "Вершина")
    Spacer(Modifier.height(14.dp))
    Text("Контрольные точки", color = White50, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    levels.forEach { (need, label) ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = if (total >= need) Frost50 else Soft50, fontSize = 13.sp)
            Text(if (total >= need) "Достигнуто" else "$total / $need", color = Soft50, fontSize = 12.sp)
        }
        Spacer(Modifier.height(5.dp))
    }
    Text("Отметки за последние 90 дней; этапы могут изменяться при смене периода.", color = Soft50, fontSize = 11.sp)
}
