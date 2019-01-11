package no.valg.eva.admin.counting.domain.service.votecount;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.common.mockups.MvElectionMockups;
import no.valg.eva.admin.common.mockups.ReportingUnitMockups;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.repository.ElectionDayRepository;
import no.valg.eva.admin.counting.repository.ManualContestVotingRepository;
import no.valg.eva.admin.counting.repository.VotingRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class FindProtocolCountServiceTest extends MockUtilsTestCase {

	private FindProtocolCountService service;

	private MvElection contestMvElection;
	private AreaPath operatorAreaPath;
	private CountContext context;
	private MvArea countingMvArea;
	private VoteCount voteCount;
	private java.util.Collection<PollingDistrict> pollingDistricts = new ArrayList<>();
	private Municipality municipality;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(FindProtocolCountService.class);

		operatorAreaPath = new AreaPath(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);
		ElectionPath contestPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);
		context = new CountContext(contestPath, CountCategory.VO);
		municipality = MunicipalityMockups.municipality(true);
		countingMvArea = MvAreaMockups.pollingDistrictMvArea(municipality);
		pollingDistricts.add(countingMvArea.getPollingDistrict());
		contestMvElection = MvElectionMockups.contestMvElection();
		ReportingUnit reportingUnit = ReportingUnitMockups.reportingUnit(countingMvArea);

		voteCount = makeVoteCount();

		Mockito.reset(getInjectMock(VoteCountService.class));
		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(any(AreaPath.class), eq(ReportingUnitTypeId.STEMMESTYRET))).thenReturn(
				reportingUnit);
	}

	private VoteCount makeVoteCount() {
		VoteCount voteCount = new VoteCount();
		voteCount.setId("PVO1");
		voteCount.setVoteCountStatus(new VoteCountStatus(SAVED.getId()));
		voteCount.setVoteCountCategory(voteCountCategory());
		voteCount.setApprovedBallots(1);
		voteCount.setRejectedBallots(1);
		voteCount.setManualCount(true);
		voteCount.setBallotCountSet(ballotCounts());
		return voteCount;
	}

	@Test
	public void testNoPollingDistrictsThusNoCounts() {
		assertThat(service.findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection)).isEmpty();
	}

	@Test
	public void testNoExistingCounts() {
		stub_findVoteCount(null);
		MvElection fakeContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		Collection<PollingDistrict> districts = fakePollingDistricts();
		when(getInjectMock(VoteCountService.class).pollingDistrictsForProtocolCount(context, countingMvArea, fakeContestMvElection, operatorAreaPath)).thenReturn(
				districts);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);

		assertThat(service.findProtocolCounts(operatorAreaPath, context, countingMvArea, fakeContestMvElection).get(0).getStatus()).isEqualTo(CountStatus.NEW);
	}

	private Collection<PollingDistrict> fakePollingDistricts() {
		Collection<PollingDistrict> districts = new ArrayList<>();
		districts.add(fakePollingDistrict());
		return districts;
	}

	private PollingDistrict fakePollingDistrict() {
		PollingDistrict fakePollingDistrict = mock(PollingDistrict.class);
		when(fakePollingDistrict.getId()).thenReturn("0101");
		return fakePollingDistrict;
	}

	@Test
	public void testNoExistingCountsNoXiM() {
		municipality.setElectronicMarkoffs(false);
		stub_pollingDistrictsForProtocolCount(pollingDistricts, countingMvArea);
		stub_findVoteCount(null);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
		MvElection fakeContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		assertThat(service.findProtocolCounts(operatorAreaPath, context, countingMvArea, fakeContestMvElection).get(0).getStatus()).isEqualTo(CountStatus.NEW);
	}

	@Test
	public void testOneExistingCount() {
		stub_pollingDistrictsForProtocolCount(pollingDistricts, countingMvArea);
		stub_findVoteCount(voteCount);
		MvElection fakeContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
		
		assertThat(service.findProtocolCounts(operatorAreaPath, context, countingMvArea, fakeContestMvElection).get(0).getStatus()).isEqualTo(CountStatus.SAVED);
	}

	@Test
	public void findProtocolCounts_testOneExistingCountNoXiM_countIsReturned() {
		Municipality municipality = MunicipalityMockups.municipality(false);
		MvArea countingMvArea = MvAreaMockups.pollingDistrictMvArea(municipality);
		List<PollingDistrict> pollingDistricts = new ArrayList<>();
		pollingDistricts.add(countingMvArea.getPollingDistrict());
		stub_pollingDistrictsForProtocolCount(pollingDistricts, countingMvArea);
		stub_findVoteCount(makeVoteCount());
		MvElection fakeContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
        when(getInjectMock(ManualContestVotingRepository.class).findForVoByContestAndArea(anyLong(), anyLong())).thenReturn(Collections.emptyList());

		List<ProtocolCount> protocolCounts = service.findProtocolCounts(operatorAreaPath, context, countingMvArea, fakeContestMvElection);
		
		assertThat(protocolCounts.get(0).getStatus()).isEqualTo(CountStatus.SAVED);
		verify(getInjectMock(ElectionDayRepository.class)).findForPollingDistrict(anyLong());
	}

	@Test
    public void findProtocolCounts_withBoroughContest_verifyProtocolCount() {
		stub_pollingDistrictsForProtocolCount(pollingDistricts, countingMvArea);
		stub_findVoteCount(voteCount);
		stub_findApprovedVotingsByPollingDistrictAndCategories(Arrays.asList(voting(borough(1L)), voting(borough(2L))));
		ContestArea contestArea = createMock(ContestArea.class);
		when(contestArea.getMvArea().getActualAreaLevel()).thenReturn(AreaLevelEnum.BOROUGH);
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(countingMvArea);
        contestMvElection.getContest().setContestAreaSet(new HashSet<>(Collections.singletonList(contestArea)));
		countingMvArea.setBorough(borough(1L));
		
		List<ProtocolCount> counts = service.findProtocolCounts(operatorAreaPath, context, countingMvArea, contestMvElection);

		assertThat(counts).hasSize(2);
		ProtocolCount count = counts.get(0);
		assertThat(count.getDailyMarkOffCounts().getMarkOffCount()).isEqualTo(1);
		assertThat(count.getDailyMarkOffCountsForOtherContests().getMarkOffCount()).isEqualTo(1);
	}

	@Test
    public void findProtocolCounts_withSeveralProtocolCounts_returnsSortedCountsOnAreaPath() {
		List<PollingDistrict> districts = Arrays.asList(getPollingDistrict("1003"), getPollingDistrict("1001"), getPollingDistrict("1002"));
		stub_findVoteCount(voteCount);
		stub_mvAreaRepository_findSingleByPath("1001");
		stub_mvAreaRepository_findSingleByPath("1002");
		stub_mvAreaRepository_findSingleByPath("1003");
		MvElection fakeContestMvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(fakeContestMvElection.getContest().isOnBoroughLevel()).thenReturn(false);
		when(getInjectMock(VoteCountService.class).pollingDistrictsForProtocolCount(context, countingMvArea, fakeContestMvElection, operatorAreaPath)).thenReturn(
				districts);

		List<ProtocolCount> counts = service.findProtocolCounts(operatorAreaPath, context, countingMvArea, fakeContestMvElection);

		assertThat(counts).hasSize(3);
		assertThat(counts.get(0).getAreaPath().getPollingDistrictId()).isEqualTo("1001");
		assertThat(counts.get(1).getAreaPath().getPollingDistrictId()).isEqualTo("1002");
		assertThat(counts.get(2).getAreaPath().getPollingDistrictId()).isEqualTo("1003");
	}

	private PollingDistrict getPollingDistrict(String id) {
		PollingDistrict result = new PollingDistrict();
		result.setId(id);
		return result;
	}

	private Set<BallotCount> ballotCounts() {
		Set<BallotCount> bc = new HashSet<>();
		bc.add(ballotCount(EvoteConstants.BALLOT_BLANK));
		return bc;
	}

	private BallotCount ballotCount(String id) {
		BallotCount bc = new BallotCount();
		Ballot ballot = new Ballot();
		ballot.setId(id);
		bc.setBallot(ballot);
		return bc;
	}

	private VoteCountCategory voteCountCategory() {
		VoteCountCategory vcc = new VoteCountCategory();
		vcc.setId(CountCategory.VO.getId());
		return vcc;
	}

	private void stub_findApprovedVotingsByPollingDistrictAndCategories(List<Voting> votings) {
		when(getInjectMock(VotingRepository.class).findApprovedVotingsByPollingDistrictAndCategories(any(PollingDistrict.class), any(VotingCategory[].class)))
				.thenReturn(votings);
	}

	private void stub_pollingDistrictsForProtocolCount(Collection<PollingDistrict> list, MvArea countingMvArea) {
		when(getInjectMock(VoteCountService.class).pollingDistrictsForProtocolCount(eq(context), eq(countingMvArea), any(MvElection.class),
				eq(operatorAreaPath))).thenReturn(list);
	}

	private void stub_findVoteCount(VoteCount vc) {
		when(getInjectMock(VoteCountService.class).findVoteCount(any(ReportingUnit.class), any(CountContext.class), any(MvArea.class),
				any(MvElection.class), any(CountQualifier.class))).thenReturn(vc);
	}
	
	private void stub_mvAreaRepository_findSingleByPath(String pollingDistrictId) {
		MvArea mvArea = MvAreaMockups.pollingDistrictMvArea(municipality);
		AreaPath areaPath = AreaPath.from(mvArea.getAreaPath()).toBoroughPath();
		areaPath = areaPath.toPollingDistrictSubPath(pollingDistrictId);
		mvArea.setAreaPath(areaPath.path());
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(eq(AreaPath.from(mvArea.getAreaPath())))).thenReturn(mvArea);
	}

	private Voting voting(Borough borough) {
		Voting voting = new Voting();
		MvArea mvArea = new MvArea();
		mvArea.setBorough(borough);
		voting.setMvArea(mvArea);
		voting.setCastTimestamp(new DateTime());
		return voting;
	}

	private Borough borough(Long pk) {
		Borough borough = new Borough();
		borough.setPk(pk);
		return borough;
	}

}

