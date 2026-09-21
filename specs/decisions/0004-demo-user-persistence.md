# 0004 — The demo user is persisted, in SharedPreferences

Date: 2026-09-21
Status: accepted

## Context

The constitution says the app has no offline support: no Room, no DataStore,
no WorkManager, and the repository caches the catalogue in memory only.

The Settings showcase needs the opposite for one small thing. Fedo migrates a
guest's feedback, votes and comments to an account on sign-in. If signing in
does not survive a restart, every cold start is a new guest and the migration
story — the reason the demo exists — cannot be demonstrated.

## Decision

The demo user (`id`, `name`, `email`) is stored in `SharedPreferences`, under
one file, by `DemoUserStore`. Nothing else in the app is persisted.

SharedPreferences, not DataStore: three strings written on a button tap do not
need a `Flow`, a serializer or a coroutine, and DataStore is on the
constitution's no-list. The whole store is thirty lines and needs no
dependency.

`SettingsViewModel` takes `loadUser` and `saveUser` as lambdas, so its sign-in
rules are unit tested without Android.

## Consequences

- The catalogue rule is unchanged: models are still memory-only, and a cold
  start still hits the network.
- `specs/fedo-showcase.md` says the demo id survives restarts; this file is
  why the constitution's "no persistence" line has exactly one exception.
- Adding a second thing to persist is a new decision, not a precedent.
