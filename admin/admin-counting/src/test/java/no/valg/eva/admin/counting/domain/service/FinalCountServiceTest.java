package no.valg.eva.admin.counting.domain.service;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.mockups.BallotMockups;
import no.valg.eva.admin.common.mockups.ContestMockups;
import no.valg.eva.admin.common.mockups.ElectionEventMockups;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.common.mockups.MvElectionMockups;
import no.valg.eva.admin.common.mockups.ReportingUnitMockups;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.counting.builder.AreaPathMockups;
import no.valg.eva.admin.counting.builder.CountContextMockups;
import no.valg.eva.admin.counting.builder.FinalCountMockups;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.CountQualifier;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.domain.service.votecount.VoteCountStatusendringTrigger;
import no.valg.eva.admin.counting.mockup.ContestReportMockups;
import no.valg.eva.admin.counting.repository.BallotRejectionRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class FinalCountServiceTest {
	private static final String COMMENT = "uvisst";
	private static final String AREA_NAME = "areaName";
	private static final String REPORTING_UNIT_AREA_NAME = "reportingUnitAreaName";
	private static final int BLANK_BALLOT_COUNT = 1;
	private static final String ENDELIG = "E";
	private static final String VALGTING_ORDINAERE = "VO";

	private FinalCountService finalCountService;
	@Mock
	private ReportCountCategoryRepository reportCountCategoryRepository;
	@Mock
	private ReportingUnitRepository reportingUnitRepository;
	@Mock
	private MvElectionRepository mvElectionRepository;
	@Mock
	private CountingCodeValueRepository countingCodeValueRepository;
	@Mock
	private BallotRejectionRepository ballotRejectionRepository;
	@Mock
	private VoteCountService voteCountService;
	@Mock
	private VoteCountStatusendringTrigger voteCountStatusendringTrigger;

	private ElectionEvent electionEvent;
	private AreaPath areaPath;
	private MvArea mvArea;
	private CountContext countContext;

	@BeforeSuite
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void setUp() throws Exception {
		finalCountService = new FinalCountService(reportCountCategoryRepository, countingCodeValueRepository, reportingUnitRepository,
				ballotRejectionRepository, voteCountService, voteCountStatusendringTrigger);

		electionEvent = ElectionEventMockups.electionEvent();

		Municipality municipality = MunicipalityMockups.municipality(true);
		areaPath = AreaPathMockups.areaPath("730001.47.01.0101.010100.0001");
		ElectionPath electionPath = new ElectionPath("730001.01.01.000001");
		mvArea = MvAreaMockups.municipalityMvArea(municipality);

		countContext = CountContextMockups.countContext(electionPath, CountCategory.VO);
	}

	@Test
	public void validateCountCategory_whenBoroughElection_shouldAcceptValidCategories() {
		finalCountService.validateCountCategoryForBoroughElection(CountCategory.VO);
		finalCountService.validateCountCategoryForBoroughElection(CountCategory.FO);
		finalCountService.validateCountCategoryForBoroughElection(CountCategory.FS);
		finalCountService.validateCountCategoryForBoroughElection(CountCategory.BF);
		finalCountService.validateCountCategoryForBoroughElection(CountCategory.VS);
		finalCountService.validateCountCategoryForBoroughElection(CountCategory.VB);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void validateCountCategory_whenBoroughElection_shouldRejectInvalidCategory() {
		finalCountService.validateCountCategoryForBoroughElection(CountCategory.VF);
	}

	@Test
	public void saveNewFinalCountShouldCreateAContestReportIfItDoesNotExist() {
		// har en final count som har en ID som ikke finnes for en VC
		FinalCount finalCountNew = FinalCountMockups.finalCount(
				CountStatus.NEW,
				BLANK_BALLOT_COUNT,
				true,
				COMMENT,
				areaPath,
				AREA_NAME,
				REPORTING_UNIT_AREA_NAME,
				"EVO1", ReportingUnitTypeId.FYLKESVALGSTYRET);

		CountQualifier countQualifier = new CountQualifier();
		countQualifier.setId(ENDELIG);
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(VALGTING_ORDINAERE);
		VoteCountStatus voteCountStatus = new VoteCountStatus();
		voteCountStatus.setId(2);

		Map<String, Ballot> ballotMap = new HashMap<>();
		Ballot ballot = BallotMockups.demBallot();
		Ballot nkpBallot = BallotMockups.nkpBallot();
		Ballot kystBallot = BallotMockups.kystBallot();
		Ballot ballotBlank = BallotMockups.blankBallot();
		ballotMap.put(ballot.getId(), ballot);
		ballotMap.put(ballotBlank.getId(), ballotBlank);
		ballotMap.put(nkpBallot.getId(), nkpBallot);
		ballotMap.put(kystBallot.getId(), kystBallot);

		Contest contest = ContestMockups.defaultContestWithoutContestReport(electionEvent);
		MvElection mvElection = MvElectionMockups.testMvElection(contest);

		ReportingUnit reportingUnit1 = ReportingUnitMockups.reportingUnit();

		// mvElectionRepository er en mock - mocker ut hvilken mvElection som returneres
		when(mvElectionRepository.finnEnkeltMedSti(countContext.valgdistriktSti())).thenReturn(mvElection);
		// codeValueRepository er en mock - mocker retur av riktig countQualifier
		when(countingCodeValueRepository.findCountQualifierById(anyString())).thenReturn(countQualifier);
		when(countingCodeValueRepository.findVoteCountCategoryById(anyString())).thenReturn(voteCountCategory);
		when(countingCodeValueRepository.findVoteCountStatusById(anyInt())).thenReturn(voteCountStatus);

		ContestReport contestReport = ContestReportMockups.contestReport(contest, reportingUnit1);
		when(voteCountService
				.createContestReport(any(ContestReport.class)))
						.thenReturn(contestReport);

		finalCountService.saveNewFinalCount(reportingUnit1, countContext, finalCountNew, mvArea, ballotMap, mvElection);
	}
}
