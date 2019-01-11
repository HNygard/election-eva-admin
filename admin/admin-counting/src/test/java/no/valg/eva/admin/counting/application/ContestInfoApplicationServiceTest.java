package no.valg.eva.admin.counting.application;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.service.contestinfo.ContestInfoDomainService;
import no.valg.eva.admin.counting.repository.ContestInfoRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ContestInfoApplicationServiceTest extends MockUtilsTestCase {

	private static final String ELECTION_EU = "200701.02.04.000047";
	private static final String ELECTION_MUNICIPALITY_OSLO = "200701.01.02.000301";
	private static final String ELECTION_BOROUGH = "200701.01.03";
	private static final String ELECTION_BOROUGH_GAMLE_OSLO = "200701.01.03.030101";
	private static final String ELECTION_BOROUGH_SAGENE = "200701.01.03.030103";
	private static final String ELECTION_BOROUGH_GRUNERLOKKA = "200701.01.03.030102";

	private static final ElectionPath ELECTION_BYDEL_GRUNERLOKKA = ElectionPath.from(ELECTION_BOROUGH_GRUNERLOKKA);
	private static final AreaPath AREA_OSLO_COUNTY = AreaPath.from("200701.47.03");
	private static final String ELECTION_EVENT_ID = "150001";
	private static final String ELECTION_GROUP_ID = "01";

	private ContestInfoService service;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(ContestInfoApplicationService.class);
	}

	@Test
    public void contestOrElectionByAreaPath_withOsloArea_returnsValidContestInfos() {
		when(getInjectMock(ContestInfoRepository.class).contestsForArea(anyLong())).thenReturn(getData());

		List<ContestInfo> result = service.contestOrElectionByAreaPath(AREA_OSLO_COUNTY);

		
		assertThat(result.size()).isEqualTo(3);
		
		assertThat(result.get(0).getElectionPath().path()).isEqualTo(ELECTION_MUNICIPALITY_OSLO);
		assertThat(result.get(0).getAreaLevel()).isEqualTo(AreaLevelEnum.MUNICIPALITY);
		assertThat(result.get(1).getElectionPath().path()).isEqualTo(ELECTION_BOROUGH);
		assertThat(result.get(1).getAreaLevel()).isEqualTo(AreaLevelEnum.MUNICIPALITY);
		assertThat(result.get(2).getElectionPath().path()).isEqualTo(ELECTION_EU);
		assertThat(result.get(2).getAreaLevel()).isEqualTo(AreaLevelEnum.COUNTRY);
	}

	@Test
    public void contestPathForElectionAndArea_withElectionAndArea_returnsCorrectElectionPath() {
		ContestInfo contestInfo = new ContestInfo(ELECTION_MUNICIPALITY_OSLO, "Kommunestyrevalg", "Oslo", MvAreaMockups.MV_AREA_PATH_OSLO_MUNICIPALITY);
		when(getInjectMock(ContestInfoRepository.class).contestForElectionAndArea(any(Election.class), any(MvArea.class))).thenReturn(contestInfo);

		ElectionPath electionPath = service.findContestPathByElectionAndArea(mock(UserData.class), ELECTION_BYDEL_GRUNERLOKKA, AREA_OSLO_COUNTY);

		assertThat(electionPath.path()).isEqualTo(ELECTION_MUNICIPALITY_OSLO);
	}

	private List<ContestInfo> getData() {
		List<ContestInfo> result = new ArrayList<>();
		result.add(new ContestInfo(ELECTION_MUNICIPALITY_OSLO, "Kommunestyrevalg", "Oslo", MvAreaMockups.MV_AREA_PATH_OSLO_MUNICIPALITY));
		result.add(new ContestInfo(ELECTION_BOROUGH_GAMLE_OSLO, "Bydelsvalg", "Gamle Oslo", MvAreaMockups.MV_AREA_PATH_BOROUGH));
		result.add(new ContestInfo(ELECTION_BOROUGH_GRUNERLOKKA, "Bydelsvalg", "Grünerløkka", MvAreaMockups.MV_AREA_PATH_BOROUGH));
		result.add(new ContestInfo(ELECTION_BOROUGH_SAGENE, "Bydelsvalg", "Sagene", MvAreaMockups.MV_AREA_PATH_BOROUGH));
		result.add(new ContestInfo(ELECTION_EU, "EU valg", "Medlemskap i EU", MvAreaMockups.MV_AREA_PATH_COUNTRY));
		return result;
	}

	@Test
    public void contestsByAreaPath_givenAreaPath_returnsContestInfoList() {
		AreaPath areaPath = AreaPath.from("111111.11.11.1111");
		MvArea mvArea = new MvArea();
		mvArea.setPk(1L);
        ElectionPath electionPath = new ElectionPath("111111");
        when(getInjectMock(MvAreaRepository.class).findSingleByPath(areaPath)).thenReturn(mvArea);
    
        service.contestsByAreaAndElectionPath(mock(UserData.class), areaPath, electionPath, null);

		verify(getInjectMock(MvAreaRepository.class), times(1)).findSingleByPath(areaPath);
		verify(getInjectMock(ContestInfoDomainService.class), times(1)).contestsByAreaAndElectionPath(mvArea, electionPath, null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
    public void findContestInfoByPath_withElectionOnNonContestLevel_shouldThrowIllegalArgumentException() {
		service.findContestInfoByPath(ElectionPath.from(ELECTION_BOROUGH));
	}

	@Test
    public void findContestInfoByPath_withElectionOnContestLevel_shouldThrowIllegalArgumentException() {
		MvElection mvElectionStub = createMock(MvElection.class);
		ContestArea contestAreaStub = createMock(ContestArea.class);
		ElectionPath electionPath = ElectionPath.from(ELECTION_MUNICIPALITY_OSLO);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(electionPath.tilValghierarkiSti())).thenReturn(mvElectionStub);
		when(mvElectionStub.getElectionPath()).thenReturn(electionPath.path());
		when(mvElectionStub.getContest().getContestAreaSet().iterator().next()).thenReturn(contestAreaStub);
		when(contestAreaStub.getMvArea().getAreaPath()).thenReturn(AREA_OSLO_COUNTY.path());

		ContestInfo result = service.findContestInfoByPath(electionPath);

		assertThat(result).isNotNull();
		assertThat(result.getElectionPath()).isEqualTo(electionPath);
		assertThat(result.getAreaPath()).isEqualTo(AREA_OSLO_COUNTY);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void electionsInElectionEvent_electionPathNotOnElectionEventLevel_illegalArgumentExceptionIsThrown() {
		service.electionsInElectionEvent(mock(UserData.class), ElectionPath.from(ELECTION_EVENT_ID, ELECTION_GROUP_ID));
	}

	@Test
	public void electionsInElectionEvent_returnsContestInfoForElections() {
		MvElection mvElectionStub = createMock(MvElection.class);
		when(mvElectionStub.getElectionEvent().elections()).thenReturn(makeElections());
		ElectionPath electionPath = ElectionPath.from(ELECTION_EVENT_ID);
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(electionPath.tilValghierarkiSti())).thenReturn(mvElectionStub);

		List<ContestInfo> contestInfos = service.electionsInElectionEvent(mock(UserData.class), electionPath);

		assertThat(contestInfos).hasSize(2);
		assertThat(contestInfos.get(0).getAreaPath()).isEqualTo(AreaPath.from(ELECTION_EVENT_ID));
		assertThat(contestInfos.get(0).getContestName()).isEqualTo("");
		assertThat(contestInfos.get(1).getAreaPath()).isEqualTo(AreaPath.from(ELECTION_EVENT_ID));
	}

	private Collection<Election> makeElections() {
		List<Election> elections = new ArrayList<>();
		elections.add(makeElection(1L, "01"));
		elections.add(makeElection(2L, "02"));
		return elections;
	}

	private Election makeElection(Long pk, String id) {
		Election election = new Election();
		election.setPk(pk);
		election.setName("electionName");
		election.setElectionGroup(makeElectionGroup());
		election.setId(id);
		return election;
	}

	private ElectionGroup makeElectionGroup() {
		ElectionGroup electionGroup = new ElectionGroup();
		electionGroup.setId(ELECTION_GROUP_ID);
		electionGroup.setElectionEvent(makeElectionEvent());
		return electionGroup;
	}

	private ElectionEvent makeElectionEvent() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setId(ELECTION_EVENT_ID);
		return electionEvent;
	}
}
