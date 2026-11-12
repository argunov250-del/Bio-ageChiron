#!/usr/bin/env python3
"""Non-destructive source patch: Alpha 1.00 -> Alpha 2.00.
Also accepts Alpha 0.70 by first applying the bundled 1.00 patch.
Run from repository root. Requires Python 3.
"""
from pathlib import Path
import os, shutil, subprocess, sys, tempfile
root = Path.cwd()
base = Path(__file__).resolve().parent
folder = root / 'app/src/main/java/com/bioage/alpha'
main = folder / 'MainActivity.kt'
gradle = root / 'app/build.gradle.kts'
feature = folder / 'Alpha200Features.kt'
if not main.exists() or not gradle.exists():
    sys.exit('Run from repository root.')
s = main.read_text(encoding='utf-8')
g = gradle.read_text(encoding='utf-8')
if 'ALPHA 2.00' in s and 'versionCode = 200' in g and feature.exists():
    print('Already applied'); sys.exit(0)
if 'ALPHA 0.70' in s and 'versionCode = 70' in g:
    # Stage the original 1.00 bundle under the path its script expects.
    stage = base / 'bioage_100_full'
    stage.mkdir(exist_ok=True)
    for filename in ('Alpha100Features.kt', 'apply_alpha100_full.py'):
        shutil.copy2(base / filename, stage / filename)
    result = subprocess.run([sys.executable, str(stage / 'apply_alpha100_full.py')], cwd=root)
    if result.returncode: sys.exit('Alpha 1.00 prerequisite failed; Alpha 2.00 not applied.')
    s = main.read_text(encoding='utf-8')
    g = gradle.read_text(encoding='utf-8')
if 'ALPHA 1.00' not in s or 'versionCode = 100' not in g or not (folder / 'Alpha100Features.kt').exists():
    sys.exit('Expected Alpha 1.00 (or Alpha 0.70). No Alpha 2.00 files changed.')
if feature.exists(): sys.exit('Alpha200Features.kt exists unexpectedly; no changes made.')
def once(source, old, new):
    count = source.count(old)
    if count != 1: raise ValueError(f'Expected one anchor, found {count}: {old[:80]}')
    return source.replace(old, new, 1)
try:
    s = once(s, 'КАВКАЗ  /  ALPHA 1.00', 'КАВКАЗ  /  ALPHA 2.00')
    s = once(s, 'Bio-Age Alpha 1.00', 'Bio-Age Alpha 2.00')
    s = once(s, 'SummitDashboard100(prefs, done + historyRefresh)', 'SummitRoute200(prefs, done + historyRefresh)')
    s = once(s, 'HealthDashboard100(prefs, historyRefresh)', 'HealthDashboard100(prefs, historyRefresh)\n                            SevenDayChart200(prefs, historyRefresh)\n                            CompareWeeks200(prefs, historyRefresh)')
    s = once(s, 'ChironExplain100(prefs, historyRefresh)', 'ChironCoach200(prefs, historyRefresh)')
    s = once(s, 'ReleaseStatus100(prefs)', 'ReleaseStatus100(prefs)\n                            PrivacyAndQuality200(prefs)')
    # Remove one redundant main-card progress block: SummitRoute200 replaces it.
    s = once(s, 'TrailRhythm70(prefs, done + historyRefresh)', '/* Rhythm included in SummitRoute200 */')
    g = once(g, 'versionCode = 100; versionName = "1.00.0-alpha"', 'versionCode = 200; versionName = "2.00.0-alpha"')
except ValueError as e:
    sys.exit(f'Patch refused: {e}. No Alpha 2.00 files changed.')
for path in (main, gradle):
    shutil.copy2(path, path.with_suffix(path.suffix + '.alpha100.bak'))
def atomic(path, content):
    with tempfile.NamedTemporaryFile(mode='w', encoding='utf-8', dir=path.parent, delete=False) as f:
        f.write(content); tmp = f.name
    os.replace(tmp, path)
atomic(main, s)
atomic(gradle, g)
shutil.copy2(base / 'Alpha200Features.kt', feature)
print('Alpha 2.00 sources applied. Build APK and test on device; no Android build was run by this script.')
