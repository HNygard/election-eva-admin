package no.valg.eva.admin.crypto;

import java.io.InputStream;

class Testfil {
	static final String TEST_P12_PASSORD = "testpassord";

	static InputStream test1_p12AsInputStream() {
		return Testfil.class.getResourceAsStream("/certs/test1.p12");
	}

	static InputStream test2_p12AsInputStream() {
		return Testfil.class.getResourceAsStream("/certs/test2.p12");
	}
}
