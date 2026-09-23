#!/usr/bin/env bash
# Retakes the README screenshots. See specs/screenshots.md.
#
#   scripts/screenshots.sh <adb-serial> [screen...]
#
# Screens: models-list model-detail roadmap settings (default: all).
# Every screen is shot in dark, then light, into docs/images/.
set -euo pipefail

SERIAL=${1:?usage: scripts/screenshots.sh <adb-serial> [screen...]}
shift
if [ $# -eq 0 ]; then set -- models-list model-detail roadmap settings; fi
SCREENS=("$@")

export ANDROID_SERIAL=$SERIAL
PKG=com.fedo.modelpulse
OUT=$(cd "$(dirname "$0")/.." && pwd)/docs/images

# ui <mode> <arg>: reads the current UI dump.
#   ui has  <text|desc>  -> exit 0 if a node's text or content-desc equals it
#   ui tap  <text|desc>  -> prints "x y" of that node's centre
#   ui card              -> prints "x y" of the first model card
ui() {
  adb exec-out uiautomator dump /dev/tty 2>/dev/null | python3 -c '
import sys, re, xml.etree.ElementTree as ET
mode, arg = sys.argv[1], sys.argv[2]
raw = sys.stdin.read()
if "<?xml" not in raw: sys.exit(1)  # dump failed mid-animation; callers retry
nodes = list(ET.fromstring(raw[raw.index("<?xml"):raw.rindex(">") + 1]).iter("node"))
def box(n): return list(map(int, re.findall(r"\d+", n.get("bounds"))))
def centre(n): l, t, r, b = box(n); print((l + r) // 2, (t + b) // 2)
if mode == "card":
    width = max(box(n)[2] for n in nodes)
    wide = [n for n in nodes if n.get("clickable") == "true" and box(n)[2] - box(n)[0] > width * 0.9]
    centre(wide[1])  # wide[0] is the search field
    sys.exit(0)
match = [n for n in nodes if arg in (n.get("text"), n.get("content-desc"))]
if not match: sys.exit(1)
if mode == "tap": centre(match[0])
' "$1" "${2:-}"
}

# wait_for <text|desc>: polls up to 20 s, then fails the run.
wait_for() {
  for _ in $(seq 20); do ui has "$1" && { sleep 1; return; }; sleep 1; done
  echo "timeout waiting for '$1'" >&2; exit 1
}

tap() { adb shell input tap $(ui tap "$1"); }

launch() {
  adb shell am force-stop $PKG
  adb shell am start -W -n $PKG/.MainActivity >/dev/null
  wait_for "Search models"
}

shot() { adb exec-out screencap -p > "$OUT/$1.png"; echo "$1.png"; }

# One function per screen; each starts from a fresh launch on Models.
models-list()  { launch; shot "models-list$1"; }
model-detail() { launch; adb shell input tap $(ui card); wait_for "Copy"; shot "model-detail$1"; }
roadmap()      { launch; tap "Roadmap"; wait_for "Feedback"; sleep 2; shot "roadmap$1"; }
settings()     { launch; tap "Settings"; wait_for "Fedo SDK"; shot "settings$1"; }

ORIGINAL_NIGHT=$(adb shell cmd uimode night | awk '{print $3}')
trap 'adb shell cmd uimode night "$ORIGINAL_NIGHT" >/dev/null' EXIT

for theme in dark light; do
  if [ $theme = dark ]; then adb shell cmd uimode night yes >/dev/null; suffix=""
  else adb shell cmd uimode night no >/dev/null; suffix="-light"; fi
  for s in "${SCREENS[@]}"; do "$s" "$suffix"; done
done
