package no.valg.eva.admin.backend.common.application.buypass;

import java.security.cert.X509CRL;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;

import no.valg.eva.admin.backend.common.domain.CertificateRevocationList;
import no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListService;

import org.joda.time.DateTime;

public class RevocationListUpdater {

	public static final int BUYPASS_UPDATE_FREQUENCY = 24;
	private static final X500Principal[] PRINCIPALS = new X500Principal[] { CertificateRevocationListService.BUYPASS_CLASS_3_CA_1,
			CertificateRevocationListService.BUYPASS_CLASS_3_CA_3 };

	private RevocationListHolder revocationListHolder;
	private CertificateRevocationListService certificateRevocationListService;

	@Inject
	public RevocationListUpdater(RevocationListHolder revocationListHolder, CertificateRevocationListService certificateRevocationListService) {
		this.revocationListHolder = revocationListHolder;
		this.certificateRevocationListService = certificateRevocationListService;
	}

	public void updateIfNeeded() {
		for (X500Principal principal : PRINCIPALS) {
			CertificateRevocationList crl = revocationListHolder.getBuypassCrl(principal);
			if (needsUpdate(crl)) {
				X509CRL x509CRL = certificateRevocationListService.readCrlFromBuypass(principal);
				if (x509CRL != null) {
					revocationListHolder.updateBuypassCrl(x509CRL);
				}
			}
		}
	}

	private boolean needsUpdate(CertificateRevocationList crl) {
		return (crl == null || updateIntervalReached(crl));
	}

	private boolean updateIntervalReached(CertificateRevocationList crl) {
		return new DateTime().minusHours(BUYPASS_UPDATE_FREQUENCY).isAfter(new DateTime(crl.getUpdated()));
	}
}
