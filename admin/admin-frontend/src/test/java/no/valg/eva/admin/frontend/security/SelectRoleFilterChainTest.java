package no.valg.eva.admin.frontend.security;

import no.evote.security.UserDataProducer;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import org.testng.annotations.Test;

import static no.evote.service.security.SelectRoleFilter.OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class SelectRoleFilterChainTest extends SecurityFilterChainTestCase {

	@Override
	protected void filtersInitialized() throws Exception {
		// All tests default run with tmpLogin = false, access to all URLs and not attributes on session.
		// NOTE: From super class, all tests are also executed with no UserData.
		stub_isTmpLoginEnabled(false);
		stub_hasAccess(true);
		when(getServletContainer().getHttpSessionMock().getAttribute(anyString())).thenReturn(null);
	}

	@Test
	public void selectRole_withNoOperatorRole_chainsThrough() throws Exception {
		mock_operatorService_findOperatorsById();
		when(getUserDataMock().getOperatorRole()).thenReturn(null);
		setUserDataInstance(getUserDataMock());

		request("/secure/selectRole.xhtml");

		assertThat(chainThrough()).isEqualTo(true);
		assertRequestURI("/secure/selectRole.xhtml");
	}

	@Test
	public void selectRole_withOperatorRole_preparesOperatorRoleSwitch() throws Exception {
		mock_operatorService_findOperatorsById();
		when(getUserDataMock().getOperatorRole()).thenReturn(createMock(OperatorRole.class));
		setUserDataInstance(getUserDataMock());

		request("/secure/selectRole.xhtml");

		assertThat(chainThrough()).isEqualTo(true);
		assertRequestURI("/secure/selectRole.xhtml");
		verify(getServletContainer().getHttpSessionMock()).invalidate();
		verify(getUserDataMock()).invalidateRoleSelection();
		verify(getServletContainer().getHttpSessionMock()).setAttribute(OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY, true);
		verify(getServletContainer().getHttpSessionMock()).setAttribute(OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY, getUserDataMock());
		verify(getServletContainer().getHttpSessionMock()).removeAttribute(OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY);
		verify(getMockedInstance(getOidcFilter(), "userDataProducerInstance", UserDataProducer.class)).setUserData(getUserDataMock());
	}
}

