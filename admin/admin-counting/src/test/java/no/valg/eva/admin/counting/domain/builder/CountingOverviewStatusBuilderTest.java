package no.valg.eva.admin.counting.domain.builder;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.APPROVED;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.MUNICIPALITY_REJECTED_BALLOTS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.MUNICIPALITY_FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.MUNICIPALITY;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.PARENT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.RejectedBallotsStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountingOverviewStatusBuilderTest extends MockUtilsTestCase {
	private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111.111111.1111");

	@Test(dataProvider = "testData")
	public void countingOverviewStatus_givenTestData_returnsStatus(
			CountCategory category, StatusType statusType, PollingDistrictType pollingDistrictType, CountingMode countingMode, VoteCountDigest voteCountDigest1,
			VoteCountDigest voteCountDigest2, Status expectedStatus) throws Exception {
		CountingOverviewStatusBuilder builder = initializeMocks(CountingOverviewStatusBuilder.class);

		when(getInjectMock(VoteCountDigestFilterBuilder.class)
				.voteCountDigestFilterFor(category, AREA_PATH, statusType))
				.thenReturn((voteCountDigest -> true));
		
		List<Status> statuses = builder.countingOverviewStatuses(
				category, AREA_PATH, pollingDistrictType, singletonList(statusType), asList(voteCountDigest1, voteCountDigest2), countingMode);
		assertThat(statuses).containsExactly(expectedStatus);
	}

	@DataProvider
	public Object[][] testData() {
		CountCategory category = anyOf(CountCategory.class);
		CountCategory notVo = anyBut(VO);

		StatusType anyStatusType = anyOf(StatusType.class);
		StatusType notProtocolCount = anyBut(PROTOCOL_COUNT_STATUS);

		CountingMode countingMode = anyOf(CountingMode.class);
		CountingMode pollingDistrictCount = anyOf(BY_POLLING_DISTRICT, BY_TECHNICAL_POLLING_DISTRICT, CENTRAL_AND_BY_POLLING_DISTRICT);
		CountingMode notPollingDistrictCount = anyBut(BY_POLLING_DISTRICT, BY_TECHNICAL_POLLING_DISTRICT, CENTRAL_AND_BY_POLLING_DISTRICT);

		PollingDistrictType notMunicipalityOrParent = anyBut(MUNICIPALITY, PARENT);
		PollingDistrictType notMunicipalityOrChild = anyBut(MUNICIPALITY, CHILD);
		PollingDistrictType municipalityOrParent = anyOf(MUNICIPALITY, PARENT);
		PollingDistrictType parentOrRegular = anyOf(PARENT, REGULAR);

		VoteCountDigest voteCountDigest = voteCountDigest(false, false, false, 0);
		VoteCountDigest voteCountDigestWithRejectedBallots = voteCountDigest(false, false, false, 1);
		VoteCountDigest approvedVoteCountDigest = voteCountDigest(true, false, false, 0);
		VoteCountDigest approvedVoteCountDigestWithRejectedBallots = voteCountDigest(true, false, false, 1);
		VoteCountDigest toSettlementVoteCountDigest = voteCountDigest(false, true, true, 0);
		VoteCountDigest toSettlementVoteCountDigestWithRejectedBallots = voteCountDigest(false, true, true, 1);

		return new Object[][] {
				testData(category, anyStatusType, pollingDistrictCount, MUNICIPALITY, notRequiredStatus(anyStatusType)),
				testData(category, notProtocolCount, countingMode, CHILD, notRequiredStatus(notProtocolCount)),
				testData(category, notProtocolCount, CENTRAL, notMunicipalityOrChild, notRequiredStatus(notProtocolCount)),
				testData(notVo, PROTOCOL_COUNT_STATUS, notPollingDistrictCount, notMunicipalityOrParent, notRequiredStatus(PROTOCOL_COUNT_STATUS)),
				testData(notVo, notProtocolCount, notPollingDistrictCount, MUNICIPALITY, approved(notProtocolCount)),
				testData(notVo, notProtocolCount, pollingDistrictCount, notMunicipalityOrChild, approved(notProtocolCount)),
				testData(VO, MUNICIPALITY_FINAL_COUNT_STATUS, pollingDistrictCount, parentOrRegular, approvedVoteCountDigestWithRejectedBallots,
						municipalityRejectedBallots(approvedVoteCountDigestWithRejectedBallots)),
				testData(VO, PROTOCOL_COUNT_STATUS, notPollingDistrictCount, municipalityOrParent, notRequiredStatus(PROTOCOL_COUNT_STATUS)),

				testData(VO, COUNTY_REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, voteCountDigest, countyRejectedBallotsStatus()),
				testData(VO, COUNTY_REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, voteCountDigestWithRejectedBallots,
						countyRejectedBallotsStatus(1)),
				testData(VO, COUNTY_REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, approvedVoteCountDigest, countyRejectedBallotsStatus(true)),
				testData(VO, COUNTY_REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, approvedVoteCountDigestWithRejectedBallots,
						countyRejectedBallotsStatus(true, 1)),
				testData(VO, COUNTY_REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, toSettlementVoteCountDigest,
						countyRejectedBallotsStatus(true, true, 0)),

				testData(VO, REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, voteCountDigest, rejectedBallotsStatus()),
				testData(VO, REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, voteCountDigestWithRejectedBallots, rejectedBallotsStatus(1)),
				testData(VO, REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, approvedVoteCountDigest, rejectedBallotsStatus(true)),
				testData(VO, REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, approvedVoteCountDigestWithRejectedBallots,
						rejectedBallotsStatus(true, 1)),
				testData(VO, REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, toSettlementVoteCountDigest, rejectedBallotsStatus(true, true, 0)),
				testData(VO, REJECTED_BALLOTS_STATUS, pollingDistrictCount, parentOrRegular, toSettlementVoteCountDigestWithRejectedBallots,
						rejectedBallotsStatus(true, true, 1))
		};
	}

	private Object[] testData(
			CountCategory countCategory, StatusType statusType, CountingMode countingMode, PollingDistrictType pollingDistrictType, Status expectedStatus) {
		return testData(countCategory, statusType, countingMode, pollingDistrictType, voteCountDigest(true, false, false, null), expectedStatus);
	}

	private Object[] testData(CountCategory countCategory, StatusType statusType, CountingMode countingMode, PollingDistrictType pollingDistrictType,
			VoteCountDigest statusVoteCountDigest, Status expectedStatus) {
		when(statusVoteCountDigest.getId()).thenReturn("ID02");
		return new Object[] {
				countCategory, statusType, pollingDistrictType, countingMode, statusVoteCountDigest,
				voteCountDigest("ID01", false, false, false, null), expectedStatus };
	}

	private Status notRequiredStatus(StatusType statusType) {
		return statusType.countNotRequiredStatus();
	}

	private Status approved(StatusType statusType) {
		return new CountingStatus(statusType, APPROVED, false);
	}

	private Status countyRejectedBallotsStatus() {
		return rejectedBallotsStatus(true, false, false, 0);
	}

	private Status countyRejectedBallotsStatus(boolean approved) {
		return rejectedBallotsStatus(true, approved, false, 0);
	}

	private Status countyRejectedBallotsStatus(boolean approved, int rejectedBallotCount) {
		return rejectedBallotsStatus(true, approved, false, rejectedBallotCount);
	}

	private Status countyRejectedBallotsStatus(int rejectedBallotCount) {
		return rejectedBallotsStatus(true, false, false, rejectedBallotCount);
	}

	private Status countyRejectedBallotsStatus(boolean approved, boolean rejectedBallotsProcessed, int rejectedBallotCount) {
		return rejectedBallotsStatus(true, approved, rejectedBallotsProcessed, rejectedBallotCount);
	}

	private CountingStatus municipalityRejectedBallots(VoteCountDigest approvedVoteCountDigestWithRejectedBallots) {
		return new CountingStatus(MUNICIPALITY_FINAL_COUNT_STATUS, MUNICIPALITY_REJECTED_BALLOTS, approvedVoteCountDigestWithRejectedBallots.isManualCount(),
				approvedVoteCountDigestWithRejectedBallots.getRejectedBallots());
	}

	private Status rejectedBallotsStatus() {
		return rejectedBallotsStatus(false, false, false, 0);
	}

	private Status rejectedBallotsStatus(boolean approved) {
		return rejectedBallotsStatus(false, approved, false, 0);
	}

	private Status rejectedBallotsStatus(boolean approved, int rejectedBallotCount) {
		return rejectedBallotsStatus(false, approved, false, rejectedBallotCount);
	}

	private Status rejectedBallotsStatus(int rejectedBallotCount) {
		return rejectedBallotsStatus(false, false, false, rejectedBallotCount);
	}

	private Status rejectedBallotsStatus(boolean approved, boolean rejectedBallotsProcessed, int rejectedBallotCount) {
		return rejectedBallotsStatus(false, approved, rejectedBallotsProcessed, rejectedBallotCount);
	}

	private Status rejectedBallotsStatus(boolean county, boolean approved, boolean rejectedBallotsProcessed, int rejectedBallotCount) {
		return new RejectedBallotsStatus(county, approved, rejectedBallotsProcessed, false, rejectedBallotCount);
	}

	private VoteCountDigest voteCountDigest(boolean approved, boolean toSettlement, boolean rejectedBallotsProcessed, Integer rejectedBallots) {
		return voteCountDigest(null, approved, toSettlement, rejectedBallotsProcessed, rejectedBallots);
	}

	private VoteCountDigest voteCountDigest(String id, boolean approved, boolean toSettlement, boolean rejectedBallotsProcessed, Integer rejectedBallots) {
		VoteCountDigest voteCountDigest = createMock(VoteCountDigest.class);
		if (id != null) {
			when(voteCountDigest.getId()).thenReturn(id);
		}
		when(voteCountDigest.isApproved()).thenReturn(approved);
		when(voteCountDigest.isToSettlement()).thenReturn(toSettlement);
		when(voteCountDigest.isRejectedBallotsProcessed()).thenReturn(rejectedBallotsProcessed);
		when(voteCountDigest.getRejectedBallots()).thenReturn(rejectedBallots);
		return voteCountDigest;
	}
}
