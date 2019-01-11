package no.valg.eva.admin.settlement.domain.event;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;

public class OppgjorEndrerStatus {
	private final AreaPath areaPath;
	private final ElectionPath contestPath;

	public OppgjorEndrerStatus(AreaPath areaPath, ElectionPath contestPath) {
		this.areaPath = areaPath;
		this.contestPath = contestPath;
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}

	public ElectionPath getContestPath() {
		return contestPath;
	}
}
