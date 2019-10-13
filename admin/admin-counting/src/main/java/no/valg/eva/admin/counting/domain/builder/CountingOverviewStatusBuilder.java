package no.valg.eva.admin.counting.domain.builder;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.APPROVED;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.COUNTING;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.MUNICIPALITY_REJECTED_BALLOTS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.MUNICIPALITY_FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.PARENT;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.RejectedBallotsStatus;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;

@Default
@ApplicationScoped
public class CountingOverviewStatusBuilder {
	@Inject
	private VoteCountDigestFilterBuilder voteCountDigestFilterBuilder;

	public CountingOverviewStatusBuilder() {

	}

	public CountingOverviewStatusBuilder(VoteCountDigestFilterBuilder voteCountDigestFilterBuilder) {
		this.voteCountDigestFilterBuilder = voteCountDigestFilterBuilder;
	}

	public List<Status> countingOverviewStatuses(CountCategory category, AreaPath areaPath, PollingDistrictType pollingDistrictType,
												 List<StatusType> statusTypes, List<VoteCountDigest> voteCounts, CountingMode countingMode) {
		return statusTypes
				.stream()
				.map(statusType -> countingOverviewStatus(category, areaPath, pollingDistrictType, statusType, voteCounts, countingMode))
				.collect(toList());
	}

	private Status countingOverviewStatus(CountCategory category, AreaPath areaPath, PollingDistrictType pollingDistrictType,
			StatusType statusType, List<VoteCountDigest> voteCounts, CountingMode countingMode) {
		if (isCountNotRequired(category, countingMode, pollingDistrictType, statusType)) {
			return statusType.countNotRequiredStatus();
		}
		return countingOverviewStatus(category, areaPath, statusType, voteCounts);
	}

	private boolean isCountNotRequired(CountCategory category, CountingMode countingMode, PollingDistrictType pollingDistrictType, StatusType statusType) {
		if (countingMode.isPollingDistrictOrTechnicalPollingDistrictCount() && pollingDistrictType == PollingDistrictType.MUNICIPALITY) {
			return true;
		}
		if (statusType == PROTOCOL_COUNT_STATUS) {
			EnumSet<PollingDistrictType> municipalityOrParentPollingDistrict = EnumSet.of(PollingDistrictType.MUNICIPALITY, PARENT);
			return category != VO || municipalityOrParentPollingDistrict.contains(pollingDistrictType);
		}
		return pollingDistrictType == CHILD || countingMode == CENTRAL && pollingDistrictType != PollingDistrictType.MUNICIPALITY;
	}

	private Status countingOverviewStatus(CountCategory countCategory, AreaPath areaPath, StatusType statusType, List<VoteCountDigest> voteCounts) {
		return findVoteCountFor(countCategory, areaPath, statusType, voteCounts)
				.map(voteCount -> status(statusType, voteCount))
				.orElse(statusType.defaultStatus());
	}

	private Optional<VoteCountDigest> findVoteCountFor(CountCategory countCategory, AreaPath areaPath, StatusType statusType, List<VoteCountDigest> voteCounts) {
		return voteCounts
				.stream()
				.filter(voteCountDigestFilterBuilder.voteCountDigestFilterFor(countCategory, areaPath, statusType))
				.sorted(comparing(VoteCountDigest::getId))
				.reduce((voteCount1, voteCount2) -> voteCount1.isApproved() || voteCount1.isToSettlement() ? voteCount1 : voteCount2);
	}

	private Status status(StatusType statusType, VoteCountDigest voteCountDigest) {
		boolean approved = voteCountDigest.isApproved() || voteCountDigest.isToSettlement();
		boolean manualCount = voteCountDigest.isManualCount();
		if (statusType == REJECTED_BALLOTS_STATUS || statusType == COUNTY_REJECTED_BALLOTS_STATUS) {
			return new RejectedBallotsStatus(
					statusType == COUNTY_REJECTED_BALLOTS_STATUS, approved, voteCountDigest.isRejectedBallotsProcessed(), manualCount,
					voteCountDigest.getRejectedBallots());
		}
		if (statusType == MUNICIPALITY_FINAL_COUNT_STATUS && approved && !voteCountDigest.isRejectedBallotsProcessed()) {
			return new CountingStatus(statusType, MUNICIPALITY_REJECTED_BALLOTS, manualCount, voteCountDigest.getRejectedBallots());
		}
		return new CountingStatus(statusType, approved ? APPROVED : COUNTING, manualCount);
	}
}
