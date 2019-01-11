package no.valg.eva.admin.settlement.domain;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.settlement.domain.event.OppgjorEndrerStatus;

public class OppgjorStatusendringTrigger {
	private Event<OppgjorEndrerStatus> oppgjorEndrerStatusEvent;

	@Inject
	public OppgjorStatusendringTrigger(Event<OppgjorEndrerStatus> oppgjorEndrerStatusEvent) {
		this.oppgjorEndrerStatusEvent = oppgjorEndrerStatusEvent;
	}

	public void fireEventForStatusendring(ElectionPath contestPath, AreaPath areaPath) {
		oppgjorEndrerStatusEvent.fire(new OppgjorEndrerStatus(areaPath, contestPath));
	}
}
