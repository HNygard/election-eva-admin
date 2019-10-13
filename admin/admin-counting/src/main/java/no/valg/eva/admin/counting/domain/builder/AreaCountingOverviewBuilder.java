package no.valg.eva.admin.counting.domain.builder;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;

import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.AreaCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewWithAreaCountingOverview;
import no.valg.eva.admin.common.counting.model.countingoverview.Status;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;

@Default
@ApplicationScoped
public class AreaCountingOverviewBuilder {
	@Inject
	private CountingOverviewStatusBuilder countingOverviewStatusBuilder;

	public AreaCountingOverviewBuilder() {

	}

	public AreaCountingOverviewBuilder(CountingOverviewStatusBuilder countingOverviewStatusBuilder) {
		this.countingOverviewStatusBuilder = countingOverviewStatusBuilder;
	}

	public List<AreaCountingOverview> areaCountingOverviews(
			CountCategory category, Contest contest, MvArea mvArea, List<StatusType> statusTypes, List<VoteCountDigest> voteCounts, CountingMode countingMode) {
		Collection<PollingDistrict> pollingDistricts = pollingDistricts(category, mvArea.getMunicipality(), mvArea.getBorough());
		return areaCountingOverviews(category, contest, pollingDistricts, statusTypes, voteCounts, countingMode);
	}

	private Collection<PollingDistrict> pollingDistricts(CountCategory countCategory, Municipality municipality, Borough borough) {
		if (countCategory.isEarlyVoting()) {
			return municipality.technicalPollingDistricts();
		}
		if (borough != null) {
			return borough.getPollingDistricts();
		}
		return municipality.regularPollingDistricts(true, false);
	}

	private List<AreaCountingOverview> areaCountingOverviews(CountCategory category, Contest contest, Collection<PollingDistrict> pollingDistricts,
			List<StatusType> statusTypes, List<VoteCountDigest> voteCounts, CountingMode countingMode) {
		return pollingDistricts
				.stream()
				.map(pollingDistrict -> areaCountingOverview(category, contest, pollingDistrict, statusTypes, voteCounts, countingMode))
				.sorted(comparing(CountingOverviewWithAreaCountingOverview::getAreaPath))
				.collect(toList());
	}

	private AreaCountingOverview areaCountingOverview(CountCategory category, Contest contest, PollingDistrict pollingDistrict,
			List<StatusType> statusTypes, List<VoteCountDigest> voteCounts, CountingMode countingMode) {
		ElectionPath contestPath = contest.electionPath();
		AreaPath areaPath = pollingDistrict.areaPath();
		String areaName = pollingDistrict.getName();
		List<Status> countingOverviewStatuses = countingOverviewStatusBuilder.countingOverviewStatuses(category, areaPath, pollingDistrict.type(), statusTypes,
				voteCounts, countingMode);
		if (pollingDistrict.type() == CHILD) {
			return new AreaCountingOverview(areaName, category, contestPath, areaPath, countingOverviewStatuses);
		}
		boolean hasCount = countingMode != CENTRAL;
		if (pollingDistrict.isParentPollingDistrict() && statusTypes.contains(PROTOCOL_COUNT_STATUS)) {
			List<AreaCountingOverview> areaCountingOverviews = areaCountingOverviews(category, contest, pollingDistrict.getChildPollingDistricts(), statusTypes,
					voteCounts, countingMode);
			return new AreaCountingOverview(areaName, category, contestPath, areaPath, hasCount, countingOverviewStatuses, areaCountingOverviews);
		}
		return new AreaCountingOverview(areaName, category, contestPath, areaPath, hasCount, countingOverviewStatuses);
	}

}
