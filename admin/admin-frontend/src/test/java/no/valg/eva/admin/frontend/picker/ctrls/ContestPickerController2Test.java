package no.valg.eva.admin.frontend.picker.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import org.primefaces.event.TabChangeEvent;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ContestPickerController2Test extends BaseFrontendTest {

	@Test
	public void init_withNoContestInfos_returnsWrongLevelError() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);
		when(getInjectMock(ContestInfoService.class).contestOrElectionByAreaPath(any(AreaPath.class))).thenReturn(new ArrayList<>());

		ctrl.init(getMvAreaMock("111111.11.11"), null, false);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@settlement.error.wrong_level");
	}

	@Test
	public void init_withContestInfosAndNoElectionPath_contest1ShouldBeSelected() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);
		List<ContestInfo> contestInfoStubs = getContestInfoStubs();
		when(getInjectMock(ContestInfoService.class).contestOrElectionByAreaPath(any(AreaPath.class))).thenReturn(contestInfoStubs);

		ctrl.init(getMvAreaMock("111111.11.11"), null, false);

		assertThat(ctrl.getActiveContest()).isEqualTo(0);
		assertThat(ctrl.getContestInfo().getContestName()).isEqualTo("Contest1");
	}

	@Test
	public void init_withContestInfosAndElectionPath_contest2ShouldBeSelected() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);
		List<ContestInfo> contestInfoStubs = getContestInfoStubs();
		when(getInjectMock(ContestInfoService.class).contestOrElectionByAreaPath(any(AreaPath.class))).thenReturn(contestInfoStubs);

		ctrl.init(getMvAreaMock("111111.11.11"), getElectionPathMock("111111.12"), false);

		assertThat(ctrl.getActiveContest()).isEqualTo(1);
		assertThat(ctrl.getContestInfo().getContestName()).isEqualTo("Contest2");
	}

	@Test
	public void init_withHideElectionLevel_returns1ContestOnly() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);
		List<ContestInfo> contestInfoStubs = Arrays.asList(
			new ContestInfo("111111.11.11.111111", "Election1", "Contest1", "111111.11.11.1111"),
			new ContestInfo("111111.11.11", "Election2", "Contest2", "111111.11.11.1111"));
		when(getInjectMock(ContestInfoService.class).contestOrElectionByAreaPath(any(AreaPath.class))).thenReturn(contestInfoStubs);

		ctrl.init(getMvAreaMock("111111.11.11"), getElectionPathMock("111111.12"), true);

		assertThat(ctrl.getContests()).hasSize(1);
	}

	@Test
	public void onTabChange_withListener_shouldExecuteListener() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);
		TabChangeEvent tabChangeEventMock = createMock(TabChangeEvent.class);
		ContestInfo contestInfoMock = createMock(ContestInfo.class);
		ContestPickerController2.TabChangeListener listenerMock = createMock(ContestPickerController2.TabChangeListener.class);
		when(tabChangeEventMock.getData()).thenReturn(contestInfoMock);

		ctrl.setTabChangeListener(listenerMock);
		ctrl.onTabChange(tabChangeEventMock);

		verify(listenerMock).onTabChange(contestInfoMock);
	}

	@Test
	public void getElectionLevelName_withNull_returnsNull() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);

		assertThat(ctrl.getElectionLevelName(null)).isNull();
	}

	@Test
	public void getElectionLevelName_withNoContestId_returnsElectionName() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);

		assertThat(ctrl.getElectionLevelName(getContestInfoStub(1))).isEqualTo("Election1");
	}

	@Test
	public void getElectionLevelName_withContestId_returnsElectionAndContestName() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);

		assertThat(ctrl.getElectionLevelName(new ContestInfo("111111.11.11.111111", "Election", "Contest", "111111.11"))).isEqualTo("Election Contest");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void initWithElectionsFromElectionEvent_electionPathIsNotOnElectionEventLevel_throwsIllegalArgumentException() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);

		ElectionPath electionPathNotOnElectionEventLevel = ElectionPath.from("150001", "01");
		ctrl.initWithElectionsFromElectionEvent(electionPathNotOnElectionEventLevel);
	}

	@Test
	public void initWithElectionsFromElectionEvent_setsContestInfosAndActiveContest() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);
		when(getInjectMock(ContestInfoService.class).electionsInElectionEvent(any(UserData.class), any(ElectionPath.class))).thenReturn(new ArrayList<>());
		ElectionPath electionEventPath = ElectionPath.from("150001");
		
		ctrl.initWithElectionsFromElectionEvent(electionEventPath);
		assertThat(ctrl.getActiveContest()).isEqualTo(0);
	}
	
	@Test
	public void initForSamiElectionCountyUser_setsContestInfosAndActiveContest() throws Exception {
		ContestPickerController2 ctrl = initializeMocks(ContestPickerController2.class);
		ContestInfo contestInfo = createMock(ContestInfo.class);
		when(getInjectMock(ContestInfoService.class).findContestInfoByPath(any(ElectionPath.class))).thenReturn(contestInfo);
		ElectionPath contestPath = ElectionPath.from("970004.01.01.000001");
		ctrl.initForSamiElectionCountyUser(contestPath);
		assertThat(ctrl.getContestInfo()).isEqualTo(contestInfo);
	}
	
	private MvArea getMvAreaMock(String path) {
		MvArea mvAreaMock = createMock(MvArea.class);
		when(mvAreaMock.getAreaPath()).thenReturn(path);
		return mvAreaMock;
	}

	private ElectionPath getElectionPathMock(String path) {
		ElectionPath electionPathMock = createMock(ElectionPath.class);
		when(electionPathMock.path()).thenReturn(path);
		return electionPathMock;
	}

	private List<ContestInfo> getContestInfoStubs() {
		return Arrays.asList(getContestInfoStub(1), getContestInfoStub(2));
	}

	private ContestInfo getContestInfoStub(int i) {
		return new ContestInfo("111111.1" + i, "Election" + i, "Contest" + i, "111111.11.11.111" + i);
	}

}
