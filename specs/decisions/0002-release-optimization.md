# 0002 — R8 is on for the release build

Date: 2026-09-21
Status: accepted

## Context

`app/build.gradle.kts` shipped with `release { optimization { enable =
false } }` and no note saying why. That is not cosmetic here: the Fedo SDK
0.4.0 declares `kotlin-test`, `kotlin-test-junit` and `ktor-client-mock` at
runtime scope (see 0001-library-versions.md and bead 8nq.6), so they land on
`releaseRuntimeClasspath` and get dexed. Shrinking is the only thing that
strips them from a release APK without patching the SDK's publication.

## Decision

`optimization { enable = true }` on `release`. No app keep rules are needed:
`src/main/keepRules/rules.keep` stays at its template contents, and the
libraries that need rules (Compose, OkHttp, kotlinx.serialization, the Fedo
SDK) ship their own consumer rules.

## Verified

- `:app:assembleRelease` succeeds, `lintVitalRelease` included.
- The release APK is **2.5 MB** against **46 MB** for debug, one `classes.dex`
  instead of eight.
- `strings classes.dex` finds no `org/junit` and no `MockEngine`: the SDK's
  leaked test dependencies are gone from the release build.
- Installed (signed with the local debug key) and launched on a device;
  `MainActivity` reaches `topResumedActivity` with no crash.

## Consequences

- A release build now depends on R8 being correct for whatever the app adds
  next. Reflection, `Class.forName`, JNI and custom serializers need keep
  rules in `src/main/keepRules/`; a crash that only reproduces in release is
  the first place to look.
- There is still no `signingConfig`, so `assembleRelease` produces an unsigned
  APK. Signing is the integrator's business, not this example's.
- This does not fix the SDK bug, it hides it from the release APK. The debug
  APK is still ~46 MB. 8nq.6 tracks the real fix.

## Rejected

- **Leaving optimization off with a comment.** Cheaper, but it hands
  integrators an example whose release build is 46 MB and contains JUnit.
- **Excluding the test artifacts in `dependencies { }`.** Recorded as a
  workaround in 0001; R8 already solves the release side and the exclusions
  would not shrink anything else.
