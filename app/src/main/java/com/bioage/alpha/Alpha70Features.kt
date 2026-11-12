package com.bioage.alpha

import android.content.SharedPreferences
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

private val Ice70 = Color(0xFFB5F0E7)
private val Quiet70 = Color(0xFFB7CDD5)
private val Text70 = Color(0xFFF1F8FA)

private fun marks70(prefs: SharedPreferences, date: LocalDate): Int =
    (0..2).count { prefs.getBoolean("day_${date}_habit_$it", false) }

private fun sleep70(prefs: SharedPreferences, date: LocalDate): Double? =
    prefs.getString("health_${date}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..24.0 }

private fun steps70(prefs: SharedPreferences, date: LocalDate): Int? =
    prefs.getString("health_${date}_steps", null)?.toIntOrNull()?.takeIf { it in 0..1000000 }

private fun count70(prefs: SharedPreferences, days: Int, metric: (LocalDate) -> Boolean): Int =
    (0 until days).count { metric(LocalDate.now().minusDays(it.toLong())) }

/** 0.51–0.54: explicit recording coverage; no missing-day-as-zero bias. */
@Composable
fun DataCoverage70(prefs: SharedPreferences, refresh: Int) {
    val sleep7 = count70(prefs, 7) { sleep70(prefs, it) != null }
    val steps7 = count70(prefs, 7) { steps70(prefs, it) != null }
    val sleep30 = count70(prefs, 30) { sleep70(prefs, it) != null }
    val steps30 = count70(prefs, 30) { steps70(prefs, it) != null }
    Spacer(Modifier.height(14.dp))
    Text("Полнота дневника", color = Text70, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Text("7 дней · сон $sleep7/7 · шаги $steps7/7", color = Text70, fontSize = 13.sp)
    LinearProgressIndicator(progress = { (sleep7 + steps7) / 14f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Ice70)
    Text("30 дней · сон $sleep30/30 · шаги $steps30/30", color = Quiet70, fontSize = 12.sp)
    Text("Незаполненные дни не считаются нулевыми показателями.", color = Quiet70, fontSize = 11.sp)
}

/** 0.55–0.58: offline, non-medical context using recorded values only. */
@Composable
fun ChironContext70(prefs: SharedPreferences, refresh: Int) {
    val today = LocalDate.now()
    val recentSleep = (0L..6L).mapNotNull { sleep70(prefs, today.minusDays(it)) }
    val previousSleep = (7L..13L).mapNotNull { sleep70(prefs, today.minusDays(it)) }
    val recentSteps = (0L..6L).mapNotNull { steps70(prefs, today.minusDays(it)) }
    var rest by remember { mutableStateOf(prefs.getBoolean("chiron_rest_mode_70", false)) }
    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "chiron_rest_mode_70") rest = prefs.getBoolean(key, false)
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    Spacer(Modifier.height(14.dp))
    Text("Хирон · контекст недели", color = Text70, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    val advice = when {
        rest -> "Выбран спокойный режим. Отмечай самочувствие без цели на рекорды."
        recentSleep.size >= 3 && previousSleep.size >= 3 && recentSleep.average() < previousSleep.average() - 0.5 ->
            "В записанные дни средний сон стал короче более чем на полчаса. Подумай, что мешало отдыху."
        recentSleep.size >= 3 && recentSleep.average() < 6.0 ->
            "Средний сон по ${recentSleep.size} записям меньше 6 часов. При возможности выдели время для отдыха."
        recentSteps.size >= 3 -> "Есть ${recentSteps.size} записей шагов за неделю. Следи за регулярностью, а не только за максимумом."
        else -> "Добавь несколько записей сна и шагов — тогда появятся наблюдения за динамикой."
    }
    Text(advice, color = Ice70, fontSize = 13.sp, lineHeight = 19.sp)
    Text("Правила работают офлайн. Это не диагноз и не генеративная ИИ-модель.", color = Quiet70, fontSize = 11.sp)
}

/** 0.59–0.62: persisted gentle mode, intentionally not altering existing data. */
@Composable
fun GentleMode70(prefs: SharedPreferences) {
    var enabled by remember { mutableStateOf(prefs.getBoolean("chiron_rest_mode_70", false)) }
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text("Спокойный режим", color = Text70, fontWeight = FontWeight.Bold)
            Text("Советы без акцента на достижениях", color = Quiet70, fontSize = 12.sp)
        }
        Switch(checked = enabled, onCheckedChange = { enabled = it; prefs.edit().putBoolean("chiron_rest_mode_70", it).apply() })
    }
}

/** 0.63–0.66: short rolling history, with clear game semantics. */
@Composable
fun TrailRhythm70(prefs: SharedPreferences, refresh: Int) {
    val today = LocalDate.now()
    val counts = (6L downTo 0L).map { marks70(prefs, today.minusDays(it)) }
    val active = counts.count { it > 0 }
    val total = counts.sum()
    Spacer(Modifier.height(14.dp))
    Text("Ритм восхождения", color = Text70, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Text("$active активных дней из 7 · $total отметок из 21", color = Text70, fontSize = 13.sp)
    LinearProgressIndicator(progress = { total / 21f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Ice70)
    Text("Это игровой показатель привычек, не оценка физической формы.", color = Quiet70, fontSize = 11.sp)
}

/** 0.67–0.69: a small, editable weekly focus saved in the existing backup. */
@Composable
fun WeeklyFocus70(prefs: SharedPreferences) {
    val monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
    val key = "weekly_focus_70_$monday"
    var focus by remember(key) { mutableStateOf(prefs.getString(key, "") ?: "") }
    var saved by remember(key) { mutableStateOf(false) }
    Spacer(Modifier.height(14.dp))
    Text("Фокус недели", color = Text70, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    OutlinedTextField(value = focus, onValueChange = { focus = it.take(120); saved = false },
        label = { Text("Одна небольшая цель") }, singleLine = false, maxLines = 3, modifier = Modifier.fillMaxWidth())
    OutlinedButton(onClick = { prefs.edit().putString(key, focus).apply(); saved = true }) { Text("Сохранить фокус") }
    if (saved) Text("Сохранено локально", color = Ice70, fontSize = 12.sp)
}

/** 0.70: visible, verifiable status without claiming device benchmarks. */
@Composable
fun ReleaseNotes70() {
    Spacer(Modifier.height(12.dp))
    Text("Alpha 0.70 · локальные функции", color = Text70, fontWeight = FontWeight.Bold)
    Text("Полнота записей, контекст Хирона, спокойный режим, ритм восхождения и фокус недели. 3D-движок и данные Alpha 0.50 сохранены.",
        color = Quiet70, fontSize = 12.sp, lineHeight = 18.sp)
    Text("GPU-производительность и работа на устройстве требуют отдельной проверки.", color = Quiet70, fontSize = 11.sp)
}
