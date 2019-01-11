package no.valg.eva.admin.common.rbac;

import java.io.Serializable;

/**
 * Dto for generelle bruker(/operator)parametre. Brukes ved import av nye brukere.
 */
public abstract class ImportOperatorRoleInfo implements Serializable {

    private String operatorId;
    private String firstName;
    private String lastName;
    private final String email;
    private final String telephoneNumber;

    public ImportOperatorRoleInfo(String operatorId, String firstName, String lastName, String email, String telephoneNumber) {
        this.operatorId = operatorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.telephoneNumber = telephoneNumber;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

	public void setName(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public abstract String getAreaId();

	public abstract boolean areaIsPollingDistrict();
}
