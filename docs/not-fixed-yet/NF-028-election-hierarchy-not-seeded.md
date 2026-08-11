# NF-028 — No election hierarchy, so context-dependent screens bounce

Status: OPEN — this is the next step for the demo
Milestone: M3

## Symptom

Most screens redirect rather than render:

    GET /secure/counting/countingOverview.xhtml
      302 -> /secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|2,3][side|uri|42]
    GET /secure/manntall/sok.xhtml
      302 -> /secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|3][side|uri|89]

`tools/smoke-pages.sh` measures it:

    role at election event root   19 of 68 pages return 200
    role at municipality          23 of 68 pages return 200

## This is not a defect

`kontekstvelger` is EVA Admin's context chooser. Those screens need a geography
and election context at a particular level, and the redirect is the application
correctly asking for one. Adding a municipality-level `operator_role` raised the
count precisely because it supplies part of that context directly.

## What is actually missing

The seed has an area hierarchy (country, county, municipality) but the election
side stops at the event:

    mv_election: one row, election_level 0, election_path '000000'

There are no `election_group`, `election` or `contest` rows, and therefore no
`mv_election` rows at group, election or contest level. Screens for counting,
settlement and list proposals all need those.

## How to approach

Extend `docker/postgres/seed/01-base.sql` with an election hierarchy:

1. `election_group` for the event, then an `election` (`election_type` 'F' already exists), then a `contest` covering the seeded municipality.
2. `mv_election` rows at each level, with `election_path` following `EEVENT.EG.E.C`.
3. `contest_area` linking the contest to the area, which is what `ContestRelAreaRepository`'s ltree queries walk.

Then re-run `tools/smoke-pages.sh` and see which screens move to 200. Do not
assume the number rises uniformly — some screens will need voters, ballots or
counts before they render anything meaningful.

## Done when

The smoke figure is materially higher, and every remaining redirect is either
explained as correct context-chooser behaviour or has its own item.
