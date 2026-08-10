# NF-013 — Work out how far the 2019 release is from today's EVA Admin

Status: OPEN
Milestone: M4

## Symptom

The archived valgmedarbeiderportalen copies describe the system as it was. This
repo is the 2019 release. Neither tells us what EVA Admin does now, so any
difference found in M3 is ambiguous: our reconstruction may be wrong, or the
system may simply have moved on.

## What is known

This release identifies as `admin-2019.22.7829`, downloaded 2019-06-08. Norway has
run several elections since — 2019 local, 2021 parliamentary, 2023 local, 2025
parliamentary — and Valgdirektoratet has continued publishing source and system
documentation at the same place:

https://www.valg.no/valg-i-norge/valggjennomforing-i-norge/elektronisk-valgadministrasjonssystem/systemdokumentasjon-og-kildekode-i-eva

Nothing has been checked yet. This item is the checking.

## How to approach

This is the first task of M4 and it is investigation, not code:

1. Establish what the current published release is, and whether its source is available.
2. Compare the module structure and the documented architecture against this repo — including whether `admin-docker`, `admin-testing` and `admin-other` are still withheld.
3. Note the functional differences the documentation describes, especially anything touching counting, settlement or protokoll generation, since those define correctness.
4. Record each difference with a citation, and mark clearly which ones cannot be verified from published source.

Do not start updating anything. The output is a written comparison; what to do
about it is a separate decision.

## Done when

A finding sets out the differences with sources, and separates "changed" from
"cannot tell from what is published".
