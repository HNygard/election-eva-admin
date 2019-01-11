package no.valg.eva.admin.frontend.counting.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static no.valg.eva.admin.frontend.counting.ctrls.StartCountingController.COUNTING_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.faces.context.ExternalContext;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.ServletContainer;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StartCountingControllerTest extends BaseCountControllerTest {

	private StartCountingController startCountingController;
	private ServletContainer container;

	@BeforeMethod
	public void setUp() throws Exception {
		startCountingController = initializeMocks(StartCountingController.class);
		container = getServletContainer();

		when(getConversationMock().getId()).thenReturn("CID");
		container.setQueryString("");
	}

	@Test
	public void doInit_withCountyFinalCounts_checkTabs() throws Exception {
		setUserData(AreaLevelEnum.COUNTY, "130001.47.01");
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");
		when(getCountsMock().municipalityCountsFinal()).thenReturn(true);
		when(getCountsMock().hasCountyFinalCounts()).thenReturn(true);
		when(getFinalCountControllerMock().getTabIndex()).thenReturn(0);
		when(getCountyFinalCountControllerMock().getTabIndex()).thenReturn(1);
		when(getCompareCountsControllerMock().getTabIndex()).thenReturn(2);

		startCountingController.doInit();

		assertThat(startCountingController.getTabs().size()).isEqualTo(3);
		assertThat(startCountingController.getCurrentTab()).isEqualTo(1);
		assertThat(startCountingController.getTabs().get(0).getTemplate()).isEqualTo("templates/finalCount.xhtml");
		assertThat(startCountingController.getTabs().get(0).isCurrent()).isFalse();
		assertThat(startCountingController.getTabs().get(1).getTemplate()).isEqualTo("templates/countyFinalCount.xhtml");
		assertThat(startCountingController.getTabs().get(1).isCurrent()).isTrue();
		assertThat(startCountingController.getTabs().get(2).getTemplate()).isEqualTo("templates/compareFinalCounts.xhtml");
		assertThat(startCountingController.getTabs().get(2).isCurrent()).isFalse();
	}

	@Test
	public void doInit_withSamiCountyFinalCounts_checkTabs() throws Exception {
		setSamiUserData(AreaLevelEnum.ROOT);
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");
		when(getCountsMock().municipalityCountsFinal()).thenReturn(false);
		when(getCountsMock().hasCountyFinalCounts()).thenReturn(true);
		when(getPreliminaryCountControllerMock().getTabIndex()).thenReturn(0);
		when(getCountyFinalCountControllerMock().getTabIndex()).thenReturn(1);
		when(getCompareCountsControllerMock().getTabIndex()).thenReturn(2);

		startCountingController.doInit();

		assertThat(startCountingController.getTabs().size()).isEqualTo(3);
		assertThat(startCountingController.getCurrentTab()).isEqualTo(1);
		assertThat(startCountingController.getTabs().get(0).getTemplate()).isEqualTo("templates/preliminaryCount.xhtml");
		assertThat(startCountingController.getTabs().get(0).isCurrent()).isFalse();
		assertThat(startCountingController.getTabs().get(1).getTemplate()).isEqualTo("templates/countyFinalCount.xhtml");
		assertThat(startCountingController.getTabs().get(1).isCurrent()).isTrue();
		assertThat(startCountingController.getTabs().get(2).getTemplate()).isEqualTo("templates/compareFinalCounts.xhtml");
		assertThat(startCountingController.getTabs().get(2).isCurrent()).isFalse();
	}

	@Test
	public void doInit_withProtocolAndPreliminaryCount_checkTabs() throws Exception {
		setUserData(AreaLevelEnum.POLLING_DISTRICT, "130001.47.01.0101.010100.0101");
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");
		when(getCountsMock().hasProtocolAndPreliminaryCount()).thenReturn(true);

		startCountingController.doInit();

		assertThat(startCountingController.getTabs().size()).isEqualTo(1);
		assertThat(startCountingController.getCurrentTab()).isEqualTo(0);
		assertThat(startCountingController.getTabs().get(0).getTemplate()).isEqualTo("templates/protocolAndPreliminaryCount.xhtml");
		assertThat(startCountingController.getTabs().get(0).isCurrent()).isTrue();
	}

	@Test
	public void doInit_withProtocolPreliminaryCountAndUserOnMunicipality_checkTabs() throws Exception {
		setUserData(AreaLevelEnum.POLLING_DISTRICT, "130001.47.01.0101");
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");
		when(getCountsMock().hasProtocolAndPreliminaryCount()).thenReturn(true);
		when(getCountsMock().municipalityCountsFinal()).thenReturn(true);

		startCountingController.doInit();

		assertThat(startCountingController.getTabs().size()).isEqualTo(3);
		assertThat(startCountingController.getTabs().get(0).getTemplate()).isEqualTo("templates/protocolAndPreliminaryCount.xhtml");
		assertThat(startCountingController.getTabs().get(1).getTemplate()).isEqualTo("templates/finalCount.xhtml");
		assertThat(startCountingController.getTabs().get(2).getTemplate()).isEqualTo("templates/compareFinalCounts.xhtml");
	}

	@Test
	public void doInit_withProtocolPreliminaryCountAndUserOnMunicipalityNoMunicipalityFinalCount_checkTabs() throws Exception {
		setUserData(MUNICIPALITY, "130001.47.01.0101");
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");
		when(getCountsMock().hasProtocolAndPreliminaryCount()).thenReturn(true);
		when(getCountsMock().municipalityCountsFinal()).thenReturn(false);

		startCountingController.doInit();

		assertThat(startCountingController.getTabs().size()).isEqualTo(1);
		assertThat(startCountingController.getTabs().get(0).getTemplate()).isEqualTo("templates/protocolAndPreliminaryCount.xhtml");
	}

	@Test
	public void doInit_withPreliminaryCountAndUserOnCountyNoMunicipalityFinalCount_checkTabs() throws Exception {
		setUserData(AreaLevelEnum.COUNTY, "130001.47.01");
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");
		when(getCountsMock().municipalityCountsFinal()).thenReturn(false);
		when(getCountsMock().hasProtocolAndPreliminaryCount()).thenReturn(false);
		when(getCountsMock().hasPreliminaryCount()).thenReturn(true);
		when(getCountsMock().hasApprovedPreliminaryCount()).thenReturn(true);
		when(getCountsMock().hasCountyFinalCounts()).thenReturn(true);
		when(getPreliminaryCountControllerMock().getTabIndex()).thenReturn(0);
		when(getCountyFinalCountControllerMock().getTabIndex()).thenReturn(1);
		when(getCompareCountsControllerMock().getTabIndex()).thenReturn(2);

		startCountingController.doInit();

		assertThat(startCountingController.getTabs().size()).isEqualTo(3);
		assertThat(startCountingController.getCurrentTab()).isEqualTo(1);
		assertThat(startCountingController.getTabs().get(0).getTemplate()).isEqualTo("templates/preliminaryCount.xhtml");
		assertThat(startCountingController.getTabs().get(0).isCurrent()).isFalse();
		assertThat(startCountingController.getTabs().get(1).getTemplate()).isEqualTo("templates/countyFinalCount.xhtml");
		assertThat(startCountingController.getTabs().get(1).isCurrent()).isTrue();
		assertThat(startCountingController.getTabs().get(2).getTemplate()).isEqualTo("templates/compareFinalCounts.xhtml");
		assertThat(startCountingController.getTabs().get(2).isCurrent()).isFalse();
	}

	@Test
	public void doInit_withProtocolAndPreliminaryCountAndUserOnMunicipality_checkTabs() throws Exception {
		setUserData(AreaLevelEnum.POLLING_DISTRICT, "130001.47.01.0101");
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");
		when(getCountsMock().hasProtocolCounts()).thenReturn(true);
		when(getCountsMock().municipalityCountsFinal()).thenReturn(true);

		startCountingController.doInit();

		assertThat(startCountingController.getTabs().size()).isEqualTo(4);
		assertThat(startCountingController.getTabs().get(0).getTemplate()).isEqualTo("templates/protocolCount.xhtml");
		assertThat(startCountingController.getTabs().get(1).getTemplate()).isEqualTo("templates/preliminaryCount.xhtml");
		assertThat(startCountingController.getTabs().get(2).getTemplate()).isEqualTo("templates/finalCount.xhtml");
		assertThat(startCountingController.getTabs().get(3).getTemplate()).isEqualTo("templates/compareFinalCounts.xhtml");
	}

	@Test
	public void action_withSeveralProtocolAndPreliminaryCountAndUserOnMunicipality_checkTabs() throws Exception {
		defaultSetup();
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");
		when(getCountsMock().municipalityCountsFinal()).thenReturn(true);

		startCountingController.doInit();

		assertThat(startCountingController.getTabs().size()).isEqualTo(4);
		assertThat(startCountingController.getTabs().get(0).getTemplate()).isEqualTo("templates/protocolCounts.xhtml");
		assertThat(startCountingController.getTabs().get(1).getTemplate()).isEqualTo("templates/preliminaryCount.xhtml");
		assertThat(startCountingController.getTabs().get(2).getTemplate()).isEqualTo("templates/finalCount.xhtml");
		assertThat(startCountingController.getTabs().get(3).getTemplate()).isEqualTo("templates/compareFinalCounts.xhtml");
	}

	@Test
	public void action_withSeveralProtocolAndPreliminaryCountAndUserOnParentPollingDistrict_checkTabs() throws Exception {
		defaultSetup();
		setUserData(POLLING_DISTRICT, "130001.47.01.0101.010100.0001");
		when(getCountsMock().isTellekrets()).thenReturn(true);
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");

		startCountingController.doInit();

		assertThat(startCountingController.getTabs().size()).isEqualTo(2);
		assertThat(startCountingController.getTabs().get(0).getTemplate()).isEqualTo("templates/protocolCounts.xhtml");
		assertThat(startCountingController.getTabs().get(1).getTemplate()).isEqualTo("templates/preliminaryCount.xhtml");
	}

	@Test
	public void doInit_withContestPathAndAreaPath_checkRedirect() throws Exception {
		setUserData(AreaLevelEnum.POLLING_DISTRICT, "130001.47.01.0101");
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "201301.01.01.010101");
		container.setRequestParameter("areaPath", "130001.47.01.0101.010100.0101");
		container.setServletPath("/secure/counting/startCounting.xhtml");
		container.setQueryString("test=test");

		startCountingController.doInit();

		ExternalContext externalContext = getFacesContextMock().getExternalContext();
		ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
		verify(externalContext).redirect(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).isEqualTo(COUNTING_PATH + "counting.xhtml?cid=CID");
		assertThat(startCountingController.getCountingOverviewURL()).isEqualTo("countingOverview.xhtml?test=test");
	}

	@Test
	public void doInit_withIllegalArgumentException_assertFacesMessage() throws Exception {
		container.setRequestParameter("category", "VO");
		container.setRequestParameter("contestPath", "abc");

		startCountingController.doInit();

		assertFacesMessage(SEVERITY_ERROR, "[@common.error.unexpected, 49774555]");
	}

	@Test
	public void doInit_withEvoteException_assertFacesMessage() throws Exception {
		ExternalContext stub = getFacesContextMock().getExternalContext();
		evoteException().when(stub).getRequestParameterValuesMap();

		startCountingController.doInit();

		assertFacesMessage(SEVERITY_ERROR, "[@common.error.unexpected, cb0e38f0]");
	}

	@Test
	public void isContestOnCountyLevel() throws Exception {
		assertThat(startCountingController.isContestOnCountyLevel()).isFalse();
	}

	@Test
	public void isUserOnCountyLevel_givenElectionEventAdminAndFylkesvalgstyret_returnTrue() throws Exception {
		container.setRequestParameter("category", VO);
		container.setRequestParameter("reportingUnitType", FYLKESVALGSTYRET);
		when(getUserDataMock().isElectionEventAdminUser()).thenReturn(true);
		startCountingController.doInit();
		assertThat(startCountingController.isUserOnCountyLevel()).isTrue();
	}

	@Test
	public void isUserOnCountyLevel_givenElectionEventAdmin_returnFalse() throws Exception {
		container.setRequestParameter("category", VO);
		when(getUserDataMock().isElectionEventAdminUser()).thenReturn(true);
		startCountingController.doInit();
		assertThat(startCountingController.isUserOnCountyLevel()).isFalse();
	}

	@Test
	public void isUserOnCountyLevel_givenCountyAdmin_returnTrue() throws Exception {
		container.setRequestParameter("category", VO);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AreaPath.from("111111.11.11"));
		startCountingController.doInit();
		assertThat(startCountingController.isUserOnCountyLevel()).isTrue();
	}

	@Test
	public void isUserOnCountyLevel_givenMunicipalityAdmin_returnFalse() throws Exception {
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AreaPath.from("111111.11.11.1111"));
		assertThat(startCountingController.isUserOnCountyLevel()).isFalse();
	}

	@Test
	public void isUserOnCountyLevel_givenSamiElectionCountyUser_returnTrue() throws Exception {
		when(getUserDataMock().isSamiElectionCountyUser()).thenReturn(true);
		assertThat(startCountingController.isUserOnCountyLevel()).isTrue();
	}

	@Test
	public void isUserOnCountyLevel_givenSamiElectionMunicipalityUser_returnFalse() throws Exception {
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AreaPath.from("111111.11.11.1111"));
		assertThat(startCountingController.isUserOnCountyLevel()).isFalse();
	}

	@Test
	public void doInit_givenNotFylkesvalgstyret_givesErrorMessage() throws Exception {
		container.setRequestParameter("category", VO);
		container.setRequestParameter("reportingUnitType", VALGSTYRET);

		startCountingController.doInit();

		assertFacesMessage(SEVERITY_ERROR, "[@common.error.unexpected, 7b6c2316]");

	}

	@Test(dataProvider = "getPageTitle")
	public void getPageTitle_withDataProvider_verifyExpected(CountCategory countCategory, String expected) throws Exception {
		StartCountingController ctrl = initializeMocks(StartCountingController.class);
		mockFieldValue("countCategory", countCategory);

		assertThat(ctrl.getPageTitle()).isEqualTo(expected);
	}

	@DataProvider
	public Object[][] getPageTitle() {
		return new Object[][] {
				{ FO, "@menu.counting.ordinary_advance_votes" },
				{ FS, "@menu.counting.late_advance_votes" },
				{ VO, "@menu.counting.regular_electionday_votes" },
				{ VS, "@menu.counting.special_cover_votes" },
				{ VB, "@menu.counting.emergency_envelopes" },
				{ VF, "@menu.counting.foreign_votes" },
				{ BF, "@menu.counting.foreign_votes_borough" }
		};
	}

	private void defaultSetup() throws Exception {
		setUserData(MUNICIPALITY, "130001.47.01.0101");
		when(getCountsMock().hasProtocolCounts()).thenReturn(true);
		when(getCountsMock().getProtocolCounts()).thenReturn(mockList(2, ProtocolCount.class));
	}

	private void setUserData(AreaLevelEnum areaLevel, String areaPathString) throws NoSuchFieldException, IllegalAccessException {
		UserData userData = getUserDataMock();
		when(userData.getOperatorRole().getMvArea()).thenReturn(mvArea(areaLevel, areaPathString));
		AreaPath areaPath = new AreaPath(mvArea(areaLevel, areaPathString).getPath());
		when(userData.getOperatorAreaPath()).thenReturn(areaPath);
		when(userData.operatorValggeografiSti()).thenReturn(ValggeografiSti.fra(areaPath));
	}

	private void setSamiUserData(final AreaLevelEnum areaLevel) throws Exception {
		setUserData(areaLevel, "130001");
		UserData userData = getUserDataMock();
		when(userData.isSamiElectionCountyUser()).thenReturn(true);
	}

	private MvArea mvArea(final AreaLevelEnum areaLevel, final String areaPath) {
		MvArea mvArea = new MvArea();
		mvArea.setAreaLevel(areaLevel.getLevel());
		mvArea.setAreaPath(areaPath);
		return mvArea;
	}

}

