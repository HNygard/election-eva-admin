package no.valg.eva.admin.valgnatt.domain.model.resultat;

public enum ResultatType {
	TE("TE (Telling)"), OP("OP (Oppgjør)");

	private final String stringRepresentasjon;

	ResultatType(String stringRepresentasjon) {
		this.stringRepresentasjon = stringRepresentasjon;
	}

	public String toString() {
		return stringRepresentasjon;
	}
}
