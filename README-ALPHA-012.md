# Bio-Age Alpha 0.12 — cumulative source patch

**This is Alpha, not Beta. No APK has been compiled in this environment.**

This package includes the cumulative Alpha 0.8/0.9 GLB terrain, and implements:

- **0.10**: more consistent camera slider colors and retained smooth SceneView camera orbit. No claims of physically accurate lighting; 3D sky remains part of the GLB.
- **0.11**: 7-day dated history of manually saved steps and sleep; new conservative, rule-based Chiron text that considers optional mood and completed habits.
- **0.12**: offline JSON export/import using Android's system file picker, preserving existing SharedPreferences keys; `versionCode=12`, `versionName=0.12.0-alpha`, UI labels updated.

## Apply

1. In the existing Bio-ageChiron repository root (Alpha 0.7 or later), extract this ZIP to a temporary folder, not directly over the project.
2. Run `python3 /path/to/extracted/apply_alpha012.py` from repository root.
3. Inspect `git diff`, commit, push, build via existing GitHub Actions. Keep existing signing secrets and workflow.
4. Verify on Android: date history, Chiron, export, import, slider, version and upgrade-in-place.

**Caveats:** This is a code patch, not a verified APK. The seven-day chart only contains records saved after this update; earlier undated steps/sleep cannot be reconstructed. Imported data requires app restart to refresh remembered UI values. Import overwrites matching keys, so export first. The GLB is a stylized terrain, not geodetic Elbrus data. No cloud, Health Connect, automatic sync or diagnosis is implemented. No new signing key is included; the same signing certificate is necessary for upgrade-in-place.
