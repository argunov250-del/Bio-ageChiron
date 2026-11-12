package com.bioage.alpha

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(7, 24, 38))
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

/**
 * Major 1 shell: one persistent mountain scene behind four screens.
 * All state, SharedPreferences keys and save logic are the same as before the redesign.
 */
@Composable
private fun BioAge(prefs: SharedPreferences) {
    var tab by remember { mutableIntStateOf(0) }
    var chatOpen by remember { mutableStateOf(false) }
    var steps by remember { mutableStateOf(prefs.getString("steps", "") ?: "") }
    var sleep by remember { mutableStateOf(prefs.getString("sleep", "") ?: "") }
    var mood by remember { mutableStateOf(prefs.getString("mood", "") ?: "") }
    var saved by remember { mutableStateOf(false) }
    var historyRefresh by remember { mutableIntStateOf(0) }
    val today = remember { LocalDate.now().toString() }
    val habitNames = remember { listOf("Прогулка", "Восстановление", "Самочувствие") }
    var habits by remember { mutableStateOf((0..2).map { prefs.getBoolean("day_${today}_habit_$it", false) }) }
    val done = habits.count { it }
    val totalMarks = remember(habits, historyRefresh) { m1TotalMarks(prefs) }
    val scene = remember { M1SceneController() }
    val latest = rememberM1Latest(tab)
    val sceneMode = if (chatOpen) "dim" else when (tab) { 0 -> "home"; 2 -> "progress"; else -> "dim" }
    val dim by animateFloatAsState(if (sceneMode == "dim") .94f else 0f, tween(350), label = "m1-dim")
    val heroEnd by animateFloatAsState(if (sceneMode == "progress") .40f else .47f, tween(350), label = "m1-hero")

    BackHandler(enabled = chatOpen || tab != 0) { if (chatOpen) chatOpen = false else tab = 0 }
    MaterialTheme(colorScheme = m1Colors()) {
        CompositionLocalProvider(LocalContentColor provides M1.Ink) {
            Box(Modifier.fillMaxSize().background(M1.Bg)) {
                M1FallbackBackdrop()
                M1MountainScene(scene, sceneMode, (totalMarks / 270f).coerceIn(0f, 1f))
                // Legibility scrim: clear over the peaks, solid navy under the hero. Never a full-screen sheet.
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    0f to Color(0x8C061521), .13f to Color(0x00061521),
                    (heroEnd - .12f) to Color(0x00061521), (heroEnd + .05f) to M1.Bg, 1f to M1.Bg)))
                if (dim > 0f) Box(Modifier.fillMaxSize().background(M1.Bg.copy(alpha = dim)))
                Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                    BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                        val full = maxHeight
                        if (chatOpen) {
                            M1ChatScreen(prefs, steps, sleep, mood, done) { chatOpen = false }
                        } else when (tab) {
                            0 -> M1HomeScreen(prefs, steps, sleep, mood, done, totalMarks, full * .42f, latest,
                                onChat = { chatOpen = true }, onOpenProgress = { tab = 2 })
                            1 -> M1HealthScreen(prefs, steps, sleep, mood,
                                onSteps = { steps = it.filter(Char::isDigit).take(7); saved = false },
                                onSleep = { sleep = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(5); saved = false },
                                onMood = { mood = it.take(100); saved = false },
                                saved = saved,
                                onSave = {
                                    prefs.edit().putString("steps", steps).putString("sleep", sleep).putString("mood", mood).apply()
                                    saveHealthHistory(prefs, steps, sleep, mood)
                                    historyRefresh++
                                    saved = true
                                },
                                refresh = historyRefresh)
                            2 -> M1ProgressScreen(prefs, habitNames, habits,
                                onToggle = { index ->
                                    val next = !habits[index]
                                    habits = habits.toMutableList().also { it[index] = next }
                                    prefs.edit().putBoolean("day_${today}_habit_$index", next).apply()
                                },
                                done = done, totalMarks = totalMarks, heroHeight = full * .36f)
                            else -> M1ProfileScreen(prefs, scene)
                        }
                    }
                    M1NavBar(tab) { tab = it; chatOpen = false }
                }
            }
        }
    }
}
