package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val ice200 = Color(0xFFB5F0E7)
private val muted200 = Color(0xFFB7CDD5)
private val ink200 = Color(0xFFF1F8FA)
private val tile200 = Color(0xAA23485A)

private fun sleep200(p: SharedPreferences, day: LocalDate): Double? =
    p.getString("health_${day}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..24.0 }
private fun steps200(p: SharedPreferences, day: LocalDate): Int? =
    p.getString("health_${day}_steps", null)?.toIntOrNull()?.takeIf { it in 0..1000000 }
private fun marks200(p: SharedPreferences, day: LocalDate): Int =
    (0..2).count { p.getBoolean("day_${day}_habit_$it", false) }
private fun days200(count: Int) = (0 until count).map { LocalDate.now().minusDays(it.toLong()) }

/** 1.01–1.20: real seven-day visualization, with gaps for unrecorded days. */
@Composable
fun SevenDayChart200(prefs: SharedPreferences, refresh: Int) {
    var metric by remember { mutableStateOf(0) }
    val dates = days200(7).reversed()
    val values = dates.map { if (metric == 0) sleep200(prefs, it) else steps200(prefs, it)?.toDouble() }
    val ceiling = if (metric == 0) 12.0 else maxOf(10000.0, (values.filterNotNull().maxOrNull() ?: 0.0))
    Spacer(Modifier.height(14.dp))
    Text("История за 7 дней", color = ink200, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = metric == 0, onClick = { metric = 0 }, label = { Text("Сон") })
        FilterChip(selected = metric == 1, onClick = { metric = 1 }, label = { Text("Шаги") })
    }
    Row(Modifier.fillMaxWidth().height(108.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { index, value ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box(Modifier.fillMaxWidth().height(76.dp), contentAlignment = Alignment.BottomCenter) {
                    if (value != null) {
                        Box(Modifier.fillMaxWidth(0.65f).fillMaxHeight((value / ceiling).toFloat().coerceIn(0.02f, 1f))
                            .background(ice200, RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)))
                    } else {
                        Text("·", color = muted200, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(dates[index].format(DateTimeFormatter.ofPattern("dd")), color = muted200, fontSize = 10.sp)
            }
        }
    }
    Text("Пустой столбец означает отсутствие записи, а не ноль.", color = muted200, fontSize = 11.sp)
}

/** 1.21–1.40: interpretable week-to-week comparisons with sample counts. */
@Composable
fun CompareWeeks200(prefs: SharedPreferences, refresh: Int) {
    val all = days200(14)
    val currentSleep = all.take(7).mapNotNull { sleep200(prefs, it) }
    val priorSleep = all.drop(7).mapNotNull { sleep200(prefs, it) }
    val currentSteps = all.take(7).mapNotNull { steps200(prefs, it) }
    val priorSteps = all.drop(7).mapNotNull { steps200(prefs, it) }
    Spacer(Modifier.height(14.dp))
    Text("Две недели · сравнение", color = ink200, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    fun comparison(a: List<Double>, b: List<Double>, suffix: String): String {
        if (a.size < 3 || b.size < 3) return "Нужно минимум 3 записи в каждой неделе"
        val delta = a.average() - b.average()
        val prefix = if (delta > 0) "+" else ""
        return String.format(Locale.getDefault(), "%s%.1f %s к предыдущей неделе", prefix, delta, suffix)
    }
    Text("Сон: ${comparison(currentSleep, priorSleep, "ч")}", color = ink200, fontSize = 13.sp)
    Text("Шаги: ${comparison(currentSteps.map(Int::toDouble), priorSteps.map(Int::toDouble), "шагов")}", color = ink200, fontSize = 13.sp)
    Text("Сон ${currentSleep.size}/${priorSleep.size} дней · шаги ${currentSteps.size}/${priorSteps.size} дней (сейчас/ранее)", color = muted200, fontSize = 11.sp)
}

/** 1.41–1.60: transparent non-medical offline coach and optional rest-day focus. */
@Composable
fun ChironCoach200(prefs: SharedPreferences, refresh: Int) {
    var restFocus by remember { mutableStateOf(prefs.getBoolean("chiron_rest_focus_200", false)) }
    val sleep = days200(7).mapNotNull { sleep200(prefs, it) }
    val steps = days200(7).mapNotNull { steps200(prefs, it) }
    val recorded = days200(7).count { sleep200(prefs, it) != null || steps200(prefs, it) != null }
    val advice = when {
        restFocus -> "Режим восстановления: отмечай сон и самочувствие, без давления на количество шагов."
        sleep.size >= 3 && sleep.average() < 6.0 -> "По ${sleep.size} записям средний сон короче 6 часов. При возможности выдели время на отдых."
        recorded < 3 -> "Для персонального наблюдения добавь записи хотя бы за три разных дня."
        steps.size >= 3 -> "Среднее по ${steps.size} записанным дням: ${steps.average().roundToInt()} шагов. Оценивай комфортный для себя ритм."
        else -> "Продолжай вести дневник. По отдельным дням нельзя уверенно судить о тенденциях."
    }
    Spacer(Modifier.height(14.dp))
    Text("Хирон · наблюдение недели", color = ink200, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Text(advice, color = ice200, fontSize = 13.sp, lineHeight = 20.sp)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Приоритет восстановления", color = muted200, modifier = Modifier.weight(1f), fontSize = 12.sp)
        Switch(checked = restFocus, onCheckedChange = { restFocus = it; prefs.edit().putBoolean("chiron_rest_focus_200", it).apply() })
    }
    Text("Основание: $recorded дней с записями. Офлайн-правила, не диагноз и не ИИ-модель.", color = muted200, fontSize = 11.sp)
}

/** 1.61–1.80: no duplicated summit bars, clear daily streak and next checkpoint. */
@Composable
fun SummitRoute200(prefs: SharedPreferences, refresh: Int) {
    val total = days200(90).sumOf { marks200(prefs, it) }
    val milestones = listOf(30 to "Первый привал", 90 to "Горная тропа", 180 to "Высокий лагерь", 270 to "Вершина")
    val next = milestones.firstOrNull { total < it.first }
    val streak = days200(90).takeWhile { marks200(prefs, it) > 0 }.size
    Spacer(Modifier.height(14.dp))
    Text("Восхождение · маршрут", color = ink200, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Text("$total/270 отметок · $streak дн. подряд с привычками", color = muted200, fontSize = 12.sp)
    Spacer(Modifier.height(6.dp))
    LinearProgressIndicator(progress = { (total / 270f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(7.dp), color = ice200)
    Text(if (next == null) "Вершина достигнута" else "Следующая точка: ${next.second}, осталось ${next.first - total}", color = ice200, fontSize = 12.sp)
    Text("Игровой прогресс за 90 дней. Пропуск дня не означает неудачу.", color = muted200, fontSize = 11.sp)
}

/** 1.81–2.00: preference, data hygiene, non-destructive local audit. */
@Composable
fun PrivacyAndQuality200(prefs: SharedPreferences) {
    var expanded by remember { mutableStateOf(false) }
    val dates = days200(30)
    val withSleep = dates.count { sleep200(prefs, it) != null }
    val withSteps = dates.count { steps200(prefs, it) != null }
    val rawInvalid = dates.count { day ->
        val s = prefs.getString("health_${day}_sleep", null)
        s != null && s.isNotBlank() && sleep200(prefs, day) == null
    }
    Spacer(Modifier.height(14.dp))
    Text("Данные и надёжность · Alpha 2.00", color = ink200, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Text("За 30 дней: сон $withSleep/30, шаги $withSteps/30", color = muted200, fontSize = 12.sp)
    OutlinedButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Скрыть проверку" else "Проверить данные") }
    if (expanded) {
        Text("Некорректных записей сна: $rawInvalid", color = if (rawInvalid == 0) ice200 else ink200, fontSize = 12.sp)
        Text("Все данные локальны. Для переноса используй резервную копию в профиле. Эта проверка ничего не удаляет.", color = muted200, fontSize = 12.sp)
    }
}
