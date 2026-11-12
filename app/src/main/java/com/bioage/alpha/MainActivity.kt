package com.bioage.alpha

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.*

private val Ink = Color(0xFFF1F8FA)
private val Muted = Color(0xFFB7CDD5)
private val Aqua = Color(0xFFB5F0E7)
private val Deep = Color(0xFF091D2C)
private val Glass = Color(0xDF102D40)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(9, 29, 44))
        )
        val prefs = getSharedPreferences("bioage_local", Context.MODE_PRIVATE)
        migrateFromAlpha01(prefs)
        setContent { BioAge(prefs) }
    }
}

/**
 * Alpha 0.1 stored habit checkmarks without dates ("habit_0".."habit_2").
 * Alpha 0.2 keys them per day. Carry the old marks over to today once, so
 * nothing the user ticked disappears after the update. Steps, sleep and mood
 * keep the same keys and need no migration.
 */
private fun migrateFromAlpha01(prefs: SharedPreferences) {
    if (prefs.getBoolean("migrated_v2", false)) return
    val today = LocalDate.now().toString()
    val edit = prefs.edit()
    for (i in 0..2) {
        val old = "habit_$i"
        if (prefs.contains(old)) {
            val newKey = "day_${today}_habit_$i"
            if (!prefs.contains(newKey)) edit.putBoolean(newKey, prefs.getBoolean(old, false))
            edit.remove(old)
        }
    }
    edit.putBoolean("migrated_v2", true).apply()
}

@Composable
private fun BioAge(prefs: SharedPreferences) {
    var tab by remember { mutableIntStateOf(0) }
    var chatOpen by remember { mutableStateOf(false) }
    var healthExpanded by remember { mutableStateOf(false) }
    var steps by remember { mutableStateOf(prefs.getString("steps", "") ?: "") }
    var sleep by remember { mutableStateOf(prefs.getString("sleep", "") ?: "") }
    var mood by remember { mutableStateOf(prefs.getString("mood", "") ?: "") }
    var saved by remember { mutableStateOf(false) }
    var historyRefresh by remember { mutableIntStateOf(0) }
    var camera by remember { mutableFloatStateOf(0f) }
    val today = remember { LocalDate.now().toString() }
    val habitNames = listOf("Прогулка", "Восстановление", "Самочувствие")
    var habits by remember { mutableStateOf((0..2).map { prefs.getBoolean("day_${today}_habit_$it", false) }) }
    val done = habits.count { it }
    val colors = darkColorScheme(primary = Aqua, onPrimary = Deep, background = Deep, surface = Deep, onSurface = Ink, onSurfaceVariant = Muted, outline = Muted)
    BackHandler(enabled = chatOpen || tab != 0) { if (chatOpen) chatOpen = false else tab = 0 }
    MaterialTheme(colorScheme = colors) {
        Box(Modifier.fillMaxSize().background(Deep)) {
            Caucasus3DScene(camera)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x65051728), Color.Transparent, Color(0xC9081927)), startY = 0f)))
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp)) {
                    Text("BIO—AGE", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink, letterSpacing = 2.sp)
                    Text("КАВКАЗ  /  ALPHA 0.50", fontSize = 11.sp, color = Aqua, letterSpacing = 1.5.sp)
                }
                // Card area takes the space between header and tab bar; the card sits at its bottom
                // and scrolls inside it, so the tab bar is never pushed off screen.
                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.BottomCenter) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                    .then(if (tab == 0 && !chatOpen) Modifier.heightIn(max = maxHeight * 0.59f) else Modifier)
                    .clip(RoundedCornerShape(28.dp)).background(Glass)
                    .verticalScroll(rememberScrollState()).padding(20.dp)) {
                    when (tab) {
                        0 -> {
                            if (chatOpen) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { chatOpen = false }) { Text("← Назад") }
                                    Text("Хирон · диалог", color = Ink, fontSize = 20.sp)
                                }
                                ChironChat30(prefs, sleep, steps, mood, done)
                            } else {
                            Text("Твой путь к вершине", fontSize = 23.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            Spacer(Modifier.height(6.dp))
                            Text("Каждый день — небольшой шаг вперёд", color = Muted, fontSize = 13.sp)
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Metric("ШАГИ", steps.ifBlank { "—" }, Modifier.weight(1f))
                                Metric("СОН", if(sleep.isBlank()) "—" else "$sleep ч", Modifier.weight(1f))
                                Metric("ПРИВЫЧКИ", "$done/3", Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(14.dp))
                            OrbitControl30(camera) { camera = it }
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(Aqua))
                                Spacer(Modifier.width(9.dp))
                                Text("ХИРОН", fontWeight = FontWeight.Bold, color = Aqua, letterSpacing = 1.sp)
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(chiron30(prefs, sleep, steps, mood, done), color = Ink, fontSize = 14.sp, lineHeight = 21.sp)
                            HomeSummit50(prefs, done + historyRefresh)
                            SceneInfo50()
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(onClick = { chatOpen = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Открыть диалог с Хироном")
                            }
                            }
                        }
                        1 -> {
                            Text("Здоровье", fontSize = 23.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            Text("Ручная запись · только на устройстве", color = Muted, fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(steps, { steps = it.filter(Char::isDigit).take(7); saved = false }, label = { Text("Шаги за день") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(sleep, { sleep = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(5); saved = false }, label = { Text("Сон, часов") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(mood, { mood = it.take(100); saved = false }, label = { Text("Как ты себя чувствуешь?") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(14.dp))
                            Button(onClick = { prefs.edit().putString("steps", steps).putString("sleep", sleep).putString("mood", mood).apply(); saveHealthHistory(prefs, steps, sleep, mood); historyRefresh++; saved = true }, modifier = Modifier.fillMaxWidth()) { Text("Сохранить") }
                            if (saved) Text("Данные сохранены", color = Aqua, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Показатели не являются медицинской оценкой.", color = Muted, fontSize = 12.sp)
                            HealthSummary50(prefs, historyRefresh)
                            Recovery30(prefs, historyRefresh)
                            Goals30(prefs, steps)
                            ChironInsights50(prefs, historyRefresh)
                            WeeklyReflection50(prefs)
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(onClick = { healthExpanded = !healthExpanded }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (healthExpanded) "Скрыть историю и графики" else "Показать историю и графики")
                            }
                            if (healthExpanded) {
                                HealthHistoryPanel(prefs, historyRefresh)
                                HealthTrendsPanel(prefs, historyRefresh)
                                Analytics30(prefs, historyRefresh)
                            }
                        }
                        2 -> {
                            Text("Твой прогресс", fontSize = 23.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            Text("Отметки за сегодня · $today", color = Muted, fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))
                            habitNames.forEachIndexed { i, name ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    val next = !habits[i]; habits = habits.toMutableList().also { it[i] = next }; prefs.edit().putBoolean("day_${today}_habit_$i", next).apply()
                                }) {
                                    Checkbox(checked = habits[i], onCheckedChange = { next -> habits = habits.toMutableList().also { it[i] = next }; prefs.edit().putBoolean("day_${today}_habit_$i", next).apply() })
                                    Text(name, color = Ink, fontSize = 15.sp)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Сегодня выполнено: $done из 3", color = Ink, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { done / 3f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)))
                            Spacer(Modifier.height(18.dp))
                            Text("Последние 7 дней", color = Ink, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                                (6 downTo 0).forEach { back ->
                                    val day = LocalDate.now().minusDays(back.toLong()).toString()
                                    val count = if (back == 0) done else (0..2).count { prefs.getBoolean("day_${day}_habit_$it", false) }
                                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(8.dp)).background(if (count == 3) Aqua else if(count > 0) Color(0xFF527E87) else Color(0xFF244355)))
                                        Spacer(Modifier.height(5.dp))
                                        Text(LocalDate.now().minusDays(back.toLong()).format(DateTimeFormatter.ofPattern("dd")), fontSize = 11.sp, color = Muted)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            SummitJourney30(prefs, done + historyRefresh)
                            SummitMilestones50(prefs, done + historyRefresh)
                            Reports30(prefs, done + historyRefresh)
                        }
                        else -> {
                            Text("Профиль", fontSize = 23.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            Spacer(Modifier.height(10.dp))
                            Text("Bio-Age Alpha 0.50", color = Aqua, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("Офлайн-прототип. Записи хранятся локально на этом телефоне.", color = Ink, lineHeight = 21.sp)
                            LocalBackupPanel(prefs)
                            Diagnostics30(prefs)
                            DataAudit50(prefs)
                            Spacer(Modifier.height(12.dp))
                            Text("3D-Кавказ работает локально. Health Connect и медицинский расчёт биологического возраста пока не подключены.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
                        }
                    }
                }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth().background(Color(0xF5091D2C)).padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Главная", "Здоровье", "Прогресс", "Профиль").forEachIndexed { i, title ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { tab = i; chatOpen = false }.padding(horizontal = 4.dp, vertical = 6.dp)) {
                            Text(title, color = if(tab == i) Aqua else Muted, fontWeight = if(tab == i) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.width(19.dp).height(2.dp).background(if(tab == i) Aqua else Color.Transparent))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xAA23485A)).padding(10.dp)) {
        Text(label, color = Muted, fontSize = 9.sp, letterSpacing = .6.sp)
        Spacer(Modifier.height(5.dp))
        Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1)
    }
}

private fun chiron(sleep: String, steps: String): String {
    val hours = sleep.replace(',', '.').toDoubleOrNull()
    val walking = steps.toIntOrNull()
    return when {
        hours != null && hours < 6 -> "Сон был коротким. Сегодня разумно оставить время на отдых и восстановление."
        hours != null && hours >= 7 -> "Сон записан: $sleep ч. Смотри на самочувствие в динамике, а не по одному дню."
        walking != null && walking > 0 -> "Сегодня записано $walking шагов. Двигайся в комфортном для себя темпе."
        else -> "Начни с небольшой записи о сне или самочувствии. Даже один шаг — начало пути."
    }
}

@Composable
private fun AlpineScene(camera: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF102B43), Color(0xFF537E91), Color(0xFFA2B7B8), Color(0xFF25475A))))
        val sun = Offset(w * (.73f + camera / 800f), h * .23f)
        drawCircle(brush = Brush.radialGradient(listOf(Color(0x55FFF4D9), Color.Transparent), center = sun, radius = w * .38f), radius = w * .38f, center = sun)
        drawCircle(color = Color(0xFFDFE6DD), radius = w * .075f, center = sun)
        // Layered mountain silhouettes: lightweight 2D scene, deliberately not represented as 3D.
        mountains(w, h, camera, .43f, Color(0xFF9DAEB6), 0)
        mountains(w, h, camera, .54f, Color(0xFF728C9B), 1)
        mountains(w, h, camera, .68f, Color(0xFF436679), 2)
        mountains(w, h, camera, .81f, Color(0xFF24495D), 3)
        drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color(0xA30A2537)), startY = h * .58f, endY = h))
    }
}

private fun DrawScope.mountains(w: Float, h: Float, shift: Float, base: Float, color: Color, layer: Int) {
    val points = 14
    val path = Path().apply { moveTo(0f, h) }
    val ridge = mutableListOf<Offset>()
    for(i in 0..points) {
        val x = i * w / points
        val t = i + shift * (0.015f + layer * .008f)
        val spikes = abs(sin(t * 1.27 + layer * 2.0)).pow(3.0).toFloat()
        val secondary = abs(sin(t * 2.8 + layer)).toFloat()
        val y = h * (base - spikes * (.17f - layer * .016f) - secondary * .04f)
        ridge.add(Offset(x, y)); path.lineTo(x, y)
    }
    path.lineTo(w,h); path.close()
    drawPath(path, color)
    if(layer <= 1) {
        ridge.windowed(3, 2).forEach { segment ->
            val peak = segment[1]
            if(peak.y < h * (base - .10f)) {
                val cap = Path().apply { moveTo(peak.x, peak.y); lineTo(peak.x - w * .026f, peak.y + h * .035f); lineTo(peak.x, peak.y + h * .021f); lineTo(peak.x + w * .025f, peak.y + h * .04f); close() }
                drawPath(cap, Color(0xFFDDE8E9).copy(alpha = .85f))
            }
        }
    }
}
