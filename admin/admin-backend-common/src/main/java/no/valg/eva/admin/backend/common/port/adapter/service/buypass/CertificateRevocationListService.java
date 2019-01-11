package no.valg.eva.admin.backend.common.port.adapter.service.buypass;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;

import no.evote.exception.EvoteException;
import no.evote.service.CertificateService;
import no.evote.util.EvoteProperties;

import org.apache.log4j.Logger;

/**
 * Reads and verifies Certificate Revocation Lists (CRL) from Buypass for Class 3 CA 1 and CA 3.
 */
public class CertificateRevocationListService {
	private static final Logger LOGGER = Logger.getLogger(CertificateRevocationListService.class);

	public static final X500Principal BUYPASS_CLASS_3_CA_1 = new X500Principal("CN=Buypass Class 3 CA 1, O=Buypass AS-983163327, C=NO");
	public static final X500Principal BUYPASS_CLASS_3_CA_3 = new X500Principal("CN=Buypass Class 3 CA 3, O=Buypass AS-983163327, C=NO");
	public static final String BUYPASS_CLASS_3_CA_1_URL_DEFAULT = "http://int-distro.eva.lokal/buypass/BPClass3CA1.crl";
	public static final String BUYPASS_CLASS_3_CA_3_URL_DEFAULT = "http://int-distro.eva.lokal/buypass/BPClass3CA3.crl";
	private static final Map<X500Principal, String> PRINCIPAL_CLR_URL_MAP = new HashMap<>();

	{
		PRINCIPAL_CLR_URL_MAP.put(BUYPASS_CLASS_3_CA_1, EvoteProperties.getProperty(EvoteProperties.BYPASS_CRL_CA1_URL, BUYPASS_CLASS_3_CA_1_URL_DEFAULT));
		PRINCIPAL_CLR_URL_MAP.put(BUYPASS_CLASS_3_CA_3, EvoteProperties.getProperty(EvoteProperties.BYPASS_CRL_CA3_URL, BUYPASS_CLASS_3_CA_3_URL_DEFAULT));
	}

	/* Buypass CA Issuer Certificates */
	private final Map<X500Principal, X509Certificate> principalCaIssuerCertificateMap = new HashMap<>();
	private final CertificateService certificateService;

	@Inject
	public CertificateRevocationListService(CertificateService certificateService) {
		this.certificateService = certificateService;
	}

	@PostConstruct
	public void init() {
		// les opp Buypass CA issuer sertifikater
		Collection<X509Certificate> buypassCaCertificates = certificateService.getScanningCertificateIssuersFromBundle();
		for (X509Certificate x509Certificate : buypassCaCertificates) {
			principalCaIssuerCertificateMap.put(x509Certificate.getSubjectX500Principal(), x509Certificate);
		}
	}

	public X509CRL readCrlFromBuypass(X500Principal principal) {
		String url = PRINCIPAL_CLR_URL_MAP.get(principal);
		if (url == null) {
			LOGGER.warn("URL for certificate revocation list not found for principal: " + principal);
			return null;
		}

		X509Certificate caCertificate = principalCaIssuerCertificateMap.get(principal);
		return readCrlFromBuypass(url, caCertificate);
	}

	private X509CRL readCrlFromBuypass(String url, X509Certificate caCertificate) {
		try {
			return readCrlFromBuypass(new URL(url), caCertificate.getPublicKey());
		} catch (Exception e) {
			throw new EvoteException("Error reading CRL from Buypass at url: " + url, e);
		}
	}

	private X509CRL readCrlFromBuypass(URL url, PublicKey caPublicKey) throws Exception {
		try (DataInputStream inStream = new DataInputStream(getUrlConnection(url).getInputStream())) {

			CertificateFactory cf = CertificateFactory.getInstance("X509");
			X509CRL x509CRL = (X509CRL) cf.generateCRL(inStream);
			x509CRL.verify(caPublicKey);

			return x509CRL;
		}
	}

	URLConnection getUrlConnection(URL url) throws IOException {
		URLConnection urlConnection = url.openConnection();
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);
		return urlConnection;
	}
}
