package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val Ice100 = Color(0xFFB5F0E7)
private val Muted100 = Color(0xFFB7CDD5)
private val Text100 = Color(0xFFF1F8FA)
private val Surface100 = Color(0xAA23485A)
private fun sleep100(p: SharedPreferences, d: LocalDate): Double? = p.getString("health_${d}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..24.0 }
private fun steps100(p: SharedPreferences, d: LocalDate): Int? = p.getString("health_${d}_steps", null)?.toIntOrNull()?.takeIf { it in 0..1000000 }
private fun marks100(p: SharedPreferences, d: LocalDate) = (0..2).count { p.getBoolean("day_${d}_habit_$it", false) }

/** 0.71–0.76: compact health dashboard; missing values remain missing. */
@Composable
fun HealthDashboard100(prefs: SharedPreferences, refresh: Int) {
    val days = (0L..6L).map { LocalDate.now().minusDays(it) }
    val sleep = days.mapNotNull { sleep100(prefs, it) }
    val steps = days.mapNotNull { steps100(prefs, it) }
    Spacer(Modifier.height(14.dp))
    Text("Сводка за 7 дней", color = Text100, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f).background(Surface100, RoundedCornerShape(14.dp)).padding(12.dp)) {
            Text("СОН", color = Muted100, fontSize = 11.sp)
            Text(if (sleep.isEmpty()) "—" else String.format(Locale.getDefault(), "%.1f ч", sleep.average()), color = Text100, fontSize = 20.sp)
            Text("${sleep.size}/7 записей", color = Muted100, fontSize = 11.sp)
        }
        Column(Modifier.weight(1f).background(Surface100, RoundedCornerShape(14.dp)).padding(12.dp)) {
            Text("ШАГИ", color = Muted100, fontSize = 11.sp)
            Text(if (steps.isEmpty()) "—" else "${steps.average().roundToInt()}", color = Text100, fontSize = 20.sp)
            Text("${steps.size}/7 записей", color = Muted100, fontSize = 11.sp)
        }
    }
    Text("Среднее только по сохранённым дням.", color = Muted100, fontSize = 11.sp)
}

/** 0.77–0.82: one summit overview instead of duplicate progress bars. */
@Composable
fun SummitDashboard100(prefs: SharedPreferences, refresh: Int) {
    val total = (0L..89L).sumOf { marks100(prefs, LocalDate.now().minusDays(it)) }
    val checkpoints = listOf(0 to "Старт", 30 to "Привал", 90 to "Тропа", 180 to "Лагерь", 270 to "Вершина")
    val next = checkpoints.firstOrNull { total < it.first }
    Spacer(Modifier.height(14.dp))
    Text("Маршрут к вершине", color = Text100, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Text("$total из 270 отметок за последние 90 дней", color = Muted100, fontSize = 12.sp)
    Spacer(Modifier.height(6.dp))
    LinearProgressIndicator(progress = { (total / 270f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(7.dp), color = Ice100)
    Spacer(Modifier.height(6.dp))
    Text(if (next == null) "Вершина достигнута" else "Следующая точка: ${next.second} · ещё ${next.first - total} отметок", color = Ice100, fontSize = 12.sp)
    Text("Это игровая шкала привычек, не оценка физической формы.", color = Muted100, fontSize = 11.sp)
}

/** 0.83–0.88: deterministic, explainable offline guidance based on actual records. */
@Composable
fun ChironExplain100(prefs: SharedPreferences, refresh: Int) {
    val days = (0L..13L).map { LocalDate.now().minusDays(it) }
    val current = days.take(7).mapNotNull { sleep100(prefs, it) }
    val previous = days.drop(7).mapNotNull { sleep100(prefs, it) }
    val calm = prefs.getBoolean("chiron_rest_mode_70", false)
    val observation = when {
        calm -> "Спокойный режим включён. Рекомендации не ориентируются на рекорды."
        current.size < 3 -> "Недостаточно записей сна: ${current.size} из 3 необходимых за неделю."
        previous.size < 3 -> "Есть ${current.size} записей за эту неделю. Для сравнения нужно ещё ${3 - previous.size} за предыдущую."
        current.average() < previous.average() - 0.5 -> "Средний сон за записанные дни сократился более чем на 30 минут."
        current.average() > previous.average() + 0.5 -> "Средний сон за записанные дни увеличился более чем на 30 минут."
        else -> "Средняя продолжительность сна за две недели близка."
    }
    Spacer(Modifier.height(14.dp))
    Text("Хирон · почему такой совет", color = Text100, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Text(observation, color = Ice100, fontSize = 13.sp)
    Text("Основание: ${current.size} записей за текущие 7 дней, ${previous.size} за предыдущие 7 дней.", color = Muted100, fontSize = 11.sp)
    Text("Офлайн-правила. Не медицинский диагноз и не обученная модель.", color = Muted100, fontSize = 11.sp)
}

/** 0.89–0.95: reversible, locally persisted preference for less visual clutter. */
@Composable
fun CompactMode100(prefs: SharedPreferences) {
    var compact by remember { mutableStateOf(prefs.getBoolean("compact_health_100", true)) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Компактная аналитика", color = Text100, fontWeight = FontWeight.Bold)
            Text("Скрывать подробные разделы", color = Muted100, fontSize = 12.sp)
        }
        Switch(checked = compact, onCheckedChange = { compact = it; prefs.edit().putBoolean("compact_health_100", it).apply() })
    }
}

/** 0.96–1.00: release diagnostics without destructive migration. */
@Composable
fun ReleaseStatus100(prefs: SharedPreferences) {
    val days = (0L..29L).map { LocalDate.now().minusDays(it) }
    val n = days.count { sleep100(prefs, it) != null || steps100(prefs, it) != null }
    Spacer(Modifier.height(12.dp))
    Text("Alpha 1.00 · проверка данных", color = Text100, fontWeight = FontWeight.Bold)
    Text("За 30 дней: $n дней с записями. Данные хранятся локально.", color = Muted100, fontSize = 12.sp)
    Text("Перед переустановкой APK сделай резервную копию в профиле.", color = Muted100, fontSize = 11.sp)
}
