# 0006 — material3 held at 1.5.0-alpha01 so the Fedo sheet works

Date: 2026-09-23
Status: accepted

## Context

`FedoCreateFeedbackSheet` killed the app the moment it composed:

```
java.lang.NoSuchMethodError: No static method ModalBottomSheet-dYc4hso(…)
  at com.fedo.sdk.internal.ui.sheet.InternalFeedbackSheetKt.InternalFeedbackSheet
```

The SDK depends on JetBrains Compose `material3 1.8.0`, which maps to an
androidx `material3` whose `ModalBottomSheet` has that signature. This project
pinned androidx `material3 1.5.0-alpha28` for expressive (decision 0003), and
androidx replaced the overload somewhere in between. At runtime the androidx
artifact wins, the SDK's call finds no matching method, and the process dies.

Measured, not guessed — which versions still carry the overload the SDK calls:

| material3 | `ModalBottomSheet-dYc4hso` |
|-----------|---------------------------|
| 1.3.2 | yes |
| 1.4.0 | yes (plus the replacement) |
| 1.5.0-alpha01 | yes |
| 1.5.0-alpha10 and later | no |

And what each choice costs this app:

| Pin | Expressive | Sheet |
|-----|-----------|-------|
| 1.4.0 | no — 19 compile errors, `MaterialExpressiveTheme`, `MediumFlexibleTopAppBar`, `ContainedLoadingIndicator`, `LoadingIndicator` and the `*Emphasized` type styles are all internal | works |
| 1.5.0-alpha01 | yes — two extra `@OptIn` call sites | works |
| 1.5.0-alpha28 | yes | crashes |

## Decision

Hold `material3` at **1.5.0-alpha01**. It is the newest version that keeps
both promises: Material 3 expressive stays public, and the SDK's sheet
composes. Verified on a device: the sheet opens from the models list's
no-results and error states, and dismissing it leaves the list untouched.

Rejected:

- **1.4.0 stable.** The constitution requires expressive components, and this
  gives them up across three files to buy nothing the alpha does not.
- **Keeping alpha28 and leaving the sheet unwired.** That drops a showcase
  surface from an app whose entire job is showing the SDK's surfaces.
- **Excluding JetBrains material3 from the SDK.** It is not the artifact that
  loses; androidx is already the one on the classpath.

## Consequences

- The pin is a workaround for an SDK built against older material3. When the
  SDK is rebuilt against current androidx material3, move back to the newest
  alpha and delete this constraint — bead `8nq.7` carries that.
- Alpha01 is ~27 alphas behind: fixes landed since are not here. It also pulls
  an older compose-ui-test, so the UI tests use `createComposeRule`, not the
  `junit4.v2` variant.
- Bumping `material3` now means re-checking `ModalBottomSheet` against the
  SDK, not just that the app compiles.
