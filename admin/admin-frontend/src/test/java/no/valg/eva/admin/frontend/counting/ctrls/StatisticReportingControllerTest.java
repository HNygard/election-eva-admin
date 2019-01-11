package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.frontend.picker.ctrls.ContestPickerController2;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.KONTEKST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for StatisticReportingController
 */
public class StatisticReportingControllerTest extends BaseFrontendTest {

	@Test
	public void isRenderCorrectedResult_withCountyLevelContestAndMunicipalityLevelUser_returnsFalse() throws Exception {
		StatisticReportingController ctrl = initializeMocks(StatisticReportingController.class);
		ContestInfo contestInfo = mockField("contestInfo", ContestInfo.class);
		when(contestInfo.getAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);
		when(getUserDataMock().isMunicipalityLevelUser()).thenReturn(true);

		assertThat(ctrl.isRenderCorrectedResult()).isFalse();
	}

	@Test
	public void isRenderCorrectedResult_withSamiContestAndMunicipalityLevelUser_returnsFalse() throws Exception {
		StatisticReportingController ctrl = initializeMocks(StatisticReportingController.class);
		ContestInfo contestInfo = mockField("contestInfo", ContestInfo.class);
		when(contestInfo.getAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
		when(getUserDataMock().isMunicipalityLevelUser()).thenReturn(true);
		Contest contest = mockField("contest", Contest.class);
		when(contest.isSingleArea()).thenReturn(false);

		assertThat(ctrl.isRenderCorrectedResult()).isFalse();
	}

	@Test
	public void isRenderCorrectedResult_withSamiContestAndSamiCountyLevelUser_returnsTrue() throws Exception {
		StatisticReportingController ctrl = initializeMocks(StatisticReportingController.class);
		ContestInfo contestInfo = mockField("contestInfo", ContestInfo.class);
		when(contestInfo.getAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
		when(getUserDataMock().isSamiElectionCountyUser()).thenReturn(true);
		Contest contest = mockField("contest", Contest.class);
		when(contest.isSingleArea()).thenReturn(false);
		when(getInjectMock(UserDataController.class).getUserAccess().isResultatRapporter()).thenReturn(true);
		List<Valgnattrapportering> valgnattrapporterings = new ArrayList<>();
		Valgnattrapportering fakeValgnattrapportering = mock(Valgnattrapportering.class);
		when(fakeValgnattrapportering.isNotSendt()).thenReturn(true);
		valgnattrapporterings.add(fakeValgnattrapportering);
		mockFieldValue("oppgjorsskjemaRapportering", valgnattrapporterings);

		assertThat(ctrl.isRenderCorrectedResult()).isTrue();
	}

	@Test
	public void isRenderCorrectedResult_withCountyLevelContestAndNotMunicipalityLevelUser_returnsTrue() throws Exception {
		StatisticReportingController ctrl = initializeMocks(StatisticReportingController.class);
		ContestInfo contestInfo = mockField("contestInfo", ContestInfo.class);
		when(contestInfo.getAreaLevel()).thenReturn(AreaLevelEnum.COUNTY);
		when(getUserDataMock().isMunicipalityLevelUser()).thenReturn(false);
		Contest contest = mockField("contest", Contest.class);
		when(contest.isSingleArea()).thenReturn(true);
		assertThat(ctrl.isRenderCorrectedResult()).isFalse();
	}

	@Test
	public void isRenderCorrectedResult_withAccessAndData_returnsTrue() throws Exception {
		StatisticReportingController ctrl = initializeMocks(StatisticReportingController.class);
		List<Valgnattrapportering> valgnattrapporterings = new ArrayList<>();
		Valgnattrapportering fakeValgnattrapportering = mock(Valgnattrapportering.class);
		when(fakeValgnattrapportering.isNotSendt()).thenReturn(true);
		valgnattrapporterings.add(fakeValgnattrapportering);
		mockFieldValue("oppgjorsskjemaRapportering", valgnattrapporterings);
		Contest contest = mockField("contest", Contest.class);
		when(contest.isSingleArea()).thenReturn(true);
		when(getInjectMock(UserDataController.class).getUserAccess().isResultatRapporter()).thenReturn(true);

		assertThat(ctrl.isRenderCorrectedResult()).isTrue();
	}

	@Test
	public void doInit_samiElectionCountyUser_findsContestInfoFromContestPath() throws Exception {
		StatisticReportingController ctrl = initializeMocks(StatisticReportingController.class);
		when(getUserDataMock().isSamiElectionCountyUser()).thenReturn(true);
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_MUNICIPALITY);
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
		getServletContainer().setRequestParameter(KONTEKST.toString(), null);

		ctrl.init();

		verify(getInjectMock(ContestPickerController2.class), times(1)).initForSamiElectionCountyUser(any(ElectionPath.class));
	}
}
