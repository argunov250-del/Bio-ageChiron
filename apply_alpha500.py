#!/usr/bin/env python3
"""Apply Alpha 5.00 to verified Alpha 4.00 checkout; fail closed on mismatches."""
from pathlib import Path
import re, shutil, sys
root = Path.cwd()
base = root/'app/src/main/java/com/bioage/alpha'
main = base/'MainActivity.kt'
gradle = root/'app/build.gradle.kts'
if not main.exists() or not gradle.exists():
    sys.exit('Run from repository root')
s = main.read_text()
g = gradle.read_text()
if 'ALPHA 5.00' in s:
    sys.exit('Alpha 5.00 already applied')
anchors = ['ALPHA 4.00', 'Hero300(steps, sleep, done)', 'AlpineRoute400(prefs, done + historyRefresh)', 'HealthSnapshot300(prefs, historyRefresh)', 'LocalBackupPanel(prefs)', 'Diagnostics30(prefs)', 'PrivacyAndQuality200(prefs)']
missing = [a for a in anchors if a not in s]
if missing or 'versionCode = 400' not in g:
    sys.exit('Unsupported baseline. Missing: '+str(missing)+'; requires Alpha 4.00. No files changed.')
# Full home content block replacement with exact markers, preserving chat branch.
start = s.index('                            Hero300(steps, sleep, done)')
end = s.index('\n                            }\n                        }\n                        1 -> {', start)
s = s[:start] + '''                            Home500(prefs, steps, sleep, done, done + historyRefresh) { chatOpen = true }''' + s[end:]
# Reduce home panel so mountains remain the hero; leave all other tabs full height.
s = s.replace('maxHeight * 0.56f', 'maxHeight * 0.47f', 1)
s = s.replace('ALPHA 4.00', 'ALPHA 5.00', 1)
s = s.replace('HealthSnapshot300(prefs, historyRefresh)', 'Health500(prefs, historyRefresh)', 1)
# Consolidate redundant health sections behind existing disclosure.
start = s.index('                            HealthDashboard100(prefs, historyRefresh)')
end = s.index('                            Spacer(Modifier.height(12.dp))\n                            OutlinedButton(onClick = { healthExpanded', start)
s = s[:start] + '''                            Goals30(prefs, steps)
                            Spacer(Modifier.height(10.dp))
                            ChironCoach200(prefs, historyRefresh)
''' + s[end:]
s = s.replace('''                                HealthHistoryPanel(prefs, historyRefresh)''','''                                SevenDayChart200(prefs, historyRefresh)
                                CompareWeeks200(prefs, historyRefresh)
                                DataCoverage70(prefs, historyRefresh)
                                Recovery30(prefs, historyRefresh)
                                ChironInsights50(prefs, historyRefresh)
                                GentleMode70(prefs)
                                ChironContext70(prefs, historyRefresh)
                                WeeklyReflection50(prefs)
                                HealthHistoryPanel(prefs, historyRefresh)''',1)
# Profile: keep backup directly accessible, put diagnostics in expandable panel.
start=s.index('                            Diagnostics30(prefs)')
end=s.index('                            Spacer(Modifier.height(12.dp))',start)
s=s[:start]+'                            ProfileDiagnostics500(prefs)\n'+s[end:]
s=s.replace('Bio-Age Alpha 4.00','Bio-Age Alpha 5.00',1)
g=g.replace('versionCode = 400; versionName = "4.00.0-alpha"','versionCode = 500; versionName = "5.00.0-alpha"',1)
# Prepare edits only after all checks; backups are kept outside Git.
shutil.copy2(main, main.with_suffix('.kt.alpha400.bak'))
shutil.copy2(gradle, gradle.with_suffix('.kts.alpha400.bak'))
main.write_text(s)
gradle.write_text(g)
shutil.copy2(Path(__file__).with_name('Alpha500Features.kt'), base/'Alpha500Features.kt')
print('Alpha 5.00 applied. Review diff and compile APK. GLB is intentionally unchanged.')
