package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private val Ice400 = Color(0xFFB5F0E7)
private val Soft400 = Color(0xFFB7CDD5)
private val White400 = Color(0xFFF1F8FA)

/** Offline route based on the existing 90-day habit marks, not physical altitude. */
@Composable
fun AlpineRoute400(prefs: SharedPreferences, refresh: Int) {
    val today = LocalDate.now()
    val marks = (0L..89L).sumOf { back ->
        val day = today.minusDays(back).toString()
        (0..2).count { prefs.getBoolean("day_${day}_habit_$it", false) }
    }
    val fraction = (marks / 270f).coerceIn(0f, 1f)
    val checkpoints = listOf(0 to "Старт", 30 to "Привал", 90 to "Тропа", 180 to "Лагерь", 270 to "Вершина")
    val next = checkpoints.firstOrNull { it.first > marks }
    Column(Modifier.fillMaxWidth().background(Color(0x99304D60), RoundedCornerShape(20.dp)).padding(14.dp)) {
        Text("МАРШРУТ НА ЭЛЬБРУС", color = Ice400, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(5.dp))
        Text("${(fraction * 100).toInt()}% · ${next?.second ?: "Вершина достигнута"}", color = White400, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Canvas(Modifier.fillMaxWidth().height(70.dp)) {
            val pts = listOf(
                Offset(size.width*.04f,size.height*.83f),
                Offset(size.width*.25f,size.height*.62f),
                Offset(size.width*.46f,size.height*.55f),
                Offset(size.width*.69f,size.height*.33f),
                Offset(size.width*.95f,size.height*.09f)
            )
            val path = Path().apply { moveTo(pts.first().x,pts.first().y);pts.drop(1).forEach { lineTo(it.x,it.y) } }
            drawPath(path, Soft400.copy(alpha=.65f), style=Stroke(width=3.dp.toPx()))
            val traversed = Path().apply { moveTo(pts.first().x,pts.first().y) }
            val progress = fraction*4f
            for (i in 0..3) {
                if (progress <= i) break
                val t = (progress-i).coerceIn(0f,1f)
                traversed.lineTo(pts[i].x+(pts[i+1].x-pts[i].x)*t,pts[i].y+(pts[i+1].y-pts[i].y)*t)
            }
            drawPath(traversed, Ice400, style=Stroke(width=4.dp.toPx()))
            pts.forEachIndexed { i,p -> drawCircle(if(fraction*4>=i) Ice400 else Soft400, radius=5.dp.toPx(),center=p) }
        }
        Text("$marks из 270 отметок · игровой маршрут, не реальная высота", color = Soft400, fontSize = 11.sp)
    }
}

@Composable
fun MountainInfo400() {
    Spacer(Modifier.height(8.dp))
    Text("Кавказ · скальный рельеф и снежные хребты · 3D офлайн", color = Soft400, fontSize = 11.sp)
}
