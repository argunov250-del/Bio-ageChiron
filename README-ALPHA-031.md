# Bio-Age Chiron Alpha 0.31 — UX patch

Apply on **the Alpha 0.30 repository**, not Alpha 0.17. Unzip anywhere, then from the repository root:

```bash
python3 /path/to/bioage_031/apply_alpha031.py
./gradlew :app:assembleDebug
```

Changes:
- Home screen panel is capped at 59% of available space; mountain panorama stays visible above it. The home panel remains scrollable.
- Chiron dialogue moves to a separate in-app view with a back button and Android Back handling.
- Bottom navigation remains fixed; changing tabs closes dialogue.
- Empty analytics graph shows an informative message rather than a blank chart.
- Step goal uses 500-step buttons, avoiding the device-specific malformed Material slider.
- Version 0.31.0-alpha / code 31. No data keys, signing, applicationId, or 3D assets changed.

**Not yet Android-built or tested on a physical device.** Claude should compile and fix only integration errors. Do not uninstall the previous APK before backing up user data. Debug APKs built on different runners may have incompatible signing certificates.
