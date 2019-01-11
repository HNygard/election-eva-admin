package no.valg.eva.admin.common;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.Serializable;

/**
 * Value object for Address.
 */
public class Address implements Serializable {

	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String postalCode;
	private String postTown;
	private String municipality;

	public Address(String addressLine1, String addressLine2, String addressLine3, String postalCode, String postTown, String municipality) {
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.postalCode = postalCode;
		this.postTown = postTown;
		this.municipality = municipality;
	}

	/**
	 * @return Norwegian gateadresse
	 */
	public String streetAddress() {
		return addressLine1;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getAddressLine3() {
		return addressLine3;
	}

	public void setAddressLine3(String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPostTown() {
		return postTown;
	}

	public void setPostTown(String postTown) {
		this.postTown = postTown;
	}

	public String getMunicipality() {
		return municipality;
	}

	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}

	public String getShortDisplay() {
		StringBuilder adressBuilder = new StringBuilder();
		if (isNotBlank(addressLine1)) {
			adressBuilder.append(addressLine1).append(' ');
		} else if (isNotBlank(addressLine2)) {
			adressBuilder.append(addressLine2).append(' ');
		} else if (isNotBlank(addressLine3)) {
			adressBuilder.append(addressLine3).append(' ');
		}
		if (isNotBlank(municipality)) {
			adressBuilder.append(municipality);
		} else {
			if (isNotBlank(postalCode)) {
				adressBuilder.append(postalCode).append(' ');
			}
			if (isNotBlank(postTown)) {
				adressBuilder.append(postTown);
			}
		}
		return adressBuilder.toString().trim();
	}
}
