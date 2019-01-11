package no.valg.eva.admin.felles.sti.valggeografi;

import java.util.regex.Pattern;

public class FylkeskommuneSti extends ValggeografiSti<LandSti> {
	public static final String REGEX = "^\\d{2}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	
	public FylkeskommuneSti(String valghendelseId, String landId, String fylkeskommuneId) {
		this(new LandSti(valghendelseId, landId), fylkeskommuneId);
	}

	public FylkeskommuneSti(LandSti landSti, String fylkeskommuneId) {
		super(PATTERN, landSti, fylkeskommuneId);
		validerNull(landSti, "Mangler landSti");
		validerPattern(fylkeskommuneId, "Ugyldig fylkeskommuneId: %s");
	}

	public String valghendelseId() {
		return forelderSti().valghendelseId();
	}

	public String landId() {
		return forelderSti().landId();
	}

	public String fylkeskommuneId() {
		return sisteId();
	}

	public KommuneSti kommuneSti(String kommuneId) {
		return new KommuneSti(this, kommuneId);
	}

	@Override
	public ValghendelseSti valghendelseSti() {
		return forelderSti().valghendelseSti();
	}

	public LandSti landSti() {
		return forelderSti();
	}
}
