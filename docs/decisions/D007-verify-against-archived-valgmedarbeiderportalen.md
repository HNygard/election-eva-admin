# D007 — Verify M3 against archived valgmedarbeiderportalen material

Date: 2026-08-10

## Decision

"The application works" is judged against archived copies of
valgmedarbeiderportalen and the official instruction manuals, which contain
screenshots of how each screen should look. Reference material lives in
`docs/reference/`, indexed by which screen each screenshot shows.

The material is bulky and externally owned, so `docs/reference/manuals/` and
`docs/reference/valgmedarbeiderportalen/` are gitignored. `docs/reference/INDEX.md`
is tracked and records what belongs there, where it came from, and its checksum,
so a missing file is detectable rather than silently absent.

## Why

Without an external reference, "it renders" is the only available standard, and a
JSF page that renders with empty models looks a lot like one that works. The
manuals show what a correct screen contains — which fields, which navigation,
which state — so a comparison can fail for the right reasons.

Checksums in a tracked index rather than the files themselves: this repo is a
copy of published source code, and bulking it up with third-party PDFs makes it
harder to see the code changes that matter.

## Consequence for M4

The archived portal describes the system as it was around the 2019 release. Where
it disagrees with what the code does, that gap is M4's subject matter, not
necessarily a defect in our setup.

## Alternatives rejected

- **Judge by "no exceptions in the log".** Passes for screens that are entirely empty.
- **Commit the manuals.** Large binaries in a source-inspection repo, with unclear redistribution footing.
