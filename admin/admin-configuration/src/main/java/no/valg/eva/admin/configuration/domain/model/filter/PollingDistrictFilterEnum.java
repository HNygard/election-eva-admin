package no.valg.eva.admin.configuration.domain.model.filter;

import java.util.function.Predicate;

import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

public enum PollingDistrictFilterEnum implements Predicate<MvArea> {
	FOR_BY_TECHNICAL_POLLING_DISTRICT((Predicate<MvArea>) mvArea -> mvArea.getPollingDistrict().isTechnicalPollingDistrict()),
	FOR_CENTRAL_AND_OPERATOR_NOT_ON_POLLING_DISTRICT((Predicate<MvArea>) mvArea -> mvArea.getPollingDistrict().isMunicipality()),
	FOR_ELECTRONIC_VOTING((Predicate<MvArea>) mvArea -> {
		PollingDistrict pollingDistrict = mvArea.getPollingDistrict();
		return !(pollingDistrict.isParentPollingDistrict() || pollingDistrict.isTechnicalPollingDistrict());
	}),
	FOR_OPERATOR_NOT_ON_POLLING_DISTRICT((Predicate<MvArea>) mvArea -> {
		PollingDistrict pollingDistrict = mvArea.getPollingDistrict();
		return !pollingDistrict.isMunicipality() && pollingDistrict.getPollingDistrict() == null && !pollingDistrict.isTechnicalPollingDistrict();
	}),
	DEFAULT((Predicate<MvArea>) mvArea -> {
		PollingDistrict pollingDistrict = mvArea.getPollingDistrict();
		return !(pollingDistrict.isMunicipality() || pollingDistrict.isTechnicalPollingDistrict());
	});
	
	private Predicate<MvArea> filter;

	PollingDistrictFilterEnum(Predicate<MvArea> filter) {
		this.filter = filter;
	}

	public boolean test(MvArea mvArea) {
		return filter.test(mvArea);
	}
}
