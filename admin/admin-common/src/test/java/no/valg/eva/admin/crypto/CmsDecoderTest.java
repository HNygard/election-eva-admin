package no.valg.eva.admin.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.Security;

import static no.valg.eva.admin.crypto.Testfil.TEST_P12_PASSORD;
import static org.assertj.core.api.Assertions.assertThat;

public class CmsDecoderTest {

	@BeforeClass
	public void addBouncyCastleProvider() {
		Security.addProvider(new BouncyCastleProvider());
	}

	@Test
	public void verifiserSignertAv_gir_riktig_svar() throws Exception {
		SertifikatOgNøkkel sertifikatOgNøkkel = lesTest1Pkcs12();
		CmsEncoder cmsEncoder = new CmsEncoder();
		CmsDecoder cmsDecoder = new CmsDecoder();

		byte[] data = "Data som skal signeres".getBytes();
		byte[] signatur = cmsEncoder.signer(sertifikatOgNøkkel.sertifikat(), sertifikatOgNøkkel.sertifikatkjede(), sertifikatOgNøkkel.nøkkel(), data);

		assertThat(cmsDecoder.verifySignedBy(data, signatur, sertifikatOgNøkkel.sertifikat())).isTrue();
	}

	@Test
	public void verifiser_returnerer_false_hvis_feil_signatur_og_riktig_signerer() throws Exception {
		SertifikatOgNøkkel sertifikatOgNøkkel = lesTest1Pkcs12();
		CmsEncoder cmsEncoder = new CmsEncoder();
		CmsDecoder cmsDecoder = new CmsDecoder();

		byte[] data = "Data som skal signeres".getBytes();
		byte[] falskeData = "Data som egentlig ble signert".getBytes();
		byte[] falskSignatur = cmsEncoder.signer(sertifikatOgNøkkel.sertifikat(), sertifikatOgNøkkel.sertifikatkjede(), sertifikatOgNøkkel.nøkkel(),
				falskeData);

		assertThat(cmsDecoder.verifySignedBy(data, falskSignatur, sertifikatOgNøkkel.sertifikat())).isFalse();
	}

	@Test
	public void verifiser_returnerer_false_hvis_gyldig_signatur_men_feil_signerer() throws Exception {
		SertifikatOgNøkkel p12Bruker1 = lesTest1Pkcs12();
		SertifikatOgNøkkel p12Bruker2 = lesTest2Pkcs12();
		CmsEncoder cmsEncoder = new CmsEncoder();
		CmsDecoder cmsDecoder = new CmsDecoder();

		byte[] data = "Data som skal signeres".getBytes();
		byte[] signaturBruker1 = cmsEncoder.signer(p12Bruker1.sertifikat(), p12Bruker1.sertifikatkjede(), p12Bruker1.nøkkel(), data);

		assertThat(cmsDecoder.verifySignedBy(data, signaturBruker1, p12Bruker2.sertifikat())).isFalse();
	}

	private SertifikatOgNøkkel lesTest1Pkcs12() throws CryptoException, IOException {
		try (InputStream pkcs12Stream = Testfil.test1_p12AsInputStream()) {
			return new Pkcs12Decoder().lesPkcs12(pkcs12Stream, TEST_P12_PASSORD);
		}
	}

	private SertifikatOgNøkkel lesTest2Pkcs12() throws CryptoException, IOException {
		try (InputStream pkcs12Stream = Testfil.test2_p12AsInputStream()) {
			return new Pkcs12Decoder().lesPkcs12(pkcs12Stream, TEST_P12_PASSORD);
		}
	}
}
