package no.valg.eva.admin.felles.sti.valggeografi;

import java.util.regex.Pattern;

public class BydelSti extends ValggeografiSti<KommuneSti> {
	public static final String REGEX = "^\\d{6}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public BydelSti(KommuneSti kommuneSti, String bydelId) {
		super(PATTERN, kommuneSti, bydelId);
		validerNull(kommuneSti, "Mangler kommuneSti");
		validerPattern(bydelId, "Ugyldig bydelId: %s");
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
		return forelderSti().kommuneId();
	}

	public String bydelId() {
		return sisteId();
	}

	public StemmekretsSti stemmekretsSti(String stemmekretsId) {
		return new StemmekretsSti(this, stemmekretsId);
	}

	@Override
	public ValghendelseSti valghendelseSti() {
		return forelderSti().valghendelseSti();
	}

	public LandSti landSti() {
		return forelderSti().landSti();
	}

	public FylkeskommuneSti fylkeskommuneSti() {
		return forelderSti().fylkeskommuneSti();
	}

	public KommuneSti kommuneSti() {
		return forelderSti();
	}
}
