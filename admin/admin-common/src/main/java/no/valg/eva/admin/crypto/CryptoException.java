package no.valg.eva.admin.crypto;

public class CryptoException extends Exception {
	public CryptoException(String message) {
		super(message);
	}
	
	public CryptoException(String message, Throwable exception) {
		super(message, exception);
	}
}
