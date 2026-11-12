# Bio-Age Chiron Alpha 6.20 — Chiron energy & honest wellness

Base: repository Alpha 6.10 (`MainActivity.kt` contains ALPHA 6.10, Gradle versionCode 610).

## Apply
From repository root: `python3 /path/to/bioage_620/apply.py`

## Included
- Abstract animated Chiron energy entity (Jetpack Compose Canvas, offline, no human avatar).
- Existing Chiron chat retained below the energy presence.
- Daily wellness summary of actual saved sleep, steps and habits, explicitly no fabricated HRV/recovery/strain scores.
- No changes to Three.js background, single automatic camera, signatures, package ID, or SharedPreferences keys.
- `versionCode 620`, `versionName 6.20.0-alpha`.

## QA
Compile with existing GitHub Actions signed APK. Verify Chiron screen, chat keyboard, Health summary after saving data, all tabs, smooth camera, missing-data states, backup/export. On low-end phones check energy animation performance. Do not change signing configuration or uninstall app.

## Limits
Not all 160+ original requirements are implemented. No connected wearable, real HRV, validated biological-age estimate, or trained LLM in this patch. Android build not executed locally.
