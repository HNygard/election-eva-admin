package no.valg.eva.admin.settlement.test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Party;

@SuppressWarnings({ "unused" })
public class AffiliationTestData {
	private boolean approved;
	private PartyTestData party;
	private List<CandidateTestData> candidates = new ArrayList<>();

	public Affiliation affiliation(SettlementBuilderTestData.Cache cache, Ballot ballot) {
		Party party = this.party.party();
		Affiliation affiliation = new Affiliation();
		affiliation.setPk(party.getPk());
		affiliation.setBallot(ballot);
		affiliation.setApproved(approved);
		affiliation.setParty(party);
		affiliation.setCandidates(candidates(cache, affiliation));
		cache.affiliationMap().put(party.getId(), affiliation);
		return affiliation;
	}

	private Set<Candidate> candidates(SettlementBuilderTestData.Cache cache, Affiliation affiliation) {
		Set<Candidate> candidates = new LinkedHashSet<>();
		int lastDisplayOrder = 0;
		for (CandidateTestData candidateTestData : this.candidates) {
			candidates.add(candidateTestData.candidate(cache, affiliation, ++lastDisplayOrder));
		}
		return candidates;
	}
}
