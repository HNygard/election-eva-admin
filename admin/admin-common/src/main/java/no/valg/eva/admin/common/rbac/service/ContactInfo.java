package no.valg.eva.admin.common.rbac.service;

import java.io.Serializable;

public class ContactInfo implements Serializable {
	private String phone;
	private String email;

	public ContactInfo(String phone, String email) {
		this.phone = phone;
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ContactInfo that = (ContactInfo) o;

		if (phone != null ? !phone.equals(that.phone) : that.phone != null) {
			return false;
		}
		if (email != null ? !email.equals(that.email) : that.email != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = phone != null ? phone.hashCode() : 0;
		result = 31 * result + (email != null ? email.hashCode() : 0);
		return result;
	}
}
