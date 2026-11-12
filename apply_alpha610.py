#!/usr/bin/env python3
"""Apply Three.js background on current Bio-Age Alpha 5/6 source tree."""
from pathlib import Path
import shutil, re, sys, urllib.request
root=Path.cwd(); pkg=Path(__file__).resolve().parent
src=root/'app/src/main/java/com/bioage/alpha'; assets=root/'app/src/main/assets/three_caucasus'; gradle=root/'app/build.gradle.kts'
main=src/'MainActivity.kt'; home=src/'Alpha500Features.kt'
for p in (main,home,gradle):
    if not p.is_file(): sys.exit(f'MISSING {p}; run from repository root')
a=main.read_text(); b=home.read_text(); g=gradle.read_text()
if 'Caucasus3DScene(camera)' not in a and 'CaucasusThreeBackground()' not in a: sys.exit('ANCHOR MISSING: MainActivity background; manual merge required')
if 'OrbitControl30(camera, onCamera)' not in b and 'OrbitControl30(camera,onCamera)' not in b and 'Ручное управление камерой отключено' not in b: sys.exit('ANCHOR MISSING: Alpha500Features orbit; manual merge required')
# Download the pinned runtime into the ZIP project at patch time, so the APK runs offline.
assets.mkdir(parents=True,exist_ok=True)
lib=assets/'three.min.js'
if not lib.exists():
    url='https://cdn.jsdelivr.net/npm/three@0.152.2/build/three.min.js'
    try:
        with urllib.request.urlopen(url,timeout=25) as r: data=r.read()
        if len(data)<100000 or b'THREE' not in data[:2000]: raise ValueError('Unexpected Three.js payload')
        lib.write_bytes(data)
    except Exception as e: sys.exit(f'Cannot vendor Three.js ({e}). Download three@0.152.2/build/three.min.js to {lib} and retry; no files changed.')
backup=root/'.bioage_three_backup';backup.mkdir(exist_ok=True)
for p in (main,home,gradle): shutil.copy2(p,backup/p.name)
shutil.copy2(pkg/'kotlin/CaucasusThreeBackground.kt',src/'CaucasusThreeBackground.kt')
shutil.copy2(pkg/'assets/index.html',assets/'index.html')
a=a.replace('Caucasus3DScene(camera)','CaucasusThreeBackground()')
b=re.sub(r'OrbitControl30\(camera,\s*onCamera\)', 'Text("Автоматическая панорама Кавказа", color = soft500, fontSize = 11.sp) // Ручное управление камерой отключено',b)
g=re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 610',g)
g=re.sub(r'versionName\s*=\s*"[^"]+"','versionName = "6.10.0-alpha"',g)
a=re.sub(r'ALPHA\s+[0-9]+\.[0-9]+', 'ALPHA 6.10',a)
main.write_text(a);home.write_text(b);gradle.write_text(g)
print('Applied Alpha 6.10 foundation. Three.js bundled offline. Build and test on device.')
