package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.evote.service.configuration.ContestAreaService;
import no.evote.service.counting.ContestReportService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CountyVoteCountsControllerTest extends BaseFrontendTest {

	private static final AreaPath MUNICIPALITY_AREA_LEVEL = AreaPath.from("111111.11.11.1111");
	private static final AreaPath COUNTY_AREA_LEVEL = AreaPath.from("111111.11.11");
	private static final String CONTEST_ELECTION_LEVEL = "111111.11.11.111111";

	@Test
	public void init_withUserOnCountyLevel_shouldAddErrorMessage() throws Exception {
        testFor(COUNTY_AREA_LEVEL);
	}

    @Test
	public void init_withNoContestsFound_shouldAddErrorMessage() throws Exception {
        testFor(MUNICIPALITY_AREA_LEVEL);
	}

    private void testFor(AreaPath countyAreaLevel) throws Exception {
        CountyVoteCountsController ctrl = initializeMocks(CountyVoteCountsController.class);
        when(getUserDataMock().getOperatorAreaPath()).thenReturn(countyAreaLevel);

        ctrl.init();

        assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@settlement.error.wrong_level");
        assertThat(ctrl.getContestPath()).isNull();
    }

	@Test
	public void init_withOneCountyContestFound_verifyControllerState() throws Exception {
		CountyVoteCountsController ctrl = initializeMocks(CountyVoteCountsController.class);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(MUNICIPALITY_AREA_LEVEL);
		List<ContestInfo> contests = mockList(2, ContestInfo.class);
		when(contests.get(0).getAreaPath().isCountyLevel()).thenReturn(true);
		when(contests.get(0).getElectionPath().path()).thenReturn(CONTEST_ELECTION_LEVEL);
		when(getInjectMock(ContestInfoService.class).contestsByAreaAndElectionPath(any(UserData.class), eq(MUNICIPALITY_AREA_LEVEL), any(ElectionPath.class),
                any())).thenReturn(contests);
		when(getInjectMock(ContestAreaService.class).findContestAreasForContestPath(any(UserData.class), any(ElectionPath.class)))
			.thenReturn(mockList(1, ContestArea.class));
		when(getInjectMock(ContestReportService.class).hasContestReport(eq(getUserDataMock()), any(ElectionPath.class), any(AreaPath.class))).thenReturn(true);

		ctrl.init();

		assertThat(ctrl.getContestPath()).isEqualTo(CONTEST_ELECTION_LEVEL);
		assertThat(ctrl.getContestList()).hasSize(1);
		assertThat(ctrl.getContestList().get(0)).isSameAs(contests.get(0));
		assertThat(ctrl.getSettlementSummary()).isNotNull();
	}
}
