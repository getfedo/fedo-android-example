# Constitution
- Single activity, unidirectional data flow. Each screen = stateless
  `Screen(state, onAction)` + ViewModel exposing `StateFlow<UiState>`.
- Modules: single `:app` module.
- No business logic in composables. Parsing, formatting and filtering are
  pure functions, unit tested.
- DI: Koin. Navigation: navigation-3 (`NavDisplay` + `@Serializable` `NavKey`
  back stack). HTTP: OkHttp. JSON: kotlinx.serialization.
- No offline support. Remote is the source of truth; the repository caches
  the last successful response in memory for the session. No Room, no
  DataStore, no WorkManager. One exception: the demo user in
  SharedPreferences — see decisions/0004-demo-user-persistence.md.
- UI: use material 3 expressive ui elements.
- Every user-facing string lives in `strings.xml`. Composables read it with
  `stringResource`, counts with `pluralStringResource`, and never concatenate
  sentences. A ViewModel that needs to say something to the user carries a
  `@StringRes Int`, not English text — the string is chosen in the UI layer.
  Data-layer values (a model id, a provider name) are data, not copy.
- Accessibility, the minimum bar: every actionable icon-only control has a
  `contentDescription`; decorative icons pass `null`. Touch targets stay at
  the Material minimum of 48dp — keep the components' defaults rather than
  shrinking them. State is never signalled by colour alone.
- UI tests find nodes by the text the user sees, resolved from the same
  string resource the screen uses, not by a copied literal. A `testTag` is for
  what has no text, never a substitute for semantics.
- A failed refresh never wipes loaded data.
- Every screen composable has a `@Preview` per `UiState`.
- Every acceptance criterion has an ID (`AC-1`, `AC-2`, …) and maps to ≥1
  test named after that ID.
- Fedo SDK: `com.getfedo:sdk-android:0.4.0` (Maven Central).
- Secrets (Fedo API key) come from gitignored `local.properties`. Never
  committed, never hardcoded, never logged.
- Java source/target 21. Kotlin 2.3.21 (set by the Fedo SDK's floor —
  see decisions/0001-library-versions.md).
- Per-bead gate = `./gradlew :app:testDebugUnitTest :app:lintDebug` passes.
- Done = `./gradlew build test lint` passes.
- Emulator: always prefer any adb connected device over emulator.
