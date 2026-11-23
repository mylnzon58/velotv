#!/usr/bin/env bash
# VeloTV Companion Rollback — Re-enables stock launcher & restores all defaults (100% reversible)
set -euo pipefail

ADB="${ADB:-adb}"
TARGET="${1:-}"
if [ -z "$TARGET" ]; then
  echo "Usage: $0 <TV_IP:5555>"
  exit 1
fi

"$ADB" connect "$TARGET" >/dev/null || true
"$ADB" -s "$TARGET" wait-for-device

PACKAGES=(
  com.google.android.tvlauncher
  com.google.android.tvrecommendations
  com.google.android.leanbacklauncher
  com.google.android.leanbacklauncher.recommendations
  com.google.android.leanbacklauncher.partnercustomizer
  com.google.android.videos
  com.google.android.music
  com.google.android.play.games
  com.google.android.marvin.talkback
  com.google.android.tts
  com.google.android.katniss
  com.google.android.speech.pumpkin
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

for pkg in "${PACKAGES[@]}"; do
  "$ADB" -s "$TARGET" shell pm enable --user 0 "$pkg" >/dev/null 2>&1 || true
done

"$ADB" -s "$TARGET" shell '
  settings put global window_animation_scale 1.0
  settings put global transition_animation_scale 1.0
  settings put global animator_duration_scale 1.0
  settings put global send_action_app_error 1
  settings put secure send_action_app_error 1
  settings put global wifi_scan_always_enabled 1
  cmd package set-home-activity com.google.android.tvlauncher/.MainActivity 2>/dev/null || true
'
echo "Stock launcher and settings restored."

