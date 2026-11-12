#!/usr/bin/env python3
from pathlib import Path
import re, shutil, sys
base=Path.cwd()
main=base/"app/src/main/java/com/bioage/alpha/MainActivity.kt"
gradle=base/"app/build.gradle.kts"
if not main.exists() or not gradle.exists():
    sys.exit("Run from the Bio-ageChiron repository root")
src=main.read_text(encoding="utf-8")
required=[
    "CaucasusThreeBackground()",
    "Home600(prefs, steps, sleep, done, done + historyRefresh) { chatOpen = true }",
    "ChironChat30(prefs, sleep, steps, mood, done)",
    "Health500(prefs, historyRefresh)",
    "SummitJourney30(prefs, done + historyRefresh)",
]
missing=[s for s in required if s not in src]
if missing:
    sys.exit("Source mismatch; no files changed. Missing: "+repr(missing))
backup=main.with_suffix(".kt.pre_unified_backup")
if not backup.exists(): shutil.copy2(main,backup)
src=src.replace("Home600(prefs, steps, sleep, done, done + historyRefresh) { chatOpen = true }",
    "UnifiedHome(prefs, steps, sleep, done, done + historyRefresh) { chatOpen = true }")
src=src.replace("ChironChat30(prefs, sleep, steps, mood, done)",
    "UnifiedChiron(prefs, steps, sleep, mood, done)")
src=src.replace("Health500(prefs, historyRefresh)",
    "UnifiedHealth(prefs, historyRefresh)")
src=src.replace("SummitJourney30(prefs, done + historyRefresh)",
    "UnifiedProgress(prefs, done + historyRefresh)")
# Remove duplicate legacy displays while preserving data entry, habit editing and backup.
for line in [
    "Goals30(prefs, steps)",
    "ChironCoach200(prefs, historyRefresh)",
    "SummitMilestones50(prefs, done + historyRefresh)",
    "WeeklyFocus70(prefs)",
    "Reports30(prefs, done + historyRefresh)",
]:
    src=src.replace(line,"// Moved into unified experience: "+line)
src=src.replace("КАВКАЗ  /  ALPHA 6.10","КАВКАЗ  /  ALPHA 7.00")
# Larger home card for complete dashboard, still scrollable.
src=src.replace("maxHeight * 0.47f","maxHeight * 0.60f")
g=gradle.read_text(encoding="utf-8")
g,n=re.subn(r"versionCode\s*=\s*\d+", "versionCode = 700",g,count=1)
if n!=1: sys.exit("versionCode not found")
g,n=re.subn(r'versionName\s*=\s*"[^"]+"','versionName = "7.00.0-alpha"',g,count=1)
if n!=1: sys.exit("versionName not found")
dest=main.parent/"UnifiedBetaFeatures.kt"
dest.write_bytes((Path(__file__).parent/"UnifiedBetaFeatures.kt").read_bytes())
main.write_text(src,encoding="utf-8")
gradle.write_text(g,encoding="utf-8")
print("Applied Alpha 7.00 unified features. Existing data keys, signing and Three.js background unchanged.")
