package no.valg.eva.admin.felles.sti.valggeografi;

import static java.lang.String.format;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.RODE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;

import java.util.regex.Pattern;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.felles.sti.Sti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

public abstract class ValggeografiSti<F extends ValggeografiSti<? extends ValggeografiSti>> extends Sti<F> {
	public ValggeografiSti(Pattern patternForValideringAvId, F forelderSti, String sisteId) {
		super(patternForValideringAvId, forelderSti, sisteId);
	}

	public static ValggeografiSti fra(AreaPath areaPath) {
		AreaLevelEnum level = areaPath.getLevel();
		switch (level) {
			case ROOT:
				return valghendelseSti(areaPath);
			case COUNTRY:
				return landSti(areaPath);
			case COUNTY:
				return fylkeskommuneSti(areaPath);
			case MUNICIPALITY:
				return kommuneSti(areaPath);
			case BOROUGH:
				return bydelSti(areaPath);
			case POLLING_DISTRICT:
				return stemmekretsSti(areaPath);
			case POLLING_PLACE:
				return stemmestedSti(areaPath);
			case POLLING_STATION:
				return rodeSti(areaPath);
			default:
				throw new IllegalArgumentException(format("ukjent nivå: %s", level));
		}
	}

	public static ValghendelseSti valghendelseSti(AreaPath areaPath) {
		return new ValghendelseSti(areaPath.getElectionEventId());
	}

	public static LandSti landSti(AreaPath areaPath) {
		return new LandSti(valghendelseSti(areaPath), areaPath.getCountryId());
	}

	public static FylkeskommuneSti fylkeskommuneSti(AreaPath areaPath) {
		return new FylkeskommuneSti(landSti(areaPath), areaPath.getCountyId());
	}

	public static KommuneSti kommuneSti(AreaPath areaPath) {
		return new KommuneSti(fylkeskommuneSti(areaPath), areaPath.getMunicipalityId());
	}

	public static BydelSti bydelSti(AreaPath areaPath) {
		return new BydelSti(kommuneSti(areaPath), areaPath.getBoroughId());
	}

	public static StemmekretsSti stemmekretsSti(AreaPath areaPath) {
		return new StemmekretsSti(bydelSti(areaPath), areaPath.getPollingDistrictId());
	}

	public static StemmestedSti stemmestedSti(AreaPath areaPath) {
		return new StemmestedSti(stemmekretsSti(areaPath), areaPath.getPollingPlaceId());
	}

	public static RodeSti rodeSti(AreaPath areaPath) {
		return new RodeSti(stemmestedSti(areaPath), areaPath.getPollingStationId());
	}

	public AreaPath areaPath() {
		return AreaPath.from(toString());
	}

	public abstract ValghendelseSti valghendelseSti();

	public ValggeografiNivaa nivaa() {
		if (this instanceof ValghendelseSti) {
			return VALGHENDELSE;
		}
		if (this instanceof LandSti) {
			return LAND;
		}
		if (this instanceof FylkeskommuneSti) {
			return FYLKESKOMMUNE;
		}
		if (this instanceof KommuneSti) {
			return KOMMUNE;
		}
		if (this instanceof BydelSti) {
			return BYDEL;
		}
		if (this instanceof StemmekretsSti) {
			return STEMMEKRETS;
		}
		if (this instanceof StemmestedSti) {
			return STEMMESTED;
		}
		if (this instanceof RodeSti) {
			return RODE;
		}
		throw new IllegalStateException(format("ukjent stitype: %s", getClass()));
	}

	public boolean isValghendelseSti() {
		return nivaa() == VALGHENDELSE;
	}

	public boolean isStemmekretsSti() {
		return nivaa() == STEMMEKRETS;
	}

	public ValghendelseSti tilValghendelseSti() {
		return tilSti(VALGHENDELSE);
	}

	public LandSti tilLandSti() {
		return tilSti(LAND);
	}

	public FylkeskommuneSti tilFylkeskommuneSti() {
		return tilSti(FYLKESKOMMUNE);
	}

	public KommuneSti tilKommuneSti() {
		return tilSti(KOMMUNE);
	}

	public BydelSti tilBydelSti() {
		return tilSti(BYDEL);
	}

	public StemmekretsSti tilStemmekretsSti() {
		return tilSti(STEMMEKRETS);
	}

	public StemmestedSti tilStemmestedSti() {
		return tilSti(STEMMESTED);
	}

	public RodeSti tilRodeSti() {
		return tilSti(RODE);
	}

	@SuppressWarnings("unchecked")
	private <S extends ValggeografiSti> S tilSti(ValggeografiNivaa valggeografiNivaa) {
		if (nivaa() == valggeografiNivaa) {
			return (S) this;
		}
		throw new IllegalStateException(format("forventet sti på nivå <%s>, men var på nivå <%s>", valggeografiNivaa, nivaa()));
	}
}
