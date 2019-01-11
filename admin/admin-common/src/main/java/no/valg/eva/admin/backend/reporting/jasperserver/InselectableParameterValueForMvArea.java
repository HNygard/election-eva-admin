package no.valg.eva.admin.backend.reporting.jasperserver;

import java.io.Serializable;

public class InselectableParameterValueForMvArea implements Serializable {
	private String userRoleMvAreaRegExp;

	public InselectableParameterValueForMvArea(String userRoleMvAreaRegExp) {
		this.userRoleMvAreaRegExp = userRoleMvAreaRegExp;
	}

	public boolean isUnselectableForValueAndUserPath(String userRoleAreaPath) {
		return userRoleMvAreaRegExp == null || userRoleAreaPath.matches(userRoleMvAreaRegExp);
	}
}
