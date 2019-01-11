package no.valg.eva.admin.common.counting.model;

import static java.lang.String.format;

/**
 * Qualifiers for counting.
 */
public enum CountQualifier {
	/** Urnetelling */
	PROTOCOL("P"),
	/** Foreløpig telling */
	PRELIMINARY("F"),
	/** Endelig telling */
	FINAL("E");

	/**
	 * @return count qualifier for id
	 * @throws java.lang.IllegalArgumentException for unknown count qualifier id
	 */
	public static CountQualifier fromId(String id) {
		switch (id) {
			case "P":
				return PROTOCOL;
			case "F":
				return PRELIMINARY;
			case "E":
				return FINAL;
			default:
				throw new IllegalArgumentException(format("unknown count qualifier id: <%s>", id));
		}
	}

	private final String id;

	private CountQualifier(String id) {
		this.id = id;
	}

	/**
	 * @return count qualifier id mapped in the database
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return text id for this count qualifier used in GUI
	 */
	public String getName() {
		return "@count_qualifier[" + id + "].name";
	}
}
