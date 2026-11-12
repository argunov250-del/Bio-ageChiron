# Bio-Age Alpha 0.6 — visual scene patch

This patch replaces the existing **real 3D GLB** with a mountain mesh plus an inward-facing, vertex-colored **unlit sky sphere** (KHR_materials_unlit). It also adjusts the mountain vertex colors toward dark slate rock and pale snow. This is a stylized rendering, not photorealistic terrain or a geographic reconstruction of Elbrus. It does not implement dynamic sunlight, real shadows, cloud simulation or a custom camera slider.

## Install in repository root

1. Extract this ZIP at the **root of the existing repository**, preserving paths. The GLB overwrites `app/src/main/assets/models/elbrus_terrain.glb`.
2. Run `python3 apply_alpha06.py` once. The script updates `versionCode` to 6, `versionName` to `0.6.0-alpha`, and the UI labels.
3. Commit, push and build using the **existing working GitHub Actions workflow**. Do not overwrite workflow or signing settings from this patch.
4. Test on phone. In particular check that sky is visible behind the peaks and that the GLB does not render black or white due to GPU / unlit material handling.

## Upgrades / data

Package `applicationId` stays unchanged. SharedPreferences keys remain unchanged. Installing over an existing version requires the **same APK signing certificate**. If Android refuses an update, **do not uninstall** while important local data remains; first check signing key and package ID. This patch does not fix signing keys and cannot guarantee in-place updates.

## Known limitations

No Android SDK is available in the patch authoring environment; compilation and GPU rendering are **not verified**. The GLB was structurally checked with trimesh. The existing 3D component and SceneView version are intentionally unchanged.
