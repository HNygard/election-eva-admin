package no.valg.eva.admin.common.configuration.model.party;

public enum Partikategori {
	
	STORTING("1"), LANDSDEKKENDE("2"), LOKALT("3");

	private final String id;

	Partikategori(String id) {
		this.id = id;
	}

	/**
	 * @return text id for this category used in GUI
	 */
	public String getName() {
		return "@party_category[" + getId() + "].name";
	}

	public String getId() {
		return id;
	}

	public static Partikategori fromId(String id) {
		switch (id) {
		case "1":
			return STORTING;
		case "2":
			return LANDSDEKKENDE;
		case "3":
			return LOKALT;
		default:
			throw new IllegalArgumentException("No enum defined for id " + id);
		}
	}

}
