package no.valg.eva.admin.common.configuration.model.local;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;

public class TechnicalPollingDistrict extends PollingDistrict {

	public TechnicalPollingDistrict(AreaPath path) {
		this(path, 0);
	}

	public TechnicalPollingDistrict(AreaPath path, int version) {
		super(path, PollingDistrictType.TECHNICAL, version);
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
