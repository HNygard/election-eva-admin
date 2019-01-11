package no.valg.eva.admin.configuration.domain.model.filter;

import java.util.function.Predicate;

import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.MvArea;

public enum BoroughFilterEnum implements Predicate<MvArea> {
	FOR_BOROUGH_ELECTION(mvArea -> !mvArea.getBorough().isMunicipality1()),
	FOR_NOT_VO_AND_CENTRAL(mvArea -> mvArea.getBorough().isMunicipality1()),
	FOR_VO_AND_BY_POLLING_DISTRICT_OR_CENTRAL_AND_BY_POLLING_DISTRICT(mvArea -> {
		Borough borough = mvArea.getBorough();
		return !borough.isMunicipality1() || borough.hasRegularPollingDistricts() || borough.hasParentPollingDistricts();
	}),
	FOR_BY_TECHNICAL_POLLING_DISTRICT(mvArea -> {
		Borough borough = mvArea.getBorough();
		return borough.isMunicipality1();
	});
	
	private Predicate<MvArea> filter;

	BoroughFilterEnum(Predicate<MvArea> filter) {
		this.filter = filter;
	}

	public boolean test(MvArea mvArea) {
		return filter.test(mvArea);
	}
}
