package no.valg.eva.admin.felles.sti.valghierarki;

import java.util.regex.Pattern;

public class ValgSti extends ValghierarkiSti<ValggruppeSti> {
	public static final String REGEX = "^\\d{2}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public ValgSti(String valghendelseId, String valggruppeId, String valgId) {
		this(new ValggruppeSti(valghendelseId, valggruppeId), valgId);
	}
	
	public ValgSti(ValggruppeSti valggruppeSti, String valgId) {
		super(PATTERN, valggruppeSti, valgId);
		validerNull(valggruppeSti, "Mangler valggruppeSti");
		validerPattern(valgId, "Ugyldig valgId: %s");
	}

	public String valghendelseId() {
		return forelderSti().valghendelseId();
	}

	public String valggruppeId() {
		return forelderSti().valggruppeId();
	}

	public String valgId() {
		return sisteId();
	}

	public ValgdistriktSti valgdistriktSti(String valgdistriktId) {
		return new ValgdistriktSti(this, valgdistriktId);
	}

	@Override
	public ValghendelseSti valghendelseSti() {
		return forelderSti().valghendelseSti();
	}

	public ValggruppeSti valggruppeSti() {
		return forelderSti();
	}
}
