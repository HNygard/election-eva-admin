package no.evote.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.model.SigningKey;
import no.valg.eva.admin.backend.common.repository.SigningKeyRepository;

import org.apache.log4j.Logger;

@ApplicationScoped
@Default
public class CertificateService {
	private static final Logger LOGGER = Logger.getLogger(CertificateService.class);
	@Inject
	private SigningKeyRepository signingKeyRepository;

	@SuppressWarnings("unchecked")
	public Collection<X509Certificate> getScanningCertificateIssuersFromBundle() {
		Collection<X509Certificate> certs = new HashSet<>();

		SigningKey scanningCountVerificationSigningKey = signingKeyRepository.getScanningCountVerificationSigningKey();
		if (scanningCountVerificationSigningKey != null) {

			byte[] certBundleBytes = scanningCountVerificationSigningKey.getBinaryData().getBinaryData();

			try (ByteArrayInputStream inStream = new ByteArrayInputStream(certBundleBytes)) {
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				synchronized (cf) {
					certs = (Collection<X509Certificate>) cf.generateCertificates(inStream);
				}
			} catch (CertificateException e) {
				LOGGER.error("Unable to create CertificateFactory", e);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return certs;
	}
}
