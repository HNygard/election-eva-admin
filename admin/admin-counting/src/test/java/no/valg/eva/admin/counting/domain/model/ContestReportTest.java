package no.valg.eva.admin.counting.domain.model;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.NEW;
import static no.valg.eva.admin.common.counting.model.CountStatus.REVOKED;
import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import no.evote.constants.VoteCountStatusEnum;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.counting.domain.visitor.CountingVisitor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ContestReportTest {
	private static final Long MV_AREA_POLLING_DISTRICT = 1L;
	private static final Long ANOTHER_MV_AREA_POLLING_DISTRICT = 2L;
	private static final AreaPath AREA_PATH_1 = AreaPath.from("111111.11.11.1111.111111.1111");
	private static final AreaPath AREA_PATH_2 = AreaPath.from("111111.11.11.1111.111111.2222");
	private static final String ID_COUNT_1 = "PVO1";
	private static final String ID_COUNT_2 = "FVO1";
	private static final String ID_COUNT_3 = "EVO1";
	private static final String ID_NOT_FOUND = "finnesIkke";
	private static final String ANY_AREA_PATH = "201301.47.03.0301";
	private static final Long ANY_MV_AREA_PK = 1L;
	private static final String V = "V";
	private static final String H = "H";
	private static final String A = "A";
	private static final int BALLOT_COUNTS_FOR_FO_IN_VOTE_COUNT_SET = 3;
	private static final int BALLOT_COUNTS_FOR_VO_IN_VOTE_COUNT_SET = 3 + 3;
	private static final long MV_AREA_PK_0 = 0L;
	private static final long MV_AREA_PK_1 = 1L;
	private static final long MV_AREA_PK_2 = 2L;
	private static final int FORVENTET_ANTALL_0 = 0;
	private static final int FORVENTET_ANTALL_6 = 6;
	private static final int FORVENTET_ANTALL_9 = 9;
	private static final int FORVENTET_ANTALL_12 = 12;
	private static final boolean ELECTRONIC_MARKOFF = true;
	private static final boolean NOT_ON_BOROUGH_LEVEL = false;
	private static final boolean NOT_ELECTRONIC_MARKOFF = false;
	private static final boolean ON_BOROUGH_LEVEL = true;
	private ContestReport contestReportField;
	private VoteCount savedFinalVoteCountNo1ForVoAndArea1;
	private VoteCount approvedFinalVoteCountNo1ForVoAndArea2;
	private VoteCount approvedPreliminaryVoteCountNo1ForFoAndArea1;
	private VoteCount approvedFinalVoteCountNo1ForFoAndArea1;
	private VoteCount approvedFinalVoteCountNo2ForFoAndArea2;
	private VoteCount protocolCountInMvArea;
	private MvArea mvAreaForPollingDistrict;
	private CountQualifier qualifierFinal;
	private CountQualifier preliminary;
	private VoteCountCategory categoryVo;
	private ReportingUnit reportingUnit;

	@BeforeMethod
	public void setUp() {
		contestReportField = new ContestReport();
		reportingUnit = new ReportingUnit();
		reportingUnit.setPk(1L);
		contestReportField.setReportingUnit(reportingUnit);

		final Set<VoteCount> voteCountSet = new HashSet<>();
		VoteCount preliminaryCountInMvArea = count(PRELIMINARY, MV_AREA_POLLING_DISTRICT, ID_COUNT_1, ANY_AREA_PATH);
		voteCountSet.add(preliminaryCountInMvArea);
		voteCountSet.add(count(PROTOCOL, ANOTHER_MV_AREA_POLLING_DISTRICT, ID_COUNT_2, ANY_AREA_PATH));
		protocolCountInMvArea = count(PROTOCOL, MV_AREA_POLLING_DISTRICT, ID_COUNT_3, ANY_AREA_PATH);
		voteCountSet.add(protocolCountInMvArea);
		contestReportField.setVoteCountSet(voteCountSet);

		mvAreaForPollingDistrict = new MvArea();
		mvAreaForPollingDistrict.setPk(MV_AREA_POLLING_DISTRICT);

		qualifierFinal = new CountQualifier();
		qualifierFinal.setId(FINAL.getId());
		preliminary = new CountQualifier();
		preliminary.setId(PRELIMINARY.getId());
		categoryVo = new VoteCountCategory();
		categoryVo.setId(VO.getId());
	}

	private VoteCount count(no.valg.eva.admin.common.counting.model.CountQualifier qualifier, final Long mvAreaPk, final String countId, String areaPath) {
		VoteCount count = new VoteCount();
		count.setCountQualifier(countQualifier(qualifier));
		count.setId(countId);
		VoteCountCategory voteCountCategory = voteCountCategory(VO);
		count.setVoteCountCategory(voteCountCategory);
		count.setVoteCountStatus(buildVoteCountStatus(VoteCountStatusEnum.APPROVED));
		MvArea mvArea = makeMvArea(mvAreaPk, ELECTRONIC_MARKOFF);
		mvArea.setAreaPath(areaPath);
		count.setMvArea(mvArea);
		return count;
	}

	private CountQualifier countQualifier(no.valg.eva.admin.common.counting.model.CountQualifier qualifier) {
		if (qualifier == null) {
			return null;
		}
		CountQualifier countQualifier = new CountQualifier();
		countQualifier.setId(qualifier.getId());
		return countQualifier;
	}

	private VoteCountCategory voteCountCategory(CountCategory category) {
		if (category == null) {
			return null;
		}
		VoteCountCategory voteCountCategory = new VoteCountCategory();
		voteCountCategory.setId(category.getId());
		voteCountCategory.setEarlyVoting(category.isEarlyVoting());
		return voteCountCategory;
	}

	private VoteCountStatus buildVoteCountStatus(VoteCountStatusEnum status) {
		VoteCountStatus voteCountStatus = new VoteCountStatus();
		voteCountStatus.setId(status.getStatus());
		return voteCountStatus;
	}

	private MvArea makeMvArea(long mvAreaPk, boolean electronicMarkoff) {
		MvArea mvArea = new MvArea();
		mvArea.setPk(mvAreaPk);
		// Lager nødvendige objekter og pekere, mvAreaPk blir konvertert til 4-sifret id med ledende nuller og blir polling-district-id
		Borough borough = new Borough("0", "br-0", null);
		PollingDistrict pollingDistrict = new PollingDistrict(String.format("%04d", mvAreaPk), "pd-" + mvAreaPk, borough);
		borough.setPollingDistricts(Collections.singleton(pollingDistrict));
		borough.setMunicipality(makeMunicipality(electronicMarkoff));
		mvArea.setPollingDistrict(pollingDistrict);
		return mvArea;
	}

	private Municipality makeMunicipality(boolean electronicMarkoff) {
		Municipality municipality = new Municipality("1", "Municipality1", null);
		municipality.setElectronicMarkoffs(electronicMarkoff);
		return municipality;
	}

	@Test
	public void testFindVoteCountByCountingAreaAndIdWhenCountIsFound() {
		assertThat(contestReportField.findVoteCountByCountingAreaAndId(mvAreaForPollingDistrict, ID_COUNT_3)).isEqualTo(protocolCountInMvArea);
	}

	@Test
	public void findFirstVoteCountByCountQualifierAndCategory_noCountsExist_returnsNull() {
		ContestReport contestReport = new ContestReport();
		VoteCount result = contestReport.findFirstVoteCountByCountQualifierAndCategory(PROTOCOL, VO);
		assertThat(result).isNull();
	}

	@Test
	public void findFirstVoteCountByCountQualifierAndCategory_countsExist_returnsFirstCount() {
		ContestReport contestReport = new ContestReport();
		Set<VoteCount> voteCounts = new HashSet<>();
		VoteCount voProtocolCount = count(PROTOCOL, ANY_MV_AREA_PK, ID_COUNT_1, ANY_AREA_PATH);
		voteCounts.add(voProtocolCount);
		contestReport.setVoteCountSet(voteCounts);

		VoteCount result = contestReport.findFirstVoteCountByCountQualifierAndCategory(PROTOCOL, VO);
		assertThat(result).isEqualTo(voProtocolCount);
	}

	@Test
	public void findFirstVoteCountByAreaPathQualifierAndCategory_countsExist_returnsFirstCount() {
		ContestReport contestReport = new ContestReport();
		Set<VoteCount> voteCounts = new HashSet<>();
		String areaPath = "201301.47.01.0101";
		VoteCount voProtocolCount = count(PROTOCOL, ANY_MV_AREA_PK, ID_COUNT_1, areaPath);
		voteCounts.add(voProtocolCount);
		contestReport.setVoteCountSet(voteCounts);

		VoteCount result = contestReport.findFirstVoteCountByAreaPathQualifierAndCategory(
				AreaPath.from(areaPath), PROTOCOL, VO);

		assertThat(result).isEqualTo(voProtocolCount);
	}

	@Test
	public void findFirstVoteCountByAreaPathQualifierAndCategory_noCountsExist_returnsNull() {
		ContestReport contestReport = new ContestReport();
		Set<VoteCount> voteCounts = new HashSet<>();
		String areaPath = "201301.47.01.0101";
		String differentAreaPath = "201301.47.02.0201";
		VoteCount voProtocolCount = count(PROTOCOL, ANY_MV_AREA_PK, ID_COUNT_1, areaPath);
		voteCounts.add(voProtocolCount);
		contestReport.setVoteCountSet(voteCounts);

		VoteCount result = contestReport.findFirstVoteCountByAreaPathQualifierAndCategory(
				AreaPath.from(differentAreaPath), PROTOCOL, VO);

		assertThat(result).isNull();
	}

	@Test
	public void testFindVoteCountByCountingAreaAndIdWhenCountIsNotFound() {
		assertThat(contestReportField.findVoteCountByCountingAreaAndId(mvAreaForPollingDistrict, ID_NOT_FOUND)).isNull();
	}

	@Test
	public void testIsReportedBy() {
		assertThat(contestReportField.isReportedBy(reportingUnit)).isTrue();
	}

	@Test
	public void uniqueKindCount_findsOne() {
		assertEquals(1, contestReportField.uniqueKindCount(preliminary, categoryVo, mvAreaForPollingDistrict));
	}

	@Test
	public void uniqueKindCount_findsNone() {
		assertEquals(0, contestReportField.uniqueKindCount(qualifierFinal, categoryVo, mvAreaForPollingDistrict));
	}

	@Test
	public void uniqueKindCount_withStatusApproved_findsOne() {
		assertEquals(1, contestReportField.uniqueKindCount(preliminary, categoryVo, mvAreaForPollingDistrict, VoteCountStatusEnum.APPROVED));
	}

	@Test
	public void uniqueKindCount_withStatusToApproval_findsNone() {
		assertEquals(0, contestReportField.uniqueKindCount(preliminary, categoryVo, mvAreaForPollingDistrict, VoteCountStatusEnum.TO_APPROVAL));
	}

	@Test
	public void findVoteCountsByAreaPathQualifierAndCategory_givenAreaPathQualifierAndCategory_returnsVoteCounts() throws Exception {
		List<VoteCount> result = contestReport().findVoteCountsByAreaPathQualifierAndCategory(AREA_PATH_1, FINAL, VO);

		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isSameAs(savedFinalVoteCountNo1ForVoAndArea1);
		assertThat(result.get(1)).isSameAs(approvedFinalVoteCountNo2ForFoAndArea2);
	}

	@Test
	public void findVoteCountsByMvAreaCountQualifierAndCategory_givenMvAreaPkQualifierAndCategory_returnsVoteCounts() throws Exception {
		List<VoteCount> result = contestReport().findVoteCountsByMvAreaCountQualifierAndCategory(1L, FINAL, VO);

		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isSameAs(savedFinalVoteCountNo1ForVoAndArea1);
		assertThat(result.get(1)).isSameAs(approvedFinalVoteCountNo2ForFoAndArea2);
	}

	@Test
	public void findVoteCountsByCountQualifierAndStatus_givenQualifierAndCategory_returnsVoteCounts() throws Exception {
		List<VoteCount> result = contestReport().findVoteCountsByCountQualifierAndStatus(FINAL, APPROVED);

		assertThat(result).hasSize(3);

		assertThat(result.get(0)).isSameAs(approvedFinalVoteCountNo1ForVoAndArea2);
		assertThat(result.get(1)).isSameAs(approvedFinalVoteCountNo1ForFoAndArea1);
		assertThat(result.get(2)).isSameAs(approvedFinalVoteCountNo2ForFoAndArea2);
	}

	@Test
	public void findFirstVoteCountByAreaPathQualifierAndCategory_givenAreaPathQualifierAndCategory_returnsVoteCount() throws Exception {
		VoteCount result = contestReport().findFirstVoteCountByAreaPathQualifierAndCategory(AREA_PATH_1, FINAL, VO);

		assertThat(result).isSameAs(savedFinalVoteCountNo1ForVoAndArea1);
	}

	@Test
	public void findFirstVoteCountByMvAreaCountQualifierAndCategory_givenMvAreaPkQualifierAndCategory_returnsVoteCount() throws Exception {
		VoteCount result = contestReport().findFirstVoteCountByMvAreaCountQualifierAndCategory(1L, FINAL, VO);

		assertThat(result).isSameAs(savedFinalVoteCountNo1ForVoAndArea1);
	}

	@Test
	public void findApprovedVoteCountByMvAreaCountQualifierAndCategory_givenMvAreaPkQualifierAndCategory_returnsVoteCount() throws Exception {
		VoteCount result = contestReport().findApprovedVoteCountByMvAreaCountQualifierAndCategory(1L, FINAL, VO);

		assertThat(result).isSameAs(approvedFinalVoteCountNo2ForFoAndArea2);
	}

	@Test
	public void findApprovedVoteCountByMvAreaCountQualifierAndCategory_givenUnknownMvAreaPk_returnsNull() throws Exception {

		VoteCount result = contestReport().findApprovedVoteCountByMvAreaCountQualifierAndCategory(9L, FINAL, VO);

		assertThat(result).isNull();
	}

	@Test
	public void findFirstVoteCountByMvAreaCountQualifierAndCategory_givenUnknownMvAreaPk_returnsNull() throws Exception {

		VoteCount result = contestReport().findFirstVoteCountByMvAreaCountQualifierAndCategory(9L, FINAL, VO);

		assertThat(result).isNull();
	}

	@Test
	public void findVoteCountsByCategoryAndQualifier_whenFoAndPreliminary_returnPreliminaryFoVoteCounts() throws Exception {
		List<VoteCount> preliminaryFoVoteCounts = contestReport().findVoteCountsByCategoryAndQualifier(FO, PRELIMINARY);

		assertThat(preliminaryFoVoteCounts).containsExactly(approvedPreliminaryVoteCountNo1ForFoAndArea1);
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@database.error.stale_object")
	public void add_givenProtocolVoteCountAndExistingProtocolVoteCountForSameAreaAndCategory_throwsException() throws Exception {
		contestReportField.add(voteCount(null, PROTOCOL, VO, null, SAVED, mvAreaForPollingDistrict));
	}

	private VoteCount voteCount(Long pk, no.valg.eva.admin.common.counting.model.CountQualifier qualifier, CountCategory category, Integer index,
								CountStatus status, MvArea mvArea) {
		VoteCount voteCount1 = new VoteCount();
		if (pk != null) {
			voteCount1.setPk(pk);
		}
		if (qualifier != null && category != null && index != null) {
			voteCount1.setId(voteCountId(qualifier, category, index));
		}
		voteCount1.setMvArea(mvArea);
		voteCount1.setCountQualifier(countQualifier(qualifier));
		voteCount1.setVoteCountCategory(voteCountCategory(category));
		voteCount1.setVoteCountStatus(voteCountStatus(status));
		return voteCount1;
	}

	private String voteCountId(no.valg.eva.admin.common.counting.model.CountQualifier qualifier, CountCategory category, int index) {
		return qualifier.getId() + category.getId() + index;
	}

	private VoteCountStatus voteCountStatus(CountStatus status) {
		if (status == null) {
			return null;
		}
		VoteCountStatus approvedVoteCountStatus = new VoteCountStatus();
		approvedVoteCountStatus.setId(status.getId());
		return approvedVoteCountStatus;
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@database.error.stale_object")
	public void add_givenPreliminaryVoteCountAndExistingPreliminaryVoteCountForSameAreaAndCategory_throwsException() throws Exception {
		contestReportField.add(voteCount(null, PRELIMINARY, VO, null, SAVED, mvAreaForPollingDistrict));
	}

	@Test
	public void accept_givenVisitorWithIncludeTrue_callsVisitOnVisitor() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		ContestReport contestReport = new ContestReport();
		contestReport.setPk(new Random().nextLong());
		when(countingVisitor.include(contestReport)).thenReturn(true);
		contestReport.accept(countingVisitor);
		verify(countingVisitor).visit(contestReport);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallVisitOnVisitor() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		ContestReport contestReport = new ContestReport();
		contestReport.setPk(new Random().nextLong());
		when(countingVisitor.include(contestReport)).thenReturn(false);
		contestReport.accept(countingVisitor);
		verify(countingVisitor, never()).visit(contestReport);
	}

	@Test
	public void accept_givenVisitorWithIncludeTrue_callsAcceptOnVoteCounts() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		ContestReport contestReport = new ContestReport();
		contestReport.setPk(new Random().nextLong());
		VoteCount voteCount = mock(VoteCount.class);
		contestReport.getVoteCountSet().add(voteCount);
		when(countingVisitor.include(contestReport)).thenReturn(true);
		contestReport.accept(countingVisitor);
		verify(voteCount).accept(countingVisitor);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallAcceptOnVoteCounts() throws Exception {
		CountingVisitor countingVisitor = mock(CountingVisitor.class);
		ContestReport contestReport = new ContestReport();
		contestReport.setPk(new Random().nextLong());
		VoteCount voteCount = mock(VoteCount.class);
		contestReport.getVoteCountSet().add(voteCount);
		when(countingVisitor.include(contestReport)).thenReturn(false);
		contestReport.accept(countingVisitor);
		verify(voteCount, never()).accept(countingVisitor);
	}

	@Test
	public void voteCountsFor_givenMvArea_returnsVoteCountsInMvArea() throws Exception {
		MvArea mvArea = mock(MvArea.class);
		when(mvArea.getAreaPath()).thenReturn("111111.11.11.1111");
		VoteCount voteCount1 = mock(VoteCount.class, RETURNS_DEEP_STUBS);
		when(voteCount1.getMvArea().getAreaPath()).thenReturn("111111.11.11.2222.111111.1111");
		VoteCount voteCount2 = mock(VoteCount.class, RETURNS_DEEP_STUBS);
		when(voteCount2.getMvArea().getAreaPath()).thenReturn("111111.11.11.1111.111111.1111");
		VoteCount voteCount3 = mock(VoteCount.class, RETURNS_DEEP_STUBS);
		when(voteCount3.getMvArea().getAreaPath()).thenReturn("111111.11.11.1111.111111");
		Set<VoteCount> voteCounts = new LinkedHashSet<>();
		voteCounts.add(voteCount1);
		voteCounts.add(voteCount2);
		voteCounts.add(voteCount3);
		ContestReport contestReport = new ContestReport();
		contestReport.setVoteCountSet(voteCounts);
		Collection<VoteCount> results = contestReport.voteCountsFor(mvArea);
		assertThat(results).containsExactly(voteCount2, voteCount3);
	}

	@Test(dataProvider = "tellingerForForeløpigRapportering")
	public void tellingerForRapportering_countQualifierPreliminary_girForeløpigeTellinger(String beskrivelse, VoteCount voteCount,
																						  int forventetAntallBallotCounts) {
		ContestReport contestReport = makeContestReport(voteCount);

		List<BallotCount> tellinger = contestReport.tellingerForRapportering(makeQualifiers(PRELIMINARY), makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0),
				makeStatuses(), CountingMode.CENTRAL, CountingMode.CENTRAL);

		assertThat(tellinger).hasSize(forventetAntallBallotCounts);
	}

	private ContestReport makeContestReport(VoteCount voteCount) {
		return makeContestReport(Collections.singletonList(voteCount), ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL);
	}

	private ContestReport makeContestReport(List<VoteCount> voteCounts, boolean isElectronicMarkoffs, boolean isOnBoroughLevel) {
		ContestReport contestReport = new ContestReport();
		Set<VoteCount> voteCountSet = new HashSet<>();
		voteCountSet.addAll(voteCounts);
		contestReport.setVoteCountSet(voteCountSet);
		contestReport.setContest(makeMockContest(isElectronicMarkoffs, isOnBoroughLevel));
		return contestReport;
	}

	private Contest makeMockContest(boolean isElectronicMarkoffs, boolean isOnBoroughLevel) {
		Contest contest = mock(Contest.class);
		Election election = mock(Election.class);
		ElectionGroup electionGroup = mock(ElectionGroup.class);
		when(contest.getElection()).thenReturn(election);
		when(election.getElectionGroup()).thenReturn(electionGroup);
		when(electionGroup.isElectronicMarkoffs()).thenReturn(isElectronicMarkoffs);
		when(contest.isOnBoroughLevel()).thenReturn(isOnBoroughLevel);
		return contest;
	}

	private Set<no.valg.eva.admin.common.counting.model.CountQualifier> makeQualifiers(
			no.valg.eva.admin.common.counting.model.CountQualifier... countQualifiers) {
		Set<no.valg.eva.admin.common.counting.model.CountQualifier> result = new HashSet<>();
		Collections.addAll(result, countQualifiers);
		return result;
	}

	private Set<MvArea> makeMvAreaSet(boolean electronicMarkoff, long... mvAreaPks) {
		Set<MvArea> mvAreas = new HashSet<>();
		Borough borough = new Borough("0", "br-0", null);
		for (long mvAreaPk : mvAreaPks) {
			MvArea mvArea = makeMvArea(mvAreaPk, borough, electronicMarkoff);
			mvAreas.add(mvArea);
		}
		return mvAreas;
	}

	// Brukes dersom man lager flere mvArea som skal kobles sammen i en borough
	private MvArea makeMvArea(long mvAreaPk, Borough borough, boolean electronicMarkoff) {
		MvArea mvArea = new MvArea();
		mvArea.setPk(mvAreaPk);
		// Lager nødvendige objekter og pekere, mvAreaPk blir konvertert til 4-sifret id med ledende nuller og blir polling-district-id
		PollingDistrict pd = new PollingDistrict(String.format("%04d", mvAreaPk), "pd-" + mvAreaPk, borough);
		if (borough.getPollingDistricts() == null) {
			borough.setPollingDistricts(Collections.singleton(pd));
		} else {
			borough.getPollingDistricts().add(pd);
		}
		borough.setMunicipality(makeMunicipality(electronicMarkoff));
		mvArea.setPollingDistrict(pd);
		return mvArea;
	}

	private Set<CountStatus> makeStatuses() {
		Set<CountStatus> countStatuses = new HashSet<>();
		countStatuses.add(CountStatus.APPROVED);
		countStatuses.add(CountStatus.TO_SETTLEMENT);
		return countStatuses;
	}

	@DataProvider
	public Object[][] tellingerForForeløpigRapportering() {

		return new Object[][]{
				{"foreløpig telling i samme område som er godkjent skal med", makeVoteCount(1L, PRELIMINARY, APPROVED, FO, MV_AREA_PK_0), 3},
				{"foreløpig telling i samme område til valgoppgjør skal med", makeVoteCount(1L, PRELIMINARY, TO_SETTLEMENT, FO, MV_AREA_PK_0), 3},
				{"foreløpig telling i samme område status ny filtreres bort", makeVoteCount(1L, PRELIMINARY, NEW, FO, MV_AREA_PK_0), 0},
				{"foreløpig telling i samme område status revoked filtreres bort", makeVoteCount(1L, PRELIMINARY, REVOKED, FO, MV_AREA_PK_0), 0},
				{"foreløpig telling i samme område status saved filtreres bort", makeVoteCount(1L, PRELIMINARY, SAVED, FO, MV_AREA_PK_0), 0},
				{"urnetelling i samme område status godkjent filtreres bort", makeVoteCount(1L, PROTOCOL, APPROVED, FO, MV_AREA_PK_0), 0},
				{"endelig telling i samme område status godkjent filtreres bort", makeVoteCount(1L, FINAL, APPROVED, FO, MV_AREA_PK_0), 0},
				{"godkjent foreløpig telling i annet område filtreres bort", makeVoteCount(1L, PRELIMINARY, APPROVED, FO, MV_AREA_PK_1), 0},
				{"godkjent foreløpig telling for VB i samme område filtreres bort", makeVoteCount(1L, PRELIMINARY, APPROVED, VB, MV_AREA_PK_0), 0},
				{"godkjent foreløpig telling for VS i samme område filtreres bort", makeVoteCount(1L, PRELIMINARY, APPROVED, VB, MV_AREA_PK_0), 0},
				{"godkjent foreløpig telling for VF i samme område filtreres bort", makeVoteCount(1L, PRELIMINARY, APPROVED, VF, MV_AREA_PK_0), 0},
				{"endelig telling for VF i samme område filtreres bort", makeVoteCount(1L, FINAL, APPROVED, VF, MV_AREA_PK_0), 0}
		};

	}

	private VoteCount makeVoteCount(long pk, no.valg.eva.admin.common.counting.model.CountQualifier countQualifier, CountStatus countStatus,
									CountCategory countCategory, long mvAreaPk) {
		VoteCount voteCount = new VoteCount();
		voteCount.setPk(pk);
		voteCount.setCountQualifier(countQualifier(countQualifier));
		voteCount.setBallotCountSet(makeBallotCountSet(voteCount));
		voteCount.setVoteCountStatus(voteCountStatus(countStatus));
		voteCount.setVoteCountCategory(voteCountCategory(countCategory));
		voteCount.setMvArea(makeMvArea(mvAreaPk, ELECTRONIC_MARKOFF));
		return voteCount;
	}

	private Set<BallotCount> makeBallotCountSet(VoteCount voteCount) {
		HashSet<BallotCount> ballotCounts = new HashSet<>();
		ballotCounts.add(makeBallotCount(1L, V, voteCount));
		ballotCounts.add(makeBallotCount(2L, H, voteCount));

		ballotCounts.add(makeBallotCount(3L, A, voteCount));

		return ballotCounts;
	}

	private BallotCount makeBallotCount(long pk, String id, VoteCount voteCount) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(pk);
		ballotCount.setBallot(makeBallot(pk, id));
		ballotCount.setVoteCount(voteCount);
		return ballotCount;
	}

	private Ballot makeBallot(Long pk, String id) {
		Ballot ballot = new Ballot();
		ballot.setPk(pk);
		ballot.setId(id);
		return ballot;
	}

	@Test(dataProvider = "tellingerForEndeligRapportering")
	public void tellingerForRapportering_countQualifierFinal_girEndeligeTellinger(List<VoteCount> voteCounts, boolean isElectronicMarkoffs,
																				  boolean isOnBoroughLevel, int forventetAntallBallotCounts, Set<MvArea> mvAreas) {
		ContestReport contestReport = makeContestReport(voteCounts, isElectronicMarkoffs, isOnBoroughLevel);

		List<BallotCount> tellinger = contestReport.tellingerForRapportering(makeQualifiers(FINAL), mvAreas, makeStatuses(), CountingMode.CENTRAL, CountingMode.CENTRAL);

		assertThat(tellinger).hasSize(forventetAntallBallotCounts);
	}

	@DataProvider
	public Object[][] tellingerForEndeligRapportering() {
		return new Object[][]{
				{Collections.singletonList(makeVoteCount(1L, FINAL, APPROVED, FO, MV_AREA_PK_0)),
						ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL, FORVENTET_ANTALL_0, makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0)},
				{Collections.singletonList(makeVoteCount(1L, FINAL, APPROVED, FS, MV_AREA_PK_0)),
						ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL, FORVENTET_ANTALL_0, makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0)},
				{asList(makeVoteCount(1L, FINAL, APPROVED, FO, MV_AREA_PK_0),
						makeVoteCount(2L, FINAL, APPROVED, FS, MV_AREA_PK_0)),
						ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL, FORVENTET_ANTALL_6, makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0)},
				{asList(makeVoteCount(1L, FINAL, APPROVED, VO, MV_AREA_PK_0),
						makeVoteCount(2L, FINAL, APPROVED, VS, MV_AREA_PK_0),
						makeVoteCount(4L, FINAL, APPROVED, VB, MV_AREA_PK_0)),
						ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL, FORVENTET_ANTALL_9, makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0)},
				{asList(makeVoteCount(1L, FINAL, APPROVED, VO, MV_AREA_PK_0),
						makeVoteCount(2L, FINAL, APPROVED, VS, MV_AREA_PK_0),
						makeVoteCount(3L, FINAL, APPROVED, VF, MV_AREA_PK_0)),
						ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL, FORVENTET_ANTALL_0, makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0)},
				{asList(makeVoteCount(1L, FINAL, APPROVED, FO, MV_AREA_PK_0),
						makeVoteCount(99L, PRELIMINARY, APPROVED, FO, MV_AREA_PK_1),
						makeVoteCount(3L, FINAL, APPROVED, FS, MV_AREA_PK_2)),
						ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL, FORVENTET_ANTALL_0,
						makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0, MV_AREA_PK_1, MV_AREA_PK_2)},
				{asList(makeVoteCount(1L, FINAL, APPROVED, VO, MV_AREA_PK_0),
						makeVoteCount(2L, FINAL, APPROVED, VS, MV_AREA_PK_0),
						makeVoteCount(3L, FINAL, APPROVED, VF, MV_AREA_PK_0)),
						NOT_ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL, FORVENTET_ANTALL_9, makeMvAreaSet(NOT_ELECTRONIC_MARKOFF, MV_AREA_PK_0)},
				{asList(makeVoteCount(1L, FINAL, APPROVED, VO, MV_AREA_PK_0),
						makeVoteCount(2L, FINAL, APPROVED, VS, MV_AREA_PK_0),
						makeVoteCount(4L, FINAL, APPROVED, VB, MV_AREA_PK_0)),
						NOT_ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL, FORVENTET_ANTALL_0, makeMvAreaSet(NOT_ELECTRONIC_MARKOFF, MV_AREA_PK_0)},
				{asList(makeVoteCount(71L, FINAL, APPROVED, VO, MV_AREA_PK_0),
						makeVoteCount(2L, FINAL, APPROVED, VS, MV_AREA_PK_0),
						makeVoteCount(3L, FINAL, APPROVED, VF, MV_AREA_PK_0)),
						NOT_ELECTRONIC_MARKOFF, ON_BOROUGH_LEVEL, FORVENTET_ANTALL_9, makeMvAreaSet(NOT_ELECTRONIC_MARKOFF, MV_AREA_PK_0)},
				{asList(makeVoteCount(1L, FINAL, APPROVED, VO, MV_AREA_PK_0),
						makeVoteCount(2L, FINAL, APPROVED, VS, MV_AREA_PK_0),
						makeVoteCount(3L, FINAL, APPROVED, VF, MV_AREA_PK_0),
						makeVoteCount(5L, FINAL, APPROVED, BF, MV_AREA_PK_0)),
						NOT_ELECTRONIC_MARKOFF, ON_BOROUGH_LEVEL, FORVENTET_ANTALL_12, makeMvAreaSet(NOT_ELECTRONIC_MARKOFF, MV_AREA_PK_0)},
		};

	}

	@Test
	public void tellingerForRapportering_ballotCountsPerBallotId() {
		ContestReport contestReport = makeContestReport();
		List<BallotCount> tellinger = contestReport.tellingerForRapportering(makeQualifiers(PRELIMINARY), makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0),
				makeStatuses(), CountingMode.CENTRAL, CountingMode.CENTRAL);

		Map<CountCategory, List<BallotCount>> result = tellinger.stream().collect(groupingBy(ballotCount -> ballotCount.getVoteCount().getCountCategory()));
		assertThat(result).containsOnlyKeys(FO, VO);
		assertThat(result.get(FO)).hasSize(BALLOT_COUNTS_FOR_FO_IN_VOTE_COUNT_SET);
		assertThat(result.get(VO)).hasSize(BALLOT_COUNTS_FOR_VO_IN_VOTE_COUNT_SET);
	}

	private ContestReport makeContestReport() {
		ContestReport contestReport = new ContestReport();
		contestReport.setVoteCountSet(makeVoteCountSet());
		contestReport.setContest(makeMockContest(ELECTRONIC_MARKOFF, NOT_ON_BOROUGH_LEVEL));
		return contestReport;
	}

	private Set<VoteCount> makeVoteCountSet() {
		HashSet<VoteCount> voteCounts = new HashSet<>();
		voteCounts.add(makeVoteCount(1L, PRELIMINARY, APPROVED, FO, MV_AREA_PK_0));
		voteCounts.add(makeVoteCount(2L, PRELIMINARY, TO_SETTLEMENT, VO, MV_AREA_PK_0));
		voteCounts.add(makeVoteCount(3L, PRELIMINARY, NEW, VB, MV_AREA_PK_0)); // skal filtreres bort
		voteCounts.add(makeVoteCount(4L, PRELIMINARY, REVOKED, VF, MV_AREA_PK_0)); // skal filtreres bort
		voteCounts.add(makeVoteCount(5L, PRELIMINARY, SAVED, VS, MV_AREA_PK_0)); // skal filtreres bort
		voteCounts.add(makeVoteCount(6L, PROTOCOL, APPROVED, VO, MV_AREA_PK_0)); // skal filtreres bort
		voteCounts.add(makeVoteCount(7L, FINAL, TO_SETTLEMENT, FO, MV_AREA_PK_0)); // skal filtreres bort
		voteCounts.add(makeVoteCount(8L, PRELIMINARY, APPROVED, VO, MV_AREA_PK_0));
		voteCounts.add(makeVoteCount(9L, PRELIMINARY, APPROVED, FS, MV_AREA_PK_0)); // skal filtreres bort
		voteCounts.add(makeVoteCount(10L, PRELIMINARY, APPROVED, FO, MV_AREA_PK_1)); // skal filtreres bort (feil mvArea)
		voteCounts.add(makeVoteCount(11L, PRELIMINARY, APPROVED, VF, MV_AREA_PK_0)); // skal filtreres bort
		return voteCounts;
	}

	@Test
	public void finnesRapporterbareTellingerForAlle_alleKretserHarEnGodkjentTelling_true() {
		Set<MvArea> tekniskeKretser = makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0, MV_AREA_PK_1);
		ContestReport contestReport = makeContestReport(tekniskeKretser);

		assertThat(contestReport.finnesRapporterbareTellingerForAlle(tekniskeKretser, PRELIMINARY)).isTrue();
	}

	private ContestReport makeContestReport(Set<MvArea> tekniskeKretser) {
		ContestReport contestReport = new ContestReport();
		for (MvArea tekniskKrets : tekniskeKretser) {
			contestReport.add(makeVoteCount(tekniskKrets.getPk(), PRELIMINARY, APPROVED, FO, tekniskKrets));
		}
		return contestReport;
	}

	private VoteCount makeVoteCount(long pk, no.valg.eva.admin.common.counting.model.CountQualifier preliminary, CountStatus status,
									CountCategory countCategory, MvArea mvArea) {
		VoteCount voteCount = new VoteCount();
		voteCount.setPk(pk);
		voteCount.setCountQualifier(countQualifier(preliminary));
		voteCount.setBallotCountSet(makeBallotCountSet(voteCount));
		voteCount.setVoteCountStatus(voteCountStatus(status));
		voteCount.setVoteCountCategory(voteCountCategory(countCategory));
		voteCount.setMvArea(mvArea);
		return voteCount;
	}

	@Test
	public void finnesRapporterbareTellingerForAlle_ikkeAlleKretserHarEnGodkjentTelling_false() {
		Set<MvArea> tekniskeKretser = makeMvAreaSet(ELECTRONIC_MARKOFF, MV_AREA_PK_0, MV_AREA_PK_1);
		ContestReport contestReport = makeContestReport(tekniskeKretser);
		tekniskeKretser.add(makeMvArea(MV_AREA_PK_2, ELECTRONIC_MARKOFF));
		assertThat(contestReport.finnesRapporterbareTellingerForAlle(tekniskeKretser, PRELIMINARY)).isFalse();
	}

	private ContestReport contestReport(VoteCount... voteCounts) {

		if (voteCounts.length == 0) {
			MvArea mvArea1 = mvArea(1, AREA_PATH_1);
			MvArea mvArea2 = mvArea(2, AREA_PATH_2);
			savedFinalVoteCountNo1ForVoAndArea1 = voteCount(1L, FINAL, VO, 1, SAVED, mvArea1);
			approvedFinalVoteCountNo1ForVoAndArea2 = voteCount(2L, FINAL, VO, 1, APPROVED, mvArea2);
			VoteCount approvedPreliminaryVoteCountNo1ForVoAndArea1 = voteCount(3L, PRELIMINARY, VO, 1, APPROVED, mvArea1);
			approvedPreliminaryVoteCountNo1ForFoAndArea1 = voteCount(4L, PRELIMINARY, FO, 1, APPROVED, mvArea1);
			approvedFinalVoteCountNo1ForFoAndArea1 = voteCount(5L, FINAL, FO, 1, APPROVED, mvArea1);
			approvedFinalVoteCountNo2ForFoAndArea2 = voteCount(6L, FINAL, VO, 2, APPROVED, mvArea1);
			return contestReport(
					savedFinalVoteCountNo1ForVoAndArea1,
					approvedFinalVoteCountNo1ForVoAndArea2,
					approvedPreliminaryVoteCountNo1ForVoAndArea1,
					approvedPreliminaryVoteCountNo1ForFoAndArea1,
					approvedFinalVoteCountNo1ForFoAndArea1,
					approvedFinalVoteCountNo2ForFoAndArea2);
		}

		Set<VoteCount> voteCountSet = new LinkedHashSet<>();
		Collections.addAll(voteCountSet, voteCounts);
		contestReportField.setVoteCountSet(voteCountSet);
		return contestReportField;
	}

	private MvArea mvArea(long mvAreaPk, AreaPath areaPath) {
		MvArea area = makeMvArea(mvAreaPk, ELECTRONIC_MARKOFF);
		area.setAreaPath(areaPath.path());
		return area;
	}

}
