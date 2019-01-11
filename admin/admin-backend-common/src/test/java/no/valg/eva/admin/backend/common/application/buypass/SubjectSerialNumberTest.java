package no.valg.eva.admin.backend.common.application.buypass;

import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.jcajce.JcaCertStoreBuilder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.AssertJUnit.fail;

public class SubjectSerialNumberTest {

    private static final String SERIAL_NUMBER_KEY = "OID.2.5.4.5=";
    private static final String SERIAL_NUMBER_WITHOUT_DASHES = "10001000123456789";
    private static final String SERIAL_NUMBER_WITH_DASHES = "1000-1000-123456789";
    private static final String COUNTRY_AND_NAME = "CN=Ola Nordmann, C=NO";
    private static final String COMMA_SPACE = ", ";

    @Test
    public void fromPrincipal_withCorrectDn_returnsSerialNumber() {
        assertThat(SubjectSerialNumber.fromPrincipal(new X500Principal(SERIAL_NUMBER_KEY + SERIAL_NUMBER_WITH_DASHES + COMMA_SPACE + COUNTRY_AND_NAME))
                .serialNumber()).isEqualTo(new BigInteger(SERIAL_NUMBER_WITHOUT_DASHES));
        assertThat(SubjectSerialNumber.fromPrincipal(new X500Principal(SERIAL_NUMBER_KEY + SERIAL_NUMBER_WITHOUT_DASHES + COMMA_SPACE + COUNTRY_AND_NAME))
                .serialNumber()).isEqualTo(new BigInteger(SERIAL_NUMBER_WITHOUT_DASHES));
        assertThat(SubjectSerialNumber.fromPrincipal(new X500Principal("C=NO, CN=Ola Nordmann, OID.2.5.4.5=" + SERIAL_NUMBER_WITH_DASHES))
                .serialNumber()).isEqualTo(new BigInteger(SERIAL_NUMBER_WITHOUT_DASHES));
    }

    @Test
    public void fromPrincipal_withCorrectDn_matchesCorrectly() {
        assertThat(SubjectSerialNumber.fromPrincipal(new X500Principal(SERIAL_NUMBER_KEY + SERIAL_NUMBER_WITH_DASHES + COMMA_SPACE + COUNTRY_AND_NAME))
                .matches(SERIAL_NUMBER_WITH_DASHES)).isTrue();
        assertThat(SubjectSerialNumber.fromPrincipal(new X500Principal(SERIAL_NUMBER_KEY + SERIAL_NUMBER_WITH_DASHES + COMMA_SPACE + COUNTRY_AND_NAME))
                .matches(SERIAL_NUMBER_WITHOUT_DASHES)).isTrue();
    }

    @Test
    public void matches_withNullSerialNumber_doesNotMatch() {
        assertThat(SubjectSerialNumber.fromPrincipal(new X500Principal(SERIAL_NUMBER_KEY + SERIAL_NUMBER_WITH_DASHES + COMMA_SPACE + COUNTRY_AND_NAME))
                .matches(null)).isFalse();
        assertThat(SubjectSerialNumber.fromPrincipal(new X500Principal(SERIAL_NUMBER_KEY + SERIAL_NUMBER_WITH_DASHES + COMMA_SPACE + COUNTRY_AND_NAME))
                .matches(" ")).isFalse();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "No serial number in DN.*")
    public void fromPrincipal_withoutSerialNumberInDn_throwsIllegalArgumentException() {
        SubjectSerialNumber.fromPrincipal(new X500Principal(COUNTRY_AND_NAME));
        fail("Expected IllegalArgumentException");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Not key=value format.*")
    public void fromPrincipal_withIllegalDn_throwsIllegalArgumentException() {
        SubjectSerialNumber.fromPrincipal(new X500Principal("OID.2.5.4.5=, " + COUNTRY_AND_NAME));
        fail("Expected IllegalArgumentException");
    }

    @Test
    public void subjectSerialNumber_whenReadFromFile_returnsSubjectSerialNumber() throws Exception {
        X509Certificate x509Certificate = getX509Certificate();
        assertThat(SubjectSerialNumber.fromCertificate(x509Certificate).matches("95784050128560414")).isTrue();
    }

    private X509Certificate getX509Certificate() throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("crypto-service-test/Counts.zip.pem")) {
            PEMParser pemParser = new PEMParser(new InputStreamReader(is));
            ContentInfo contentInfo = (ContentInfo) pemParser.readObject();
            CMSSignedData cmsSignedData = new CMSSignedData(contentInfo);
            SignerInformationStore signerInfos = cmsSignedData.getSignerInfos();
            Collection signers = signerInfos.getSigners();
            SignerInformation signer = (SignerInformation) signers.iterator().next();
            assertThat(signer).isNotNull();

            CertStore certStore = new JcaCertStoreBuilder().setProvider("BC").addCertificates(cmsSignedData.getCertificates()).build();
            return (X509Certificate) certStore.getCertificates(null).iterator().next();
        }
    }
}
