#!/usr/bin/env bash
# VeloTV OWASP Post-Install Hardening — Disables unknown sources and guides closing ADB port 5555
set -euo pipefail

ADB="${ADB:-adb}"
TARGET="${1:-}"
if [ -z "$TARGET" ]; then
  echo "Usage: $0 <TV_IP:5555>"
  exit 1
fi

"$ADB" connect "$TARGET" >/dev/null || true
"$ADB" -s "$TARGET" wait-for-device
"$ADB" -s "$TARGET" shell 'settings put secure install_non_market_apps 0'
"$ADB" disconnect "$TARGET" >/dev/null || true

echo "Done: install_non_market_apps set to 0 and ADB disconnected."
echo "Final manual step on TV: Settings -> Developer Options -> Disable USB Debugging -> Reboot TV."

