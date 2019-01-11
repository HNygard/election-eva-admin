package no.valg.eva.admin.configuration.domain.model.valgnatt;

/**
 * PollingDistrict data for Valgnatt
 */
public abstract class PollingDistrictData {

	protected final String municipalityId;
	protected final String municipalityName;
	protected final String countyName;
	protected final String countyId;
	protected final String pollingDistrictId;
	protected final String pollingDistrictName;
	protected final Long pollingDistrictPk;
	protected final String boroughId;
	protected final String boroughName;
	protected final String valgdistriktId;
	protected final String valgdistriktName;
	protected final Integer contestPk;

	public PollingDistrictData(Integer pollingDistrictPk, String boroughName, String municipalityId, String municipalityName, String pollingDistrictId,
							   String countyId, String boroughId, String countyName, String pollingDistrictName, String valgdistriktId, String valgdistriktName, Integer
									   contestPk) {
		this.boroughName = boroughName;
		this.municipalityId = municipalityId;
		this.municipalityName = municipalityName;
		this.pollingDistrictId = pollingDistrictId;
		this.pollingDistrictPk = pollingDistrictPk.longValue();
		this.countyId = countyId;
		this.boroughId = boroughId;
		this.countyName = countyName;
		this.pollingDistrictName = pollingDistrictName;
		this.valgdistriktId = valgdistriktId;
		this.valgdistriktName = valgdistriktName;
		this.contestPk = contestPk;
	}

	public Long getPollingDistrictPk() {
		return pollingDistrictPk;
	}

	public String getPollingDistrictId() {
		return pollingDistrictId;
	}

	public String getPollingDistrictName() {
		return pollingDistrictName;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public String getMunicipalityName() {
		return municipalityName;
	}

	public String getCountyId() {
		return countyId;
	}

	public String getCountyName() {
		return countyName;
	}

	public String getBoroughId() {
		return boroughId;
	}

	public String getBoroughName() {
		return boroughName;
	}

	public String getValgdistriktId() {
		return valgdistriktId;
	}

	public String getValgdistriktName() {
		return valgdistriktName;
	}

	public Integer getContestPk() {
		return contestPk;
	}
}
