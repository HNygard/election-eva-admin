package no.valg.eva.admin.frontend.counting.ctrls;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.settlement.ctrls.SettlementSummaryController;

/**
 * This class is used to display vote counts and results for the county election done by the municipality. It is for viewing purposes only. The roles who get
 * access to this functionality must be at least on the municipality level. As of now only two roles have access: counting_resp_advance and counting_resp_el_com
 */
@Named
@ViewScoped
public class CountyVoteCountsController extends SettlementSummaryController {

	@Inject
	private MvElectionService mvElectionService;

	@Override
	@PostConstruct
	public void init() {
		if (!isUserOnMunicipalityLevel()) {
			MessageUtil.buildDetailMessage(messageProvider.get("@settlement.error.wrong_level"), FacesMessage.SEVERITY_ERROR);
			return;
		}

		List<ContestInfo> contestInfoList = contestInfoService.contestsByAreaAndElectionPath(userData,
				userData.getOperatorAreaPath(), userData.getOperatorElectionPath(), null);
		List<ContestInfo> finalContestInfoList = new ArrayList<>();
		for (ContestInfo contest : contestInfoList) {
			MvElection mvElection = mvElectionService.findSingleByPath(contest.getElectionPath());
			// Only interested in contest for county, or in the case of sami election contest on municipality level with not single area
			if (!mvElection.getElection().isReferendum() && (contest.getAreaPath().isCountyLevel() || (contest.getAreaPath().isMunicipalityLevel()
					&& !mvElection.getElection().isSingleArea()))) {
				finalContestInfoList.add(contest);
			}
		}

		/*Has only access to one contest that is on the same area level as the operator role*/
		if (finalContestInfoList.isEmpty() || finalContestInfoList.size() > 1) {
			MessageUtil.buildDetailMessage(messageProvider.get("@settlement.error.wrong_level"), FacesMessage.SEVERITY_ERROR);
			return;
		}

		setContestList(finalContestInfoList);
		setContestPath(finalContestInfoList.get(0).getElectionPath().path());
		selectContest();
	}

	private boolean isUserOnMunicipalityLevel() {
		return userData.getOperatorAreaPath().isMunicipalityLevel();
	}

	public List<PageTitleMetaModel> getPageTitleMeta() {
		return pageTitleMetaBuilder.settlementTitle(getMvElection(), contestArea);
	}
}
