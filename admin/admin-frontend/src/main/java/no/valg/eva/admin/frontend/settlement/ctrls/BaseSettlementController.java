package no.valg.eva.admin.frontend.settlement.ctrls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.ContestAreaService;
import no.evote.service.configuration.MvElectionService;
import no.evote.service.counting.ContestReportService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.common.settlement.service.SettlementService;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.contest.SelectContestProvider;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

public abstract class BaseSettlementController extends BaseController implements SelectContestProvider {

	public static final String VIEW_SETTLEMENT_RESULT = "settlementResult.xhtml";
	public static final String VIEW_SETTLEMENT_STATUS = "settlementStatus.xhtml";
	public static final String VIEW_SETTLEMENT_SUMMARY = "settlementSummary.xhtml";
	public static final String VIEW_CANDIDATE_ANNOUNCEMENT = "candidateAnnouncement.xhtml";

	@Inject
	protected MessageProvider messageProvider;
	@Inject
	protected UserData userData;
	@Inject
	protected ContestInfoService contestInfoService;
	@Inject
	protected ContestAreaService contestAreaService;
	@Inject
	protected SettlementService settlementService;
	@Inject
	protected PageTitleMetaBuilder pageTitleMetaBuilder;
	@Inject
	protected ContestReportService contestReportService;
	protected ContestInfo contestInfo;
	protected MvArea contestArea;
	@Inject
	private MvElectionService mvElectionService;
	private List<String> pageTitleViews = Arrays.asList(VIEW_CANDIDATE_ANNOUNCEMENT, VIEW_SETTLEMENT_RESULT, VIEW_SETTLEMENT_STATUS, VIEW_SETTLEMENT_SUMMARY);
	private List<ContestInfo> contestList = new ArrayList<>();
	private MvElection mvElection;
	private boolean settlementDone;
	private boolean hasContestReport = false;

	protected abstract void initView();

	protected abstract String getView();

	@PostConstruct
	public void init() {
		String contestPathStr = getRequestParameter("contestPath");
		AreaPath operatorAreaPath = userData.getOperatorAreaPath();
		setContestList(
				contestInfoService.contestsByAreaAndElectionPath(userData, operatorAreaPath, userData.getOperatorElectionPath(), operatorAreaPath.getLevel()));
		ElectionPath contestPath = null;
		if (contestPathStr == null) {
			if (getContestList().size() == 1) {
				contestPath = getContestList().get(0).getElectionPath();

			}
		} else {
			ElectionPath contestPathFromRequest = ElectionPath.from(contestPathStr);
			if (validate(contestPathFromRequest)) {
				contestPath = contestPathFromRequest;
			} else {
				return;
			}
		}
		if (contestPath != null) {
			setContestPath(contestPath.path());
			selectContest();
		}
	}

	public String backToSelectContest() {
		return addRedirect(getView());
	}

	public boolean isSettlementDone() {
		return settlementDone;
	}

	public void setSettlementDone(boolean settlementDone) {
		this.settlementDone = settlementDone;
	}

	@Override
	public String getSelectContestHeader() {
		return messageProvider.get("@common.choose") + " " + messageProvider.get("@election_level[3].name");
	}

	@Override
	public String getContestPath() {
		return contestInfo == null ? null : contestInfo.getElectionPath().path();
	}

	@Override
	public void setContestPath(String contestPath) {
		contestInfo = null;
		for (ContestInfo ci : getContestList()) {
			if (ci.getElectionPath().path().equals(contestPath)) {
				contestInfo = ci;
				return;
			}
		}
	}

	@Override
	public List<ContestInfo> getContestList() {
		return contestList;
	}

	protected void setContestList(List<ContestInfo> contestList) {
		this.contestList = contestList;
	}

	@Override
	public String selectContest() {
		List<ContestArea> areas = contestAreaService.findContestAreasForContestPath(userData, contestInfo.getElectionPath());
		contestArea = areas.get(0).getMvArea();
		mvElection = mvElectionService.findSingleByPath(contestInfo.getElectionPath());
		setSettlementDone(settlementService.hasSettlementForContest(userData, contestInfo.getElectionPath()));

		if (initSettlement()) {
			initView();
		}
		return null;
	}

	public List<PageTitleMetaModel> getPageTitleMeta() {
		List<PageTitleMetaModel> result = pageTitleMetaBuilder.settlementTitle(mvElection, contestArea);
		if (!result.isEmpty()) {
			if (pageTitleViews.contains(getView())) {
				if (hasContestReport && !VIEW_SETTLEMENT_STATUS.equals(getView()) && !isSettlementDone()) {
					result.add(new PageTitleMetaModel(messageProvider.get("@menu.settlement.status"), getContestURL(VIEW_SETTLEMENT_STATUS), true));
				}
				if (hasContestReport && !VIEW_SETTLEMENT_SUMMARY.equals(getView())) {
					result.add(new PageTitleMetaModel(messageProvider.get("@menu.settlement.result"), getContestURL(VIEW_SETTLEMENT_SUMMARY), true));
				}
				if (isSettlementDone() && !VIEW_SETTLEMENT_RESULT.equals(getView())) {
					result.add(
							new PageTitleMetaModel(messageProvider.get("@menu.settlement.mandate_distribution"), getContestURL(VIEW_SETTLEMENT_RESULT), true));
				}
				if (isSettlementDone() && !VIEW_CANDIDATE_ANNOUNCEMENT.equals(getView())) {
					result.add(new PageTitleMetaModel(messageProvider.get("@menu.settlement.candidate_announcement"),
							getContestURL(VIEW_CANDIDATE_ANNOUNCEMENT), true));
				}
			}
		}
		return result;
	}

	private String getContestURL(String url) {
		return addRedirect(url + "?contestPath=" + getContestPath());
	}

	protected ContestInfo getContestInfo() {
		return contestInfo;
	}

	protected MvElection getMvElection() {
		return mvElection;
	}

	private boolean initSettlement() {
		if (!hasContestReport()) {
			reset();
			MessageUtil.buildDetailMessage(messageProvider.get("@settlement.error.no_counts"), FacesMessage.SEVERITY_ERROR);
			hasContestReport = false;
		} else {
			hasContestReport = true;
		}
		return hasContestReport;
	}

	private boolean hasContestReport() {
		return contestReportService.hasContestReport(userData, contestInfo.getElectionPath(), userData.getOperatorAreaPath());
	}

	private boolean validate(ElectionPath contestPath) {
		contestPath.assertContestLevel();
		for (ContestInfo ci : getContestList()) {
			if (ci.getElectionPath().equals(contestPath)) {
				return true;
			}
		}
		MessageUtil.buildDetailMessage(messageProvider.get("@settlement.error.wrong_level"), FacesMessage.SEVERITY_ERROR);
		return false;
	}

	protected void reset() {
		contestInfo = null;
		contestArea = null;
		setContestList(new ArrayList<ContestInfo>());
		mvElection = null;
		setSettlementDone(false);
	}
}
