package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.MUNICIPALITY_PK_SERIES;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Municipality;

public final class MunicipalityMockups {

	public static final long MUNICIPALITY_PK = MUNICIPALITY_PK_SERIES + 1;
	public static final String MUNICIPALITY_ID = "0101";
	public static final String MUNICIPALITY_ID_OSLO = AreaPath.OSLO_MUNICIPALITY_ID;
	public static final String MUNICIPALITY_NAME = "Halden";
	public static final String MUNICIPALITY_NAME_OSLO = "Oslo";
	public static final boolean REQUIRED_PROTOCOL_COUNT_TRUE = true;

	public static Municipality municipality(final boolean electronicMarkOffs) {
		Municipality municipality = mock(Municipality.class, RETURNS_DEEP_STUBS);
		when(municipality.getPk()).thenReturn(MUNICIPALITY_PK);
		when(municipality.getId()).thenReturn(MUNICIPALITY_ID);
		when(municipality.getName()).thenReturn(MUNICIPALITY_NAME);
		when(municipality.isElectronicMarkoffs()).thenReturn(electronicMarkOffs);
		when(municipality.isRequiredProtocolCount()).thenReturn(REQUIRED_PROTOCOL_COUNT_TRUE);
		return municipality;
	}
	
	public static Municipality municipalityOslo() {
		Municipality municipality = new Municipality();
		municipality.setPk(MUNICIPALITY_PK + 1);
		municipality.setId(MUNICIPALITY_ID_OSLO);
		municipality.setName(MUNICIPALITY_NAME_OSLO);
		return municipality;
	}

	private MunicipalityMockups() {
		// no instances allowed
	}
}
