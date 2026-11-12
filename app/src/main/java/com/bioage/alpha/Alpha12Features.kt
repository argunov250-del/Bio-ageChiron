package com.bioage.alpha

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Highlight = Color(0xFFB5F0E7)
private val TextSoft = Color(0xFFC1D6DE)

/** Records are keyed by date. Existing current-value keys remain untouched. */
fun saveHealthHistory(prefs: SharedPreferences, steps: String, sleep: String, mood: String) {
    val day = LocalDate.now().toString()
    prefs.edit().putString("health_${day}_steps", steps)
        .putString("health_${day}_sleep", sleep)
        .putString("health_${day}_mood", mood).apply()
}

@Composable
fun HealthHistoryPanel(prefs: SharedPreferences, refresh: Int) {
    // refresh makes the panel update immediately after saving.
    val entries = remember(refresh) {
        (6 downTo 0).map { back ->
            val day = LocalDate.now().minusDays(back.toLong())
            val value = prefs.getString("health_${day}_steps", null)?.toIntOrNull()?.coerceAtLeast(0)
            Triple(day.format(DateTimeFormatter.ofPattern("dd.MM")), value, prefs.getString("health_${day}_sleep", null))
        }
    }
    Spacer(Modifier.height(18.dp))
    Text("История за 7 дней", color = Color.White, fontSize = 17.sp)
    Spacer(Modifier.height(6.dp))
    Text("Записи сохраняются по датам, без автоматического сбора данных", color = TextSoft, fontSize = 12.sp)
    Spacer(Modifier.height(12.dp))
    val maxSteps = (entries.mapNotNull { it.second }.maxOrNull() ?: 0).coerceAtLeast(1)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        entries.forEach { (day, steps, sleep) ->
            Column(Modifier.weight(1f)) {
                Box(Modifier.fillMaxWidth().height(64.dp).background(Color(0x442B5265), RoundedCornerShape(8.dp))) {
                    if (steps != null) {
                        Canvas(Modifier.fillMaxSize()) {
                            val bar = size.height * (steps.toFloat() / maxSteps)
                            drawRect(Highlight, topLeft = Offset(0f, size.height - bar), size = androidx.compose.ui.geometry.Size(size.width, bar))
                        }
                    }
                }
                Text(day.substring(0, 2), color = TextSoft, fontSize = 10.sp)
                if (sleep != null) Text("${sleep}ч", color = Highlight, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
    if (entries.all { it.second == null && it.third == null }) {
        Text("Пока нет записей по дням. Сохрани показатели на вкладке «Здоровье».", color = TextSoft, fontSize = 12.sp)
    }
}

/** Local JSON export/import via Android's document picker; no network or extra permissions. */
@Composable
fun LocalBackupPanel(prefs: SharedPreferences) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("Резервная копия хранится только там, где ты выберешь.") }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            val json = JSONObject().put("format", "bioage-local-v1")
            val values = JSONObject()
            prefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> values.put(key, value)
                    is Boolean -> values.put(key, value)
                    is Int -> values.put(key, value)
                    is Long -> values.put(key, value)
                    is Float -> values.put(key, value.toDouble())
                }
            }
            json.put("preferences", values)
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toString(2).toByteArray(Charsets.UTF_8)) }
                ?: error("Не удалось открыть файл")
        }.onSuccess { status = "Копия сохранена" }.onFailure { status = "Ошибка экспорта: ${it.localizedMessage}" }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("Не удалось открыть файл")
            require(raw.length < 2_000_000) { "Файл слишком большой" }
            val root = JSONObject(raw)
            require(root.optString("format") == "bioage-local-v1") { "Неподдерживаемый формат" }
            val values = root.getJSONObject("preferences")
            val edit = prefs.edit()
            val keys = values.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                // Only import application-owned keys; do not overwrite migration metadata.
                // Alpha 0.30 adds rest days (rest_<date>) and the step goal (goal_steps).
                // Alpha 0.70 adds gentle mode (chiron_rest_mode_70) and weekly focus (weekly_focus_70_<monday>).
                // Alpha 1.00/2.00 add compact_health_100 and chiron_rest_focus_200; Alpha 3.00 adds visual_compact_300.
                if (!(key in setOf("steps", "sleep", "mood", "goal_steps", "chiron_rest_mode_70", "compact_health_100", "chiron_rest_focus_200", "visual_compact_300") || key.startsWith("day_") || key.startsWith("health_") || key.startsWith("rest_") || key.startsWith("weekly_focus_"))) continue
                when (val value = values.get(key)) {
                    is String -> edit.putString(key, value.take(1000))
                    is Boolean -> edit.putBoolean(key, value)
                    is Int -> edit.putInt(key, value)
                    is Long -> edit.putLong(key, value)
                    is Double -> edit.putFloat(key, value.toFloat())
                }
            }
            edit.apply()
        }.onSuccess { status = "Данные импортированы. Перезапусти приложение, чтобы обновить экран." }
            .onFailure { status = "Ошибка импорта: ${it.localizedMessage}" }
    }
    Spacer(Modifier.height(16.dp))
    Text("Локальная резервная копия", color = Color.White, fontSize = 17.sp)
    Text(status, color = TextSoft, fontSize = 12.sp)
    Spacer(Modifier.height(10.dp))
    Button(onClick = { export.launch("bioage-backup-${LocalDate.now()}.json") }, modifier = Modifier.fillMaxWidth()) {
        Text("Экспортировать данные")
    }
    OutlinedButton(onClick = { import.launch(arrayOf("application/json", "text/plain")) }, modifier = Modifier.fillMaxWidth()) {
        Text("Импортировать данные")
    }
    Text("Импорт заменяет совпадающие записи. Перед импортом сохрани копию текущих данных.", color = TextSoft, fontSize = 11.sp)
}

fun chironV12(sleep: String, steps: String, mood: String, habitsDone: Int): String {
    val hours = sleep.replace(',', '.').toDoubleOrNull()
    val walking = steps.toIntOrNull()
    return when {
        hours != null && (hours < 0 || hours > 24) -> "Проверь запись сна: количество часов должно быть от 0 до 24."
        hours != null && hours < 6 -> "Сон был коротким. Сегодня можно снизить нагрузку и оставить больше времени на восстановление."
        mood.isNotBlank() -> "Ты отметил самочувствие: «${mood.take(65)}». Сравни его с качеством сна за несколько дней."
        habitsDone == 3 -> "Все три привычки отмечены. Регулярность важнее рекордов — отдых тоже часть прогресса."
        hours != null && hours >= 7 -> "Сон записан: $sleep ч. Оценивай самочувствие по тенденции, а не по одному дню."
        walking != null && walking > 0 -> "Сегодня записано $walking шагов. Поддерживай комфортный для себя темп."
        else -> "Запиши сон, шаги или самочувствие. Хирон поможет замечать изменения со временем."
    }
}
