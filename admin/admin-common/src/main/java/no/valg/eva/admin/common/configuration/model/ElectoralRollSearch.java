package no.valg.eva.admin.common.configuration.model;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

public class ElectoralRollSearch implements Serializable {

	private static final int SSN_LENGTH = 11;

	private String ssn;
	private LocalDate birthDate;
	private String name;

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isValid() {
		return hasValidSsn() || hasValidBirthDateAndOrName();
	}

	public boolean hasValidSsn() {
		return ssn != null && ssn.length() == SSN_LENGTH;
	}

	public boolean hasValidBirthDateAndOrName() {
		return birthDate != null || !StringUtils.isEmpty(name);
	}
}
