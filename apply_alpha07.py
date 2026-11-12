#!/usr/bin/env python3
from pathlib import Path
import re
root=Path(__file__).resolve().parent
build=root/'app/build.gradle.kts'
s=build.read_text(encoding='utf-8')
s,n=re.subn(r'versionCode\s*=\s*\d+', 'versionCode = 7',s,count=1)
if n!=1: raise SystemExit('versionCode not found')
s,n=re.subn(r'versionName\s*=\s*"[^"]+"', 'versionName = "0.7.0-alpha"',s,count=1)
if n!=1: raise SystemExit('versionName not found')
build.write_text(s,encoding='utf-8')
p=root/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
s=p.read_text(encoding='utf-8')
s,n=re.subn(r'КАВКАЗ\s*/\s*ALPHA 0\.\d+', 'КАВКАЗ  /  ALPHA 0.7',s)
if n<1: raise SystemExit('Header version not found')
s,n2=re.subn(r'Bio-Age Alpha 0\.\d+', 'Bio-Age Alpha 0.7',s)
if n2<1: raise SystemExit('Profile version not found')
# Make the camera slider thumb/track standard and predictable.
s=s.replace('Slider(value = camera, onValueChange = { camera = it }, valueRange = -45f..45f, modifier = Modifier.fillMaxWidth())', 'Slider(value = camera, onValueChange = { camera = it }, valueRange = -45f..45f, modifier = Modifier.fillMaxWidth(), colors = SliderDefaults.colors(thumbColor = Aqua, activeTrackColor = Aqua, inactiveTrackColor = Color(0xFF496677)))')
p.write_text(s,encoding='utf-8')
print('Alpha 0.7 applied; no changes to app ID, health data, or signing workflow')
