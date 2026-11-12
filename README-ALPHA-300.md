# Bio-Age Chiron Alpha 3.00 — visual redesign

**Base:** GitHub main Alpha 2.00 (`versionCode 200`). Do not apply Alpha 1.00 or 2.00 patches again.

## Install
1. Extract this ZIP.
2. In a checked-out `argunov250-del/Bio-ageChiron` repository, run:
   `python3 /path/to/bioage_300_full/apply_alpha300.py`
3. Review `git diff`; commit `Alpha300Design.kt` and modified MainActivity / Gradle. Do not commit `.pre300.bak` files.
4. Build `./gradlew assembleDebug` or existing GitHub Actions workflow.
5. Install APK **without uninstalling** existing app; retain package/signing key.

## Delivered
- New three-metric glass hero, glass summit card and Chiron preview.
- More restrained overlays to reveal 3D mountain; orbit controls retained.
- Compact 7-day health snapshot, no fake bio-age calculation.
- Visual setting to reduce main-screen content; stored in existing SharedPreferences.
- versionCode 300 / versionName 3.00.0-alpha.

## Not delivered / constraints
- The GLB mountain asset is unchanged; real terrain reconstruction is reserved for Alpha 4.00.
- No Health Connect, online LLM, clinical bio-age metric or cloud service.
- Kotlin/Android APK build not executed in ChatGPT environment. Claude must compile and fix any integration issues before delivery.
- The existing four tabs, data keys, backup and Chiron chat are preserved.
