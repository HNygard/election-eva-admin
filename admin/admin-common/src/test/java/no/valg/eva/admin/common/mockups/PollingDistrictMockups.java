package no.valg.eva.admin.common.mockups;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.POLLING_DISTRICT_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

public final class PollingDistrictMockups {
	
	public static final long POLLING_DISTRICT_PK_1 = POLLING_DISTRICT_PK_SERIES + 1;
	public static final long POLLING_DISTRICT_PK_2 = POLLING_DISTRICT_PK_SERIES + 2;
	public static final long POLLING_DISTRICT_PK_3 = POLLING_DISTRICT_PK_SERIES + 3;
	
	public static final String POLLING_DISTRICT_ID = "0101";
	public static final String POLLING_DISTRICT_NAME = "Halden";

	public static PollingDistrict pollingDistrict() {
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setPk(POLLING_DISTRICT_PK_1);
		pollingDistrict.setId(POLLING_DISTRICT_ID);
		pollingDistrict.setName(POLLING_DISTRICT_NAME);

		return pollingDistrict;
	}

	public static PollingDistrict child1PollingDistrict() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setPk(POLLING_DISTRICT_PK_2);

		return pollingDistrict;
	}

	public static PollingDistrict child2PollingDistrict() {
		PollingDistrict pollingDistrict = pollingDistrict();
		pollingDistrict.setPk(POLLING_DISTRICT_PK_3);

		return pollingDistrict;
	}

	private PollingDistrictMockups() {
		// no instances allowed
	}
}
