# D001 — Work in four staged milestones

Date: 2026-08-10

## Decision

Getting EVA Admin running is split into four milestones, worked in order:

1. **M1** reproducible build
2. **M2** backend deploys against PostgreSQL in WildFly
3. **M3** full app, verified against archived valgmedarbeiderportalen material, producing a valgprotokoll
4. **M4** determine how far the 2019 release has drifted from today's system

`docs/status.md` names the current one. Every work item and every declared file
change is tagged with the milestone it belongs to.

## Why

The release is missing three whole modules (`admin-docker`, `admin-testing`,
`admin-other`), so there is no single fix that makes it run. Without an explicit
current milestone, an agent picking up the work has no way to tell whether a
given failure is in scope or a distraction — reconstructing Jasper report
templates is real work, but not while the build still fails.

Tagging changes by milestone also keeps the diff-against-release answerable per
stage: "what did M2 cost us" is a grep.

## Alternatives rejected

- **One goal, "make it work".** Gives no way to decide what to ignore, and no way to tell progress from motion.
- **Build only, deployment out of scope.** The point is to see the system run; stopping at artifacts would not reach it.
