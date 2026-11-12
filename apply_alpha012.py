#!/usr/bin/env python3
"""Apply cumulative Alpha 0.8–0.12 patch to existing Bio-ageChiron repo."""
from pathlib import Path
import re, shutil
root=Path.cwd(); src=Path(__file__).resolve().parent
main=root/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
build=root/'app/build.gradle.kts'
scene=root/'app/src/main/java/com/bioage/alpha/Caucasus3DScene.kt'
assert main.is_file() and build.is_file() and scene.is_file(), 'Run from repository root containing Alpha 0.7+ sources'
m=main.read_text(); b=build.read_text()
assert 'Caucasus3DScene(camera)' in m and 'versionCode' in b, 'Unexpected project layout; abort'
assert 'fun BioAge(' in m and 'private fun chiron(' in m, 'Unexpected MainActivity; abort'
assert 'HealthHistoryPanel(' not in m, 'Patch already applied; abort'
# Alpha 0.10: remove the odd-looking default Material3 slider styling; maintain its proven gesture behavior.
m=m.replace('Slider(value = camera, onValueChange = { camera = it }, valueRange = -45f..45f, modifier = Modifier.fillMaxWidth())',
'''Slider(
                                value = camera,
                                onValueChange = { camera = it },
                                valueRange = -45f..45f,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Aqua, activeTrackColor = Aqua,
                                    inactiveTrackColor = Color(0xFF45677A)
                                )
                            )''')
# Alpha 0.11: dated history, existing current-value keys preserved.
m=m.replace('var saved by remember { mutableStateOf(false) }', 'var saved by remember { mutableStateOf(false) }\n    var historyRefresh by remember { mutableIntStateOf(0) }', 1)
old='prefs.edit().putString("steps", steps).putString("sleep", sleep).putString("mood", mood).apply(); saved = true'
new='prefs.edit().putString("steps", steps).putString("sleep", sleep).putString("mood", mood).apply(); saveHealthHistory(prefs, steps, sleep, mood); historyRefresh++; saved = true'
assert old in m, 'Health save action changed; abort'
m=m.replace(old,new,1)
old='Text("Показатели не являются медицинской оценкой.", color = Muted, fontSize = 12.sp)'
assert old in m
m=m.replace(old, old+'\n                            HealthHistoryPanel(prefs, historyRefresh)',1)
# Alpha 0.11: Chiron rule updates.
assert 'Text(chiron(sleep, steps), color = Ink' in m
m=m.replace('Text(chiron(sleep, steps), color = Ink','Text(chironV12(sleep, steps, mood, done), color = Ink',1)
# Alpha 0.12: local JSON backup, import (no cloud access).
old='Text("Панорама — настоящая 3D-модель рельефа на Filament (стилизованная, не геодезическая). Health Connect, резервные копии и расчёт биологического возраста ещё не добавлены.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)'
if old not in m:
    matches=re.findall(r'Text\("Панорама —[^\n]+',m)
    assert matches, 'Profile copy changed; abort'
    m=m.replace(matches[0], 'Text("Панорама — стилизованная 3D-модель Кавказа на Filament. Health Connect и расчёт биологического возраста пока не добавлены.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)',1)
else:
    m=m.replace(old,'Text("Панорама — стилизованная 3D-модель Кавказа на Filament. Health Connect и расчёт биологического возраста пока не добавлены.", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)',1)
needle='Text("Офлайн-прототип. Записи хранятся локально на этом телефоне.", color = Ink, lineHeight = 21.sp)'
assert needle in m
m=m.replace(needle,needle+'\n                            LocalBackupPanel(prefs)',1)
m=re.sub(r'ALPHA 0\.\d+', 'ALPHA 0.12', m)
m=re.sub(r'Alpha 0\.\d+', 'Alpha 0.12', m)
b=re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 12', b, count=1)
b=re.sub(r'versionName\s*=\s*"[^"]+"', 'versionName = "0.12.0-alpha"', b, count=1)
assert 'ALPHA 0.12' in m and 'Alpha 0.12' in m
# Commit only after every assertion passes.
main.write_text(m);build.write_text(b)
target=root/'app/src/main/java/com/bioage/alpha/Alpha12Features.kt';target.parent.mkdir(parents=True,exist_ok=True)
shutil.copy2(src/'app/src/main/java/com/bioage/alpha/Alpha12Features.kt',target)
model=root/'app/src/main/assets/models/elbrus_terrain.glb';model.parent.mkdir(parents=True,exist_ok=True)
shutil.copy2(src/'app/src/main/assets/models/elbrus_terrain.glb',model)
print('Alpha 0.12 applied (cumulative 0.8/0.9 mesh + 0.10–0.12 features). Build APK and test on device.')
