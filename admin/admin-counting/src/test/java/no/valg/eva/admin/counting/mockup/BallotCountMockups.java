package no.valg.eva.admin.counting.mockup;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.mockups.BallotMockups.BALLOT_ID_DEM;
import static no.valg.eva.admin.common.mockups.BallotMockups.BALLOT_ID_KYST;
import static no.valg.eva.admin.common.mockups.BallotMockups.BALLOT_ID_NKP;
import static no.valg.eva.admin.common.mockups.BallotMockups.demBallot;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_NAME_DEM;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_NAME_KYST;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_NAME_NKP;
import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.BALLOT_COUNT_PK_SERIES;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;

public final class BallotCountMockups {

	
	public static final long BALLOT_COUNT_PK_DEM = BALLOT_COUNT_PK_SERIES + 4;

	public static final int MODIFIED_BALLOT_COUNT_ZERO = 0;
	public static final int MODIFIED_BALLOT_COUNT_NKP = 100;
	public static final int MODIFIED_BALLOT_COUNT_KYST = 205;
	public static final int MODIFIED_BALLOT_COUNT_DEM = 325;

	public static final int UNMODIFIED_BALLOT_COUNT_ZERO = 0;
	public static final int UNMODIFIED_BALLOT_COUNT_BLANK = 300;
	public static final int UNMODIFIED_BALLOT_COUNT_NKP = 900;
	public static final int UNMODIFIED_BALLOT_COUNT_KYST = 795;
	public static final int UNMODIFIED_BALLOT_COUNT_DEM = 675;

	public static final int BALLOT_COUNT_NKP = MODIFIED_BALLOT_COUNT_NKP + UNMODIFIED_BALLOT_COUNT_NKP;
	public static final int BALLOT_COUNT_KYST = MODIFIED_BALLOT_COUNT_KYST + UNMODIFIED_BALLOT_COUNT_KYST;
	public static final int BALLOT_COUNT_DEM = MODIFIED_BALLOT_COUNT_DEM + UNMODIFIED_BALLOT_COUNT_DEM;
	

	public static BallotCount modelBallotCount(
			final Long ballotCountPk,
			final VoteCount voteCount,
			final Ballot ballot,
			final int modifiedBallots,
			final int unmodifiedBallots) {

		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(ballotCountPk);
		ballotCount.setVoteCount(voteCount);
		ballotCount.setBallot(ballot);
		ballotCount.setModifiedBallots(modifiedBallots);
		ballotCount.setUnmodifiedBallots(unmodifiedBallots);
		return ballotCount;
	}

	public static BallotCount demModelBallotCount(final boolean includePrimaryKey, final VoteCount voteCount) {
		Long pk = includePrimaryKey ? BALLOT_COUNT_PK_DEM : null;
		return modelBallotCount(pk, voteCount, demBallot(), MODIFIED_BALLOT_COUNT_ZERO, MODIFIED_BALLOT_COUNT_DEM + UNMODIFIED_BALLOT_COUNT_DEM);
	}

	public static List<no.valg.eva.admin.common.counting.model.BallotCount> dtoExistingBallotCounts() {
		return asList(
				dtoBallotCount(BALLOT_ID_DEM, PARTY_NAME_DEM, MODIFIED_BALLOT_COUNT_DEM, UNMODIFIED_BALLOT_COUNT_DEM),
				dtoBallotCount(BALLOT_ID_KYST, PARTY_NAME_KYST, MODIFIED_BALLOT_COUNT_KYST, UNMODIFIED_BALLOT_COUNT_KYST),
				dtoBallotCount(BALLOT_ID_NKP, PARTY_NAME_NKP, MODIFIED_BALLOT_COUNT_NKP, UNMODIFIED_BALLOT_COUNT_NKP));
	}

	public static List<no.valg.eva.admin.common.counting.model.BallotCount> dtoNewBallotCounts() {
		return asList(
				dtoBallotCount(BALLOT_ID_DEM, PARTY_NAME_DEM, MODIFIED_BALLOT_COUNT_ZERO, UNMODIFIED_BALLOT_COUNT_ZERO),
				dtoBallotCount(BALLOT_ID_KYST, PARTY_NAME_KYST, MODIFIED_BALLOT_COUNT_ZERO, UNMODIFIED_BALLOT_COUNT_ZERO),
				dtoBallotCount(BALLOT_ID_NKP, PARTY_NAME_NKP, MODIFIED_BALLOT_COUNT_ZERO, UNMODIFIED_BALLOT_COUNT_ZERO));
	}

	public static no.valg.eva.admin.common.counting.model.BallotCount dtoBallotCount(
			final String ballotCountId,
			final String ballotCountName,
			final int ballotCountModified,
			final int ballotCountUnmodified) {

		no.valg.eva.admin.common.counting.model.BallotCount ballotCount = new no.valg.eva.admin.common.counting.model.BallotCount();
		ballotCount.setId(ballotCountId);
		ballotCount.setName(ballotCountName);
		ballotCount.setModifiedCount(0);
		ballotCount.setUnmodifiedCount(ballotCountUnmodified + ballotCountModified);
		return ballotCount;
	}

	private BallotCountMockups() {
		// no instances allowed
	}
}
