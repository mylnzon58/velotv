# VeloTV Companion ADB Toolkit

Safe, idempotent, **100% reversible** shell toolkit for optimizing any Android TV / Google TV (profiled on **Skyworth G6 / 9K02B / MediaTek MT5891 / Android 8.0 / 2 GB RAM**).

## Scripts

- `optimize_tv.sh <TV_IP:5555> [VeloTV.apk]` — Disables obsolete/telemetry bloatware (`--user 0`), applies POLA privacy settings, sets `0.5×` animations, trims caches, installs VeloTV, and compiles with ART `speed-profile`.
- `restore_tv.sh <TV_IP:5555>` — Re-enables every disabled package and restores stock launcher & defaults.
- `harden_close_adb.sh <TV_IP:5555>` — Sets `install_non_market_apps=0`, disconnects ADB, and prints the final TV toggle to close TCP port `5555`.

## Safety Guarantee
Never touches MediaTek/OEM hardware services (`com.mediatek.*`, `mtktvapi.agent`, `com.coocaa.factory`), DRM/Widevine, Netflix, Disney+, Chromecast, Play Store, or System Settings.

