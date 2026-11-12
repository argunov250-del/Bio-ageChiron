# Bio-Age Alpha 0.50 FULL — cumulative source patch 0.32–0.50

Apply to GitHub repository Alpha 0.31, NOT the earlier partial 0.50 patch.

From repository root: `python3 /path/to/bioage_050_full/apply_alpha050_full.py`
Build: `./gradlew :app:assembleDebug` and upload APK from GitHub Actions.

Included workstreams:
- 0.32–0.35: rebuilt GLB mountain geometry and neutral snow/rock vertex colors.
- 0.36–0.40: compact home summit progress and panorama description.
- 0.41–0.45: weekly health summary, 14-day sleep trend comparison with minimum sample sizes, collapsible detailed charts.
- 0.46–0.48: five summit milestones and weekly notes saved offline.
- 0.49: local data validation.
- 0.50: integrated features, versionCode 50 and versionName 0.50.0-alpha.

Not implemented: Health Connect, generative AI, dynamic weather, medical biological age, proven GPU optimizations. These require separate engineering and tests. Do not advertise as completed.

Manual QA: 4 tabs, camera -45/0/45, Chiron open/back, save health values, save weekly reflection/reopen, milestones, rest toggle, backup JSON, keyboard, small-screen scroll, no loss of preferences. Build and device behavior are NOT verified by this package.
