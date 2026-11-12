package com.bioage.alpha

import android.content.SharedPreferences
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Locale

private val ice9 = Color(0xFFE9F8F8)
private val mint9 = Color(0xFFA8F4E4)
private val soft9 = Color(0xFFB9D1DA)
private val glass9 = Color(0xD9163045)

@Composable
private fun Glass9(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 11.dp)
        .background(glass9, RoundedCornerShape(22.dp))
        .border(1.dp, Color(0x775E99A5), RoundedCornerShape(22.dp))
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}
@Composable
private fun Number9(label:String, value:String, modifier:Modifier=Modifier) {
    Column(modifier.background(Color(0x88345768),RoundedCornerShape(14.dp)).padding(11.dp)) {
        Text(label,color=soft9,fontSize=10.sp,letterSpacing=0.8.sp)
        Text(value,color=ice9,fontSize=19.sp,fontWeight=FontWeight.SemiBold,maxLines=1)
    }
}
@Composable
fun Major9Home(p:SharedPreferences, steps:String, sleep:String, done:Int, onChat:()->Unit) {
    val marks=(0L..89L).sumOf { back ->
        val date=LocalDate.now().minusDays(back).toString()
        (0..2).count { p.getBoolean("day_${date}_habit_$it",false) }
    }
    val progress=(marks/270f).coerceIn(0f,1f)
    Text("ТВОЯ ЭКСПЕДИЦИЯ",color=mint9,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
    Text("Каждый день — ближе к вершине",color=ice9,fontSize=21.sp,fontWeight=FontWeight.SemiBold)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
        Number9("ВОССТАНОВЛЕНИЕ","—",Modifier.weight(1f))
        Number9("НАГРУЗКА","—",Modifier.weight(1f))
        Number9("СОН",sleep.takeIf{it.isNotBlank()}?.let{"$it ч"}?:"—",Modifier.weight(1f))
    }
    Glass9 {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            Text("МАРШРУТ НА ЭЛЬБРУС",color=mint9,fontSize=12.sp,fontWeight=FontWeight.Bold)
            Text("${(progress*100).toInt()}%",color=ice9,fontSize=20.sp,fontWeight=FontWeight.Bold)
        }
        LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth().height(8.dp),color=mint9,trackColor=Color(0xFF395365))
        Text("${marks} из 270 отметок · 5 этапов",color=soft9,fontSize=12.sp)
        Text("Отдых не обнуляет твой путь",color=soft9,fontSize=11.sp)
    }
    Glass9 {
        Text("ХИРОН",color=mint9,fontSize=12.sp,fontWeight=FontWeight.Bold)
        Text("Я рядом. Начни с записи сна или активности — и мы увидим твой ритм.",color=ice9,fontSize=13.sp)
        OutlinedButton(onClick=onChat,modifier=Modifier.fillMaxWidth()){Text("Открыть диалог")}
    }
    Text("Показатели Recovery и Strain появятся после подключения измерений в Major 2.",color=soft9,fontSize=11.sp)
}
@Composable
fun Major9Health(p:SharedPreferences, refresh:Int) {
    var period by remember { mutableIntStateOf(7) }
    val dates=(0 until period).map { LocalDate.now().minusDays(it.toLong()).toString() }
    val sleeps=dates.mapNotNull { p.getString("health_${it}_sleep",null)?.replace(',','.')?.toDoubleOrNull()?.takeIf{n->n in 0.0..24.0} }
    val steps=dates.mapNotNull { p.getString("health_${it}_steps",null)?.toIntOrNull()?.takeIf{n->n in 0..1000000} }
    Row(horizontalArrangement=Arrangement.spacedBy(7.dp)) {
        listOf(7,30,90).forEach { d -> FilterChip(selected=period==d,onClick={period=d},label={Text("$d д")}) }
    }
    Glass9 {
        Text("ТВОИ ПОКАЗАТЕЛИ",color=mint9,fontWeight=FontWeight.Bold,fontSize=12.sp)
        Row(horizontalArrangement=Arrangement.spacedBy(7.dp)) {
            Number9("СРЕДНИЙ СОН",if(sleeps.isEmpty())"—" else String.format(Locale.getDefault(),"%.1f ч",sleeps.average()),Modifier.weight(1f))
            Number9("СР. ШАГИ",if(steps.isEmpty())"—" else steps.average().toInt().toString(),Modifier.weight(1f))
        }
        Text("Сон ${sleeps.size}/$period дней · шаги ${steps.size}/$period дней",color=soft9,fontSize=12.sp)
        Text("Средние только по записанным дням",color=soft9,fontSize=11.sp)
    }
    Glass9 {
        Text("RECOVERY  /  STRAIN",color=mint9,fontWeight=FontWeight.Bold,fontSize=12.sp)
        Text("Ожидаются данные пульса, HRV и тренировок",color=ice9,fontSize=14.sp)
        Text("Подключение Apple Health и Health Connect — в Major 2. Значения не имитируются.",color=soft9,fontSize=11.sp)
    }
    Glass9 {
        Text("ДОЛГОЛЕТИЕ",color=mint9,fontWeight=FontWeight.Bold,fontSize=12.sp)
        Text("Биологический возраст: нет оценки",color=ice9,fontSize=15.sp)
        Text("Будет рассчитываться только при достаточных данных и применимой научной модели.",color=soft9,fontSize=11.sp)
    }
}
@Composable
fun Major9Progress(p:SharedPreferences, refresh:Int) {
    val marks=(0L..89L).sumOf { back ->
        val date=LocalDate.now().minusDays(back).toString()
        (0..2).count { p.getBoolean("day_${date}_habit_$it",false) }
    }
    val stages=listOf("Базовый лагерь" to 0,"Скальный пояс" to 30,"Ледник" to 90,"Седловина" to 180,"Вершина" to 270)
    Glass9 {
        Text("ВОСХОЖДЕНИЕ",color=mint9,fontSize=12.sp,fontWeight=FontWeight.Bold)
        Text("${(marks/270f*100).toInt()}% пути",color=ice9,fontSize=27.sp,fontWeight=FontWeight.Bold)
        LinearProgressIndicator(progress={marks/270f},modifier=Modifier.fillMaxWidth().height(8.dp),color=mint9)
        stages.forEachIndexed { index, stage ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text("${index+1}. ${stage.first}",color=if(marks>=stage.second)mint9 else soft9,fontSize=13.sp)
                Text(if(marks>=stage.second)"✓" else "${stage.second}",color=soft9,fontSize=12.sp)
            }
        }
        Text("Игровой маршрут, не измерение высоты или физической формы",color=soft9,fontSize=11.sp)
    }
}
