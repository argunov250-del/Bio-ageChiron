# Bio-Age Chiron Alpha 0.30 — cumulative source patch

**Base required:** GitHub repository `argunov250-del/Bio-ageChiron`, current Alpha 0.17 (the working version shown in screenshots). This is a source patch, **not an APK**. `applicationId` and existing SharedPreferences keys are preserved. Do not uninstall the app before backing up data.

## Install
1. Extract this archive anywhere in Claude's workspace.
2. In the **repository root** run `python3 /path/to/bioage_030/apply_alpha030.py`.
3. Review `git diff` and verify `versionCode = 30`, `versionName = 0.30.0-alpha`.
4. Run `./gradlew :app:assembleDebug` or the existing GitHub Actions workflow; correct build errors without silently removing functionality.
5. Test on Android before calling this a stable release. Preserve existing signing key; debug APKs from different runners may not install over an existing APK.

## Included changes
- 0.17.1: terrain vertex colors recalculated with darker rock and patchy snow; translucent cloud strips removed; camera pulled back; old broken slider replaced with discrete camera control.
- 0.18: virtual climb based on habit checkmarks; rest day toggle, no punishment for rest.
- 0.20: versioned cumulative integration and local-data compatibility.
- 0.21: 7/30/90-day bar chart and averages from dated manual records.
- 0.22: transparent, **non-medical** recovery hint based on recorded sleep.
- 0.23: expanded local Chiron rules, including rest days and sleep history.
- 0.24: persistent adjustable step target.
- 0.25: 30-day offline report.
- 0.26: UI content improvements and compact camera control.
- 0.27: terrain material-color improvements and revised framing; no live weather or advanced atmosphere.
- 0.28: local topic-based chat; **not a generative AI model**.
- 0.29: data diagnostics, limited manual validation; no automated device test suite included.
- 0.30: version metadata and integrated source package.

## Not implemented (do not claim otherwise)
- **0.19 Health Connect**: no permissions, SDK integration or sensor sync in this package. Requires explicit consent, proper deduplication and Android testing.
- **0.27 dynamic clouds / day-night cycle**: not implemented; 3D is static.
- **0.28 actual LLM model**: this is deterministic local chat, not a trained model.
- **0.29 comprehensive test automation / security audit**: not performed.
- Medical biological-age estimation: not implemented.

## Manual checks
- Health tab scrolls and bottom navigation stays available; keyboard open/closed.
- Steps, sleep and mood save and remain after restart; backup export/import.
- 7/30/90 charts display missing days as missing, not zero.
- Rest day persists; habit and virtual ascent values update.
- Camera angle buttons work at both ends and 3D is visible without purple/green artifacts.
- Chiron chat answers basic sleep/steps/habits/rest queries; no medical claims.
- Check upgrade signing before installing over the existing app.
