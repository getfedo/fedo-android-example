# README Screenshots

How to retake the eight images in `docs/images/` that `README.md` shows.
One script does it: `scripts/screenshots.sh`. Run it and do nothing else by
hand, so every run and every agent produces the same images.

## Run

```bash
./gradlew :app:installDebug            # with ANDROID_SERIAL set to the same device
scripts/screenshots.sh <adb-serial>                 # all screens, dark + light
scripts/screenshots.sh <adb-serial> roadmap settings # only these screens
```

Get the serial from `adb devices -l`. Prefer a physical phone (constitution).
The script needs `adb` and `python3` (stdlib only), and nothing else.

## Preconditions

- Debug build installed with a Fedo API key in `local.properties`. Without a
  key, `roadmap` times out because the board never renders.
- Demo user signed in (Settings → **Sign in as demo user**), so Settings
  shows the signed-in card.
- Device unlocked, screen on, network up, app language English. The script
  finds nodes by their English text.

## Screens

Every screen starts from a fresh launch (`force-stop` + `am start`) on Models,
so no screen depends on the one before it. Each screen is shot in dark
(`<name>.png`), then in light (`<name>-light.png`). The device's night mode
is restored when the script exits.

| Screen | Steps | Ready when | File |
|--------|-------|------------|------|
| `models-list` | launch | `Search models` shown | `models-list.png` |
| `model-detail` | launch, tap the first model card | `Copy` shown | `model-detail.png` |
| `roadmap` | launch, tap the nav item with desc `Settings`, tap `Roadmap` | `Feedback` shown, +2 s for the SDK list | `roadmap.png` |
| `settings` | launch, tap the nav item with desc `Settings` | `Fedo SDK` shown | `settings.png` |

Nodes are found in `uiautomator dump` by text or content-desc, never by
fixed coordinates. The nav bar changes width when you select an item, so
fixed taps land on the wrong target. The first model card is the second
clickable node that is at least 90% of the screen width. The first such node
is the search field.

Every wait polls for 20 s and then fails the run. It never shoots a
half-loaded screen.

## Adding a screen

1. Add a one-line function to `scripts/screenshots.sh` named after the file:
   `launch`, then navigate with `tap "<text or desc>"`, `wait_for "<text>"`,
   then `shot "<name>$1"`.
2. Add `<name>` to the default list at the top of the script.
3. Add a column to both tables in `README.md`.

Use the text from `strings.xml`, which is the text the user sees. That is
the same rule the UI tests follow.

## After a run

- Check the eight images by eye. The data is live OpenRouter and Fedo
  content, so it changes between runs.
- If you changed the device, update the "Captured on …" line under the
  README screenshots.
- Add a line under `### Changed` in `CHANGELOG.md`.

## Known limits

- The status bar shows the real clock, battery and notification icons.
  `com.android.systemui.demo` has no effect on Samsung devices. On a Pixel,
  demo mode works if you want a clean bar.
- Colours follow the device wallpaper (dynamic colour on API 31+).
