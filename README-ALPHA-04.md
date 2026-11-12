# Bio-Age Alpha 0.4 — 3D landscape patch

Apply to the existing Alpha 0.3 repo, **not** as a new project.

1. Copy the `app/` directory from this patch into the repository root, merging folders and replacing the existing `Caucasus3DScene.kt` and `elbrus_terrain.glb`.
2. Copy `apply_alpha04.py` to the repository root.
3. Run `python3 apply_alpha04.py` from the repository root.
4. Commit and push, wait for GitHub Actions to produce `app-debug.apk`.

This patch replaces the 3D mesh with a 180×180 grid containing two summits, irregular rocky ridges and vertex-color snow/rock materials. Camera is pulled back and orbits the scene. No external texture assets required. It does **not** yet provide geographic accuracy, photorealistic PBR, realistic sky/atmosphere or shadow mapping. The existing SceneView integration controls lighting/background; do not claim those features are implemented. No changes to health, progress, Chiron, app package, or SharedPreferences keys.

Build/runtime caveat: the project was not compiled in this environment; test on Android hardware. If model appears black, check Filament material/vertex color support and lighting; do not replace with a flat image. If orientation is inverted, check glTF normals/winding. If Alpha 0.3 has diverged, adjust the script carefully instead of overwriting user code.
