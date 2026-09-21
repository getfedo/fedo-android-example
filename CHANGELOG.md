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
- Pure formatters `perMillionLabel()`, `contextLabel()` and `relativeLabel()`,
  covering "Free", "Variable" and the "<$0.01" floor, with unit tests by @mabd-agent

### Changed

- `specs/architecture.md` now matches the shipped data layer: the data source
  and repository snippets compile, sorting is documented as the data source's
  job, and the mapping shows the lowercased provider slug and the id-prefix
  fallback for the provider name by @mabd-agent
- `OpenRouterDataSource` is `internal`, as the architecture spec states by @mabd-agent
- R8 is enabled for the release build, which strips the Fedo SDK's leaked test
  dependencies from the release APK: 46 MB debug against 2.5 MB release.
  Recorded in `specs/decisions/0002-release-optimization.md` by @mabd-agent

[Unreleased]: https://github.com/getfedo/fedo-android-example/commits/main
