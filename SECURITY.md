# Security Policy — VeloTV

## Security & Privacy Guarantees (OWASP MASVS / POLA)

VeloTV is engineered with strict **Principle of Least Privilege (POLA)** and **offline-first** guarantees:

1. **Zero `INTERNET` Permission:** The VeloTV launcher APK does **not** declare `android.permission.INTERNET`. It is mathematically incapable of sending telemetry, analytics, crash logs, or user data over any network.
2. **Zero Dangerous Permissions:** No microphone, camera, location, contacts, or external storage permissions are requested.
3. **Hardened Manifest:** `android:allowBackup="false"`, `android:usesCleartextTraffic="false"`, and only the main launcher activity (`HomeActivity`) is exported.
4. **Parental PIN Protection:** Optional PIN codes are stored using salted SHA-256 hashes (never plaintext) and verified in constant time.
5. **Offline QR Generation:** Donation and repository QR codes are rendered 100% locally in memory using `com.google.zxing:core`.

## Reporting a Vulnerability

If you discover a security vulnerability, please report it responsibly:

- Or open a Private Security Advisory on GitHub (`github.com/mylnzon58/velotv/security/advisories`).

Please include Android version, TV/device model, and reproduction steps.

