#!/usr/bin/env python3
"""Apply Alpha 0.30 incremental source patch to Bio-ageChiron Alpha 0.17."""
from pathlib import Path
import re, shutil, sys
root=Path.cwd(); base=Path(__file__).resolve().parent
main=root/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
gradle=root/'app/build.gradle.kts'
scene=root/'app/src/main/java/com/bioage/alpha/Caucasus3DScene.kt'
if not all(x.is_file() for x in [main,gradle,scene,root/'app/src/main/java/com/bioage/alpha/Alpha17Features.kt']):
    sys.exit('ERROR: run from repository root with Alpha 0.17 sources')
s=main.read_text();g=gradle.read_text();c=scene.read_text()
if 'ALPHA 0.30' in s: sys.exit('Already patched: Alpha 0.30')
checks=['ALPHA 0.17','chironV17(prefs, sleep, steps, mood, done, historyRefresh)','HealthTrendsPanel(prefs, historyRefresh)','LocalBackupPanel(prefs)','when (tab) {','Bio-Age Alpha 0.17']
for needle in checks:
    if needle not in s: sys.exit('ERROR: expected Alpha 0.17 pattern missing: '+needle)
if 'versionCode = 17' not in g or '0.17.0-alpha' not in g: sys.exit('ERROR: unexpected Gradle version')
# Make screen card content truly bounded and scrollable; preserves bottom navigation.
card='Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(28.dp)).background(Glass).verticalScroll(rememberScrollState()).padding(20.dp)) {'
if card not in s: sys.exit('ERROR: cannot identify existing card')
# Existing Alpha 0.17 bounded card and fixed bottom bar are preserved.
# Header and home view.
s=s.replace('ALPHA 0.17','ALPHA 0.30').replace('Bio-Age Alpha 0.17','Bio-Age Alpha 0.30')
s=s.replace('Text("Поворот камеры  ·  ${camera.roundToInt()}°", color = Muted, fontSize = 12.sp)\n                            Slider(value = camera, onValueChange = { camera = it }, valueRange = -45f..45f, modifier = Modifier.fillMaxWidth(), colors = SliderDefaults.colors(thumbColor = Aqua, activeTrackColor = Aqua, inactiveTrackColor = Color(0xFF496677)))', 'OrbitControl30(camera) { camera = it }',1)
s=s.replace('chironV17(prefs, sleep, steps, mood, done, historyRefresh)','chiron30(prefs, sleep, steps, mood, done)',1)
# New functions are kept in their own file, reducing risk of breaking existing UI.
s=s.replace('Text(chiron30(prefs, sleep, steps, mood, done), color = Ink, fontSize = 14.sp, lineHeight = 21.sp)', 'Text(chiron30(prefs, sleep, steps, mood, done), color = Ink, fontSize = 14.sp, lineHeight = 21.sp)\n                            ChironChat30(prefs, sleep, steps, mood, done)',1)
s=s.replace('HealthTrendsPanel(prefs, historyRefresh)', 'HealthTrendsPanel(prefs, historyRefresh)\n                            Analytics30(prefs, historyRefresh)\n                            Recovery30(prefs, historyRefresh)\n                            Goals30(prefs, steps)',1)
s=s.replace('Text("Отметки теперь привязаны к датам. Исторические серии и награды — в следующих версиях.", color = Muted, fontSize = 12.sp)', 'SummitJourney30(prefs, done + historyRefresh)\n                            Reports30(prefs, done + historyRefresh)',1)
s=s.replace('LocalBackupPanel(prefs)', 'LocalBackupPanel(prefs)\n                            Diagnostics30(prefs)',1)
s=s.replace('Панорама — стилизованная 3D-модель Кавказа на Filament. Health Connect и расчёт биологического возраста пока не добавлены.', '3D-Кавказ работает локально. Health Connect и медицинский расчёт биологического возраста пока не подключены.',1)
g=g.replace('versionCode = 17','versionCode = 30').replace('0.17.0-alpha','0.30.0-alpha')
# Move camera away slightly for better mountain framing.
c=c.replace('z = 16.5f','z = 19.5f').replace('val radius = 16.5','val radius = 19.5')
# Verify no unexpected old version remains in displayed UI.
assert 'ALPHA 0.17' not in s and 'versionCode = 17' not in g
# All preflight checks passed; now write files.
for path,content in [(main,s),(gradle,g),(scene,c)]:
    path.write_text(content)
for rel in ['app/src/main/java/com/bioage/alpha/Alpha30Features.kt','app/src/main/assets/models/elbrus_terrain.glb']:
    dst=root/rel;dst.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(base/rel,dst)
print('Alpha 0.30 patch applied. Run ./gradlew :app:assembleDebug; Android build NOT verified by this script.')
