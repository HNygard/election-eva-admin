# NF-029 — 41 of 71 screens still do not render

Status: OPEN — the standing work item for the demo
Milestone: M3

## Where things stand

    tools/smoke-pages.sh, municipality role:  45 of 69 pages return 200

Progress so far, each step a separate commit:

    19  menu reconstructed (widget:menu was a stub)
    23  municipality-level role added
    29  election hierarchy seeded
    30  ltree extension installed into the public schema
    41  ehcache-local.xml added (NF-030) -- one file, eleven screens
    42  message keys that are format patterns given real values
    41  deep geography added: one screen that had been rendering only because
        the data was absent now resolves a context and fails further along
    45  frontend log4j.xml, and mv_area's foreign keys populated

That last line is worth keeping. A rising count is not by itself progress: a
screen can render because a controller found nothing to do.

The other 41 split into two quite different problems.

## 23 screens: redirect to the context chooser (HTTP 302)

These are **not broken**. `kontekstvelger.xhtml` is EVA Admin's context chooser,
and the redirect is the application asking for a geography or election context it
does not have:

    GET /secure/counting/countingOverview.xhtml
      302 -> /secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|2,3][side|uri|42]

Seeding the election hierarchy moved several screens out of this group; the rest
want a context at a level the seed does not reach, most often a polling district
or polling place, or a second municipality to choose between.

The honest way to clear these is not more redirect-dodging but more geography:
boroughs, polling districts, polling places and stations under the seeded
municipality, and a second municipality so the chooser has a choice to offer.

## 18 screens: server errors (HTTP 500)

Each is its own missing-data or missing-behaviour question. The recurring shapes
seen so far:

- **`KontekstAvhengigController.init()` and `MvAreaController.init()` NPEs.** These are the bulk of what remains. Both are context-dependent controllers, and they now have geography to work with, so the missing piece is further along: most likely the election-to-area wiring (`contest_area` covers the municipality only) or rows these screens read directly — voters, ballots, counts, list proposals.
- **Placeholder composite components**, where a screen's behaviour lived in markup that was withheld (NF-024). No amount of data fixes these; they need the component reconstructed the way `widget:menu` was.

## How to work this

`tools/smoke-pages.sh` is the measure. Take one screen, get its root cause from
the server log, fix the cause rather than the symptom, re-run the script and
watch the number. That is the loop that took this from 19 to 30.

Resist the temptation to make a screen return 200 by giving a controller
something arbitrary to render. A screen that renders the wrong thing is worse
than one that fails, because it is demoable and wrong.

## Done when

Either every screen renders, or every screen that does not has a recorded reason
that is either "needs data of kind X" or "needs component Y reconstructed".

## What remains: 12 redirects, 5 errors

The redirects are the context chooser doing its job and are not listed
individually. The five errors each have a named cause now, which they did not
before `admin-frontend` got a log4j configuration:

**`/secure/counting/countingOverview.xhtml`** and
**`/secure/reporting/statistics/evaResultatRapportering.xhtml`**

    IllegalArgumentException: Could not locate appropriate constructor on class :
    no.valg.eva.admin.configuration.domain.model.valgnatt.ReportConfiguration

Not missing data. `MvArea.reportConfigurationQuery` is a native query whose
result is mapped through a constructor taking `Integer pollingDistrictPk` and
`Integer mvAreaPk`, while the generated schema types those columns `int8`, so
JDBC hands back `Long`. This is a **schema generation artefact**: JPA maps the
entity pk as `Long`, so `bigserial` is what the generator emits, and the real
database evidently used a narrower type for these columns. Fixing it means
either narrowing those columns in the generated schema or casting in the query,
and the first is closer to what the original must have been. Worth checking
whether other native queries have the same latent mismatch.

**`/secure/settlement/levelingSeats.xhtml`**

    EvoteException: @leveling_seats.error.missing_election

This is arguably correct behaviour. The seeded election has `leveling_seats = 0`,
and the screen exists to show leveling seat settlement, so it refuses. Giving the
election leveling seats would make the screen render, but it would then be
showing a leveling seat calculation for an election configuration nobody chose.
Decide deliberately before changing the seed.

**`/secure/settlement/candidateAnnouncement.xhtml`**

    javax.persistence.NoResultException: No entity found for query

Needs settlement data: candidates, ballots and a completed settlement. This is
genuine election data, not configuration.

**`/secure/config/local/local.xhtml`**

    NullPointerException, surfacing through ForceLocaleFilter

Still needs its own investigation; the visible frame is the filter, not the
cause.

