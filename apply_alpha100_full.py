#!/usr/bin/env python3
"""Apply Alpha 1.00 changes to the exact Alpha 0.70 repository baseline.
No destructive migrations. Atomic replacement of text files after validation.
"""
from pathlib import Path
import sys, os, shutil, tempfile
root = Path.cwd()
base = Path(__file__).resolve().parent
folder = root / 'app/src/main/java/com/bioage/alpha'
main = folder / 'MainActivity.kt'
gradle = root / 'app/build.gradle.kts'
feature = folder / 'Alpha100Features.kt'
if not all(p.is_file() for p in (main, gradle, folder/'Alpha70Features.kt')):
    sys.exit('Run from the Bio-Age repository root with Alpha 0.70 installed.')
s = main.read_text(encoding='utf-8')
g = gradle.read_text(encoding='utf-8')
if 'ALPHA 1.00' in s and 'versionCode = 100' in g and feature.exists():
    print('Already applied.'); sys.exit(0)
if 'ALPHA 0.70' not in s or 'versionCode = 70' not in g or feature.exists():
    sys.exit('Expected Alpha 0.70 baseline. No files changed.')
def once(src, old, new):
    n = src.count(old)
    if n != 1: raise ValueError(f'Expected one anchor, got {n}: {old[:85]!r}')
    return src.replace(old,new,1)
try:
    s = once(s, 'КАВКАЗ  /  ALPHA 0.70', 'КАВКАЗ  /  ALPHA 1.00')
    s = once(s, 'Bio-Age Alpha 0.70', 'Bio-Age Alpha 1.00')
    # The existing 3D model and orbit remain intact. Do not duplicate summit bars.
    s = once(s, 'HomeSummit50(prefs, done + historyRefresh)\n                            TrailRhythm70(prefs, done + historyRefresh)',
             'SummitDashboard100(prefs, done + historyRefresh)\n                            TrailRhythm70(prefs, done + historyRefresh)')
    s = once(s, 'HealthSummary50(prefs, historyRefresh)\n                            DataCoverage70(prefs, historyRefresh)',
             'HealthDashboard100(prefs, historyRefresh)\n                            DataCoverage70(prefs, historyRefresh)')
    s = once(s, 'ChironContext70(prefs, historyRefresh)\n                            WeeklyReflection50(prefs)',
             'ChironContext70(prefs, historyRefresh)\n                            ChironExplain100(prefs, historyRefresh)\n                            WeeklyReflection50(prefs)')
    # Compact mode actually controls whether extended recovery/goal panels appear.
    s = once(s, 'Recovery30(prefs, historyRefresh)\n                            Goals30(prefs, steps)',
             'if (!prefs.getBoolean("compact_health_100", true)) {\n                                Recovery30(prefs, historyRefresh)\n                            }\n                            Goals30(prefs, steps)')
    s = once(s, 'SummitJourney30(prefs, done + historyRefresh)\n                            SummitMilestones50(prefs, done + historyRefresh)',
             'SummitJourney30(prefs, done + historyRefresh)\n                            SummitMilestones50(prefs, done + historyRefresh)') if False else s
    s = once(s, 'DataAudit50(prefs)\n                            ReleaseNotes70()',
             'DataAudit50(prefs)\n                            CompactMode100(prefs)\n                            ReleaseStatus100(prefs)\n                            ReleaseNotes70()')
    g = once(g, 'versionCode = 70; versionName = "0.70.0-alpha"',
             'versionCode = 100; versionName = "1.00.0-alpha"')
except ValueError as e:
    sys.exit(f'Patch refused: {e}. No files changed.')
# stage all files first, then install; backups permit recovery from an interrupted write.
for p in (main, gradle):
    shutil.copy2(p, p.with_suffix(p.suffix+'.alpha070.bak'))
def atomic(path, content):
    with tempfile.NamedTemporaryFile(mode='w', encoding='utf-8', dir=path.parent, delete=False) as tmp:
        tmp.write(content); name = tmp.name
    os.replace(name, path)
atomic(main, s)
atomic(gradle, g)
shutil.copy2(base/'Alpha100Features.kt', feature)
print('Alpha 1.00 source patch applied. Run ./gradlew :app:assembleDebug and device QA.')
