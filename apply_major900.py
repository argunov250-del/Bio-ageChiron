#!/usr/bin/env python3
from pathlib import Path
import re, shutil, sys
base=Path.cwd()
main=base/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
gradle=base/'app/build.gradle.kts'
if not main.is_file() or not gradle.is_file(): sys.exit('Запусти из корня репозитория Bio-ageChiron')
s=main.read_text(encoding='utf-8')
replacements={
    'UnifiedHome(prefs, steps, sleep, done, done + historyRefresh) { chatOpen = true }':'Major9Home(prefs, steps, sleep, done) { chatOpen = true }',
    'UnifiedHealth(prefs, historyRefresh)':'Major9Health(prefs, historyRefresh)',
    'UnifiedProgress(prefs, done + historyRefresh)':'Major9Progress(prefs, done + historyRefresh)',
    'КАВКАЗ  /  ALPHA 8.00':'КАВКАЗ  /  VISUAL 9.00',
    'Bio-Age Alpha 8.00':'Bio-Age Visual 9.00',
    'maxHeight * 0.60f':'maxHeight * 0.46f',
}
missing=[k for k in replacements if k not in s]
if missing: sys.exit('Несовместимая версия MainActivity, ничего не изменено: '+repr(missing))
for a,b in replacements.items(): s=s.replace(a,b)
# Remove old stacked visual duplicates but retain all original editing controls and history on demand.
s=s.replace('Wellness620(prefs, historyRefresh)','// Major 9: old duplicated wellness block hidden')
s=s.replace('Goals30(prefs, steps)  // kept: the only place to edit the step goal','Goals30(prefs, steps)')
# Keep diagnostic history under an expandable button.
# The actual Three.js remains in the project; a scenic fallback is drawn below the WebView.
s=s.replace('CaucasusThreeBackground()','AlpineFallback9()\n            CaucasusThreeBackground()')
# Make main card translucent and improve text legibility.
s=s.replace('Color(0xD9163044)','Color(0xC9163044)')
s=s.replace('Color(0x48051728), Color.Transparent, Color(0xB9081927)','Color(0x38051728), Color.Transparent, Color(0xB9081927)')
g=gradle.read_text(encoding='utf-8')
if not re.search(r'versionCode\s*=\s*\d+',g) or not re.search(r'versionName\s*=\s*"[^"]+"',g): sys.exit('Не найден номер версии')
g=re.sub(r'versionCode\s*=\s*\d+','versionCode = 900',g,count=1)
g=re.sub(r'versionName\s*=\s*"[^"]+"','versionName = "9.00.0-alpha"',g,count=1)
# Ensure the mountains are visible even on tall phone screens: a lightweight
# SVG snow-ridge overlay is always present over the Three.js canvas.
html=base/'app/src/main/assets/three_caucasus/index.html'
if not html.is_file(): sys.exit('Не найден Three.js index.html')
h=html.read_text(encoding='utf-8')
if '<div id="haze"></div>' not in h: sys.exit('Не найден якорь сцены Three.js')
mountain='<svg id="alpine-ridge" viewBox="0 0 1000 900" preserveAspectRatio="xMidYMid slice" style="position:absolute;left:0;top:18%;width:100%;height:76%;pointer-events:none;opacity:.94" xmlns="http://www.w3.org/2000/svg"><defs><linearGradient id="snow" x2="0" y2="1"><stop stop-color="#f5f8f6"/><stop offset=".56" stop-color="#b2c8d0"/><stop offset="1" stop-color="#537488"/></linearGradient><linearGradient id="fog" x2="0" y2="1"><stop stop-color="#91b1bd" stop-opacity="0"/><stop offset="1" stop-color="#122f42" stop-opacity=".8"/></linearGradient></defs><path fill="#a3bdc9" d="M0 620L85 530 150 560 255 400 345 535 480 320 610 520 735 355 875 540 1000 445V900H0Z"/><path fill="url(#snow)" d="M0 750L130 625 210 670 355 360 415 490 520 585 650 305 715 435 790 515 905 400 1000 570V900H0Z"/><path fill="#e9f0ef" d="M280 465L355 360 415 490 374 459 351 438 330 476Z M590 420L650 305 715 435 672 408 647 380 625 427Z"/><path fill="#456b7e" d="M0 770L130 700 270 775 430 685 550 790 710 685 850 750 1000 680V900H0Z"/><path fill="url(#fog)" d="M0 550H1000V900H0Z"/></svg>'
h=h.replace('<div id="haze"></div>','<div id="haze"></div>'+mountain)
# SVG is a reliable visual fallback while the procedural 3D relief remains active.
# Validate all changes before touching files.
assert 'Major9Home(' in s and 'Major9Health(' in s and 'Major9Progress(' in s
shutil.copy2(main,main.with_suffix('.kt.pre_major9'))
main.write_text(s,encoding='utf-8')
gradle.write_text(g,encoding='utf-8')
html.write_text(h,encoding='utf-8')
for file in ['Major9UI.kt','AlpineFallback9.kt']:
    shutil.copy2(Path(__file__).parent/file,main.parent/file)
print('Major 9 applied. Build and test APK; preserve signing key and app data.')
