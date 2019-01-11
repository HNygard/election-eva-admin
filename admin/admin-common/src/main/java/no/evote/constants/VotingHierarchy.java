package no.evote.constants;


/**
 * Provides a way to specify the levels of data used in supporting voting.
 * 
 * Used in conjunction with creation of new election events with copying of data from other election events.
 *
 * E.g. if you want to copy the electoralRoll, you also will need the election hierarchy and the area hierarchy.
 */
public enum VotingHierarchy {
	NONE(0), AREA_HIERARCHY(1), ELECTION_HIERARCHY(2), ELECTORAL_ROLL(3), VOTING(4);

	private int level = 0;

	private VotingHierarchy(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public static VotingHierarchy getVotingHierarchy(boolean copyAreas, boolean copyElections, boolean copyElectoralRoll, boolean copyVotings) {
		if (copyVotings) {
			return VOTING;
		} else if (copyElectoralRoll) {
			return ELECTORAL_ROLL;
		} else if (copyElections) {
			return ELECTION_HIERARCHY;
		} else if (copyAreas) {
			return AREA_HIERARCHY;
		}
		return NONE;
	}
}
