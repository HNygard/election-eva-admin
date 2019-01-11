package no.valg.eva.admin.backend.common.application.buypass;

import java.security.cert.X509CRL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;

import no.valg.eva.admin.backend.common.domain.CertificateRevocationList;
import no.valg.eva.admin.backend.common.repository.CertificateRevocationListRepository;

/**
 * Original intention was to cache CRLs - this is changed to just proxy the call to the CRL repository too keep it simple in a multiserver environment.
 */
@ApplicationScoped
public class RevocationListHolder {

	private CertificateRevocationListRepository certificateRevocationListRepository;

	@Inject
	public RevocationListHolder(CertificateRevocationListRepository certificateRevocationListRepository) {
		this.certificateRevocationListRepository = certificateRevocationListRepository;
	}

	@SuppressWarnings("unused")
	public RevocationListHolder() {
	}

	/**
	 * @param principal
	 *            subject for CA issuer certificate: Buypass Class 3 CA 1 or Buypass Class 3 CA 3
	 * @return revocation list instance
	 */
	public CertificateRevocationList getBuypassCrl(X500Principal principal) {
		return certificateRevocationListRepository.certificateRevocationListByIssuer(principal);
	}

	public void updateBuypassCrl(X509CRL x509CRL) {
		CertificateRevocationList buypassCrl = new CertificateRevocationList(x509CRL);
		certificateRevocationListRepository.add(buypassCrl);
	}
}
