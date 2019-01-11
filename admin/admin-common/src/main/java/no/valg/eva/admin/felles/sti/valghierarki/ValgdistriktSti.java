package no.valg.eva.admin.felles.sti.valghierarki;

import java.util.regex.Pattern;

public class ValgdistriktSti extends ValghierarkiSti<ValgSti> {
	public static final String REGEX = "^\\d{6}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	
	public ValgdistriktSti(String valghendelseId, String valggruppeId, String valgId, String valgdistriktId) {
		this(new ValgSti(valghendelseId, valggruppeId, valgId), valgdistriktId);
	}

	public ValgdistriktSti(ValgSti valgSti, String valgdistriktId) {
		super(PATTERN, valgSti, valgdistriktId);
		validerNull(valgSti, "Mangler valgId");
		validerPattern(valgdistriktId, "Ugyldig valgdistriktId: %s");
	}

	public String valghendelseId() {
		return forelderSti().valghendelseId();
	}

	public String valggruppeId() {
		return forelderSti().valggruppeId();
	}

	public String valgId() {
		return forelderSti().valgId();
	}

	public String valgdistriktId() {
		return sisteId();
	}

	@Override
	public ValghendelseSti valghendelseSti() {
		return forelderSti().valghendelseSti();
	}

	public ValggruppeSti valggruppeSti() {
		return forelderSti().valggruppeSti();
	}

	public ValgSti valgSti() {
		return forelderSti();
	}
}
