package com.bioage.alpha

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import kotlin.concurrent.thread

/**
 * Major 1 read-only data access. Nothing here writes or migrates user data.
 * Only measured values are returned; no Recovery / Strain / biological-age maths (that is Major 2).
 */
data class M1Vital(val value: Double, val day: String)
data class M1Latest(val hrv: M1Vital?, val restingHr: M1Vital?)
data class M1PeriodAvg(val value: Double, val days: Int)
data class M1Period(val heartRate: M1PeriodAvg?, val hrv: M1PeriodAvg?)

fun m1Sleep(prefs: SharedPreferences, day: LocalDate): Double? =
    prefs.getString("health_${day}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..24.0 }

fun m1Steps(prefs: SharedPreferences, day: LocalDate): Int? =
    prefs.getString("health_${day}_steps", null)?.toIntOrNull()?.takeIf { it in 0..1000000 }

fun m1DayMarks(prefs: SharedPreferences, day: LocalDate): Int =
    (0..2).count { prefs.getBoolean("day_${day}_habit_$it", false) }

/** Habit marks over the last 90 days: the same rule every earlier version used (max 270). */
fun m1TotalMarks(prefs: SharedPreferences): Int =
    (0L..89L).sumOf { m1DayMarks(prefs, LocalDate.now().minusDays(it)) }

val M1StageNames = listOf("Базовый лагерь", "Скальный пояс", "Ледник", "Седловина", "Вершина")
val M1StageMarks = listOf(0, 30, 90, 180, 270)

/** SQLiteOpenHelper is AutoCloseable only from API 29; minSdk is 26, so close explicitly. */
fun <T> m1WithStore(context: Context, block: (ChironStore) -> T): T {
    val store = ChironStore(context)
    try { return block(store) } finally { store.close() }
}

private fun latestVital(store: ChironStore, type: String): M1Vital? =
    store.readableDatabase.rawQuery(
        "SELECT day, AVG(value) FROM samples WHERE type=? AND day=(SELECT MAX(day) FROM samples WHERE type=?)",
        arrayOf(type, type)
    ).use { c -> if (c.moveToFirst() && !c.isNull(0) && !c.isNull(1)) M1Vital(c.getDouble(1), c.getString(0)) else null }

private fun periodAvg(store: ChironStore, type: String, fromDay: String): M1PeriodAvg? =
    store.readableDatabase.rawQuery(
        "SELECT AVG(value), COUNT(DISTINCT day) FROM samples WHERE type=? AND day>=?",
        arrayOf(type, fromDay)
    ).use { c -> if (c.moveToFirst() && !c.isNull(0) && c.getInt(1) > 0) M1PeriodAvg(c.getDouble(0), c.getInt(1)) else null }

/** Latest imported HRV and resting heart rate, read off the main thread. */
@Composable
fun rememberM1Latest(refresh: Int): M1Latest {
    val context = LocalContext.current.applicationContext
    var state by remember { mutableStateOf(M1Latest(null, null)) }
    DisposableEffect(refresh) {
        thread(name = "m1-latest") {
            val result = runCatching {
                m1WithStore(context) { M1Latest(latestVital(it, "hrv_sdnn"), latestVital(it, "resting_hr")) }
            }.getOrNull()
            if (result != null) state = result
        }
        onDispose { }
    }
    return state
}

/** Imported averages for the selected period, read off the main thread. */
@Composable
fun rememberM1Period(days: Int, refresh: Int): M1Period {
    val context = LocalContext.current.applicationContext
    var state by remember { mutableStateOf(M1Period(null, null)) }
    DisposableEffect(days, refresh) {
        val from = LocalDate.now().minusDays((days - 1).toLong()).toString()
        thread(name = "m1-period") {
            val result = runCatching {
                m1WithStore(context) { M1Period(periodAvg(it, "heart_rate", from), periodAvg(it, "hrv_sdnn", from)) }
            }.getOrNull()
            if (result != null) state = result
        }
        onDispose { }
    }
    return state
}

/** "сегодня" or dd.MM for a yyyy-MM-dd day string. */
fun m1DayLabel(day: String): String =
    if (day == LocalDate.now().toString()) "сегодня" else if (day.length >= 10) "${day.substring(8, 10)}.${day.substring(5, 7)}" else day
