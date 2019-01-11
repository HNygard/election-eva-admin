package no.evote.service.configuration;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class ContestAreaServiceEjbTest extends MockUtilsTestCase {

	@Test
	public void create_withExistingAreaInContest_throwsException() throws Exception {
		ContestAreaServiceEjb ejb = initializeMocks(ContestAreaServiceEjb.class);
		ContestArea contestArea = contestArea(mvArea(10L, 3));
		stub_findAreaLevelById(areaLevel(3));
        stub_findContestAreasForContest(Collections.singletonList(contestArea));

		try {
			ejb.create(createMock(UserData.class), contestArea);
			fail("EvoteException not thrown");
		} catch (EvoteException e) {
			assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ERROR_CODE_0450_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_AREA);
			assertThat(e.getParams()).isEqualTo(new String[] { "@area_level[3].name", "MunicipalityName", "ContestName" });
		}
	}

	@Test
	public void create_withExistingAreaInElection_throwsException() throws Exception {
		ContestAreaServiceEjb ejb = initializeMocks(ContestAreaServiceEjb.class);
		ContestArea contestArea = contestArea(mvArea(10L, 3));
		stub_findAreaLevelById(areaLevel(3));
        stub_findContestAreasForContest(Collections.singletonList(contestArea(mvArea(11L, 3))));
        stub_findContestAreaForElectionAndMvArea(Collections.singletonList(contestArea));

		try {
			ejb.create(createMock(UserData.class), contestArea);
			fail("EvoteException not thrown");
		} catch (EvoteException e) {
			assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ERROR_CODE_0451_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_ELECTION);
			assertThat(e.getParams()).isEqualTo(new String[] { "@area_level[3].name", "MunicipalityName", "ElectionName" });
		}
	}

	@Test
	public void create_withValidContestArea_createsContestArea() throws Exception {
		ContestAreaServiceEjb ejb = initializeMocks(ContestAreaServiceEjb.class);
		ContestArea contestArea = contestArea(mvArea(10L, 3));
		stub_findAreaLevelById(areaLevel(3));
        stub_findContestAreasForContest(Collections.singletonList(contestArea(mvArea(11L, 3))));
        stub_findContestAreaForElectionAndMvArea(new ArrayList<>());

		ejb.create(createMock(UserData.class), contestArea);

		verify(getInjectMock(ContestAreaRepository.class)).create(any(UserData.class), eq(contestArea));

	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected <ELECTION> election path, but got <ELECTION_EVENT>")
	public void findContestAreasForElectionPath_withInvalidLevel_throwsException() throws Exception {
		ContestAreaServiceEjb ejb = initializeMocks(ContestAreaServiceEjb.class);
		ElectionPath electionPath = ElectionPath.from("111111");
        when(getInjectMock(ContestAreaRepository.class).findByElection(any(Election.class))).thenReturn(Collections.singletonList(createMock(ContestArea.class)));

		List<ContestArea> result = ejb.findContestAreasForElectionPath(createMock(UserData.class), electionPath);

		verify(getInjectMock(MvElectionRepository.class)).finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		assertThat(result).hasSize(1);
	}

	@Test
	public void findContestAreasForElectionPath_withValidLevel_returnsContestAreas() throws Exception {
		ContestAreaServiceEjb ejb = initializeMocks(ContestAreaServiceEjb.class);
		ElectionPath electionPath = ElectionPath.from("111111.11.11");

		ejb.findContestAreasForElectionPath(createMock(UserData.class), electionPath);
	}

	private void stub_findAreaLevelById(AreaLevel areaLevel) {
		when(getInjectMock(MvAreaRepository.class).findAreaLevelById(anyString())).thenReturn(areaLevel);
	}

	private void stub_findContestAreasForContest(List<ContestArea> contestAreas) {
		when(getInjectMock(ContestAreaRepository.class).findContestAreasForContest(anyLong())).thenReturn(contestAreas);
	}

	private void stub_findContestAreaForElectionAndMvArea(List<ContestArea> contestAreas) {
		when(getInjectMock(ContestAreaRepository.class).findContestAreaForElectionAndMvArea(anyLong(), anyLong())).thenReturn(contestAreas);
	}

	private AreaLevel areaLevel(int level) {
		AreaLevel result = new AreaLevel();
		result.setName("@area_level[" + level + "].name");
		return result;
	}

	private MvArea mvArea(long pk, int level) {
		MvArea result = new MvArea();
		result.setPk(pk);
		result.setAreaLevel(level);
		result.setMunicipalityName("MunicipalityName");
		return result;
	}

	private ContestArea contestArea(MvArea mvArea) {
		ContestArea result = createMock(ContestArea.class);
		when(result.getMvArea()).thenReturn(mvArea);
		when(result.getContest().getName()).thenReturn("ContestName");
		when(result.getContest().getElection().getName()).thenReturn("ElectionName");
		return result;
	}
}

