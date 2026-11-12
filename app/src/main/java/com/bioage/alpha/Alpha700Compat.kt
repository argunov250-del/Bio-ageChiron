package com.bioage.alpha

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

/**
 * Alpha 7.00 integration: UnifiedProgress replaced SummitJourney30, which also held the only
 * "rest day" switch. Same storage key as before (rest_<date>), so existing records keep working.
 */
@Composable
fun RestDayToggle700() {
    val prefs = LocalContext.current.getSharedPreferences("bioage_local", Context.MODE_PRIVATE)
    val key = "rest_${LocalDate.now()}"
    var rest by remember(key) { mutableStateOf(prefs.getBoolean(key, false)) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = rest, onCheckedChange = { rest = it; prefs.edit().putBoolean(key, it).apply() })
        Text("Сегодня день отдыха", color = Color(0xFFEBF7FA))
    }
}
