# NF-023 — The page authorisation map is unrecoverable

Status: OPEN — disabled, not solved
Milestone: M3

## Symptom

    WELD-000049: Unable to invoke public void PageAccess.init()
    Caused by: java.lang.NullPointerException

`PageAccess.load()` reads `/PageAccess.properties` from the classpath root and
dereferences the stream without checking it. The release ships no such file, so
every secure page fails.

## Why it cannot be reconstructed

The file mapped pages to required accesses: 207 secure pages against the 156
constants of `no.valg.eva.admin.common.rbac.Accesses`. Nothing published records
which page needed which — no page references an access name, no class holds a
fallback map.

## What was done instead

`tools/generate-page-access.sh` writes every page as `*`, i.e. open. **Page-level
authorisation is disabled.** D012 explains why an invented mapping was judged
worse than an open one: it would look like authorisation while enforcing rules
nobody chose, and would be indistinguishable from the real thing in a screenshot.

## Done when

The real mapping is obtained from outside this repository — the withheld
`admin-other`, the system documentation, or the operator manuals (NF-011) — and
the generator is replaced by it.

Until then, nothing this system shows about who may see what is evidence of
anything.
