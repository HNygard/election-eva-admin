package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Listeforslag;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valg_Valgdistrikt;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.repository.ContestRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "LegacyContestService")
@Remote(LegacyContestService.class)
public class ContestServiceEjb implements LegacyContestService {
	@Inject
	private ContestRepository contestRepository;

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valgdistrikt, type = READ)
	@Deprecated
	// use ContestRepository from an application service...
	public Contest findByPk(final UserData userData, final Long pk) {
		return contestRepository.findByPk(pk);
	}

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	@Deprecated
	public Contest findByElectionPath(UserData userData, ElectionPath electionPath) {
		return contestRepository.findSingleByPath(electionPath);
	}
}
