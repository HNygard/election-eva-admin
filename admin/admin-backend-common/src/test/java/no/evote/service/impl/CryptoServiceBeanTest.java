package no.evote.service.impl;

import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.service.CertificateService;
import no.evote.service.CryptoServiceBean;
import no.valg.eva.admin.backend.common.application.buypass.RevocationListHolder;
import no.valg.eva.admin.backend.common.domain.CertificateRevocationList;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.util.IOUtil;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

public class CryptoServiceBeanTest extends MockUtilsTestCase {
	private CryptoServiceBean cryptoService;
	private Collection<X509Certificate> scanningCertificates = new ArrayList<>();
	private Operator mockOperator;
	private Operator mockOperator2;
	
	@BeforeClass
	public void installerBouncyCastleProvider() {
		Security.addProvider(new BouncyCastleProvider());
	}

	@BeforeMethod
	public void setUp() throws Exception {
		cryptoService = initializeMocks(CryptoServiceBean.class);

		X509Certificate buyPassRootCACertificate = getX509Certificate("crypto-service-test/BPClass3RootCA1.pem");
		X509Certificate buyPassCACertificate = getX509Certificate("crypto-service-test/BPClass3CA3.pem");

		scanningCertificates.add(buyPassCACertificate);
		scanningCertificates.add(buyPassRootCACertificate);
		mockOperator = mock(Operator.class);
		when(mockOperator.getKeySerialNumber()).thenReturn("9578-4050-100147222");
		when(mockOperator.getId()).thenReturn("12345678901");
		mockOperator2 = mock(Operator.class);
		when(mockOperator2.getKeySerialNumber()).thenReturn("9578-4050-124732710");
		when(mockOperator2.getId()).thenReturn("12345678901");
	}

	@Test
	public void verifyScanningCountSignature_signatureIsABuypassSignatureCrlIsNotFound_returnsTrue() throws Exception {
		byte[] countFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip");
		byte[] signatureFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip.signature");

		when(getInjectMock(CertificateService.class).getScanningCertificateIssuersFromBundle()).thenReturn(scanningCertificates);
		when(getInjectMock(RevocationListHolder.class).getBuypassCrl(any(X500Principal.class))).thenReturn(null);

		cryptoService.verifyScanningCountSignature(countFileBytes, signatureFileBytes, mockOperator);
	}

	@Test(enabled = false, groups = { TestGroups.INTEGRATION, TestGroups.SLOW })
	public void verifyScanningCountSignature_signatureIsABuypassSignatureAndCertificateNotInCrl() throws Exception {
		byte[] countFileBytes = readResourceAsBytes("crypto-service-test/2015/Counts.zip");
		byte[] signatureFileBytes = readResourceAsBytes("crypto-service-test/2015/Counts.zip.signature");

		// proxy for "http://crl.buypass.no/crl/BPClass3CA3.crl"
		X509CRL x509CRL = getX509Crl(new URL("http://int-distro.eva.lokal/buypass/BPClass3CA3.crl"));

		CertificateRevocationList crl = new CertificateRevocationList(x509CRL);

		when(getInjectMock(CertificateService.class).getScanningCertificateIssuersFromBundle()).thenReturn(scanningCertificates);
		when(getInjectMock(RevocationListHolder.class).getBuypassCrl(any(X500Principal.class))).thenReturn(crl);

		cryptoService.verifyScanningCountSignature(countFileBytes, signatureFileBytes, mockOperator2);
	}

	private X509CRL getX509Crl(URL url) throws IOException, CRLException, CertificateException {
		try (InputStream inStream = url.openConnection().getInputStream()) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509CRL) cf.generateCRL(inStream);
		}
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Certificate serial number mismatch.*")
	public void verifyScanningCountSignature_signatureIsABuypassSignatureButSerialNumberIsWrong_returnsFalse() throws Exception {
		when(mockOperator.getKeySerialNumber()).thenReturn("0000000000000000");
		byte[] countFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip");
		byte[] signatureFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip.signature");

		when(getInjectMock(CertificateService.class).getScanningCertificateIssuersFromBundle()).thenReturn(scanningCertificates);
		when(getInjectMock(RevocationListHolder.class).getBuypassCrl(any(X500Principal.class))).thenReturn(null);

		cryptoService.verifyScanningCountSignature(countFileBytes, signatureFileBytes, mockOperator);

		fail();
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@count.error.operator_missing_subject_serial_number")
	public void verifyScanningCountSignature_operatorSubjectSerialNumberIsNull_throwsException() throws Exception {
		when(mockOperator.getKeySerialNumber()).thenReturn(null);
		byte[] countFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip");
		byte[] signatureFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip.signature");

		when(getInjectMock(CertificateService.class).getScanningCertificateIssuersFromBundle()).thenReturn(scanningCertificates);
		when(getInjectMock(RevocationListHolder.class).getBuypassCrl(any(X500Principal.class))).thenReturn(null);

		cryptoService.verifyScanningCountSignature(countFileBytes, signatureFileBytes, mockOperator);

		fail();
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Signing certificate has been revoked")
	public void verifyScanningCountSignature_signatureIsABuypassSignatureCrlValididationFailsDueToMissingProvider_returnsFalse() throws Exception {
		byte[] countFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip");
		byte[] signatureFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip.signature");
		byte[] encodedCrl = getEncodedCrl("certificate-revocation-list/crl-base64");

		when(getInjectMock(CertificateService.class).getScanningCertificateIssuersFromBundle()).thenReturn(scanningCertificates);
		CertificateRevocationList fakeCrl = mock(CertificateRevocationList.class);
		when(getInjectMock(RevocationListHolder.class).getBuypassCrl(any(X500Principal.class))).thenReturn(fakeCrl);
		when(fakeCrl.getEncodedCrl()).thenReturn(encodedCrl);

		cryptoService.verifyScanningCountSignature(countFileBytes, signatureFileBytes, mockOperator);

		fail();
	}

	private byte[] getEncodedCrl(String resourcePath) throws Exception {
		try (BufferedReader reader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(resourcePath).getFile()))) {
			return Base64.decode(reader);
		}
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Signature validation failed")
	public void verifyFailureWhenSignatureIsNotValidPEM() throws Exception {
		byte[] countFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip");
		byte[] signatureFileBytes = readResourceAsBytes("crypto-service-test/Counts-not-valid-signature.zip.signature");

		when(getInjectMock(CertificateService.class).getScanningCertificateIssuersFromBundle()).thenReturn(scanningCertificates);

		cryptoService.verifyScanningCountSignature(countFileBytes, signatureFileBytes, mockOperator);

		fail();
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Signature validation failed")
	public void verifyFailureWhenSignatureIsManipulated() throws Exception {
		byte[] countFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip");
		byte[] signatureFileBytes = readResourceAsBytes("crypto-service-test/Counts-manipulated.zip.signature");

		when(getInjectMock(CertificateService.class).getScanningCertificateIssuersFromBundle()).thenReturn(scanningCertificates);

		cryptoService.verifyScanningCountSignature(countFileBytes, signatureFileBytes, mockOperator);

		fail();
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Signature validation failed")
	public void verifyScanningCountSignature_withInvalidScanningBundle_shouldReturnFalse() throws Exception {
		byte[] countFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip");
		byte[] signatureFileBytes = readResourceAsBytes("crypto-service-test/Counts.zip.signature");

		List<X509Certificate> scanningCerts = new ArrayList<>();
		scanningCerts.add(getX509Certificate("crypto-service-test/BPClass3RootCA.pem"));
		when(getInjectMock(CertificateService.class).getScanningCertificateIssuersFromBundle()).thenReturn(scanningCerts);

		cryptoService.verifyScanningCountSignature(countFileBytes, signatureFileBytes, mockOperator);

		fail();
	}

	private byte[] readResourceAsBytes(final String resourcePath) throws IOException {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			return IOUtil.getBytes(is);
		}
	}

	private X509Certificate getX509Certificate(String resourcePath) throws IOException, CertificateException {
		try (InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(inStream);
		}
	}

	@Test
	public void testEncryptAndDecryptSymmetrically() throws UnsupportedEncodingException, Base64DecodingException {
		String systemPassword = "funkerdetta";
		byte[] p12passwordInPlaintext = "TDBN4WHHDONJ35ZVWJ4A".getBytes(EvoteConstants.CHARACTER_SET);
		byte[] encryptedP12password = cryptoService.encryptSymmetricallyOpenSSL(p12passwordInPlaintext, systemPassword);
		byte[] decryptedP12password = cryptoService.decryptSymmetricallyOpenSSL(encryptedP12password, systemPassword);

		String encryptedP12passwordString = Base64.encode(encryptedP12password);
		String decryptedP12passwordString = new String(decryptedP12password, EvoteConstants.CHARACTER_SET);
		System.out.println("kryptert passord er " + encryptedP12passwordString);
		System.out.println("dekryptert passord er " + decryptedP12passwordString);

		String passordFraDatabasen1 = "U2FsdGVkX19bX73FsNLXxuEeNttNrqlN1GobnY3GrXUe31T9oykHifFJGnlic6cG";
		byte[] decryptedP12passwordDB1 = cryptoService.decryptSymmetricallyOpenSSL(Base64.decode(passordFraDatabasen1), "system");
		System.out.println("dekryptert passord fra DB er " + new String(decryptedP12passwordDB1, EvoteConstants.CHARACTER_SET));

		String passordFraDatabasen2 = "U2FsdGVkX1+dogAJNQh6rrpI8oVLxMkZK4UWdzBsmXH0rl6LuwSJHXQRyfO1wu3a";
		byte[] decryptedP12passwordDB2 = cryptoService.decryptSymmetricallyOpenSSL(Base64.decode(passordFraDatabasen2), "system");
		System.out.println("dekryptert passord fra DB er " + new String(decryptedP12passwordDB2, EvoteConstants.CHARACTER_SET));

		assertThat(decryptedP12passwordString).isEqualTo("TDBN4WHHDONJ35ZVWJ4A");
	}
}
