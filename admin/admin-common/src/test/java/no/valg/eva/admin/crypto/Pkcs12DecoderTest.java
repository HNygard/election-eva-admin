package no.valg.eva.admin.crypto;

import static no.valg.eva.admin.crypto.Testfil.TEST_P12_PASSORD;
import static no.valg.eva.admin.crypto.Testfil.test1_p12AsInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import java.io.InputStream;
import java.security.Security;
import java.util.Base64;

import javax.crypto.KeyGenerator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Pkcs12DecoderTest {

	@BeforeClass
	public void addBouncyCastleProvider() {
		Security.addProvider(new BouncyCastleProvider());
	}

	@Test
	public void sjekk_at_BouncyCastle_er_installert() throws Exception {
		assertThat(KeyGenerator.getInstance("DES", "BC")).isNotNull();
	}

	@Test
	public void sjekk_at_testp12_finnes() throws Exception {
		try (InputStream pkcs12Stream = test1_p12AsInputStream()) {
			assertThat(pkcs12Stream.available()).isGreaterThan(0);
		}
	}

	@Test
	public void validerPassord_gir_true_ved_riktig_passord() throws Exception {
		Pkcs12Decoder pkcs12Decoder = new Pkcs12Decoder();
		try (InputStream pkcs12Stream = test1_p12AsInputStream()) {
			assertThat(pkcs12Decoder.validerPassord(pkcs12Stream, TEST_P12_PASSORD)).isTrue();
		}
	}

	@Test
	public void validerPassord_gir_false_ved_feil_passord() throws Exception {
		Pkcs12Decoder pkcs12Decoder = new Pkcs12Decoder();
		try (InputStream pkcs12Stream = test1_p12AsInputStream()) {
			assertThat(pkcs12Decoder.validerPassord(pkcs12Stream, "feilpassord")).isFalse();
		}
	}

	@Test
	public void lesPkcs12_leser_sertifikat_og_privat_nøkkel_fra_p12fil() throws Exception {
		Pkcs12Decoder pkcs12Decoder = new Pkcs12Decoder();
		try (InputStream pkcs12Stream = test1_p12AsInputStream()) {
			SertifikatOgNøkkel sertifikatOgNøkkel = pkcs12Decoder.lesPkcs12(pkcs12Stream, TEST_P12_PASSORD);

			assertThat(sertifikatOgNøkkel.sertifikat()).isNotNull();
			assertThat(sertifikatOgNøkkel.nøkkel()).isNotNull();
		}
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Illegal base64 character a")
	public void javaUtilBase64Decoder_støtter_ikke_linjeskift() throws Exception {
		// I noen tilfeller er det observert Base64-kodet tekst i databasen, som termineres med linjeskift.
		// Dette støtter ikke java.util.Base64, så vi må bruke Base64 fra commons-codec i stedet.
		// Hvis denne testen feiler, er det et tegn på at java.util.Base64 har begynt å støtte
		// linjeskift og commons-codec-varianten kan fases ut.

		String tekst = "En testtekst";
		String encodedMedLinjeskift = Base64.getEncoder().encodeToString(tekst.getBytes()) + "\n"; // encoding fungerer som det skal

		byte[] commonsDecoded = org.apache.commons.codec.binary.Base64.decodeBase64(encodedMedLinjeskift);
		assertThat(commonsDecoded.length).isGreaterThan(0);

		Base64.getDecoder().decode(encodedMedLinjeskift); // forventes å feile

		fail();
	}
}
