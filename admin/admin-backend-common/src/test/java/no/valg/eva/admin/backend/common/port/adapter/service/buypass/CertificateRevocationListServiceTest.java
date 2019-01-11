package no.valg.eva.admin.backend.common.port.adapter.service.buypass;

import static no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListService.BUYPASS_CLASS_3_CA_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import javax.security.auth.x500.X500Principal;

import no.evote.service.CertificateService;

import org.apache.xml.security.utils.Base64;
import org.testng.annotations.Test;

public class CertificateRevocationListServiceTest {

	private static final X500Principal UNKNOWN_PRINCIPAL = new X500Principal("CN=Buypass Unknown, O=Buypass AS-983163327, C=NO");

	@Test
	public void readCrlFromBuypass_returnsCrl() throws Exception {
		CertificateRevocationListService certificateRevocationListService = makeCertificateRevocationListService();
		certificateRevocationListService.init();

		X509CRL x509Crl = certificateRevocationListService.readCrlFromBuypass(BUYPASS_CLASS_3_CA_3);

		assertNotNull(x509Crl);
	}

	@Test
	public void readCrlFromBuypass_noUrlFoundForPrincipal_returnsCrl() throws Exception {
		CertificateRevocationListService certificateRevocationListService = makeCertificateRevocationListService();
		certificateRevocationListService.init();

		X509CRL x509Crl = certificateRevocationListService.readCrlFromBuypass(UNKNOWN_PRINCIPAL);

		assertThat(x509Crl).isNull();
	}

	private X509Certificate getX509Certificate(String resourcePath) throws IOException, CertificateException {
		try (InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(inStream);
		}
	}

	private CertificateRevocationListService makeCertificateRevocationListService() throws Exception {

		final URLConnection fakeUrlConnection = mock(URLConnection.class);
		when(fakeUrlConnection.getInputStream()).thenReturn(getX509CrlInputStream("certificate-revocation-list/crl-base64"));

		CertificateService fakeCertificateService = mock(CertificateService.class);
		Collection<X509Certificate> caCerts = new ArrayList<>();
		caCerts.add(getX509Certificate("certificate-revocation-list/BPClass3CA3.pem"));
		when(fakeCertificateService.getScanningCertificateIssuersFromBundle()).thenReturn(caCerts);

		return new CertificateRevocationListService(fakeCertificateService) {
			URLConnection getUrlConnection(URL url) throws IOException {
				return fakeUrlConnection;
			}
		};
	}

	private ByteArrayInputStream getX509CrlInputStream(String resourcePath) throws Exception {
		try (BufferedReader reader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(resourcePath).getFile()))) {
			return new ByteArrayInputStream(Base64.decode(reader));
		}
	}
}
