package no.valg.eva.admin.frontend.settlement.ctrls;

import no.evote.security.UserData;
import no.evote.service.configuration.ContestAreaService;
import no.evote.service.counting.ContestReportService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class BaseSettlementControllerTest extends BaseFrontendTest {

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected <CONTEST> election path, but got <ELECTION>")
	public void init_withContestPathAtWrongLevel_shouldThrowIllegalArgumentException() throws Exception {
		BaseSettlementController ctrl = initializeMocks(new MySettlementController());
		getServletContainer().setRequestParameter("contestPath", "752900.01.02");

		ctrl.init();
	}

	@Test
	public void init_withContestPathNotInContestList_shouldAddWrongLevelMessage() throws Exception {
		BaseSettlementController ctrl = initializeMocks(new MySettlementController());
		getServletContainer().setRequestParameter("contestPath", "752900.01.02.000303");
		stub_contestInfoService_contestByAreaPath(2);

		ctrl.init();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@settlement.error.wrong_level");
	}

	@Test
	public void init_withValidContestPath_shouldHaveContestPath() throws Exception {
		BaseSettlementController ctrl = initializeMocks(new MySettlementController());
		getServletContainer().setRequestParameter("contestPath", "752900.01.02.000302");
		stub_contestInfoService_contestByAreaPath(2);
		stub_contestAreaService_findContestAreasForContest();
		stub_contestReportService_hasContestReport(true);

		ctrl.init();

		assertThat(ctrl.getContestPath()).isEqualTo("752900.01.02.000302");
	}

	@Test
	public void init_withOneItemInContestListThatHasNoReport_shouldAddNoCountsError() throws Exception {
		BaseSettlementController ctrl = initializeMocks(new MySettlementController());
		getServletContainer().setRequestParameter("x", "x");
		stub_contestInfoService_contestByAreaPath(1);
		stub_contestAreaService_findContestAreasForContest();

		ctrl.init();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@settlement.error.no_counts");
	}

	@Test
	public void init_withTwoItemsInContestList_returnsNullForContestPath() throws Exception {
		BaseSettlementController ctrl = initializeMocks(new MySettlementController());
		getServletContainer().setRequestParameter("x", "x");
		stub_contestInfoService_contestByAreaPath(2);

		ctrl.init();

		assertThat(ctrl.getContestPath()).isNull();
		assertThat(ctrl.getContestList()).hasSize(2);
	}

	@Test
	public void getSelectContestHeader_shouldReturnChooseHeaderString() throws Exception {
		BaseSettlementController ctrl = initializeMocks(new MySettlementController());

		assertThat(ctrl.getSelectContestHeader()).isEqualTo("@common.choose @election_level[3].name");
	}

	@Test
	public void setContestPath_withInvalidPath_returnsNullForContestPath() throws Exception {
		BaseSettlementController ctrl = initializeMocks(new MySettlementController());
		getServletContainer().setRequestParameter("x", "x");
		stub_contestInfoService_contestByAreaPath(2);

		ctrl.init();
		ctrl.setContestPath("752900.01.02.000310");

		assertThat(ctrl.getContestPath()).isNull();
	}

	@Test
	public void getPageTitleMeta_shouldReturnModelList() throws Exception {
		BaseSettlementController ctrl = initializeMocks(new MySettlementController(BaseSettlementController.VIEW_SETTLEMENT_STATUS));
		List<PageTitleMetaModel> list = new ArrayList<>();
		list.add(new PageTitleMetaModel("a", "b"));
		ContestInfo contestInfoMock = mockField("contestInfo", ContestInfo.class);
		when(contestInfoMock.getElectionPath().path()).thenReturn("111111.22.33");
		when(getInjectMock(PageTitleMetaBuilder.class).settlementTitle(any(MvElection.class), any(MvArea.class))).thenReturn(list);
		mockFieldValue("hasContestReport", true);
		mockFieldValue("settlementDone", true);

		List<PageTitleMetaModel> result = ctrl.getPageTitleMeta();

		assertThat(result).hasSize(4);
		assertThat(result.get(0).getLabel()).isEqualTo("a");
		assertThat(result.get(0).getValue()).isEqualTo("b");
		assertThat(result.get(0).isLink()).isFalse();
		assertThat(result.get(1).getLabel()).isEqualTo("@menu.settlement.result");
		assertThat(result.get(1).getValue()).isEqualTo("settlementSummary.xhtml?contestPath=111111.22.33&faces-redirect=true");
		assertThat(result.get(1).isLink()).isTrue();
		assertThat(result.get(2).getLabel()).isEqualTo("@menu.settlement.mandate_distribution");
		assertThat(result.get(2).getValue()).isEqualTo("settlementResult.xhtml?contestPath=111111.22.33&faces-redirect=true");
		assertThat(result.get(2).isLink()).isTrue();
		assertThat(result.get(3).getLabel()).isEqualTo("@menu.settlement.candidate_announcement");
		assertThat(result.get(3).getValue()).isEqualTo("candidateAnnouncement.xhtml?contestPath=111111.22.33&faces-redirect=true");
		assertThat(result.get(3).isLink()).isTrue();
	}

	@Test
	public void backToSelectContest_returns() throws Exception {
		BaseSettlementController ctrl = initializeMocks(new MySettlementController(BaseSettlementController.VIEW_SETTLEMENT_STATUS));

		assertThat(ctrl.backToSelectContest()).isEqualTo("settlementStatus.xhtml?faces-redirect=true");
	}

	private void stub_contestInfoService_contestByAreaPath(int num) {
		List<ContestInfo> list = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			ContestInfo stub = createMock(ContestInfo.class);
			when(stub.getElectionPath()).thenReturn(ElectionPath.from("752900.01.02.00030" + (i + 1)));
			list.add(stub);
		}
		when(getInjectMock(ContestInfoService.class).contestsByAreaAndElectionPath(any(UserData.class),
                any(AreaPath.class), any(ElectionPath.class), any())).thenReturn(list);
	}

	private void stub_contestAreaService_findContestAreasForContest() {
		List<ContestArea> areas = new ArrayList<>();
		areas.add(createMock(ContestArea.class));
		when(getInjectMock(ContestAreaService.class).findContestAreasForContestPath(any(UserData.class), any(ElectionPath.class))).thenReturn(areas);
	}

	private void stub_contestReportService_hasContestReport(boolean value) {
		when(getInjectMock(ContestReportService.class).hasContestReport(eq(getUserDataMock()), any(ElectionPath.class), any(AreaPath.class))).thenReturn(value);
	}

	class MySettlementController extends BaseSettlementController {

		private String view;

		MySettlementController() {
			this(null);
		}

		MySettlementController(String view) {
			this.view = view;
		}

		@Override
		protected void initView() {

		}

		@Override
		protected String getView() {
			return view;
		}
	}
}

