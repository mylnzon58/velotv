#!/usr/bin/env bash
# VeloTV Companion ADB Optimizer — Works on any Android TV (profiled for 1–2 GB RAM TVs like Skyworth MT5891)
# Principles: SOLID, DRY, KISS, POLA (100% reversible via restore_tv.sh)
set -euo pipefail

ADB="${ADB:-adb}"
TARGET="${1:-}"
if [ -z "$TARGET" ]; then
  echo "Usage: $0 <TV_IP:5555> [path/to/VeloTV.apk]"
  exit 1
fi
VELO_APK="${2:-}"

adb_cmd() { "$ADB" -s "$TARGET" "$@"; }

echo "[1/5] Connecting to $TARGET..."
"$ADB" connect "$TARGET" >/dev/null || true
adb_cmd wait-for-device
echo "      Device: $(adb_cmd shell getprop ro.product.manufacturer | tr -d '\r') $(adb_cmd shell getprop ro.product.model | tr -d '\r') (Android $(adb_cmd shell getprop ro.build.version.release | tr -d '\r'))"

echo "[2/5] Disabling obsolete/telemetry bloatware (--user 0, 100% reversible)..."
BLOAT=(
  com.google.android.videos
  com.google.android.music
  com.google.android.play.games
  com.google.android.marvin.talkback
  com.google.android.tts
  com.google.android.tv.bugreportsender
  com.google.android.feedback
  com.google.android.tvtutorials
  com.oem.home.apps.electronicmanual
  com.android.printspooler
  com.google.android.syncadapters.calendar
  com.google.android.syncadapters.contacts
  com.android.providers.calendar
  com.google.android.inputmethod.japanese
  com.google.android.inputmethod.pinyin
  com.google.android.inputmethod.korean
  com.google.android.apps.inputmethod.hindi
  com.google.android.apps.inputmethod.zhuyin
)
for pkg in "${BLOAT[@]}"; do
  if adb_cmd shell pm path "$pkg" >/dev/null 2>&1; then
    adb_cmd shell am force-stop "$pkg" >/dev/null 2>&1 || true
    adb_cmd shell pm disable-user --user 0 "$pkg" >/dev/null 2>&1 || true
    echo "      [OFF] $pkg"
  fi
done

echo "[3/5] Applying POLA privacy settings & 0.5x snappy animations..."
adb_cmd shell '
  settings put global window_animation_scale 0.5
  settings put global transition_animation_scale 0.5
  settings put global animator_duration_scale 0.5
  settings put global send_action_app_error 0
  settings put secure send_action_app_error 0
  settings put global wifi_scan_always_enabled 0
  settings put global wifi_verbose_logging_enabled 0
  settings put global wifi_wakeup_enabled 0
  settings put global mobile_data_always_on 0
  settings put secure enabled_notification_listeners ""
'
adb_cmd shell pm trim-caches 1024M >/dev/null 2>&1 || true

if [ -n "$VELO_APK" ] && [ -f "$VELO_APK" ]; then
  echo "[4/5] Installing VeloTV & setting as default HOME launcher..."
  adb_cmd install -r "$VELO_APK"
  for stock in com.google.android.tvrecommendations com.google.android.leanbacklauncher.recommendations com.google.android.leanbacklauncher.partnercustomizer com.google.android.tvlauncher com.google.android.leanbacklauncher; do
    if adb_cmd shell pm path "$stock" >/dev/null 2>&1; then
      adb_cmd shell pm disable-user --user 0 "$stock" >/dev/null 2>&1 || true
    fi
  done
  adb_cmd shell cmd package set-home-activity io.github.mylnzon58.velotv/.ui.home.HomeActivity >/dev/null 2>&1 || true
  adb_cmd shell cmd package compile -m speed-profile io.github.mylnzon58.velotv >/dev/null 2>&1 || true
  adb_cmd shell input keyevent KEYCODE_HOME >/dev/null 2>&1 || true
else
  echo "[4/5] Skipping APK install (no APK path passed)."
fi

echo "[5/5] Done! Free RAM:"
adb_cmd shell 'grep -E "^(MemTotal|MemFree|Cached):" /proc/meminfo'

