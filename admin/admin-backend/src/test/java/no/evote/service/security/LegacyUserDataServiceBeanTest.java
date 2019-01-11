package no.evote.service.security;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Se;
import static no.valg.eva.admin.common.rbac.Accesses.Parti;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.AccessTokenAndSignature;
import no.evote.service.CryptoServiceBean;
import no.evote.service.LegacyUserDataService;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.RBACTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.valg.eva.admin.util.XMLUtil;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.backend.rbac.RBACAuthenticator;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.apache.commons.lang3.SerializationUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = TestGroups.REPOSITORY)
public class LegacyUserDataServiceBeanTest extends AbstractJpaTestBase {

	public static final String ENCRYPTION_PASSWORD = "system";

	private static final Logger LOG = LoggerFactory.getLogger(LegacyUserDataServiceBeanTest.class);
	private static final String VALG2007 = "200701";

	private final XPathFactory xPathFactory = XPathFactory.instance();

	private LegacyUserDataServiceBean userDataService;
	private RBACAuthenticator rbacAuthenticator;
	private ElectionEventRepository electionEventRepository;
	private CryptoServiceBean cryptoService;
	private RBACTestFixture rbacTestFixture;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		BackendContainer backend = new BackendContainer(getEntityManager());
		backend.initServices();

		userDataService = backend.getUserDataService();
		electionEventRepository = backend.getElectionEventRepository();
		rbacAuthenticator = backend.getRbacAuthenticator();
		cryptoService = backend.getCryptoService();

		rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
		rbacTestFixture.init();

		backend.getSystemPasswordStore().setPassword(ENCRYPTION_PASSWORD);
	}

	@Test
	public void illegalEventExport() {
		UserData userData = rbacTestFixture.getUserData();
		userData.setUid(rbacTestFixture.getOperator2().getId());

		assertNull(userDataService.exportAccessToken(userData, rbacTestFixture.getOperator()));
	}

	@Test
	public void testHasAccessSingleSecObjTopHir() {
		UserData userData = new UserData();
		userData.setOperatorRole(rbacTestFixture.getOperatorRoleOperatorRoot());
		userData.setUid(rbacTestFixture.getOperator().getId());
		userData.getOperatorRole().setRole(rbacTestFixture.getRoleRoot());

		assertTrue(rbacAuthenticator.hasAccess(userData, Opptelling_Valgting_Se));
	}

	@Test
	public void testHasAccessSingleSecObjBottomHir() {
		UserData userData = new UserData();
		userData.setOperatorRole(rbacTestFixture.getOperatorRoleOperatorRoot());
		userData.setUid(rbacTestFixture.getOperator().getId());
		userData.getOperatorRole().setRole(rbacTestFixture.getRoleVotingCountElectionDayApprove());

		assertTrue(rbacAuthenticator.hasAccess(userData, Opptelling_Valgting_Rediger));
		assertFalse(rbacAuthenticator.hasAccess(userData, Opptelling_Valgting_Se));
	}

	@Test
	public void testHasAccessSingleSecObjMidHir() {
		UserData userData = new UserData();
		userData.setOperatorRole(rbacTestFixture.getOperatorRoleOperatorRoot());
		userData.setUid(rbacTestFixture.getOperator().getId());
		userData.getOperatorRole().setRole(rbacTestFixture.getRolePartyTVoteCountVotingCountElectionDayAll());

		assertTrue(rbacAuthenticator.hasAccess(userData, Opptelling_Valgting_Rediger));
		assertTrue(rbacAuthenticator.hasAccess(userData, Parti));
		assertFalse(rbacAuthenticator.hasAccess(userData, Opptelling));
	}

	@Test
	public void testGetUserDataScheduledImportUser() throws UnknownHostException {
		UserData userData = userDataService.getUserData(EvoteConstants.SCHEDULED_IMPORT_OPERATOR_ID, EvoteConstants.SCHEDULED_IMPORT_ROLE, VALG2007, VALG2007,
				InetAddress.getLocalHost());
		ElectionEvent electionEvent = electionEventRepository.findByPk(rbacTestFixture.getUserData().getElectionEventPk());

		assertEquals(electionEvent.getId(), VALG2007);
		assertEquals(userData.getOperatorRole().getOperator().getId(), EvoteConstants.SCHEDULED_IMPORT_OPERATOR_ID);
		assertEquals(userData.getOperatorRole().getRole().getId(), EvoteConstants.SCHEDULED_IMPORT_ROLE);
	}

	@Test
	public void exportSignedAccessToken_responseHasValidSignature() throws Exception {
		UserData userData = rbacTestFixture.getUserData();
		userData.setUid(rbacTestFixture.getOperator().getId());

		AccessTokenAndSignature accessTokenAndSignature = userDataService.exportSignedAccessToken(userData, rbacTestFixture.getOperator());

		assertTrue(cryptoService.verifyAdminElectionEventSignature(
				rbacTestFixture.getUserData(),
				XMLUtil.documentToBytes(accessTokenAndSignature.getAccessToken()), accessTokenAndSignature.getSignature(),
				rbacTestFixture.getUserData().getElectionEventPk()));
	}

	@Test
	public void exportSignedAccessToken_resultIsSerializable() throws Exception {
		UserData userData = rbacTestFixture.getUserData();
		userData.setUid(rbacTestFixture.getOperator().getId());

		AccessTokenAndSignature accessTokenAndSignature = userDataService.exportSignedAccessToken(userData, rbacTestFixture.getOperator());
		assertNotNull(SerializationUtils.deserialize(SerializationUtils.serialize(accessTokenAndSignature)));
	}

	@Test
	public void exportAccessToken_whenBoroughContestAndRoleInformationOnMunicipality_shallRepeatRoleInformationOnBoroughLevel() throws Exception {
		UserData userData = rbacTestFixture.getUserData();
		userData.setSecurityLevel(4); // required by the test data (does not make much sense domain-wise)
		Operator operator2 = rbacTestFixture.getOperator2();
		userData.setUid(operator2.getId());

		Document accessTokenDocument = userDataService.exportAccessToken(userData, operator2);

		XPathExpression<Element> accessOnOsloXPath = xPathFactory.compile(
				"//role[areaContextId = '" + VALG2007 + ".47.03.0301']//access['" + Opptelling_Valgting_Rediger.paths()[0] + "']",
				Filters.fclass(Element.class));
		XPathExpression<Element> accessOnNordreAkerXPath = xPathFactory.compile(
				"//role[areaContextId = '" + VALG2007 + ".47.03.0301.030108']//access['" + Opptelling_Valgting_Rediger.paths()[0]
						+ "']",
				Filters.fclass(Element.class));
		assertThat(accessOnOsloXPath.evaluate(accessTokenDocument)).hasSize(1);
		assertThat(accessOnNordreAkerXPath.evaluate(accessTokenDocument)).hasSize(1);

		if (LOG.isDebugEnabled()) {
			String accessTokenXml = XMLUtil.documentToString(accessTokenDocument);
			LOG.debug("Access token: {}", accessTokenXml);
		}
	}

	@Test(expectedExceptions = EvoteException.class)
	public void isFileUploadDownloadTokenValid_withMissingPem_shouldThrowEvoteException() throws Exception {
		UserData userData = getUserDataWithUID();

		byte[] accessToken = "<xml/>".getBytes();
		byte[] signature = null;
		byte[] tokenZip = createTokenZip(accessToken, signature);

		userDataService.isFileUploadDownloadTokenValid(userData, tokenZip, userData.getUid());
	}

	@Test(expectedExceptions = EvoteException.class)
	public void isFileUploadDownloadTokenValid_withInvalidPem_shouldThrowEvoteException() throws Exception {
		UserData userData = getUserDataWithUID();

		byte[] accessToken = "<xml/>".getBytes();
		byte[] signature = "gurba".getBytes();
		byte[] tokenZip = createTokenZip(accessToken, signature);

		userDataService.isFileUploadDownloadTokenValid(userData, tokenZip, userData.getUid());
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Token has expired")
	public void isFileUploadDownloadTokenValid_withExpiredToken_shouldThrowEvoteException() throws Exception {
		UserData userData = getUserDataWithUID();

		byte[] accessToken = createTokenXMLWith(userData, LegacyUserDataService.EXPIRATION_TAG, getExpiredTime());
		byte[] signature = cryptoService.signDataWithCurrentElectionEventCertificate(userData, accessToken);
		byte[] tokenZip = createTokenZip(accessToken, signature);

		userDataService.isFileUploadDownloadTokenValid(userData, tokenZip, userData.getUid());
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Token xml parsing error.*")
	public void isFileUploadDownloadTokenValid_withInvalidDateFormat_shouldThrowEvoteException() throws Exception {
		UserData userData = getUserDataWithUID();

		byte[] accessToken = createTokenXMLWith(userData, LegacyUserDataService.EXPIRATION_TAG, "invalid");
		byte[] signature = cryptoService.signDataWithCurrentElectionEventCertificate(userData, accessToken);
		byte[] tokenZip = createTokenZip(accessToken, signature);

		userDataService.isFileUploadDownloadTokenValid(userData, tokenZip, userData.getUid());
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Wrong user ID")
	public void isFileUploadDownloadTokenValid_withInvalidUID_shouldThrowEvoteException() throws Exception {
		UserData userData = getUserDataWithUID();

		byte[] accessToken = createTokenXMLWith(userData, LegacyUserDataService.UID_TAG, "invalid");
		byte[] signature = cryptoService.signDataWithCurrentElectionEventCertificate(userData, accessToken);
		byte[] tokenZip = createTokenZip(accessToken, signature);

		userDataService.isFileUploadDownloadTokenValid(userData, tokenZip, userData.getUid());
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Token xml parsing error.*")
	public void isFileUploadDownloadTokenValid_withInvalidXML_shouldThrowEvoteException() throws Exception {
		UserData userData = getUserDataWithUID();

		byte[] accessToken = "<invalidXML<>>".getBytes();
		byte[] signature = cryptoService.signDataWithCurrentElectionEventCertificate(userData, accessToken);
		byte[] tokenZip = createTokenZip(accessToken, signature);

		userDataService.isFileUploadDownloadTokenValid(userData, tokenZip, userData.getUid());
	}

	@Test
	public void isFileUploadDownloadTokenValid_withValidZip_shouldReturnTrue() throws Exception {
		UserData userData = getUserDataWithUID();

		byte[] accessToken = XMLUtil.documentToBytes(userDataService.exportAccessToken(userData, rbacTestFixture.getOperator()));
		byte[] signature = cryptoService.signDataWithCurrentElectionEventCertificate(userData, accessToken);
		byte[] tokenZip = createTokenZip(accessToken, signature);

		assertThat(userDataService.isFileUploadDownloadTokenValid(userData, tokenZip, userData.getUid())).isTrue();
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Token size is too big")
	public void isFileUploadDownloadTokenValid_withZipBomb_shouldReturnFalse() throws Exception {
		UserData userData = getUserDataWithUID();

		byte[] accessToken = new byte[10000000];
		Arrays.fill(accessToken, (byte) 1);
		byte[] signature = new byte[1000000];
		Arrays.fill(signature, (byte) 2);
		byte[] tokenZip = createTokenZip(accessToken, signature);

		userDataService.isFileUploadDownloadTokenValid(userData, tokenZip, userData.getUid());
	}

	private String getExpiredTime() {
		DateTime expiredTime = DateTime.now().minusDays(1);
		return DateTimeFormat.forPattern("yyyy.MM.dd-HH:mm:ss").withLocale(new Locale("nb_NO")).print(expiredTime);
	}

	private byte[] createTokenXMLWith(UserData userData, String tagName, String value) {
		Document accessTokenDoc = userDataService.exportAccessToken(userData, rbacTestFixture.getOperator());
		Element root = accessTokenDoc.getRootElement();
		for (Element child : root.getChildren()) {
			if (tagName.equals(child.getName())) {
				child.setText(value);
				break;
			}
		}
		return XMLUtil.documentToBytes(accessTokenDoc);
	}

	private byte[] createTokenZip(byte[] accessToken, byte[] signature) throws Exception {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(bo))) {
			// token.xml
			ZipEntry tokenXmlEntry = new ZipEntry("token.xml");
			zipOut.putNextEntry(tokenXmlEntry);
			zipOut.write(accessToken);
			zipOut.closeEntry();
			// token.pem
			if (signature != null) {
				ZipEntry signEntry = new ZipEntry("token.pem");
				zipOut.putNextEntry(signEntry);
				zipOut.write(signature);
				zipOut.closeEntry();
			}
		} finally {
			bo.close();
		}
		return bo.toByteArray();
	}

	private UserData getUserDataWithUID() {
		UserData userData = rbacTestFixture.getUserData();
		userData.setUid(rbacTestFixture.getOperator().getId());
		return userData;
	}
}

