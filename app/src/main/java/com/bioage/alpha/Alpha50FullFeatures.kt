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
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val Frost = Color(0xFFB5F0E7)
private val Soft = Color(0xFFB7CDD5)
private val White = Color(0xFFF1F8FA)

/** 0.36–0.40: compact scene legend and 90-day summit display. */
@Composable
fun SceneInfo50() {
    Text("Эльбрус · стилизованная 3D-панорама", color = Soft, fontSize = 11.sp)
}

/** 0.41–0.45: explicitly evidence-limited trend summary; missing days are not zero. */
@Composable
fun ChironInsights50(prefs: SharedPreferences, refresh: Int) {
    val dates = remember(refresh) { (0L..13L).map { LocalDate.now().minusDays(it) } }
    val first = dates.take(7)
    val second = dates.drop(7)
    fun avgSleep(ds: List<LocalDate>): Double? {
        val values = ds.mapNotNull { day ->
            prefs.getString("health_${day}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..24.0 }
        }
        return if (values.size >= 3) values.average() else null
    }
    val current = avgSleep(first)
    val previous = avgSleep(second)
    Spacer(Modifier.height(16.dp))
    Text("Хирон · динамика сна", color = White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    if (current == null || previous == null) {
        Text("Для сравнения нужны минимум три записи сна в каждой из двух недель.", color = Soft, fontSize = 12.sp)
    } else {
        val delta = current - previous
        Text("Прошлая неделя: ${String.format(Locale.getDefault(), "%.1f", previous)} ч · текущая: ${String.format(Locale.getDefault(), "%.1f", current)} ч", color = White, fontSize = 13.sp)
        Text(when {
            delta > .5 -> "По сохранённым дням средний сон увеличился."
            delta < -.5 -> "По сохранённым дням средний сон уменьшился."
            else -> "Средний сон за две недели близок."
        }, color = Frost, fontSize = 13.sp)
    }
    Text("Это наблюдение по ручным записям, не медицинский вывод.", color = Soft, fontSize = 11.sp)
}

/** 0.46–0.48: personal weekly reflection, saved offline and included in JSON backup. */
@Composable
fun WeeklyReflection50(prefs: SharedPreferences) {
    val week = LocalDate.now().with(java.time.DayOfWeek.MONDAY).toString()
    val key = "health_reflection_$week"
    var note by remember(week) { mutableStateOf(prefs.getString(key, "") ?: "") }
    var confirmed by remember { mutableStateOf(false) }
    Spacer(Modifier.height(16.dp))
    Text("Итог недели", color = White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
    OutlinedTextField(value = note, onValueChange = { note = it.take(400); confirmed = false },
        label = { Text("Что помогало восстанавливаться?") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
    Button(onClick = { prefs.edit().putString(key, note).apply(); confirmed = true }) { Text("Сохранить заметку") }
    if (confirmed) Text("Сохранено на устройстве", color = Frost, fontSize = 12.sp)
}

/** 0.49: useful, testable on-device validation of existing data. */
@Composable
fun DataAudit50(prefs: SharedPreferences) {
    val all = prefs.all
    val healthDays = all.keys.mapNotNull { k ->
        if (!k.startsWith("health_") || !k.endsWith("_sleep")) null
        else runCatching { LocalDate.parse(k.removePrefix("health_").removeSuffix("_sleep")) }.getOrNull()
    }.distinct()
    val badSleep = healthDays.count { day ->
        val raw = prefs.getString("health_${day}_sleep", null)
        raw?.isNotBlank() == true && raw.replace(',', '.').toDoubleOrNull()?.let { it !in 0.0..24.0 } != false
    }
    Spacer(Modifier.height(12.dp))
    Text("Локальная проверка", color = White, fontWeight = FontWeight.Bold)
    Text("Дней с записью сна: ${healthDays.size}. Некорректных значений: $badSleep.", color = Soft, fontSize = 12.sp)
    Text("Это проверка сохранённых полей, а не автоматический тест APK.", color = Soft, fontSize = 11.sp)
}
