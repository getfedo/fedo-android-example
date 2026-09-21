# Fedo SDK Showcase

How ModelPulse presents the Fedo SDK. The app is documentation for
integrators, so every Fedo surface exists to show one integration point and
nothing more.

## The SDK surface we use

`com.getfedo:sdk-android:0.4.0` exposes exactly this:

| API | Use here |
|-----|----------|
| `Fedo.initialize(context, apiKey) { }` | Once, in `Application.onCreate`, only when a key is present |
| `Fedo.setUserID(String)` | Demo sign-in |
| `Fedo.setUserDisplayName(String)` | Demo sign-in |
| `Fedo.setUserEmail(String)` | Demo sign-in |
| `Fedo.setUserProperty(key, value)` | `favorite_provider` from the models filter |
| `Fedo.logout()` | Demo sign-out |
| `FedoFeedbackScreen(modifier, slots, style, onDismiss)` | The Roadmap destination |
| `FedoCreateFeedbackSheet(onDismiss, modifier)` | Contextual feedback |

Everything else in the artifact is `internal`. All of these return `Unit`.

## Placement

Three top-level destinations in the Navigation 3 back stack, in this order:
**Models**, **Roadmap**, **Settings**. Models is the back-stack root, so
system back from Roadmap or Settings returns to the list rather than leaving
the app (`backStackFor()` in `ui/navigation`).

### Roadmap — `RoadmapKey`

Hosts `FedoFeedbackScreen` full-bleed, below our `NavigationBar`. The screen
carries its own `NavHostController` internally: it owns its list → detail →
compose navigation and its own back handling. Our `onDismiss` lambda pops
*our* back stack, and the SDK only calls it when its internal stack is
already at its root. ModelPulse must not wrap the screen in another top app bar — the
SDK draws its own.

### Contextual feedback — `FedoCreateFeedbackSheet`

Opened from the models list, not from a destination of its own:

- the **no-results** state ("nothing matched your search") — the search that
  failed is the feedback worth collecting;
- the **error** state, next to Retry.

The sheet is dismissed through its `onDismiss`; the list state underneath is
untouched.

### Settings — `SettingsKey`

Shows SDK status and the demo sign-in. This is the screen an integrator reads
to see which calls exist.

## Demo sign-in

"Demo sign-in" is a local, fake identity. There is no auth backend in this
example and there is no sign-in form. Tapping **Sign in as demo user** calls,
in this order:

```kotlin
Fedo.setUserID(demoUserId)                  // "demo-" + a UUID, generated once
Fedo.setUserDisplayName("Demo User")
Fedo.setUserEmail("demo@modelpulse.example")
```

- `demoUserId` is generated on first sign-in and kept for the process only.
  There is no DataStore in this app (constitution), so a cold start signs in
  as a new demo user. That is honest for a demo and keeps the rule intact.
- `demo@modelpulse.example` is a reserved example domain — never a real
  address.
- **Sign out** calls `Fedo.logout()` and nothing else.
- `favorite_provider` (`Fedo.setUserProperty`) is set from the models provider
  filter, and cleared to `""` when the filter is cleared.

## No API key

`local.properties` is gitignored, so a clean clone has no key and
`BuildConfig.FEDO_API_KEY` is empty. `FedoIntegration.isConfigured` is the one
place that decides this. When it is false:

- `Fedo.initialize` is **not** called;
- the Roadmap destination stays in the bottom bar and renders a short
  explainer: what the Fedo board is and the two lines of
  `local.properties` that enable it. The tab is never hidden — a missing key
  must teach, not disappear;
- the contextual feedback entry points are not shown;
- Settings shows "SDK not configured" and disables the demo sign-in controls;
- everything else — the catalogue, search, filter, detail — works normally.

The key is never logged, never shown in the UI, and never committed. Settings
reports *whether* a key is present, never its value or any prefix of it.

## Failures

The SDK's public API returns `Unit` and reports nothing back to the caller: a
network failure inside `FedoFeedbackScreen` is the SDK's own UI to handle, and
`setUserID` and friends cannot fail visibly. So ModelPulse **does not invent
error UI for Fedo**. The only Fedo condition the app surfaces is the one it
can actually observe — the missing API key.

The SDK is never in the path of the catalogue. If a Fedo call throws, the
models list, search, filter and detail keep working.

## Testing

`isFedoConfigured()` is a pure function and is unit tested. The Fedo
composables are not unit tested: they are the SDK's UI, and an example app
testing someone else's screens is noise. Our side of each surface — the
placement, the configured/unconfigured branch, the demo user fields — is
tested through the state that drives it, and verified once on a device.
