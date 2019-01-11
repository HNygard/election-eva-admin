package no.valg.eva.admin.configuration.domain.service;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.factory.BoroughFilterFactory;
import no.valg.eva.admin.configuration.domain.model.factory.PollingDistrictFilterFactory;
import no.valg.eva.admin.configuration.domain.model.filter.BoroughFilterEnum;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

public class OmraadehierarkiDomainService {

	private MvAreaRepository mvAreaRepository;
	private MvElectionRepository mvElectionRepository;
	private ReportCountCategoryRepository reportCountCategoryRepository;
	private ContestAreaDomainService contestAreaDomainService;
	private BoroughFilterFactory boroughFilterFactory;
	private PollingDistrictFilterFactory pollingDistrictFilterFactory;
	
	@Inject
	public OmraadehierarkiDomainService(MvAreaRepository mvAreaRepository, MvElectionRepository mvElectionRepository,
										ReportCountCategoryRepository reportCountCategoryRepository, ContestAreaDomainService contestAreaDomainService,
										BoroughFilterFactory filterFactoryProviderToBeRefactored, PollingDistrictFilterFactory pollingDistrictFilterFactory) {
		this.mvAreaRepository = mvAreaRepository;
		this.mvElectionRepository = mvElectionRepository;
		this.reportCountCategoryRepository = reportCountCategoryRepository;
		this.contestAreaDomainService = contestAreaDomainService;
		this.boroughFilterFactory = filterFactoryProviderToBeRefactored;
		this.pollingDistrictFilterFactory = pollingDistrictFilterFactory;
	}

	public List<MvArea> getCountiesFor(UserData userData, ElectionPath electionPath, AreaPath countryPath, CountCategory countCategory) {
		List<MvArea> mvAreas = getMvAreas(userData, countryPath, COUNTY);
		
		return mvAreas.stream()
			.filter(mvArea -> {
				ElectionPath electionPathToUse = getEffectiveElectionPath(userData, electionPath);
				if (userData.isOpptellingsvalgstyret()) {
					if ("00".equals(mvArea.getCountyId())) {
						Collection<ContestArea> contestAreas = contestAreaDomainService.contestAreasFor(userData.getOperatorElectionPath());
						EnumSet<CountCategory> forhaandstemmer = EnumSet.of(FO, FS);
						return forhaandstemmer.contains(countCategory) && contestAreas.stream().anyMatch(ContestArea::isChildArea);	
					}
					return hasNonChildAreaMunicipalitiesInContest(electionPathToUse, mvArea, countCategory, userData);
				} else {
					ValghierarkiSti valghierarkiSti = ValghierarkiSti.fra(electionPathToUse);
					ValggeografiSti valggeografiSti = mvArea.valggeografiSti();
					return mvElectionRepository.matcherValghierarkiStiOgValggeografiSti(valghierarkiSti, valggeografiSti);
				}
			})
			.collect(toList());
	}

	private boolean hasNonChildAreaMunicipalitiesInContest(ElectionPath electionPath, MvArea mvAreaForCounty, CountCategory countCategory, UserData userData) {
		final MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		final AreaPath countyPath = AreaPath.from(mvAreaForCounty.getAreaPath());
		List<ReportCountCategory> reportCountCategories =
			reportCountCategoryRepository.findByCountyElectionGroupAndCountCategory(countyPath, mvElection.getElectionGroup().getPk(), countCategory);
		final Set<String> municipalityIds = municipalityIds(reportCountCategories, userData, electionPath);
		List<MvArea> mvAreas = mvAreaRepository.findByPathAndLevel(countyPath, MUNICIPALITY);
		return mvAreas.stream()
			.filter(mvArea -> municipalityIds.contains(mvArea.getMunicipalityId()))
			.limit(1).count() > 0;
	}

	private Set<String> municipalityIds(List<ReportCountCategory> reportCountCategories, UserData userData, ElectionPath electionPath) {
		if (userData.isOpptellingsvalgstyret()) {
			Set<String> municipalityIdsForNonChildAreasInContest = getMunicipalityIdsForNonChildAreasInContest(userData, electionPath);
			return mapReportCountCategoriesToMunicipalityIds(reportCountCategories).stream()
				.filter(municipalityIdsForNonChildAreasInContest::contains)
				.collect(toCollection(HashSet::new));
		} else {
			return mapReportCountCategoriesToMunicipalityIds(reportCountCategories);
		}
	}

	private Set<String> getMunicipalityIdsForNonChildAreasInContest(UserData userData, ElectionPath electionPath) {
		Collection<ContestArea> contestAreas = contestAreaDomainService.contestAreasFor(getEffectiveElectionPath(userData, electionPath));
		return contestAreas.stream()
			.filter(ca -> !ca.isChildArea())
			.map(ca -> ca.getMvArea().getMunicipalityId())
			.collect(toCollection(HashSet::new));
	}

	private Set<String> mapReportCountCategoriesToMunicipalityIds(List<ReportCountCategory> reportCountCategories) {
		return reportCountCategories.stream()
			.map(rcc -> rcc.getMunicipality().getId())
			.collect(toCollection(HashSet::new));
	}
	
	/**
	 * @return valghierarki-sti basert på rollen til brukeren
	 */
	private ElectionPath getEffectiveElectionPath(UserData userData, ElectionPath electionPath) {
		if (userData.isOpptellingsvalgstyret()) {
			return userData.getOperatorElectionPath();
		} else {
			return electionPath;
		}
	}

	public List<MvArea> getMunicipalitiesFor(UserData userData, ElectionPath electionPath, AreaPath countyPath, CountCategory countCategory) {
		MvArea operatorMvArea = userData.getOperatorMvArea();

		if (operatorMvArea.getActualAreaLevel().equalOrlowerThan(MUNICIPALITY)) {
			return getAreasForSingleMunicipality(operatorMvArea);
		} else {
			return getAreasForMunicipality(electionPath, countyPath, countCategory, userData);
		}
	}

	private List<MvArea> getAreasForSingleMunicipality(MvArea operatorMvArea) {
		AreaPath operatorAreaPath = AreaPath.from(operatorMvArea.getAreaPath());
		return singletonList(mvAreaRepository.findSingleByPath(operatorAreaPath.toMunicipalityPath()));
	}

	private List<MvArea> getAreasForMunicipality(ElectionPath electionPath, AreaPath countyPath, CountCategory countCategory, UserData userData) {
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
		Set<String> municipalityIds;
		if (EnumSet.of(ELECTION, CONTEST).contains(mvElection.getActualElectionLevel()) && mvElection.getActualAreaLevel() == BOROUGH) {
			municipalityIds = new HashSet<>();
			municipalityIds.add("0301");
		} else {
			List<ReportCountCategory> reportCountCategories =
					reportCountCategoryRepository.findByCountyElectionGroupAndCountCategory(countyPath, mvElection.getElectionGroup().getPk(), countCategory);
			municipalityIds = municipalityIds(reportCountCategories, userData, electionPath);
		}
		return mvAreaRepository.findByPathAndLevel(countyPath, MUNICIPALITY).stream()
			.filter(mvArea -> municipalityIds.contains(mvArea.getMunicipalityId()))
			.collect(toList());
	}

	public List<MvArea> getBoroughsFor(UserData userData, CountCategory selectedCountCategory, ElectionPath selectedElectionPath, AreaPath municipalityPath) {
		List<MvArea> mvAreas = getMvAreas(userData, municipalityPath, BOROUGH);

		final Optional<BoroughFilterEnum> boroughFilter = boroughFilterFactory.build(selectedCountCategory, selectedElectionPath, municipalityPath);

		return mvAreas.stream()
			.filter(mvArea -> {
				boolean hasContestsForElectionAndArea = mvElectionRepository.hasContestsForElectionAndArea(selectedElectionPath, AreaPath.from(mvArea.getAreaPath()));
				return boroughFilter
					.map(mvAreaFilter -> hasContestsForElectionAndArea && mvAreaFilter.test(mvArea))
					.orElse(hasContestsForElectionAndArea);
			})
			.collect(toList());
	}
	
	public List<MvArea> getPollingDistrictsFor(UserData userData, CountCategory selectedCountCategory, ElectionPath selectedElectionPath, AreaPath boroughPath) {
		Predicate<MvArea> mvAreaFilter = pollingDistrictFilterFactory.build(userData, selectedCountCategory, selectedElectionPath, boroughPath);
		return getMvAreas(userData, boroughPath, POLLING_DISTRICT, mvAreaFilter);
	}

	private List<MvArea> getMvAreas(UserData userData, AreaPath parentAreaPath, AreaLevelEnum childrenAreaLevel, Predicate<MvArea> mvAreaFilter) {
		List<MvArea> mvAreas = getMvAreas(userData, parentAreaPath, childrenAreaLevel);
		
		return mvAreas.stream()
			.filter(mvAreaFilter)
			.collect(toList());
	}

	private List<MvArea> getMvAreas(UserData userData, AreaPath parentAreaPath, AreaLevelEnum childrenAreaLevel) {
		MvArea operatorMvArea = userData.getOperatorMvArea();

		if (operatorMvArea.getAreaLevel() >= childrenAreaLevel.getLevel()) {
			AreaPath operatorAreaPath = AreaPath.from(operatorMvArea.getAreaPath());
			return singletonList(mvAreaRepository.findSingleByPath(operatorAreaPath.toAreaLevelPath(childrenAreaLevel)));
		} else {
			return mvAreaRepository.findByPathAndLevel(parentAreaPath, childrenAreaLevel);
		}
	}
}
