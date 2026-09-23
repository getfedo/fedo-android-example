# Contributing

Thanks for helping improve ModelPulse, the example app for the Fedo Android SDK (`com.getfedo:sdk-android`). This app is documentation: people read it to learn how to integrate Fedo, so changes should keep it small and easy to follow.

By participating you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Prerequisites

- JDK 21 or later (the build targets Java 21; the Gradle toolchain resolves the rest)
- Android Studio, or just the command line — the Gradle wrapper (`./gradlew`) downloads Gradle 9.5.0 itself
- An Android device or emulator on API 29 or newer (`minSdk` 29, `targetSdk` 37)

Everything else — AGP 9.3.3, Kotlin 2.3.21, the Compose BOM and the Fedo SDK — is resolved by Gradle. Do not bump versions without reading [`specs/decisions/0001-library-versions.md`](specs/decisions/0001-library-versions.md); the Fedo SDK sets the Kotlin floor for the whole graph.

## Setup

1. Clone the repository:

   ```sh
   git clone https://github.com/getfedo/fedo-android-example.git
   cd fedo-android-example
   ```

2. Optional: add a Fedo API key. The app builds, runs and tests without one; the Fedo-powered screens then explain how to set it up.

   ```sh
   cp local.properties.example local.properties
   ```

   Set `FEDO_API_KEY` in `local.properties`. The file is gitignored, and the
   key reaches the app as `BuildConfig.FEDO_API_KEY`. If you already have a
   `local.properties` (Android Studio writes one with `sdk.dir`), just add the
   `FEDO_API_KEY=` line to it instead of overwriting the file.

3. Build and install on a connected device or a running emulator:

   ```sh
   ./gradlew :app:installDebug
   ```

   Or open the project in Android Studio and press Run.

## Build and test

Run this gate after every change:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug
```

Instrumented Compose tests need a device or emulator:

```sh
./gradlew :app:connectedDebugAndroidTest
```

Before opening a pull request, run the full gate:

```sh
./gradlew build test lint
```

## Project layout

Single `:app` module, package `com.fedo.modelpulse`.

| Path | Contents |
| --- | --- |
| `app/src/main/java/com/fedo/modelpulse/ModelPulseApplication.kt` | App entry point; starts Koin and initializes Fedo when an API key is configured |
| `app/src/main/java/com/fedo/modelpulse/MainActivity.kt` | The single activity; hosts the Compose tree |
| `app/src/main/java/com/fedo/modelpulse/FedoIntegration.kt` | The one place that decides whether the SDK is configured |
| `app/src/main/java/com/fedo/modelpulse/data/` | `AiModel` and its formatters, filtering, `ModelsRepository`, `OpenRouterDataSource` |
| `app/src/main/java/com/fedo/modelpulse/di/Modules.kt` | Koin modules, one per layer |
| `app/src/main/java/com/fedo/modelpulse/ui/navigation/` | Navigation 3 keys and `NavDisplay` |
| `app/src/main/java/com/fedo/modelpulse/ui/models/` | Models list: screen, UI state, ViewModel |
| `app/src/main/java/com/fedo/modelpulse/ui/detail/` | Model detail screen |
| `app/src/main/java/com/fedo/modelpulse/ui/roadmap/` | Roadmap destination hosting the Fedo feedback screen |
| `app/src/main/java/com/fedo/modelpulse/ui/settings/` | Demo sign-in and sign-out (Fedo user identity) and Fedo setup status |
| `app/src/main/java/com/fedo/modelpulse/ui/theme/` | Material 3 theme, typography and icons |
| `app/src/test/` | JVM unit tests: parsing, formatting, filtering, ViewModels |
| `app/src/androidTest/` | Instrumented Compose tests |
| `local.properties.example` | Template for the gitignored `local.properties` |
| `specs/` | Project rules, architecture, Compose and testing patterns, decisions |

## Guidelines

The rules are written down in [`specs/constitution.md`](specs/constitution.md); read it before a non-trivial change. The short version:

- Jetpack Compose and Material 3 expressive. Single activity, unidirectional data flow: each screen is a stateless `Screen(state, onAction)` plus a ViewModel exposing `StateFlow<UiState>`.
- No business logic in composables. Parsing, formatting and filtering are pure functions with unit tests.
- Koin for DI, Navigation 3 for navigation, OkHttp for HTTP, kotlinx.serialization for JSON. Do not add dependencies beyond these and the Fedo SDK.
- No offline support: remote is the source of truth, the repository caches the last response in memory, and a failed refresh never wipes loaded data.
- Every screen composable has a `@Preview` per `UiState`.
- Acceptance criteria carry IDs (`AC-1`, `AC-2`, …) and each maps to at least one test named after its ID. See [`specs/testing.md`](specs/testing.md).
- Keep the example minimal and readable. Prefer the obvious solution over abstractions; comment only where the reason is not clear from the code.
- Build with zero warnings and no new lint findings. CI enforces this with
  `-PwarningsAsErrors=true`; run `./gradlew :app:assembleDebug -PwarningsAsErrors=true`
  locally if you want the same check before pushing.

If a spec turns out to be wrong or incomplete, propose the spec change first rather than working around it.

## Workflow

1. Create a branch from `main`.
2. Make your change, then run `./gradlew :app:testDebugUnitTest :app:lintDebug`.
3. Add an entry under `## [Unreleased]` in [`CHANGELOG.md`](CHANGELOG.md).
4. Open a pull request against `main` and fill in the template.

Never commit `local.properties` or any API key, and never paste keys in issues, pull requests or logs.

## Issue tracking

Maintainers plan work with [beads](https://github.com/gastownhall/beads) (`bd`); its data lives in `.beads/`. You do not need it: external contributors can use [GitHub issues](https://github.com/getfedo/fedo-android-example/issues).

## Remotes

Development happens on the Gitea remote; GitHub is where the repository is
published, the same arrangement as the iOS example. CI, issue forms, the
security advisory link and the README badge all point at the GitHub home, so
they only come alive once the code is pushed there.

## Where to report

- Bugs and ideas for this example app: [GitHub issues](https://github.com/getfedo/fedo-android-example/issues) in this repository.
- Bugs in the Fedo Android SDK itself: [kusa-software/fedo-sdk issues](https://github.com/kusa-software/fedo-sdk/issues).
- Security vulnerabilities: privately, as described in [SECURITY.md](SECURITY.md).
- Conduct concerns: see the [Code of Conduct](CODE_OF_CONDUCT.md).
