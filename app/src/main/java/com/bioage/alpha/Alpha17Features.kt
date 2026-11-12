package com.bioage.alpha

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
        val recent7 = dates.take(7).mapNotNull { prefs.getString("health_${it}_steps", null)?.toIntOrNull()?.takeIf { n -> n >= 0 } }
        if (recent7.isNotEmpty()) Text("Последние 7 дней: в среднем ${recent7.average().toInt()} шагов (${recent7.size} записей)", color = Color(0xFFB5F0E7))
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
