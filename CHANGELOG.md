# Changelog

Notable changes to ModelPulse, the Fedo Android example app. The format
follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

ModelPulse versions follow the Fedo SDK's minor version: 0.5.x builds against
`com.getfedo:sdk-android` 0.5.x.

## [0.5.0] - 2026-10-25

Targets `com.getfedo:sdk-android:0.5.0`.

### Fedo SDK

New in the SDK, in short:

- Threaded comment replies on the feedback board, shown when the board allows
  replies.
- `done` feedback is labelled "Shipped". A status the SDK doesn't know shows
  as "Unknown" instead of breaking the list.
- SDK logs go to a `logHandler` you provide. The `debug` flag is gone, and
  logs no longer contain the user's email, name or property values.
- Session fixes: no crash when refreshing during `logout()` or opening a
  feedback before sign-in finishes, and a late token refresh can't bring back
  the previous user.

### ModelPulse

- **Models list.** The public OpenRouter catalogue, newest first. Search by
  name or id, and filter by provider with chips that show model counts. Pull
  to refresh. If a refresh fails, the list stays and a snackbar offers Retry.
- **Model detail.** Input and output price per 1M tokens, context window,
  release date, input modalities, the full description, and a copyable model
  id.
- **Roadmap.** The Fedo feedback board (`FedoFeedbackScreen`), opened from
  Settings. It handles its own navigation and back.
- **Contextual feedback.** "Missing a model? Request it" when a search finds
  nothing, and "Report a problem" when models fail to load. Both open
  `FedoCreateFeedbackSheet`.
- **Demo sign-in.** Settings shows the SDK status. Signing in calls
  `setUserID`, `setUserDisplayName` and `setUserEmail`, and signing out calls
  `logout()`. The demo user is kept after a restart.
- **`favorite_provider`.** Picking a provider filter sets this user property,
  and clearing the filter clears it.
- **Runs without an API key.** The key is read from the gitignored
  `local.properties`. Without one, Fedo isn't initialized, the Fedo screens
  explain how to add a key, and the catalogue works as usual.
- **Logging.** `Fedo.initialize` sets a `logHandler` that sends the SDK's
  warnings and errors to Logcat under the `ModelPulse` tag.
- **Look.** Material 3 expressive, with a floating toolbar for Models and
  Settings and the same top app bar on every screen. Dynamic colour on
  Android 12+, and ModelPulse's own light and dark colour schemes on older
  versions.

### Build and CI

- `versionName` is `0.5.0`. It was `0.4.0` before.
- R8 is enabled for the release build.
- `material3` is held at 1.5.0-alpha01, the newest version that still has the
  `ModalBottomSheet` overload the SDK's feedback sheet calls (see
  `specs/decisions/0006`).
- GitHub Actions checks that no secrets are committed, then builds, runs unit
  tests and lint, with Kotlin warnings treated as errors. Actions are pinned
  by commit SHA.
- README with light and dark screenshots (`scripts/screenshots.sh` retakes
  them all), contributing guide, security policy, MIT license, and the
  project specs and decisions under `specs/`.

[0.5.0]: https://github.com/getfedo/fedo-android-example/releases/tag/v0.5.0
