# Changelog

All notable changes to this project are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Each release states the Fedo SDK version it targets.

## [Unreleased]

Targets Fedo SDK `0.4.0`.

### Added

- Android project skeleton: single `:app` module, Compose, Material 3,
  `minSdk` 29 / `targetSdk` 37, Java 21 by @mabd-agent
- Fedo SDK `com.getfedo:sdk-android:0.4.0`, initialized at app startup when an
  API key is present. Without a key the app runs and logs a hint instead by @mabd-agent
- `BuildConfig.FEDO_API_KEY`, read from the gitignored `local.properties` and
  empty by default so a clean clone still builds. Documented in
  `local.properties.example` @mabd-agent
- OkHttp and kotlinx.serialization for the OpenRouter client by @mabd-agent
- Project specs under `specs/`: constitution, architecture, Compose patterns,
  testing, and recorded decisions by @mabd-agent
- OpenRouter data layer: `AiModel` with the derived provider, short name,
  prices and context window, plus `OpenRouterDataSource.getModels()` returning
  the catalogue newest first as a `Result`, never throwing by @mabd-agent
- GitHub Actions CI: a secrets guard job, then assemble, unit tests and lint on
  a keyless checkout, with every action pinned by commit SHA. Kotlin warnings
  are errors in CI via `-PwarningsAsErrors=true` by @mabd-agent
- The model detail copy action moved from the deprecated
  `LocalClipboardManager` to `LocalClipboard` by @mabd-agent
- Provider ids that share a display name now merge into one filter entry:
  OpenRouter ships both `meta/…` and `meta-llama/…` as "Meta", which used to
  render as two identical chips. Selecting the entry filters on every id
  behind it by @mabd-agent
- `README.md`, `CONTRIBUTING.md`, `SECURITY.md` and `CODE_OF_CONDUCT.md`,
  ported from the iOS example and rewritten for the Gradle/Android workflow,
  plus `.github/` issue forms, PR template and dependabot config by @mabd-agent
- Screenshots in `docs/images/`, captured on a device by @mabd-agent
- A failed refresh now shows a snackbar with Retry instead of an item at the
  top of the list, so it is visible wherever the list is scrolled by @mabd-agent
- MIT `LICENSE` (Copyright 2026 Fedo) and a short `README.md` linking it, with
  a note that the Fedo SDK ships under its own license by @mabd-agent
- `app/src/test/resources/models.json`: a trimmed real OpenRouter response
  covering the decoder's awkward cases — a `~`-prefixed id, `"0"` and `"-1"`
  prices, a null `context_length`, a missing `architecture` block and an
  unknown field — with tests that decode it through the production `Json`
  config by @mabd-agent
- Fedo SDK test dependencies (`kotlin-test`, `kotlin-test-junit`,
  `ktor-client-mock`) excluded from the app's runtime classpath: the SDK
  publishes them at runtime scope, so they were dexed into the APK. The debug
  APK drops from ~80 MB to ~62 MB by @mabd-agent
- Settings screen: Fedo SDK status and a demo account. Signing in calls
  `setUserID` (a generated `demo-` id, never the email), `setUserDisplayName`
  and `setUserEmail`; signing out calls `logout()`. The demo user is kept in
  SharedPreferences so sign-in survives a restart, and the controls are
  disabled with an explanation when no API key is configured by @mabd-agent
- `specs/decisions/0004-demo-user-persistence.md`: why the demo user is the
  one thing this app persists by @mabd-agent
- Roadmap destination hosting the Fedo feedback board (`FedoFeedbackScreen`),
  which owns its internal navigation; backing out of its root returns to the
  models list. Without an API key the tab explains how to add one instead of
  disappearing by @mabd-agent
- `specs/fedo-showcase.md`: where each Fedo surface sits, what demo sign-in
  sets, what the app does without an API key, and why Fedo failures get no
  invented error UI by @mabd-agent
- Model detail screen, opened by tapping a row: provider, input and output
  price per 1M tokens, context window, release date, input modalities and the
  full scrolling description, plus the model id in monospace with a copy
  action. An id missing from the catalogue shows a not-found state
  by @mabd-agent
- App navigation scaffold: a bottom navigation bar with Models, Roadmap and
  Settings, built on Navigation 3 (`NavDisplay` + a `@Serializable` `NavKey`
  back stack). Models is the back-stack root, so system back from any other
  destination returns to it. Roadmap and Settings are placeholders until the
  Fedo screens land by @mabd-agent
- Search and provider filter on the models list: search matches a model's name
  or id case-insensitively, the provider chips count their models and list the
  biggest first, and the two combine. Providers group by the stable
  `providerSlug`, so a refresh that drops the selected provider clears the
  selection instead of stranding an empty screen. A search or filter that
  matches nothing shows a no-results state, distinct from an empty catalogue
  by @mabd-agent
- `ic_search` and `ic_close` vector drawables, inlined from Material Symbols so
  the search field has icons without the `material-icons` artifact by @mabd-agent
- Pure formatters `perMillionLabel()`, `contextLabel()` and `relativeLabel()`,
  covering "Free", "Variable" and the "<$0.01" floor, with unit tests by @mabd-agent

- Models list screen: newest models first, with loading, empty and error
  states, pull to refresh, and an inline notice when a refresh fails while
  models are already on screen — the list is never wiped by @mabd-agent
- `ModelsRepository`, which keeps the last successful response in memory for
  the session. A refresh started while a load is already running joins it and
  shares its result, so two callers never cause two fetches and neither is
  left with a cancellation instead of a result by @mabd-agent
- Koin wiring for the data and UI layers, started from
  `ModelPulseApplication` by @mabd-agent

### Changed

- `specs/architecture.md` now matches the shipped data layer: the data source
  and repository snippets compile, sorting is documented as the data source's
  job, and the mapping shows the lowercased provider slug and the id-prefix
  fallback for the provider name by @mabd-agent
- `OpenRouterDataSource` is `internal`, as the architecture spec states by @mabd-agent
- `material3` is pinned to 1.5.0-alpha28, ahead of the Compose BOM, because
  the Material 3 expressive APIs are `internal` in every stable release.
  Recorded in `specs/decisions/0003-material3-expressive.md` by @mabd-agent
- The app theme is `MaterialExpressiveTheme` by @mabd-agent
- R8 is enabled for the release build, which strips the Fedo SDK's leaked test
  dependencies from the release APK: 46 MB debug against 2.5 MB release.
  Recorded in `specs/decisions/0002-release-optimization.md` by @mabd-agent

[Unreleased]: https://github.com/getfedo/fedo-android-example/commits/main
