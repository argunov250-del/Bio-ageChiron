# Bio-ageChiron Alpha 0.2

Android Kotlin + Jetpack Compose, offline-first prototype.

## Changes since 0.1
- Readable light-on-dark glass-like cards and compact dashboard.
- Improved procedural **2D** alpine landscape (not actual 3D/Filament).
- Daily habits stored with date keys and a seven-day activity strip.
- Four tabs, local health entries, rules-based Chiron and camera slider.
- Preserves the same applicationId and SharedPreferences file as 0.1.

## Build
GitHub Actions > Build Bio-Age Android APK > latest successful run > Artifacts > Bio-ageChiron-Alpha-APK.
GitHub Actions runs `./gradlew :app:assembleDebug` (Gradle wrapper 8.9, JDK 17, Android SDK 35).
The APK is a debug build; install only from your own trusted repository.

## Limitations
Not a medical product. No validated biological age, no Health Connect, no 3D, no real background blur, no sync. Build status is verified by GitHub Actions.

## Upgrade from 0.1
Steps, sleep and mood entries keep their keys. Undated 0.1 habit marks are migrated once to today's date on first launch.
