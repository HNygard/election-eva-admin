# NF-003 — The whole test suite is skipped

Status: OPEN
Milestone: M1

## Symptom

`tools/build.sh` passes `-Dmaven.test.skip`, so nothing verifies that the code
compiles into something that behaves. A green build currently means very little.

## What is known

Two separate causes are tangled here:

1. **NF-002** — `EVOTE_PROPERTIES` brings down unrelated tests through failed static initialisation.
2. **Missing test infrastructure** — `admin-testing` is not in the release, and the root pom's default Surefire config excludes the groups `repository,slow,erasesTestData,resources`. Tests in those groups need a populated PostgreSQL database that does not exist yet.

On `origin/attempt`, commit `13fba0b` "Removing test scoped dependencies - We skip
tests since they don't work" deleted test-scoped dependencies from several poms.
That makes the symptom disappear by deleting the thing that reports it. Do not
copy it (D004).

## How to approach

Fix NF-002 first, then find out how many tests actually run and pass in the
default "fast tests" group. Record the real number. Only then decide whether the
remaining failures are missing infrastructure (M2 work) or genuine.

## Done when

A finding states how many tests run, pass and fail with tests enabled, and each
failure category is either fixed or has its own item.
