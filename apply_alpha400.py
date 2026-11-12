#!/usr/bin/env python3
"""Apply Alpha 4.00 on the existing Bio-Age Android project; non-destructive."""
from pathlib import Path
import re, shutil, sys
root=Path.cwd(); here=Path(__file__).resolve().parent
src=root/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
gradle=root/'app/build.gradle.kts'
asset=root/'app/src/main/assets/models/elbrus_terrain.glb'
if not src.is_file() or not gradle.is_file() or not (root/'settings.gradle.kts').is_file():
    sys.exit('ERROR: run this script from the Bio-ageChiron repository root')
s=src.read_text(encoding='utf-8')
if 'AlpineRoute400(prefs' not in s:
    old='SummitRoute200(prefs, done + historyRefresh)'
    if old not in s: sys.exit('ERROR: expected Alpha 2.00 route anchor not found; no changes made')
    s=s.replace(old,'AlpineRoute400(prefs, done + historyRefresh)',1)
    # Keep the rest of the working screen unchanged.
    s=s.replace('SceneInfo50()','MountainInfo400()',1)
s=re.sub(r'ALPHA (?:2\.00|3\.00|4\.00)', 'ALPHA 4.00',s)
s=s.replace('Bio-Age Alpha 2.00','Bio-Age Alpha 4.00').replace('Bio-Age Alpha 3.00','Bio-Age Alpha 4.00')
g=gradle.read_text(encoding='utf-8')
g,n=re.subn(r'versionCode\s*=\s*\d+','versionCode = 400',g,count=1)
if n!=1:sys.exit('ERROR: versionCode not found')
g,n=re.subn(r'versionName\s*=\s*"[^"]+"','versionName = "4.00.0-alpha"',g,count=1)
if n!=1:sys.exit('ERROR: versionName not found')
# Backup exact previous files, once.
backup=root/'.alpha400_backup';backup.mkdir(exist_ok=True)
for p in (src,gradle,asset):
    if p.exists():
        target=backup/p.relative_to(root);target.parent.mkdir(parents=True,exist_ok=True)
        if not target.exists():shutil.copy2(p,target)
src.write_text(s,encoding='utf-8');gradle.write_text(g,encoding='utf-8')
shutil.copy2(here/'Alpha400Features.kt',src.parent/'Alpha400Features.kt')
asset.parent.mkdir(parents=True,exist_ok=True)
shutil.copy2(here/'assets/models/elbrus_terrain.glb',asset)
print('Alpha 4.00 applied. Backup in .alpha400_backup. Build and test APK before release.')
