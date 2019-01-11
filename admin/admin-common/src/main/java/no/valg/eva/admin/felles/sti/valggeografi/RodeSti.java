package no.valg.eva.admin.felles.sti.valggeografi;

import java.util.regex.Pattern;

public class RodeSti extends ValggeografiSti<StemmestedSti> {
	public static final String REGEX = "^\\d{2}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public RodeSti(StemmestedSti stemmestedSti, String rodeId) {
		super(PATTERN, stemmestedSti, rodeId);
		validerNull(stemmestedSti, "Mangler stemmestedSti");
		validerPattern(rodeId, "Ugyldig rodeId: %s");
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
		return forelderSti().stemmestedId();
	}

	public String rodeId() {
		return sisteId();
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
		return forelderSti().stemmekretsSti();
	}

	public StemmestedSti stemmestedSti() {
		return forelderSti();
	}
}
