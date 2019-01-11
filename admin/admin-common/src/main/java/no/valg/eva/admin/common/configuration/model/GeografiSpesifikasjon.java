package no.valg.eva.admin.common.configuration.model;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Objects;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.configuration.domain.model.MvArea;

public class GeografiSpesifikasjon {
	
	private static final int LENGDE_FYLKESID = 2;
	private static final int LENGDE_KOMMUNEID = 4;
	private static final int INDEX_START_KRETSID = 5;
	private static final int LENGDE_KRETSID = 4;
	private static final int INDEX_START_STEMMESTEDSID = 10;
	private static final int LENGDE_STEMMESTEDSID = 4;

	private List<String> beholdKommuner;
	private List<String> beholdKretser;
	private List<String> beholdStemmesteder;
	
	public GeografiSpesifikasjon(List<String> beholdKommuner, List<String> beholdKretser) {
		this(beholdKommuner, beholdKretser, emptyList());
	}

	public GeografiSpesifikasjon(List<String> beholdKommuner, List<String> beholdKretser, List<String> beholdStemmesteder) {
		this.beholdKommuner = beholdKommuner;
		this.beholdKretser = beholdKretser;
		this.beholdStemmesteder = beholdStemmesteder;
	}

	public List<String> getBeholdKommuner() {
		return beholdKommuner;
	}

	public List<String> getBeholdKretser() {
		return beholdKretser;
	}

	public boolean referererFylke(String fylkesId) {
		return kommunerReferererFylke(fylkesId)
			|| kretserReferererFylke(fylkesId)
			|| stemmestederReferererFylke(fylkesId);
	}

	private boolean kommunerReferererFylke(String fylkesId) {
		return beholdKommuner.stream().anyMatch(kommuneId -> kommuneId.substring(0, LENGDE_FYLKESID).equals(fylkesId));
	}

	private boolean kretserReferererFylke(String fylkesId) {
		return beholdKretser.stream().anyMatch(kommuneOgKretsId -> kommuneOgKretsId.substring(0, LENGDE_FYLKESID).equals(fylkesId));
	}

	private boolean stemmestederReferererFylke(String fylkesId) {
		return beholdStemmesteder.stream().anyMatch(kommuneKretsOgStemmestedId -> kommuneKretsOgStemmestedId.substring(0, LENGDE_FYLKESID).equals(fylkesId));
	}

	public boolean referererKommune(String kommuneId) {
		return kommunerReferererKommune(kommuneId)
			|| kretserReferererKrets(kommuneId, null)
			|| stemmestederReferererStemmested(kommuneId, null, null);
	}

	private boolean kommunerReferererKommune(String kommuneId) {
		return beholdKommuner.stream().anyMatch(enKommuneId -> enKommuneId.substring(0, LENGDE_KOMMUNEID).equals(kommuneId));
	}

	private boolean kretserReferererKrets(String kommuneId, String kretsId) {
		return beholdKretser.stream().anyMatch(kommuneOgKretsId
			-> kommuneOgKretsId.substring(0, LENGDE_KOMMUNEID).equals(kommuneId) && kretsErNullEllerMatcher(kretsId, kommuneOgKretsId));
	}

	private boolean kretsErNullEllerMatcher(String kretsId, String kommuneKretsOgStemmestedId) {
		return kommuneKretsOgStemmestedId.substring(INDEX_START_KRETSID, INDEX_START_KRETSID + LENGDE_KRETSID).equals(kretsId) || kretsId == null;
	}

	private boolean stemmestederReferererStemmested(String kommuneId, String kretsId, String stemmestedId) {
		return beholdStemmesteder.stream().anyMatch(kommuneKretsOgStemmestedId ->
			kommuneKretsOgStemmestedId.substring(0, LENGDE_KOMMUNEID).equals(kommuneId)
				&& kretsErNullEllerMatcher(kretsId, kommuneKretsOgStemmestedId)
				&& stemmestedErNullEllerMatcher(stemmestedId, kommuneKretsOgStemmestedId));
	}

	private boolean stemmestedErNullEllerMatcher(String stemmestedId, String kommuneKretsOgStemmestedId) {
		return kommuneKretsOgStemmestedId.substring(INDEX_START_STEMMESTEDSID, INDEX_START_STEMMESTEDSID + LENGDE_STEMMESTEDSID).equals(stemmestedId)
			|| stemmestedId == null;
	}

	public boolean referererKrets(String kommuneId, String kretsId) {
		return kommunerReferererKommune(kommuneId)
			|| kretserReferererKrets(kommuneId, kretsId)
			|| stemmestederReferererStemmested(kommuneId, kretsId, null);
	}

	public boolean referererStemmested(String kommuneId, String kretsId, String stemmestedId) {
		return kommunerReferererKommune(kommuneId)
			|| kretserReferererKrets(kommuneId, kretsId)
			|| stemmestederReferererStemmested(kommuneId, kretsId, stemmestedId);
	}
	
	public boolean referererOmraade(MvArea omraade) {
		AreaLevelEnum omraadenivaa = omraade.getActualAreaLevel();
		switch (omraadenivaa) {
			case COUNTY:
				return referererFylke(omraade.getCountyId());
			case MUNICIPALITY:
				return referererKommune(omraade.getMunicipalityId());
			case POLLING_DISTRICT:
				return referererKrets(omraade.getMunicipalityId(), omraade.getPollingDistrictId());
			case POLLING_PLACE:
				return referererStemmested(omraade.getMunicipalityId(), omraade.getPollingDistrictId(), omraade.getPollingPlaceId());
			default:
				throw new IllegalArgumentException("Refanse til nivaa " + omraadenivaa + " er ikke implementert");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GeografiSpesifikasjon that = (GeografiSpesifikasjon) o;
		return Objects.equals(beholdKommuner, that.beholdKommuner)
			&& Objects.equals(beholdKretser, that.beholdKretser)
			&& Objects.equals(beholdStemmesteder, that.beholdStemmesteder);
	}

	@Override
	public int hashCode() {
		return Objects.hash(beholdKommuner, beholdKretser, beholdStemmesteder);
	}

	@Override
	public String toString() {
		return "GeografiSpesifikasjon{"
			+ "beholdKommuner=" + beholdKommuner
			+ ", beholdKretser=" + beholdKretser
			+ ", beholdStemmesteder=" + beholdStemmesteder
			+ '}';
	}
}
