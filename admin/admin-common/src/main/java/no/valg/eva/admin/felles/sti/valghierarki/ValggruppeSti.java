package no.valg.eva.admin.felles.sti.valghierarki;

import java.util.regex.Pattern;

public class ValggruppeSti extends ValghierarkiSti<ValghendelseSti> {
	public static final String REGEX = "^\\d{2}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public ValggruppeSti(String valghendelseId, String valggruppeId) {
		this(new ValghendelseSti(valghendelseId), valggruppeId);
	}
	
	public ValggruppeSti(ValghendelseSti valghendelseSti, String valggruppeId) {
		super(PATTERN, valghendelseSti, valggruppeId);
		validerNull(valghendelseSti, "Mangler valghendelseSti");
		validerPattern(valggruppeId, "Ugyldig valggruppeId: %s");
	}

	public String valghendelseId() {
		return forelderSti().valghendelseId();
	}

	public String valggruppeId() {
		return sisteId();
	}

	@Override
	public ValghendelseSti valghendelseSti() {
		return forelderSti();
	}

	public ValgSti valgSti(String valgId) {
		return new ValgSti(this, valgId);
	}
}
