package no.valg.eva.admin.counting.domain.service.settlement;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.counting.domain.service.VoteCountService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.PARENT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.TECHNICAL;

/**
 * Domain logic for finding counting areas related to settlement overview.
 */
@Default
@ApplicationScoped
public class CountingAreaDomainService {

	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private VoteCountService voteCountService;
	@Inject
	private VoteCountCategoryRepository voteCountCategoryRepository;

	public CountingAreaDomainService() {

	}

	public CountingAreaDomainService(MvAreaRepository mvAreaRepository, VoteCountService voteCountService,
			VoteCountCategoryRepository voteCountCategoryRepository) {
		this.mvAreaRepository = mvAreaRepository;
		this.voteCountService = voteCountService;
		this.voteCountCategoryRepository = voteCountCategoryRepository;
	}

	public List<MvArea> countingMvAreas(MvElection contestMvElection, CountCategory category) {
		MvArea contestMvArea = contestMvElection.contestMvArea();
		if (contestMvElection.getContest().isOnBoroughLevel() && !category.equals(CountCategory.VO)) {
			return countingMvAreasByBorough(contestMvElection, contestMvArea, category);
		}
		if (contestMvElection.getContest().isOnCountyLevel()) {
			return countingMvAreasByCounty(contestMvElection, contestMvArea, category);
		}
		if (!contestMvElection.getContest().isSingleArea()) {
			return countingMvAreasForSamiElection(contestMvElection, category);
		}
		return countingMvAreasByMunicipality(contestMvElection, contestMvArea, category);
	}

	private List<MvArea> countingMvAreasByBorough(MvElection contestMvElection, MvArea contestMvArea, CountCategory category) {
		CountingMode countingMode = voteCountService.countingMode(category, contestMvArea.getMunicipality(), contestMvElection);
		if (countingMode == CENTRAL_AND_BY_POLLING_DISTRICT) {
			return mvAreaRepository.findByPathAndLevel(contestMvArea.getPath(), AreaLevelEnum.POLLING_DISTRICT.getLevel());
		}
		return singletonList(contestMvArea);
	}

	private List<MvArea> countingMvAreasByCounty(MvElection contestMvElection, MvArea contestMvArea, CountCategory category) {
		List<MvArea> municipalityMvAreas = mvAreaRepository.findByPathAndLevel(AreaPath.from(contestMvArea.getAreaPath()), MUNICIPALITY);
		Map<AreaPath, EnumSet<CountCategory>> municipalityCountCategoryMap = municipalityCountCategoryMap(contestMvElection, municipalityMvAreas);
		List<MvArea> countingAreas = new ArrayList<>();
		for (MvArea municipalityMvArea : municipalityMvAreas) {
			if (municipalityCountCategoryMap.get(AreaPath.from(municipalityMvArea.getAreaPath())).contains(category)) {
				countingAreas.addAll(countingMvAreasByMunicipality(contestMvElection, municipalityMvArea, category));
			}
		}
		return countingAreas;
	}

	private List<MvArea> countingMvAreasForSamiElection(MvElection contestMvElection, CountCategory category) {
		Set<ContestArea> contestAreas = contestMvElection.getContest().getContestAreaSet();
		long childAreaCount = contestAreas.stream().filter(ContestArea::isChildArea).count();
		return contestAreas
				.stream()
				.filter(contestArea -> includeArea(contestArea, childAreaCount))
				.flatMap(contestArea -> countingMvAreasByMunicipality(contestMvElection, contestArea.getMvArea(), category).stream())
				.collect(Collectors.toList());
	}

	private boolean includeArea(ContestArea contestArea, long childAreaCount) {
		return contestArea.isParentArea() && childAreaCount > 0 || !contestArea.isChildArea() && !contestArea.isParentArea();
	}

	private List<MvArea> countingMvAreasByMunicipality(MvElection contestMvElection, MvArea municipalityMvArea, CountCategory category) {
		CountingMode countingMode = voteCountService.countingMode(category, municipalityMvArea.getMunicipality(), contestMvElection);
		if (countingMode == null) {
			return Collections.emptyList();
		}
		AreaPath municipalityPath = AreaPath.from(municipalityMvArea.getAreaPath());
		if (countingMode == CENTRAL) {
			AreaPath municipalityPollingDistrictPath = municipalityPath.toMunicipalityPollingDistrictPath();
			return singletonList(mvAreaRepository.findSingleByPath(municipalityPollingDistrictPath));
		}
		if (countingMode == BY_TECHNICAL_POLLING_DISTRICT) {
			return pollingDistrictMvAreas(municipalityPath, TECHNICAL);
		}
		return pollingDistrictMvAreas(municipalityPath, PARENT, REGULAR);
	}

	private List<MvArea> pollingDistrictMvAreas(AreaPath municipalityPath, PollingDistrictType first, PollingDistrictType... rest) {
		EnumSet<PollingDistrictType> pollingDistrictTypes = EnumSet.of(first, rest);
		List<MvArea> pollingDistrictMvAreas = mvAreaRepository.findByPathAndLevel(municipalityPath, POLLING_DISTRICT);
		return pollingDistrictMvAreas
				.stream()
				.filter(pollingDistrictMvArea -> pollingDistrictTypes.contains(pollingDistrictMvArea.pollingDistrictType()))
				.collect(Collectors.toList());
	}

	private Map<AreaPath, EnumSet<CountCategory>> municipalityCountCategoryMap(MvElection contestMvElection, List<MvArea> municipalityMvAreas) {
		Map<AreaPath, EnumSet<CountCategory>> municipalityCountCategoryMap = new HashMap<>();
		for (MvArea municipalityMvArea : municipalityMvAreas) {
			long municipalityPk = municipalityMvArea.getMunicipality().getPk();
			long electionGroupPk = contestMvElection.getElectionGroup().getPk();
			EnumSet<CountCategory> countCategorySet = countCategorySet(voteCountCategoryRepository.findByMunicipality(municipalityPk, electionGroupPk, false));
			AreaPath areaPath = AreaPath.from(municipalityMvArea.getAreaPath());
			municipalityCountCategoryMap.put(areaPath, countCategorySet);
		}
		return municipalityCountCategoryMap;
	}

	private EnumSet<CountCategory> countCategorySet(List<VoteCountCategory> voteCountCategories) {
		EnumSet<CountCategory> countCategorySet = EnumSet.noneOf(CountCategory.class);
		for (VoteCountCategory voteCountCategory : voteCountCategories) {
			countCategorySet.add(voteCountCategory.getCountCategory());
		}
		return countCategorySet;
	}

}
