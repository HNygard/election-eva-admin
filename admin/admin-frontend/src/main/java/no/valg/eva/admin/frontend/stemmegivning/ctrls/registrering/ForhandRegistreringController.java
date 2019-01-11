package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.voting.domain.model.Voting;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.voting.VotingPhase.ADVANCE;
import static no.valg.eva.admin.frontend.util.MessageUtil.VOTING_NUMBER_ENVELOPE;
import static no.valg.eva.admin.frontend.util.MessageUtil.buildDetailMessage;

/**
 * Superklasse for alle formhånd stemmegivning controllere med felles funksjonalitet.
 */
public abstract class ForhandRegistreringController extends RegistreringController {

	private boolean sentInnkommet;

	/**
	 * Sletter siste avgitte stemme.
	 */
	public void slettForhandsstemme() {
		execute(() -> {
			String navn = getNavn(getStemmegivning().getVoter());
			votingService.delete(getUserData(), getStemmegivning().getPk());
			if (getStemmegivning().getVotingCategory().getId().equals(VotingCategory.FA.getId())) {
				buildDetailMessage("@voting.requestRemoveAdvanceVotingFA.response", new String[] { navn }, SEVERITY_INFO);
			} else {
				buildDetailMessage("@voting.requestRemoveAdvanceVoting.response",
						new String[] { getStemmegivning().getVotingCategory().getId(), getStemmegivning().getVotingNumber() + "", navn },
						SEVERITY_INFO);
			}
			setStemmegivning(null);
		});
	}

	/**
	 * Skal nedtrekksliste for stemmetype (under registrering av stemme) være disbled?
	 */
	public boolean isStemmetypeDisabled() {
		return isVelgerEgenKommune() && (!isKanRegistrereStemmegivning() || isForhandsstemmeRettIUrne());
	}

	/**
	 * Skal link til opprett fiktiv velger (etter tomt søk) vises?
	 */
	public boolean isVisOpprettFiktivVelgerLink() {
		return isIngenVelgerFunnet() && !isForhandsstemmeRettIUrne();
	}

	/**
	 * Skal link til "slett stemmegivning" vises etter et stemmegivning?
	 */
	public boolean isVisSlettForhandsstemmeLink() {
		return getStemmegivning() != null && !isForhandsstemmeRettIUrne();
	}

	/**
	 * Registrer stemmegivning rett i urne.
	 */
	void registrerStemmegivningUrne() {
		execute(() -> {

			setStemmegivning(votingService.markOffVoterAdvanceVoteInBallotBox(
					getUserData(),
					getStemmested().getPollingPlace(),
					getValgGruppe().getElectionGroup(),
					getVelger(),
					isVelgerEgenKommune(), 
					ADVANCE));

			String melding = byggStemmegivningsMelding(getVelger(), getStemmegivning(), "@voting.markOff.voterMarkedOffAdvance");
			if (!isVelgerEgenKommune() && !getVelger().isFictitious()) {
				melding += " " + getMessageProvider().get("@voting.markOff.advanceForeignEnvelope");
			} else {
				melding = byggStemmegivningsMelding(getVelger(), getStemmegivning(), "@voting.markOff.voterMarkedOff");
			}
			buildDetailMessage(melding, SEVERITY_INFO);
		});
		manntallsSokWidget.reset();
		manntallsSokVelger(null);

	}

	/**
	 * Registrer konvoluttstemme.
	 */
    protected void registrerStemmegivningKonvolutt() {
		execute(() -> {

			Voting voting = votingService.markOffVoterAdvance(
					getUserData(),
					getStemmested().getPollingPlace(),
					getValgGruppe().getElectionGroup(),
					getVelger(),
					isVelgerEgenKommune(),
					getStemmetype(),
					null,
					ADVANCE);
			setStemmegivning(voting);

			String melding = byggStemmegivningsMelding(getVelger(), getStemmegivning(), "@voting.markOff.voterMarkedOffAdvance");
			if (!getStemmegivning().getVotingCategory().getId().equalsIgnoreCase(VotingCategory.FA.getId())) {
				melding += " " + getMessageProvider().get(VOTING_NUMBER_ENVELOPE, getStemmegivning().getVotingCategory().getId(),
						getStemmegivning().getVotingNumber());
			} else {
				melding += " " + getMessageProvider().get("@voting.markOff.advanceForeignEnvelope");
			}
			buildDetailMessage(melding, SEVERITY_INFO);
		});

		manntallsSokWidget.reset();
		manntallsSokVelger(null);
	}

	public boolean isSentInnkommet() {
		return sentInnkommet;
	}

	public void setSentInnkommet(boolean sentInnkommet) {
		this.sentInnkommet = sentInnkommet;
	}
}
