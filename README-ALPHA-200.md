# Bio-Age Chiron Alpha 2.00 — cumulative source patch

## Baseline
Supports repository source Alpha 0.70 (applies bundled 1.00 prerequisite automatically) or Alpha 1.00. Do not apply old partial Alpha 0.50 patches. If repository is already 2.00, script is idempotent.

## Installation
From **repository root** after extracting ZIP:

```bash
python3 /path/to/bioage_200_full/apply_alpha200_full.py
./gradlew :app:assembleDebug
```

The script checks exact anchors before writing 2.00 files, makes `.alpha100.bak` backups of edited text files, and does not touch SharedPreferences, package ID, signing, or GLB assets. If upgrading from 0.70, the bundled 1.00 script also creates `.alpha070.bak` backups.

## Actual implemented scope
- 1.01–1.20: 7-day sleep/step bar chart with missing-day gaps.
- 1.21–1.40: two-week comparisons with minimum 3 recorded days in each week.
- 1.41–1.60: offline, evidence-limited Chiron recommendations and persistent recovery focus.
- 1.61–1.80: unified summit progress, daily streak, next milestone; redundant main progress removed.
- 1.81–2.00: local data audit, coverage and backup reminder.
- Version code 200; version name 2.00.0-alpha.

## Important limitations
This is a **source-code Alpha milestone**, not a verified release. The Kotlin/Android project has NOT been compiled in this environment. The existing 3D model and shaders have NOT been replaced; no claim of realistic 3D improvement, Health Connect, trained AI model, or medical biological-age estimation. The source patch does not make an APK by itself. Claude should run a build, fix compilation issues, and perform device QA before distributing an APK.

## Device QA
1. Upgrade without uninstalling; confirm existing steps, sleep, habits and notes remain.
2. Check main scroll, tab navigation, summit progress and next milestone.
3. Check health chart empty state and 1/3/7-day samples; no false zero bars.
4. Check two-week comparison only activates with enough data.
5. Toggle recovery focus, restart, verify persistence.
6. Check Profile data audit and existing JSON backup.
7. Test back navigation and performance on target Android device.

Do not claim Alpha 2.00 is production-stable until testing passes.
