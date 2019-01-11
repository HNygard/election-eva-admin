package no.evote.constants;

/**
 * Provides a way to specify the levels of data used in supporting counting.
 *
 * Used in conjunction with creation of new election events with copying of data from other election events.
 *
 * E.g. if you want to copy the local configuration, you will also need the list proposals, central configuration, the election hierarchy and the area hierarchy.
 */
public enum CountingHierarchy {
	NONE(0), AREA_HIERARCHY(1), ELECTION_HIERARCHY(2), LIST_PROPOSAL(3), CENTRAL_CONFIGURATION(3), LIST_AND_CENTRAL(3), LOCAL_CONFIGURATION(4), REPORTING_UNITS(
			5), COUNTING(6);

	private int level;

	CountingHierarchy(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public static CountingHierarchy getCountingHierarchy(boolean copyAreas, boolean copyElections, boolean copyListProposals,
			boolean copyElectionReportCountCategories, boolean copyReportCountCategories, boolean copyReportingUnits, boolean copyCountings) {
		if (copyCountings) {
			return COUNTING;
		}
		if (copyReportingUnits) {
			return REPORTING_UNITS;
		}
		if (copyReportCountCategories) {
			return LOCAL_CONFIGURATION;
		}
		if (copyElectionReportCountCategories && copyListProposals) {
			return LIST_AND_CENTRAL;
		}
		if (copyElectionReportCountCategories) {
			return CENTRAL_CONFIGURATION;
		}
		if (copyListProposals) {
			return LIST_PROPOSAL;
		}
		if (copyElections) {
			return ELECTION_HIERARCHY;
		}
		if (copyAreas) {
			return AREA_HIERARCHY;
		}
		return NONE;
	}
}
