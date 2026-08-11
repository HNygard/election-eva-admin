# Status

**Current milestone: M2 — backend deploys**

M1 is done, as of 2026-08-11. The released source builds and its tests pass with
**no modification to any release file** — two Maven flags and one environment
variable were the whole story:

    16/16 modules SUCCESS, 3m16s
    Tests run: 8083, Failures: 0, Errors: 0, Skipped: 0
    35M admin-backend-2019.22-SNAPSHOT.war
    30M admin-frontend-2019.22-SNAPSHOT.war

See `docs/findings/2026-08-10-first-successful-build.md` and
`docs/findings/2026-08-11-full-test-suite-passes.md`.

The manifest still reports **3233 original / 1 modified / N added** — the one
modification is `readme.md`, which predates this work. Nothing in `admin/` has
been touched.

Pick work from [docs/not-fixed-yet/INDEX.md](not-fixed-yet/INDEX.md): the topmost
`OPEN` item tagged with the current milestone.

## Milestones

### M1 — reproducible build — DONE 2026-08-11

`tools/build.sh` produces every JAR and WAR from a clean container and an empty
Maven volume.

- ✅ Two consecutive clean builds, the first from an empty `eva-admin-maven-repo` volume.
- ✅ All 16 modules and both WARs verified on disk, not just in the log.
- ✅ Tests are not merely unskipped but passing: 8083, zero failures.

One caveat carried into M2: only the default Surefire group ran. The
`repository,slow,erasesTestData,resources` groups need a database and have never
been attempted (NF-003).

### M2 — backend deploys

`admin-backend.war` starts in WildFly against PostgreSQL with no `MSC000001`
failure.

Exit criteria:
- Postgres from `docker/postgres/` is populated by a schema generated from this repo's entity classes.
- WildFly reports the deployment as `Deployed`, and the server log excerpt proving it is in a finding.
- Every descriptor that had to be reconstructed (`web.xml`, `beans.xml`, datasource, persistence unit) is declared in `manifest/changes.tsv` with the reasoning.

### M3 — full app

A browser reaches a logged-in EVA Admin page, and the application can produce a
valgprotokoll.

Exit criteria:
- `admin-frontend.war` resolves its `ejb:/admin-backend//…` lookups against the backend.
- Rendered pages are compared against screenshots from the archived valgmedarbeiderportalen copies, with differences recorded rather than explained away.
- A valgprotokoll is produced end to end. This needs Jasper templates that the release does not ship (NF-012), so expect that to dominate the milestone.

### M4 — what has the system become

The archived valgmedarbeiderportalen copies describe the system as it was. The
first task of M4 is to work out how far the 2019 code has drifted from what EVA
Admin does today, before deciding what to update.

Exit criteria:
- A written comparison of the archived portal against the current published EVA documentation and this code.
- A list of concrete differences, each traced to code in this repo or marked as unverifiable from the released source.
