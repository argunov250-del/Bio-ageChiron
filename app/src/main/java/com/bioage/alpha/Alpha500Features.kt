package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Locale

private val ice500 = Color(0xFFAEF5E9)
private val ink500 = Color(0xFFF2FAFD)
private val soft500 = Color(0xFFBED2DC)

/** Clear home: compact, single route and one coach action. Existing preferences are unchanged. */
@Composable
fun Home500(prefs: SharedPreferences, steps: String, sleep: String, done: Int, refresh: Int,
            camera: Float, onCamera: (Float) -> Unit, onChat: () -> Unit) {
    Hero300(steps, sleep, done)
    Spacer(Modifier.height(12.dp))
    // Keep the 3D camera control (OrbitControl30) and the Alpha 4.00 route (AlpineRoute400) on Home.
    OrbitControl30(camera, onCamera)
    Spacer(Modifier.height(12.dp))
    AlpineRoute400(prefs, refresh)
    Spacer(Modifier.height(10.dp))
    GlassPanel300(Modifier.fillMaxWidth()) {
        Text("ХИРОН · СЕГОДНЯ", color = ice500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        val h = sleep.replace(',', '.').toDoubleOrNull()
        val tip = when {
            h != null && h < 6 -> "Сон был коротким. Сегодня можно выбрать спокойный темп."
            done == 3 -> "Все три привычки отмечены. Не забывай про восстановление."
            else -> "Записывай сон и движение — вместе увидим динамику."
        }
        Text(tip, color = ink500, fontSize = 13.sp, lineHeight = 19.sp)
        OutlinedButton(onClick = onChat, modifier = Modifier.fillMaxWidth()) { Text("Диалог с Хироном") }
    }
}

/** Health summary uses actual recorded days; never pretends missing days are zeros. */
@Composable
fun Health500(prefs: SharedPreferences, refresh: Int) {
    val dates = (0L..6L).map { LocalDate.now().minusDays(it).toString() }
    val sleep = dates.mapNotNull { prefs.getString("health_${it}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { n -> n in 0.0..24.0 } }
    val steps = dates.mapNotNull { prefs.getString("health_${it}_steps", null)?.toIntOrNull()?.takeIf { n -> n in 0..1000000 } }
    GlassPanel300(Modifier.fillMaxWidth()) {
        Text("ОБЗОР ЗА 7 ДНЕЙ", color = ice500, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text("СОН", color = soft500, fontSize = 11.sp)
                Text(if (sleep.isEmpty()) "—" else String.format(Locale.getDefault(), "%.1f ч", sleep.average()), color = ink500, fontSize = 20.sp)
                Text("${sleep.size}/7 дней", color = soft500, fontSize = 11.sp)
            }
            Column(Modifier.weight(1f)) {
                Text("ШАГИ", color = soft500, fontSize = 11.sp)
                Text(if (steps.isEmpty()) "—" else "${steps.average().toInt()}", color = ink500, fontSize = 20.sp)
                Text("${steps.size}/7 дней", color = soft500, fontSize = 11.sp)
            }
        }
        Text("Среднее только по записанным дням · не медицинская оценка", color = soft500, fontSize = 10.sp)
    }
}

/** Keeps old diagnostic features accessible without dominating profile. */
@Composable
fun ProfileDiagnostics500(prefs: SharedPreferences) {
    var expanded by remember { mutableStateOf(false) }
    GlassPanel300(Modifier.fillMaxWidth()) {
        Text("ДАННЫЕ И НАСТРОЙКИ", color = ice500, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("Все записи хранятся на устройстве. Перед переустановкой сохрани резервную копию.", color = ink500, fontSize = 13.sp)
        OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            Text(if (expanded) "Скрыть диагностику" else "Открыть диагностику")
        }
        if (expanded) {
            Diagnostics30(prefs)
            DataAudit50(prefs)
            CompactMode100(prefs)
            VisualSettings300(prefs)
            ReleaseStatus100(prefs)
            PrivacyAndQuality200(prefs)
            ReleaseNotes70()
        }
    }
}
