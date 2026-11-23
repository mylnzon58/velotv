<p align="center">
  <img src="assets/velotv-hero-banner.png" alt="VeloTV — Native Android TV Launcher" width="100%" />
</p>

<p align="center">
  <a href="https://mylnzon58.github.io/velotv/"><img src="https://img.shields.io/badge/Website-GitHub_Pages-DF9D3E?style=flat-square&labelColor=1B1815" alt="Official Website" /></a>
  <a href="README.md"><img src="https://img.shields.io/badge/Espa%C3%B1ol-Documentaci%C3%B3n-C9C1B7?style=flat-square&labelColor=1B1815" alt="Documentación en Español" /></a>
  <img src="https://img.shields.io/badge/Release_APK-2.1_MB-EDE8E1?style=flat-square&labelColor=1B1815" alt="APK 2.1 MB" />
  <img src="https://img.shields.io/badge/Live_RAM-23.7_MB-7CB68A?style=flat-square&labelColor=1B1815" alt="RAM 23.7 MB with HD wallpaper" />
  <img src="https://img.shields.io/badge/Telemetry-Zero-DF9D3E?style=flat-square&labelColor=1B1815" alt="Zero Telemetry" />
  <img src="https://img.shields.io/badge/Android_TV-7.0_%E2%80%93_15-C9C1B7?style=flat-square&labelColor=1B1815" alt="Android TV 7 to 15" />
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-DF9D3E?style=flat-square&labelColor=1B1815" alt="MIT License" /></a>
</p>

# VeloTV

Native Kotlin launcher for Android TV and Google TV engineered for steady 60 fps responsiveness on 1–2 GB RAM hardware, featuring auto-rotating open-license wallpapers, Material 3 focus physics, dynamic ambient color bloom, zero ads, and zero telemetry.

Benchmarked on real hardware (**Skyworth G6 / `9K02B` board / MediaTek MT5891 / 32-bit ARMv7 / Mali-T860 / 2 GB RAM / Android 8.0**) and compatible with Android TV / Google TV devices from API 24 to API 35.

---

## Real Hardware Benchmark (Skyworth MT5891 · 2 GB RAM)

Measured via `adb shell dumpsys meminfo` on the same physical television:

| Metric | Google TV Launcher + Recs | Projectivy Launcher 4.74 | **VeloTV 1.1 (Cinema Lounge)** |
|---|---:|---:|---:|
| **Release APK Size** | ~82 MB | 11.0 MB | **2.1 MB** *(4 Outfit OFL weights + 8 HD WebP landscapes)* |
| **Live RAM (PSS)** | 92.6 MB | 36.8 MB | **23.7 MB** *(with live HD wallpaper · ~4× less than Google)* |
| **Offline Smart Shelf ("Up Next")** | Cloud (tracked) | No | **Yes, 100% on-device (recency + time-of-day, zero telemetry)** |
| **1-Click Multi-Launcher Hub** | No | No | **Yes (VeloTV + Stock Android TV + Projectivy coexistence)** |
| **Physical Remote Shortcuts (`1–9` + color keys)** | No | Partial | **Yes (Speed Dial `1–9`, Green/Yellow/Blue/Red, Play Resume)** |
| **Cinema Night Mode + Auto-RAM Guard** | No | No | **Yes (warm 22:00–07:00 veil + auto-reclaim `<320 MB`)** |
| **Rotating Open Wallpapers** | No | Partial (Pro paywall) | **Yes, 100% free (Wikimedia + Unsplash + Bundled)** |
| **Ads / Paywalls / Telemetry** | Yes / N/A / Yes | No / Freemium / Yes | **Zero · MIT Open Source · Zero Telemetry** |

---

## What Makes VeloTV Distinct? (Clear USPs)

1. **Cinema Lounge UI with 70% Less Text Clutter:** Minimal vector icon bar, soft **Outfit** typography, edge-to-edge gliding cards, floating rounded Control Center drawers, and native cinema feedback pills.
2. **100% Offline "Up Next" Smart Shelf:** Learns locally which apps you use in the morning, afternoon, or night and surfaces them automatically — zero cloud, zero tracking.
3. **1-Click Multi-Launcher Hub:** Launch **Stock Android TV Home** or **Projectivy** in 1 tap (or Red key) while keeping **VeloTV** locked as your default Home button target.
4. **Physical TV Remote Superpowers:** Keys **`1`–`9`** speed-dial Favorites #1–#9; **Green** runs Turbo RAM Clean; **Yellow** switches wallpaper; **Blue** opens Ambient OLED Clock; **Red** opens Launchers & HDMI; **Play/Pause** resumes your last app.
5. **Cinema Night Mode + Auto-RAM Guard:** Automatic warm eye-comfort veil (`22:00–07:00` or 1-click toggle) plus silent background RAM recovery whenever free memory drops below `320 MB` on 1–2 GB RAM TVs.
6. **23.7 MB Live RAM PSS & 2.1 MB APK (MIT):** Includes 4K/HDR calibration patterns, OLED anti-burn-in screensaver, salted SHA-256 parental PIN + schedule, and regional catalog with zero paywalls.

---

## Live TV Screenshots

| Home with Rotating Free Wallpaper & Ambient Glow | Live Wallpaper Gallery (Auto & Manual Preview) |
|---|---|
| ![VeloTV Home](assets/screenshots/02-home-darkroom.png) | ![Wallpaper Gallery](assets/screenshots/08-wallpapers-gallery.png) |

| Ambient Gallery & OLED Anti-Burn-in Clock | System & Native Developer Drawer |
|---|---|
| ![Ambient Gallery](assets/screenshots/09-ambient-mode.png) | ![System & Developer](assets/screenshots/03-system-developer.png) |

| Tabbed Settings Drawer | Regional Catalog (AR · LATAM · US · EU · ASIA) |
|---|---|
| ![VeloTV Settings](assets/screenshots/04-settings-drawer.png) | ![Regional Catalog](assets/screenshots/07-catalog-ar.png) |

| 4K / HDR Display Calibration | Offline QR Creator Support |
|---|---|
| ![4K Calibration](assets/screenshots/05-calibration-4k.png) | ![Support QR](assets/screenshots/06-support-qr.png) |

---

## Feature Overview

- **Low-Memory Open Wallpaper Engine (`RGB_565` · 1.84 MB RAM):** Auto-rotates every 5 minutes by default (`1m / 5m / 15m / 30m / on return / manual`) combining **8 bundled free-license landscapes** for 0 ms cold start + live open image banks (**Wikimedia Commons Featured Pictures** and **Unsplash / Picsum**), live-preview thumbnail gallery, configurable cinema scrim, subtle GPU Ken Burns motion, and a **100% Offline Mode** toggle.
- **60 fps Native Rendering on 1–2 GB RAM:** Kotlin + native Android Views + shared `RecyclerView.RecycledViewPool`, synchronous LRU icon fast-path, smooth TV D-pad overscan scrolling, Material 3 GPU elevation (`translationZ`), and focused-app dynamic ambient glow.
- **Six Calibrated Palettes (All Free):** Darkroom Amber, Darkroom Red, Midnight, Studio Daylight, Mineral Forest, and Graphite Monochrome; three corner radii (`0 dp`, `10 dp`, `16 dp`) and three card densities.
- **Full App, Input & Row Control:** Discovers Leanback TV (with activity banners) and sideloaded mobile apps, live search, user-created custom categories, row reordering and hiding, custom renaming for apps and HDMI inputs, and boot auto-launch.
- **Real TV & HDMI Inputs (`TvInputManager`):** Launches physical `HDMI`, `Tuner`, and `AV` sources across brands with live status indicators.
- **System, Quick Clean & Native Developer Hub:** One-click **Clean** button in the top bar (safely releases idle background apps via `ActivityManager`, trims internal caches, and opens native Storage Cache on long-press); live readout of ADB state, developer mode, unknown sources, animation scales, SoC, kernel, WebView, local IP, and RAM; plus dynamically resolved shortcuts to native Android TV Settings and OEM hardware menus when present.
- **Built-in Display Tools:** 10 native 4K/HDR calibration patterns (11-step PLUGE grayscale, SMPTE bars, 5%/10% overscan grid, uniformity and dead-pixel checks, 3-meter sharpness test) and an **Ambient Gallery + OLED Clock** screensaver with 60-second anti-burn-in shift and instant `OK` toggle to pure OLED black.
- **Parental Controls & Schedule:** 4-digit PIN hashed with salted SHA-256 (constant-time verify) protecting Settings, Developer Hub, apps, or HDMI inputs, with optional scheduled restrictions.
- **10 Languages & Regional Catalog:** Spanish default plus instant switching across `es-AR`, `es`, `en`, `pt-BR`, `fr`, `de`, `it`, `ja`, `ko`, `zh-CN`, and SDK-aware regional recommendations for AR, LATAM, USA, Europe, and Asia.
- **OWASP Security & Attribution:** Zero analytics, zero dangerous permissions, `allowBackup="false"`, `usesCleartextTraffic="false"`, and unit-tested HTTPS host allowlist ([SECURITY.md](SECURITY.md) and [CREDITS.md](CREDITS.md)).

---

## Install (3 Easy Options)

### Option 1 — With an AI Agent: Antigravity or OpenCode *(Recommended)*
1. On your TV: `Settings → About → Build` (7 clicks) → enable **USB / ADB Debugging**.
2. **Reboot your TV** (opens ADB Wi-Fi port `5555`) and connect it to the **same Wi-Fi** as your PC.
3. Paste this prompt into **Antigravity** (or **OpenCode**) and accept **Always allow** on the TV screen:

```text
Connect to my Android TV on my local Wi-Fi (scan port 5555), download the latest release from https://github.com/mylnzon58/velotv verifying its SHA-256, install VeloTV, and apply safe reversible optimization (--user 0, 0.5x animations) without touching DRM or Play Store. Guide me concisely only when TV confirmation is needed.
```

### Option 2 — 3-Click Desktop GUI Installer (Windows / Linux / macOS)
Run [tools/installer/velotv_installer.py](tools/installer/velotv_installer.py) (or double-click `install-windows.bat`, `install-linux.sh`, or `install-macos.command`). Auto-scans your Wi-Fi, fetches official Google ADB if missing, verifies SHA-256, installs, optimizes, or restores stock apps.

### Option 3 — Manual Build, USB, LocalSend, or ADB

```bash
# Run unit tests & build release APK (JDK 17 + Android SDK 35)
./gradlew testDebugUnitTest assembleRelease

# Optional ADB optimization & reversible rollback toolkit
./tools/tv-toolkit/optimize_tv.sh <TV_IP>:5555 release/VeloTV-v1.1.0-universal.apk
./tools/tv-toolkit/restore_tv.sh <TV_IP>:5555
./tools/tv-toolkit/harden_close_adb.sh <TV_IP>:5555
```

---

## Author, License & Support

Created and maintained by **[Juan José Monzón](https://github.com/mylnzon58)** (`@mylnzon58`) in Argentina under the **[MIT License](LICENSE)** (see also [NOTICE](NOTICE)).

You are free to use, study, modify, and redistribute this project, provided that the **original copyright and attribution notice are preserved** in all copies and derivative works.

| Region | Platform | Link |
|---|---|---|
| Argentina (ARS) | Mercado Pago | [link.mercadopago.com.ar/mylnzon58](https://link.mercadopago.com.ar/mylnzon58) |
| Argentina / LATAM | Tecito | [tecito.app/juanjoclassic](https://tecito.app/juanjoclassic) |
| Global (US / EU / Asia) | GitHub Sponsors | [github.com/sponsors/mylnzon58](https://github.com/sponsors/mylnzon58) |
| Monthly Sponsors | Patreon | [patreon.com/JuanjoClassic](https://www.patreon.com/JuanjoClassic) |
