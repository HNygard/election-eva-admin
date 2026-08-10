# D004 — `origin/attempt` is a reference, not a source to merge

Date: 2026-08-10

## Decision

The `origin/attempt` branch (`dc1e8e1`, "EVA Admin backend starts up!") is read
for evidence and never merged or cherry-picked. Each change it contains is
re-derived deliberately, declared in `manifest/changes.tsv` with its own reason.

Read it with:

    git diff master origin/attempt --stat
    git diff master origin/attempt -- <path>

## Why

That branch is proof the backend can be made to start, and its diff is the
closest thing available to a list of what the release is actually missing: 342
files, +2266/-301, mostly no-arg constructors added for CDI plus test-scoped
dependencies stripped from poms.

But it was produced by changing whatever was needed to get past the next error,
with no record of why any individual change was made. Merging it would import
342 undeclared modifications and destroy the property that makes this repo useful
for inspection: that every deviation from the published source is visible and
justified.

Re-deriving is slower and produces a smaller, explicable diff. Some of those 342
changes will turn out to be unnecessary once earlier problems are fixed properly.

## Alternatives rejected

- **Merge `attempt` and move on.** Fastest route to a running backend, at the cost of not knowing what was changed or why.
- **Ignore it.** Throws away the one working example we have.
