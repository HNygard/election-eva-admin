package no.valg.eva.admin.common.mockups;

import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Municipality;

/**
 */
public final class BoroughMockups {
	public static final String BOROUGH_NAME_GAMLE_OSLO = "Gamle Oslo";
	public static final String BOROUGH_NAME_VESTRE_AKER = "Vestre Aker";
	private static final String BOROUGH_ID_GAMLE_OSLO = "030301";
	private static final String BOROUGH_ID_VESTRE_AKER = "030307";

	private BoroughMockups() {
	}
	
	public static Borough borough(Municipality municipality) {
		return new Borough(BOROUGH_ID_GAMLE_OSLO, BOROUGH_NAME_GAMLE_OSLO, municipality);
	}
	
	public static Borough boroughVestreAker() {
		return new Borough(BOROUGH_ID_VESTRE_AKER, BOROUGH_NAME_VESTRE_AKER, MunicipalityMockups.municipalityOslo());
	}

	public static Borough boroughGamleOslo() {
		return new Borough(BOROUGH_ID_GAMLE_OSLO, BOROUGH_NAME_GAMLE_OSLO, MunicipalityMockups.municipalityOslo());
	}
}
