package no.valg.eva.admin.counting.domain.service.contestinfo;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.repository.ContestInfoRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContestInfoDomainServiceTest extends MockUtilsTestCase {

	private ContestInfoDomainService service;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(ContestInfoDomainService.class);
	}

	@Test
    public void contestsByAreaPath_givenElectionPathOnElectionEvent_returnsContestInfoListForNormalElections() {
		List<ContestInfo> contestInfoList = new ArrayList<>();
		MvArea mvArea = new MvArea();
		mvArea.setPk(1L);
		ElectionPath electionPath = new ElectionPath("111111");
		when(getInjectMock(ContestInfoRepository.class).contestsForArea(1L)).thenReturn(contestInfoList);

		service.contestsByAreaAndElectionPath(mvArea, electionPath, null);

		verify(getInjectMock(ContestInfoRepository.class), times(1)).contestsForArea(anyLong());
	}

	@Test
    public void contestsByAreaPath_givenElectionPathNotOnElectionEvent_returnsContestInfoListForSamiElection() {
		MvArea mvArea = new MvArea();
		mvArea.setPk(1L);
		ValgdistriktSti valgdistriktSti = new ValgdistriktSti("111111", "01", "01", "000001");
		ContestInfo contestInfo = new ContestInfo(valgdistriktSti.electionPath(), "", "", null);
		Contest contest = new Contest();
		MvElection mvElection = new MvElection();
		mvElection.setContest(contest);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(valgdistriktSti)).thenReturn(mvElection);
		when(getInjectMock(ContestInfoRepository.class).contestForSamiElection(contest)).thenReturn(contestInfo);

		service.contestsByAreaAndElectionPath(mvArea, valgdistriktSti.electionPath(), null);

		verify(getInjectMock(ContestInfoRepository.class), times(1)).contestForSamiElection(contest);
	}
}
