#!/usr/bin/env python3
"""Apply additive Alpha 3.00 UI upgrade to checked-out Bio-Age Chiron repo."""
from pathlib import Path
import re, shutil, sys
root = Path.cwd()
base = root/'app/src/main/java/com/bioage/alpha'
main = base/'MainActivity.kt'
gradle = root/'app/build.gradle.kts'
if not main.exists() or not gradle.exists():
    sys.exit('ERROR: Run from Bio-ageChiron repository root.')
src = main.read_text(encoding='utf-8')
if 'ALPHA 3.00' in src:
    sys.exit('Alpha 3.00 already installed; no changes.')
required = ['Text("Твой путь к вершине"', 'SummitRoute200(prefs, done + historyRefresh)', 'Text("Здоровье",', 'Text("Bio-Age Alpha 2.00"']
missing = [x for x in required if x not in src]
if missing:
    sys.exit('ERROR: Unexpected MainActivity version, no changes applied. Missing: '+str(missing))
# Modify only stable, previously verified Alpha 2.00 UI anchors.
src = src.replace('КАВКАЗ  /  ALPHA 2.00', 'КАВКАЗ  /  ALPHA 3.00')
start = src.index('                            Text("Твой путь к вершине"')
end = src.index('                            }\n                        }\n                        1 -> {', start)
src = src[:start] + '''                            Hero300(steps, sleep, done)
                            Spacer(Modifier.height(12.dp))
                            OrbitControl30(camera) { camera = it }
                            Spacer(Modifier.height(12.dp))
                            SummitCard300(prefs, done + historyRefresh)
                            if (!prefs.getBoolean("visual_compact_300", false)) {
                                Spacer(Modifier.height(10.dp))
                                ChironPreview300(prefs, sleep, steps, done) { chatOpen = true }
                            } else {
                                OutlinedButton(onClick = { chatOpen = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Открыть диалог с Хироном")
                                }
                            }
''' + src[end:]
src = src.replace('                            HealthDashboard100(prefs, historyRefresh)', '                            HealthSnapshot300(prefs, historyRefresh)\n                            HealthDashboard100(prefs, historyRefresh)')
src = src.replace('Text("Bio-Age Alpha 2.00"', 'Text("Bio-Age Alpha 3.00"')
src = src.replace('                            CompactMode100(prefs)', '                            CompactMode100(prefs)\n                            VisualSettings300(prefs)')
# Increase contrast, soften bottom overlay, keep mountain panorama unobstructed.
src = src.replace('private val Glass = Color(0xDF102D40)', 'private val Glass = Color(0xD9163044)')
src = src.replace('Modifier.heightIn(max = maxHeight * 0.59f)', 'Modifier.heightIn(max = maxHeight * 0.56f)')
src = src.replace('Color(0x65051728), Color.Transparent, Color(0xC9081927)', 'Color(0x48051728), Color.Transparent, Color(0xB9081927)')
g = gradle.read_text(encoding='utf-8')
if 'versionCode = 200' not in g or 'versionName = "2.00.0-alpha"' not in g:
    sys.exit('ERROR: Expected Alpha 2.00 Gradle version; no changes applied.')
g = g.replace('versionCode = 200', 'versionCode = 300').replace('versionName = "2.00.0-alpha"', 'versionName = "3.00.0-alpha"')
shutil.copy2(main, main.with_suffix('.kt.pre300.bak'))
shutil.copy2(gradle, gradle.with_suffix('.kts.pre300.bak'))
main.write_text(src, encoding='utf-8')
gradle.write_text(g, encoding='utf-8')
shutil.copy2(Path(__file__).parent/'Alpha300Design.kt', base/'Alpha300Design.kt')
print('Alpha 3.00 installed. Build and test APK; pre300 backups retained.')
