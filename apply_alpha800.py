from pathlib import Path
import re, shutil, sys
root=Path.cwd()
main=root/'app/src/main/java/com/bioage/alpha/MainActivity.kt'
gradle=root/'app/build.gradle.kts'
if not main.exists() or not gradle.exists():sys.exit('Run from repository root')
s=main.read_text()
anchor='LocalBackupPanel(prefs)'
if anchor not in s:sys.exit('Unsupported MainActivity: backup anchor missing')
if 'ChironDataCenter(prefs)' in s:sys.exit('Already installed')
dest=main.parent
for name in ['AppleHealthEngine.kt','ChironImportUI.kt']:
    if (dest/name).exists():sys.exit('Destination already exists: '+name)
shutil.copy2(main,main.with_suffix('.kt.before_import'))
s=s.replace(anchor,'ChironDataCenter(prefs)\n                            '+anchor,1)
main.write_text(s)
for name in ['AppleHealthEngine.kt','ChironImportUI.kt']:
    shutil.copy2(Path(__file__).parent/name,dest/name)
g=gradle.read_text()
g=re.sub(r'versionCode\s*=\s*\d+','versionCode = 800',g,count=1)
g=re.sub(r'versionName\s*=\s*"[^"]+"','versionName = "8.00.0-alpha"',g,count=1)
gradle.write_text(g)
print('Applied import engine; run Gradle build and tests before shipping')
