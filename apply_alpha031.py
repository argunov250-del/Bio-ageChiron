#!/usr/bin/env python3
"""Apply a narrowly-scoped Alpha 0.31 UX patch to verified Alpha 0.30 sources."""
from pathlib import Path
import re, shutil, sys
root=Path.cwd();base=Path(__file__).resolve().parent
main=root/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
feat=root/'app/src/main/java/com/bioage/alpha/Alpha30Features.kt'
gradle=root/'app/build.gradle.kts'
for p in (main,feat,gradle):
    if not p.is_file():sys.exit(f'Missing {p}; run from Bio-ageChiron repository root')
s=main.read_text(); f=feat.read_text(); g=gradle.read_text()
if 'ALPHA 0.31' in s:sys.exit('Already at Alpha 0.31')
assert 'ALPHA 0.30' in s and 'versionCode = 30' in g, 'Expected Alpha 0.30 source'

def once(source,old,new):
    n=source.count(old)
    if n!=1:raise ValueError(f'Expected exactly one match ({n}): {old[:110]}')
    return source.replace(old,new,1)

s=once(s,'var tab by remember { mutableIntStateOf(0) }','var tab by remember { mutableIntStateOf(0) }\n    var chatOpen by remember { mutableStateOf(false) }')
s=once(s,'BackHandler(enabled = tab != 0) { tab = 0 }','BackHandler(enabled = chatOpen || tab != 0) { if (chatOpen) chatOpen = false else tab = 0 }')
s=once(s,'ALPHA 0.30','ALPHA 0.31')
s=once(s,'Bio-Age Alpha 0.30','Bio-Age Alpha 0.31')
s=once(s,'Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.BottomCenter) {','BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.BottomCenter) {')
s=once(s,'Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(28.dp)).background(Glass).verticalScroll(rememberScrollState()).padding(20.dp)) {','Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)\n                    .then(if (tab == 0 && !chatOpen) Modifier.heightIn(max = maxHeight * 0.59f) else Modifier)\n                    .clip(RoundedCornerShape(28.dp)).background(Glass)\n                    .verticalScroll(rememberScrollState()).padding(20.dp)) {')
s=once(s,'0 -> {\n                            Text("Твой путь к вершине"','0 -> {\n                            if (chatOpen) {\n                                Row(verticalAlignment = Alignment.CenterVertically) {\n                                    TextButton(onClick = { chatOpen = false }) { Text("← Назад") }\n                                    Text("Хирон · диалог", color = Ink, fontSize = 20.sp)\n                                }\n                                ChironChat30(prefs, sleep, steps, mood, done)\n                            } else {\n                            Text("Твой путь к вершине"')
s=once(s,'                            ChironChat30(prefs, sleep, steps, mood, done)\n                        }\n                        1 -> {','                            Spacer(Modifier.height(10.dp))\n                            OutlinedButton(onClick = { chatOpen = true }, modifier = Modifier.fillMaxWidth()) {\n                                Text("Открыть диалог с Хироном")\n                            }\n                            }\n                        }\n                        1 -> {')
s=once(s,'Modifier.clickable { tab = i }.padding(horizontal = 4.dp, vertical = 6.dp)','Modifier.clickable { tab = i; chatOpen = false }.padding(horizontal = 4.dp, vertical = 6.dp)')
# Leave the camera and model intact. Preserve all user data keys.
# Correct empty-state and replace distorted Slider with accessible +/- buttons.
f=once(f,'    Canvas(Modifier.fillMaxWidth().height(96.dp)) {','    if (values.all { it == null }) {\n        Text("Пока нет записей за выбранный период. Добавь данные в разделе «Здоровье».", color = Dim30, fontSize = 12.sp)\n    } else Canvas(Modifier.fillMaxWidth().height(96.dp)) {')
old='''    Slider(value = target, onValueChange = { target = (it / 500f).roundToInt() * 500f }, onValueChangeFinished = { prefs.edit().putInt("goal_steps", target.roundToInt()).apply() }, valueRange = 1000f..20000f, steps = 37)'''
new='''    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        OutlinedButton(onClick = {
            target = (target - 500f).coerceAtLeast(1000f)
            prefs.edit().putInt("goal_steps", target.roundToInt()).apply()
        }) { Text("−500") }
        Text("${target.roundToInt()}", color = Accent30, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = {
            target = (target + 500f).coerceAtMost(20000f)
            prefs.edit().putInt("goal_steps", target.roundToInt()).apply()
        }) { Text("+500") }
    }'''
f=once(f,old,new)
g=once(g,'versionCode = 30; versionName = "0.30.0-alpha"','versionCode = 31; versionName = "0.31.0-alpha"')
# Preflight all modifications before writing.
assert s.count('ChironChat30(prefs, sleep, steps, mood, done)')==1
assert 'versionCode = 31' in g
for path,data in ((main,s),(feat,f),(gradle,g)):
    path.write_text(data)
print('Alpha 0.31 UX patch applied. Build with ./gradlew :app:assembleDebug')
