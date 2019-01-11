package no.valg.eva.admin.backend.common.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;

import no.valg.eva.admin.backend.common.domain.CertificateRevocationList;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class CertificateRevocationListRepositoryTest extends AbstractJpaTestBase {

	private CertificateRevocationListRepository certificateRevocationListRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		certificateRevocationListRepository = new CertificateRevocationListRepository(getEntityManager());
	}

	/**
	 * Assumes that there is an existing BP CLASS 3 CA 3 in the testdatabase
	 */
	@Test
	public void add_whenCrlExistsInTestData_updatesCrl() throws Exception {
		X509CRL x509Crl = getX509Crl("certificate-revocation-list/BPClass3CA3.crl");
		CertificateRevocationList crl = new CertificateRevocationList(x509Crl);

		certificateRevocationListRepository.add(crl);
		CertificateRevocationList storedCrl = certificateRevocationListRepository.certificateRevocationListByIssuer(x509Crl.getIssuerX500Principal());
		assertThat(crl).isEqualTo(storedCrl);
	}

	/**
	 * Assumes that there is no BP CLASS 3 CA 1 in the testdatabase
	 */
	@Test
	public void add_whenNoCrlInDatabase_insertsNewCrl() throws Exception {
		X509CRL x509Crl = getX509Crl("certificate-revocation-list/BPClass3CA1.crl");
		CertificateRevocationList crl = new CertificateRevocationList(x509Crl);

		certificateRevocationListRepository.add(crl);
		CertificateRevocationList storedCrl = certificateRevocationListRepository.certificateRevocationListByIssuer(x509Crl.getIssuerX500Principal());
		assertThat(crl.getEncodedCrl()).isEqualTo(storedCrl.getEncodedCrl());
	}

	private X509CRL getX509Crl(String resourcePath) throws Exception {
		try (InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509CRL) cf.generateCRL(inStream);
		}
	}
}
