package no.valg.eva.admin.felles.bakgrunnsjobb.service;

import java.io.Serializable;

import no.evote.security.UserData;

public interface BakgrunnsjobbService extends Serializable {

	boolean erManntallsnummergenereringStartetEllerFullfort(UserData userData);
	
}
