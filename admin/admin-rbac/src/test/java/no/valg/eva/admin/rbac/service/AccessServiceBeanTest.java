package no.valg.eva.admin.rbac.service;

import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.evote.service.CryptoServiceBean;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Collections.emptySet;
import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AccessServiceBeanTest extends MockUtilsTestCase {

    private static final Long AN_ELECTION_EVENT_PK = 1L;
    private static final String AN_ELECTION_EVENT_ID = "1";
    private static final Long ROOT_ELECTION_EVENT_PK = 2L;
    private static final String A_ROLE = "role";
	private final ElectionEvent anElectionEvent;
	{
		anElectionEvent = new ElectionEvent();
		anElectionEvent.setId(AN_ELECTION_EVENT_ID);
	}
	private final ElectionEvent rootElectionEvent;
	{
		rootElectionEvent = new ElectionEvent();
		rootElectionEvent.setId(ROOT_ELECTION_EVENT_ID);
	}
	private AccessServiceBean accessService;
	@Mock
	private UserData userData;

	@BeforeSuite
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		accessService = initializeMocks(AccessServiceBean.class);
	}

	@Test
	public void testFindAccessCacheForReturnsAccessCacheWithOutSignature() {
		when(getInjectMock(AccessRepository.class).getIncludedAccessesNoDisabledRoles(any(Role.class))).thenReturn(emptySet());
		when(userData.getElectionEventPk()).thenReturn(ROOT_ELECTION_EVENT_PK);
		when(getInjectMock(ElectionEventRepository.class).findByPk(ROOT_ELECTION_EVENT_PK)).thenReturn(rootElectionEvent);

		AccessCache accessCache = accessService.findAccessCacheFor(userData);
		assertThat(accessCache.getSignature()).isNull();
	}

	@Test
	public void testFindAccessCacheForReturnsAccessCacheWithSignature() {
		when(getInjectMock(AccessRepository.class).getIncludedAccessesNoDisabledRoles(any(Role.class))).thenReturn(of(A_ROLE));
		when(userData.getElectionEventPk()).thenReturn(AN_ELECTION_EVENT_PK);
		when(getInjectMock(ElectionEventRepository.class).findByPk(AN_ELECTION_EVENT_PK)).thenReturn(anElectionEvent);
		byte[] expectedSignature = "signature".getBytes();
		when(getInjectMock(CryptoServiceBean.class).signDataWithCurrentElectionEventCertificate(userData, A_ROLE.getBytes())).thenReturn(expectedSignature);
		AccessCache accessCache = accessService.findAccessCacheFor(userData);
		assertThat(accessCache.getSignature()).isEqualTo(expectedSignature);
	}
}
