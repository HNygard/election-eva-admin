package no.valg.eva.admin.settlement.test;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Contest;

@SuppressWarnings("unused")
public class BallotTestData {
	private AffiliationTestData affiliation;

	public Ballot ballot(SettlementBuilderTestData.Cache cache, Contest contest) {
		Ballot ballot = new Ballot();
		ballot.setContest(contest);
		Affiliation affiliation = this.affiliation.affiliation(cache, ballot);
		ballot.setAffiliation(affiliation);
		ballot.setPk(affiliation.getPk());
		ballot.setId(affiliation.getParty().getId());
		cache.ballotMap().put(affiliation.getParty().getId(), ballot);
		return ballot;
	}
}
