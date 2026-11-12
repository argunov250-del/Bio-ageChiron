# Bio-Age Alpha 0.70 FULL — cumulative 0.51–0.70

**Base:** the currently committed Alpha 0.50 FULL, not Alpha 0.31. This package does not contain the full Android project.

From the repository root:

```bash
python3 /path/to/bioage_070_full/apply_alpha070_full.py
./gradlew :app:assembleDebug
```

What is actually implemented:
- 0.51–0.54: 7/30-day diary coverage, missing data handled explicitly.
- 0.55–0.58: context-aware offline Chiron observations based on 7/14-day sleep and step history, with sample minimums.
- 0.59–0.62: persisted gentle/rest advice mode, without changing historic records.
- 0.63–0.66: rolling 7-day habit rhythm and 21-mark game indicator.
- 0.67–0.69: editable weekly focus saved in existing SharedPreferences and JSON backup.
- 0.70: cumulative integration, version bump, profile release note.

**Not included:** new GLB mountain geometry, animated weather, generative AI, Health Connect, verified GPU performance gains, or medical age calculations. These require additional implementation and tests; do not claim they are done.

QA checklist: confirm 4 tabs, Elbrus scene and camera orbit, Chiron chat/back, health save, 7/30-day coverage, context messages after sample entries, gentle mode toggle persists after restart, weekly focus persists after restart, backup/export includes new keys, old data preserved, small-screen scroll, build succeeds. No uninstall unless backup is verified. The patch has not been compiled in this environment.
