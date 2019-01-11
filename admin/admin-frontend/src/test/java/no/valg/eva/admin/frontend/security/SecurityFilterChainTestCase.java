package no.valg.eva.admin.frontend.security;

import no.evote.security.UserData;
import no.evote.security.UserDataProducer;
import no.evote.service.rbac.OperatorService;
import no.evote.service.security.PageAccessFilter;
import no.evote.service.security.SelectRoleFilter;
import no.valg.eva.admin.common.auditlog.AuditLogService;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.OperatorLoginAuditEvent;
import no.valg.eva.admin.frontend.FilterChainTestCase;
import no.valg.eva.admin.rbac.domain.model.Operator;
import org.mockito.ArgumentCaptor;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

import static no.evote.util.MockUtils.setPrivateField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public abstract class SecurityFilterChainTestCase extends FilterChainTestCase {

	@Override
	protected FilterChainConfig[] getFilterChain() throws Exception {
		return new FilterChainConfig[]{
				new FilterChainConfig(TmpLoginFilter.class, "/secure/*"),
				new FilterChainConfig(OidcFilter.class, "/secure/*"),
				new FilterChainConfig(SelectRoleFilter.class, "/secure/*"),
				new FilterChainConfig(PageAccessFilter.class, "/secure/*")
		};
	}

	protected void verify_operatorLoginAuditEvent(Filter filter, Outcome outcome) {
		ArgumentCaptor<OperatorLoginAuditEvent> captor = ArgumentCaptor.forClass(OperatorLoginAuditEvent.class);
		verify(getInjectMock(filter, AuditLogService.class)).addToAuditTrail(captor.capture());
		assertThat(captor.getValue().outcome()).isSameAs(outcome);
	}

	protected UserData getCurrentUserData() throws Exception {
		return getMockedInstance(getOidcFilter(), "userDataProducerInstance", UserDataProducer.class).getUserData();
	}

	protected void stub_isTmpLoginEnabled(boolean isTmpLoginEnabled) throws Exception {
		TmpLoginDetector tmpLoginDetector = createMock(TmpLoginDetector.class);
		when(tmpLoginDetector.isTmpLoginEnabled()).thenReturn(isTmpLoginEnabled);
		setPrivateField(getTmpLoginFilter(), "tmpLoginDetector", tmpLoginDetector);
	}

	protected void assertRequestURI(String uri) {
		assertThat(getServletContainer().getRequestMock().getRequestURI()).isEqualTo(uri);
	}

	protected void stub_hasAccess(boolean hasAccess) {
		when(getInjectMock(getPageAccessFilter(), PageAccess.class).hasAccess(any(UserData.class), anyString()))
				.thenReturn(hasAccess);
	}

	protected void mock_operatorService_findOperatorsById() {
		List<Operator> operatorList = new ArrayList<>();
		Operator operator = new Operator();
		operator.setId("id");
		operator.setFirstName("Test");
		operator.setLastName("Testesen");
		operator.setMiddleName("Middle");
		operatorList.add(operator);
		when(getInjectMock(getSelectRoleFilter(), OperatorService.class).findOperatorsById(any())).thenReturn(operatorList);
	}

	protected TmpLoginFilter getTmpLoginFilter() {
		return (TmpLoginFilter) getFilters()[0];
	}

	protected OidcFilter getOidcFilter() {
		return (OidcFilter) getFilters()[1];
	}

	private SelectRoleFilter getSelectRoleFilter() {
		return (SelectRoleFilter) getFilters()[2];
	}

	private PageAccessFilter getPageAccessFilter() {
		return (PageAccessFilter) getFilters()[3];
	}

}

