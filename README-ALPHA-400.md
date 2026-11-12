# Bio-Age Chiron Alpha 4.00 — 3D Caucasus & Route

Based on repository Alpha 2.00 (also compatible with Alpha 3.00 if it retains the same route call). This is a source patch, NOT a built APK.

## Real changes
- Replaces `app/src/main/assets/models/elbrus_terrain.glb` with a new offline, procedural double-peaked terrain model. Approximately 39,000 triangles, vertex-colored snow/rock faces, ridges and erosion detail. Generated with trimesh and deterministic noise.
- Adds `Alpha400Features.kt`: an interactive-data-driven, static-drawn 5-checkpoint route that reflects the existing 90-day habit marks (0–270), with honest game-progress labeling.
- Updates home route call and mountain caption; increments Android version to 4.00.0-alpha / code 400.
- Preserves the SceneView camera, SharedPreferences keys, navigation, Health/Chiron functionality and applicationId.

## Install
From the GitHub repository root, run `python3 /path/to/bioage_400_full/apply_alpha400.py`.
Then compile with the existing GitHub Actions Android workflow. Review changes, especially mountain framing and 3D performance on the actual phone. The model is a stylized synthetic Elbrus-like terrain, not a geographically accurate survey.

## Safety
The installer creates `.alpha400_backup/` for the overwritten files. Do not commit the backup. Do not uninstall the previous app or change signing keys without first exporting data. No Gradle/Android compilation was performed in this environment.
