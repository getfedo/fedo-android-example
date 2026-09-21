# Agent Instructions

ModelPulse — a single-module Android example app that browses the public
OpenRouter model catalogue and showcases the Fedo SDK. The app is
documentation for SDK integrators: keep it small and readable.

**CLAUDE.md is a symbolic link to this file.** Edit AGENTS.md; both tools
read the same rules.

## Specs
Project rules: @specs/constitution.md
Architecture: @specs/architecture.md
UI patterns: @specs/compose-pattern.md
Testing: @specs/testing.md
Recorded decisions: `specs/decisions/`

All tasks live in beads - never in markdown

Workflow:
- Bugs and small tweaks: a single bead, no spec.
- Work from `bd ready`. After each bead: run
  `./gradlew :app:testDebugUnitTest :app:lintDebug`, then close it.
- Extra implementation work found → new bead, discovered-from current one.
- Spec found wrong or incomplete → stop and propose a spec edit first.
- Project rules and decisions go in constitution.md or decisions/,
  not in beads memory.
- Epic closed → its spec is frozen.
- Beads data is local until pushed: `bd dolt push` / `bd dolt pull`. No Dolt
  remote is configured yet, so `.beads/issues.jsonl` is currently the only
  copy that travels with git — and it is an export, not the source of truth.

## Build & Test

```bash
./gradlew :app:testDebugUnitTest      # unit tests — per-bead gate
./gradlew :app:lintDebug              # lint — per-bead gate
./gradlew :app:assembleDebug          # build the APK
./gradlew :app:connectedDebugAndroidTest  # instrumented tests (device needed)
./gradlew build test lint             # full gate, "done" per the constitution
```

## Architecture Overview

ModelPulse: single-activity Compose app that lists AI models from the public
OpenRouter API (`GET https://openrouter.ai/api/v1/models`, no key) and
showcases the Fedo SDK. Single `:app` module.

UI (stateless `Screen` + ViewModel with `StateFlow<UiState>`) → optional
domain layer → `ModelsRepository` → OkHttp remote data source. Remote is the
source of truth; the last successful response is cached in memory only. No
Room, no DataStore, no WorkManager, no offline support.

Full detail: @specs/architecture.md, @specs/compose-pattern.md, @specs/testing.md

## Conventions & Patterns

- DI: Koin. Navigation: Navigation 3 (`NavDisplay` + `@Serializable` `NavKey`
  back stack — no `NavHost`/`NavController`). HTTP: OkHttp. JSON:
  kotlinx.serialization.
- Parsing, formatting and filtering are pure functions with unit tests;
  composables never format.
- Screens are stateless and take every input as a parameter, so they preview
  and test without Koin.
- A failed refresh keeps the loaded list and reports inline; only an empty
  screen becomes an error state.
- Acceptance criteria carry IDs (`AC-1`, …); each maps to ≥1 test named after
  it.
- Library versions are pinned by Kotlin binary compatibility — see
  @specs/decisions/0001-library-versions.md before bumping anything.
- Secrets live in gitignored `local.properties`; never commit or log them.

## Non-Interactive Shell Commands

**ALWAYS use non-interactive flags** with file operations to avoid hanging on confirmation prompts.

Shell commands like `cp`, `mv`, and `rm` may be aliased to include `-i` (interactive) mode on some systems, causing the agent to hang indefinitely waiting for y/n input.

**Use these forms instead:**
```bash
# Force overwrite without prompting
cp -f source dest           # NOT: cp source dest
mv -f source dest           # NOT: mv source dest
rm -f file                  # NOT: rm file

# For recursive operations
rm -rf directory            # NOT: rm -r directory
cp -rf source dest          # NOT: cp -r source dest
```

**Other commands that may prompt:**
- `scp` - use `-o BatchMode=yes` for non-interactive
- `ssh` - use `-o BatchMode=yes` to fail instead of prompting
- `apt-get` - use `-y` flag
- `brew` - use `HOMEBREW_NO_AUTO_UPDATE=1` env var

<!-- BEGIN BEADS INTEGRATION v:1 profile:minimal hash:6cd5cc61 -->
## Beads Issue Tracker

This project uses **bd (beads)** for issue tracking. Run `bd prime` to see full workflow context and commands.

### Quick Reference

```bash
bd ready              # Find available work
bd show <id>          # View issue details
bd update <id> --claim  # Claim work
bd close <id>         # Complete work
```

### Rules

- Use `bd` for ALL task tracking — do NOT use TodoWrite, TaskCreate, or markdown TODO lists
- Run `bd prime` for detailed command reference and session close protocol
- Use `bd remember` for persistent knowledge — do NOT use MEMORY.md files

**Architecture in one line:** issues live in a local Dolt DB; sync uses `refs/dolt/data` on your git remote; `.beads/issues.jsonl` is a passive export. See https://github.com/gastownhall/beads/blob/main/docs/SYNC_CONCEPTS.md for details and anti-patterns.


## Agent Context Profiles

The managed Beads block is task-tracking guidance, not permission to override repository, user, or orchestrator instructions.

- **Conservative (default)**: Use `bd` for task tracking. Do not run git commits, git pushes, or Dolt remote sync unless explicitly asked. At handoff, report changed files, validation, and suggested next commands.
- **Minimal**: Keep tool instruction files as pointers to `bd prime`; use the same conservative git policy unless active instructions say otherwise.
- **Team-maintainer**: Only when the repository explicitly opts in, agents may close beads, run quality gates, commit, and push as part of session close. A current "do not commit" or "do not push" instruction still wins.

## Session Completion

This protocol applies when ending a Beads implementation workflow. It is subordinate to explicit user, repository, and orchestrator instructions.

1. **File issues for remaining work** - Create beads for anything that needs follow-up
2. **Run quality gates** (if code changed) - Tests, linters, builds
3. **Update issue status** - Close finished work, update in-progress items
4. **Handle git/sync by active profile**:
   ```bash
   # Conservative/minimal/default: report status and proposed commands; wait for approval.
   git status

   # Team-maintainer opt-in only, unless current instructions forbid it:
   git pull --rebase
   git push
   git status
   ```
5. **Hand off** - Summarize changes, validation, issue status, and any blocked sync/commit/push step

**Critical rules:**
- Explicit user or orchestrator instructions override this Beads block.
- Do not commit or push without clear authority from the active profile or the current user request.
- If a required sync or push is blocked, stop and report the exact command and error.
<!-- END BEADS INTEGRATION -->
