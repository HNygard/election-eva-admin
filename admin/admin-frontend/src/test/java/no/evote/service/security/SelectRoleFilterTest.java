package no.evote.service.security;

import no.evote.security.UserData;
import no.evote.service.rbac.OperatorService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;
import no.valg.eva.admin.rbac.domain.model.Operator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.inject.Instance;
import javax.servlet.http.HttpSession;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SelectRoleFilterTest extends BaseFrontendTest {

	private SelectRoleFilter filter;
	private Instance<UserData> userDataInstance;
	private ServletContainer container;

	@BeforeMethod
	public void setUp() throws Exception {
		filter = initializeMocks(SelectRoleFilter.class);
		userDataInstance = mockInstance("userDataInstance", UserData.class);
		container = getServletContainer();

		container.setServletContextPath("");
		container.setRequestURI("");
		setOperator();
	}

	@Test
	public void doFilter_withAlreadySelectedRole_checkChainFiltering() throws Exception {
		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		container.verifyChainDoFilter();
	}

	@Test
	public void doFilter_withNoSelectedRoleAndRequestingSelectRolePage_checkChainFiltering() throws Exception {
		setNoSelectedRole();
		container.setServletPath("/secure/selectRole.xhtml");
		container.setRequestURI("/secure/selectRole.xhtml");

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		container.verifyChainDoFilter();
	}

	@Test
	public void doFilter_withSelectedRoleAndRequestingSelectRolePage_shouldCleanupSessionAndRedirect() throws Exception {
		container.setServletPath("/secure/selectRole.xhtml");
		container.setRequestURI("/secure/selectRole.xhtml");
		HttpSession oldSession = createMock(HttpSession.class);
		HttpSession newSession = createMock(HttpSession.class);
		when(container.getRequestMock().getSession(false)).thenReturn(oldSession);
		when(container.getRequestMock().getSession(true)).thenReturn(newSession);

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(oldSession).setAttribute(SelectRoleFilter.OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY, true);
		verify(oldSession).invalidate();
		verify(userDataInstance.get()).invalidateRoleSelection();
		verify(newSession).setAttribute(eq(SelectRoleFilter.OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY), any(UserData.class));
		verify(container.getResponseMock()).sendRedirect("/secure/selectRole.xhtml");
	}

	@Test
	public void doFilter_withNoSelectedRoleAndIsScanningLogin_checkChainFiltering() throws Exception {
		setNoSelectedRole();
		container.setServletPath("/secure/scanningLoginSelectElectionEvent.xhtml");

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		container.verifyChainDoFilter();
	}

	@Test
	public void doFilter_withNoSelectedRole_checkGotoAndRedirect() throws Exception {
		setNoSelectedRole();
		container.setServletPath("/secure/someURL.xhtml");

		filter.doFilter(container.getRequestMock(), container.getResponseMock(), container.getFilterChainMock());

		verify(container.getHttpSessionMock()).setAttribute("goto", "/secure/someURL.xhtml");
		verify(container.getResponseMock()).sendRedirect("/secure/selectRole.xhtml");
	}

	private void setNoSelectedRole() {
		when(userDataInstance.get().getOperatorRole()).thenReturn(null);
	}

	private void setOperator() {
		Operator operator = mock(Operator.class, RETURNS_DEEP_STUBS);
		when(operator.getId()).thenReturn("id");
		when(operator.getFirstName()).thenReturn("firstName");
		when(operator.getLastName()).thenReturn("lastName");
        when(getInjectMock(OperatorService.class).findOperatorsById(any())).thenReturn(Collections.singletonList(operator));
	}
}
