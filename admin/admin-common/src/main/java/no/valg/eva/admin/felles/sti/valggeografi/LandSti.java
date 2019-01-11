package no.valg.eva.admin.felles.sti.valggeografi;

import java.util.regex.Pattern;

public class LandSti extends ValggeografiSti<ValghendelseSti> {
	public static final String REGEX = "^\\d{2}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	
	public LandSti(String valghendelseId, String landId) {
		this(new ValghendelseSti(valghendelseId), landId);
	}

	public LandSti(ValghendelseSti valghendelseSti, String landId) {
		super(PATTERN, valghendelseSti, landId);
		validerNull(valghendelseSti, "Mangler valghendelseSti");
		validerPattern(landId, "Ugyldig landId: %s");
	}

	public String valghendelseId() {
		return forelderSti().valghendelseId();
	}

	public String landId() {
		return sisteId();
	}

	public FylkeskommuneSti fylkeskommuneSti(String fylkeskommuneId) {
		return new FylkeskommuneSti(this, fylkeskommuneId);
	}

	@Override
	public ValghendelseSti valghendelseSti() {
		return forelderSti();
	}
}
