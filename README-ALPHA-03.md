# Bio-Age Alpha 0.3 — 3D patch

This is a patch for the existing Alpha 0.2 repository, **not a full standalone Android project**.

## Files
- `app/src/main/java/com/bioage/alpha/Caucasus3DScene.kt` — SceneView/Filament scene.
- `app/src/main/assets/models/elbrus_terrain.glb` — original procedural triangulated mountain terrain.
- `apply_alpha03.py` — safely patches the existing MainActivity and app Gradle file.

## Apply in repository root
1. Copy these files to the corresponding paths, preserving existing files.
2. Run `python3 apply_alpha03.py` from the repo root.
3. Commit, push, run existing GitHub Actions workflow.
4. Fix any SceneView API mismatches reported by Gradle before claiming build success.

The 3D geometry is an original stylized procedural mountain approximation, **not a geospatially accurate Elbrus DEM**. It is genuine GLB 3D, not a flat illustration. It has no externally sourced textures or licensed assets.

**Compatibility:** existing package/applicationId, data keys, habits and four tabs are preserved. Do not uninstall Alpha 0.2 before installing the update.

**Build status:** not compiled in this environment (no Android SDK). Integration must be validated in GitHub Actions and on the actual phone.
