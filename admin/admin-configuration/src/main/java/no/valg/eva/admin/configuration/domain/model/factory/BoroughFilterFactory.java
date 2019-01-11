package no.valg.eva.admin.configuration.domain.model.factory;

import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.configuration.domain.model.filter.BoroughFilterEnum.FOR_BOROUGH_ELECTION;
import static no.valg.eva.admin.configuration.domain.model.filter.BoroughFilterEnum.FOR_BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.configuration.domain.model.filter.BoroughFilterEnum.FOR_NOT_VO_AND_CENTRAL;
import static no.valg.eva.admin.configuration.domain.model.filter.BoroughFilterEnum.FOR_VO_AND_BY_POLLING_DISTRICT_OR_CENTRAL_AND_BY_POLLING_DISTRICT;

import java.util.EnumSet;
import java.util.Optional;

import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.filter.BoroughFilterEnum;
import no.valg.eva.admin.configuration.domain.service.CountingModeDomainService;
import no.valg.eva.admin.configuration.domain.service.ValghierarkiDomainService;

public class BoroughFilterFactory {
	
	private CountingModeDomainService countingModeDomainService;
	private ValghierarkiDomainService valghierarkiDomainService;

	@Inject
	public BoroughFilterFactory(ValghierarkiDomainService valghierarkiDomainService, CountingModeDomainService countingModeDomainService) {
		this.countingModeDomainService = countingModeDomainService;
		this.valghierarkiDomainService = valghierarkiDomainService;
	}
	
	public Optional<BoroughFilterEnum> build(CountCategory selectedCountCategory, ElectionPath selectedElectionPath, AreaPath municipalityPath) {
		if (isElectionOnBoroughLevel(selectedElectionPath)) {
			return Optional.of(FOR_BOROUGH_ELECTION);
		}
		CountingMode countingMode = countingModeDomainService.findCountingMode(selectedCountCategory, selectedElectionPath, municipalityPath);
		if (isCountCategoryNotVoAndCentralCountingMode(countingMode, selectedCountCategory)) {
			return Optional.of(FOR_NOT_VO_AND_CENTRAL);
		}
		if (isCountCategoryVoAndByPollingDistrictOrCentralAndByPollingDistrict(countingMode, selectedCountCategory)) {
			return Optional.of(FOR_VO_AND_BY_POLLING_DISTRICT_OR_CENTRAL_AND_BY_POLLING_DISTRICT);
		}
		if (countingMode == BY_TECHNICAL_POLLING_DISTRICT) {
			return Optional.of(FOR_BY_TECHNICAL_POLLING_DISTRICT);
		}
		return Optional.empty();
	}

	private boolean isCountCategoryVoAndByPollingDistrictOrCentralAndByPollingDistrict(CountingMode countingMode, CountCategory selectedCountCategory) {
		return selectedCountCategory == VO && EnumSet.of(BY_POLLING_DISTRICT, CENTRAL_AND_BY_POLLING_DISTRICT).contains(countingMode);
	}

	private boolean isCountCategoryNotVoAndCentralCountingMode(CountingMode countingMode, CountCategory selectedCountCategory) {
		return selectedCountCategory != VO && countingMode == CENTRAL;
	}

	private boolean isElectionOnBoroughLevel(ElectionPath selectedElectionPath) {
		return valghierarkiDomainService.isElectionOnBoroughLevel(selectedElectionPath);
	}
}
