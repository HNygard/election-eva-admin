package no.valg.eva.admin.felles.sti.valghierarki;

import java.util.regex.Pattern;

public class ValghendelseSti extends ValghierarkiSti<ValghendelseSti> {
	public static final String REGEX = "^\\d{6}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public ValghendelseSti(String valghendelseId) {
		super(PATTERN, null, valghendelseId);
		validerPattern(valghendelseId, "Ugyldig valghendelseId: %s");
	}

	public String valghendelseId() {
		return sisteId();
	}

	public ValggruppeSti valggruppeSti(String valggruppeId) {
		return new ValggruppeSti(this, valggruppeId);
	}

	@Override
	public ValghendelseSti valghendelseSti() {
		return this;
	}
}
