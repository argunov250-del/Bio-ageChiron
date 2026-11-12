# Bio-Age Alpha 0.17 — cumulative source patch

This patch is intended for the existing Bio-ageChiron repository at Alpha 0.12 (or the prior 0.20 navigation-only patch). It does not include the complete Android project.

## Changes
- 0.13: bound scrollable card height; keep bottom tab bar visible, including on Health screen.
- 0.14–0.15: preserve existing 3D scene; no claim of new photorealistic mountain assets. Camera retains native Material3 Slider.
- 0.16: 30-day averages for manually recorded steps and sleep, excluding missing dates.
- 0.17: local Chiron guidance from user-entered data and recent sleep records, with no diagnosis.
- Version: `versionCode=17`, `versionName=0.17.0-alpha`; labels updated.

## Apply
Extract the archive and run `python3 bioage_alpha017/apply_alpha017.py` from the repository root, or copy the script to the root and run it there. Commit changed files and build with existing GitHub Actions. **Do not change signing credentials.**

## Test checklist
1. Open Health: scroll to the bottom and switch tabs; keyboard open and closed.
2. Save steps and sleep on different dates; verify 7-day history and 30-day averages.
3. Check Chiron with no data, with sleep below 6h, and with 3+ recorded sleep days.
4. Verify version in Android app info and UI.
5. Install over existing APK without uninstalling; if signature differs, stop and preserve data.

## Limits
No Health Connect, real-time weather, advanced 3D lighting, or actual AI model in this patch. The code has not been compiled in Android CI. The import/export feature is unchanged and still requires user testing.
