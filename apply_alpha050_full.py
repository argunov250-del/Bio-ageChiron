#!/usr/bin/env python3
from pathlib import Path
import shutil,sys
root=Path.cwd();base=Path(__file__).resolve().parent
main=root/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
feat=root/'app/src/main/java/com/bioage/alpha/Alpha30Features.kt'
gradle=root/'app/build.gradle.kts'
extra=root/'app/src/main/java/com/bioage/alpha/Alpha50Features.kt'
full=root/'app/src/main/java/com/bioage/alpha/Alpha50FullFeatures.kt'
model=root/'app/src/main/assets/models/elbrus_terrain.glb'
for p in (main,feat,gradle,model):
    if not p.is_file():sys.exit(f'Missing {p}; run from repository root')
s=main.read_text();f=feat.read_text();g=gradle.read_text()
if 'ALPHA 0.31' not in s or 'versionCode = 31' not in g:sys.exit('Expected Alpha 0.31; no changes made')
if extra.exists() or full.exists():sys.exit('Feature files already exist; no changes made')
def once(s,a,b):
    if s.count(a)!=1:raise ValueError(f'Expected one match, got {s.count(a)}: {a[:70]}')
    return s.replace(a,b,1)
s=once(s,'ALPHA 0.31','ALPHA 0.50')
s=once(s,'Bio-Age Alpha 0.31','Bio-Age Alpha 0.50')
s=once(s,'var chatOpen by remember { mutableStateOf(false) }','var chatOpen by remember { mutableStateOf(false) }\n    var healthExpanded by remember { mutableStateOf(false) }')
s=once(s,'Text(chiron30(prefs, sleep, steps, mood, done), color = Ink, fontSize = 14.sp, lineHeight = 21.sp)',
    'Text(chiron30(prefs, sleep, steps, mood, done), color = Ink, fontSize = 14.sp, lineHeight = 21.sp)\n                            HomeSummit50(prefs, done + historyRefresh)\n                            SceneInfo50()')
s=once(s,'HealthHistoryPanel(prefs, historyRefresh)\n                            HealthTrendsPanel(prefs, historyRefresh)\n                            Analytics30(prefs, historyRefresh)\n                            Recovery30(prefs, historyRefresh)\n                            Goals30(prefs, steps)',
    'HealthSummary50(prefs, historyRefresh)\n                            Recovery30(prefs, historyRefresh)\n                            Goals30(prefs, steps)\n                            ChironInsights50(prefs, historyRefresh)\n                            WeeklyReflection50(prefs)\n                            Spacer(Modifier.height(12.dp))\n                            OutlinedButton(onClick = { healthExpanded = !healthExpanded }, modifier = Modifier.fillMaxWidth()) {\n                                Text(if (healthExpanded) "Скрыть историю и графики" else "Показать историю и графики")\n                            }\n                            if (healthExpanded) {\n                                HealthHistoryPanel(prefs, historyRefresh)\n                                HealthTrendsPanel(prefs, historyRefresh)\n                                Analytics30(prefs, historyRefresh)\n                            }')
s=once(s,'SummitJourney30(prefs, done + historyRefresh)\n                            Reports30(prefs, done + historyRefresh)',
    'SummitJourney30(prefs, done + historyRefresh)\n                            SummitMilestones50(prefs, done + historyRefresh)\n                            Reports30(prefs, done + historyRefresh)')
s=once(s,'Diagnostics30(prefs)','Diagnostics30(prefs)\n                            DataAudit50(prefs)')
f=once(f,'Text("Каждый столбец — день с записью. Пустые дни не заменяются нулями.", color = Dim30, fontSize = 11.sp)',
    'if (values.any { it != null }) Text("Каждый столбец — день с записью. Пустые дни не заменяются нулями.", color = Dim30, fontSize = 11.sp)')
g=once(g,'versionCode = 31; versionName = "0.31.0-alpha"','versionCode = 50; versionName = "0.50.0-alpha"')
for p,data in ((main,s),(feat,f),(gradle,g)):p.write_text(data)
shutil.copyfile(base/'Alpha50Features.kt',extra)
shutil.copyfile(base/'Alpha50FullFeatures.kt',full)
shutil.copyfile(base/'app/src/main/assets/models/elbrus_terrain.glb',model)
print('Alpha 0.50 FULL installed; build ./gradlew :app:assembleDebug')
