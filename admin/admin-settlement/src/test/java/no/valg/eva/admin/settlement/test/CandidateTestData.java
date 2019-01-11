package no.valg.eva.admin.settlement.test;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;

@SuppressWarnings("unused")
public class CandidateTestData {
	private String id;
	private boolean baselineVotes;

	public Candidate candidate(SettlementBuilderTestData.Cache cache, Affiliation affiliation, int displayOrder) {
		Candidate candidate = new Candidate();
		candidate.setPk((long) id.hashCode());
		candidate.setId(id);
		candidate.setFirstName(id);
		candidate.setDisplayOrder(displayOrder);
		candidate.setAffiliation(affiliation);
		candidate.setBaselineVotes(baselineVotes);
		cache.candidateMap().put(id, candidate);
		return candidate;
	}
}
