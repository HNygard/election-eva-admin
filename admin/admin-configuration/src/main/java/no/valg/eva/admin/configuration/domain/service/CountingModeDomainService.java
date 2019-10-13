package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;

@Default
@ApplicationScoped
public class CountingModeDomainService {
    @Inject
	private ReportCountCategoryRepository reportCountCategoryRepository;
    @Inject
	private MvAreaRepository mvAreaRepository;
    @Inject
	private MvElectionRepository mvElectionRepository;
    @Inject
    private BoroughElectionDomainService boroughElectionDomainService;

	public CountingModeDomainService(ReportCountCategoryRepository reportCountCategoryRepository, MvAreaRepository mvAreaRepository,
                                     MvElectionRepository mvElectionRepository, BoroughElectionDomainService boroughElectionDomainService) {
		this.reportCountCategoryRepository = reportCountCategoryRepository;
		this.mvAreaRepository = mvAreaRepository;
		this.mvElectionRepository = mvElectionRepository;
        this.boroughElectionDomainService = boroughElectionDomainService;
	}
    public CountingModeDomainService() {

    }

	public Function<CountCategory, CountingMode> countingModeMapper(Contest contest, Municipality municipality) {
		if (contest.isOnBoroughLevel()) {
			return this::countingModeForBorughContestCategory;
		}
		List<ReportCountCategory> reportCountCategories = reportCountCategoryRepository.findByContestAndMunicipality(contest, municipality);
		Map<CountCategory, CountingMode> countingModeMap = reportCountCategories
				.stream()
				.collect(toMap(ReportCountCategory::getCountCategory, ReportCountCategory::getCountingMode));
		return countingModeMap::get;
	}

	private CountingMode countingModeForBorughContestCategory(CountCategory category) {
		if (category == VO) {
			return BY_POLLING_DISTRICT;
		}
		return CENTRAL;
	}
	
	public CountingMode findCountingMode(CountCategory countCategory, ElectionPath electionPath, AreaPath areaPath) {
		Municipality municipality = mvAreaRepository.findSingleByPath(areaPath.toMunicipalityPath()).getMunicipality();
		ElectionGroup electionGroup = mvElectionRepository.finnEnkeltMedSti(electionPath.toElectionGroupPath().tilValghierarkiSti()).getElectionGroup();
		ReportCountCategory reportCountCategory = reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(
			municipality, electionGroup, countCategory);

        if (boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(electionPath, areaPath)) {
            return boroughElectionDomainService.countingModeForBoroughWithCountCategory(countCategory);
        }
		
		return reportCountCategory.getCountingMode();
	}
	
}
