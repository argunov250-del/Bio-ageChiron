# Bio-Age Alpha 0.7 — patch for existing repository

Copy all contents of this archive into the root of `argunov250-del/Bio-ageChiron`, preserving paths. Run `python3 apply_alpha07.py`, then commit and push. Do not delete other project files.

## Changes
- Replaces `app/src/main/assets/models/elbrus_terrain.glb` with an updated 3D mesh using slope/height-dependent rock and snow vertex colors and a blue atmospheric sky dome.
- `apply_alpha07.py` sets `versionCode=7`, `versionName=0.7.0-alpha`, and updates BOTH the header and Profile to 0.7. Changes slider colors only.
- Does not change package name, stored data keys, SceneView dependency, or signing workflow.

## Important
This is a procedural/stylized mesh, not a photorealistic geospatial model. Android compilation and device appearance have NOT been tested. If vertex-color sky appears wrong on Filament, report the issue rather than silently replacing the scene with 2D. Keep the existing signing configuration; confirm APK signing certificate matches previous install before claiming in-place update compatibility. GitHub Actions debug APK signing is not automatically stable without a configured keystore.
