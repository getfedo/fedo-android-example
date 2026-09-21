# 0001 — Library stack and version pins

Date: 2026-09-21
Status: accepted

## Context

Toolchain: Gradle 9.5.0, AGP 9.3.3, Compose BOM 2026.02.01, JDK toolchain
25, `minSdk` 29 / `targetSdk` 37, Java source/target **21**.

The binding constraint is the **Fedo SDK** (`com.getfedo:sdk-android:0.4.0`,
Maven Central). It is built on Kotlin **2.3.10** and drags the whole graph
forward: `kotlin-stdlib` resolves to **2.3.21**, `kotlinx-serialization-json`
to 1.11.0, plus Ktor 3.5.0, Compose Multiplatform 1.11.1, DataStore 1.1.7
and Tink 1.11.0.

Everything below was verified by compiling and assembling, not inferred.

## Decision

Kotlin **2.3.21**. AGP 9's built-in Kotlin defaults to 2.2.10; declaring the
Kotlin plugin version in the catalog overrides it, and 2.3.21 builds clean
on AGP 9.3.3. Staying on 2.2.10 also compiles — the Kotlin compiler reads
metadata one minor version ahead — but it means a 2.2 compiler against a
2.3.21 stdlib. Matching the compiler to the stdlib the SDK forces is
cheaper than tracking which mismatch is still tolerated.

| Need | Choice | Version |
|------|--------|---------|
| Kotlin | Kotlin Gradle plugin / compose compiler | `2.3.21` |
| Fedo SDK | `com.getfedo:sdk-android` | `0.4.0` |
| DI | Koin (BOM) | `4.2.2` |
| HTTP | OkHttp (BOM) | `5.5.0` |
| JSON | kotlinx-serialization-json | `1.11.0` |
| Navigation | androidx.navigation3 runtime + ui | `1.1.7` |
| Nav3 ViewModel scoping | androidx.lifecycle-viewmodel-navigation3 | `2.11.0` |
| Coroutines test | kotlinx-coroutines-test | `1.11.0` |
| Flow test | Turbine | `1.2.1` |

`kotlinx-serialization-json` is pinned to 1.11.0 because the SDK brings that
version transitively; pinning anything lower is pointless, Gradle upgrades
it anyway.

All `lifecycle-*` artifacts share one `lifecycle = "2.11.0"` ref so runtime,
compose and navigation3 cannot drift apart.

Rejected:

- **Navigation3 1.2.0-rc01** — prerelease; 1.1.7 is the newest stable. (The
  one prerelease this project does accept is `material3`, because expressive
  is internal in every stable release — see
  [0003](0003-material3-expressive.md).)
- **Retrofit** — OkHttp plus one `Request` covers a single unauthenticated
  GET. A second HTTP abstraction earns nothing here.
- **Room / DataStore / WorkManager in app code** — no offline requirement.
  (The SDK pulls DataStore in for its own storage; that is its business.)
- **Ktor as the app's client** — the SDK already ships Ktor, so reusing it
  would save APK size, but OkHttp keeps the example readable for integrators
  who do not know Ktor, and the SDK's client is internal.

## Verified

- `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug` all pass
  with the SDK on the classpath at Kotlin 2.3.21.
- Kotlin 2.2.10 against the SDK also compiles (metadata `mv=[2,3,0]` is
  readable by a 2.2 compiler). Recorded so nobody re-derives it.

## Known problem: the SDK ships its test dependencies

`sdk-android:0.4.0` declares `kotlin-test`, `kotlin-test-junit` and
`ktor-client-mock` at **`runtime` scope**. They land on
`debugRuntimeClasspath` and are dexed into the APK — `Lorg/junit/…` and
`MockEngine` are present in `classes7.dex`/`classes8.dex` of a debug build.
The debug APK is ~46 MB.

This is a packaging bug in the SDK (`kusa-software/fedo-sdk`), not something
this repo can fix properly. Workaround if it blocks a release:

```kotlin
implementation(libs.fedo.sdk) {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-test")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    exclude(group = "io.ktor", module = "ktor-client-mock")
}
```

Prefer fixing the SDK's publication (`testImplementation`, not `api`/
`implementation`) and releasing 0.4.1.

## Consequences

- Kotlin, the compose compiler plugin and the serialization plugin all move
  together on the `kotlin` version ref. Keep it that way.
- Bumping the Fedo SDK means re-checking the Kotlin version it was built
  with, since it sets the floor for the whole graph.
- `koin-androidx-compose` and `koin-compose-viewmodel` are both needed:
  `koinViewModel { parametersOf(key) }`, used by Navigation 3, comes from
  `org.koin.compose.viewmodel`.
