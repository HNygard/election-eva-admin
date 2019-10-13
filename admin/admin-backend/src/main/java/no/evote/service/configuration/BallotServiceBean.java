package no.evote.service.configuration;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.BallotRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Default
@ApplicationScoped
public class BallotServiceBean {
	@Inject
	private AffiliationRepository affiliationRepository;
	@Inject
	private BallotRepository ballotRepository;

	public void updateBallotStatus(UserData userData, Affiliation affiliation, BallotStatus ballotStatus) {
		Ballot ballot = affiliation.getBallot();
		Long contestPk = ballot.getContest().getPk();

		int oldBallotStatusId = ballot.getBallotStatus().getId();
		ballot.setBallotStatus(ballotStatus);
		if (ballotStatus.getId() != BallotStatus.BallotStatusValue.APPROVED.getId()) {
			ballot.setDisplayOrder(null);
			ballot.setApproved(false);
		}
		if (ballotStatus.getId() == BallotStatus.BallotStatusValue.APPROVED.getId()) {
			ballot.setApproved(true);
		}

		ballotRepository.updateBallot(userData, ballot);

		if (ballotStatus.getId() == BallotStatus.BallotStatusValue.APPROVED.getId()) {
			approveAffiliation(userData, affiliation, contestPk);
		} else if (ballotStatus.getId() != BallotStatus.BallotStatusValue.APPROVED.getId()
				&& oldBallotStatusId == BallotStatus.BallotStatusValue.APPROVED.getId()) {
			unApproveAffiliation(userData, affiliation, contestPk);
		}
	}

	public void updateBallotStatusWithdrawn(UserData userData, Ballot ballot) {
		BallotStatus ballotStatus = ballotRepository.findBallotStatusById(BallotStatus.BallotStatusValue.WITHDRAWN.getId());
		ballot.setBallotStatus(ballotStatus);
		ballotRepository.updateBallot(userData, ballot);
	}

	public void updateBallotStatusPending(UserData userData, Ballot ballot) {
		BallotStatus ballotStatus = ballotRepository.findBallotStatusById(BallotStatus.BallotStatusValue.PENDING.getId());
		ballot.setBallotStatus(ballotStatus);
		ballotRepository.updateBallot(userData, ballot);
	}

	private void approveAffiliation(UserData userData, Affiliation affiliation, Long contestPk) {
		affiliation.setApproved(true);
		affiliationRepository.updateAffiliation(userData, affiliation);
		reOrderDisplayOrder(userData, contestPk);
	}

	private void unApproveAffiliation(UserData userData, Affiliation affiliation, Long contestPk) {
		affiliation.setApproved(false);
		affiliation.setDisplayOrder(null);
		affiliationRepository.updateAffiliation(userData, affiliation);
		reOrderDisplayOrder(userData, contestPk);
	}

	/**
	 * When affiliation/ballot is approved or unapproved, every approved affiliations/ballot need to reorder their display order.
	 */
	private void reOrderDisplayOrder(UserData userData, Long contestPk) {
		List<Affiliation> affiliationList = affiliationRepository.findByBallotStatusAndContest(contestPk, BallotStatus.BallotStatusValue.APPROVED.getId());

		int displayOrder = 1;
		/** BLANK has displayOrder 1 */
		for (Affiliation affiliation : affiliationList) {
			affiliation.setDisplayOrder(displayOrder);
			affiliation.getBallot().setDisplayOrder(displayOrder);

			ballotRepository.updateBallot(userData, affiliation.getBallot());
			affiliationRepository.updateAffiliation(userData, affiliation);

			displayOrder++;
		}
	}
}
