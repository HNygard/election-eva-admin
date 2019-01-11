package no.valg.eva.admin.felles.sti.valggeografi;

import java.util.regex.Pattern;

public class StemmestedSti extends ValggeografiSti<StemmekretsSti> {
	public static final String REGEX = "^\\d{4}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public StemmestedSti(StemmekretsSti stemmekretsSti, String stemmestedId) {
		super(PATTERN, stemmekretsSti, stemmestedId);
		validerNull(stemmekretsSti, "Mangler stemmekretsSti");
		validerPattern(stemmestedId, "Ugyldig stemmestedId: %s");
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
		return forelderSti().bydelId();
	}

	public String stemmekretsId() {
		return forelderSti().stemmekretsId();
	}

	public String stemmestedId() {
		return sisteId();
	}

	public RodeSti rodeSti(String rodeId) {
		return new RodeSti(this, rodeId);
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
		return forelderSti().kommuneSti();
	}

	public BydelSti bydelSti() {
		return forelderSti().bydelSti();
	}

	public StemmekretsSti stemmekretsSti() {
		return forelderSti();
	}
}
