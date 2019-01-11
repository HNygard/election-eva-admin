package no.valg.eva.admin.frontend.security;

import java.io.Serializable;

import no.evote.security.SecurityLevel;

public class TmpLoginForm implements Serializable {

	private String uid;
	private SecurityLevel securityLevel;
	private boolean scanning;

	public TmpLoginForm(String uid, SecurityLevel securityLevel, boolean scanning) {
		this.uid = uid;
		this.securityLevel = securityLevel;
		this.scanning = scanning;
	}

	public String getUid() {
		return uid;
	}

	public SecurityLevel getSecurityLevel() {
		return securityLevel;
	}

	public boolean isScanning() {
		return scanning;
	}
}
