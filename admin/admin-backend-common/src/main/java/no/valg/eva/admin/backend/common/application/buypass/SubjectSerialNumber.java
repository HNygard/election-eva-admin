package no.valg.eva.admin.backend.common.application.buypass;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

/**
 * Represents a serial number read from a certificate's distinguished name (DN).
 * Buypass encodes their serial numbers into the DN.
 */
public final class SubjectSerialNumber {
	static final String SERIAL_NUMBER_IDENTIFIER = "OID.2.5.4.5";

	private String serialNumberStr;
	private BigInteger serialNumber;

	private SubjectSerialNumber(String serialNumberStr) {
		this.serialNumberStr = requireNonNull(serialNumberStr);
		this.serialNumber = toBigInteger(serialNumberStr);
	}

	public BigInteger serialNumber() {
		return serialNumber;
	}

	public String asString() {
		return serialNumberStr;
	}

	public boolean matches(String serialNumberString) {
		if (serialNumberString == null || serialNumberString.trim().isEmpty()) {
			return false;
		}
		return serialNumber().equals(toBigInteger(serialNumberString));
	}

	public static SubjectSerialNumber fromCertificate(X509Certificate certificate) {
		X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
		return fromPrincipal(subjectPrincipal);
	}

	/**
	 * Principal's name must match {@value X500Principal#RFC1779}.
	 */
	public static SubjectSerialNumber fromPrincipal(X500Principal principal) {
		String dn = principal.getName(X500Principal.RFC1779);
		String serialNumberWithDashes = getSerialNumberFromDn(dn);
		if (serialNumberWithDashes == null) {
			throw new IllegalArgumentException("No serial number in DN: " + dn);
		}

		return new SubjectSerialNumber(serialNumberWithDashes);
	}

	private static String getSerialNumberFromDn(String dn) {
		String[] keyValuePairs = dn.split(",");
		for (String keyValuePair : keyValuePairs) {
			String[] keyAndValue = keyValuePair.trim().split("=");
			if (keyAndValue.length != 2) {
				throw new IllegalArgumentException("Not key=value format: " + dn);
			}

			String key = keyAndValue[0];
			String value = keyAndValue[1];

			if (SERIAL_NUMBER_IDENTIFIER.equals(key)) {
				return value;
			}
		}

		return null;
	}

	private static BigInteger toBigInteger(String serialNumberWithDashes) {
		return new BigInteger(removeDashes(serialNumberWithDashes));
	}

	private static String removeDashes(String serialNumberWithDashes) {
		return serialNumberWithDashes.replaceAll("-", "");
	}
}
