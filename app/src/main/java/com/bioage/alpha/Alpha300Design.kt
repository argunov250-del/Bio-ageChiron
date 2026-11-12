package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Locale

private val ice300 = Color(0xFFAEF5E9)
private val white300 = Color(0xFFF2FAFD)
private val dim300 = Color(0xFFBED2DC)
private val edge300 = Color(0x66B7E7F3)
private val glass300 = Brush.linearGradient(listOf(Color(0xD6224861), Color(0xE00B2337), Color(0xD514344B)))

@Composable
fun GlassPanel300(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.background(glass300, RoundedCornerShape(23.dp))
        .border(1.dp, edge300, RoundedCornerShape(23.dp)).padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp), content = content)
}

@Composable
fun Hero300(steps: String, sleep: String, habits: Int) {
    Text("Твой путь к вершине", color = white300, fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
    Text("Здоровье · Ритм · Восхождение", color = dim300, fontSize = 12.sp)
    Spacer(Modifier.height(5.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf(Triple("ШАГИ", steps.ifBlank { "—" }, "↗"), Triple("СОН", if(sleep.isBlank()) "—" else "$sleep ч", "☾"), Triple("ПРИВЫЧКИ", "$habits/3", "✓")).forEach { (label, value, symbol) ->
            GlassPanel300(Modifier.weight(1f)) {
                Text(symbol, color = ice300, fontSize = 17.sp)
                Text(label, color = dim300, fontSize = 9.sp, maxLines = 1)
                Text(value, color = white300, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
fun SummitCard300(prefs: SharedPreferences, refresh: Int) {
    val today = LocalDate.now()
    val total = (0L..89L).sumOf { offset ->
        val date = today.minusDays(offset).toString()
        (0..2).count { prefs.getBoolean("day_${date}_habit_$it", false) }
    }
    val checkpoints = listOf(30 to "Первый привал", 90 to "Горная тропа", 180 to "Высокий лагерь", 270 to "Вершина")
    val next = checkpoints.firstOrNull { total < it.first }
    GlassPanel300(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("ВОСХОЖДЕНИЕ НА ЭЛЬБРУС", color = ice300, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("${(total * 100 / 270).coerceIn(0,100)}%", color = white300, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { (total / 270f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(7.dp), color = ice300, trackColor = Color(0xFF35566C))
        Text(if(next == null) "Вершина достигнута" else "Следующая точка: ${next.second} · ${next.first - total} отметок", color = dim300, fontSize = 12.sp)
        Text("Игровой маршрут · отметки за последние 90 дней", color = dim300, fontSize = 10.sp)
    }
}

@Composable
fun HealthSnapshot300(prefs: SharedPreferences, refresh: Int) {
    val dates = (0L..6L).map { LocalDate.now().minusDays(it).toString() }
    val sleeps = dates.mapNotNull { prefs.getString("health_${it}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { n -> n in 0.0..24.0 } }
    val steps = dates.mapNotNull { prefs.getString("health_${it}_steps", null)?.toIntOrNull()?.takeIf { n -> n in 0..1000000 } }
    GlassPanel300(Modifier.fillMaxWidth()) {
        Text("НЕДЕЛЯ В ЦИФРАХ", color = ice300, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text("СОН", color = dim300, fontSize = 10.sp)
                Text(if(sleeps.isEmpty()) "—" else String.format(Locale.getDefault(), "%.1f ч", sleeps.average()), color = white300, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${sleeps.size}/7 записей", color = dim300, fontSize = 11.sp)
            }
            Column(Modifier.weight(1f)) {
                Text("ШАГИ", color = dim300, fontSize = 10.sp)
                Text(if(steps.isEmpty()) "—" else "${steps.average().toInt()}", color = white300, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${steps.size}/7 записей", color = dim300, fontSize = 11.sp)
            }
        }
        Text("Средние только по сохранённым дням, без подстановки нулей", color = dim300, fontSize = 10.sp)
    }
}

@Composable
fun ChironPreview300(prefs: SharedPreferences, sleep: String, steps: String, habits: Int, onOpen: () -> Unit) {
    val h = sleep.replace(',', '.').toDoubleOrNull()
    val tip = when {
        h != null && h < 6 -> "Сон короче шести часов. Сегодня можно снизить нагрузку и выделить время на отдых."
        habits == 3 -> "Все привычки отмечены. Сохраняй комфортный ритм: отдых тоже важен."
        // Use the user's saved step goal (Goals30, default 6000) rather than a fixed 10000.
        steps.toIntOrNull()?.let { it >= prefs.getInt("goal_steps", 6000) } == true -> "Цель по шагам достигнута. Оценивай нагрузку по своему самочувствию."
        else -> "Добавь записи о сне и движении — я помогу увидеть изменения со временем."
    }
    GlassPanel300(Modifier.fillMaxWidth()) {
        Text("ХИРОН · ТВОЙ ПОМОЩНИК", color = ice300, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(tip, color = white300, fontSize = 14.sp, lineHeight = 21.sp)
        Text("Офлайн-рекомендация по записям, не медицинский вывод", color = dim300, fontSize = 10.sp)
        OutlinedButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text("Открыть диалог с Хироном") }
    }
}

@Composable
fun VisualSettings300(prefs: SharedPreferences) {
    var compact by remember { mutableStateOf(prefs.getBoolean("visual_compact_300", false)) }
    GlassPanel300(Modifier.fillMaxWidth()) {
        Text("ВНЕШНИЙ ВИД · ALPHA 3.00", color = ice300, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Компактный главный экран", color = white300, fontSize = 13.sp)
                Text("Уменьшает число карточек поверх гор", color = dim300, fontSize = 11.sp)
            }
            Switch(checked = compact, onCheckedChange = { compact = it; prefs.edit().putBoolean("visual_compact_300", it).apply() })
        }
        Text("3D-сцена и данные сохраняются без изменений", color = dim300, fontSize = 11.sp)
    }
}
