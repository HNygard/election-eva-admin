# Spec — bringing the released EVA Admin source to a running state

Date: 2026-08-10

## Problem

This repo is the published EVA Admin 2019 source (`admin-2019.22.7829`). Three
modules were withheld from publication — `admin-docker`, `admin-testing`,
`admin-other` — along with several files inside the modules that did ship. The
code cannot be built or run as published.

The aim is to get it running with the smallest possible set of changes, while
keeping it obvious at all times what was changed and why, so the result stays
usable as an object of inspection rather than becoming a fork.

## Constraints

- Deviations from the release must be individually visible and justified.
- Nothing is installed on the host; the toolchain is containers (D003).
- Correctness is judged against external reference material, not against the absence of errors (D007).

## Approach

Four milestones — build, backend deployment, full application, drift analysis —
worked in order, with the current one named in `docs/status.md` (D001). The final
target is producing a valgprotokoll (D008).

Changes land in place. Control comes from a checksum manifest plus a declaration
file, enforced by a pre-commit hook (D002). `origin/attempt` is read as evidence
and never merged (D004). The database schema is generated from this repo's entity
classes (D005).

## Components

**`manifest/`** — `BASELINE` names the pristine commit; `originals.sha` pins all
3234 release files by blob sha; `changes.tsv` declares every deviation as
`STATUS<TAB>PATH<TAB>MILESTONE<TAB>REASON`. `tools/manifest-check.sh` compares the
working tree against both and fails on anything undeclared, including stale
declarations. `ADDED_TREE` covers directories of our own scaffolding and is
rejected if it overlaps release paths.

**`tools/`** — `mvn.sh` runs Maven in `maven:3-jdk-8` as the invoking user against
a named `~/.m2` volume; `seed-local-repo.sh` installs the bundled jcoord JAR;
`build.sh` composes the two. `docs-index.sh` regenerates the docs indexes and
`--check`s them. `githooks/pre-commit` runs both checks; `install-hooks.sh` wires
`core.hooksPath` once per clone.

**`docker/postgres/`** — PostgreSQL and Adminer on the coordinates the release
expects: port 15432, database `evote`, user and password `admin`, schema `admin`
with `search_path` set so both schema-qualified and unqualified entities land
together.

**`docs/`** — `status.md` (current milestone and exit criteria), and one file per
item under `decisions/`, `findings/`, `not-fixed-yet/`, `specs/`, plus
`reference/` for the material M3 is judged against (D006).

## Data flow of a work session

An agent reads `docs/status.md`, opens `docs/not-fixed-yet/INDEX.md`, takes the
topmost `OPEN` item for the current milestone, and works it. Any file it changes
inside the release tree gets a row in `manifest/changes.tsv`. Whatever it learns —
including failed attempts, with the verbatim error — becomes a file in
`docs/findings/`. The item's `Status:` line is updated. The pre-commit hook
refuses the commit if the manifest or an index is inconsistent.

## Error handling

The manifest check is the only enforcement point, and it fails closed: unknown
status values, missing reasons, duplicate rows, stale declarations and overlapping
`ADDED_TREE` prefixes all fail. It never modifies anything.

`.gitignore` exists only to keep build output and bulky reference material out of
the check. It must never be used to hide source changes; anything inside `admin/`
is outside its remit by construction, since `ADDED_TREE` cannot cover release
paths.

## Verification

- **The manifest control**: `tools/manifest-check.sh` on the untouched tree reports 3233 original / 1 modified, and fails when a release file is edited without a declaration.
- **M1**: `tools/build.sh` exits 0 twice from an empty Maven volume.
- **M2**: WildFly reports `Deployed` for `admin-backend.war`, with the log excerpt recorded.
- **M3**: pages compared against reference screenshots; a valgprotokoll produced end to end.
- **M4**: a written, sourced comparison against today's published EVA Admin.

Every claim of success is backed by recorded command output. "It should work" is
not a result.

## Out of scope

Reimplementing the withheld modules as such, updating the code to current EVA
Admin behaviour (that is M4's output, not its method), and anything requiring the
JasperSoft report server or Buypass certificate infrastructure, neither of which
is published.
