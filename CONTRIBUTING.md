# Contributing to VeloTV

Thank you for helping make Android TV fast, clean, and ad-free for everyone!

## Engineering Standards
- **SOLID / DRY / KISS / POLA:** Keep `domain/` 100% free of Android framework imports so business logic stays unit-testable.
- **Zero Telemetry / Zero `INTERNET` Permission:** Never add analytics SDKs, ad SDKs, or `android.permission.INTERNET` to the core launcher.
- **Low-RAM First:** Every feature must stay smooth at 60 fps on 1–2 GB RAM / 32-bit `armeabi-v7a` Android 7–8 TVs as well as modern Google TV 14/15 devices.
- **Minimal Copy:** UI strings must stay concise (1–3 words per label) to remain readable from 3 meters away.

## Ways to Contribute
1. **Translations:** Add or improve `app/src/main/res/values-<locale>/strings.xml` (LATAM, US, Europe, Asia).
2. **Regional App Hub:** Suggest legal, official Play Store / open-source TV apps for your country in `RegionalCatalogRepository.kt`.
3. **Hardware Testing:** Share RAM/FPS measurements on your TV or TV box model via Issues.

## Local Build & Test
```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

