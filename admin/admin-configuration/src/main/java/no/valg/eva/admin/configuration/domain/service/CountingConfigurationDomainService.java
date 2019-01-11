package no.valg.eva.admin.configuration.domain.service;

import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;

import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.service.configuration.CountingConfiguration;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;

public class CountingConfigurationDomainService {

	private MvAreaRepository mvAreaRepository;
	private MvElectionRepository mvElectionRepository;
	private ReportCountCategoryRepository reportCountCategoryRepository;

	@Inject
	public CountingConfigurationDomainService(MvAreaRepository mvAreaRepository, MvElectionRepository mvElectionRepository,
											  ReportCountCategoryRepository reportCountCategoryRepository) {
		this.mvAreaRepository = mvAreaRepository;
		this.mvElectionRepository = mvElectionRepository;
		this.reportCountCategoryRepository = reportCountCategoryRepository;
	}

	public CountingConfiguration getCountingConfiguration(CountContext countContext, AreaPath areaPath) {
		MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
		MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(countContext.valgdistriktSti());
		Contest contest = mvElection.getContest();
		AreaLevelEnum contestAreaLevel = AreaLevelEnum.getLevel(mvElection.getAreaLevel());
		Municipality municipality = mvArea.getMunicipality();

		CountingMode countingMode = getCountingMode(countContext.getCategory(), mvElection.getElectionGroup(), contestAreaLevel, municipality);

		CountingConfiguration countingConfiguration = new CountingConfiguration();
		countingConfiguration.setContestAreaLevel(contestAreaLevel);
		countingConfiguration.setCountingMode(countingMode);
		countingConfiguration.setRequiredProtocolCount(municipality.isRequiredProtocolCount());
		countingConfiguration.setPenultimateRecount(contest.isContestOrElectionPenultimateRecount());
		return countingConfiguration;
	}

	private CountingMode getCountingMode(CountCategory countCategory, ElectionGroup electionGroup, AreaLevelEnum contestAreaLevel, Municipality municipality) {
		CountingMode countingMode;
		if (contestAreaLevel == BOROUGH) {
			countingMode = countingModeForBoroughContests(countCategory);
		} else {
			countingMode = countingModeForOtherContests(municipality, electionGroup, countCategory);
		}
		return countingMode;
	}

	private CountingMode countingModeForBoroughContests(CountCategory category) {
		if (category == VO) {
			return CENTRAL_AND_BY_POLLING_DISTRICT;
		}
		return CENTRAL;
	}

	private CountingMode countingModeForOtherContests(Municipality municipality, ElectionGroup electionGroup, CountCategory voteCountCategory) {
		return reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(municipality, electionGroup, voteCountCategory).getCountingMode();
	}
	
}
