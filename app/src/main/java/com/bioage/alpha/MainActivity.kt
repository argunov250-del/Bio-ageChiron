package com.bioage.alpha

import android.app.Activity
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.draw.clip
import kotlin.math.*

private val Night = Color(0xFF0A1C2B)
private val Glass = Color(0xAA19364A)
private val Snow = Color(0xFFF0F7FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = getSharedPreferences("bioage_local", Context.MODE_PRIVATE)
        setContent { BioAgeApp(store) }
    }
}

@Composable
private fun BioAgeApp(store: android.content.SharedPreferences) {
    var page by remember { mutableStateOf(0) }
    var steps by remember { mutableStateOf(store.getString("steps", "") ?: "") }
    var sleep by remember { mutableStateOf(store.getString("sleep", "") ?: "") }
    var mood by remember { mutableStateOf(store.getString("mood", "") ?: "") }
    var saved by remember { mutableStateOf(false) }
    var habits by remember { mutableStateOf((0..2).map { store.getBoolean("habit_$it", false) }) }
    var camera by remember { mutableFloatStateOf(0f) }
    val tabs = listOf("Главная", "Здоровье", "Прогресс", "Профиль")
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFBDE6E7), surface = Night, onSurface = Snow)) {
        Box(Modifier.fillMaxSize().background(Night)) {
            MountainWorld(camera)
            Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x18000000), Color(0x99081725))))) {
                Spacer(Modifier.height(34.dp))
                Text("BIO—AGE  ·  ALPHA 0.1", Modifier.padding(horizontal = 22.dp), color = Snow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Эльбрус • Кавказ • офлайн", Modifier.padding(horizontal = 22.dp, vertical = 4.dp), color = Snow.copy(alpha = .8f), fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Column(Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(25.dp)).background(Glass).verticalScroll(rememberScrollState()).padding(16.dp)) {
                    when(page) {
                        0 -> {
                            Text("Твой путь начинается здесь", fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text("Живой Кавказ — первая облегчённая сцена. 3D SceneView + Filament подключим следующим этапом.", fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Камера: ${camera.roundToInt()}°")
                            Slider(value = camera, onValueChange = { camera = it }, valueRange = -45f..45f)
                            Text("Хирон", fontWeight = FontWeight.Bold, color = Color(0xFFBDE6E7))
                            Text(chironMessage(sleep, steps), fontSize = 13.sp)
                        }
                        1 -> {
                            Text("Показатели здоровья", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(steps, { steps = it.filter(Char::isDigit).take(7); saved = false }, label = { Text("Шаги за день") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(sleep, { sleep = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(5); saved = false }, label = { Text("Сон, часов") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(mood, { mood = it.take(80); saved = false }, label = { Text("Самочувствие (необязательно)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = { store.edit().putString("steps", steps).putString("sleep", sleep).putString("mood", mood).apply(); saved = true }) { Text("Сохранить локально") }
                            if (saved) Text("Сохранено на устройстве", fontSize = 12.sp)
                            Text("Данные введены вручную. Медицинские выводы не выполняются.", fontSize = 12.sp)
                        }
                        2 -> {
                            Text("Привычки и прогресс", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                            val names = listOf("Небольшая прогулка", "Отдых и восстановление", "Отметить самочувствие")
                            names.forEachIndexed { i, name ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = habits[i], onCheckedChange = { value -> habits = habits.toMutableList().also { it[i] = value }; store.edit().putBoolean("habit_$i", value).apply() })
                                    Text(name, fontSize = 14.sp)
                                }
                            }
                            val done = habits.count { it }
                            Text("Выполнено: $done из 3", fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(progress = { done / 3f }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Text("Исторические серии и горные награды появятся позже. Пока отметки сохраняются без дат.", fontSize = 12.sp)
                        }
                        else -> {
                            Text("Профиль и настройки", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                            Text("Bio-Age Alpha 0.1 • локальный прототип", fontSize = 14.sp)
                            Spacer(Modifier.height(10.dp))
                            Text("Интернет не требуется. Данные остаются на этом устройстве.", fontSize = 13.sp)
                            Text("Health Connect, резервное копирование, биологический возраст и облачная синхронизация ещё не реализованы.", fontSize = 13.sp)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().background(Color(0xEE0A1C2B)).padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    tabs.forEachIndexed { i, title -> Text(title, Modifier.clickable { page = i }.padding(horizontal = 5.dp, vertical = 9.dp), fontSize = 12.sp, fontWeight = if(page == i) FontWeight.Bold else FontWeight.Normal, color = if(page == i) Color(0xFFBDE6E7) else Snow.copy(alpha = .65f)) }
                }
            }
        }
    }
}

private fun chironMessage(sleep: String, steps: String): String {
    val hours = sleep.replace(',', '.').toDoubleOrNull()
    return when {
        hours != null && hours < 6 -> "Вы указали менее 6 часов сна. Если это типично для вахты, учитывайте возможность дополнительного отдыха. Это наблюдение, не диагноз."
        hours != null -> "Сон записан: $sleep ч. Следите за изменениями в динамике; по одному значению нельзя судить о восстановлении."
        steps.isNotBlank() -> "Шаги записаны. Для оценки нагрузки нужны контекст, самочувствие и история наблюдений."
        else -> "Добро пожаловать. Начните с небольшого наблюдения за сном или активностью. Все рекомендации пока основаны на простых правилах."
    }
}

@Composable
private fun MountainWorld(camera: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFF14304C), Color(0xFF7496AA), Color(0xFFCFD8D8)), endY = h * .8f))
        drawCircle(Color(0x55FFF4D6), radius = w * .17f, center = Offset(w * .76f, h * .20f))
        fun ridge(base: Float, amp: Float, tint: Color, shift: Float, snow: Boolean) {
            val path = Path().apply { moveTo(0f, h); lineTo(0f, base)
                for(i in 0..100) {
                    val x = w * i / 100f
                    val u = (i / 100f) * 6.28f + shift
                    val peak = abs(sin(u * 1.2f)) * amp + abs(cos(u * 2.4f)) * amp * .32f
                    lineTo(x, base - peak)
                }
                lineTo(w, h); close() }
            drawPath(path, tint)
        }
        val offset = camera / 45f
        ridge(h * .65f, h * .24f, Color(0xFF9BAEBA), offset * .35f, true)
        ridge(h * .72f, h * .20f, Color(0xFF5F8192), offset * .5f, true)
        ridge(h * .84f, h * .15f, Color(0xFF254C60), offset * .8f, false)
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA071A27)), startY = h * .45f, endY = h))
    }
}
