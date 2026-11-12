#!/usr/bin/env python3
from pathlib import Path
import re
p=Path('app/build.gradle.kts')
s=p.read_text()
s,n=re.subn(r'versionCode\s*=\s*\d+', 'versionCode = 6',s,count=1)
if n!=1: raise SystemExit('versionCode not found')
s,n=re.subn(r'versionName\s*=\s*"[^"]+"', 'versionName = "0.6.0-alpha"',s,count=1)
if n!=1: raise SystemExit('versionName not found')
p.write_text(s)
p=Path('app/src/main/java/com/bioage/alpha/MainActivity.kt')
s=p.read_text()
s=s.replace('ALPHA 0.4','ALPHA 0.6').replace('Bio-Age Alpha 0.4','Bio-Age Alpha 0.6')
# Improve readability without hiding the brighter 3D sky.
s=s.replace('Color(0x77051728), Color.Transparent, Color(0xD9081927)', 'Color(0x65051728), Color.Transparent, Color(0xC9081927)')
p.write_text(s)
print('Alpha 0.6 version and labels updated; user data keys preserved')
