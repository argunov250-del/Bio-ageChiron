from pathlib import Path
import re,shutil
root=Path.cwd()
base=Path(__file__).resolve().parent
app=root/'app/src/main/java/com/bioage/alpha'
main=app/'MainActivity.kt'
gradle=root/'app/build.gradle.kts'
if not all(p.exists() for p in (main,gradle,app/'Alpha12Features.kt')): raise SystemExit('Run from Bio-ageChiron repo root with Alpha 0.12 installed')
s=main.read_text()
if 'ALPHA 0.17' in s: raise SystemExit('Alpha 0.17 already applied; no changes made')
if 'ALPHA 0.12' not in s: raise SystemExit('Expected Alpha 0.12 base; inspect manually before applying')
old='Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(28.dp)).background(Glass).verticalScroll(rememberScrollState()).padding(20.dp)) {'
assert s.count(old)==1, 'Unexpected scroll card layout'
s=s.replace(old,'Column(Modifier.fillMaxWidth().weight(1f, fill = false).padding(horizontal = 12.dp).clip(RoundedCornerShape(28.dp)).background(Glass).verticalScroll(rememberScrollState()).padding(20.dp)) {')
assert s.count('Spacer(Modifier.weight(1f))')==1
s=s.replace('Spacer(Modifier.weight(1f))','Spacer(Modifier.weight(1f, fill = false))')
assert s.count('HealthHistoryPanel(prefs, historyRefresh)')==1
s=s.replace('HealthHistoryPanel(prefs, historyRefresh)','HealthHistoryPanel(prefs, historyRefresh)\n                            HealthTrendsPanel(prefs, historyRefresh)')
assert s.count('chironV12(sleep, steps, mood, done)')==1
s=s.replace('chironV12(sleep, steps, mood, done)','chironV17(prefs, sleep, steps, mood, done, historyRefresh)')
# Ensure back button exits a stuck form by returning to Home.
s=s.replace('import androidx.activity.compose.setContent','import androidx.activity.compose.setContent\nimport androidx.activity.compose.BackHandler')
s=s.replace('    MaterialTheme(colorScheme = colors) {','    BackHandler(enabled = tab != 0) { tab = 0 }\n    MaterialTheme(colorScheme = colors) {',1)
s=s.replace('ALPHA 0.12','ALPHA 0.17').replace('Alpha 0.12','Alpha 0.17')
# Rename UI slider to clarify that it controls camera orbit; keep standard Material3 slider.
s=s.replace('Панорама  ·','Поворот камеры  ·')
g=gradle.read_text()
assert 'versionCode = 12' in g and '0.12.0-alpha' in g
g=g.replace('versionCode = 12','versionCode = 17').replace('0.12.0-alpha','0.17.0-alpha')
# Write only after all preflight assertions.
for p in (main,gradle): shutil.copy2(p,p.with_suffix(p.suffix+'.alpha12.bak'))
main.write_text(s);gradle.write_text(g)
for rel in ['app/src/main/java/com/bioage/alpha/Alpha17Features.kt','app/src/main/java/com/bioage/alpha/Caucasus3DScene.kt','app/src/main/assets/models/elbrus_terrain.glb']:
    target=root/rel;target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(base/rel,target)
print('Alpha 0.17 applied: 0.13 navigation, 0.14 rugged GLB, 0.15 lighting/clouds/camera, 0.16 trends, 0.17 Chiron')
