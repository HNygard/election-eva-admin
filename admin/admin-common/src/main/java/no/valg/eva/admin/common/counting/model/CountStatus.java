package no.valg.eva.admin.common.counting.model;

import static java.lang.String.format;

/**
 * Statuses for counting.
 */
public enum CountStatus {
	/** New, unsaved counting */
	NEW(-1),
	/** Saved, but not approved counting */
	SAVED(0),
	/** Saved and approved counting */
	APPROVED(2),
	/** Counting ready for settlement. Only valid for final counts */
	TO_SETTLEMENT(3),
	/** Counting revoked. Only valid for protocol, preliminary and final counts */
	REVOKED(5);

	/**
	 * @return count status for id
	 * @throws java.lang.IllegalArgumentException
	 *             for unknown count status id
	 */
	public static CountStatus fromId(int id) {
		
		switch (id) {
		case -1:
			return NEW;
		case 0:
			return SAVED;
		case 2:
			return APPROVED;
		case 3:
			return TO_SETTLEMENT;
		case 5:
			return REVOKED;
		default:
			throw new IllegalArgumentException(format("unknown count status id: <%d>", id));
		}
		
	}

	private final int id;

	private CountStatus(int id) {
		this.id = id;
	}

	/**
	 * @return count status id mapped in the database
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return text id for this category used in GUI
	 */
	public String getName() {
		return "@vote_count_status[" + name() + "].name";
	}
}
