#!/usr/bin/env python3
"""Cumulative Alpha 0.51–0.70 update, applied to repository Alpha 0.50 FULL."""
from pathlib import Path
import sys, shutil
root = Path.cwd()
base = Path(__file__).resolve().parent
folder = root / 'app/src/main/java/com/bioage/alpha'
main = folder / 'MainActivity.kt'
gradle = root / 'app/build.gradle.kts'
feature = folder / 'Alpha70Features.kt'
for p in (main, gradle, folder / 'Alpha50FullFeatures.kt'):
    if not p.is_file(): sys.exit(f'Missing {p}. Run from the repository root after Alpha 0.50 FULL.')
s = main.read_text(encoding='utf-8')
g = gradle.read_text(encoding='utf-8')
if 'ALPHA 0.70' in s and 'versionCode = 70' in g and feature.is_file():
    print('Alpha 0.70 already applied; no changes.'); sys.exit(0)
if 'ALPHA 0.50' not in s or 'versionCode = 50' not in g or feature.exists():
    sys.exit('Expected clean Alpha 0.50 FULL. No files changed.')
def replace_once(source, old, new):
    count = source.count(old)
    if count != 1: raise ValueError(f'Expected 1 match, found {count}: {old[:100]!r}. No files changed.')
    return source.replace(old, new, 1)
try:
    s = replace_once(s, 'КАВКАЗ  /  ALPHA 0.50', 'КАВКАЗ  /  ALPHA 0.70')
    s = replace_once(s, 'HomeSummit50(prefs, done + historyRefresh)\n                            SceneInfo50()',
        'HomeSummit50(prefs, done + historyRefresh)\n                            TrailRhythm70(prefs, done + historyRefresh)\n                            SceneInfo50()')
    s = replace_once(s, 'HealthSummary50(prefs, historyRefresh)\n                            Recovery30(prefs, historyRefresh)',
        'HealthSummary50(prefs, historyRefresh)\n                            DataCoverage70(prefs, historyRefresh)\n                            Recovery30(prefs, historyRefresh)')
    s = replace_once(s, 'ChironInsights50(prefs, historyRefresh)\n                            WeeklyReflection50(prefs)',
        'ChironInsights50(prefs, historyRefresh)\n                            GentleMode70(prefs)\n                            ChironContext70(prefs, historyRefresh)\n                            WeeklyReflection50(prefs)')
    s = replace_once(s, 'SummitMilestones50(prefs, done + historyRefresh)\n                            Reports30(prefs, done + historyRefresh)',
        'SummitMilestones50(prefs, done + historyRefresh)\n                            WeeklyFocus70(prefs)\n                            Reports30(prefs, done + historyRefresh)')
    s = replace_once(s, 'Bio-Age Alpha 0.50', 'Bio-Age Alpha 0.70')
    s = replace_once(s, 'DataAudit50(prefs)', 'DataAudit50(prefs)\n                            ReleaseNotes70()')
    g = replace_once(g, 'versionCode = 50; versionName = "0.50.0-alpha"', 'versionCode = 70; versionName = "0.70.0-alpha"')
except ValueError as e: sys.exit(str(e))
# Validate all anchors before modifying any file.
main.write_text(s, encoding='utf-8')
gradle.write_text(g, encoding='utf-8')
shutil.copyfile(base / 'Alpha70Features.kt', feature)
print('Applied Alpha 0.70. Run ./gradlew :app:assembleDebug and perform on-device QA.')
