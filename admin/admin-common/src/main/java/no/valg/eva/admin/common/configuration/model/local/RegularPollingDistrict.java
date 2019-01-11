package no.valg.eva.admin.common.configuration.model.local;

import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;

public class RegularPollingDistrict extends PollingDistrict {

	public RegularPollingDistrict(AreaPath path, PollingDistrictType type) {
		this(path, type, 0);
	}

	public RegularPollingDistrict(AreaPath path, PollingDistrictType type, int version) {
		super(path, type, version);
	}

	public RegularPollingDistrict toChild() {
		setType(CHILD);
		return this;
	}

	@Override
	public void validateType() {
		if (getType() != REGULAR && getType() != CHILD) {
			throw new EvoteException("Invalid PollingDistrictType in RegularPollingDistrict: " + getType());
		}
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
