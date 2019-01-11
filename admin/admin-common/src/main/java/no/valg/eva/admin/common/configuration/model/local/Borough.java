package no.valg.eva.admin.common.configuration.model.local;

import no.valg.eva.admin.common.AreaPath;

public class Borough extends Place {

	public Borough(AreaPath areaPath, int version) {
		super(areaPath, version);
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public boolean isMunicipalityBorough() {
		return getPath().isMunicipalityBorough();
	}
}
