package no.valg.eva.admin.counting.domain.service.votecount;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.NEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PreliminaryCountDataProviderTest {
	private static final boolean INCLUDE_MARK_OFF_COUNT = true;
	private static final boolean EXCLUDE_MARK_OFF_COUNT = false;
	private static final ElectionPath CONTEST_PATH = ElectionPath.from("111111.11.11.111111");
	private static final String COUNT_ID = "FVO1";
	private static final String COMMENT = "comment";
	private static final int ZERO = 0;
	private static final int BLANK_BALLOT_COUNT = 1;
	private static final int QUESTIONABLE_BALLOT_COUNT = 2;
	private static final String INFO_TEXT = COMMENT;
	private static final int REJECTED_BALLOTS = QUESTIONABLE_BALLOT_COUNT;
	private static final String BALLOT_ID_1 = "B1";
	private static final String BALLOT_ID_2 = "B2";
	private static final String BALLOT_ID_3 = "B3";
	private static final String PARTY_ID_1 = "B1";
	private static final String PARTY_ID_2 = "B2";
	private static final String PARTY_ID_3 = "B3";
	private static final String BALLOT_NAME_1 = "@party[B1].name";
	private static final String BALLOT_NAME_2 = "@party[B2].name";
	private static final String BALLOT_NAME_3 = "@party[B3].name";
	private static final int UNMODIFIED_COUNT_1 = 11;
	private static final int UNMODIFIED_COUNT_2 = 12;
	private static final int MODIFIED_COUNT_1 = 21;
	private static final int MODIFIED_COUNT_2 = 22;
	private static final int ORDINARY_BALLOT_COUNT =
			BLANK_BALLOT_COUNT + UNMODIFIED_COUNT_1 + UNMODIFIED_COUNT_2 + MODIFIED_COUNT_1 + MODIFIED_COUNT_2;
	private static final int APPROVED_BALLOTS = ORDINARY_BALLOT_COUNT;
	private static final long CONTEST_PK = 111;
	private static final int LATE_VALIDATION_COVERS = 3;
	private static final int TECHNICAL_VOTINGS = 4;
	private static final int EXPECTED_BALLOT_COUNT = TECHNICAL_VOTINGS;
	private static final int MARK_OFF_COUNT = 5;
	private static final int TOTAL_BALLOT_COUNT_FOR_OTHER_POLLING_DISTRICTS =
			(ORDINARY_BALLOT_COUNT + REJECTED_BALLOTS) * 2;
	private static final String PARTY_ID_BLANK = "BLANK";

	@Test
	public void id_whenNoPreliminaryVoteCount_returnsNull() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider();
		assertThat(dataProvider.id()).isNull();
	}

	@Test
	public void id_whenPreliminaryVoteCount_returnsId() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubPreliminaryVoteCount());
		assertThat(dataProvider.id()).isEqualTo(COUNT_ID);
	}

	@Test
	public void status_whenNoPreliminaryVoteCount_returnsNew() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider();
		assertThat(dataProvider.status()).isEqualTo(NEW);
	}

	@Test
	public void status_whenPreliminaryVoteCount_returnsStatus() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubPreliminaryVoteCount());
		assertThat(dataProvider.status()).isEqualTo(APPROVED);
	}

	@Test
	public void comment_whenNoPreliminaryVoteCount_returnsNull() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider();
		assertThat(dataProvider.comment()).isNull();
	}

	@Test
	public void comment_whenPreliminaryVoteCount_returnsComment() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubPreliminaryVoteCount());
		assertThat(dataProvider.comment()).isEqualTo(COMMENT);
	}

	@Test
	public void blankBallotCount_whenNoPreliminaryVoteCount_returnsZero() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider();
		assertThat(dataProvider.blankBallotCount()).isEqualTo(ZERO);
	}

	@Test
	public void blankBallotCount_whenPreliminaryVoteCount_returnsBlankBallotCount() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubPreliminaryVoteCount());
		assertThat(dataProvider.blankBallotCount()).isEqualTo(BLANK_BALLOT_COUNT);
	}

	@Test
	public void questionableBallotCount_whenNoPreliminaryVoteCount_returnsZero() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider();
		assertThat(dataProvider.questionableBallotCount()).isEqualTo(ZERO);
	}

	@Test
	public void questionableBallotCount_whenPreliminaryVoteCount_returnsQuestionableBallotCount() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubPreliminaryVoteCount());
		assertThat(dataProvider.questionableBallotCount()).isEqualTo(QUESTIONABLE_BALLOT_COUNT);
	}

	@Test
	public void ballotCounts_whenNoPreliminaryVoteCount_returnsBallotCountsFromAffiliations() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubAffiliationRepository());
		assertThat(dataProvider.ballotCounts()).containsExactly(
				ballotCount(BALLOT_ID_1, BALLOT_NAME_1, ZERO, ZERO),
				ballotCount(BALLOT_ID_2, BALLOT_NAME_2, ZERO, ZERO)
				);
	}

	@Test
	public void ballotCounts_whenPreliminaryVoteCount_returnsBallotCountsFromPreliminaryVoteCount() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubPreliminaryVoteCount());
		assertThat(dataProvider.ballotCounts()).containsExactly(
				ballotCount(BALLOT_ID_1, BALLOT_NAME_1, UNMODIFIED_COUNT_1, MODIFIED_COUNT_1),
				ballotCount(BALLOT_ID_2, BALLOT_NAME_2, UNMODIFIED_COUNT_2, MODIFIED_COUNT_2),
				ballotCount(BALLOT_ID_3, BALLOT_NAME_3, ZERO, ZERO)
				);
	}

	@Test
	public void lateValidationCovers_whenFo_returnsLateValidationCovers() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(FO, stubAntallStemmesedlerLagtTilSideDomainService());
		assertThat(dataProvider.lateValidationCovers()).isEqualTo(LATE_VALIDATION_COVERS);
	}	
	
	@Test
	public void lateValidationCovers_whenFs_returnsLateValidationCovers() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(FS, stubAntallStemmesedlerLagtTilSideDomainService());
		assertThat(dataProvider.lateValidationCovers()).isEqualTo(LATE_VALIDATION_COVERS);
	}

	@Test
	public void lateValidationCovers_whenNotFoOrFs_returnsNull() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(VO);
		assertThat(dataProvider.lateValidationCovers()).isNull();
	}

	@Test
	public void expectedBallotCount_whenNotFo_returnNull() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(FS);
		assertThat(dataProvider.expectedBallotCount()).isNull();
	}

	@Test
	public void expectedBallotCount_whenFoAndPreliminaryVoteCount_returnExpectedBallotCount() {
		PreliminaryCountDataProvider dataProvider =
				preliminaryCountDataProvider(stubVoteCountService(BY_TECHNICAL_POLLING_DISTRICT), stubPreliminaryVoteCount(), FO);
		assertThat(dataProvider.expectedBallotCount()).isEqualTo(EXPECTED_BALLOT_COUNT);
	}

	@Test
	public void expectedBallotCount_whenFoAndNoPreliminaryVoteCountAndOnTechnicalPollingDistrict_returnsZero() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubVoteCountService(BY_TECHNICAL_POLLING_DISTRICT), FO);
		assertThat(dataProvider.expectedBallotCount()).isZero();
	}

	@Test
	public void expectedBallotCount_whenFoAndNoPreliminaryVoteCountAndNotOnTechnicalPollingDistrict_returnsNull() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubVoteCountService(), FO);
		assertThat(dataProvider.expectedBallotCount()).isNull();
	}

	@Test
	public void markOffCount_whenVo_returnsNull() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(VO);
		assertThat(dataProvider.markOffCount()).isNull();
	}

	@Test
	public void markOffCount_whenNotVo_returnsMarkOffCount() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubVoteCountService(INCLUDE_MARK_OFF_COUNT), FO);
		assertThat(dataProvider.markOffCount()).isEqualTo(MARK_OFF_COUNT);
	}

	@Test
	public void markOffCount_whenNotVoAndNoMarkOffCount_returnsNull() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubVoteCountService(EXCLUDE_MARK_OFF_COUNT), FO);
		assertThat(dataProvider.markOffCount()).isNull();
	}

	@Test
	public void totalBallotCountForOtherPollingDistricts_whenFoAndTechnicalPollingDistrictAndMarkOffCount_returnsCount() {
		MvArea stubMvArea = stub(MvArea.class);
		PreliminaryCountDataProvider dataProvider =
				preliminaryCountDataProvider(stubVoteCountService(stubMvArea, BY_TECHNICAL_POLLING_DISTRICT, INCLUDE_MARK_OFF_COUNT), FO, stubMvArea);
		assertThat(dataProvider.totalBallotCountForOtherPollingDistricts()).isEqualTo(TOTAL_BALLOT_COUNT_FOR_OTHER_POLLING_DISTRICTS);
	}

	@Test
	public void totalBallotCountForOtherPollingDistricts_whenFoAndTechnicalPollingDistrictAndNoMarkOffCount_returnsNull() {
		MvArea stubMvArea = stub(MvArea.class);
		PreliminaryCountDataProvider dataProvider =
				preliminaryCountDataProvider(stubVoteCountService(stubMvArea, BY_TECHNICAL_POLLING_DISTRICT, EXCLUDE_MARK_OFF_COUNT), FO);
		assertThat(dataProvider.totalBallotCountForOtherPollingDistricts()).isNull();
	}

	@Test
	public void totalBallotCountForOtherPollingDistricts_whenFoAndNotTechnicalPollingDistrict_returnsNull() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubVoteCountService(), FO);
		assertThat(dataProvider.totalBallotCountForOtherPollingDistricts()).isNull();
	}

	@Test
	public void totalBallotCountForOtherPollingDistricts_whenNotFo_returnsNull() {
		PreliminaryCountDataProvider dataProvider = preliminaryCountDataProvider(stubVoteCountService(), VO);
		assertThat(dataProvider.totalBallotCountForOtherPollingDistricts()).isNull();
	}

	private VoteCountService stubVoteCountService() {
		return stubVoteCountService(null, null, false);
	}

	private VoteCountService stubVoteCountService(boolean includeMarkOffCount) {
		return stubVoteCountService(null, null, includeMarkOffCount);
	}

	private VoteCountService stubVoteCountService(CountingMode countingMode) {
		return stubVoteCountService(null, countingMode, false);
	}

	private VoteCountService stubVoteCountService(MvArea countingMvArea, CountingMode countingMode, boolean includeMarkOffCount) {
		VoteCountService stubVoteCountService = stub(VoteCountService.class);
		if (countingMvArea != null) {
			when(
					stubVoteCountService
                            .findPreliminaryVoteCountsByReportingUnitContestPathAndCategory(any(), any(ElectionPath.class),
									any(CountCategory.class)))
					.thenReturn(preliminaryVoteCounts(countingMvArea));
		}
		if (countingMode != null) {
			when(stubVoteCountService.countingMode(any(CountContext.class), any(Municipality.class), any(MvElection.class))).thenReturn(countingMode);
		}
		if (includeMarkOffCount) {
			when(
					stubVoteCountService.markOffCountForPreliminaryCount(any(CountContext.class), any(MvElection.class), any(MvArea.class),
							any(CountCategory.class)))
					.thenReturn((long) MARK_OFF_COUNT);
		} else {
			when(
					stubVoteCountService.markOffCountForPreliminaryCount(any(CountContext.class), any(MvElection.class), any(MvArea.class),
							any(CountCategory.class)))
					.thenReturn(null);
		}
		return stubVoteCountService;
	}

	private List<VoteCount> preliminaryVoteCounts(MvArea countingMvArea) {
		return asList(
				preliminaryVoteCount(),
				preliminaryVoteCount(countingMvArea),
				preliminaryVoteCount());
	}

	private VoteCount preliminaryVoteCount() {
		return preliminaryVoteCount(stub(MvArea.class));
	}

	private VoteCount preliminaryVoteCount(MvArea countingMvArea) {
		VoteCount voteCount = new VoteCount();
		voteCount.setMvArea(countingMvArea);
		voteCount.setApprovedBallots(APPROVED_BALLOTS);
		voteCount.setRejectedBallots(REJECTED_BALLOTS);
		return voteCount;
	}

	private AntallStemmesedlerLagtTilSideDomainService stubAntallStemmesedlerLagtTilSideDomainService() {
		AntallStemmesedlerLagtTilSideDomainService stubAntallStemmesedlerLagtTilSideDomainService = stub(AntallStemmesedlerLagtTilSideDomainService.class);
		AntallStemmesedlerLagtTilSide stubAntallStemmesedlerLagtTilSide = stub(AntallStemmesedlerLagtTilSide.class);

		when(stubAntallStemmesedlerLagtTilSideDomainService.hentAntallStemmesedlerLagtTilSide(any(Municipality.class))).thenReturn(stubAntallStemmesedlerLagtTilSide);
		when(stubAntallStemmesedlerLagtTilSide.getTotaltAntallStemmesedlerLagtTilSideForValg()).thenReturn(LATE_VALIDATION_COVERS);

		return stubAntallStemmesedlerLagtTilSideDomainService;
	}

	private BallotCount ballotCount(String id, String name, int unmodifiedCount, int modifiedCount) {
		return new BallotCount(id, name, unmodifiedCount, modifiedCount);
	}

	private AffiliationRepository stubAffiliationRepository() {
		AffiliationRepository stubAffiliationRepository = stub(AffiliationRepository.class);
		when(stubAffiliationRepository.findApprovedByContest(CONTEST_PK)).thenReturn(affiliations());
		return stubAffiliationRepository;
	}

	private List<Affiliation> affiliations() {
		return asList(
				affiliation(EvoteConstants.BALLOT_BLANK, PARTY_ID_BLANK),
				affiliation(BALLOT_ID_1, PARTY_ID_1),
				affiliation(BALLOT_ID_2, PARTY_ID_2));
	}

	private Affiliation affiliation(String ballotId, String partyName) {
		Affiliation affiliation = new Affiliation();
		Ballot ballot = ballot(ballotId);
		affiliation.setBallot(ballot);
		ballot.setAffiliation(affiliation);
		if (partyName != null) {
			affiliation.setParty(party(partyName));
		}
		return affiliation;
	}

	private Party party(String partyName) {
		Party party = new Party();
		party.setId(partyName);
		return party;
	}

	private Ballot ballot(String ballotId) {
		Ballot ballot = new Ballot();
		ballot.setPk(new Random().nextLong());
		ballot.setId(ballotId);
		return ballot;
	}

	private VoteCount stubPreliminaryVoteCount() {
		return stubPreliminaryVoteCount(null);
	}

	private VoteCount stubPreliminaryVoteCount(MvArea mvArea) {
		VoteCount voteCount = stub(VoteCount.class);
		when(voteCount.getId()).thenReturn(COUNT_ID);
		when(voteCount.getCountStatus()).thenReturn(APPROVED);
		when(voteCount.getInfoText()).thenReturn(INFO_TEXT);
		when(voteCount.getBlankBallotCount()).thenReturn(BLANK_BALLOT_COUNT);
		when(voteCount.getRejectedBallots()).thenReturn(REJECTED_BALLOTS);
		when(voteCount.getBallotCountMap()).thenReturn(ballotCountMap());
		when(voteCount.getContestReport().getContest().getSortedApprovedBallots()).thenReturn(ballots());
		when(voteCount.getTechnicalVotings()).thenReturn(TECHNICAL_VOTINGS);
		if (mvArea != null) {
			when(voteCount.getMvArea()).thenReturn(mvArea);
		} else {
			when(voteCount.getMvArea()).thenReturn(stub(MvArea.class));
		}
		return voteCount;
	}

	private Set<Ballot> ballots() {
		LinkedHashSet<Ballot> ballots = new LinkedHashSet<>();
		Collections.addAll(
				ballots,
				ballot(EvoteConstants.BALLOT_BLANK),
				ballot(BALLOT_ID_1),
				ballot(BALLOT_ID_2),
				affiliation(BALLOT_ID_3, PARTY_ID_3).getBallot());
		return ballots;
	}

	private Map<String, no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountMap() {
		HashMap<String, no.valg.eva.admin.counting.domain.model.BallotCount> ballotCountMap = new HashMap<>();
		ballotCountMap.put(EvoteConstants.BALLOT_BLANK, ballotCountEntity(EvoteConstants.BALLOT_BLANK, PARTY_ID_BLANK, BLANK_BALLOT_COUNT, ZERO));
		ballotCountMap.put(BALLOT_ID_1, ballotCountEntity(BALLOT_ID_1, PARTY_ID_1, UNMODIFIED_COUNT_1, MODIFIED_COUNT_1));
		ballotCountMap.put(BALLOT_ID_2, ballotCountEntity(BALLOT_ID_2, PARTY_ID_2, UNMODIFIED_COUNT_2, MODIFIED_COUNT_2));
		return ballotCountMap;
	}

	private no.valg.eva.admin.counting.domain.model.BallotCount ballotCountEntity(String ballotId, String ballotName, int unmodifiedBallots, int modifiedBallots) {
		no.valg.eva.admin.counting.domain.model.BallotCount ballotCount = new no.valg.eva.admin.counting.domain.model.BallotCount();
		ballotCount.setPk(new Random().nextLong());
		ballotCount.setBallot(affiliation(ballotId, ballotName).getBallot());
		ballotCount.setUnmodifiedBallots(unmodifiedBallots);
		ballotCount.setModifiedBallots(modifiedBallots);
		return ballotCount;
	}

	private <T> T stub(Class<T> type) {
		return mock(type, RETURNS_DEEP_STUBS);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider() {
		return preliminaryCountDataProvider(null, null, null, VO, null, stub(MvArea.class), null);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider(AffiliationRepository affiliationRepository) {
		return preliminaryCountDataProvider(affiliationRepository, null, null, VO, null, stub(MvArea.class), null);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider(VoteCount preliminaryVoteCount) {
		return preliminaryCountDataProvider(null, null, preliminaryVoteCount, VO, null, stub(MvArea.class), null);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider(CountCategory category) {
		return preliminaryCountDataProvider(null, mock(VoteCountService.class), null, category, null, stub(MvArea.class), null);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider(VoteCountService voteCountService, CountCategory category) {
		return preliminaryCountDataProvider(null, voteCountService, null, category, null, stub(MvArea.class), null);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider(VoteCountService voteCountService, VoteCount preliminaryVoteCount, CountCategory category) {
		return preliminaryCountDataProvider(null, voteCountService, preliminaryVoteCount, category, null, stub(MvArea.class), null);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider(VoteCountService voteCountService, CountCategory category, MvArea countingMvArea) {
		return preliminaryCountDataProvider(null, voteCountService, null, category, null, countingMvArea, null);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider(CountCategory category, ContestReport contestReport) {
		VoteCountService fakeVoteCountService = mock(VoteCountService.class);
		when(fakeVoteCountService
				.findContestReport(any(ReportingUnit.class), any(MvElection.class)))
				.thenReturn(contestReport);
		return preliminaryCountDataProvider(null, fakeVoteCountService, null, category, contestReport, stub(MvArea.class), null);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider(CountCategory category,
																	  AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService) {
		return preliminaryCountDataProvider(null, null, null, category, null, stub(MvArea.class), antallStemmesedlerLagtTilSideDomainService);
	}

	private PreliminaryCountDataProvider preliminaryCountDataProvider(
			AffiliationRepository affiliationRepository, VoteCountService voteCountService, VoteCount preliminaryVoteCount, CountCategory category,
			ContestReport contestReport, MvArea countingMvArea, AntallStemmesedlerLagtTilSideDomainService antallStemmesedlerLagtTilSideDomainService) {
		CountContext context = new CountContext(CONTEST_PATH, category);
		ReportingUnit reportingUnit = contestReport != null ? contestReport.getReportingUnit() : null;
		MvElection stubContestMvElection = stubContestMvElection();
		return new PreliminaryCountDataProvider(affiliationRepository, voteCountService, reportingUnit, preliminaryVoteCount, context,
				stubContestMvElection, countingMvArea, antallStemmesedlerLagtTilSideDomainService);
	}

	private MvElection stubContestMvElection() {
		MvElection stubContestMvElection = stub(MvElection.class);
		when(stubContestMvElection.getContest().getPk()).thenReturn(CONTEST_PK);
		when(stubContestMvElection.getPath()).thenReturn(CONTEST_PATH.path());
		return stubContestMvElection;
	}
}
