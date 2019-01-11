package no.valg.eva.admin.common.settlement.model;

import java.io.Serializable;

import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.configuration.domain.model.MvArea;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CountingArea implements Serializable {
	private final String municipalityName;
	private final String boroughName;
	private final String pollingDistrictName;
	private final CountStatus countStatus;

	public CountingArea(String municipalityName, String boroughName, String pollingDistrictName, CountStatus countStatus) {
		this.municipalityName = municipalityName;
		this.boroughName = boroughName;
		this.pollingDistrictName = pollingDistrictName;
		this.countStatus = countStatus;
	}

	public CountingArea(MvArea countingMvArea, CountStatus countStatus) {
		this.municipalityName = countingMvArea.getMunicipalityName();
		this.boroughName = countingMvArea.getBoroughName();
		this.pollingDistrictName = countingMvArea.getPollingDistrictName();
		this.countStatus = countStatus;
	}

	public String getMunicipalityName() {
		return municipalityName;
	}

	public String getBoroughName() {
		return boroughName;
	}

	public String getPollingDistrictName() {
		return pollingDistrictName;
	}

	public String getName() {
		StringBuilder name = new StringBuilder(municipalityName);
		name.append(", ").append(boroughName);
		if (pollingDistrictName != null) {
			name.append(", ").append(pollingDistrictName);
		}
		return name.toString();
	}

	public CountStatus getCountStatus() {
		return countStatus;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		CountingArea rhs = (CountingArea) obj;
		return new EqualsBuilder()
				.append(this.municipalityName, rhs.municipalityName)
				.append(this.boroughName, rhs.boroughName)
				.append(this.pollingDistrictName, rhs.pollingDistrictName)
				.append(this.countStatus, rhs.countStatus)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(municipalityName)
				.append(boroughName)
				.append(pollingDistrictName)
				.append(countStatus)
				.toHashCode();
	}
}
