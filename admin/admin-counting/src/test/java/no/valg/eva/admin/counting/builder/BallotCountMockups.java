package no.valg.eva.admin.counting.builder;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.mockups.BallotMockups.BALLOT_ID_DEM;
import static no.valg.eva.admin.common.mockups.BallotMockups.BALLOT_ID_KYST;
import static no.valg.eva.admin.common.mockups.BallotMockups.BALLOT_ID_NKP;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_NAME_DEM;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_NAME_KYST;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_NAME_NKP;

import java.util.List;

import no.valg.eva.admin.common.counting.model.BallotCountRef;

public final class BallotCountMockups {

	
	public static final int MODIFIED_BALLOT_COUNT_ZERO = 0;

	public static final int UNMODIFIED_BALLOT_COUNT_ZERO = 0;

	private static final long BALLOT_PK_DEM = 0L;
	private static final long BALLOT_PK_KYST = 1L;
	private static final long BALLOT_PK_NKP = 2L;
	

	public static List<no.valg.eva.admin.common.counting.model.BallotCount> dtoNewBallotCounts() {
		return asList(
				dtoBallotCount(BALLOT_ID_DEM, PARTY_NAME_DEM, MODIFIED_BALLOT_COUNT_ZERO, UNMODIFIED_BALLOT_COUNT_ZERO, BALLOT_PK_DEM),
				dtoBallotCount(BALLOT_ID_KYST, PARTY_NAME_KYST, MODIFIED_BALLOT_COUNT_ZERO, UNMODIFIED_BALLOT_COUNT_ZERO, BALLOT_PK_KYST),
				dtoBallotCount(BALLOT_ID_NKP, PARTY_NAME_NKP, MODIFIED_BALLOT_COUNT_ZERO, UNMODIFIED_BALLOT_COUNT_ZERO, BALLOT_PK_NKP));
	}

	public static no.valg.eva.admin.common.counting.model.BallotCount dtoBallotCount(
			final String ballotCountId,
			final String ballotCountName,
			final int ballotCountModified,
			final int ballotCountUnmodified, long ballotCountPk) {

		no.valg.eva.admin.common.counting.model.BallotCount ballotCount = new no.valg.eva.admin.common.counting.model.BallotCount();
		ballotCount.setId(ballotCountId);
		ballotCount.setName(ballotCountName);
		ballotCount.setModifiedCount(0);
		ballotCount.setUnmodifiedCount(ballotCountUnmodified + ballotCountModified);
		ballotCount.setBallotCountRef(new BallotCountRef(ballotCountPk));
		return ballotCount;
	}

	private BallotCountMockups() {
		// no instances allowed
	}
}
