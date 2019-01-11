package no.valg.eva.admin.configuration.domain.model;

/**
 * Norwegian election types.
 */
public enum Valgtype {
	KOMMUNESTYREVALG("KO"), FYLKESTINGSVALG("FY"), STORTINGSVALG("ST"), SAMETINGSVALG("SA"), BYDELSVALG("BY"), LOKALSTYREVALG("LO");

	private final String id;

	Valgtype(String id) {
		this.id = id;
	}

    public String getId() {
        return id;
    }
}
