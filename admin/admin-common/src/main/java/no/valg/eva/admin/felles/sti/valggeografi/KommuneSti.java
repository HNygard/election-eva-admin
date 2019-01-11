package no.valg.eva.admin.felles.sti.valggeografi;

import java.util.regex.Pattern;

public class KommuneSti extends ValggeografiSti<FylkeskommuneSti> {
	public static final String REGEX = "^\\d{4}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);
	
	public KommuneSti(String valghendelseId, String landId, String fylkeskommuneId, String kommuneId) {
		this(new FylkeskommuneSti(valghendelseId, landId, fylkeskommuneId), kommuneId);
	}

	public KommuneSti(FylkeskommuneSti fylkeskommuneSti, String kommuneId) {
		super(PATTERN, fylkeskommuneSti, kommuneId);
		validerNull(fylkeskommuneSti, "Mangler fylkeskommuneSti");
		validerPattern(kommuneId, "Ugyldig kommunId: %s");
	}

	public String valghendelseId() {
		return forelderSti().valghendelseId();
	}

	public String landId() {
		return forelderSti().landId();
	}

	public String fylkeskommuneId() {
		return forelderSti().fylkeskommuneId();
	}

	public String kommuneId() {
		return sisteId();
	}

	public BydelSti bydelSti(String bydelId) {
		return new BydelSti(this, bydelId);
	}

	@Override
	public ValghendelseSti valghendelseSti() {
		return forelderSti().valghendelseSti();
	}

	public LandSti landSti() {
		return forelderSti().landSti();
	}

	public FylkeskommuneSti fylkeskommuneSti() {
		return forelderSti();
	}
}
