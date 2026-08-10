# Status

**Current milestone: M1 — reproducible build**

Nothing has been built successfully yet in this repo under the current setup. The
repo is byte-identical to the EVA Admin 2019 release except for `readme.md`,
`added-stuff/`, and the scaffolding added on 2026-08-10.

Pick work from [docs/not-fixed-yet/INDEX.md](not-fixed-yet/INDEX.md): the topmost
`OPEN` item tagged with the current milestone.

## Milestones

### M1 — reproducible build

`tools/build.sh` produces every JAR and WAR from a clean container and an empty
Maven volume.

Exit criteria:
- `tools/build.sh` exits 0 twice in a row starting from an empty `eva-admin-maven-repo` volume.
- Every artifact the build should produce is listed in a finding, with the exact `find`/`ls` output that shows it.
- Tests may still be skipped, but the reason is recorded and tracked as an open item, not treated as normal.

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
