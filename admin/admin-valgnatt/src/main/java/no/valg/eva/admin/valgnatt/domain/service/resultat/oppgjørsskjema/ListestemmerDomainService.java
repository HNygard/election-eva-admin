package no.valg.eva.admin.valgnatt.domain.service.resultat.oppgjørsskjema;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.repository.SettlementRepository;

/**
 * Håndterer listestemmer.  Dersom det ikke er kommunestyrevalg har ikke listestemmer noen betydning og de hentes ikke opp.
 */
@Default
@ApplicationScoped
public class ListestemmerDomainService {

	@Inject
	private SettlementRepository settlementRepository;

	public ListestemmerDomainService() {

	}
	public ListestemmerDomainService(SettlementRepository settlementRepository) {
		this.settlementRepository = settlementRepository;
	}

	Set<AffiliationVoteCount> finnListestemmer(MvElection mvElectionContest) {
		if (mvElectionContest.getElection().isWritein()) {
			return settlementRepository.findSettlementByContest(mvElectionContest.getContest().getPk()).getAffiliationVoteCounts();
		}
		return Collections.emptySet();

	}
}
