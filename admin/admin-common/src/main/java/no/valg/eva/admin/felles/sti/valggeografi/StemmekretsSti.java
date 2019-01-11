package no.valg.eva.admin.felles.sti.valggeografi;

import java.util.regex.Pattern;

import no.valg.eva.admin.common.AreaPath;

public class StemmekretsSti extends ValggeografiSti<BydelSti> {
	public static final String REGEX = "^\\d{4}$";
	private static final Pattern PATTERN = Pattern.compile(REGEX);

	public StemmekretsSti(BydelSti bydelSti, String stemmekretsId) {
		super(PATTERN, bydelSti, stemmekretsId);
		validerNull(bydelSti, "Mangler bydelSti");
		validerPattern(stemmekretsId, "Ugyldig stemmekretsId: %s");
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
		return sisteId();
	}

	public StemmestedSti stemmestedSti(String stemmestedId) {
		return new StemmestedSti(this, stemmestedId);
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
		return forelderSti();
	}

	public boolean erKommunekretsen() {
		return stemmekretsId().equals(AreaPath.MUNICIPALITY_POLLING_DISTRICT_ID);
	}
}
