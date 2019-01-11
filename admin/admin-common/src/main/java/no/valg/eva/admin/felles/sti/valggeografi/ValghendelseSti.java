package no.valg.eva.admin.felles.sti.valggeografi;

import java.util.regex.Pattern;

public class ValghendelseSti extends ValggeografiSti<ValghendelseSti> {
	public static final String REGEX = "^\\d{6}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public ValghendelseSti(String valghendelseId) {
		super(PATTERN, null, valghendelseId);
		validerPattern(valghendelseId, "Ugyldig valghendelseId: %s");
	}

	public String valghendelseId() {
		return sisteId();
	}

	public LandSti landSti(String landId) {
		return new LandSti(this, landId);
	}

	@Override
	public ValghendelseSti valghendelseSti() {
		return this;
	}

	public no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti tilValghierarkiSti() {
		return new no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti(valghendelseId());
	}
}
