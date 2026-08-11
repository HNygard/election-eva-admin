# D012 — Page-level authorisation is left open, not invented

Date: 2026-08-11

## Decision

`PageAccess.properties` is generated with every one of the 207 secure pages
mapped to `*`, meaning open to any authenticated operator. Page-level
authorisation is therefore **disabled**, not reconstructed.

## Why not reconstruct it

The file paired each page with constants of the `Accesses` enum — 207 pages
against 156 constants. Nothing in the published source records the pairing: no
page references an access name, no class holds a default map, and the enum
carries no page hints. The mapping existed only in the withheld file.

So the choice was between an invented mapping and an open one.

An invented mapping is worse, and not marginally. It would *look* like
authorisation while enforcing rules nobody chose. Anyone examining the running
system — including from a screenshot — would see a role being denied a page and
reasonably conclude that is how EVA Admin behaves, when in fact it is how a
guess behaves. For source published so that the election system can be
inspected, manufacturing plausible security rules is the worst available option.

Open is wrong in an obvious, announced way instead of a subtle, convincing one.

## What this means when reading the running system

- Any logged-in operator reaches every page, whatever their role.
- A screen being reachable proves nothing about who should reach it.
- No screenshot of this reconstruction is evidence about EVA Admin's real authorisation model.

The generated file says all of this in its own header, so it is visible to
anyone who opens it rather than only to whoever reads this decision.

## What would change it

The real mapping, from a source outside this repository: the withheld
`admin-other`, the system documentation, or the operator manuals. Until then
NF-023 stays open.

## Relationship to D011

D011 disabled the system passphrase, which is a gate on the deployment. This
disables authorisation *within* the application. Both are recorded the same way
and for the same reason: they are the price of making withheld material
inspectable, and the price has to be stated rather than absorbed.
