package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.configuration.status.ManntallsnummergenereringStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

public interface ManntallsnummerService extends Serializable {
	
	boolean erValgaarssifferGyldig(UserData userData, Manntallsnummer manntallsnummer);

	Manntallsnummer beregnFulltManntallsnummer(UserData userData, Long kortManntallsnummer);

	void genererManntallsnumre(UserData userData, Long electionEventPk);

	ManntallsnummergenereringStatus hentManntallsnummergenereringStatus(UserData userData, ElectionEvent electionEvent);
}
