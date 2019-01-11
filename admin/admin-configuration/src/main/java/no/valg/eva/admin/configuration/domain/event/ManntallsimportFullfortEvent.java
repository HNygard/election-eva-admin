package no.valg.eva.admin.configuration.domain.event;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

public class ManntallsimportFullfortEvent {

	private final ElectionEvent valghendelse;
	private final UserData userData;

	public ManntallsimportFullfortEvent(UserData userData, ElectionEvent valghendelse) {
		this.valghendelse = valghendelse;
		this.userData = userData;
	}

	public ElectionEvent getValghendelse() {
		return valghendelse;
	}

	public UserData getUserData() {
		return userData;
	}
}
