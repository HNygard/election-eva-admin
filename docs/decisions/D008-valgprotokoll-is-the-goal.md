# D008 — Producing a valgprotokoll is the goal that defines "working"

Date: 2026-08-10

## Decision

The target for M3 is a valgprotokoll produced end to end by the running system.
Work that does not eventually serve that is deferred.

## Why

A valgprotokoll exercises nearly the whole system at once: configuration of an
election event, electoral roll, votings, counting, settlement, and the reporting
stack that renders the result. Something that can produce one is genuinely
running, not merely deployed.

It is also the artefact that matters outside this repo. Valgprotokoller are what
municipalities publish, and being able to produce one from the released source is
what makes the source inspectable in a meaningful sense.

## What this runs into

The reporting stack is missing from the release, and this is the largest known
obstacle in the project:

- `admin/admin-report-templates/` contains 8 files, all Groovy/Java uploader code. Zero `.jrxml` templates ship.
- Its own `src/test/resources/ReportConfig.xml` references **32** Jasper templates by path (`Rapport 1/report_1/report_1.jrxml` and so on), none of which exist. The first is even marked `replaceWithBlank="true"`.
- The relevant category is `060.protocols` in that config, `ReportCategory.MØTEBØKER` in `admin/admin-common/src/main/java/no/valg/eva/admin/common/rapport/model/ReportCategory.java`.
- The system documentation states reports are rendered by a JasperSoft report server paired 1:1 with each backend node — a component that is not part of the release at all.

So M3 will require either reconstructing a report template or bypassing Jasper to
extract the same data. Tracked as NF-012.

## Alternatives rejected

- **Stop at a logged-in page.** Reachable much sooner, but proves little about whether the system functions.
- **Treat any report as sufficient.** The other 31 templates are equally missing, and none of them is the artefact that matters.
