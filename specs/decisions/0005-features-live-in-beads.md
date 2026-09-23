# 0005 — Features live in beads, not in a spec folder

Date: 2026-09-23
Status: accepted

## Context

The agent instructions once described a spec-first workflow: every non-trivial
feature got `specs/features/NNN-name/` with `spec.md`, `plan.md` and
`tasks.md`, filed as an epic only after the spec was approved.

That is not what happened. All four epics were filed straight into beads with
child tasks and acceptance criteria, `specs/features/` was never created, and
nothing was lost by its absence. The instructions still pointed at the
directory, so the rule could not be followed or checked.

## Decision

Beads is the only place a feature is described. An epic plus its child beads,
each carrying `AC-n` criteria that map to tests, is the whole specification.

`specs/` holds what outlives any one feature: the constitution, the
architecture guide, the Compose and testing patterns, the Fedo showcase
surface, and `decisions/`.

A feature that needs a written design others will build on — an integration
surface, a cross-cutting rule — adds or updates a file in `specs/` and records
the why here in `decisions/`. That work is its own bead. The Fedo showcase
spec (`uyb.5` → `specs/fedo-showcase.md`) is the shape to copy.

Rejected:

- **Restoring spec-first and backfilling four epics.** The specs would be
  written after the code, which makes them transcription, not design. The
  beads already carry the criteria, and the code already has the tests.
- **Keeping both.** Two descriptions of one feature drift; the one nobody runs
  tests against is the one that goes stale.

## Consequences

- A bead is the unit of work and of description. Criteria belong in its
  acceptance field, with IDs, per the constitution.
- `specs/` grows only when a rule or an interface needs to outlive a bead.
- A closed epic's decisions are frozen: change them with a new record here,
  never by editing an accepted one.
