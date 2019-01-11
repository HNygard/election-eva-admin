package no.valg.eva.admin.backend.common.application.buypass;

import no.valg.eva.admin.backend.common.domain.CertificateRevocationList;
import no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListService;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RevocationListUpdaterTest {

	@Test
	public void updateIfNeeded_crlIsNull_readCrlFromBuypassIsInvoked() {
		RevocationListHolder fakeRevocationListHolder = mock(RevocationListHolder.class);
		CertificateRevocationListService fakeCertficateRevocationListService = mock(CertificateRevocationListService.class);
		RevocationListUpdater revocationListUpdater = new RevocationListUpdater(fakeRevocationListHolder, fakeCertficateRevocationListService);

		revocationListUpdater.updateIfNeeded();

		verify(fakeCertficateRevocationListService, times(2)).readCrlFromBuypass(any(X500Principal.class));
	}

	@Test
	public void updateIfNeeded_crlIsNotNullAndDoesNotNeedUpdate_readCrlFromBuypassIsNeverInvoked() {
		RevocationListHolder mockRevocationListHolder = mock(RevocationListHolder.class);
		CertificateRevocationList fakeCrl = mock(CertificateRevocationList.class);
		when(fakeCrl.getUpdated()).thenReturn(DateTime.now());
		when(mockRevocationListHolder.getBuypassCrl(any(X500Principal.class))).thenReturn(fakeCrl);
		CertificateRevocationListService fakeCertficateRevocationListService = mock(CertificateRevocationListService.class);
		RevocationListUpdater revocationListUpdater = new RevocationListUpdater(mockRevocationListHolder, fakeCertficateRevocationListService);

		revocationListUpdater.updateIfNeeded();

		verify(fakeCertficateRevocationListService, never()).readCrlFromBuypass(any(X500Principal.class));
	}
}
