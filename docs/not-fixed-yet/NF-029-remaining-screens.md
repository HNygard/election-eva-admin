# NF-029 — 41 of 71 screens still do not render

Status: OPEN — the standing work item for the demo
Milestone: M3

## Where things stand

    tools/smoke-pages.sh, municipality role:  30 of 71 pages return 200

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

- **A null deep in a controller's `init()`**, where the screen expects rows the seed does not have — voters, ballots, counts, list proposals.
- **`userDataController.currentElectionEventDisabled` NPE** from `templates/layout.xhtml`, which affects several screens at once and is therefore the highest-value one to chase next. `isCurrentElectionEventDisabled` guards `getElectionEvent() != null` and then reads `getElectionEventStatus().getId()`; the association is EAGER and the seeded row exists, so the null is more likely `userData.getElectionEventPk()` being unset at that point than a missing row.
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

## The 23 redirects

  302 /secure/delete/deleteVotings.xhtml
  302 /secure/opptelling/behandleManueltForkastede.xhtml
  302 /secure/opptelling/behandleSkannetForkastede.xhtml
  302 /secure/opptelling/genererStrekkodelapper.xhtml
  302 /secure/selectRole.xhtml
  302 /secure/settlement/candidateAnnouncement.xhtml
  302 /secure/settlement/correctionsReport.xhtml
  302 /secure/settlement/levelingSeats.xhtml
  302 /secure/settlement/settlementResult.xhtml
  302 /secure/settlement/settlementStatus.xhtml
  302 /secure/settlement/settlementSummary.xhtml
  302 /secure/stemmegivning/emptyElectionCard.xhtml
  302 /secure/stemmegivning/faVotingsSentFromMunicipality.xhtml
  302 /secure/stemmegivning/forhandOrdinaer.xhtml
  302 /secure/stemmegivning/forhandProvingSamlet.xhtml
  302 /secure/stemmegivning/forhandProvingVelger.xhtml
  302 /secure/stemmegivning/rejectedVotingsReport.xhtml
  302 /secure/stemmegivning/valgtingOrdinaer.xhtml
  302 /secure/stemmegivning/valgtingProvingSamlet.xhtml
  302 /secure/stemmegivning/valgtingProvingVelger.xhtml
  302 /secure/translation/translationsEdit.xhtml
  302 /secure/voting/confirming/votingConfirmation.xhtml
  302 /secure/voting/registerVotingInEnvelope.xhtml
- /secure/delete/deleteVotings.xhtml
- /secure/opptelling/behandleManueltForkastede.xhtml
- /secure/opptelling/behandleSkannetForkastede.xhtml
- /secure/opptelling/genererStrekkodelapper.xhtml
- /secure/selectRole.xhtml
- /secure/settlement/candidateAnnouncement.xhtml
- /secure/settlement/correctionsReport.xhtml
- /secure/settlement/levelingSeats.xhtml
- /secure/settlement/settlementResult.xhtml
- /secure/settlement/settlementStatus.xhtml
- /secure/settlement/settlementSummary.xhtml
- /secure/stemmegivning/emptyElectionCard.xhtml
- /secure/stemmegivning/faVotingsSentFromMunicipality.xhtml
- /secure/stemmegivning/forhandOrdinaer.xhtml
- /secure/stemmegivning/forhandProvingSamlet.xhtml
- /secure/stemmegivning/forhandProvingVelger.xhtml
- /secure/stemmegivning/rejectedVotingsReport.xhtml
- /secure/stemmegivning/valgtingOrdinaer.xhtml
- /secure/stemmegivning/valgtingProvingSamlet.xhtml
- /secure/stemmegivning/valgtingProvingVelger.xhtml
- /secure/translation/translationsEdit.xhtml
- /secure/voting/confirming/votingConfirmation.xhtml
- /secure/voting/registerVotingInEnvelope.xhtml

## The 18 errors

  500 /secure/config/generateEML.xhtml
  500 /secure/config/listAreas.xhtml
  500 /secure/config/listElections.xhtml
  500 /secure/config/local/local.xhtml
  500 /secure/counting/antallStemmesedlerLagtTilSide.xhtml
  500 /secure/counting/countingOverview.xhtml
  500 /secure/delete/deleteVotersBatches.xhtml
  500 /secure/election/electionEvent.xhtml
  500 /secure/election/listElectionEvents.xhtml
  500 /secure/manntall/generateVoterNumbers.xhtml
  500 /secure/manntall/genererValgkortgrunnlag.xhtml
  500 /secure/manntall/importElectoralRoll.xhtml
  500 /secure/manntall/listVoterAudit.xhtml
  500 /secure/manntall/opprett.xhtml
  500 /secure/manntall/sok.xhtml
  500 /secure/opptelling/slettOpptellinger.xhtml
  500 /secure/rbac/accessOverview.xhtml
  500 /secure/reportingUnit/reportingUnitType.xhtml
- /secure/config/generateEML.xhtml
- /secure/config/listAreas.xhtml
- /secure/config/listElections.xhtml
- /secure/config/local/local.xhtml
- /secure/counting/antallStemmesedlerLagtTilSide.xhtml
- /secure/counting/countingOverview.xhtml
- /secure/delete/deleteVotersBatches.xhtml
- /secure/election/electionEvent.xhtml
- /secure/election/listElectionEvents.xhtml
- /secure/manntall/generateVoterNumbers.xhtml
- /secure/manntall/genererValgkortgrunnlag.xhtml
- /secure/manntall/importElectoralRoll.xhtml
- /secure/manntall/listVoterAudit.xhtml
- /secure/manntall/opprett.xhtml
- /secure/manntall/sok.xhtml
- /secure/opptelling/slettOpptellinger.xhtml
- /secure/rbac/accessOverview.xhtml
- /secure/reportingUnit/reportingUnitType.xhtml
