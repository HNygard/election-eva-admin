package no.valg.eva.admin.frontend.settlement.ctrls;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.evote.dto.CandidateVoteCountDto;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.settlement.model.AffiliationVoteCount;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Election;

import org.primefaces.event.SelectEvent;

@Named
@ViewScoped
public class CandidateAnnouncementController extends BaseSettlementController {

	private List<Integer> mandates;
	private List<Affiliation> affiliations;
	private Map<Long, List<CandidateVoteCountDto>> candidateVoteCountMap;
	private List<CandidateVoteCountDto> candidateVoteCounts;
	private Affiliation selectedAffiliation;

	@Override
	protected void initView() {
		if (!isSettlementDone()) {
			reset();
			MessageUtil.buildDetailMessage(messageProvider.get("@settlement.error.no_settlement"), FacesMessage.SEVERITY_ERROR);
			return;
		}

		ElectionPath contestPath = getContestInfo().getElectionPath();
		List<AffiliationVoteCount> affiliationVoteCounts = settlementService.findAffiliationVoteCountsBySettlement(userData, contestPath);
		mandates = settlementService.findMandatesBySettlement(userData, contestPath);
		affiliations = new ArrayList<>();
		for (AffiliationVoteCount affiliationVoteCount : affiliationVoteCounts) {
			affiliations.add(affiliationVoteCount.getAffiliation());
		}
		candidateVoteCountMap = settlementService.findCandidateVoteCountsBySettlement(userData, contestPath);
		if (!affiliations.isEmpty()) {
			selectedAffiliation = affiliations.get(0);
			candidateVoteCounts = candidateVoteCountMap.get(selectedAffiliation.getPk());
		}
	}

	@Override
	protected String getView() {
		return VIEW_CANDIDATE_ANNOUNCEMENT;
	}

	public void onRowSelect(SelectEvent event) {
		selectedAffiliation = (Affiliation) event.getObject();
		candidateVoteCounts = candidateVoteCountMap.get(selectedAffiliation.getPk());
	}

	public boolean isBaselineConfigured() {
		return getMvElection().getElection().getBaselineVoteFactor() != null && getMvElection().getElection().getBaselineVoteFactor().equals(BigDecimal.ZERO);
	}

	public Election getElection() {
		return getMvElection().getElection();
	}

	public boolean hasCandidateRanking() {
		Election election = getElection();
		return election.isRenumber() || election.isPersonal() || election.isStrikeout() || election.isWritein();
	}

	public List<Affiliation> getAffiliations() {
		return affiliations;
	}

	public List<Integer> getMandates() {
		return mandates;
	}

	public List<CandidateVoteCountDto> getCandidateVoteCounts() {
		return candidateVoteCounts;
	}

	public Affiliation getSelectedAffiliation() {
		return selectedAffiliation;
	}

	public void setSelectedAffiliation(Affiliation selectedAffiliation) {
		this.selectedAffiliation = selectedAffiliation;
	}
}
