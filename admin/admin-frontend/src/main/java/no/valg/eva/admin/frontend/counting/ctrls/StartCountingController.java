package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.Counts;
import no.valg.eva.admin.common.counting.service.CountingConfigurationService;
import no.valg.eva.admin.common.counting.service.configuration.CountingConfiguration;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.counting.view.Tab;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;

@Named
@ConversationScoped
public class StartCountingController extends BaseCountController {
	static final String COUNTING_PATH = "/secure/counting/";

	@Inject
	private CountingConfigurationService countingConfigurationService;
	@Inject
	private MvElectionService electionService;
	@Inject
	private ProtocolCountController protocolCountController;
	@Inject
	private ProtocolAndPreliminaryCountController protocolAndPreliminaryCountController;
	@Inject
	private PreliminaryCountController preliminaryCountController;
	@Inject
	private FinalCountController finalCountController;
	@Inject
	private CountyFinalCountController countyFinalCountController;
	@Inject
	private CompareCountsController compareCountsController;
	@Inject
	private UserDataController userDataController;
	@Inject
	private MessageProvider messageProvider;

	private Counts counts;
	private List<Tab> tabs = new ArrayList<>();
	private int currentTab;
	private boolean breadCrumbRendered;
	private ElectionPath contestPath;
	private AreaPath areaPath;
	private CountCategory countCategory;
	private CountContext countContext;
	private String countingOverviewURL;
	private ReportingUnitTypeId reportingUnitTypeId;

	@Override
	protected void doInit() {
		try {
			countCategory = getRequestParameter("category", CountCategory::fromId);
			contestPath = getRequestParameter("contestPath", ElectionPath::from);
			areaPath = getRequestParameter("areaPath", AreaPath::from);
			boolean fraMeny = parseBoolean(getRequestParameter("fraMeny"));
			breadCrumbRendered = (areaPath != null || contestPath != null) && !fraMeny;
			countingOverviewURL = "countingOverview.xhtml?" + getQueryString();
			reportingUnitTypeId = getRequestParameter("reportingUnitType", ReportingUnitTypeId::valueOf);
			if (reportingUnitTypeId != null && reportingUnitTypeId != FYLKESVALGSTYRET) {
				throw new IllegalArgumentException(
						format("expected <reportingUnitType> to be <%s>, but was <%s>", FYLKESVALGSTYRET, reportingUnitTypeId));
			}

			initCounts();
			initCountControllers();
			if (isStartCountingXhtmlPath()) {
				initTabsAndRedirectToCountingXhtml();
			}
		} catch (Exception e) {
			logAndBuildDetailErrorMessage(e);
		}
	}

	public String getPageTitle() {
		switch (countCategory) {
		case FO:
			return "@menu.counting.ordinary_advance_votes";
		case FS:
			return "@menu.counting.late_advance_votes";
		case VO:
			return "@menu.counting.regular_electionday_votes";
		case VS:
			return "@menu.counting.special_cover_votes";
		case VB:
			return "@menu.counting.emergency_envelopes";
		case VF:
			return "@menu.counting.foreign_votes";
		case BF:
			return "@menu.counting.foreign_votes_borough";
		default:
			return "NA";
		}
	}

	public String getCountingOverviewURL() {
		return countingOverviewURL;
	}

	private void initCountControllers() {
		List<CountController> countControllers = asList(
				protocolCountController,
				protocolAndPreliminaryCountController,
				preliminaryCountController,
				finalCountController,
				countyFinalCountController,
				compareCountsController);
		for (CountController countController : countControllers) {
			countController.setStartCountingController(this);
			countController.setUserDataController(userDataController);
			countController.initCountController();
		}
		if (isUserOnCountyLevel()) {
			compareCountsController.setFinalCountController(countyFinalCountController);
		} else if (isUserOnMunicipalityLevelOrHigher()) {
			compareCountsController.setFinalCountController(finalCountController);
		}
	}

	public boolean isBreadCrumbRendered() {
		return breadCrumbRendered;
	}

	private boolean isStartCountingXhtmlPath() {
		return getFacesContext().getExternalContext().getRequestServletPath().contains("startCounting.xhtml");
	}

	public ElectionPath getContestPath() {
		return contestPath;
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}

	public CountCategory getCountCategory() {
		return countCategory;
	}

	private void initCounts() {
		countContext = new CountContext(contestPath, countCategory);
		counts = countingService.getCounts(userData, getCountContext(), areaPath);
	}

	private void initTabsAndRedirectToCountingXhtml() throws IOException {
		initTabs();
		getFacesContext().getExternalContext().redirect(COUNTING_PATH + "counting.xhtml?cid=" + getCid());
	}

	private void initTabs() {
		buildTabs();
		for (int i = 0; i < tabs.size(); i++) {
			Tab tab = getTabs().get(i);
			tab.getController().setTabIndex(i);
			tab.getController().init();
			if (tab.isCurrent()) {
				setCurrentTab(i);
			}
		}
	}

	private void buildTabs() {
		tabs = new ArrayList<>();
		addTabAndSetIndex(protocolCountsTab());
		addTabAndSetIndex(preliminaryCountTab());
		addTabAndSetIndex(protocolAndPreliminaryCountTab());
		addTabAndSetIndex(finalCountTab());
		addTabAndSetIndex(countyFinalCountTab());
		addTabAndSetIndex(compareCountsTab());
	}

	private void addTabAndSetIndex(Tab tab) {
		if (tab != null) {
			tabs.add(tab);
			tabs.get(tabs.size() - 1).getController().setTabIndex(tabs.size() - 1);
		}
	}

	private Tab protocolCountsTab() {
		if (isUserOnCountyLevel() || getCounts().hasProtocolAndPreliminaryCount() || !getCounts().hasProtocolCounts()) {
			return null;
		}
		String template = getCounts().getProtocolCounts().size() > 1 ? "templates/protocolCounts.xhtml" : "templates/protocolCount.xhtml";
		return new Tab("P", template, protocolCountController, !getCounts().isProtocolCountsApproved());
	}

	private Tab preliminaryCountTab() {
		if (getCounts().hasProtocolAndPreliminaryCount()) {
			return null;
		}
		if (isUserOnCountyLevel() && getCounts().municipalityCountsFinal()) {
			return null;
		}
		if (isUserOnMunicipalityLevelOrHigher() || erBrukerPaaTellekrets() || isElectionOnBoroughLevel()) {
			boolean protocolCountsApproved = !getCounts().hasProtocolCounts() || getCounts().isProtocolCountsApproved();
			boolean isCurrentTab = protocolCountsApproved && !getCounts().hasApprovedPreliminaryCount() && !userData.isSamiElectionCountyUser();
			return new Tab("F", "templates/preliminaryCount.xhtml", preliminaryCountController, isCurrentTab);
		}
		return null;
	}

	private Tab protocolAndPreliminaryCountTab() {
		if (!isUserOnCountyLevel() && getCounts().hasProtocolAndPreliminaryCount()) {
			boolean isCurrentTab = !getCounts().hasApprovedProtocolAndPreliminaryCount();
			return new Tab("PF", "templates/protocolAndPreliminaryCount.xhtml", protocolAndPreliminaryCountController, isCurrentTab);
		}
		return null;
	}

	private Tab finalCountTab() {
		if (isUserOnCountyLevel() && getCounts().municipalityCountsFinal()) {
			return new Tab("KE", "@count.tab.type[KE].approved", "templates/finalCount.xhtml", finalCountController, false);
		}
		if (isUserOnMunicipalityLevelOrHigher() && getCounts().municipalityCountsFinal()) {
			boolean isCurrentTab = getCounts().hasApprovedPreliminaryCount() && !getCounts().hasApprovedFinalCount();
			return new Tab("E", "templates/finalCount.xhtml", finalCountController, isCurrentTab);
		}
		return null;
	}

	private Tab countyFinalCountTab() {
		if (isUserOnCountyLevel()) {
			return new Tab("M", "templates/countyFinalCount.xhtml", countyFinalCountController, !getCounts().hasApprovedCountyFinalCount());
		}
		return null;
	}

	private Tab compareCountsTab() {
		if (isUserOnCountyLevel()) {
			return new Tab("compare", "@count.tab.compare", "templates/compareFinalCounts.xhtml", compareCountsController, getCounts()
					.hasApprovedCountyFinalCount());
		}
		if (isUserOnMunicipalityLevelOrHigher() && getCounts().municipalityCountsFinal()) {
			return new Tab("compare", "@count.tab.compare", "templates/compareFinalCounts.xhtml", compareCountsController, getCounts().hasApprovedFinalCount());
		}
		return null;
	}

	private boolean isUserOnMunicipalityLevelOrHigher() {
		return userData.getOperatorAreaPath().getLevel().getLevel() <= AreaLevelEnum.MUNICIPALITY.getLevel();
	}
	
	private boolean erBrukerPaaTellekrets() {
		return userData.operatorValggeografiSti().isStemmekretsSti() && counts.isTellekrets();
	}

	private boolean isElectionOnBoroughLevel() {
		MvElection mvElection = electionService.findSingleByPath(ValghierarkiSti.valgSti(contestPath));
		return mvElection.getElection().getAreaLevel() == AreaLevelEnum.BOROUGH.getLevel();
	}

	public List<Tab> getTabs() {
		return tabs;
	}

	public int getCurrentTab() {
		return currentTab;
	}

	public void setCurrentTab(int currentTab) {
		boolean changed = this.currentTab != currentTab;
		this.currentTab = currentTab;
		for (Tab tab : getTabs()) {
			tab.setCurrent(currentTab == tab.getController().getTabIndex());
			if (changed && tab.isCurrent() && tab.getController() instanceof CompareCountsController) {
				((CompareCountsController) tab.getController()).setupDefaultCompare();
			}
		}
	}

	public CountController getCurrentController() {
		return tabs.get(currentTab).getController();
	}

	public Counts getCounts() {
		return counts;
	}

	@Override
	protected MessageProvider getMessageProvider() {
		return messageProvider;
	}

	boolean isContestOnCountyLevel() {
		CountingConfiguration countingConfiguration = countingConfigurationService.getCountingConfiguration(userData, getCountContext(), getAreaPath());
		return countingConfiguration.isContestOnCountyLevel();
	}

	@Override
	public CountContext getCountContext() {
		return countContext;
	}

	@Override
	public boolean isUserOnCountyLevel() {
		return userData.getOperatorAreaPath().isCountyLevel() || userData.isElectionEventAdminUser() && reportingUnitTypeId == FYLKESVALGSTYRET
				|| userData.isSamiElectionCountyUser();
	}
}
