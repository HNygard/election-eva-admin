package no.valg.eva.admin.counting.domain.service.countingoverview;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.COUNTY_REJECTED_BALLOTS_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.MUNICIPALITY_FINAL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PRELIMINARY_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.PROTOCOL_COUNT_STATUS;
import static no.valg.eva.admin.common.counting.model.countingoverview.StatusType.REJECTED_BALLOTS_STATUS;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;
import no.valg.eva.admin.common.counting.model.countingoverview.StatusType;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.service.CountingModeDomainService;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.counting.domain.builder.CountingOverviewRootBuilder;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.counting.domain.service.settlement.CountCategoryDomainService;

/**
 * Domain service for providing overview of counts.
 */
@Default
@ApplicationScoped
public class CountingOverviewDomainService {
    @Inject
	private VoteCountService voteCountService;
    @Inject
	private CountingOverviewRootBuilder countingOverviewRootBuilder;
    @Inject
	private ReportCountCategoryRepository reportCountCategoryRepository;
    @Inject
	private CountCategoryDomainService countCategoryDomainService;
    @Inject
	private CountingModeDomainService countingModeDomainService;

	public CountingOverviewDomainService(
			VoteCountService voteCountService, CountingOverviewRootBuilder countingOverviewRootBuilder,
			ReportCountCategoryRepository reportCountCategoryRepository, CountCategoryDomainService countCategoryDomainService,
			CountingModeDomainService countingModeDomainService) {
		this.voteCountService = voteCountService;
		this.countingOverviewRootBuilder = countingOverviewRootBuilder;
		this.reportCountCategoryRepository = reportCountCategoryRepository;
		this.countCategoryDomainService = countCategoryDomainService;
		this.countingModeDomainService = countingModeDomainService;
	}
    public CountingOverviewDomainService() {

    }

    public CountingOverviewRoot countingOverviewForOpptellingsvalgstyret(ContestArea contestArea) {
		Contest contest = contestArea.getContest();
		MvArea mvArea = contestArea.getMvArea();
		List<StatusType> statusTypes = statusTypesForOpptellingsvalgstyret();
		List<VoteCountDigest> voteCountDigests = voteCountService.voteCountDigestsForOpptellingsvalgstyret(contest, mvArea);
		Municipality municipality = mvArea.getMunicipality();
		List<CountCategory> countCategories = countCategoryDomainService.countCategories(contest, municipality);
		Function<CountCategory, CountingMode> countingModeMapper = countingModeDomainService.countingModeMapper(contest, municipality);
		return countingOverviewRootBuilder.countingOverviewRoot(contest, mvArea, statusTypes, countCategories, voteCountDigests, countingModeMapper);
	}

	private List<StatusType> statusTypesForOpptellingsvalgstyret() {
		return asList(PRELIMINARY_COUNT_STATUS, FINAL_COUNT_STATUS, REJECTED_BALLOTS_STATUS);
	}

	public CountingOverviewRoot countingOverviewForValgstyret(Contest contest, MvArea mvArea) {
		List<StatusType> statusTypes = statusTypesForValgstyret(contest, mvArea.getMunicipality());
		List<VoteCountDigest> voteCountDigests = voteCountService.voteCountDigestsForValgstyret(contest, mvArea);
		Municipality municipality = mvArea.getMunicipality();
		List<CountCategory> countCategories = countCategoryDomainService.countCategories(contest, municipality);
		Function<CountCategory, CountingMode> countingModeMapper = countingModeDomainService.countingModeMapper(contest, municipality);
		return countingOverviewRootBuilder.countingOverviewRoot(contest, mvArea, statusTypes, countCategories, voteCountDigests, countingModeMapper);
	}

	private List<StatusType> statusTypesForValgstyret(Contest contest, Municipality municipality) {
		List<StatusType> countingStatusTypes = new ArrayList<>();
		if (isProtocolCountStatusIncluded(contest, municipality)) {
			countingStatusTypes.add(PROTOCOL_COUNT_STATUS);
		}
		countingStatusTypes.add(PRELIMINARY_COUNT_STATUS);
		if (contest.isContestOrElectionPenultimateRecount()) {
			countingStatusTypes.add(FINAL_COUNT_STATUS);
			countingStatusTypes.add(REJECTED_BALLOTS_STATUS);
		}
		return countingStatusTypes;
	}

	private boolean isProtocolCountStatusIncluded(Contest contest, Municipality municipality) {
		if (contest.isOnBoroughLevel()) {
			return true;
		}
		ReportCountCategory reportCountCategory = reportCountCategoryRepository.findByContestAndMunicipalityAndCategory(contest, municipality, VO);
		if (reportCountCategory == null) {
			return false;
		}
		boolean requiredProtocolCount = municipality.isRequiredProtocolCount();
		boolean hasParentPollingDistricts = municipality.hasParentPollingDistricts();
		return requiredProtocolCount && (reportCountCategory.getCountingMode() != BY_POLLING_DISTRICT || hasParentPollingDistricts);
	}

	public CountingOverviewRoot countingOverviewForFylkesvalgstyret(Contest contest, MvArea mvArea) {
		List<StatusType> statusTypes = statusTypesForFylkesvalgstyret(contest);
		List<VoteCountDigest> voteCountDigests = voteCountService.voteCountDigestsForFylkesvalgstyret(contest, mvArea);
		Municipality municipality = mvArea.getMunicipality();
		List<CountCategory> countCategories = countCategoryDomainService.countCategories(contest, municipality);
		Function<CountCategory, CountingMode> countingModeMapper = countingModeDomainService.countingModeMapper(contest, municipality);
		return countingOverviewRootBuilder.countingOverviewRoot(contest, mvArea, statusTypes, countCategories, voteCountDigests, countingModeMapper);
	}

	private List<StatusType> statusTypesForFylkesvalgstyret(Contest contest) {
		List<StatusType> countingStatusTypes = new ArrayList<>();
		if (contest.isContestOrElectionPenultimateRecount()) {
			countingStatusTypes.add(MUNICIPALITY_FINAL_COUNT_STATUS);
		} else {
			countingStatusTypes.add(PRELIMINARY_COUNT_STATUS);
		}
		countingStatusTypes.add(COUNTY_FINAL_COUNT_STATUS);
		countingStatusTypes.add(COUNTY_REJECTED_BALLOTS_STATUS);
		return countingStatusTypes;
	}
}
