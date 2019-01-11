package no.evote.service.configuration;

import java.util.List;

import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.BallotRepository;

import org.apache.log4j.Logger;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public class AffiliationServiceBean {
	private static final Logger LOGGER = Logger.getLogger(AffiliationServiceBean.class);

	@Inject
	private PartyServiceBean partyServiceBean;
	@Inject
	private BallotRepository ballotRepository;
	@Inject
	private AffiliationRepository affiliationRepository;

	public Affiliation createNewAffiliation(UserData userData, Contest contest, Party party, Locale locale, int ballotStatus) {
		if (party == null) {
			return null;
		}

		Ballot newBallot = ballotRepository.createNewBallot(contest, party.getId(), locale, ballotStatus);

		Affiliation newAffiliation = new Affiliation();
		newAffiliation.setParty(party);
		newAffiliation.setBallot(newBallot);

		ballotRepository.createBallot(userData, newBallot);
		newAffiliation = affiliationRepository.createAffiliation(userData, newAffiliation);

		newBallot.setAffiliation(newAffiliation);
		ballotRepository.updateBallot(userData, newBallot);

		return newAffiliation;
	}

	public Affiliation createNewPartyAndAffiliation(UserData userData, Contest currentContest, Party newParty, Locale locale) {
		partyServiceBean.create(userData, newParty);
		return createNewAffiliation(userData, currentContest, newParty, locale, BallotStatus.BallotStatusValue.PENDING.getId());
	}

	/**
	 * Saves columns(residence/profession) shown on ballot.
	 */
	public Affiliation saveColumns(UserData userData, Affiliation affiliation) {
		Affiliation dbAffiliation = affiliationRepository.findAffiliationByPk(affiliation.getPk());
		dbAffiliation.setShowCandidateProfession(affiliation.isShowCandidateProfession());
		dbAffiliation.setShowCandidateResidence(affiliation.isShowCandidateResidence());
		return affiliationRepository.updateAffiliation(userData, dbAffiliation);
	}
	
	public List<Affiliation> changeDisplayOrder(UserData userData, Affiliation affiliation, int fromPosition, int toPosition) {
		if (fromPosition == toPosition) {
			throw new IllegalArgumentException("changeDisplayOrder displayOrderFrom[" + fromPosition + "] equal to displayOrderTo[" + toPosition + "]");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("changeDisplayOrder[" + affiliation + ", from " + fromPosition + ", to " + toPosition);
		}

		// Get the affected candidates
		boolean fromTopToBottom = fromPosition < toPosition;
		int low = fromTopToBottom ? fromPosition : toPosition;
		int high = fromTopToBottom ? toPosition : fromPosition;
		List<Affiliation> list = affiliationRepository.findAffiliationByContestAndDisplayOrderRange(affiliation.getBallot().getContest().getPk(), low, high);
		if (list.size() != (high - low + 1)) {
			throw new IllegalArgumentException("changeDisplayOrder from/to [" + fromPosition + "/" + toPosition + "] does not match actual size " + list.size());
		}

		// Make the physical change
		if (fromTopToBottom) {
			list.add(list.remove(0));
		} else {
			list.add(0, list.remove(list.size() - 1));
		}

		// Correct all the display order values.
		int currentDisplayOrder = low;
		for (Affiliation a : list) {
			a.setDisplayOrder(currentDisplayOrder++);
		}
		return affiliationRepository.updateAffiliations(list);
	}

}
