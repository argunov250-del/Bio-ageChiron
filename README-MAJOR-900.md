# Bio-Age Chiron — Major 1 / Visual Experience 9.00

This patch is based on the actual GitHub `MainActivity.kt` Alpha 8.00 checked 2026-11-13.

## Apply

From repository root:

`python3 /path/to/major9/apply.py`

The script validates source anchors before writing, creates `MainActivity.kt.pre_major9` locally, updates the version to 9.00, adds Compose UI files, and adds a snowy mountain ridge overlay to the existing offline Three.js background. Do not commit the backup.

## Changes

- Main: compact health preview, mountain route, quick access to Chiron.
- Health: compact 7/30/90-day view; manual inputs and goal editing preserved.
- Progress: five checkpoints; habits and rest controls preserved.
- Profile: Apple Health ZIP import, backup and diagnostics preserved.
- Three.js camera remains automatic; no gestures.
- Alpine ridge overlay prevents the blank-sky experience seen in Alpha 8.00.
- No changes to application ID, database, signing, permissions or stored health data.

## Limitations and validation

No APK build was run here. Run the existing GitHub Actions build and test all four tabs, ZIP import, backups, background and device performance. This is Major 1 (UI), not the WHOOP-like analytics or Chiron intelligence planned for Majors 2 and 3.
