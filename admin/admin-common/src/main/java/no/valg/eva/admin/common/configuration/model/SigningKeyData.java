package no.valg.eva.admin.common.configuration.model;

import java.io.Serializable;

public class SigningKeyData implements Serializable {
	private String electionEventId;
	private String electionEventName;
	private String keyDomainId;
	private String keyDomainName;
	private Long signingKeyPk;
	private String fileName;

	public SigningKeyData(String electionEventId, String electionEventName, String keyDomainId, String keyDomainName) {
		this.electionEventId = electionEventId;
		this.electionEventName = electionEventName;
		this.keyDomainId = keyDomainId;
		this.keyDomainName = keyDomainName;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getElectionEventName() {
		return electionEventName;
	}

	public String getKeyDomainId() {
		return keyDomainId;
	}

	public String getKeyDomainName() {
		return keyDomainName;
	}

	public Long getSigningKeyPk() {
		return signingKeyPk;
	}

	public void setSigningKeyPk(Long signingKeyPk) {
		this.signingKeyPk = signingKeyPk;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
