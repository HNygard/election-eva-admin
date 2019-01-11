package no.evote.service.backendmock;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;

import java.io.File;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import no.evote.security.UserData;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.rbac.repository.AccessRepository;

/**
 * Provides a base fixture for tests.
 */
public class BaseTestFixture {
	public static final String LOCALE_NB_NO = "nb-NO";
	public static final String ID200701 = "200701";

	protected LegacyUserDataServiceBean userDataService;
	protected AccessRepository accessRepository;

	private UserData userData;
	private UserData sysAdminUserData;

	/**
	 * Constructs the test fixture.
	 * <p/>
	 * NOTE: Remember to invoke {@link #init()} before using the instance.
	 */
	public BaseTestFixture(LegacyUserDataServiceBean userDataService, AccessRepository accessRepository) {
		this.userDataService = userDataService;
		this.accessRepository = accessRepository;
	}

	public static File fileFromResources(final String fileName) throws URISyntaxException {
		return new File(BaseTestFixture.class.getClassLoader().getResource(fileName).toURI());
	}

	protected static InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			throw new RuntimeException("Could not resolve address for localhost", e);
		}
	}

	/**
	 * Initializes the instance. Must be invoked after constructing the instance, and before using other methods on it.
	 */
	public void init() {
		userData = userDataService.getUserData("03011700143", "valghendelse_admin", ID200701, ID200701, getLocalHost());
		sysAdminUserData = userDataService.getUserData("03011700143", "system_admin", "000000", ROOT_ELECTION_EVENT_ID, getLocalHost());
	}

	public UserData getUserData(final String uid) {
		UserData userData = userDataService.getUserData("03011700143", "valghendelse_admin", ID200701, ID200701, getLocalHost());
		userData.setUid(uid);
		return userData;
	}

	public UserData getUserData() {
		return userData;
	}

	public UserData getSysAdminUserData() {
		return sysAdminUserData;
	}
}
