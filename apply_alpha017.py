from pathlib import Path
import re
root=Path.cwd()
main=root/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
features=root/'app/src/main/java/com/bioage/alpha/Alpha12Features.kt'
gradle=root/'app/build.gradle.kts'
for p in (main,features,gradle):
    if not p.exists(): raise SystemExit(f'Missing {p}; run in Bio-ageChiron repository root')
s=main.read_text()
assert 'ALPHA 0.12' in s or 'ALPHA 0.20' in s, 'Unexpected MainActivity version'
assert 'Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)' in s, 'Card layout changed; manual integration required'
# 0.13: bound scroll region so footer is always visible.
s=s.replace('Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(28.dp)).background(Glass).verticalScroll(rememberScrollState()).padding(20.dp)) {',
'''Column(Modifier.fillMaxWidth().weight(1f, fill = false).padding(horizontal = 12.dp).clip(RoundedCornerShape(28.dp)).background(Glass).verticalScroll(rememberScrollState()).padding(20.dp)) {''')
# replace flexible spacer that can squeeze content; keep scene visible with proportional space
s=s.replace('Spacer(Modifier.weight(1f))','Spacer(Modifier.weight(1f, fill = false))')
# UI 0.15 slider: Material3 slider native avoids custom drawn bars
s=s.replace('Slider(value = camera, onValueChange = { camera = it }, valueRange = -45f..45f, modifier = Modifier.fillMaxWidth(), colors = SliderDefaults.colors(thumbColor = Aqua, activeTrackColor = Aqua, inactiveTrackColor = Color(0xFF496677)))',
'''Slider(value = camera, onValueChange = { camera = it }, valueRange = -45f..45f, modifier = Modifier.fillMaxWidth(), colors = SliderDefaults.colors(thumbColor = Aqua, activeTrackColor = Aqua, inactiveTrackColor = Color(0xFF496677)))''')
# 0.16 trends
s=s.replace('HealthHistoryPanel(prefs, historyRefresh)','HealthHistoryPanel(prefs, historyRefresh)\n                            HealthTrendsPanel(prefs, historyRefresh)')
# 0.17 Chiron explanation with historical records
s=s.replace('Text(chironV12(sleep, steps, mood, done), color = Ink, fontSize = 14.sp, lineHeight = 21.sp)',
'''Text(chironV17(prefs, sleep, steps, mood, done, historyRefresh), color = Ink, fontSize = 14.sp, lineHeight = 21.sp)''')
s=s.replace('ALPHA 0.12','ALPHA 0.17').replace('Alpha 0.12','Alpha 0.17').replace('ALPHA 0.20','ALPHA 0.17').replace('Alpha 0.20','Alpha 0.17')
main.write_text(s)
g=gradle.read_text()
g=re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 17', g)
g=re.sub(r'versionName\s*=\s*"[^"]+"', 'versionName = "0.17.0-alpha"', g)
gradle.write_text(g)
new=(root/'app/src/main/java/com/bioage/alpha/Alpha17Features.kt')
new.write_text('''package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.Locale

/** Alpha 0.16: trends use only dated, manually entered records. */
@Composable
fun HealthTrendsPanel(prefs: SharedPreferences, refresh: Int) {
    val dates = remember(refresh) { (0L..29L).map { LocalDate.now().minusDays(it) } }
    val steps = dates.mapNotNull { prefs.getString("health_${it}_steps", null)?.toIntOrNull()?.takeIf { n -> n >= 0 } }
    val sleep = dates.mapNotNull { prefs.getString("health_${it}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { h -> h in 0.0..24.0 } }
    Spacer(Modifier.height(18.dp))
    Text("Тенденции за 30 дней", color = Color.White, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(6.dp))
    Text("Только дни с записями; пропущенные дни не считаются нулём.", color = Color(0xFFC1D6DE), style = MaterialTheme.typography.bodySmall)
    if (steps.isEmpty() && sleep.isEmpty()) {
        Text("Пока недостаточно записей для тенденций.", color = Color(0xFFC1D6DE))
    } else {
        if (steps.isNotEmpty()) Text("Шаги: в среднем ${steps.average().toInt()} за ${steps.size} дн.", color = Color.White)
        if (sleep.isNotEmpty()) Text("Сон: в среднем ${String.format(Locale.getDefault(), "%.1f", sleep.average())} ч за ${sleep.size} дн.", color = Color.White)
    }
}

/** Alpha 0.17: transparent local rules; no medical diagnosis or fake AI inference. */
fun chironV17(prefs: SharedPreferences, sleep: String, steps: String, mood: String, habitsDone: Int, refresh: Int): String {
    val hours = sleep.replace(',', '.').toDoubleOrNull()
    val walking = steps.toIntOrNull()
    if (hours != null && hours !in 0.0..24.0) return "Проверь запись сна: допустимо от 0 до 24 часов."
    val recent = (0L..6L).mapNotNull { day ->
        prefs.getString("health_${LocalDate.now().minusDays(day)}_sleep", null)
            ?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..24.0 }
    }
    return when {
        hours != null && hours < 6.0 -> "Сегодня записано $sleep ч сна. Возможно, стоит уделить больше времени восстановлению. Это подсказка по записи, не медицинский вывод."
        recent.size >= 3 && recent.average() < 7.0 -> "За ${recent.size} записанных дней средний сон — ${String.format(Locale.getDefault(), "%.1f", recent.average())} ч. Обрати внимание на самочувствие и отдых."
        mood.isNotBlank() -> "Ты отметил самочувствие: «${mood.take(60)}». Сравни записи за несколько дней, чтобы увидеть изменения."
        habitsDone == 3 -> "Все три привычки отмечены. Отдых тоже часть устойчивого прогресса."
        walking != null && walking > 0 -> "Записано $walking шагов. Поддерживай комфортный темп; цели можно менять под самочувствие."
        else -> "Запиши сон, шаги или самочувствие. Когда появится несколько дней записей, я смогу показать тенденции."
    }
}
''')
print('Patched Alpha 0.17:', main, new, gradle)
