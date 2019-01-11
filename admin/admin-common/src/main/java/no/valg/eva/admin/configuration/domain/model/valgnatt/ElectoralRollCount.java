package no.valg.eva.admin.configuration.domain.model.valgnatt;

import java.math.BigInteger;

/**
 * Contains resultdata for electoral roll query
 */
public class ElectoralRollCount extends PollingDistrictData {

	private final int voterTotal;
	private final boolean parentPollingDistrict;

	public ElectoralRollCount(String municipalityId, String municipalityName, String countyName, String countyId, String pollingDistrictId,
							  String pollingDistrictName, BigInteger voterTotal, boolean parentPollingDistrict, Integer pollingDistrictPk, String boroughId, String
									  boroughName,
							  String valgdistriktId, String valgdistriktName, Integer contestPk) {
		super(pollingDistrictPk, boroughName, municipalityId, municipalityName, pollingDistrictId, countyId, boroughId, countyName, pollingDistrictName,
				valgdistriktId, valgdistriktName, contestPk);
		this.voterTotal = voterTotal.intValue();
		this.parentPollingDistrict = parentPollingDistrict;
	}

	public static ElectoralRollCount emptyInstance(Long pollingDistrictPk, String pollingDistrictId, String pollingDistrictName, String municipalityId,
												   String municipalityName, String countyName, String countyId, String boroughId, String boroughName, String
														   valgdistriktId, String valgdistriktName, Integer contestPk) {
		return new ElectoralRollCount(municipalityId, municipalityName, countyName, countyId, pollingDistrictId, pollingDistrictName, BigInteger.ZERO, true,
				pollingDistrictPk.intValue(), boroughId, boroughName, valgdistriktId, valgdistriktName, contestPk);
	}

	/**
	 * @param electoralRollCount
	 * @return result with voters added, valgdistrikt is determined by the "child" added.
	 */
	public ElectoralRollCount add(ElectoralRollCount electoralRollCount) {
		return new ElectoralRollCount(
				this.municipalityId,
				this.getMunicipalityName(),
				this.getCountyName(),
				this.countyId,
				this.pollingDistrictId,
				this.pollingDistrictName,
				asBigInt(voterTotal + electoralRollCount.getVoterTotal()),
				this.parentPollingDistrict,
				this.pollingDistrictPk.intValue(),
				this.boroughId,
				this.boroughName,
				this.valgdistriktId,
				this.valgdistriktName,
				this.contestPk);
	}

	private BigInteger asBigInt(int i) {
		return BigInteger.valueOf(i);
	}

	public int getVoterTotal() {
		return voterTotal;
	}

	public ElectoralRollCount setVoterTotal(Integer voterTotal) {
		return new ElectoralRollCount(
				this.municipalityId,
				this.municipalityName,
				this.getCountyName(),
				this.countyId,
				this.pollingDistrictId,
				this.pollingDistrictName,
				asBigInt(voterTotal),
				this.parentPollingDistrict,
				this.pollingDistrictPk.intValue(),
				this.boroughId,
				this.boroughName,
				this.valgdistriktId,
				this.valgdistriktName,
				this.contestPk
		);
	}

	public ElectoralRollCount withValgdistrikt(String valgdistriktId, String valgdistriktName) {
		return new ElectoralRollCount(
				this.municipalityId,
				this.municipalityName,
				this.getCountyName(),
				this.countyId,
				this.pollingDistrictId,
				this.pollingDistrictName,
				asBigInt(voterTotal),
				this.parentPollingDistrict,
				this.pollingDistrictPk.intValue(),
				this.boroughId,
				this.boroughName,
				valgdistriktId,
				valgdistriktName,
				this.contestPk
		);
	}
}
