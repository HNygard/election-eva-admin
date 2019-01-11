package no.valg.eva.admin.frontend.settlement.ctrls;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.settlement.model.AffiliationVoteCount;
import no.valg.eva.admin.common.settlement.model.CandidateSeat;

@Named
@ViewScoped
public class SettlementResultController extends BaseSettlementController {

	private List<CandidateSeat> candidateSeats;
	private List<AffiliationVoteCount> affiliationVoteCounts;
	private List<Integer> mandates;

	@Override
	protected void initView() {
		if (!isSettlementDone()) {
			reset();
			MessageUtil.buildDetailMessage(messageProvider.get("@settlement.error.no_settlement"), FacesMessage.SEVERITY_ERROR);
			return;
		}
		ElectionPath contestPath = getContestInfo().getElectionPath();
		candidateSeats = settlementService.findAffiliationCandidateSeatsBySettlement(userData, contestPath);
		affiliationVoteCounts = settlementService.findAffiliationVoteCountsBySettlement(userData, contestPath);
		mandates = settlementService.findMandatesBySettlement(userData, contestPath);
	}

	public List<AffiliationVoteCount> getAffiliationVoteCounts() {
		return affiliationVoteCounts;
	}

	public List<CandidateSeat> getCandidateSeats() {
		return candidateSeats;
	}

	public List<Integer> getMandates() {
		return mandates;
	}

	public boolean isWritein() {
		return getMvElection().getElection().isWritein();
	}

	@Override
	protected String getView() {
		return VIEW_SETTLEMENT_RESULT;
	}
}
