# Bio-Age Alpha 0.17 — full cumulative patch (0.13–0.17)

Baseline: verified public GitHub Alpha 0.12 (MainActivity.kt + Alpha12Features.kt + SceneView 2.3.0).

- 0.13: constrain scrollable content to leave bottom navigation visible, Android back returns to Home.
- 0.14: physically new GLB vertex positions for more rugged rock and snow coloration.
- 0.15: baked directional light on the rock/snow vertex colors, cloud bands in GLB, smooth orbit camera animation. Lighting is baked, **not** dynamic sunlight; clouds are static.
- 0.16: existing seven-day chart plus 30-day averages and seven-day step average, missing days excluded.
- 0.17: Chiron rule-based guidance uses dated sleep records and explains observations, no diagnoses or neural network.

Run in repository root: `python3 /path/to/apply_alpha017_full.py` (keep extracted directory structure). The script refuses unexpected source versions. It backs up original MainActivity and Gradle files locally; **do not commit `.alpha12.bak` files**.

VersionCode=17, versionName=0.17.0-alpha, existing applicationId and signing remain unchanged. Preserve existing backup/import code. Verify GitHub Actions compile, then test Health screen with keyboard, tabs, 3D and history on device. No Android build has been run here. This is NOT a Health Connect integration.
