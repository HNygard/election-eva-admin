package no.valg.eva.admin.counting.domain.service.votecount;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.ReportingUnitDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.FYLKESVALGSTYRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class FindCountServiceTest extends MockUtilsTestCase {
	private static final String COUNTY_AREA_PATH_STRING = "111111.11.11";
	private static final AreaPath COUNTY_OPERATOR_AREA_PATH = AreaPath.from(COUNTY_AREA_PATH_STRING);
	private static final String POLLING_DISTRICT_AREA_PATH_STRING_1 = "111111.11.11.1111.111111.1111";
	private static final AreaPath POLLING_DISTRICT_AREA_PATH_1 = AreaPath.from(POLLING_DISTRICT_AREA_PATH_STRING_1);
	private static final String MUNICIPALITY_AREA_PATH_STRING = "111111.11.11.1111";
	private static final AreaPath MUNICIPALITY_OPERATOR_AREA_PATH = AreaPath.from(MUNICIPALITY_AREA_PATH_STRING);
	private static final String PARTY_NAME_1 = "partyName1";
	private static final String PARTY_NAME_2 = "partyName2";
	private static final String PARTY_NAME_3 = "partyName3";
	private static final String REJECTED_BALLOT_1 = "rejectedBallot1";
	private static final String REJECTED_BALLOT_2 = "rejectedBallot2";
	private static final String REJECTED_BALLOT_3 = "rejectedBallot3";
	private static final String REJECTED_BALLOT_4 = "rejectedBallot4";
	private static final String CONTEST_PATH_STRING = "111111.11.11.111111";
	private static final ElectionPath CONTEST_PATH = ElectionPath.from(CONTEST_PATH_STRING);
	private static final CountCategory COUNT_CATEGORY = CountCategory.VO;
	private static final CountContext COUNT_CONTEXT = new CountContext(CONTEST_PATH, COUNT_CATEGORY);
	private static final String VOTE_COUNT_ID_1 = "EVO1";
	private static final String VOTE_COUNT_ID_2 = "EVO2";
	private static final String AREA_NAME = "areaName";
	private static final String REPORTING_UNIT_AREA_NAME = "reportingUnitAreaName";
	private static final String POLLING_DISTRICT_AREA_PATH_STRING_2 = "111111.11.11.1111.111111.2222";
	private static final AreaPath POLLING_DISTRICT_AREA_PATH_2 = AreaPath.from(POLLING_DISTRICT_AREA_PATH_STRING_2);
	private static final String A_CONTEST_PATH = "111111.01.01.000001";

	

	private FindCountService service;

	@Mock
	private MvElection stubMvElection;
	@Mock
	private Contest stubContest;
	@Mock
	private Ballot stubBallot1;
	@Mock
	private Ballot stubBallot2;
	@Mock
	private Ballot stubBallot3;
	@Mock
	private Ballot stubBlankBallot;
	@Mock
	private Affiliation stubAffiliation1;
	@Mock
	private Affiliation stubAffiliation2;
	@Mock
	private Affiliation stubAffiliation3;
	@Mock
	private Affiliation stubBlankAffiliation;
	@Mock
	private Party stubParty1;
	@Mock
	private Party stubParty2;
	@Mock
	private Party stubParty3;
	@Mock
	private BallotRejection stubBallotRejection1;
	@Mock
	private BallotRejection stubBallotRejection2;
	@Mock
	private BallotRejection stubBallotRejection3;
	@Mock
	private BallotRejection stubBallotRejection4;
	@Mock
	private MvArea stubCountingMvArea1;
	@Mock
	private MvArea stubCountingMvArea2;
	@Mock
	private ReportingUnit stubReportingUnit;
	@Mock
	private ContestReport stubContestReport;
	@Mock
	private VoteCount stubVoteCount1;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubBallotCount1;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubBallotCount2;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubBallotCount3;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubBlankBallotCount1;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubRejectedBallotCount1;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubRejectedBallotCount2;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubRejectedBallotCount3;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubRejectedBallotCount4;
	@Mock
	private VoteCount stubVoteCount2;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubBallotCount4;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubBallotCount5;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubBallotCount6;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubBlankBallotCount2;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubRejectedBallotCount5;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubRejectedBallotCount6;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubRejectedBallotCount7;
	@Mock
	private no.valg.eva.admin.counting.domain.model.BallotCount stubRejectedBallotCount8;
	@Mock
	private VoteCountCategory stubVoteCountCategory;

	@BeforeMethod
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		service = initializeMocks(FindCountService.class);

		initContest();
		initBallotRejections();
	}

	private void initContest() {
		when(stubMvElection.getContest()).thenReturn(stubContest);
		when(stubAffiliation1.getBallot()).thenReturn(stubBallot1);
		when(stubAffiliation1.getParty()).thenReturn(stubParty1);
		when(stubAffiliation2.getBallot()).thenReturn(stubBallot2);
		when(stubAffiliation2.getParty()).thenReturn(stubParty2);
		when(stubAffiliation3.getBallot()).thenReturn(stubBallot3);
		when(stubAffiliation3.getParty()).thenReturn(stubParty3);
		when(stubBlankAffiliation.getBallot()).thenReturn(stubBlankBallot);
		when(stubParty1.getName()).thenReturn(PARTY_NAME_1);
		when(stubParty2.getName()).thenReturn(PARTY_NAME_2);
		when(stubParty3.getName()).thenReturn(PARTY_NAME_3);
		when(stubBallot1.getId()).thenReturn("P1");
		when(stubBallot1.getAffiliation()).thenReturn(stubAffiliation1);
		when(stubBallot2.getId()).thenReturn("P2");
		when(stubBallot2.getAffiliation()).thenReturn(stubAffiliation2);
		when(stubBallot3.getId()).thenReturn("P3");
		when(stubBallot3.getAffiliation()).thenReturn(stubAffiliation3);
		when(stubBlankBallot.isBlank()).thenReturn(true);
		when(stubBlankBallot.getId()).thenReturn(EvoteConstants.BALLOT_BLANK);
		when(stubContest.getSortedApprovedBallots()).thenReturn(new LinkedHashSet<>(asList(stubBallot1, stubBallot2, stubBallot3, stubBlankBallot)));
		when(stubContestReport.getContest()).thenReturn(stubContest);
	}

	private void initBallotRejections() {
		List<BallotRejection> ballotRejections = new ArrayList<>();
		ballotRejections.add(stubBallotRejection1);
		ballotRejections.add(stubBallotRejection2);
		ballotRejections.add(stubBallotRejection3);
		ballotRejections.add(stubBallotRejection4);

		when(stubBallotRejection1.getId()).thenReturn("R1");
		when(stubBallotRejection1.getName()).thenReturn(REJECTED_BALLOT_1);
		when(stubBallotRejection2.getId()).thenReturn("R2");
		when(stubBallotRejection2.getName()).thenReturn(REJECTED_BALLOT_2);
		when(stubBallotRejection3.getId()).thenReturn("R3");
		when(stubBallotRejection3.getName()).thenReturn(REJECTED_BALLOT_3);
		when(stubBallotRejection4.getId()).thenReturn("R4");
		when(stubBallotRejection4.getName()).thenReturn(REJECTED_BALLOT_4);
		when(getInjectMock(BallotRejectionRepository.class).findBallotRejectionsByEarlyVoting(false)).thenReturn(ballotRejections);
	}

	@Test
	public void findCountyFinalCounts_givenParameters_returnsFinalCounts() {
		initVoteCount1(VOTE_COUNT_ID_1);
		initVoteCount2(VOTE_COUNT_ID_2);
		initCommonStubs(POLLING_DISTRICT_AREA_PATH_STRING_1, AREA_NAME);

		when(
				getInjectMock(ReportingUnitDomainService.class).areaPathForFindingReportingUnit(FYLKESVALGSTYRET, COUNTY_OPERATOR_AREA_PATH,
						stubCountingMvArea1))
								.thenReturn(COUNTY_OPERATOR_AREA_PATH);
		when(getInjectMock(VoteCountService.class).findFinalVoteCounts(stubReportingUnit, stubMvElection, stubCountingMvArea1, COUNT_CATEGORY))
				.thenReturn(asList(stubVoteCount1, stubVoteCount2));
		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(COUNTY_OPERATOR_AREA_PATH, FYLKESVALGSTYRET))
				.thenReturn(stubReportingUnit);
		when(stubContest.isOnCountyLevel()).thenReturn(true);
		when(stubContest.isSingleArea()).thenReturn(true);

		List<FinalCount> countyFinalCounts = service.findCountyFinalCounts(COUNTY_OPERATOR_AREA_PATH, stubMvElection, stubCountingMvArea1, COUNT_CATEGORY);

		assertThat(countyFinalCounts).containsExactly(buildFinalCount1(VOTE_COUNT_ID_1, POLLING_DISTRICT_AREA_PATH_1, AREA_NAME),
				buildFinalCount2(VOTE_COUNT_ID_2, POLLING_DISTRICT_AREA_PATH_1, AREA_NAME));
	}

	@Test
	public void findCountyFinalCounts_whenNoFinalCounts_returnsNewFinalCount() {
		initCommonStubs(POLLING_DISTRICT_AREA_PATH_STRING_1, AREA_NAME);

		when(getInjectMock(AffiliationRepository.class).findApprovedByContest(anyLong())).thenReturn(
				asList(stubAffiliation1, stubAffiliation2, stubAffiliation3, stubBlankAffiliation));
		when(getInjectMock(ReportingUnitDomainService.class).areaPathForFindingReportingUnit(FYLKESVALGSTYRET, COUNTY_OPERATOR_AREA_PATH,
				stubCountingMvArea1))
						.thenReturn(COUNTY_OPERATOR_AREA_PATH);
		when(getInjectMock(VoteCountService.class).findFinalVoteCounts(stubReportingUnit, stubMvElection, stubCountingMvArea1, COUNT_CATEGORY))
				.thenReturn(Collections.emptyList());
		when(getInjectMock(ReportingUnitRepository.class).byAreaPathElectionPathAndType(any(AreaPath.class), any(ElectionPath.class),
				eq(ReportingUnitTypeId.OPPTELLINGSVALGSTYRET)))
						.thenReturn(stubReportingUnit);
		when(stubContest.isOnCountyLevel()).thenReturn(false);
		when(stubContest.isSingleArea()).thenReturn(false);
		when(stubMvElection.getElectionPath()).thenReturn(A_CONTEST_PATH);

		List<FinalCount> countyFinalCounts = service.findCountyFinalCounts(COUNTY_OPERATOR_AREA_PATH, stubMvElection, stubCountingMvArea1, COUNT_CATEGORY);

		assertThat(countyFinalCounts).containsExactly(buildNewFinalCount(POLLING_DISTRICT_AREA_PATH_1));
	}

	@Test
	public void findMunicipalityFinalCounts_givenParameters_returnsFinalCounts() {
		initVoteCount1(VOTE_COUNT_ID_1);
		initVoteCount2(VOTE_COUNT_ID_2);
		initCommonStubs(POLLING_DISTRICT_AREA_PATH_STRING_1, AREA_NAME);

		when(
				getInjectMock(ReportingUnitDomainService.class).areaPathForFindingReportingUnit(ReportingUnitTypeId.VALGSTYRET, MUNICIPALITY_OPERATOR_AREA_PATH,
						stubCountingMvArea1))
								.thenReturn(MUNICIPALITY_OPERATOR_AREA_PATH);
		when(getInjectMock(VoteCountService.class).findFinalVoteCounts(stubReportingUnit, stubMvElection, stubCountingMvArea1, COUNT_CATEGORY))
				.thenReturn(asList(stubVoteCount1, stubVoteCount2));
		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(MUNICIPALITY_OPERATOR_AREA_PATH, ReportingUnitTypeId.VALGSTYRET))
				.thenReturn(stubReportingUnit);

		List<FinalCount> municipalityFinalCounts = service.findMunicipalityFinalCounts(MUNICIPALITY_OPERATOR_AREA_PATH, stubMvElection, stubCountingMvArea1,
				COUNT_CATEGORY);

		assertThat(municipalityFinalCounts).containsExactly(buildFinalCount1(
				VOTE_COUNT_ID_1, POLLING_DISTRICT_AREA_PATH_1, AREA_NAME), buildFinalCount2(VOTE_COUNT_ID_2, POLLING_DISTRICT_AREA_PATH_1, AREA_NAME));
	}

	@Test
	public void findMunicipalityFinalCounts_whenNoFinalCounts_returnsNewFinalCount() {
		initCommonStubs(POLLING_DISTRICT_AREA_PATH_STRING_1, AREA_NAME);

		when(getInjectMock(AffiliationRepository.class).findApprovedByContest(anyLong())).thenReturn(
				asList(stubAffiliation1, stubAffiliation2, stubAffiliation3, stubBlankAffiliation));
		when(
				getInjectMock(ReportingUnitDomainService.class).areaPathForFindingReportingUnit(ReportingUnitTypeId.VALGSTYRET, MUNICIPALITY_OPERATOR_AREA_PATH,
						stubCountingMvArea1))
								.thenReturn(MUNICIPALITY_OPERATOR_AREA_PATH);
		when(getInjectMock(VoteCountService.class).findFinalVoteCounts(stubReportingUnit, stubMvElection, stubCountingMvArea1, COUNT_CATEGORY))
				.thenReturn(Collections.emptyList());
		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(MUNICIPALITY_OPERATOR_AREA_PATH, ReportingUnitTypeId.VALGSTYRET))
				.thenReturn(stubReportingUnit);

		List<FinalCount> municipalityFinalCounts = service.findMunicipalityFinalCounts(MUNICIPALITY_OPERATOR_AREA_PATH, stubMvElection, stubCountingMvArea1,
				COUNT_CATEGORY);

		assertThat(municipalityFinalCounts).containsExactly(buildNewFinalCount(POLLING_DISTRICT_AREA_PATH_1));
	}

	@Test
	public void findApprovedCountyFinalCount_givenParameters_returnApprovedFinalCount() {
		initVoteCount1(VOTE_COUNT_ID_1);
		initCommonStubs(POLLING_DISTRICT_AREA_PATH_STRING_1, AREA_NAME);

		when(stubVoteCount1.getVoteCountStatusId()).thenReturn(CountStatus.APPROVED.getId());
		when(stubVoteCount1.isModifiedBallotsProcessed()).thenReturn(true);
		when(getInjectMock(ReportingUnitDomainService.class).reportingUnitForCountyFinalCount(COUNTY_OPERATOR_AREA_PATH,
				AreaPath.from(stubCountingMvArea1.getAreaPath()), stubMvElection))
						.thenReturn(stubReportingUnit);
		when(getInjectMock(VoteCountService.class).findApprovedFinalVoteCount(stubReportingUnit, COUNT_CONTEXT, stubCountingMvArea1, stubMvElection))
				.thenReturn(stubVoteCount1);

		FinalCount approvedCountyFinalCount = service.findApprovedCountyFinalCount(COUNTY_OPERATOR_AREA_PATH, COUNT_CONTEXT, stubCountingMvArea1,
				stubMvElection);

		assertThat(approvedCountyFinalCount).isEqualTo(buildApprovedFinalCount(POLLING_DISTRICT_AREA_PATH_1));
	}

	@Test
	public void findApprovedCountyFinalCount_whenNoApprovedFinalCount_returnsNull() {
		initCommonStubs(POLLING_DISTRICT_AREA_PATH_STRING_1, AREA_NAME);

		when(
				getInjectMock(ReportingUnitDomainService.class).areaPathForFindingReportingUnit(FYLKESVALGSTYRET, COUNTY_OPERATOR_AREA_PATH,
						stubCountingMvArea1))
								.thenReturn(COUNTY_OPERATOR_AREA_PATH);
		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(COUNTY_OPERATOR_AREA_PATH, FYLKESVALGSTYRET))
				.thenReturn(stubReportingUnit);
		when(getInjectMock(VoteCountService.class).findApprovedFinalVoteCount(any(ReportingUnit.class), any(CountContext.class),
				any(MvArea.class), any(MvElection.class))).thenReturn(null);

		FinalCount approvedCountyFinalCount = service.findApprovedCountyFinalCount(COUNTY_OPERATOR_AREA_PATH, COUNT_CONTEXT, stubCountingMvArea1,
				stubMvElection);

		assertThat(approvedCountyFinalCount).isNull();
	}

	@Test
	public void findApprovedMunicipalityCountyFinalCount_givenParameters_returnApprovedFinalCount() {
		initVoteCount1(VOTE_COUNT_ID_1);
		initCommonStubs(POLLING_DISTRICT_AREA_PATH_STRING_1, AREA_NAME);

		when(stubVoteCount1.getVoteCountStatusId()).thenReturn(CountStatus.APPROVED.getId());
		when(stubVoteCount1.isModifiedBallotsProcessed()).thenReturn(true);
		when(
				getInjectMock(ReportingUnitDomainService.class).areaPathForFindingReportingUnit(ReportingUnitTypeId.VALGSTYRET, MUNICIPALITY_OPERATOR_AREA_PATH,
						stubCountingMvArea1))
								.thenReturn(MUNICIPALITY_OPERATOR_AREA_PATH);
		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(MUNICIPALITY_OPERATOR_AREA_PATH, ReportingUnitTypeId.VALGSTYRET))
				.thenReturn(stubReportingUnit);
		when(getInjectMock(VoteCountService.class).findApprovedFinalVoteCount(stubReportingUnit, COUNT_CONTEXT, stubCountingMvArea1, stubMvElection))
				.thenReturn(stubVoteCount1);

		FinalCount approvedMunicipalityFinalCount = service.findApprovedMunicipalityFinalCount(MUNICIPALITY_OPERATOR_AREA_PATH, COUNT_CONTEXT,
				stubCountingMvArea1,
				stubMvElection);

		assertThat(approvedMunicipalityFinalCount).isEqualTo(buildApprovedFinalCount(POLLING_DISTRICT_AREA_PATH_1));
	}

	@Test
	public void findApprovedMunicipalityCountyFinalCount_whenNoApprovedFinalCount_returnsNull() {
		initCommonStubs(POLLING_DISTRICT_AREA_PATH_STRING_1, AREA_NAME);

		when(
				getInjectMock(ReportingUnitDomainService.class).areaPathForFindingReportingUnit(ReportingUnitTypeId.VALGSTYRET, MUNICIPALITY_OPERATOR_AREA_PATH,
						stubCountingMvArea1))
								.thenReturn(MUNICIPALITY_OPERATOR_AREA_PATH);
		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(MUNICIPALITY_OPERATOR_AREA_PATH, ReportingUnitTypeId.VALGSTYRET))
				.thenReturn(stubReportingUnit);
		when(getInjectMock(VoteCountService.class).findApprovedFinalVoteCount(any(ReportingUnit.class), any(CountContext.class),
				any(MvArea.class), any(MvElection.class))).thenReturn(null);

		FinalCount approvedMunicipalityFinalCount = service.findApprovedMunicipalityFinalCount(MUNICIPALITY_OPERATOR_AREA_PATH, COUNT_CONTEXT,
				stubCountingMvArea1,
				stubMvElection);

		assertThat(approvedMunicipalityFinalCount).isNull();
	}

	@Test
	public void findMunicipalityCountsByStatus_givenStatusApproved_returnsApprovedMunicipalityFinalCounts() {
		initVoteCount1(VOTE_COUNT_ID_1);
		initVoteCount2(VOTE_COUNT_ID_2);
		initCommonStubs(POLLING_DISTRICT_AREA_PATH_STRING_1, "areaName1");

		when(stubCountingMvArea2.getPath()).thenReturn(POLLING_DISTRICT_AREA_PATH_STRING_2);
		when(stubCountingMvArea2.getAreaPath()).thenReturn(POLLING_DISTRICT_AREA_PATH_STRING_2);
		when(stubCountingMvArea2.getAreaName()).thenReturn("areaName2");
		when(stubVoteCount1.getMvArea()).thenReturn(stubCountingMvArea1);
		when(stubVoteCount2.getMvArea()).thenReturn(stubCountingMvArea2);
		when(getInjectMock(VoteCountService.class).findVoteCountsFor(stubReportingUnit, CONTEST_PATH, CountStatus.APPROVED, CountQualifier.FINAL))
				.thenReturn(asList(stubVoteCount2, stubVoteCount1));
		when(getInjectMock(ReportingUnitRepository.class).findByAreaPathAndType(MUNICIPALITY_OPERATOR_AREA_PATH, ReportingUnitTypeId.VALGSTYRET))
				.thenReturn(stubReportingUnit);

		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(any(ValghierarkiSti.class)).getContest().isContestOrElectionPenultimateRecount())
				.thenReturn(true);

		List<FinalCount> municipalityFinalCounts = (List<FinalCount>) service.findMunicipalityCountsByStatus(MUNICIPALITY_OPERATOR_AREA_PATH, CONTEST_PATH,
				CountStatus.APPROVED);

		assertThat(municipalityFinalCounts)
				.containsExactly(buildFinalCount1(VOTE_COUNT_ID_1, POLLING_DISTRICT_AREA_PATH_1, "areaName1"),
						buildFinalCount2(VOTE_COUNT_ID_2, POLLING_DISTRICT_AREA_PATH_2, "areaName2"));
	}

	private void initVoteCount1(String id) {
		when(stubVoteCount1.getId()).thenReturn(id);
		when(stubVoteCount1.getMvArea()).thenReturn(stubCountingMvArea1);
		when(stubVoteCount1.getVoteCountCategory()).thenReturn(stubVoteCountCategory);
		when(stubVoteCount1.getVoteCountCategoryId()).thenReturn(CountCategory.VO.getId());
		when(stubVoteCount1.getContestReport()).thenReturn(stubContestReport);
		when(stubVoteCount1.isManualCount()).thenReturn(true);
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountMap1 = new HashMap<>();
		ballotCountMap1.put("P1", stubBallotCount1);
		ballotCountMap1.put("P2", stubBallotCount2);
		ballotCountMap1.put("P3", stubBallotCount3);
		ballotCountMap1.put(EvoteConstants.BALLOT_BLANK, stubBlankBallotCount1);
		when(stubVoteCount1.getBallotCountMap()).thenReturn(ballotCountMap1);
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> rejectedBallotCountMap1 = new HashMap<>();
		rejectedBallotCountMap1.put("R1", stubRejectedBallotCount1);
		rejectedBallotCountMap1.put("R2", stubRejectedBallotCount2);
		rejectedBallotCountMap1.put("R3", stubRejectedBallotCount3);
		rejectedBallotCountMap1.put("R4", stubRejectedBallotCount4);
		when(stubVoteCount1.getRejectedBallotCountMap()).thenReturn(rejectedBallotCountMap1);
		when(stubVoteCount1.getInfoText()).thenReturn("comment1");
		when(stubVoteCount1.getPk()).thenReturn(1L);
		when(stubBallotCount1.getBallot()).thenReturn(stubBallot1);
		when(stubBallotCount1.getUnmodifiedBallots()).thenReturn(11);
		when(stubBallotCount1.getModifiedBallots()).thenReturn(21);
		when(stubBallotCount2.getBallot()).thenReturn(stubBallot2);
		when(stubBallotCount2.getUnmodifiedBallots()).thenReturn(12);
		when(stubBallotCount2.getModifiedBallots()).thenReturn(22);
		when(stubBallotCount3.getBallot()).thenReturn(stubBallot3);
		when(stubBallotCount3.getUnmodifiedBallots()).thenReturn(13);
		when(stubBallotCount3.getModifiedBallots()).thenReturn(23);
		when(stubBlankBallotCount1.getBallot()).thenReturn(stubBlankBallot);
		when(stubBlankBallotCount1.getUnmodifiedBallots()).thenReturn(1);
		when(stubRejectedBallotCount1.getUnmodifiedBallots()).thenReturn(31);
		when(stubRejectedBallotCount2.getUnmodifiedBallots()).thenReturn(32);
		when(stubRejectedBallotCount3.getUnmodifiedBallots()).thenReturn(33);
		when(stubRejectedBallotCount4.getUnmodifiedBallots()).thenReturn(34);
	}

	private void initVoteCount2(String id) {
		when(stubVoteCount2.getId()).thenReturn(id);
		when(stubVoteCount2.getMvArea()).thenReturn(stubCountingMvArea1);
		when(stubVoteCount2.getVoteCountCategory()).thenReturn(stubVoteCountCategory);
		when(stubVoteCount2.getVoteCountCategoryId()).thenReturn(CountCategory.VO.getId());
		when(stubVoteCount2.getContestReport()).thenReturn(stubContestReport);
		when(stubVoteCount2.isManualCount()).thenReturn(true);
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountMap2 = new HashMap<>();
		ballotCountMap2.put("P1", stubBallotCount4);
		ballotCountMap2.put("P2", stubBallotCount5);
		ballotCountMap2.put("P3", stubBallotCount6);
		ballotCountMap2.put(EvoteConstants.BALLOT_BLANK, stubBlankBallotCount2);
		when(stubVoteCount2.getBallotCountMap()).thenReturn(ballotCountMap2);
		Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> rejectedBallotCountMap2 = new HashMap<>();
		rejectedBallotCountMap2.put("R1", stubRejectedBallotCount5);
		rejectedBallotCountMap2.put("R2", stubRejectedBallotCount6);
		rejectedBallotCountMap2.put("R3", stubRejectedBallotCount7);
		rejectedBallotCountMap2.put("R4", stubRejectedBallotCount8);
		when(stubVoteCount2.getRejectedBallotCountMap()).thenReturn(rejectedBallotCountMap2);
		when(stubVoteCount2.getInfoText()).thenReturn("comment2");
		when(stubVoteCount2.getPk()).thenReturn(2L);
		when(stubBallotCount4.getBallot()).thenReturn(stubBallot1);
		when(stubBallotCount4.getUnmodifiedBallots()).thenReturn(14);
		when(stubBallotCount4.getModifiedBallots()).thenReturn(24);
		when(stubBallotCount5.getBallot()).thenReturn(stubBallot2);
		when(stubBallotCount5.getUnmodifiedBallots()).thenReturn(15);
		when(stubBallotCount5.getModifiedBallots()).thenReturn(25);
		when(stubBallotCount6.getBallot()).thenReturn(stubBallot3);
		when(stubBallotCount6.getUnmodifiedBallots()).thenReturn(16);
		when(stubBallotCount6.getModifiedBallots()).thenReturn(26);
		when(stubBlankBallotCount2.getBallot()).thenReturn(stubBlankBallot);
		when(stubBlankBallotCount2.getUnmodifiedBallots()).thenReturn(2);
		when(stubRejectedBallotCount5.getUnmodifiedBallots()).thenReturn(35);
		when(stubRejectedBallotCount6.getUnmodifiedBallots()).thenReturn(36);
		when(stubRejectedBallotCount7.getUnmodifiedBallots()).thenReturn(37);
		when(stubRejectedBallotCount8.getUnmodifiedBallots()).thenReturn(38);
	}

	private void initCommonStubs(String countingAreaPathString, String countingAreaName) {
		when(stubReportingUnit.getNameLine()).thenReturn(REPORTING_UNIT_AREA_NAME);
		when(stubCountingMvArea1.getAreaName()).thenReturn(countingAreaName);
		when(stubCountingMvArea1.getPath()).thenReturn(countingAreaPathString);
		when(stubCountingMvArea1.getAreaPath()).thenReturn(countingAreaPathString);
	}

	private FinalCount buildNewFinalCount(AreaPath countingAreaPath) {
		FinalCount countyFinalCount1 = new FinalCount(null, countingAreaPath, CountCategory.VO, AREA_NAME, FYLKESVALGSTYRET, REPORTING_UNIT_AREA_NAME, true);
		countyFinalCount1.setStatus(CountStatus.NEW);
		countyFinalCount1.setComment(null);
		countyFinalCount1.setVoteCountPk(null);
		BallotCount ballotCount1 = new BallotCount("P1", PARTY_NAME_1, 0, 0);
		BallotCount ballotCount2 = new BallotCount("P2", PARTY_NAME_2, 0, 0);
		BallotCount ballotCount3 = new BallotCount("P3", PARTY_NAME_3, 0, 0);
		List<BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(ballotCount1);
		ballotCounts.add(ballotCount2);
		ballotCounts.add(ballotCount3);
		countyFinalCount1.setBallotCounts(ballotCounts);
		countyFinalCount1.setBlankBallotCount(0);
		RejectedBallotCount rejectedBallotCount1 = new RejectedBallotCount("R1", REJECTED_BALLOT_1, 0);
		RejectedBallotCount rejectedBallotCount2 = new RejectedBallotCount("R2", REJECTED_BALLOT_2, 0);
		RejectedBallotCount rejectedBallotCount3 = new RejectedBallotCount("R3", REJECTED_BALLOT_3, 0);
		RejectedBallotCount rejectedBallotCount4 = new RejectedBallotCount("R4", REJECTED_BALLOT_4, 0);
		List<RejectedBallotCount> rejectedBallotCounts = new ArrayList<>();
		rejectedBallotCounts.add(rejectedBallotCount1);
		rejectedBallotCounts.add(rejectedBallotCount2);
		rejectedBallotCounts.add(rejectedBallotCount3);
		rejectedBallotCounts.add(rejectedBallotCount4);
		countyFinalCount1.setRejectedBallotCounts(rejectedBallotCounts);
		return countyFinalCount1;
	}

	private FinalCount buildApprovedFinalCount(AreaPath countingAreaPath) {
		FinalCount countyFinalCount1 = new FinalCount(VOTE_COUNT_ID_1, countingAreaPath, CountCategory.VO, AREA_NAME, FYLKESVALGSTYRET,
				REPORTING_UNIT_AREA_NAME, true);
		countyFinalCount1.setStatus(CountStatus.APPROVED);
		countyFinalCount1.setModifiedBallotsProcessed(true);
		countyFinalCount1.setComment("comment1");
		countyFinalCount1.setVoteCountPk(1L);
		BallotCount ballotCount1 = new BallotCount("P1", PARTY_NAME_1, 11, 21);
		BallotCount ballotCount2 = new BallotCount("P2", PARTY_NAME_2, 12, 22);
		BallotCount ballotCount3 = new BallotCount("P3", PARTY_NAME_3, 13, 23);
		List<BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(ballotCount1);
		ballotCounts.add(ballotCount2);
		ballotCounts.add(ballotCount3);
		countyFinalCount1.setBallotCounts(ballotCounts);
		countyFinalCount1.setBlankBallotCount(1);
		RejectedBallotCount rejectedBallotCount1 = new RejectedBallotCount("R1", REJECTED_BALLOT_1, 31);
		RejectedBallotCount rejectedBallotCount2 = new RejectedBallotCount("R2", REJECTED_BALLOT_2, 32);
		RejectedBallotCount rejectedBallotCount3 = new RejectedBallotCount("R3", REJECTED_BALLOT_3, 33);
		RejectedBallotCount rejectedBallotCount4 = new RejectedBallotCount("R4", REJECTED_BALLOT_4, 34);
		List<RejectedBallotCount> rejectedBallotCounts = new ArrayList<>();
		rejectedBallotCounts.add(rejectedBallotCount1);
		rejectedBallotCounts.add(rejectedBallotCount2);
		rejectedBallotCounts.add(rejectedBallotCount3);
		rejectedBallotCounts.add(rejectedBallotCount4);
		countyFinalCount1.setRejectedBallotCounts(rejectedBallotCounts);
		return countyFinalCount1;
	}

	private FinalCount buildFinalCount1(String id, AreaPath countingAreaPath, String areaName) {
		FinalCount countyFinalCount1 = new FinalCount(id, countingAreaPath, CountCategory.VO, areaName, FYLKESVALGSTYRET, REPORTING_UNIT_AREA_NAME, true);
		countyFinalCount1.setStatus(CountStatus.SAVED);
		countyFinalCount1.setComment("comment1");
		countyFinalCount1.setVoteCountPk(1L);
		BallotCount ballotCount1 = new BallotCount("P1", PARTY_NAME_1, 11, 21);
		BallotCount ballotCount2 = new BallotCount("P2", PARTY_NAME_2, 12, 22);
		BallotCount ballotCount3 = new BallotCount("P3", PARTY_NAME_3, 13, 23);
		List<BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(ballotCount1);
		ballotCounts.add(ballotCount2);
		ballotCounts.add(ballotCount3);
		countyFinalCount1.setBallotCounts(ballotCounts);
		countyFinalCount1.setBlankBallotCount(1);
		RejectedBallotCount rejectedBallotCount1 = new RejectedBallotCount("R1", REJECTED_BALLOT_1, 31);
		RejectedBallotCount rejectedBallotCount2 = new RejectedBallotCount("R2", REJECTED_BALLOT_2, 32);
		RejectedBallotCount rejectedBallotCount3 = new RejectedBallotCount("R3", REJECTED_BALLOT_3, 33);
		RejectedBallotCount rejectedBallotCount4 = new RejectedBallotCount("R4", REJECTED_BALLOT_4, 34);
		List<RejectedBallotCount> rejectedBallotCounts = new ArrayList<>();
		rejectedBallotCounts.add(rejectedBallotCount1);
		rejectedBallotCounts.add(rejectedBallotCount2);
		rejectedBallotCounts.add(rejectedBallotCount3);
		rejectedBallotCounts.add(rejectedBallotCount4);
		countyFinalCount1.setRejectedBallotCounts(rejectedBallotCounts);
		return countyFinalCount1;
	}

	private FinalCount buildFinalCount2(String id, AreaPath countingAreaPath, String areaName) {
		FinalCount countyFinalCount2 = new FinalCount(id, countingAreaPath, CountCategory.VO, areaName, FYLKESVALGSTYRET, REPORTING_UNIT_AREA_NAME, true);
		countyFinalCount2.setStatus(CountStatus.SAVED);
		countyFinalCount2.setComment("comment2");
		countyFinalCount2.setVoteCountPk(2L);
		BallotCount ballotCount1 = new BallotCount("P1", PARTY_NAME_1, 14, 24);
		BallotCount ballotCount2 = new BallotCount("P2", PARTY_NAME_2, 15, 25);
		BallotCount ballotCount3 = new BallotCount("P3", PARTY_NAME_3, 16, 26);
		List<BallotCount> ballotCounts = new ArrayList<>();
		ballotCounts.add(ballotCount1);
		ballotCounts.add(ballotCount2);
		ballotCounts.add(ballotCount3);
		countyFinalCount2.setBallotCounts(ballotCounts);
		countyFinalCount2.setBlankBallotCount(2);
		RejectedBallotCount rejectedBallotCount1 = new RejectedBallotCount("R1", REJECTED_BALLOT_1, 35);
		RejectedBallotCount rejectedBallotCount2 = new RejectedBallotCount("R2", REJECTED_BALLOT_2, 36);
		RejectedBallotCount rejectedBallotCount3 = new RejectedBallotCount("R3", REJECTED_BALLOT_3, 37);
		RejectedBallotCount rejectedBallotCount4 = new RejectedBallotCount("R4", REJECTED_BALLOT_4, 38);
		List<RejectedBallotCount> rejectedBallotCounts = new ArrayList<>();
		rejectedBallotCounts.add(rejectedBallotCount1);
		rejectedBallotCounts.add(rejectedBallotCount2);
		rejectedBallotCounts.add(rejectedBallotCount3);
		rejectedBallotCounts.add(rejectedBallotCount4);
		countyFinalCount2.setRejectedBallotCounts(rejectedBallotCounts);
		return countyFinalCount2;
	}

	
}
