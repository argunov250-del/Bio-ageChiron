from pathlib import Path
import shutil
root=Path.cwd()
main=root/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
gradle=root/'app/build.gradle.kts'
scene=root/'app/src/main/java/com/bioage/alpha/Caucasus3DScene.kt'
asset=root/'app/src/main/assets/models/elbrus_terrain.glb'
for p in (main,gradle,scene,asset):
    if not p.is_file(): raise SystemExit(f'Missing {p}. Copy patch contents to repo root first.')
s=main.read_text(encoding='utf-8')
if 'Caucasus3DScene(camera)' not in s: raise SystemExit('Expected Alpha 0.3 3D scene call missing. Stop, do not overwrite UI.')
s=s.replace('КАВКАЗ  /  ALPHA 0.3','КАВКАЗ  /  ALPHA 0.4').replace('Bio-Age Alpha 0.3','Bio-Age Alpha 0.4')
main.write_text(s,encoding='utf-8')
g=gradle.read_text(encoding='utf-8')
if 'versionCode = 3' not in g or 'versionName = "0.3.0-alpha"' not in g: raise SystemExit('Unexpected version; inspect before modifying')
g=g.replace('versionCode = 3','versionCode = 4').replace('versionName = "0.3.0-alpha"','versionName = "0.4.0-alpha"')
gradle.write_text(g,encoding='utf-8')
print('Alpha 0.4 patch applied. Run GitHub Actions build.')
