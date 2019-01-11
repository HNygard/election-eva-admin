package no.valg.eva.admin.backend.common.application.buypass;

import no.valg.eva.admin.backend.common.domain.CertificateRevocationList;
import no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListService;
import no.valg.eva.admin.backend.common.repository.CertificateRevocationListRepository;
import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509CRL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RevocationListHolderTest {

	@Test
	public void getBuypassCrl_returnsCrl() {
		CertificateRevocationListRepository fakeCrlRepository = mock(CertificateRevocationListRepository.class);
		CertificateRevocationList fakeCrl = mock(CertificateRevocationList.class);
		X500Principal principal = CertificateRevocationListService.BUYPASS_CLASS_3_CA_3;
		when(fakeCrlRepository.certificateRevocationListByIssuer(principal)).thenReturn(fakeCrl);
		RevocationListHolder revocationListHolder = new RevocationListHolder(fakeCrlRepository);

		assertThat(revocationListHolder.getBuypassCrl(principal)).isEqualTo(fakeCrl);
	}

	@Test
	public void update_savesToRepository() {
		CertificateRevocationListRepository mockCrlRepository = mock(CertificateRevocationListRepository.class);
		RevocationListHolder revocationListHolder = new RevocationListHolder(mockCrlRepository);
		X509CRL fakeCrl = mock(X509CRL.class);
		when(fakeCrl.getIssuerX500Principal()).thenReturn(CertificateRevocationListService.BUYPASS_CLASS_3_CA_3);

		revocationListHolder.updateBuypassCrl(fakeCrl);

		verify(mockCrlRepository).add(any(CertificateRevocationList.class));
	}
}
