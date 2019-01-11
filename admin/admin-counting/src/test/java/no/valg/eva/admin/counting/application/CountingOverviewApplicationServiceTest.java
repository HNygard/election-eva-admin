package no.valg.eva.admin.counting.application;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.service.ContestAreaDomainService;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.service.countingoverview.CountingOverviewDomainService;
import no.valg.eva.admin.test.BaseTakeTimeTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class CountingOverviewApplicationServiceTest extends BaseTakeTimeTest {

	@DataProvider
	public static Object[][] electionsForTestData() {
		return new Object[][] {
				new Object[] { true },
				new Object[] { false }
		};
	}

	@DataProvider
	public static Object[][] countingOverviewForTestData() {
		return new Object[][] {
				new Object[] { true },
				new Object[] { false }
		};
	}

	@Test
	public void electionsFor_givenOpptellingsvalgstyret_returnsContestInfoList() throws Exception {
		CountingOverviewApplicationService service = initializeMocks(CountingOverviewApplicationService.class);
		UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);
		MvElection mvElection = mock(MvElection.class);
		long operatorMvElectionPk = 1;
		String electionPath = "111111.11.11.111111";
		String electionName = "electionName";
		String contestName = "contestName";

		when(userData.isOpptellingsvalgstyret()).thenReturn(true);
		when(userData.getOperatorMvElection().getPk()).thenReturn(operatorMvElectionPk);
		when(mvElection.getElectionPath()).thenReturn(electionPath);
		when(mvElection.getElectionName()).thenReturn(electionName);
		when(mvElection.getContestName()).thenReturn(contestName);
		when(getInjectMock(MvElectionRepository.class).findByPk(operatorMvElectionPk)).thenReturn(mvElection);

		assertThat(service.electionsFor(userData, null)).containsExactly(new ContestInfo(electionPath, electionName, contestName, null));
	}

	@Test(dataProvider = "electionsForTestData")
	public void electionsFor_givenFylkesvalgstyret_returnContestInfoList(boolean electionEventAdmin) throws Exception {
		MvElectionRepository mvElectionRepository = mock(MvElectionRepository.class);
		CountingOverviewApplicationService countingOverviewApplicationService = new CountingOverviewApplicationService(
				mock(CountingOverviewDomainService.class), mvElectionRepository, mock(MvAreaRepository.class), mock(ContestAreaDomainService.class));
		UserData userData = mock(UserData.class);
		AreaPath countyPath = AreaPath.from("111111.11.11");
		ElectionPath operatorElectionPath = ElectionPath.from("111111");
		MvElection mvElection = mock(MvElection.class);

		if (!electionEventAdmin) {
			when(userData.isFylkesvalgstyret()).thenReturn(true);
			when(userData.getOperatorAreaPath()).thenReturn(countyPath);
		}
		when(userData.isElectionEventAdminUser()).thenReturn(electionEventAdmin);
		when(userData.getOperatorElectionPath()).thenReturn(operatorElectionPath);
		when(mvElection.getElectionPath()).thenReturn("111111.11.11.111111");
		when(mvElection.getElectionName()).thenReturn("electionName");
		when(mvElection.getContestName()).thenReturn("contestName");
		when(mvElection.getAreaLevel()).thenReturn(COUNTY.getLevel());
		when(mvElectionRepository.findContestsForElectionAndArea(operatorElectionPath, countyPath)).thenReturn(singletonList(mvElection));

		AreaPath areaPath = electionEventAdmin ? countyPath : null;
		List<ContestInfo> contestInfos = countingOverviewApplicationService.electionsFor(userData, areaPath);

		ContestInfo contestInfo = new ContestInfo("111111.11.11.111111", "electionName", "contestName", null);
		assertThat(contestInfos).containsExactly(contestInfo);
	}

	@Test(dataProvider = "electionsForTestData")
	public void electionsFor_givenValgstyret_returnContestInfoList(boolean electionEventAdmin) throws Exception {
		MvElectionRepository mvElectionRepository = mock(MvElectionRepository.class);
		CountingOverviewApplicationService countingOverviewApplicationService = new CountingOverviewApplicationService(
				mock(CountingOverviewDomainService.class), mvElectionRepository, mock(MvAreaRepository.class), mock(ContestAreaDomainService.class));
		UserData userData = mock(UserData.class);
		AreaPath municipalityPath = AreaPath.from("111111.11.11.1111");
		ElectionPath operatorElectionPath = ElectionPath.from("111111");
		MvElection mvElection1 = mock(MvElection.class);
		MvElection mvElection2 = mock(MvElection.class);

		if (!electionEventAdmin) {
			when(userData.isValgstyret()).thenReturn(true);
			when(userData.getOperatorAreaPath()).thenReturn(municipalityPath);
		}
		when(userData.isElectionEventAdminUser()).thenReturn(electionEventAdmin);
		when(userData.getOperatorElectionPath()).thenReturn(operatorElectionPath);
		when(mvElection1.getElectionPath()).thenReturn("111111.11.11.111111");
		when(mvElection1.getElectionName()).thenReturn("electionName1");
		when(mvElection1.getContestName()).thenReturn("contestName1");
		when(mvElection1.getAreaLevel()).thenReturn(MUNICIPALITY.getLevel());
		when(mvElection2.getElectionPath()).thenReturn("111111.11.22.222222");
		when(mvElection2.getElectionName()).thenReturn("electionName2");
		when(mvElection2.getContestName()).thenReturn("contestName2");
		when(mvElection2.getAreaLevel()).thenReturn(COUNTY.getLevel());
		when(mvElectionRepository.findContestsForElectionAndArea(operatorElectionPath, municipalityPath)).thenReturn(asList(mvElection1, mvElection2));

		AreaPath areaPath = electionEventAdmin ? municipalityPath : null;
		List<ContestInfo> contestInfos = countingOverviewApplicationService.electionsFor(userData, areaPath);

		ContestInfo contestInfo1 = new ContestInfo("111111.11.11.111111", "electionName1", "contestName1", null);
		ContestInfo contestInfo2 = new ContestInfo("111111.11.22.222222", "electionName2", "contestName2", null);
		assertThat(contestInfos).containsExactly(contestInfo1, contestInfo2);
	}

	@Test(dataProvider = "electionsForTestData")
	public void electionsFor_givenValgstyretAndIncludesBoroughContest_returnContestInfoList(boolean electionEventAdmin) throws Exception {
		MvElectionRepository mvElectionRepository = mock(MvElectionRepository.class);
		CountingOverviewApplicationService countingOverviewApplicationService = new CountingOverviewApplicationService(
				mock(CountingOverviewDomainService.class), mvElectionRepository, mock(MvAreaRepository.class), mock(ContestAreaDomainService.class));
		UserData userData = mock(UserData.class);
		AreaPath municipalityPath = AreaPath.from("111111.11.11.1111");
		ElectionPath operatorElectionPath = ElectionPath.from("111111");
		MvElection mvElection1 = mock(MvElection.class);
		MvElection mvElection2 = mock(MvElection.class);
		MvElection mvElection3 = mock(MvElection.class);

		if (!electionEventAdmin) {
			when(userData.isValgstyret()).thenReturn(true);
			when(userData.getOperatorAreaPath()).thenReturn(municipalityPath);
		}
		when(userData.isElectionEventAdminUser()).thenReturn(electionEventAdmin);
		when(userData.getOperatorElectionPath()).thenReturn(operatorElectionPath);
		when(mvElection1.getElectionPath()).thenReturn("111111.11.11.111111");
		when(mvElection1.getElectionName()).thenReturn("electionName1");
		when(mvElection1.getContestName()).thenReturn("contestName1");
		when(mvElection1.getAreaLevel()).thenReturn(MUNICIPALITY.getLevel());
		when(mvElection2.getElectionPath()).thenReturn("111111.11.22.222222");
		when(mvElection2.getElectionName()).thenReturn("electionName2");
		when(mvElection2.getContestName()).thenReturn("contestName2");
		when(mvElection2.getAreaLevel()).thenReturn(BOROUGH.getLevel());
		when(mvElection2.getActualAreaLevel()).thenReturn(BOROUGH);
		when(mvElection3.getElectionPath()).thenReturn("111111.11.22.333333");
		when(mvElection3.getElectionName()).thenReturn("electionName2");
		when(mvElection3.getContestName()).thenReturn("contestName3");
		when(mvElection3.getAreaLevel()).thenReturn(BOROUGH.getLevel());
		when(mvElection3.getActualAreaLevel()).thenReturn(BOROUGH);
		when(mvElectionRepository.findContestsForElectionAndArea(operatorElectionPath, municipalityPath))
				.thenReturn(asList(mvElection1, mvElection2, mvElection3));

		AreaPath areaPath = electionEventAdmin ? municipalityPath : null;
		List<ContestInfo> contestInfos = countingOverviewApplicationService.electionsFor(userData, areaPath);

		ContestInfo contestInfo1 = new ContestInfo("111111.11.11.111111", "electionName1", "contestName1", null);
		ContestInfo contestInfo2 = new ContestInfo("111111.11.22", "electionName2", null, null);
		assertThat(contestInfos).containsExactly(contestInfo1, contestInfo2);
	}

	@Test
	public void countingOverviewsFor_givenOpptellingsvalgstyretAndContestAreasIncludingChildArea_returnsCountingOverviewRoots() throws Exception {
		String contestPathString = "111111.11.11.111111";
		ElectionPath contestPath = ElectionPath.from(contestPathString);
		UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);
		when(userData.isOpptellingsvalgstyret()).thenReturn(true);
		when(userData.getOperatorMvElection().getElectionPath()).thenReturn(contestPathString);
		ContestAreaDomainService contestAreaDomainService = mock(ContestAreaDomainService.class);
		ContestArea parentContestArea = parentContestArea("111111.11.11.1111");
		ContestArea childContestArea = childContestArea("111111.11.11.1112");
		ContestArea contestArea1 = contestArea("111111.11.11.1113");
		ContestArea contestArea2 = contestArea("111111.11.11.1114");
		when(contestAreaDomainService.contestAreasFor(contestPath)).thenReturn(asList(contestArea2, childContestArea, parentContestArea, contestArea1));
		CountingOverviewDomainService countingOverviewDomainService = mock(CountingOverviewDomainService.class);
		CountingOverviewRoot countingOverviewRoot1 = mock(CountingOverviewRoot.class);
		CountingOverviewRoot countingOverviewRoot2 = mock(CountingOverviewRoot.class);
		CountingOverviewRoot countingOverviewRoot3 = mock(CountingOverviewRoot.class);
		when(countingOverviewDomainService.countingOverviewForOpptellingsvalgstyret(parentContestArea)).thenReturn(countingOverviewRoot1);
		when(countingOverviewDomainService.countingOverviewForOpptellingsvalgstyret(contestArea1)).thenReturn(countingOverviewRoot2);
		when(countingOverviewDomainService.countingOverviewForOpptellingsvalgstyret(contestArea2)).thenReturn(countingOverviewRoot3);
		CountingOverviewApplicationService countingOverviewApplicationService = new CountingOverviewApplicationService(countingOverviewDomainService,
				mock(MvElectionRepository.class), mock(MvAreaRepository.class), contestAreaDomainService);

		List<CountingOverviewRoot> countingOverviewRoots = countingOverviewApplicationService.countingOverviewsFor(userData, null, null);

		assertThat(countingOverviewRoots).containsExactly(countingOverviewRoot1, countingOverviewRoot2, countingOverviewRoot3);
	}

	@Test
	public void countingOverviewsFor_givenOpptellingsvalgstyretAndContestAreasExcludingChildArea_returnsCountingOverviewRoots() throws Exception {
		String contestPathString = "111111.11.11.111111";
		ElectionPath contestPath = ElectionPath.from(contestPathString);
		UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);
		when(userData.isOpptellingsvalgstyret()).thenReturn(true);
		when(userData.getOperatorMvElection().getElectionPath()).thenReturn(contestPathString);
		ContestAreaDomainService contestAreaDomainService = mock(ContestAreaDomainService.class);
		ContestArea parentContestArea = parentContestArea("111111.11.11.1111");
		ContestArea contestArea1 = contestArea("111111.11.11.1112");
		ContestArea contestArea2 = contestArea("111111.11.11.1113");
		when(contestAreaDomainService.contestAreasFor(contestPath)).thenReturn(asList(contestArea1, parentContestArea, contestArea2));
		CountingOverviewDomainService countingOverviewDomainService = mock(CountingOverviewDomainService.class);
		CountingOverviewRoot countingOverviewRoot1 = mock(CountingOverviewRoot.class);
		CountingOverviewRoot countingOverviewRoot2 = mock(CountingOverviewRoot.class);
		when(countingOverviewDomainService.countingOverviewForOpptellingsvalgstyret(contestArea1)).thenReturn(countingOverviewRoot1);
		when(countingOverviewDomainService.countingOverviewForOpptellingsvalgstyret(contestArea2)).thenReturn(countingOverviewRoot2);
		CountingOverviewApplicationService countingOverviewApplicationService = new CountingOverviewApplicationService(countingOverviewDomainService,
				mock(MvElectionRepository.class), mock(MvAreaRepository.class), contestAreaDomainService);
		
		List<CountingOverviewRoot> countingOverviewRoots = countingOverviewApplicationService.countingOverviewsFor(userData, null, null);
		
		assertThat(countingOverviewRoots).containsExactly(countingOverviewRoot1, countingOverviewRoot2);
	}

	private ContestArea parentContestArea(String areaPath) {
		return contestArea(areaPath, true, false);
	}

	private ContestArea childContestArea(String areaPath) {
		return contestArea(areaPath, false, true);
	}

	private ContestArea contestArea(String areaPath) {
		return contestArea(areaPath, false, false);
	}

	private ContestArea contestArea(String areaPath, boolean parentArea, boolean childArea) {
		ContestArea contestArea = mock(ContestArea.class);
		when(contestArea.getAreaPath()).thenReturn(AreaPath.from(areaPath));
		when(contestArea.isChildArea()).thenReturn(childArea);
		when(contestArea.isParentArea()).thenReturn(parentArea);
		return contestArea;
	}

	@Test(dataProvider = "countingOverviewForTestData")
	public void countingOverviewsFor_givenFylkesvalgstyret_returnsCountingOverviewRoots(boolean electionEventAdmin) throws Exception {
		CountingOverviewDomainService countingOverviewDomainService = mock(CountingOverviewDomainService.class);
		MvElectionRepository mvElectionRepository = mock(MvElectionRepository.class);
		MvAreaRepository mvAreaRepository = mock(MvAreaRepository.class);
		CountingOverviewApplicationService countingOverviewApplicationService = new CountingOverviewApplicationService(
				countingOverviewDomainService, mvElectionRepository, mvAreaRepository, mock(ContestAreaDomainService.class));
		UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);
		AreaPath countyPath = AreaPath.from("111111.11.11");
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		MvElection mvElection = mock(MvElection.class);
		Contest contest = mock(Contest.class);
		MvArea mvArea1 = mock(MvArea.class);
		MvArea mvArea2 = mock(MvArea.class);
		MvArea mvArea3 = mock(MvArea.class);
		CountingOverviewRoot countingOverviewRoot1 = mock(CountingOverviewRoot.class);
		CountingOverviewRoot countingOverviewRoot2 = mock(CountingOverviewRoot.class);
		CountingOverviewRoot countingOverviewRoot3 = mock(CountingOverviewRoot.class);

		if (!electionEventAdmin) {
			when(userData.isFylkesvalgstyret()).thenReturn(true);
			when(userData.getOperatorAreaPath()).thenReturn(countyPath);
		}
		when(userData.isElectionEventAdminUser()).thenReturn(electionEventAdmin);
		when(mvElection.getContest()).thenReturn(contest);
		when(mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(mvElection);
		when(mvAreaRepository.findByPathAndChildLevel(countyPath)).thenReturn(asList(mvArea1, mvArea2, mvArea3));
		when(countingOverviewDomainService.countingOverviewForFylkesvalgstyret(contest, mvArea1)).thenReturn(countingOverviewRoot1);
		when(countingOverviewDomainService.countingOverviewForFylkesvalgstyret(contest, mvArea2)).thenReturn(countingOverviewRoot2);
		when(countingOverviewDomainService.countingOverviewForFylkesvalgstyret(contest, mvArea3)).thenReturn(countingOverviewRoot3);

		AreaPath areaPath = electionEventAdmin ? countyPath : null;
		List<CountingOverviewRoot> countingOverviewRoots = countingOverviewApplicationService.countingOverviewsFor(userData, contestPath, areaPath);

		assertThat(countingOverviewRoots).containsExactly(countingOverviewRoot1, countingOverviewRoot2, countingOverviewRoot3);
	}

	@Test(dataProvider = "countingOverviewForTestData")
	public void countingOverviewsFor_givenValgstyret_returnsCountingOverviewRoots(boolean electionEventAdmin) throws Exception {
		CountingOverviewDomainService countingOverviewDomainService = mock(CountingOverviewDomainService.class);
		MvElectionRepository mvElectionRepository = mock(MvElectionRepository.class);
		MvAreaRepository mvAreaRepository = mock(MvAreaRepository.class);
		CountingOverviewApplicationService countingOverviewApplicationService = new CountingOverviewApplicationService(
				countingOverviewDomainService, mvElectionRepository, mvAreaRepository, mock(ContestAreaDomainService.class));
		UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);
		AreaPath municipalityPath = AreaPath.from("111111.11.11.1111");
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		MvElection mvElection = mock(MvElection.class);
		Contest contest = mock(Contest.class);
		MvArea mvArea = mock(MvArea.class);
		CountingOverviewRoot countingOverviewRoot = mock(CountingOverviewRoot.class);

		if (!electionEventAdmin) {
			when(userData.isValgstyret()).thenReturn(true);
			when(userData.getOperatorAreaPath()).thenReturn(municipalityPath);
		}
		when(userData.isElectionEventAdminUser()).thenReturn(electionEventAdmin);
		when(mvElection.getContest()).thenReturn(contest);
		when(mvElection.getActualElectionLevel()).thenReturn(CONTEST);
		when(mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti())).thenReturn(mvElection);
		when(mvAreaRepository.findSingleByPath(municipalityPath)).thenReturn(mvArea);
		when(countingOverviewDomainService.countingOverviewForValgstyret(contest, mvArea)).thenReturn(countingOverviewRoot);

		AreaPath areaPath = electionEventAdmin ? municipalityPath : null;
		List<CountingOverviewRoot> countingOverviewRoots = countingOverviewApplicationService.countingOverviewsFor(userData, contestPath, areaPath);

		assertThat(countingOverviewRoots).containsExactly(countingOverviewRoot);
	}

	@Test(dataProvider = "countingOverviewForTestData")
	public void countingOverviewsFor_givenValgstyretAndBoroughElection_returnsCountingOverviewRoots(boolean electionEventAdmin) throws Exception {
		CountingOverviewDomainService countingOverviewDomainService = mock(CountingOverviewDomainService.class);
		MvElectionRepository mvElectionRepository = mock(MvElectionRepository.class);
		MvAreaRepository mvAreaRepository = mock(MvAreaRepository.class);
		CountingOverviewApplicationService countingOverviewApplicationService = new CountingOverviewApplicationService(
				countingOverviewDomainService, mvElectionRepository, mvAreaRepository, mock(ContestAreaDomainService.class));
		UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);
		AreaPath municipalityPath = AreaPath.from("111111.11.11.1111");
		ElectionPath electionPath = ElectionPath.from("111111.11.11");
		MvElection mvElection = mock(MvElection.class);
		Election election = mock(Election.class);
		Contest contest2 = mock(Contest.class);
		Contest contest3 = mock(Contest.class);
		MvArea mvArea1 = mock(MvArea.class);
		MvArea mvArea2 = mock(MvArea.class);
		MvArea mvArea3 = mock(MvArea.class);
		CountingOverviewRoot countingOverviewRoot2 = mock(CountingOverviewRoot.class);
		CountingOverviewRoot countingOverviewRoot3 = mock(CountingOverviewRoot.class);

		if (!electionEventAdmin) {
			when(userData.isValgstyret()).thenReturn(true);
			when(userData.getOperatorAreaPath()).thenReturn(municipalityPath);
		}
		when(userData.isElectionEventAdminUser()).thenReturn(electionEventAdmin);
		when(election.contestRelatedTo(mvArea2)).thenReturn(contest2);
		when(election.contestRelatedTo(mvArea3)).thenReturn(contest3);
		when(mvElection.getElection()).thenReturn(election);
		when(mvElection.getActualAreaLevel()).thenReturn(BOROUGH);
		when(mvElection.getActualElectionLevel()).thenReturn(ELECTION);
		when(mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti())).thenReturn(mvElection);
		when(mvArea1.isNotMunicipalityBorough()).thenReturn(false);
		when(mvArea2.isNotMunicipalityBorough()).thenReturn(true);
		when(mvArea3.isNotMunicipalityBorough()).thenReturn(true);
		when(mvAreaRepository.findByPathAndChildLevel(municipalityPath)).thenReturn(asList(mvArea1, mvArea2, mvArea3));
		when(countingOverviewDomainService.countingOverviewForValgstyret(contest2, mvArea2)).thenReturn(countingOverviewRoot2);
		when(countingOverviewDomainService.countingOverviewForValgstyret(contest3, mvArea3)).thenReturn(countingOverviewRoot3);

		AreaPath areaPath = electionEventAdmin ? municipalityPath : null;
		List<CountingOverviewRoot> countingOverviewRoots = countingOverviewApplicationService.countingOverviewsFor(userData, electionPath, areaPath);

		assertThat(countingOverviewRoots).containsExactly(countingOverviewRoot2, countingOverviewRoot3);
	}
}

