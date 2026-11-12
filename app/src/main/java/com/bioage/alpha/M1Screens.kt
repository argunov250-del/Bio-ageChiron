package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/* ------------------------------------------------------------------ HOME */

@Composable
fun M1HomeScreen(
    prefs: SharedPreferences, steps: String, sleep: String, mood: String, done: Int, totalMarks: Int,
    heroHeight: Dp, latest: M1Latest, onChat: () -> Unit, onOpenProgress: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Box(Modifier.fillMaxWidth().height(heroHeight)) {
            Column(Modifier.align(Alignment.TopStart).padding(top = 10.dp)) {
                Text("BIO—AGE", color = M1.Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, style = M1.HeroShadow)
                Text("CHIRON", color = M1.Muted, fontSize = 12.sp, letterSpacing = 3.sp, style = M1.HeroShadow)
            }
            Column(Modifier.align(Alignment.BottomStart).padding(bottom = 14.dp)) {
                Text("Твоя экспедиция\nна Эльбрус", color = M1.Ink, fontSize = 26.sp, lineHeight = 31.sp, fontWeight = FontWeight.SemiBold, style = M1.HeroShadow)
                Spacer(Modifier.height(4.dp))
                Text("Здоровье · Ритм · Долголетие", color = M1.Muted, fontSize = 13.sp, style = M1.HeroShadow)
            }
        }
        M1Dashboard(sleep, latest)
        Spacer(Modifier.height(12.dp))
        M1AscentCard(totalMarks, onOpenProgress)
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth().clickable { onChat() }, padding = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                M1Orb(46.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Хирон", color = M1.Primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(chiron30(prefs, sleep, steps, mood, done), color = M1.Ink, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(8.dp))
                M1Icon(M1Glyph.Chevron, M1.Muted, 20.dp)
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

/** Recovery ring with the four vitals. Only measured values are shown; the rest is an em dash. */
@Composable
private fun M1Dashboard(sleep: String, latest: M1Latest) {
    val hrv = latest.hrv?.let { it.value.roundToInt().toString() }
    val rhr = latest.restingHr?.let { it.value.roundToInt().toString() }
    val sleepValue = sleep.trim().takeIf { it.isNotEmpty() }
    val hrvNote = latest.hrv?.let { m1DayLabel(it.day) }
    val rhrNote = latest.restingHr?.let { m1DayLabel(it.day) }
    val sleepNote = if (sleepValue != null) "сегодня" else null
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cluster = maxWidth >= 360.dp && LocalDensity.current.fontScale <= 1.12f
        if (cluster) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    M1MiniMetric(M1Glyph.Heart, "HRV", hrv, "мс")
                    M1MiniMetric(M1Glyph.Pulse, "Пульс", rhr, "уд/мин")
                }
                M1Ring(null, 134.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Восстановление", color = M1.Muted, fontSize = 12.sp, maxLines = 1)
                        Text("—", color = M1.Ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("Нет данных", color = M1.Muted, fontSize = 12.sp, maxLines = 1)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    M1MiniMetric(M1Glyph.Bolt, "Нагрузка", null, "")
                    M1MiniMetric(M1Glyph.Moon, "Сон", sleepValue, "ч")
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                M1Card(Modifier.fillMaxWidth(), padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        M1Ring(null, 84.dp, strokeWidth = 8.dp) { Text("—", color = M1.Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Восстановление", color = M1.Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Нет данных", color = M1.Muted, fontSize = 13.sp)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    M1Tile(M1Glyph.Heart, "HRV", hrv, "мс", hrvNote, Modifier.weight(1f))
                    M1Tile(M1Glyph.Pulse, "Пульс покоя", rhr, "уд/мин", rhrNote, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    M1Tile(M1Glyph.Bolt, "Нагрузка", null, "", null, Modifier.weight(1f))
                    M1Tile(M1Glyph.Moon, "Сон", sleepValue, "ч", sleepNote, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun M1MiniMetric(glyph: M1Glyph, label: String, value: String?, unit: String) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xC2112A3C))
            .border(1.dp, M1.Line, RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            M1Icon(glyph, M1.Primary, 14.dp)
            Spacer(Modifier.width(5.dp))
            Text(label, color = M1.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(value ?: "—", color = M1.Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(if (value != null) unit else "нет данных", color = M1.Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Trail with five luminous waypoints placed at their real thresholds (0/30/90/180/270 marks). */
@Composable
private fun M1AscentCard(totalMarks: Int, onOpen: () -> Unit) {
    val fraction = (totalMarks / 270f).coerceIn(0f, 1f)
    M1Card(Modifier.fillMaxWidth().clickable { onOpen() }, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            M1Icon(M1Glyph.Mountain, M1.Primary, 20.dp)
            Spacer(Modifier.width(8.dp))
            Text("Восхождение на Эльбрус", color = M1.Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            M1Icon(M1Glyph.Chevron, M1.Muted, 18.dp)
        }
        Spacer(Modifier.height(10.dp))
        Canvas(Modifier.fillMaxWidth().height(26.dp)) {
            val pad = 10.dp.toPx()
            val y = size.height / 2f
            val x0 = pad
            val x1 = size.width - pad
            drawLine(M1.Track, Offset(x0, y), Offset(x1, y), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            if (fraction > 0f) drawLine(M1.Primary, Offset(x0, y), Offset(x0 + (x1 - x0) * fraction, y), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
            M1StageMarks.forEach { mark ->
                val x = x0 + (x1 - x0) * (mark / 270f)
                val reached = totalMarks >= mark
                if (reached) {
                    drawCircle(Brush.radialGradient(listOf(M1.Primary.copy(alpha = .55f), Color.Transparent), center = Offset(x, y), radius = 11.dp.toPx()), radius = 11.dp.toPx(), center = Offset(x, y))
                    drawCircle(M1.Primary, radius = 5.dp.toPx(), center = Offset(x, y))
                } else {
                    drawCircle(M1.Bg, radius = 5.dp.toPx(), center = Offset(x, y))
                    drawCircle(M1.Muted, radius = 5.dp.toPx(), center = Offset(x, y), style = Stroke(1.5.dp.toPx()))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("${(fraction * 100).toInt()}% · $totalMarks из 270 отметок", color = M1.Muted, fontSize = 12.sp)
    }
}

/* ---------------------------------------------------------------- HEALTH */

@Composable
fun M1HealthScreen(
    prefs: SharedPreferences, steps: String, sleep: String, mood: String,
    onSteps: (String) -> Unit, onSleep: (String) -> Unit, onMood: (String) -> Unit,
    saved: Boolean, onSave: () -> Unit, refresh: Int
) {
    var period by remember { mutableIntStateOf(7) }
    var entryOpen by remember { mutableStateOf(steps.isBlank() && sleep.isBlank()) }
    var detailsOpen by remember { mutableStateOf(false) }
    val imported = rememberM1Period(period, refresh)
    val days = remember(period) { (period - 1 downTo 0).map { LocalDate.now().minusDays(it.toLong()) } }
    val sleeps = remember(period, refresh) { days.map { m1Sleep(prefs, it) } }
    val stepList = remember(period, refresh) { days.map { m1Steps(prefs, it)?.toDouble() } }
    val sleepKnown = sleeps.filterNotNull()
    val stepsKnown = stepList.filterNotNull()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Здоровье", color = M1.Ink, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        M1Segmented(listOf(7, 30, 90), period, { period = it })
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth()) {
            M1SectionTitle("Ключевые показатели")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                M1Tile(M1Glyph.Heart, "Средний пульс", imported.heartRate?.value?.roundToInt()?.toString(), "уд/мин",
                    imported.heartRate?.let { "${it.days} дн. с данными" }, Modifier.weight(1f))
                M1Tile(M1Glyph.Pulse, "HRV", imported.hrv?.value?.roundToInt()?.toString(), "мс",
                    imported.hrv?.let { "${it.days} дн. с данными" }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                M1Tile(M1Glyph.Moon, "Сон", if (sleepKnown.isEmpty()) null else String.format(Locale.getDefault(), "%.1f", sleepKnown.average()), "ч",
                    if (sleepKnown.isEmpty()) null else "${sleepKnown.size} дн. с данными", Modifier.weight(1f))
                M1Tile(M1Glyph.Steps, "Шаги", if (stepsKnown.isEmpty()) null else stepsKnown.average().roundToInt().toString(), "в день",
                    if (stepsKnown.isEmpty()) null else "${stepsKnown.size} дн. с данными", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text("Средние только по дням с записями.", color = M1.Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    M1SectionTitle("Восстановление")
                    Text("Нет данных", color = M1.Muted, fontSize = 13.sp)
                }
                Text("—", color = M1.Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text("Оценка появится, когда будут подключены расчёты по HRV, пульсу покоя и сну. Значения не имитируются.", color = M1.Muted, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth()) {
            M1SectionTitle("Сон по дням")
            Spacer(Modifier.height(10.dp))
            M1Bars(sleeps, 12.0)
            Spacer(Modifier.height(14.dp))
            M1SectionTitle("Шаги по дням")
            Spacer(Modifier.height(10.dp))
            M1Bars(stepList, 10000.0)
            Spacer(Modifier.height(8.dp))
            Text("Пустое место — день без записи, а не ноль.", color = M1.Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth(), padding = 8.dp) {
            M1Row(M1Glyph.Box, "Записать показатели", "Шаги, сон, самочувствие, цель", onClick = { entryOpen = !entryOpen }) {
                M1Icon(if (entryOpen) M1Glyph.ChevronDown else M1Glyph.Chevron, M1.Muted, 20.dp)
            }
            if (entryOpen) {
                Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    OutlinedTextField(steps, onSteps, label = { Text("Шаги за день") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(sleep, onSleep, label = { Text("Сон, часов") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(mood, onMood, label = { Text("Как ты себя чувствуешь?") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onSave, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Сохранить") }
                    if (saved) Text("Данные сохранены", color = M1.Primary, fontSize = 12.sp)
                    Goals30(prefs, steps)
                }
            }
            M1Divider()
            M1Row(M1Glyph.Pulse, "Подробная аналитика", "История, сравнение недель, заметки", onClick = { detailsOpen = !detailsOpen }) {
                M1Icon(if (detailsOpen) M1Glyph.ChevronDown else M1Glyph.Chevron, M1.Muted, 20.dp)
            }
            if (detailsOpen) {
                Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    HealthHistoryPanel(prefs, refresh)
                    CompareWeeks200(prefs, refresh)
                    DataCoverage70(prefs, refresh)
                    GentleMode70(prefs)
                    ChironContext70(prefs, refresh)
                    WeeklyReflection50(prefs)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Показатели не являются медицинской оценкой.", color = M1.Muted, fontSize = 12.sp)
        Spacer(Modifier.height(20.dp))
    }
}

/** Bars for recorded days only; missing days stay empty. */
@Composable
private fun M1Bars(values: List<Double?>, minCeiling: Double) {
    if (values.all { it == null }) {
        Text("Нет записей за период", color = M1.Muted, fontSize = 13.sp)
        return
    }
    val ceiling = maxOf(minCeiling, values.filterNotNull().maxOrNull() ?: 0.0)
    Canvas(Modifier.fillMaxWidth().height(64.dp)) {
        val n = values.size
        val slot = size.width / n
        val barWidth = (slot * .62f).coerceAtLeast(1.5f)
        drawLine(M1.Track, Offset(0f, size.height - 1f), Offset(size.width, size.height - 1f), strokeWidth = 1.dp.toPx())
        values.forEachIndexed { index, value ->
            if (value != null) {
                val barHeight = (size.height - 2f) * (value / ceiling).toFloat().coerceIn(.04f, 1f)
                drawRoundRect(M1.Primary, topLeft = Offset(slot * index + (slot - barWidth) / 2f, size.height - 1f - barHeight),
                    size = Size(barWidth, barHeight), cornerRadius = CornerRadius(barWidth / 3f, barWidth / 3f))
            }
        }
    }
}

/* -------------------------------------------------------------- PROGRESS */

@Composable
fun M1ProgressScreen(
    prefs: SharedPreferences, habitNames: List<String>, habits: List<Boolean>, onToggle: (Int) -> Unit,
    done: Int, totalMarks: Int, heroHeight: Dp
) {
    val fraction = (totalMarks / 270f).coerceIn(0f, 1f)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Box(Modifier.fillMaxWidth().height(heroHeight)) {
            Text("Прогресс", color = M1.Ink, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, style = M1.HeroShadow,
                modifier = Modifier.align(Alignment.TopStart).padding(top = 12.dp))
            Column(Modifier.align(Alignment.BottomStart).padding(bottom = 12.dp)) {
                Text("Эльбрус", color = M1.Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = M1.HeroShadow)
                Text("Игровой маршрут по отметкам привычек", color = M1.Muted, fontSize = 12.sp, style = M1.HeroShadow)
            }
        }
        M1Card(Modifier.fillMaxWidth()) {
            Text("Твой путь", color = M1.Muted, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${(fraction * 100).toInt()}%", color = M1.Ink, fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 6.dp)) {
                    Text("$totalMarks / 270", color = M1.Ink, fontSize = 15.sp)
                    Text("отметок за 90 дней", color = M1.Muted, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(M1.Track)) {
                Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(M1.Primary))
            }
        }
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth(), padding = 8.dp) {
            M1StageNames.forEachIndexed { index, name ->
                val from = M1StageMarks[index]
                val to = if (index < 4) M1StageMarks[index + 1] else 270
                val reached = if (index < 4) totalMarks >= to else totalMarks >= 270
                val current = !reached && totalMarks >= from && index < 4
                val glyph = if (reached) M1Glyph.Check else if (current) M1Glyph.Mountain else M1Glyph.Lock
                val range = if (index < 4) "$from–$to отметок" else "270 отметок"
                M1Row(glyph, name, range, enabled = reached || current) {
                    if (current) {
                        val part = ((totalMarks - from) * 100 / (to - from)).coerceIn(0, 100)
                        Text("$part%", color = M1.Primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    } else if (reached) {
                        Text("Пройдено", color = M1.Primary, fontSize = 12.sp)
                    }
                }
                if (index < 4) M1Divider()
            }
        }
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth(), padding = 8.dp) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                M1SectionTitle("Сегодня")
                Text("Выполнено $done из 3", color = M1.Muted, fontSize = 12.sp)
            }
            habitNames.forEachIndexed { index, name ->
                val checked = habits.getOrElse(index) { false }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(14.dp)).clickable { onToggle(index) }.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(if (checked) M1.Primary else Color.Transparent)
                            .border(1.5.dp, if (checked) M1.Primary else M1.Muted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { if (checked) M1Icon(M1Glyph.Check, M1.Bg, 16.dp) }
                    Spacer(Modifier.width(12.dp))
                    Text(name, color = M1.Ink, fontSize = 15.sp)
                }
            }
            Column(Modifier.padding(horizontal = 4.dp)) { RestDayToggle700() }
        }
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth()) {
            M1SectionTitle("Ритм недели")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                (6 downTo 0).forEach { back ->
                    val day = LocalDate.now().minusDays(back.toLong())
                    val count = if (back == 0) done else m1DayMarks(prefs, day)
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(30.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (count == 3) M1.Primary else if (count > 0) Color(0xFF3B8C88) else M1.Track))
                        Spacer(Modifier.height(5.dp))
                        Text(day.format(DateTimeFormatter.ofPattern("dd")), fontSize = 12.sp, color = M1.Muted)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth()) { WeeklyFocus70(prefs) }
        Spacer(Modifier.height(10.dp))
        Text("Отдых не обнуляет путь. Маршрут игровой: это не высота и не оценка формы.", color = M1.Muted, fontSize = 12.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(20.dp))
    }
}

/* --------------------------------------------------------------- PROFILE */

@Composable
fun M1ProfileScreen(prefs: SharedPreferences, scene: M1SceneController) {
    val context = LocalContext.current
    val version = remember { runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "—" }
    var importOpen by remember { mutableStateOf(false) }
    var backupOpen by remember { mutableStateOf(false) }
    var diagnosticsOpen by remember { mutableStateOf(false) }
    var aboutOpen by remember { mutableStateOf(false) }
    var sceneStatus by remember { mutableStateOf("проверяется…") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Профиль", color = M1.Ink, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(68.dp).clip(CircleShape).background(Brush.verticalGradient(listOf(Color(0xFF2D5A7A), Color(0xFF0E2434))))
                    .border(1.dp, M1.Line, CircleShape),
                contentAlignment = Alignment.Center
            ) { M1Icon(M1Glyph.Mountain, M1.Ink, 34.dp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Bio-Age Chiron", color = M1.Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text("Версия $version", color = M1.Muted, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text("Офлайн · данные на устройстве", color = M1.Primary, fontSize = 12.sp,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x2667E6CD)).border(1.dp, Color(0x5567E6CD), RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        M1Card(Modifier.fillMaxWidth(), padding = 8.dp) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) { M1SectionTitle("Данные и синхронизация") }
            M1Row(M1Glyph.Heart, "Apple Health (ZIP)", "Импорт и обновление", onClick = { importOpen = !importOpen }) {
                M1Icon(if (importOpen) M1Glyph.ChevronDown else M1Glyph.Chevron, M1.Muted, 20.dp)
            }
            if (importOpen) Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) { ChironDataCenter(prefs) }
            M1Divider()
            M1Row(M1Glyph.Pulse, "Health Connect", "Пока не подключено", enabled = false)
        }
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth(), padding = 8.dp) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) { M1SectionTitle("Мои показатели") }
            M1Row(M1Glyph.Leaf, "Биологический возраст", "Нет оценки") { Text("—", color = M1.Ink, fontSize = 18.sp) }
            M1Divider()
            M1Row(M1Glyph.Bolt, "Скорость старения", "Нет оценки") { Text("—", color = M1.Ink, fontSize = 18.sp) }
            Text("Оценки появятся только при достаточных данных и применимой научной модели.", color = M1.Muted, fontSize = 12.sp, lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
        }
        Spacer(Modifier.height(12.dp))
        M1Card(Modifier.fillMaxWidth(), padding = 8.dp) {
            M1Row(M1Glyph.Box, "Резервная копия", "Экспорт и импорт данных (JSON)", onClick = { backupOpen = !backupOpen }) {
                M1Icon(if (backupOpen) M1Glyph.ChevronDown else M1Glyph.Chevron, M1.Muted, 20.dp)
            }
            if (backupOpen) Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) { LocalBackupPanel(prefs) }
            M1Divider()
            M1Row(M1Glyph.Pulse, "Диагностика", "Проверка данных и 3D-сцены", onClick = {
                diagnosticsOpen = !diagnosticsOpen
                if (diagnosticsOpen) scene.status { sceneStatus = it }
            }) { M1Icon(if (diagnosticsOpen) M1Glyph.ChevronDown else M1Glyph.Chevron, M1.Muted, 20.dp) }
            if (diagnosticsOpen) {
                Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Text("Горная сцена: $sceneStatus", color = M1.Ink, fontSize = 13.sp)
                    Text("webgl — 3D работает; 2d — показан резервный рисунок гор.", color = M1.Muted, fontSize = 12.sp)
                    Diagnostics30(prefs)
                    DataAudit50(prefs)
                    PrivacyAndQuality200(prefs)
                }
            }
            M1Divider()
            M1Row(M1Glyph.Info, "О приложении", "Версия $version", onClick = { aboutOpen = !aboutOpen }) {
                M1Icon(if (aboutOpen) M1Glyph.ChevronDown else M1Glyph.Chevron, M1.Muted, 20.dp)
            }
            if (aboutOpen) {
                Text("Приложение работает без интернета. Горы — стилизованная процедурная 3D-сцена, а не точная модель Эльбруса. Восстановление, нагрузка и биологический возраст пока не рассчитываются.",
                    color = M1.Muted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

/* ------------------------------------------------------------------ CHAT */

@Composable
fun M1ChatScreen(prefs: SharedPreferences, steps: String, sleep: String, mood: String, done: Int, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("← Назад", color = M1.Primary, fontSize = 15.sp,
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { onBack() }.padding(horizontal = 10.dp, vertical = 14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Хирон", color = M1.Ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        }
        M1Card(Modifier.fillMaxWidth()) { UnifiedChiron(prefs, steps, sleep, mood, done) }
        Spacer(Modifier.height(20.dp))
    }
}

/* ------------------------------------------------------------ NAVIGATION */

@Composable
fun M1NavBar(tab: Int, onTab: (Int) -> Unit) {
    val items = listOf(M1Glyph.Home to "Главная", M1Glyph.Pulse to "Здоровье", M1Glyph.Mountain to "Прогресс", M1Glyph.Person to "Профиль")
    Row(
        Modifier.fillMaxWidth().background(Color(0xF2071826)).border(1.dp, M1.Line, RoundedCornerShape(0.dp)).padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEachIndexed { index, item ->
            val active = tab == index
            Column(
                Modifier.weight(1f).heightIn(min = 52.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (active) Color(0x2667E6CD) else Color.Transparent)
                    .clickable { onTab(index) }.padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                M1Icon(item.first, if (active) M1.Primary else M1.Muted, 22.dp)
                Spacer(Modifier.height(3.dp))
                Text(item.second, color = if (active) M1.Primary else M1.Muted, fontSize = 12.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
            }
        }
    }
}
