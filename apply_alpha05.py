#!/usr/bin/env python3
from pathlib import Path
import re
p=Path('app/build.gradle.kts')
s=p.read_text()
s,n=re.subn(r'versionCode\s*=\s*\d+', 'versionCode = 5',s,count=1)
if n!=1: raise SystemExit('versionCode not found')
s,n=re.subn(r'versionName\s*=\s*"[^"]+"', 'versionName = "0.5.0-alpha"',s,count=1)
if n!=1: raise SystemExit('versionName not found')
p.write_text(s)
print('Updated to Alpha 0.5; existing applicationId preserved')
