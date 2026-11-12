package com.bioage.alpha

import android.content.SharedPreferences
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.concurrent.thread

@Composable
fun ChironDataCenter(prefs:SharedPreferences) {
    val context=LocalContext.current
    var status by remember { mutableStateOf("Архив Apple Health ещё не импортирован") }
    var busy by remember { mutableStateOf(false) }
    var imported by remember { mutableIntStateOf(0) }
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null && !busy) {
            busy=true;status="Чтение архива…"
            thread(name="chiron-apple-health-import") {
                runCatching { AppleHealthImporter.import(context,uri) { count ->
                    android.os.Handler(android.os.Looper.getMainLooper()).post { status="Обработано записей: $count" }
                } }.onSuccess { result ->
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        busy=false;imported++
                        status="Готово: ${result.inserted} новых записей; ${result.ignored} пропущено/повторно"
                    }
                }.onFailure { error ->
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        busy=false;status="Ошибка: ${error.localizedMessage}"
                    }
                }
            }
        }
    }
    // The app has no Surface, so default text colour would be black on the dark glass card.
    CompositionLocalProvider(LocalContentColor provides Color(0xFFF1F8FA)) {
    Text("Центр данных здоровья",style=MaterialTheme.typography.titleLarge)
    Text("Apple Health ZIP · локальный потоковый импорт")
    Button(enabled=!busy,onClick={launcher.launch(arrayOf("application/zip","application/octet-stream","*/*"))},modifier=Modifier.fillMaxWidth()) {
        Text(if(busy)"Импорт выполняется…" else "Импортировать Apple Health ZIP")
    }
    Text(status,style=MaterialTheme.typography.bodySmall)
    val count=remember(imported) { ChironStore(context).use{it.totalRows()} }
    Text("Записей в локальной базе: $count")
    val today=LocalDate.now().toString()
    val todayData=remember(imported) { ChironStore(context).use { db ->
        Triple(db.daily(today,"hrv_sdnn"),db.daily(today,"resting_hr"),db.daily(today,"heart_rate"))
    } }
    Text("Сегодня: HRV ${todayData.first.size}, пульс покоя ${todayData.second.size}, пульс ${todayData.third.size}")
    Text("Внимание: импортированные данные пока не включены в старые карточки здоровья. Нельзя считать их WHOOP-эквивалентом.",style=MaterialTheme.typography.bodySmall)
    Spacer(Modifier.height(8.dp))
    }
}
