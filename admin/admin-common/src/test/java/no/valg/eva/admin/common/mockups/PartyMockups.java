package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.PARTY_PK_SERIES;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.configuration.domain.model.Party;

public final class PartyMockups {

	
	public static final long PARTY_PK_BLANK = PARTY_PK_SERIES + 1;
	public static final long PARTY_PK_NKP = PARTY_PK_SERIES + 2;
	public static final long PARTY_PK_KYST = PARTY_PK_SERIES + 3;
	public static final long PARTY_PK_DEM = PARTY_PK_SERIES + 4;
	

	public static final String PARTY_ID_NKP = "NKP";
	public static final String PARTY_ID_KYST = "KYST";
	public static final String PARTY_ID_DEM = "DEM";

	public static final String PARTY_NAME_NKP = "Norges kommunistiske parti";
	public static final String PARTY_NAME_KYST = "Kystpartiet";
	public static final String PARTY_NAME_DEM = "@party[DEM].name";

	public static Party party(final Long partyPk, final String partyId) {
		Party party = new Party();
		party.setPk(partyPk);
		party.setId(partyId);
		return party;
	}

	public static Party blankParty() {
		return party(PARTY_PK_BLANK, EvoteConstants.BALLOT_BLANK);
	}

	public static Party nkpParty() {
		return party(PARTY_PK_NKP, PARTY_ID_NKP);
	}

	public static Party kystParty() {
		return party(PARTY_PK_KYST, PARTY_ID_KYST);
	}

	public static Party demParty() {
		return party(PARTY_PK_DEM, PARTY_ID_DEM);
	}

	private PartyMockups() {
		// no instances allowed
	}
}
