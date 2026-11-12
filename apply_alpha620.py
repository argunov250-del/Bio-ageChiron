#!/usr/bin/env python3
"""Safe, anchored Alpha 6.20 feature patch. Run from repo root: python3 /path/to/apply.py"""
from pathlib import Path
import shutil
root = Path.cwd()
main = root / 'app/src/main/java/com/bioage/alpha/MainActivity.kt'
gradle = root / 'app/build.gradle.kts'
source = Path(__file__).resolve().parent / 'app/src/main/java/com/bioage/alpha/Chiron620.kt'
assert main.exists() and gradle.exists(), 'Run from Bio-Age repository root'
s = main.read_text()
g = gradle.read_text()
assert 'ALPHA 6.10' in s and 'Home600(' in s, 'Expected Alpha 6.10 baseline; stop rather than overwrite newer code'
assert 'versionCode = 610' in g, 'Expected versionCode 610; stop rather than overwrite newer code'
assert 'ChironPresence620(' not in s, 'Patch already applied'
old = '                                ChironChat30(prefs, sleep, steps, mood, done)'
assert s.count(old) == 1, 'Chat anchor changed'
s = s.replace(old, '                                ChironPresence620(prefs, historyRefresh)\n                                Spacer(Modifier.height(16.dp))\n' + old)
old = '                            Health500(prefs, historyRefresh)'
assert s.count(old) == 1, 'Health anchor changed'
s = s.replace(old, '                            Wellness620(prefs, historyRefresh)\n                            Spacer(Modifier.height(12.dp))\n' + old)
s = s.replace('ALPHA 6.10', 'ALPHA 6.20').replace('Bio-Age Alpha 6.10', 'Bio-Age Alpha 6.20')
g = g.replace('versionCode = 610', 'versionCode = 620').replace('6.10.0-alpha', '6.20.0-alpha')
# All preconditions checked before writing. Backups stay in the repo, never user data.
backup = root / '.alpha620_backup'
backup.mkdir(exist_ok=True)
shutil.copy2(main, backup / 'MainActivity.kt')
shutil.copy2(gradle, backup / 'build.gradle.kts')
shutil.copy2(source, root / 'app/src/main/java/com/bioage/alpha/Chiron620.kt')
main.write_text(s)
gradle.write_text(g)
print('Alpha 6.20 applied. Compile and verify on Android before release.')
