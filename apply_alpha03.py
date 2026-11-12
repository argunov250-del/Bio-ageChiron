from pathlib import Path

main = Path('app/src/main/java/com/bioage/alpha/MainActivity.kt')
gradle = Path('app/build.gradle.kts')
scene = Path('app/src/main/java/com/bioage/alpha/Caucasus3DScene.kt')
asset = Path('app/src/main/assets/models/elbrus_terrain.glb')
for p in (main, gradle, scene, asset):
    if not p.is_file():
        raise SystemExit(f'Missing file: {p}. Run from the repository root and copy patch files first.')
s = main.read_text()
if 'Caucasus3DScene(camera)' not in s:
    if 'AlpineScene(camera)' not in s:
        raise SystemExit('Expected AlpineScene(camera) call not found; refusing to alter unknown code.')
    s = s.replace('AlpineScene(camera)', 'Caucasus3DScene(camera)', 1)
    s = s.replace('КАВКАЗ  /  ALPHA 0.2', 'КАВКАЗ  /  ALPHA 0.3')
    s = s.replace('Bio-Age Alpha 0.2', 'Bio-Age Alpha 0.3')
    s = s.replace('Панорама — процедурная 2D-сцена, а не 3D-модель.', 'Панорама — настоящая 3D-модель рельефа на Filament (стилизованная, не геодезическая).')
    main.write_text(s)
g = gradle.read_text()
if 'io.github.sceneview:sceneview' not in g:
    needle = 'dependencies {'
    if needle not in g: raise SystemExit('Cannot find dependencies block')
    g = g.replace(needle, needle + '\n    implementation("io.github.sceneview:sceneview:2.3.3")', 1)
g = g.replace('versionCode = 2', 'versionCode = 3').replace('versionName = "0.2.0-alpha"', 'versionName = "0.3.0-alpha"')
gradle.write_text(g)
print('Alpha 0.3 patch applied. Run ./gradlew :app:assembleDebug and fix any dependency/API issues.')
