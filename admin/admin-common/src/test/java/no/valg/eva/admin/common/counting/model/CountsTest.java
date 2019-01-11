package no.valg.eva.admin.common.counting.model;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;



public class CountsTest extends MockUtilsTestCase {
	public static final String CONTEST_PATH = "111111.22.33.444444";
	public static final String AREA_PATH = "111111.22.33.4444.555555.6666";
    public static final String ELECTION_NAME = "electionName";
    public static final String CONTEST_NAME = "contestName";
    public static final boolean PENULTIMATE_RECOUNT_TRUE = true;
	private static final boolean PENULTIMATE_RECOUNT_FALSE = false;
	public static final String MUNICIPALITY_NAME = "municipalityName";

	@Test
	public void getContext_always_returnsTheContext() throws Exception {
		CountContext context = getCountContext();
		Counts counts = new Counts(context, ELECTION_NAME, CONTEST_NAME, PENULTIMATE_RECOUNT_TRUE, MUNICIPALITY_NAME, false);

		assertThat(counts.getContext()).isSameAs(context);
	}

    @Test
    public void municipalityCountsFinal_when_penultimateRecount() {
        Counts counts = new Counts(getCountContext(), ELECTION_NAME, CONTEST_NAME, PENULTIMATE_RECOUNT_TRUE, MUNICIPALITY_NAME, false);
        
        assertThat(counts.municipalityCountsFinal()).isTrue();
    }
    
	@Test
	public void getProtocolCountsReturnsWhatSetProtocolCountsSet() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		List<ProtocolCount> protocolCounts = asList(protocolCount(), protocolCount());
		counts.setProtocolCounts(protocolCounts);

		List<ProtocolCount> actualProtocolCounts = counts.getProtocolCounts();

		assertThat(actualProtocolCounts).isEqualTo(protocolCounts);
	}

	@Test
	public void isProtocolCountsApprovedReturnsTrueWhenAllProtocolCountsIsApproved() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubProtocolCount1 = protocolCount();
		ProtocolCount stubProtocolCount2 = protocolCount();
		List<ProtocolCount> protocolCounts = asList(stubProtocolCount1, stubProtocolCount2);
		counts.setProtocolCounts(protocolCounts);

		when(stubProtocolCount1.isApproved()).thenReturn(true);
		when(stubProtocolCount2.isApproved()).thenReturn(true);

		boolean hasApprovedProtocolCounts = counts.isProtocolCountsApproved();

		assertThat(hasApprovedProtocolCounts).isTrue();
	}

	@Test
	public void isProtocolCountsApprovedReturnsFalseWhenOneProtocolCountsIsNotApproved() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubProtocolCount1 = protocolCount();
		ProtocolCount stubProtocolCount2 = protocolCount();
		List<ProtocolCount> protocolCounts = asList(stubProtocolCount1, stubProtocolCount2);
		counts.setProtocolCounts(protocolCounts);

		when(stubProtocolCount1.isApproved()).thenReturn(false);
		when(stubProtocolCount2.isApproved()).thenReturn(true);

		boolean hasApprovedProtocolCounts = counts.isProtocolCountsApproved();

		assertThat(hasApprovedProtocolCounts).isFalse();
	}

	@Test
	public void isProtocolCountsApprovedReturnsFalseWhenAllProtocolCountsIsNotApproved() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubProtocolCount1 = protocolCount();
		ProtocolCount stubProtocolCount2 = protocolCount();
		List<ProtocolCount> protocolCounts = asList(stubProtocolCount1, stubProtocolCount2);
		counts.setProtocolCounts(protocolCounts);

		when(stubProtocolCount1.isApproved()).thenReturn(false);
		when(stubProtocolCount2.isApproved()).thenReturn(false);

		boolean hasApprovedProtocolCounts = counts.isProtocolCountsApproved();

		assertThat(hasApprovedProtocolCounts).isFalse();
	}

	@Test
	public void getFirstProtocolCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount protocolCount = protocolCount();
		List<ProtocolCount> protocolCounts = asList(protocolCount, protocolCount());
		counts.setProtocolCounts(protocolCounts);

		ProtocolCount firstProtocolCount = counts.getFirstProtocolCount();

		assertThat(firstProtocolCount).isSameAs(protocolCount);
	}

	@Test
	public void setFirstProtocolCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount firstProtocolCount = protocolCount();

		counts.setFirstProtocolCount(firstProtocolCount);

		assertThat(counts.getProtocolCounts()).containsExactly(firstProtocolCount);
	}

	@Test
	public void getPreliminaryCountReturnsWhatSetPreliminaryCountSet() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount preliminaryCount = preliminaryCount();
		counts.setPreliminaryCount(preliminaryCount);

		PreliminaryCount actualPreliminaryCount = counts.getPreliminaryCount();

		assertThat(actualPreliminaryCount).isSameAs(preliminaryCount);
	}

	@Test
	public void updateProtocolAndPreliminaryCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount protocolCount = protocolCount();
		PreliminaryCount preliminaryCount = preliminaryCount();

		ProtocolAndPreliminaryCount protocolAndPreliminaryCount = ProtocolAndPreliminaryCount.from(protocolCount, preliminaryCount);

		counts.setFirstProtocolCount(protocolCount);
		counts.setPreliminaryCount(preliminaryCount);
		counts.updateProtocolAndPreliminaryCount();

		assertThat(counts.getProtocolAndPreliminaryCount()).isEqualTo(protocolAndPreliminaryCount);
	}

	@Test
	public void setFinalCountsAndUpdateActiveCount() {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		List<FinalCount> finalCounts = asList(finalCount("FVO1", CountStatus.REVOKED), finalCount("FVO2", CountStatus.APPROVED));
		counts.setFinalCountsAndUpdateActiveCount(finalCounts);

		assertThat(counts.getFinalCountIndex()).isEqualTo(1);
	}

	@Test
	public void setCountyFinalCountsAndUpdateActiveCount() {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		List<FinalCount> finalCounts = asList(finalCount("FVO1", CountStatus.REVOKED), finalCount("FVO2", CountStatus.APPROVED));
		counts.setCountyFinalCountsAndUpdateActiveCount(finalCounts);

		assertThat(counts.getCountyFinalCountIndex()).isEqualTo(1);
	}

	private FinalCount finalCount(String id, CountStatus status) {
		FinalCount finalCount = new FinalCount(id, null, VO, "", ReportingUnitTypeId.FYLKESVALGSTYRET, "", true);
		finalCount.setStatus(status);
		return finalCount;
	}

	@Test
	public void getFinalCountIndexReturnsWhatSetFinalCountIndexSets() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setFinalCountIndex(1);

		int finalCountIndex = counts.getFinalCountIndex();

		assertThat(finalCountIndex).isEqualTo(1);
	}

	@Test
	public void getActiveCountyFinalCountIndexReturnsWhatSetFinalCountIndexSets() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setCountyFinalCountIndex(1);

		int activeCountyFinalCountIndex = counts.getCountyFinalCountIndex();

		assertThat(activeCountyFinalCountIndex).isEqualTo(1);
	}

	@Test
	public void getMarkOffCountShouldReturnMarkOffCountFromProtocolCountsWhenTheyExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubProtocolCount1 = getProtocolCountWithDailyMarkOffs();
		ProtocolCount stubProtocolCount2 = getProtocolCountWithDailyMarkOffs();
		counts.setProtocolCounts(asList(stubProtocolCount1, stubProtocolCount2));

		when(stubProtocolCount1.getDailyMarkOffCounts().getMarkOffCount()).thenReturn(11);
		when(stubProtocolCount2.getDailyMarkOffCounts().getMarkOffCount()).thenReturn(13);

		int markOffCount = counts.getMarkOffCount();

		assertThat(markOffCount).isEqualTo(24);
	}

	private ProtocolCount getProtocolCountWithDailyMarkOffs() {
		ProtocolCount stubProtocolCount = protocolCount();
		when(stubProtocolCount.getDailyMarkOffCounts()).thenReturn(mock(DailyMarkOffCounts.class));
		return stubProtocolCount;
	}

	@Test
	public void getMarkOffCountShouldReturnMarkOffCountFromPreliminaryCountWhenNoProtocolCountsExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		counts.setPreliminaryCount(stubPreliminaryCount);

		when(stubPreliminaryCount.getMarkOffCount()).thenReturn(11);

		int markOffCount = counts.getMarkOffCount();

		assertThat(markOffCount).isEqualTo(11);
	}

	@Test
	public void getMarkOffCount_withPreliminaryCountDailyMarkOffCounts_returnsDailyMarkOffCounts() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount preliminaryCount = createMock(PreliminaryCount.class);
		counts.setPreliminaryCount(preliminaryCount);
		when(preliminaryCount.getDailyMarkOffCounts().getMarkOffCount()).thenReturn(100);

		int markOffCount = counts.getMarkOffCount();

		assertThat(markOffCount).isEqualTo(100);
	}

	@Test
	public void hasProtocolCountsShouldReturnTrueWhenProtocolCountsExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setProtocolCounts(asList(protocolCount()));

		boolean hasProtocolCounts = counts.hasProtocolCounts();

		assertThat(hasProtocolCounts).isTrue();
	}

	@Test
	public void hasProtocolCountsShouldReturnFalseWhenNoProtocolCountsExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);

		boolean hasProtocolCounts = counts.hasProtocolCounts();

		assertThat(hasProtocolCounts).isFalse();
	}

	@Test
	public void hasPreliminaryCountShouldReturnTrueWhenPreliminaryCountExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setPreliminaryCount(preliminaryCount());

		boolean hasPreliminaryCount = counts.hasPreliminaryCount();

		assertThat(hasPreliminaryCount).isTrue();
	}

	@Test
	public void hasPreliminaryCountShouldReturnFalseWhenNoPreliminaryCountsExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		boolean hasPreliminaryCount = counts.hasPreliminaryCount();

		assertThat(hasPreliminaryCount).isFalse();
	}

	@Test
	public void hasFinalCountsShouldReturnTrueWhenFinalCountsExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setFinalCountsAndUpdateActiveCount(asList(finalCount()));

		boolean hasFinalCounts = counts.hasFinalCounts();

		assertThat(hasFinalCounts).isTrue();
	}

	@Test
	public void hasCountyFinalCountsShouldReturnTrueWhenCountyFinalCountsExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(finalCount()));

		boolean hasCountyFinalCounts = counts.hasCountyFinalCounts();

		assertThat(hasCountyFinalCounts).isTrue();
	}

	@Test
	public void hasFinalCountsShouldReturnFalseWhenNoFinalCountsExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		boolean hasFinalCounts = counts.hasFinalCounts();

		assertThat(hasFinalCounts).isFalse();
	}

	@Test
	public void hasCountyFinalCountsShouldReturnFalseWhenNoCountyFinalCountsExists() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		boolean hasCountyFinalCounts = counts.hasCountyFinalCounts();

		assertThat(hasCountyFinalCounts).isFalse();
	}

	@Test
	public void getOrdinaryBallotCountDifferenceBetweenOneProtocolCountAndPreliminaryCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubProtocolCount = protocolCount();
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		counts.setFirstProtocolCount(stubProtocolCount);
		counts.setPreliminaryCount(stubPreliminaryCount);

		when(stubProtocolCount.getOrdinaryBallotCount()).thenReturn(11);
		when(stubPreliminaryCount.getOrdinaryBallotCount()).thenReturn(13);

		int difference = counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY);

		assertThat(difference).isEqualTo(2);
	}

	@Test
	public void getOrdinaryBallotCountDifferenceBetweenManyProtocolCountsAndPreliminaryCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubProtocolCount1 = protocolCount();
		ProtocolCount stubProtocolCount2 = protocolCount();
		ProtocolCount stubProtocolCount3 = protocolCount();
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		counts.setProtocolCounts(asList(stubProtocolCount1, stubProtocolCount2, stubProtocolCount3));
		counts.setPreliminaryCount(stubPreliminaryCount);

		// integration
		when(stubProtocolCount1.getOrdinaryBallotCount()).thenReturn(11);
		when(stubProtocolCount2.getOrdinaryBallotCount()).thenReturn(13);
		when(stubProtocolCount3.getOrdinaryBallotCount()).thenReturn(17);
		when(stubPreliminaryCount.getOrdinaryBallotCount()).thenReturn(37);

		int difference = counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY);

		assertThat(difference).isEqualTo(-4);
	}

	@Test
	public void getOrdinaryBallotCountDifferenceBetween_preliminaryCountAndOneFinalCount_returnsDifference() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		FinalCount stubFinalCount1 = finalCount();
		FinalCount stubFinalCount2 = finalCount();
		counts.setPreliminaryCount(stubPreliminaryCount);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount1, stubFinalCount2));

		when(stubPreliminaryCount.getOrdinaryBallotCount()).thenReturn(17);
		when(stubFinalCount1.getId()).thenReturn("EVO1");
		when(stubFinalCount1.getOrdinaryBallotCount()).thenReturn(11);
		when(stubFinalCount2.getId()).thenReturn("EVO2");
		when(stubFinalCount2.getOrdinaryBallotCount()).thenReturn(19);

		int difference = counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, "EVO1");

		assertThat(difference).isEqualTo(-6);
	}

	@Test
	public void getOrdinaryBallotCountDifferenceBetween_preliminaryCountAndOneCountyFinalCount_returnsDifference() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_FALSE);
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		FinalCount stubFinalCount1 = finalCount();
		FinalCount stubFinalCount2 = finalCount();
		counts.setPreliminaryCount(stubPreliminaryCount);

		when(stubPreliminaryCount.getOrdinaryBallotCount()).thenReturn(17);
		when(stubFinalCount1.getId()).thenReturn("EVO1");
		when(stubFinalCount1.getOrdinaryBallotCount()).thenReturn(11);
		when(stubFinalCount2.getId()).thenReturn("EVO2");
		when(stubFinalCount2.getOrdinaryBallotCount()).thenReturn(19);
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubFinalCount1, stubFinalCount2));

		int difference = counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, "EVO1");

		assertThat(difference).isEqualTo(-6);
	}

	@Test
	public void getCountyOrdinaryBallotCountDifferenceBetweenMunicipalityCountAndOneFinalCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinalCount = finalCount();
		FinalCount stubCountyFinalCount = finalCount();
		when(stubFinalCount.getIndex()).thenReturn(1);
		when(stubCountyFinalCount.getIndex()).thenReturn(1);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubCountyFinalCount));

		when(stubFinalCount.getId()).thenReturn("EVO1");
		when(stubFinalCount.getOrdinaryBallotCount()).thenReturn(11);
		when(stubCountyFinalCount.getId()).thenReturn("ETO1");
		when(stubCountyFinalCount.getOrdinaryBallotCount()).thenReturn(19);

		int difference = counts.getCountyOrdinaryBallotCountDifference("ETO1");

		assertThat(difference).isEqualTo(8);
	}

	@Test
	public void getOrdinaryBallotCountDifferenceBetweenPreliminaryCountAndAnotherFinalCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		FinalCount stubFinalCount1 = finalCount();
		FinalCount stubFinalCount2 = finalCount();
		counts.setPreliminaryCount(stubPreliminaryCount);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount1, stubFinalCount2));

		when(stubPreliminaryCount.getOrdinaryBallotCount()).thenReturn(17);
		when(stubFinalCount1.getId()).thenReturn("EVO1");
		when(stubFinalCount1.getOrdinaryBallotCount()).thenReturn(11);
		when(stubFinalCount2.getId()).thenReturn("EVO2");
		when(stubFinalCount2.getOrdinaryBallotCount()).thenReturn(19);

		int difference = counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, "EVO2");

		assertThat(difference).isEqualTo(2);
	}

	@Test
	public void getCountyOrdinaryBallotCountDifferenceBetweenMunicipalityCountAndAnotherFinalCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinalCount = finalCount();
		FinalCount stubCountyFinalCount1 = finalCount();
		FinalCount stubCountyFinalCount2 = finalCount();
		when(stubFinalCount.getIndex()).thenReturn(1);
		when(stubCountyFinalCount1.getIndex()).thenReturn(1);
		when(stubCountyFinalCount2.getIndex()).thenReturn(2);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubCountyFinalCount1, stubCountyFinalCount2));

		when(stubFinalCount.getId()).thenReturn("ETO1");
		when(stubFinalCount.getOrdinaryBallotCount()).thenReturn(5);
		when(stubCountyFinalCount1.getId()).thenReturn("EVO1");
		when(stubCountyFinalCount1.getOrdinaryBallotCount()).thenReturn(11);
		when(stubCountyFinalCount2.getId()).thenReturn("EVO2");
		when(stubCountyFinalCount2.getOrdinaryBallotCount()).thenReturn(19);

		int difference = counts.getCountyOrdinaryBallotCountDifference("EVO2");

		assertThat(difference).isEqualTo(14);
	}

	@Test
	public void getOrdinaryBallotCountDifferenceBetweenTwoFinalCounts() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinalCount1 = finalCount();
		FinalCount stubFinalCount2 = finalCount();
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount1, stubFinalCount2));

		when(stubFinalCount1.getId()).thenReturn("EVO1");
		when(stubFinalCount1.getOrdinaryBallotCount()).thenReturn(11);
		when(stubFinalCount2.getId()).thenReturn("EVO2");
		when(stubFinalCount2.getOrdinaryBallotCount()).thenReturn(19);

		int difference = counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.FINAL, CountQualifier.FINAL, "EVO1", "EVO2");

		assertThat(difference).isEqualTo(8);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected one final count id required, but got none")
	public void getOrdinaryBallotCountDifferenceBetweenPreliminaryCountAndAnotherFinalCountShouldThrowExceptionWhenMissingFinalCountId() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected two final count ids required, but got only one")
	public void getOrdinaryBallotCountDifferenceBetweenTwoFinalCountsShouldThrowExceptionWhenMissingOneFinalCountId() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.FINAL, CountQualifier.FINAL, "EVO1");
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "expected two final count ids required, but got none")
	public void getOrdinaryBallotCountDifferenceBetweenTwoFinalCountsShouldThrowExceptionWhenMissingFinalCountIds() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.FINAL, CountQualifier.FINAL);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "unknown final count id: <.+>")
	public void getOrdinaryBallotCountDifferenceBetweenShouldThrowExceptionWhenUnknownFinalCountId() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinalCount = finalCount();
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));

		when(stubFinalCount.getId()).thenReturn("EVO1");

		counts.getOrdinaryBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, "EVO9");
	}

	@Test
	public void getBlankBallotCountForProtocolCounts_whenAllBlankBallotCountsInProtocolCountsAreSet_returnsSumOfBlankBallotCounts() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setProtocolCounts(asList(
				createProtocolCountWithBlankBallotCount(1),
				createProtocolCountWithBlankBallotCount(2)));

		int blankBallotCountForProtocolCounts = counts.getBlankBallotCountForProtocolCounts();

		assertThat(blankBallotCountForProtocolCounts).isEqualTo(3);
	}

	@Test
	public void getBlankBallotCountForProtocolCounts_whenABlankBallotCountIsNull_itIsCountedAsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setProtocolCounts(asList(
				createProtocolCountWithBlankBallotCount(1),
				createProtocolCountWithBlankBallotCount(null)));

		int blankBallotCountForProtocolCounts = counts.getBlankBallotCountForProtocolCounts();

		assertThat(blankBallotCountForProtocolCounts).isEqualTo(1);
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "combination of qualifier <PRELIMINARY> and <PRELIMINARY> not allowed")
	public void getBlankBallotCountDifferenceBetween_withInvalidQualifiers_throwsException() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.getBlankBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.PRELIMINARY);
	}

	@Test
	public void getBlankBallotCountDifferenceBetween_withOnePreliminaryCountWithHigerValueThanProtocolCount_returnsPositiveDifference() throws Exception {
		testBlankBallotCountDifferenceBetweenForProtocolAndPreliminaryCount(3, 1, 2);
	}

	@Test
	public void getBlankBallotCountDifferenceBetween_withOnePreliminaryCountWithLowerValueThanProtocolCount_returnsNegativeDifference() throws Exception {
		testBlankBallotCountDifferenceBetweenForProtocolAndPreliminaryCount(1, 3, -2);
	}

	@Test
	public void getBlankBallotCountDifferenceBetween_whenBlankBallotCountsIsNullForAProtocolCount_itIsCountedAsZero() throws Exception {
		testBlankBallotCountDifferenceBetweenForProtocolAndPreliminaryCount(1, null, 1);
	}

	@Test
	public void getBlankBallotCountDifferenceBetween_whenBlankBallotCountsIsNullForAPreliminaryCount_itIsCountedAsZero() throws Exception {
		testBlankBallotCountDifferenceBetweenForProtocolAndPreliminaryCount(null, 1, -1);
	}

	@Test
	public void getBlankBallotCountDifferenceBetween_whenBlankBallotCountsIsNullForAFinalCount_itIsCountedAsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		FinalCount stubFinalCount = finalCount();
		counts.setPreliminaryCount(stubPreliminaryCount);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));

		when(stubPreliminaryCount.getBlankBallotCount()).thenReturn(1);
		when(stubFinalCount.getBlankBallotCount()).thenReturn(null);
		when(stubFinalCount.getId()).thenReturn("someFinalCountId");

		int difference = counts.getBlankBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, stubFinalCount.getId());

		assertThat(difference).isEqualTo(-1);
	}

	@Test
	public void getBlankBallotCountDifferenceBetween_municipalityOnlyCountsPreliminaryAndBlankBallotCountsIsNullForAFinalCount_itIsCountedAsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_FALSE);
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		FinalCount stubFinalCount = finalCount();
		counts.setPreliminaryCount(stubPreliminaryCount);
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubFinalCount));

		when(stubPreliminaryCount.getBlankBallotCount()).thenReturn(1);
		when(stubFinalCount.getBlankBallotCount()).thenReturn(null);
		when(stubFinalCount.getId()).thenReturn("someFinalCountId");

		int difference = counts.getBlankBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, stubFinalCount.getId());

		assertThat(difference).isEqualTo(-1);
	}

	@Test
	public void getCountyBlankBallotCountDifference_whenBlankBallotCountsIsNullForAFinalCount_itIsCountedAsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinalCount = finalCount();
		FinalCount stubCountyFinalCount = finalCount();
		when(stubFinalCount.getIndex()).thenReturn(1);
		when(stubCountyFinalCount.getIndex()).thenReturn(1);

		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubCountyFinalCount));

		when(stubFinalCount.getBlankBallotCount()).thenReturn(1);
		when(stubCountyFinalCount.getBlankBallotCount()).thenReturn(null);
		when(stubCountyFinalCount.getId()).thenReturn("someFinalCountId");

		int difference = counts.getCountyBlankBallotCountDifference(stubCountyFinalCount.getId());

		assertThat(difference).isEqualTo(-1);
	}

	@Test
	public void getBlankBallotCountDifferenceBetween_whenFinalCountAndBlankBallotCountsIsNullForAPreliminaryCount_itIsCountedAsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		FinalCount stubFinalCount = finalCount();
		counts.setPreliminaryCount(stubPreliminaryCount);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));

		when(stubPreliminaryCount.getBlankBallotCount()).thenReturn(null);
		when(stubFinalCount.getBlankBallotCount()).thenReturn(1);
		when(stubFinalCount.getId()).thenReturn("someFinalCountId");

		int difference = counts.getBlankBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, stubFinalCount.getId());

		assertThat(difference).isEqualTo(1);
	}

	@Test
	public void getCountyBlankBallotCountDifference_whenFinalCountAndBlankBallotCountsIsNullForAMunicipalityFinalCount_itIsCountedAsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinalCount = finalCount();
		FinalCount stubCountyFinalCount = finalCount();
		when(stubFinalCount.getIndex()).thenReturn(1);
		when(stubCountyFinalCount.getIndex()).thenReturn(1);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubCountyFinalCount));

		when(stubFinalCount.getBlankBallotCount()).thenReturn(null);
		when(stubCountyFinalCount.getBlankBallotCount()).thenReturn(1);
		when(stubCountyFinalCount.getId()).thenReturn("someFinalCountId");

		int difference = counts.getCountyBlankBallotCountDifference(stubCountyFinalCount.getId());

		assertThat(difference).isEqualTo(1);
	}

	@Test
	public void getBlankBallotCountDifferenceBetween_whenBlankBallotCountsIsNullForTwoFinalCounts_itIsCountedAsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinalCount1 = finalCount();
		FinalCount stubFinalCount2 = finalCount();
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount1, stubFinalCount2));

		when(stubFinalCount1.getBlankBallotCount()).thenReturn(null);
		when(stubFinalCount1.getId()).thenReturn("someFinalCountId1");
		when(stubFinalCount2.getBlankBallotCount()).thenReturn(null);
		when(stubFinalCount2.getId()).thenReturn("someFinalCountId2");

		int difference = counts.getBlankBallotCountDifferenceBetween(CountQualifier.FINAL, CountQualifier.FINAL, stubFinalCount1.getId(),
				stubFinalCount2.getId());

		assertThat(difference).isEqualTo(0);
	}

	@Test
	public void getCountyBlankBallotCountDifference_whenBlankBallotCountsIsNullBothFinalCounts_itIsCountedAsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinalCount = finalCount();
		FinalCount stubCountyFinalCount = finalCount();
		when(stubFinalCount.getIndex()).thenReturn(1);
		when(stubCountyFinalCount.getIndex()).thenReturn(1);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubCountyFinalCount));

		when(stubFinalCount.getBlankBallotCount()).thenReturn(null);
		when(stubFinalCount.getId()).thenReturn("someFinalCountId1");
		when(stubCountyFinalCount.getBlankBallotCount()).thenReturn(null);
		when(stubCountyFinalCount.getId()).thenReturn("someFinalCountId2");

		int difference = counts.getCountyBlankBallotCountDifference(stubCountyFinalCount.getId());

		assertThat(difference).isEqualTo(0);
	}

	@Test
	public void getQuestionableBallotCountForProtocolCounts_whenQuestionableBallotCountIsNullForAProtocolCount_itIsCountedAsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubProtocolCount1 = protocolCount();
		when(stubProtocolCount1.getQuestionableBallotCount()).thenReturn(1);
		ProtocolCount stubProtocolCount2 = protocolCount();
		when(stubProtocolCount2.getQuestionableBallotCount()).thenReturn(null);
		counts.setProtocolCounts(asList(stubProtocolCount1, stubProtocolCount2));

		int questionableBallotCountForProtocolCounts = counts.getQuestionableBallotCountForProtocolCounts();

		assertThat(questionableBallotCountForProtocolCounts).isEqualTo(1);
	}

	@Test
	public void getQuestionableBallotCountDifferenceBetween_preliminaryCountWithHigherValueThanProtocolCount_returnsPositiveDifference() throws Exception {
		testGetQuestionableBallotCountDifferenceBetween(1, 3, 2);
	}

	@Test
	public void getQuestionableBallotCountDifferenceBetween_preliminaryCountWithLowerValueThanProtocolCount_returnsNegativeDifference() throws Exception {
		testGetQuestionableBallotCountDifferenceBetween(3, 1, -2);
	}

	@Test
	public void getQuestionableBallotCountDifferenceBetween_protocolCountAndPreliminaryCountWithNullValue_countsNullAsZero() throws Exception {
		testGetQuestionableBallotCountDifferenceBetween(1, null, -1);
	}

	@Test
	public void getQuestionableBallotCountDifferenceBetween_protocolCountWithNullValueAndPreliminaryCount_countsNullAsZero() throws Exception {
		testGetQuestionableBallotCountDifferenceBetween(null, 1, 1);
	}

	@Test
	public void getQuestionableBallotCountDifferenceBetween_preliminaryCountAndFinalCount_returnsDifferenceBetweenRejectedCountOfFinalAndQuestionableCountForPreliminary()
			throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		FinalCount stubFinalCount = finalCount();
		counts.setPreliminaryCount(stubPreliminaryCount);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));

		when(stubPreliminaryCount.getQuestionableBallotCount()).thenReturn(3);
		when(stubFinalCount.getTotalRejectedBallotCount()).thenReturn(1);
		when(stubFinalCount.getId()).thenReturn("someFinalCountId");

		int difference = counts.getQuestionableBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, stubFinalCount.getId());

		assertThat(difference).isEqualTo(-2);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "expected two final count ids required, but got none")
	public void getQuestionableBallotCountDifferenceBetween_finalFinalWithNoIds_throwsException() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.getQuestionableBallotCountDifferenceBetween(CountQualifier.FINAL, CountQualifier.FINAL);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "expected two final count ids required, but got only one")
	public void getQuestionableBallotCountDifferenceBetween_finalFinalWithOneId_throwsException() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.getQuestionableBallotCountDifferenceBetween(CountQualifier.FINAL, CountQualifier.FINAL, "test");
	}

	@Test
	public void getQuestionableBallotCountDifferenceBetween_finalFinal_returnDiff() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stub1 = whenFinalCount("id1", 1);
		FinalCount stub2 = whenFinalCount("id2", 2);
		FinalCount stub3 = whenFinalCount("id3", 3);
		counts.setFinalCountsAndUpdateActiveCount(asList(stub1, stub2, stub3));
		when(stub1.getTotalRejectedBallotCount()).thenReturn(10);
		when(stub3.getTotalRejectedBallotCount()).thenReturn(17);
		int diff = counts.getQuestionableBallotCountDifferenceBetween(CountQualifier.FINAL, CountQualifier.FINAL, "id1", "id3");
		assertThat(diff).isEqualTo(7);
	}

	@Test
	public void getCountyQuestionableBallotCountDifference_municipalityFinalCountAndCountyFinalCount_returnsDifferenceTotalRejected() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinalCount = finalCount();
		FinalCount stubCountyFinalCount = finalCount();
		when(stubFinalCount.getIndex()).thenReturn(1);
		when(stubCountyFinalCount.getIndex()).thenReturn(1);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubCountyFinalCount));

		when(stubFinalCount.getTotalRejectedBallotCount()).thenReturn(3);
		when(stubFinalCount.getId()).thenReturn("someFinalCountId1");
		when(stubCountyFinalCount.getTotalRejectedBallotCount()).thenReturn(1);
		when(stubCountyFinalCount.getId()).thenReturn("someFinalCountId");

		int difference = counts.getCountyQuestionableBallotCountDifference(stubCountyFinalCount.getId());

		assertThat(difference).isEqualTo(-2);
	}

	@Test
	public void getQuestionableBallotCountDifferenceBetween_prelimCountWithNullValueAndFinalCount_returnsDiffBetweenRejCountOfFinalAndQuestiCountForPreliminary()
			throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		FinalCount stubFinalCount = finalCount();
		counts.setPreliminaryCount(stubPreliminaryCount);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinalCount));

		when(stubPreliminaryCount.getQuestionableBallotCount()).thenReturn(null);
		when(stubFinalCount.getTotalRejectedBallotCount()).thenReturn(1);
		when(stubFinalCount.getId()).thenReturn("someFinalCountId");

		int difference = counts.getQuestionableBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, stubFinalCount.getId());

		assertThat(difference).isEqualTo(1);
	}

	@Test
	public void setFirstProtocolCount_withProtocolCount_replacesExistingFirstProtocolcount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		List<ProtocolCount> protocolCounts = new ArrayList<>();
		ProtocolCount protocolCount = getProtocolCount();
		protocolCounts.add(protocolCount);
		counts.setProtocolCounts(protocolCounts);

		ProtocolCount anotherProtocolCount = getProtocolCount();
		counts.setFirstProtocolCount(anotherProtocolCount);

		assertThat(counts.getFirstProtocolCount()).isNotSameAs(protocolCount);
	}

	@Test
	public void setFirstProtocolCount_withProtocolCount_addsFirstProtocolcount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);

		ProtocolCount protocolCount = getProtocolCount();
		counts.setFirstProtocolCount(protocolCount);

		assertThat(counts.getProtocolCounts()).hasSize(1);
		assertThat(counts.getFirstProtocolCount()).isSameAs(protocolCount);
	}

	@Test
	public void getFirstProtocolCount_withoutProtocolCounts_returnsNull() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);

		ProtocolCount protocolCount = counts.getFirstProtocolCount();

		assertThat(protocolCount).isNull();
	}

	@Test
	public void getFirstProtocolCount_withEmptyProtocolCountList_returnsNull() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setProtocolCounts(new ArrayList<ProtocolCount>());

		ProtocolCount protocolCount = counts.getFirstProtocolCount();

		assertThat(protocolCount).isNull();
	}

	@Test
	public void getFirstProtocolCount_withOneProtocolCount_returnsSameProtocolCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		List<ProtocolCount> protocolCounts = new ArrayList<>();
		ProtocolCount protocolCount = getProtocolCount();
		protocolCounts.add(protocolCount);
		counts.setProtocolCounts(protocolCounts);

		ProtocolCount firstProtocolCount = counts.getFirstProtocolCount();

		assertThat(firstProtocolCount).isSameAs(protocolCount);
	}

	@Test
	public void isProtocolCountsNew_withNoData_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		assertThat(counts.isProtocolCountsNew()).isFalse();
	}

	@Test
	public void isProtocolCountsNew_withNotNewCount_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubCount = protocolCount();
		counts.setProtocolCounts(asList(stubCount));

		when(stubCount.isNew()).thenReturn(false);

		assertThat(counts.isProtocolCountsNew()).isFalse();
	}

	@Test
	public void isProtocolCountsNew_withNewCount_returnsTrue() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubCount = protocolCount();
		counts.setProtocolCounts(asList(stubCount));
		when(stubCount.isNew()).thenReturn(true);
		assertThat(counts.isProtocolCountsNew()).isTrue();
	}

	@Test
	public void hasApprovedPreliminaryCount_withNoData_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		assertThat(counts.hasApprovedPreliminaryCount()).isFalse();
	}

	@Test
	public void hasApprovedPreliminaryCount_withPreliminary_returnsPreliminaryState() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stub = preliminaryCount();
		counts.setPreliminaryCount(stub);
		when(stub.isApproved()).thenReturn(true);
		assertThat(counts.hasApprovedPreliminaryCount()).isTrue();
	}

	@Test
	public void hasApprovedFinalCount_withNoData_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		assertThat(counts.hasApprovedFinalCount()).isFalse();
	}

	@Test
	public void hasApprovedFinalCount_withOnlyNonApproved_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stub = finalCount();
		counts.setFinalCountsAndUpdateActiveCount(asList(stub));
		assertThat(counts.hasApprovedFinalCount()).isFalse();
	}

	@Test
	public void hasApprovedFinalCount_withApproved_returnsTrue() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stub1 = finalCount();
		FinalCount stub2 = finalCount();
		counts.setFinalCountsAndUpdateActiveCount(asList(stub1, stub2));
		when(stub2.isApproved()).thenReturn(true);
		assertThat(counts.hasApprovedFinalCount()).isTrue();
	}

	@Test
	public void hasApprovedCountyFinalCount_withNoData_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		assertThat(counts.hasApprovedCountyFinalCount()).isFalse();
	}

	@Test
	public void hasApprovedCountyFinalCount_withOnlyNonApproved_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stub = finalCount();
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stub));
		assertThat(counts.hasApprovedCountyFinalCount()).isFalse();
	}

	@Test
	public void hasApprovedCountyFinalCount_withApproved_returnsTrue() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stub1 = finalCount();
		FinalCount stub2 = finalCount();
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stub1, stub2));
		when(stub2.isApproved()).thenReturn(true);
		assertThat(counts.hasApprovedCountyFinalCount()).isTrue();
	}

	@Test
	public void getTotalBallotCountForProtocolCounts_withNoData_returnsZero() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		int total = counts.getTotalBallotCountForProtocolCounts();
		assertThat(total).isEqualTo(0);
	}

	@Test
	public void getTotalBallotCountForProtocolCounts_withData_returnsSum() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stub1 = protocolCount();
		ProtocolCount stub2 = protocolCount();
		counts.setProtocolCounts(asList(stub1, stub2));
		when(stub1.getTotalBallotCount()).thenReturn(2);
		when(stub2.getTotalBallotCount()).thenReturn(4);
		int total = counts.getTotalBallotCountForProtocolCounts();
		assertThat(total).isEqualTo(6);
	}

	@Test
	public void hasProtocolAndPreliminaryCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setProtocolAndPreliminaryCount(protocolAndPreliminaryCount());
		assertThat(counts.hasProtocolAndPreliminaryCount()).isTrue();
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "combination of qualifier <FINAL> and <PRELIMINARY> not allowed")
	public void getTotalBallotCountDifferenceBetween_withInvalidQualifier_throwsException() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.getTotalBallotCountDifferenceBetween(CountQualifier.FINAL, CountQualifier.PRELIMINARY);
	}

	@Test
	public void getTotalBallotCountDifferenceBetween_withProtocolAndPreliminary_returnsCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stubPreliminary = preliminaryCount();
		when(stubPreliminary.getTotalBallotCount()).thenReturn(10);
		counts.setPreliminaryCount(stubPreliminary);
		ProtocolCount stubProtocol = protocolCount();
		when(stubProtocol.getTotalBallotCount()).thenReturn(16);
		counts.setProtocolCounts(asList(stubProtocol));
		int total = counts.getTotalBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY);
		assertThat(total).isEqualTo(-6);
	}

	@Test
	public void getTotalBallotCountDifferenceBetween_withPreliminaryAndFinal_returnsCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		PreliminaryCount stubPreliminary = preliminaryCount();
		when(stubPreliminary.getTotalBallotCount()).thenReturn(10);
		counts.setPreliminaryCount(stubPreliminary);
		FinalCount stubFinal = whenFinalCount("id1", 1);
		when(stubFinal.getTotalBallotCount()).thenReturn(15);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinal));
		int total = counts.getTotalBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, "id1");
		assertThat(total).isEqualTo(5);
	}

	@Test
	public void getTotalBallotCountDifferenceBetween_withPreliminaryAndCountyFinal_returnsCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_FALSE);
		PreliminaryCount stubPreliminary = preliminaryCount();
		when(stubPreliminary.getTotalBallotCount()).thenReturn(10);
		counts.setPreliminaryCount(stubPreliminary);
		FinalCount stubFinal = whenFinalCount("id1", 1);
		when(stubFinal.getTotalBallotCount()).thenReturn(15);
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubFinal));
		int total = counts.getTotalBallotCountDifferenceBetween(CountQualifier.PRELIMINARY, CountQualifier.FINAL, "id1");
		assertThat(total).isEqualTo(5);
	}

	@Test
	public void getTotalBallotCountDifferenceBetween_withFinalAndFinal_returnsCount() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stub1 = whenFinalCount("id1", 1);
		FinalCount stub2 = whenFinalCount("id2", 2);
		when(stub1.getTotalBallotCount()).thenReturn(15);
		when(stub2.getTotalBallotCount()).thenReturn(25);
		counts.setFinalCountsAndUpdateActiveCount(asList(stub1, stub2));
		int total = counts.getTotalBallotCountDifferenceBetween(CountQualifier.FINAL, CountQualifier.FINAL, "id2", "id1");
		assertThat(total).isEqualTo(-10);
	}

	@Test
	public void getCountyTotalBallotCountDifference() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		FinalCount stubFinal = whenFinalCount("id1", 1);
		when(stubFinal.getTotalBallotCount()).thenReturn(15);
		counts.setFinalCountsAndUpdateActiveCount(asList(stubFinal));
		FinalCount stubCountyFinal = whenFinalCount("id3", 1);
		when(stubCountyFinal.getTotalBallotCount()).thenReturn(25);
		counts.setCountyFinalCountsAndUpdateActiveCount(asList(stubCountyFinal));
		int total = counts.getCountyTotalBallotCountDifference("id3");
		assertThat(total).isEqualTo(10);
	}

	@Test
	public void isPreliminaryCountApproved() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		assertThat(counts.isPreliminaryCountApproved()).isFalse();
		PreliminaryCount stubPrelim = preliminaryCount();
		counts.setPreliminaryCount(stubPrelim);
		assertThat(counts.isPreliminaryCountApproved()).isFalse();
		when(stubPrelim.isApproved()).thenReturn(true);
		assertThat(counts.isPreliminaryCountApproved()).isTrue();
	}

	@Test
	public void isPreliminaryCountNew() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		assertThat(counts.isPreliminaryCountNew()).isFalse();
		PreliminaryCount stubPrelim = preliminaryCount();
		counts.setPreliminaryCount(stubPrelim);
		assertThat(counts.isPreliminaryCountNew()).isFalse();
		when(stubPrelim.isNew()).thenReturn(true);
		assertThat(counts.isPreliminaryCountNew()).isTrue();
	}

	@Test
	void hasApprovedProtocolCounts_withNoProtocolCounts_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		assertThat(counts.hasApprovedProtocolCounts()).isFalse();
	}

	@Test
	void hasApprovedProtocolCounts_withNoApprovedProtocolCounts_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setProtocolCounts(Arrays.asList(protocolCount()));
		assertThat(counts.hasApprovedProtocolCounts()).isFalse();
	}

	@Test
	void hasApprovedProtocolCounts_withOneApprovedProtocolCounts_returnsTrue() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount mock = protocolCount();
		when(mock.isApproved()).thenReturn(true);
		counts.setProtocolCounts(Arrays.asList(mock));
		assertThat(counts.hasApprovedProtocolCounts()).isTrue();
	}

	@Test
	public void hasApprovedProtocolAndPreliminaryCount_withNoCounts_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		assertThat(counts.hasApprovedProtocolAndPreliminaryCount()).isFalse();
	}

	@Test
	public void hasApprovedProtocolAndPreliminaryCount_withNoApprovedCounts_returnsFalse() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		counts.setProtocolAndPreliminaryCount(protocolAndPreliminaryCount());
		assertThat(counts.hasApprovedProtocolAndPreliminaryCount()).isFalse();
	}

	@Test
	public void hasApprovedProtocolAndPreliminaryCount_withApprovedCounts_returnsTrue() throws Exception {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolAndPreliminaryCount mock = protocolAndPreliminaryCount();
		when(mock.isApproved()).thenReturn(true);
		counts.setProtocolAndPreliminaryCount(mock);
		assertThat(counts.hasApprovedProtocolAndPreliminaryCount()).isTrue();
	}

	private ProtocolCount createProtocolCountWithBlankBallotCount(Integer blankBallotCount) {
		ProtocolCount stubProtocolCount = protocolCount();
		when(stubProtocolCount.getBlankBallotCount()).thenReturn(blankBallotCount);
		return stubProtocolCount;
	}

	private ProtocolCount getProtocolCount() {
		return new ProtocolCount("PVO1", AreaPath.from(AREA_PATH), "Område", "Bydel Sagene", true);
	}

	private CountContext getCountContext() {
		return new CountContext(ElectionPath.from(CONTEST_PATH), VO);
	}

	private Counts getCounts(boolean municipalityCountsFinal) {
		CountContext countContext = getCountContext();
		return new Counts(countContext, ELECTION_NAME, CONTEST_NAME, municipalityCountsFinal, MUNICIPALITY_NAME, false);
	}

	private void testBlankBallotCountDifferenceBetweenForProtocolAndPreliminaryCount(Integer blankBallotsForPreliminaryCount,
			Integer blankBallotsForProtocolCount, Integer expectedDifference) {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubProtocolCount = protocolCount();
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		counts.setFirstProtocolCount(stubProtocolCount);
		counts.setPreliminaryCount(stubPreliminaryCount);

		when(stubPreliminaryCount.getBlankBallotCount()).thenReturn(blankBallotsForPreliminaryCount);
		when(stubProtocolCount.getBlankBallotCount()).thenReturn(blankBallotsForProtocolCount);

		int difference = counts.getBlankBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY);

		assertThat(difference).isEqualTo(expectedDifference);
	}

	private void testGetQuestionableBallotCountDifferenceBetween(Integer questionableBallotCountForProtocolCount,
			Integer questionableBallotCountForPreliminaryCount, int expectedDifference) {
		Counts counts = getCounts(PENULTIMATE_RECOUNT_TRUE);
		ProtocolCount stubProtocolCount = protocolCount();
		PreliminaryCount stubPreliminaryCount = preliminaryCount();
		counts.setFirstProtocolCount(stubProtocolCount);
		counts.setPreliminaryCount(stubPreliminaryCount);

		when(stubProtocolCount.getQuestionableBallotCount()).thenReturn(questionableBallotCountForProtocolCount);
		when(stubPreliminaryCount.getQuestionableBallotCount()).thenReturn(questionableBallotCountForPreliminaryCount);

		int difference = counts.getQuestionableBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY);

		assertThat(difference).isEqualTo(expectedDifference);
	}

	private ProtocolCount protocolCount() {
		return mock(ProtocolCount.class);
	}

	private ProtocolAndPreliminaryCount protocolAndPreliminaryCount() {
		return mock(ProtocolAndPreliminaryCount.class);
	}

	private PreliminaryCount preliminaryCount() {
		return mock(PreliminaryCount.class);
	}

	private FinalCount finalCount() {
		return mock(FinalCount.class);
	}

	private FinalCount whenFinalCount(String id, int index) {
		FinalCount count = finalCount();
		when(count.getId()).thenReturn(id);
		when(count.getIndex()).thenReturn(index);
		return count;
	}

}

