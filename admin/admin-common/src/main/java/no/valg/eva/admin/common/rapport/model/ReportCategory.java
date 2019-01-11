package no.valg.eva.admin.common.rapport.model;

public enum ReportCategory {
	// @formatter:off
	GRUNNLAGSDATA("grunnlagsdata"),
	LISTEFORSLAG("listeforslag"),
	MANNTALL("manntall"),
	FORHÅNDSSTEMMEPERIODE("forhandsstemmer"),
	VALGTING("valgting"),
	OPPTELLING_ADMIN("opptelling_admin"),
	MØTEBØKER("moteboker"),
	ENDELIG_RESULTAT("endelig_resultat"),
	BEREGNINGER("beregninger"),
	VALGOPPGJØR_ADMIN("valgoppgjor_admin"),
	VALGHENDELSE_ADMIN("valghendelse_admin");
	// @formatter:on

	private final String key;

	ReportCategory(String key) {
		this.key = key;
	}

	public String getKey() {
		return "@rapport.kategori." + key;
	}
}
