package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PartyMockups.blankParty;
import static no.valg.eva.admin.common.mockups.PartyMockups.demParty;
import static no.valg.eva.admin.common.mockups.PartyMockups.kystParty;
import static no.valg.eva.admin.common.mockups.PartyMockups.nkpParty;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.AFFILIATION_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Party;

public final class AffiliationMockups {

	
	public static final long AFFILIATION_PK_BLANK = AFFILIATION_PK_SERIES + 1;
	public static final long AFFILIATION_PK_NKP = AFFILIATION_PK_SERIES + 2;
	public static final long AFFILIATION_PK_KYST = AFFILIATION_PK_SERIES + 3;
	public static final long AFFILIATION_PK_DEM = AFFILIATION_PK_SERIES + 4;
	

	public static Affiliation affiliation(final Long affiliationPk, final Party party) {
		Affiliation affiliation = new Affiliation();
		affiliation.setPk(affiliationPk);
		affiliation.setParty(party);
		return affiliation;
	}

	public static Affiliation blankAffiliation() {
		return affiliation(AFFILIATION_PK_BLANK, blankParty());
	}

	public static Affiliation nkpAffiliation() {
		return affiliation(AFFILIATION_PK_NKP, nkpParty());
	}

	public static Affiliation kystAffiliation() {
		return affiliation(AFFILIATION_PK_KYST, kystParty());
	}

	public static Affiliation demAffiliation() {
		return affiliation(AFFILIATION_PK_DEM, demParty());
	}

	private AffiliationMockups() {
		// no instances allowed
	}
}
