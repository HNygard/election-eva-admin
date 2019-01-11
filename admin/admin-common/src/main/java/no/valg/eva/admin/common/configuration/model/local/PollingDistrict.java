package no.valg.eva.admin.common.configuration.model.local;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;

public abstract class PollingDistrict extends Place {

	private Borough borough;
	private PollingDistrictType type;
	private boolean hasResponsibleOffiers;

	protected PollingDistrict(AreaPath areaPath, PollingDistrictType type, int version) {
		super(areaPath, version);
		this.type = type;
		validateType();
	}

	public Borough getBorough() {
		return borough;
	}

	public void setBorough(Borough borough) {
		this.borough = borough;
	}

	public void validateType() {
	}

	void setType(PollingDistrictType type) {
		this.type = type;
	}

	public PollingDistrictType getType() {
		return type;
	}

	public boolean isHasResponsibleOffiers() {
		return hasResponsibleOffiers;
	}

	public void setHasResponsibleOffiers(boolean hasResponsibleOffiers) {
		this.hasResponsibleOffiers = hasResponsibleOffiers;
	}

	@Override
	public boolean isValid() {
		try {
			validateType();
		} catch (RuntimeException e) {
			return false;
		}
		return super.isValid();
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
