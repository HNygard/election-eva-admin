package no.valg.eva.admin.frontend;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;

import no.evote.constants.EvoteConstants;
import no.evote.service.AccessTokenAndSignature;
import no.valg.eva.admin.util.XMLUtil;

import org.apache.commons.codec.binary.Base64;
import org.testng.annotations.Test;

public class ScanningLoginSuccessfulControllerTest {

	private static final String DUMMY_ACCESS_TOKEN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<token><id>42</id></token>\n";
	private static final byte[] DUMMY_SIGNATURE = "dummy-signature".getBytes(Charset.forName(EvoteConstants.CHARACTER_SET));

	private static final Charset UTF_8 = Charset.forName(EvoteConstants.CHARACTER_SET);

	@Test
	public void getAccessToken_xmlIsEscaped() {
		ScanningLoginSuccessfulController controller = new ScanningLoginSuccessfulController();
		controller.setAccessTokenAndSignature(buildAccessTokenAndSignature());

		String accessTokenUnescaped = new String(Base64.decodeBase64(controller.getAccessTokenAsUtf8Xml()), UTF_8);
		byte[] signatureBase64Decoded = Base64.decodeBase64(controller.getSignatureBase64Encoded());

		assertThat(accessTokenUnescaped).isEqualTo(DUMMY_ACCESS_TOKEN);
		assertThat(signatureBase64Decoded).isEqualTo(DUMMY_SIGNATURE);
	}

	private AccessTokenAndSignature buildAccessTokenAndSignature() {
		return new AccessTokenAndSignature(XMLUtil.stringToDocument(DUMMY_ACCESS_TOKEN), DUMMY_SIGNATURE);
	}
}
