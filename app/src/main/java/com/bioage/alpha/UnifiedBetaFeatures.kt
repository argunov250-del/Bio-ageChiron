package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Locale
import kotlin.math.*

private val mint = Color(0xFFADF3E4)
private val pale = Color(0xFFEBF7FA)
private val dim = Color(0xFFB8CFD8)
private val panel = Color(0xDD102B40)

private fun sleepAt(p: SharedPreferences, d: LocalDate): Double? =
    p.getString("health_${d}_sleep", null)?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..24.0 }
private fun stepsAt(p: SharedPreferences, d: LocalDate): Int? =
    p.getString("health_${d}_steps", null)?.toIntOrNull()?.takeIf { it in 0..1000000 }
private fun habitsAt(p: SharedPreferences, d: LocalDate): Int =
    (0..2).count { p.getBoolean("day_${d}_habit_$it", false) }
private fun format(n: Double) = String.format(Locale.getDefault(), "%.1f", n)
private fun recentDates(days: Int) = (days-1 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }

data class WellnessSummary(
    val sleepAverage: Double?, val stepsAverage: Double?,
    val sleepCount: Int, val stepsCount: Int,
    val activeDays: Int, val habitMarks: Int, val restDays: Int
)
fun wellnessSummary(p: SharedPreferences, days: Int): WellnessSummary {
    val ds = recentDates(days)
    val sleep = ds.mapNotNull { sleepAt(p, it) }
    val steps = ds.mapNotNull { stepsAt(p, it) }
    return WellnessSummary(
        sleep.takeIf { it.isNotEmpty() }?.average(),
        steps.takeIf { it.isNotEmpty() }?.average(),
        sleep.size, steps.size,
        ds.count { habitsAt(p,it)>0 },
        ds.sumOf { habitsAt(p,it) },
        ds.count { p.getBoolean("rest_$it",false) }
    )
}
@Composable
private fun FrostPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical=5.dp)
        .background(panel, RoundedCornerShape(22.dp))
        .border(1.dp, Color(0x665D9DAD), RoundedCornerShape(22.dp))
        .padding(15.dp), verticalArrangement=Arrangement.spacedBy(9.dp), content=content)
}
@Composable
private fun MetricCell(label: String, value: String, modifier: Modifier=Modifier) {
    Column(modifier.background(Color(0x99264A5C),RoundedCornerShape(14.dp)).padding(11.dp)) {
        Text(label,color=dim,fontSize=11.sp)
        Text(value,color=pale,fontSize=19.sp,fontWeight=FontWeight.SemiBold)
    }
}
@Composable
fun UnifiedHome(p: SharedPreferences, steps: String, sleep: String, done: Int, refresh: Int, onChat:()->Unit) {
    val week=wellnessSummary(p,7)
    Text("Твой путь к вершине",color=pale,fontSize=23.sp,fontWeight=FontWeight.Bold)
    Text("Здоровье · Ритм · Восхождение",color=dim,fontSize=12.sp)
    Row(horizontalArrangement=Arrangement.spacedBy(7.dp),modifier=Modifier.fillMaxWidth()) {
        MetricCell("ШАГИ",steps.ifBlank{"—"},Modifier.weight(1f))
        MetricCell("СОН",sleep.takeIf{it.isNotBlank()}?.let{"$it ч"}?:"—",Modifier.weight(1f))
        MetricCell("ПРИВЫЧКИ","$done/3",Modifier.weight(1f))
    }
    val total=recentDates(90).sumOf{habitsAt(p,it)}
    val pct=(total*100/270).coerceIn(0,100)
    FrostPanel {
        Text("МАРШРУТ НА ЭЛЬБРУС",color=mint,fontWeight=FontWeight.Bold,fontSize=12.sp)
        Text("$pct%  ·  $total из 270 отметок",color=pale,fontSize=19.sp)
        LinearProgressIndicator(progress={total/270f},modifier=Modifier.fillMaxWidth().height(7.dp),color=mint)
        Text("Привал  ·  Скальный пояс  ·  Ледник  ·  Седловина  ·  Вершина",color=dim,fontSize=11.sp,lineHeight=17.sp)
        Text("Игровой маршрут, не реальная высота. Отдых не обнуляет достижения.",color=dim,fontSize=11.sp)
    }
    FrostPanel {
        Text("ХИРОН · СЕГОДНЯ",color=mint,fontWeight=FontWeight.Bold)
        Text(chironAdvice(p,steps,sleep,done),color=pale,fontSize=14.sp)
        OutlinedButton(onClick=onChat,modifier=Modifier.fillMaxWidth()){Text("Открыть Хирона")}
    }
    Text("За 7 дней: сон ${week.sleepCount}/7 · шаги ${week.stepsCount}/7 · активных дней ${week.activeDays}",
        color=dim,fontSize=11.sp)
}
private fun chironAdvice(p:SharedPreferences, steps:String,sleep:String,done:Int):String {
    val week=wellnessSummary(p,7)
    val hours=sleep.replace(',','.').toDoubleOrNull()
    return when {
        p.getBoolean("rest_${LocalDate.now()}",false) -> "Сегодня день отдыха. Не нужно компенсировать его перегрузкой."
        hours!=null && hours<6 -> "Сегодня записано менее 6 часов сна. Выбери комфортную активность и время для отдыха."
        week.sleepCount>=3 && week.sleepAverage!=null && week.sleepAverage<6.5 ->
            "По ${week.sleepCount} записям средний сон ${format(week.sleepAverage)} ч. Попробуй уделить отдыху больше внимания."
        done==3 -> "Все привычки отмечены. Сохраняй устойчивый ритм, без гонки за рекордами."
        steps.isNotBlank() -> "Шаги записаны. Сравни их со своей целью, а не с чужими результатами."
        else -> "Добавь записи сна и движения. Я покажу динамику, когда появится история."
    }
}
@Composable
fun EnergyChiron() {
    val transition=rememberInfiniteTransition(label="energy")
    val pulse by transition.animateFloat(0.84f,1.14f,
        infiniteRepeatable(tween(3300,easing=FastOutSlowInEasing),RepeatMode.Reverse),label="pulse")
    val spin by transition.animateFloat(0f,360f,
        infiniteRepeatable(tween(28000,easing=LinearEasing)),label="orbit")
    Canvas(Modifier.fillMaxWidth().height(215.dp)) {
        val c=Offset(size.width/2,size.height/2)
        val r=size.minDimension*0.29f
        drawCircle(brush=Brush.radialGradient(listOf(Color(0x99A8FFF0),Color(0x5520D5D2),Color.Transparent),
            center=c,radius=r*2.2f*pulse),radius=r*2.2f*pulse,center=c)
        drawCircle(brush=Brush.radialGradient(listOf(Color.White,mint,Color(0xFF1B8DAD),Color.Transparent),
            center=c,radius=r*pulse),radius=r*pulse,center=c)
        for (i in 0..2) {
            val radius=r*(1.28f+i*.21f)
            drawArc(mint.copy(alpha=.55f-i*.12f),spin+i*70f,195f,false,
                topLeft=Offset(c.x-radius,c.y-radius*.52f),
                size=androidx.compose.ui.geometry.Size(radius*2,radius*1.04f),
                style=Stroke(width=2f+i))
        }
        for(i in 0 until 24) {
            val a=(spin*(if(i%2==0)1 else -1)+i*137.5f)*PI/180
            val d=r*(1.25f+(i%5)*.19f)
            drawCircle(mint.copy(alpha=.25f+(i%3)*.2f),radius=1.5f+(i%3),
                center=Offset(c.x+cos(a).toFloat()*d,c.y+sin(a).toFloat()*d*.65f))
        }
    }
}
@Composable
fun UnifiedChiron(p:SharedPreferences,steps:String,sleep:String,mood:String,done:Int) {
    EnergyChiron()
    Text("ХИРОН",color=mint,fontSize=22.sp,fontWeight=FontWeight.Bold)
    Text("Энергетический наставник · офлайн",color=dim,fontSize=12.sp)
    FrostPanel {
        Text(chironAdvice(p,steps,sleep,done),color=pale)
        Text("Рекомендации основаны на сохранённых записях и правилах, а не на медицинском диагнозе.",color=dim,fontSize=11.sp)
    }
    ChironChat30(p,sleep,steps,mood,done)
}
@Composable
fun UnifiedHealth(p:SharedPreferences,refresh:Int) {
    var period by remember { mutableIntStateOf(7) }
    Row(horizontalArrangement=Arrangement.spacedBy(7.dp)) {
        listOf(7,30,90).forEach { d ->
            FilterChip(selected=period==d,onClick={period=d},label={Text("$d дней")})
        }
    }
    val s=wellnessSummary(p,period)
    FrostPanel {
        Text("ОБЗОР ЗА $period ДНЕЙ",color=mint,fontWeight=FontWeight.Bold)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            MetricCell("СОН",s.sleepAverage?.let{"${format(it)} ч"}?:"—",Modifier.weight(1f))
            MetricCell("ШАГИ",s.stepsAverage?.toInt()?.toString()?:"—",Modifier.weight(1f))
        }
        Text("Записи: сон ${s.sleepCount}/$period · шаги ${s.stepsCount}/$period",color=dim,fontSize=12.sp)
        Text("Пропущенные дни не считаются нулевыми показателями.",color=dim,fontSize=11.sp)
    }
    FrostPanel {
        Text("СОН И РЕГУЛЯРНОСТЬ",color=mint,fontWeight=FontWeight.Bold)
        Text(if(s.sleepCount<3) "Для тенденции нужно хотя бы три записи сна."
             else "Средняя продолжительность сна ${format(s.sleepAverage!!)} ч за ${s.sleepCount} записанных дней.",
             color=pale)
        Text("Дефицит сна и качество восстановления нельзя достоверно определить только по длительности.",color=dim,fontSize=11.sp)
    }
    FrostPanel {
        Text("АКТИВНОСТЬ И НАГРУЗКА",color=mint,fontWeight=FontWeight.Bold)
        Text("Активных дней: ${s.activeDays} · привычек: ${s.habitMarks} · дней отдыха: ${s.restDays}",color=pale)
        Text("WHOOP-подобный Strain требует данных интенсивности и пульса. Шаги не подменяют Strain.",color=dim,fontSize=11.sp)
    }
    FrostPanel {
        Text("ВОССТАНОВЛЕНИЕ И HRV",color=mint,fontWeight=FontWeight.Bold)
        Text("Recovery — недостаточно данных",color=pale)
        Text("Пульс, HRV и достоверные измерения сна пока не подключены. Проценты не рассчитываются.",color=dim,fontSize=11.sp)
    }
    FrostPanel {
        Text("ДОЛГОЛЕТИЕ",color=mint,fontWeight=FontWeight.Bold)
        Text("Биологический возраст и скорость старения пока недоступны.",color=pale)
        Text("Для расчёта необходима валидированная методика и соответствующие данные.",color=dim,fontSize=11.sp)
    }
}
@Composable
fun UnifiedProgress(p:SharedPreferences,refresh:Int) {
    val total=recentDates(90).sumOf{habitsAt(p,it)}
    val names=listOf("Привал","Скальный пояс","Ледник","Седловина","Вершина")
    val targets=listOf(0,30,90,180,270)
    FrostPanel {
        Text("ПУТЬ К ВЕРШИНЕ",color=mint,fontWeight=FontWeight.Bold)
        Text("${total*100/270}% · $total / 270 отметок",color=pale,fontSize=22.sp)
        LinearProgressIndicator(progress={total/270f},modifier=Modifier.fillMaxWidth(),color=mint)
        names.forEachIndexed { i,name ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text("${i+1}. $name",color=if(total>=targets[i])mint else dim)
                Text(if(total>=targets[i])"✓" else "${targets[i]} отметок",color=dim,fontSize=12.sp)
            }
        }
        Text("Прогресс — игровая мотивация, а не медицинская оценка.",color=dim,fontSize=11.sp)
    }
    val s=wellnessSummary(p,7)
    FrostPanel {
        Text("РИТМ НЕДЕЛИ",color=mint,fontWeight=FontWeight.Bold)
        Text("${s.activeDays} активных дней из 7 · ${s.restDays} дней отдыха",color=pale)
        Text("Отдых — часть пути. Пропуски не обнуляют маршрут.",color=dim,fontSize=11.sp)
    }
}
