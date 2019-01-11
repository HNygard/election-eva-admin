package no.valg.eva.admin.frontend;

import java.nio.charset.Charset;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.EvoteConstants;
import no.evote.security.UserData;
import no.evote.service.AccessTokenAndSignature;
import no.evote.service.LegacyUserDataService;
import no.valg.eva.admin.util.XMLUtil;

import org.apache.commons.codec.binary.Base64;

@Named
@ConversationScoped
public class ScanningLoginSuccessfulController extends ConversationScopedController {

	private static final Charset UTF_8 = Charset.forName(EvoteConstants.CHARACTER_SET);

	@Inject
	private UserData userData;
	@Inject
	private transient LegacyUserDataService legacyUserDataService;

	private AccessTokenAndSignature accessTokenAndSignature;

	public void doInit() {
		setAccessTokenAndSignature(legacyUserDataService.exportSignedAccessToken(userData, userData.getOperator()));

		getConversation().end();
	}

	// Metoden er pakkesynlig for testbarhet
	void setAccessTokenAndSignature(AccessTokenAndSignature accessTokenAndSignature) {
		this.accessTokenAndSignature = accessTokenAndSignature;
	}

	public String getAccessTokenAsUtf8Xml() {
		return Base64.encodeBase64String(XMLUtil.documentToString(accessTokenAndSignature.getAccessToken()).getBytes(UTF_8));
	}

	public String getSignatureBase64Encoded() {
		return Base64.encodeBase64String(accessTokenAndSignature.getSignature());
	}

}
