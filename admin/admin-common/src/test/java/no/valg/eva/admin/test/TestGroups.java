package no.valg.eva.admin.test;

public final class TestGroups {
	
	private TestGroups() {
		
	}

	/** Intended to be used for classifying tests that accesses a database */
	public static final String REPOSITORY = "repository";
	 
	/** Intended to be used for tests that have long running time (longer than 1 sec is definitively slow,
	 * longer than a few tenths of a second is also slow when there are many tests */
	public static final String SLOW = "slow";

	/** Indicates that this test requires an intact electoral roll */
	public static final String NEEDS_INTACT_ELECTORAL_ROLL = "needs-intact-electoral-roll";

	/** Indicates that this test is an integration test */
	public static final String INTEGRATION = "integration";

	/** Indicates that this test is testing resources not contained in the test data */
	public static final String RESOURCES = "resources";
}
