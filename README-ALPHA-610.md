# Bio-Age Alpha 6.10 — Three.js foundation

This is a **first implementation increment**, not a claim that all 160+ historical answers have been implemented. Requires Alpha 5.00 / 6.00 repository layout and network once during patch installation to vendor pinned Three.js 0.152.2. The resulting APK runs without internet.

Run `python3 /path/to/bioage_three_foundation/apply.py` from the GitHub repository root. The script aborts on missing anchors, and does not modify the signing workflow, package ID, or preferences. It backs up three changed source files under `.bioage_three_backup/` (do not commit that directory).

Changes: native SceneView background replaced by offline Three.js WebView; automatic single fixed-composition slow drift; no gesture handling, no manual orbit buttons; low-poly procedural alpine relief, sky and fog; versionCode 610. Original GLB remains in assets for later development. Does not implement trained AI, Health Connect, biological age, or full 160+ backlog.

Test: compile signed APK; verify sky on every tab, no black background, WebView GPU performance, animation when returning from background, tab scroll, data preservation and in-place signature update. If WebView fails to initialize, CSS gradient sky fallback appears; report this as a bug, not as a successful Three.js render.
