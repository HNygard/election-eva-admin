# NF-012 — All 32 Jasper report templates are missing, which blocks valgprotokoll

Status: OPEN
Milestone: M3

## Symptom

`admin/admin-report-templates/` contains 8 files — Groovy uploaders, one Java
producer, one test, one pom — and not a single `.jrxml` or `.jasper`:

    find . -iname '*.jrxml' -o -iname '*.jasper'   →   (nothing)

Producing a valgprotokoll is the goal that defines "working" (D008), and the
templates that render it are not published.

## What is known

`admin/admin-report-templates/src/test/resources/ReportConfig.xml` declares 32
templates by path, none of which exist, for example:

    <template path="Rapport 1/report_1/report_1.jrxml" replaceWithBlank="true">
    <template path="Rapport 2/report_2/report_2.jrxml"/>
    <template path="Rapport 46_county/report_46/report_46.jrxml"/>

The same file defines the report categories, including
`060.protocols` / `@reporting.report.category.protocols`. The application-side
enum is `ReportCategory.MØTEBØKER` in
`admin/admin-common/src/main/java/no/valg/eva/admin/common/rapport/model/ReportCategory.java`.

Rendering does not happen in this codebase at all. Per the system documentation
quoted in `added-stuff/README.md`, reports are produced by a JasperSoft report
server paired 1:1 with each backend node; the Groovy classes here only upload
templates and jobs to it. So even with templates, a JasperSoft server would be
needed.

## How to approach

Two routes, and the choice matters:

1. **Reconstruct a template.** Work out which report ID is the valgprotokoll (start from the `060.protocols` category and the `@rapport.meta.*` keys), find its data model in `admin-rapport`, and write a `.jrxml`. Needs a JasperSoft server or an embedded JasperReports runner.
2. **Bypass Jasper.** Drive the same queries and application services the report would use, and emit the protokoll data directly. Proves the system computes a correct valgprotokoll without reproducing a proprietary report server.

Route 2 is likely the honest one: it tests the election logic, which is the part
that matters, instead of reproducing a rendering pipeline that was never
published. Decide explicitly and write it up as a decision before starting.

`~/git/valgprotokoller` contains parsers for published valgprotokoll documents and
is a good source for what the output should actually contain.

## Done when

Either a valgprotokoll is rendered, or the same data is produced by a documented
alternative path — and the choice is recorded as a decision.
