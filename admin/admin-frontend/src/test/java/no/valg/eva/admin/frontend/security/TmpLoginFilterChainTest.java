package no.valg.eva.admin.frontend.security;

import no.evote.security.SecurityLevel;
import no.evote.service.rbac.OperatorService;
import no.valg.eva.admin.common.auditlog.Outcome;
import org.testng.annotations.Test;

import static no.evote.service.security.ScanningLoginUtil.SELECT_ELECTION_EVENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class TmpLoginFilterChainTest extends SecurityFilterChainTestCase {

	@Override
	protected void filtersInitialized() throws Exception {
		// All tests default run with tmpLogin = true, access to all URLs and not attributes on session.
		// NOTE: From super class, all tests are also executed with no UserData.
		stub_isTmpLoginEnabled(true);
		stub_hasAccess(true);
		when(getServletContainer().getHttpSessionMock().getAttribute(anyString())).thenReturn(null);
	}

	@Test
	public void requestAnySecureURL_withNoUserData_redirectsToTmpLogin() throws Exception {
		request("/secure/any.xhtml");

		assertThat(chainThrough()).isTrue();
		assertRequestURI("/tmpLogin?scanning=false");
		assertUserData(null);
	}

	@Test
	public void requestAnySecureURL_withNoUserDataAndAjaxRequest_returnsHttpStatus401() throws Exception {
		getServletContainer().turnOnAjax();

		request("/secure/any.xhtml");

		assertThat(chainAt(TmpLoginFilter.class)).isTrue();
		assertRequestURI("/secure/any.xhtml");
		assertUserData(null);
		verify(getServletContainer().getResponseMock()).sendError(401);
	}

	@Test
	public void requestScanningLoginURL_withNoUserData_tagsSessionAndRedirectsToTmpLogin() throws Exception {
		request("/secure/" + SELECT_ELECTION_EVENT);

		assertThat(chainThrough()).isTrue();
		assertRequestURI("/tmpLogin?scanning=true");
		assertUserData(null);
	}

	@Test
	public void requestSelectRole_withInvalidTmpLoginData_invalidatesSessionAndRedirectsToWelcomePageWithError() throws Exception {
		setTmpLoginForm("invalidUID", SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC, false);
		stub_hasOperator(false);

		request("/secure/selectRole.xhtml");

		assertThat(chainThrough()).isTrue();
		assertRequestURI("/welcome.xhtml?type=error&scanning=false");
		assertUserData(null);
		verify(getServletContainer().getHttpSessionMock()).invalidate();
		verify_operatorLoginAuditEvent(getTmpLoginFilter(), Outcome.UnknownOperator);
	}

	@Test
	public void requestSelectRole_withTmpLoginForm_createsUserDataAndChainsThrough() throws Exception {
		setTmpLoginForm("validUID", SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC, false);
		stub_hasOperator(true);
		mock_operatorService_findOperatorsById();

		request("/secure/selectRole.xhtml");

		assertThat(chainThrough()).isTrue();
		assertRequestURI("/secure/selectRole.xhtml");
		assertThat(getCurrentUserData().getUid()).isEqualTo("validUID");
		verify_operatorLoginAuditEvent(getTmpLoginFilter(), Outcome.Success);
	}

	@Test
	public void requestSelectRole_withTmpLoginDataAndScanningLogin_createsUserDataAndRedirectsToScanningStartPage() throws Exception {
		setTmpLoginForm("validUID", SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC, true);
		stub_hasOperator(true);
		mock_operatorService_findOperatorsById();

		request("/secure/selectRole.xhtml");

		assertThat(chainThrough()).isTrue();
		assertRequestURI("/secure/scanningLoginSelectElectionEvent.xhtml");
		assertThat(getCurrentUserData().getUid()).isEqualTo("validUID");
		verify_operatorLoginAuditEvent(getTmpLoginFilter(), Outcome.Success);
	}

	private void stub_hasOperator(boolean hasOperator) {
		when(getInjectMock(getTmpLoginFilter(), OperatorService.class).hasOperator(anyString())).thenReturn(hasOperator);
	}

	private void setTmpLoginForm(String uid, SecurityLevel securityLevel, boolean scanning) {
		getServletContainer().getHttpSessionMock().setAttribute(TmpLoginForm.class.getName(), new TmpLoginForm(uid, securityLevel, scanning));
	}
}

