package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.AffiliationMockups.blankAffiliation;
import static no.valg.eva.admin.common.mockups.AffiliationMockups.demAffiliation;
import static no.valg.eva.admin.common.mockups.AffiliationMockups.kystAffiliation;
import static no.valg.eva.admin.common.mockups.AffiliationMockups.nkpAffiliation;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_ID_DEM;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_ID_KYST;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_ID_NKP;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.BALLOT_PK_SERIES;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;

public final class BallotMockups {

	
	public static final long BALLOT_PK_BLANK = BALLOT_PK_SERIES + 1;
	public static final long BALLOT_PK_NKP = BALLOT_PK_SERIES + 2;
	public static final long BALLOT_PK_KYST = BALLOT_PK_SERIES + 3;
	public static final long BALLOT_PK_DEM = BALLOT_PK_SERIES + 4;
	

	public static final String BALLOT_ID_NKP = PARTY_ID_NKP;
	public static final String BALLOT_ID_KYST = PARTY_ID_KYST;
	public static final String BALLOT_ID_DEM = PARTY_ID_DEM;

	private static final Integer DISPLAY_ORDER_BLANK = 12;
	private static final Integer DISPLAY_ORDER_NKP = 10;
	private static final Integer DISPLAY_ORDER_KYST = 9;
	private static final Integer DISPLAY_ORDER_DEM = 8;

	public static Ballot ballot(final Long ballotPk, final String ballotId, final Integer displayOrder, final Affiliation affiliation) {
		Ballot ballot = new Ballot();
		ballot.setPk(ballotPk);
		ballot.setId(ballotId);
		ballot.setDisplayOrder(displayOrder);
		affiliation.setBallot(ballot);
		ballot.setAffiliation(affiliation);
		affiliation.setBallot(ballot);
		return ballot;
	}

	public static Ballot blankBallot() {
		return ballot(BALLOT_PK_BLANK, EvoteConstants.BALLOT_BLANK, DISPLAY_ORDER_BLANK, blankAffiliation());
	}

	public static Ballot nkpBallot() {
		return ballot(BALLOT_PK_NKP, BALLOT_ID_NKP, DISPLAY_ORDER_NKP, nkpAffiliation());
	}

	public static Ballot kystBallot() {
		return ballot(BALLOT_PK_KYST, BALLOT_ID_KYST, DISPLAY_ORDER_KYST, kystAffiliation());
	}

	public static Ballot demBallot() {
		return ballot(BALLOT_PK_DEM, BALLOT_ID_DEM, DISPLAY_ORDER_DEM, demAffiliation());
	}

	private BallotMockups() {
		// no instances allowed
	}
}
