package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

private val Accent30 = Color(0xFFB5F0E7)
private val Text30 = Color(0xFFE8F5F7)
private val Dim30 = Color(0xFFB7CDD5)

private fun sleepValue(prefs: SharedPreferences, date: LocalDate): Double? =
    prefs.getString("health_${date}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..24.0 }
private fun stepsValue(prefs: SharedPreferences, date: LocalDate): Int? =
    prefs.getString("health_${date}_steps", null)?.toIntOrNull()?.takeIf { it in 0..1000000 }
private fun habitCount(prefs: SharedPreferences, date: LocalDate): Int =
    (0..2).count { prefs.getBoolean("day_${date}_habit_$it", false) }

private data class Summary30(val days: Int, val avgSleep: Double?, val avgSteps: Double?, val completed: Int, val restDays: Int)
private fun summary(prefs: SharedPreferences, days: Int): Summary30 {
    val dates = (0 until days).map { LocalDate.now().minusDays(it.toLong()) }
    val sleep = dates.mapNotNull { sleepValue(prefs, it) }
    val steps = dates.mapNotNull { stepsValue(prefs, it) }
    val completed = dates.sumOf { habitCount(prefs, it) }
    val restDays = dates.count { prefs.getBoolean("rest_${it}", false) }
    return Summary30(days, sleep.takeIf { it.isNotEmpty() }?.average(), steps.takeIf { it.isNotEmpty() }?.average(), completed, restDays)
}

/** Alpha 0.18: progress is a motivational metaphor, never real altitude or fitness estimate. */
@Composable
fun SummitJourney30(prefs: SharedPreferences, refresh: Int) {
    val completed = remember(refresh) {
        (0L..89L).sumOf { habitCount(prefs, LocalDate.now().minusDays(it)) }
    }
    val checkpoints = listOf(0, 30, 90, 180, 270)
    val fraction = (completed / 270f).coerceIn(0f, 1f)
    Spacer(Modifier.height(16.dp))
    Text("Виртуальное восхождение", color = Text30, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Text("$completed из 270 отметок за последние 90 дней", color = Dim30, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))
    LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(10.dp), color = Accent30)
    Spacer(Modifier.height(8.dp))
    Text("Этап: ${checkpoints.indexOfLast { completed >= it } + 1} из 5. Это игровой прогресс, не настоящая высота.", color = Dim30, fontSize = 12.sp)
    var rest by remember { mutableStateOf(prefs.getBoolean("rest_${LocalDate.now()}", false)) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = rest, onCheckedChange = { rest = it; prefs.edit().putBoolean("rest_${LocalDate.now()}", it).apply() })
        Text("Сегодня день отдыха", color = Text30)
    }
    Text("Отдых не обнуляет прогресс и не считается неудачей.", color = Dim30, fontSize = 12.sp)
}

/** Alpha 0.21: 7/30/90-day recorded values; missing days are not zero. */
@Composable
fun Analytics30(prefs: SharedPreferences, refresh: Int) {
    var range by remember { mutableIntStateOf(30) }
    val records = remember(range, refresh) {
        (range - 1 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
    }
    val values = records.map { stepsValue(prefs, it) }
    val s = summary(prefs, range)
    Spacer(Modifier.height(16.dp))
    Text("Аналитика здоровья", color = Text30, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(7, 30, 90).forEach { d ->
            FilterChip(selected = range == d, onClick = { range = d }, label = { Text("$d дней") })
        }
    }
    Text("Средние шаги: ${s.avgSteps?.roundToInt()?.toString() ?: "нет данных"}", color = Text30)
    Text("Средний сон: ${s.avgSleep?.let { String.format(Locale.getDefault(), "%.1f ч", it) } ?: "нет данных"}", color = Text30)
    val max = (values.filterNotNull().maxOrNull() ?: 1).coerceAtLeast(1)
    if (values.all { it == null }) {
        Text("Пока нет записей за выбранный период. Добавь данные в разделе «Здоровье».", color = Dim30, fontSize = 12.sp)
    } else Canvas(Modifier.fillMaxWidth().height(96.dp)) {
        val gap = size.width / records.size
        values.forEachIndexed { i, value ->
            if (value != null) {
                val x = gap * (i + .5f)
                val y = size.height * (1f - value.toFloat() / max)
                drawLine(Accent30, Offset(x, size.height), Offset(x, y), strokeWidth = (gap * .6f).coerceAtLeast(1f), cap = StrokeCap.Round)
            }
        }
    }
    if (values.any { it != null }) Text("Каждый столбец — день с записью. Пустые дни не заменяются нулями.", color = Dim30, fontSize = 11.sp)
}

/** Alpha 0.22: transparent wellness signal, deliberately not a clinical score. */
@Composable
fun Recovery30(prefs: SharedPreferences, refresh: Int) {
    val recent = remember(refresh) { (0L..6L).mapNotNull { sleepValue(prefs, LocalDate.now().minusDays(it)) } }
    Spacer(Modifier.height(16.dp))
    Text("Ориентир восстановления", color = Text30, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    if (recent.size < 3) {
        Text("Нужно минимум 3 записи сна за 7 дней. Пока оценка недоступна.", color = Dim30)
    } else {
        val avg = recent.average()
        val status = when { avg < 6.0 -> "Обрати внимание на отдых"; avg < 7.0 -> "Следи за самочувствием"; else -> "Сон по записям выглядит достаточным" }
        Text(status, color = Accent30)
        Text("Средний сон ${String.format(Locale.getDefault(), "%.1f", avg)} ч за ${recent.size} записей.", color = Text30)
        Text("Это информационный ориентир по сну, не диагноз и не медицинская оценка восстановления.", color = Dim30, fontSize = 12.sp)
    }
}

/** Alpha 0.24: configurable personal goal, persisted offline. */
@Composable
fun Goals30(prefs: SharedPreferences, steps: String) {
    var target by remember { mutableFloatStateOf(prefs.getInt("goal_steps", 6000).toFloat()) }
    Spacer(Modifier.height(16.dp))
    Text("Личная цель по шагам", color = Text30, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Text("${target.roundToInt()} шагов · можно менять в любой день", color = Dim30)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        OutlinedButton(onClick = {
            target = (target - 500f).coerceAtLeast(1000f)
            prefs.edit().putInt("goal_steps", target.roundToInt()).apply()
        }) { Text("−500") }
        Text("${target.roundToInt()}", color = Accent30, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = {
            target = (target + 500f).coerceAtMost(20000f)
            prefs.edit().putInt("goal_steps", target.roundToInt()).apply()
        }) { Text("+500") }
    }
    val actual = steps.toIntOrNull()
    if (actual != null) {
        LinearProgressIndicator(progress = { (actual / target).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
        Text("Сегодня: $actual из ${target.roundToInt()}", color = Text30)
    }
}

/** Alpha 0.25: readable, offline monthly report. */
@Composable
fun Reports30(prefs: SharedPreferences, refresh: Int) {
    val s = remember(refresh) { summary(prefs, 30) }
    Spacer(Modifier.height(16.dp))
    Text("Отчёт за 30 дней", color = Text30, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Text("Отмечено привычек: ${s.completed} из 90", color = Text30)
    Text("Дней отдыха: ${s.restDays}", color = Text30)
    Text("Шаги в записанные дни: ${s.avgSteps?.roundToInt() ?: "—"}", color = Text30)
    Text("Сон в записанные дни: ${s.avgSleep?.let { String.format(Locale.getDefault(), "%.1f ч", it) } ?: "—"}", color = Text30)
    Text("Результаты основаны только на сохранённых данных. Для экспорта используй резервную копию в профиле.", color = Dim30, fontSize = 12.sp)
}

/** Alpha 0.23/0.28: explicit rule-based offline assistant, no fake remote AI. */
fun chiron30(prefs: SharedPreferences, sleep: String, steps: String, mood: String, done: Int): String {
    val recent = (0L..6L).mapNotNull { sleepValue(prefs, LocalDate.now().minusDays(it)) }
    val rest = prefs.getBoolean("rest_${LocalDate.now()}", false)
    return when {
        rest -> "Сегодня отмечен отдых. Не нужно компенсировать его чрезмерной нагрузкой."
        sleep.replace(',', '.').toDoubleOrNull()?.let { it !in 0.0..24.0 } == true -> "Проверь число часов сна: допустимы значения от 0 до 24."
        recent.size >= 3 && recent.average() < 6.5 -> "За ${recent.size} записанных дней средний сон ниже 6,5 ч. Подумай, как выделить больше времени для отдыха."
        mood.isNotBlank() -> "Ты записал самочувствие: «${mood.take(55)}». Сопоставь его с записями сна за неделю."
        done == 3 -> "Все три привычки отмечены. Сохраняй комфортный ритм; отдых тоже важен."
        steps.toIntOrNull()?.let { it > 0 } == true -> "Сегодня есть запись шагов. Сравни её с собственной целью, а не с чужими рекордами."
        else -> "Добавь несколько записей сна и активности. Тогда я смогу показать тенденции."
    }
}

@Composable
fun ChironChat30(prefs: SharedPreferences, sleep: String, steps: String, mood: String, done: Int) {
    var question by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("Выбери тему или напиши вопрос. Ответы формируются локальными правилами.") }
    Spacer(Modifier.height(16.dp))
    Text("Диалог с Хироном · офлайн", color = Text30, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Text("Это не генеративный ИИ и не медицинская консультация.", color = Dim30, fontSize = 12.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("Сон", "Шаги", "Привычки").forEach { topic ->
            AssistChip(onClick = {
                question = topic
                response = answerChiron30(topic, prefs, sleep, steps, mood, done)
            }, label = { Text(topic) })
        }
    }
    OutlinedTextField(value = question, onValueChange = { question = it.take(200) }, label = { Text("Спроси Хирона") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
    Button(onClick = { response = answerChiron30(question, prefs, sleep, steps, mood, done) }, enabled = question.isNotBlank()) { Text("Ответить") }
    Text(response, color = Text30, lineHeight = 21.sp)
}

private fun answerChiron30(q: String, prefs: SharedPreferences, sleep: String, steps: String, mood: String, done: Int): String {
    val query = q.lowercase(Locale.getDefault())
    return when {
        "сон" in query || "спал" in query -> {
            val avg = (0L..6L).mapNotNull { sleepValue(prefs, LocalDate.now().minusDays(it)) }
            if (avg.isEmpty()) "За неделю нет сохранённых записей сна. Добавь запись в разделе «Здоровье»."
            else "За ${avg.size} записанных дней средний сон ${String.format(Locale.getDefault(), "%.1f", avg.average())} ч. Это наблюдение, не диагноз."
        }
        "шаг" in query || "ход" in query -> "Сегодня записано ${steps.ifBlank { "—" }} шагов. Личная цель: ${prefs.getInt("goal_steps", 6000)}. Подстраивай её под самочувствие."
        "привыч" in query || "прогресс" in query -> "Сегодня отмечено $done из 3 привычек. В разделе «Прогресс» есть виртуальное восхождение."
        "отдых" in query || "устал" in query -> "Если чувствуешь усталость, можно отметить день отдыха. Не нужно догонять пропущенные цели."
        else -> "Пока я понимаю вопросы о сне, шагах, привычках и отдыхе. ${chiron30(prefs, sleep, steps, mood, done)}"
    }
}

/** Alpha 0.29: locally visible validation/status, without pretending automated tests ran. */
@Composable
fun Diagnostics30(prefs: SharedPreferences) {
    Spacer(Modifier.height(16.dp))
    Text("Проверка данных", color = Text30, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    val count = prefs.all.keys.count { it.startsWith("health_") }
    Text("Локальных полей истории: $count", color = Text30)
    Text("Данные хранятся в SharedPreferences. Резервное копирование — в профиле.", color = Dim30, fontSize = 12.sp)
    Text("Health Connect и биологический возраст не включены. Подключение данных требует отдельного согласия и проверки на устройстве.", color = Dim30, fontSize = 12.sp)
}

/** Compact camera control avoids the device-specific distorted Material slider thumb. */
@Composable
fun OrbitControl30(angle: Float, onAngle: (Float) -> Unit) {
    Text("Поворот камеры · ${angle.roundToInt()}°", color = Dim30, fontSize = 12.sp)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { onAngle((angle - 15f).coerceAtLeast(-45f)) }, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("−15°") }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(-45f, -22.5f, 0f, 22.5f, 45f).forEach { value ->
                Box(Modifier.size(if (kotlin.math.abs(angle - value) < 8f) 14.dp else 10.dp)
                    .background(if (kotlin.math.abs(angle - value) < 8f) Accent30 else Dim30.copy(alpha = .5f), RoundedCornerShape(100))
                    .clickable { onAngle(value) })
            }
        }
        OutlinedButton(onClick = { onAngle((angle + 15f).coerceAtMost(45f)) }, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("+15°") }
    }
}
