# 0003 — material3 is pinned ahead of the Compose BOM for expressive

Date: 2026-09-21
Status: accepted

## Context

The constitution requires Material 3 **expressive** UI elements. In
`material3` 1.4.0 — the newest stable, and what compose-bom 2026.02.01 *and*
2026.09.00 both pin — the expressive surface is `internal`:

```
Cannot access 'fun MaterialExpressiveTheme(...)': it is internal in file.
Cannot access 'val titleMediumEmphasized: TextStyle': it is internal in 'Typography'.
```

`MaterialExpressiveTheme`, `MediumFlexibleTopAppBar`, `LoadingIndicator`,
`ContainedLoadingIndicator`, `ButtonGroup` and the `*Emphasized` type styles
are public from `1.5.0-alpha28`. There is no stable release that has them.

0001 rejected navigation3 1.2.0-rc01 for being prerelease. This is the same
kind of choice with the opposite answer, so it is recorded rather than
assumed.

## Decision

Pin `androidx.compose.material3:material3` to **1.5.0-alpha28**, overriding
the BOM for that one artifact. Every other Compose artifact still comes from
compose-bom 2026.02.01.

```toml
composeBom = "2026.02.01"
material3 = "1.5.0-alpha28"
```

The alternative was dropping "expressive" from the constitution and shipping
the stable Material 3 set. Expressive is the reason this example exists in
2026; a stable-only list screen would be a worse piece of documentation than
an alpha dependency is a risk.

## Consequences

- An alpha artifact is on the release classpath. Expressive APIs can be
  renamed or removed between alphas; a bump means re-checking every
  `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` call site.
- Only material3 is overridden. Overriding more of the BOM piecemeal is how
  Compose version skew starts — do not extend this pin without a new
  decision.
- When expressive ships stable, drop the `material3` version ref and go back
  to the BOM. That is the only exit condition worth writing down.

## Verified

- `:app:compileDebugKotlin`, `:app:testDebugUnitTest` and `:app:lintDebug`
  pass with the pin in place.
- The models list runs on a device against the live catalogue.
