package no.valg.eva.admin.settlement.test;

import no.valg.eva.admin.configuration.domain.model.Party;

@SuppressWarnings("unused")
public class PartyTestData {
	private String id;

	public Party party() {
		Party party = new Party();
		party.setPk((long) id.hashCode());
		party.setId(id);
		return party;
	}
}
