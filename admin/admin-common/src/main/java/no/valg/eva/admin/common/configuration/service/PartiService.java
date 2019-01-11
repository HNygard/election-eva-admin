package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.party.Parti;

/**
 * API for new PartyApplicationService
 */
public interface PartiService extends Serializable {

	/**
	 * Henter ut partier som ikke har listeforslag. Velger fra alle stortingspartier og landsdekkende partier, og fra lokale partier som tidligere stilte i
	 * samme område.
	 * @return partier som ikke har listeforslag
	 * @param userData
	 * @param contestPath
	 */
	List<Parti> partierUtenListeforslag(UserData userData, ElectionPath contestPath);
}
