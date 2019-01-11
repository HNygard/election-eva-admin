package no.valg.eva.admin.voting.domain.electoralroll;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

public final class PathSelector {

	private PathSelector() {
	}

	public static AreaPath path(AreaPath municipalityPath, PollingDistrict pollingDistrict) {
		return pollingDistrict != null ? pollingDistrict.areaPath() : municipalityPath;
	}
}
