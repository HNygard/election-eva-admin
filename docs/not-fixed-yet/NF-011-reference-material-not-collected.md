# NF-011 — Reference material is not in the repo yet

Status: OPEN
Milestone: M3

## Symptom

`docs/reference/` is empty. M3 is meant to be judged against screenshots from
archived valgmedarbeiderportalen copies and the official instruction manuals
(D007), and none of that material is here.

## What is known

The repo owner has archived copies of valgmedarbeiderportalen. They are not under
`~/git` — a search of the home directory to depth 4 found nothing matching
`*valgmedarbeider*`.

`added-stuff/README.md` quotes `eva-admin-2019-systemdokumentasjon.pdf` twice, for
the statements that EVA Admin runs on WildFly and that reports are rendered by a
JasperSoft server paired 1:1 with each backend node. That PDF is not local either.

The published source and documentation come from:
https://www.valg.no/valg-i-norge/valggjennomforing-i-norge/elektronisk-valgadministrasjonssystem/systemdokumentasjon-og-kildekode-i-eva

## How to approach

Ask the repo owner where the archived portal copies live, then place them under
`docs/reference/valgmedarbeiderportalen/` and the manuals under
`docs/reference/manuals/`. Both are gitignored; record each file in
`docs/reference/INDEX.md` with its origin, date and sha256 so a missing file is
detectable.

Then build the screen index: which screenshot shows which page, keyed by
something a running system can be compared against.

## Done when

`docs/reference/INDEX.md` lists the collected material with checksums, and names
at least the screens M3 will be judged on.
