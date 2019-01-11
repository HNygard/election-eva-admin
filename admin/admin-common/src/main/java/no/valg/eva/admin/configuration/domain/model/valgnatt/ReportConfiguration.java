package no.valg.eva.admin.configuration.domain.model.valgnatt;

import no.valg.eva.admin.common.AreaPath;

/**
 * Valgnatt report configuration for polling district.
 */
public class ReportConfiguration extends PollingDistrictData {

	private final boolean parent;
	private final boolean byMunicipality;
	private final Integer mvAreaPk;

	public ReportConfiguration(Integer pollingDistrictPk, String pollingDistrictId, String pollingDistrictName, String municipalityId, String municipalityName,
							   Boolean parent, Boolean byMunicipality, String countyId, String countyName, String boroughId, String boroughName, String valgdistriktId,
							   String valgdistriktName, Integer mvAreaPk, Integer contestPk) {
		super(pollingDistrictPk, boroughName, municipalityId, municipalityName, pollingDistrictId, countyId, boroughId, countyName, pollingDistrictName,
				valgdistriktId, valgdistriktName, contestPk);
		this.parent = parent;
		this.byMunicipality = byMunicipality;
		this.mvAreaPk = mvAreaPk;
	}

	public boolean isMunicipalityPollingDistrict() {
		return AreaPath.MUNICIPALITY_POLLING_DISTRICT_ID.equals(pollingDistrictId);
	}

	public boolean isByMunicipality() {
		return byMunicipality;
	}

	public boolean isParent() {
		return parent;
	}

	public Integer getMvAreaPk() {
		return mvAreaPk;
	}
}
