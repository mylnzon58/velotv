# Security Policy — VeloTV

## Security & Privacy Guarantees (OWASP MASVS / POLA)

VeloTV is engineered with strict **Principle of Least Privilege (POLA)** and verifiable zero-telemetry guarantees:

1. **Zero Telemetry / Zero Analytics:** VeloTV includes zero analytics SDKs, zero crash reporters, zero ad networks, and sends zero device identifiers or usage metrics.
2. **Hardened HTTPS Allowlist for Free Wallpapers:** Network access is strictly limited by `WallpaperPolicyUseCase` to HTTPS-only calls against verified open-license image banks (`commons.wikimedia.org`, `upload.wikimedia.org`, `thumb.wikimedia.org`, `picsum.photos`, `fastly.picsum.photos`, `images.unsplash.com`), with manual redirect validation (`<= 3` hops), `4 MB` payload cap, `12 MB` LRU cache limit, and a one-tap **100% Offline Mode** toggle.
3. **Zero Dangerous Permissions:** No microphone, camera, location, contacts, or external storage permissions are requested. `KILL_BACKGROUND_PROCESSES` is a standard non-dangerous Android permission used exclusively by the user-triggered Quick Clean action on non-critical idle apps.
4. **Hardened Manifest:** `android:allowBackup="false"`, `android:usesCleartextTraffic="false"`, and only the main launcher activity (`HomeActivity`) is exported.
5. **Parental PIN Protection:** Optional PIN codes are stored using salted SHA-256 hashes (never plaintext) and verified in constant time.
6. **Offline QR Generation:** Donation and repository QR codes are rendered 100% locally in memory using `com.google.zxing:core`.

## Reporting a Vulnerability

If you discover a security vulnerability, please report it privately via GitHub Security Advisories:

- https://github.com/mylnzon58/velotv/security/advisories

Please include Android version, TV/device model, and reproduction steps.
