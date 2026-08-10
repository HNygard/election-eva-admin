# D002 — Change files in place, control them with a manifest

Date: 2026-08-10

## Decision

Files are fixed where they live. There is no overlay directory and no patch
stack. Control comes from a manifest that pins every release file to its git blob
sha, plus a declaration file for every deviation:

- `manifest/BASELINE` — which commit is the pristine release
- `manifest/originals.sha` — generated, `<blob-sha><TAB><path>` for all 3234 release files
- `manifest/changes.tsv` — curated, `STATUS<TAB>PATH<TAB>MILESTONE<TAB>REASON`

`tools/manifest-check.sh` fails if a release file changed without a declaration,
if a declaration is stale, or if a new file appeared undeclared. It runs as a
pre-commit hook, so drift cannot reach a commit.

`ADDED_TREE` declares a whole directory of our own scaffolding at once, and is
rejected if its prefix overlaps any release path — so it can never be used to
mask an edit to EVA code. Inside the release tree, every changed file needs its
own row and its own reason.

## Why

An overlay keeps the original tree pristine but makes the running system differ
from the checked-out system, which is exactly the confusion this project cannot
afford: when something breaks, the first question is always "what is actually
deployed". In-place editing keeps one truth on disk.

The risk of in-place editing is losing track of what we touched. A checksum
manifest removes that risk more thoroughly than an overlay does, because it also
catches accidental edits — an IDE reformat, a stray sed — that an overlay would
happily carry along.

Blob shas rather than a separate checksum because git already computes them, and
`git hash-object --stdin-paths` verifies the whole tree in one process.

## Alternatives rejected

- **Overlay directory + apply script.** Two versions of every reconstructed file; the deployed one is whichever the script last copied.
- **Patch files applied with `git apply`.** Brittle across any reordering, and a `.patch` is harder to read than the file it produces.
- **Separate Maven overlay module.** Purest, but adds real build complexity for a codebase we do not yet know how to build.
- **Edit freely, write notes afterwards.** Notes rot; a hook does not.
