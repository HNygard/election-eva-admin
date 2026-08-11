# NF-026 — The demo role holds every access

Status: OPEN — deliberate, and wrong
Milestone: M3

## What was done

`tools/generate-seed.sh` emits one `access` row per standard constant of the
`Accesses` enum (189 of them; 20 are aggregated constants evaluated in code and
correctly have no row), and grants **all** of them to the single seeded role.

## Why it is wrong

Which accesses a real `Valghendelsesadministrator` holds is not published. The
grant is not a reconstruction of any real role: it is the shortest path to a
menu with something in it.

Combined with NF-023, which leaves page-level authorisation fully open, the
running system enforces essentially no authorisation. Two independent guesses
point the same way, and neither is visible on screen:

- every access granted to the only role
- every page open to any authenticated operator

## What this means when demoing

The menu is complete because the role is omnipotent, not because EVA Admin shows
everything to everyone. A demo of "what an election administrator sees" is not
what this shows.

## Done when

Real role definitions arrive from outside this repository — the withheld
`admin-other`, the system documentation, or the operator manuals (NF-011) — and
the grant is replaced by them. Until then any statement about who can do what in
EVA Admin cannot be based on this system.
