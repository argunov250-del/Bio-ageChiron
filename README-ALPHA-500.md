# Bio-Age Alpha 5.00 — UI consolidation

**Baseline:** GitHub main at Alpha 4.00, after the Claude camera/GLB fixes. This is not a standalone APK.

From repository root: `python3 /path/to/bioage_500_full/apply_alpha500.py`

Changes: compact main screen (hero, one route, Chiron), height limit 47%; seven-day health summary; detailed charts and context under expandable section; diagnostic sections collapsed in Profile; versionCode 500. Preserves all SharedPreferences keys, local backups, SceneView camera and GLB. No signing changes.

**Not delivered:** physically realistic rock/snow PBR material update, new 3D GLB, trained AI model, medical biological-age calculation. These need separate development/testing. Do not claim these were included.

Review `git diff`, compile `assembleDebug` or existing signed workflow, and verify UI/backup. Do not commit *.alpha400.bak backups. Script intentionally refuses incompatible baseline instead of silently corrupting code.
