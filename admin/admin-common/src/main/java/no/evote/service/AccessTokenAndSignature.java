package no.evote.service;

import java.io.Serializable;

import org.jdom2.Document;

public class AccessTokenAndSignature implements Serializable {

	private final Document accessToken;
	private final byte[] signature;

	public AccessTokenAndSignature(Document accessToken, byte[] signature) {
		this.accessToken = accessToken;
		this.signature = signature;
	}

	public Document getAccessToken() {
		return accessToken;
	}

	public byte[] getSignature() {
		return signature;
	}
}
