# Bio-Age Alpha 6.00 — auto panorama

Apply on top of committed Alpha 5.00, from repository root:

```bash
python3 /path/to/bioage_600/apply_alpha600.py
```

Changes: remove orbit buttons and camera state from active home screen; add automatic 28-second one-way gentle oscillation between -7 and +7 degrees, using the existing Alpha 4.00 camera framing; retain the existing 3D GLB and data; bump version to 6.00.0-alpha (600). Home600 preserves the route and Chiron action. Legacy Home500 remains in source but is no longer used.

**Not included:** new mountain materials, guaranteed fix for black sky (requires a separate 3D asset/rendering change), trained AI Chiron, Health Connect, or a full Beta feature set.

Before applying, commit current changes; the script refuses missing/changed anchors before writing files. After applying, review `git diff`, build signed APK, test on phone (all four tabs, automatic motion, black sky, preservation of data, no crashes). Do not change the signing key or uninstall the app.
