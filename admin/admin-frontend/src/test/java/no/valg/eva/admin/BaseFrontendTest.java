package no.valg.eva.admin;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteNoRollbackException;
import no.evote.exception.ValidateException;
import no.evote.security.UserData;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.faces.FacesContextBroker;
import no.valg.eva.admin.frontend.faces.RequestContextBroker;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.security.PageAccess;
import no.valg.eva.admin.frontend.user.ctrls.UserAccess;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.util.DateUtil;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.internal.matchers.VarargMatcher;
import org.mockito.stubbing.Stubber;
import org.primefaces.context.RequestContext;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Overstyre;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class BaseFrontendTest extends MockUtilsTestCase {

	public static final String COMMON_DATE_TIME_PATTERN = "HH:mm";
	public static final String COMMON_DATE_PATTERN = "dd.MM.yyyy";
	public static final String COMMON_DATE_PATTERN_PROPERTY = "@common.date.date_pattern";
	public static final String COMMON_DATE_TIME_PATTERN_PROPERTY = "@common.date.time_pattern";
	
	private FacesContext facesContextMock;
	private MessageProvider messageProviderMock;
	private RequestContext requestContextMock;
	private UserData userDataMock;
	private ServletContainer container;
	private ConfigurableNavigationHandler navigationHandler;

	@Override
	protected <T> T initializeMocks(Class<T> cls) throws IllegalAccessException, InstantiationException, NoSuchFieldException, InvocationTargetException {
		T result = super.initializeMocks(cls);
		initializeMocks();
		return result;
	}

	@Override
	protected <T> T initializeMocks(T o) throws NoSuchFieldException, IllegalAccessException {
		o = super.initializeMocks(o);
		initializeMocks();
		return o;
	}

	protected void initializeMocks() {
		if (hasInjectMock(FacesContextBroker.class)) {
			facesContextMock = getInjectMock(FacesContextBroker.class).getContext();
		} else {
			facesContextMock = createMock(FacesContext.class);
		}
		if (hasInjectMock(RequestContextBroker.class)) {
			requestContextMock = getInjectMock(RequestContextBroker.class).getContext();
		} else {
			requestContextMock = createMock(RequestContext.class);
		}
		if (hasInjectMock(MessageProvider.class)) {
			messageProviderMock = getInjectMock(MessageProvider.class);
		} else {
			messageProviderMock = createMock(MessageProvider.class);
		}
		if (hasInjectMock(UserData.class)) {
			userDataMock = getInjectMock(UserData.class);
		} else if (hasInjectMock(UserDataController.class)) {
			userDataMock = getInjectMock(UserDataController.class).getUserData();
		} else {
			userDataMock = createMock(UserData.class);
		}
		navigationHandler = createMock(ConfigurableNavigationHandler.class);

		// Setup faces context and default MessageProvider behaviour!
		MessageUtil.setContext(facesContextMock);
		FacesUtil.setContext(facesContextMock);
		FacesUtil.setRequestContext(requestContextMock);
		when(facesContextMock.getApplication().evaluateExpressionGet(facesContextMock, "#{messageProvider}", MessageProvider.class)).thenReturn(
				messageProviderMock);
		when(facesContextMock.getApplication().getNavigationHandler()).thenReturn(navigationHandler);
        when(messageProviderMock.get(anyString(), any())).thenAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			if (args.length == 1 || (args.length == 2 && args[1] == null)) {
				return args[0];
			}
			return Arrays.toString(invocation.getArguments());
		});
		when(messageProviderMock.getByElectionEvent(anyString(), anyLong()))
				.thenAnswer(invocation -> Arrays.toString(invocation.getArguments()));
        when(messageProviderMock.getWithTranslatedParams(anyString(), ArgumentMatchers.<String>any())).thenAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			if (args.length == 1 || (args.length == 2 && args[1] == null)) {
				return args[0];
			}
			return Arrays.toString(invocation.getArguments());
		});

		when(getMessageProviderMock().get(COMMON_DATE_PATTERN_PROPERTY)).thenReturn(COMMON_DATE_PATTERN);
		when(getMessageProviderMock().get(COMMON_DATE_TIME_PATTERN_PROPERTY)).thenReturn(COMMON_DATE_TIME_PATTERN);
        
		container = new ServletContainer(this);
	}

	protected void setUserDataElection(ElectionPath electionPath) {
		MvElection mvElection = createMock(MvElection.class);
		when(getUserDataMock().getOperatorElectionPath()).thenReturn(electionPath);
		when(getUserDataMock().getOperatorMvElection()).thenReturn(mvElection);
	}

	protected void setUserDataArea(AreaPath areaPath) {
		MvArea mvArea = new MvAreaBuilder(areaPath).getValue();
		when(getUserDataMock().getOperatorAreaPath()).thenReturn(areaPath);
		when(getUserDataMock().getOperatorMvArea()).thenReturn(mvArea);
		when(getUserDataMock().getOperatorAreaLevel()).thenReturn(areaPath.getLevel());
	}

	/**
	 * Will verify that a given java script command was executed with FacesUtil.executeJS();
	 */
	protected void assertFacesUtilExecutedJavaScript(String javaScript) {
		verify(getRequestContextMock()).execute(javaScript);
	}

    protected void assertFacesUtilUpdateDom(String id) {
        verify(getRequestContextMock()).update(id);
    }

	protected void assertFacesMessage(FacesMessage.Severity severity, String summary) {
		assertFacesMessage(null, severity, summary);
	}

    protected void assertFacesMessage(String clientId, FacesMessage.Severity expectedSeverity, String expectedSummary) {
        assertFacesMessages(clientId, singletonList(new FacesMessage(expectedSeverity, expectedSummary, null)));
    }

    protected void assertFacesMessages(List<FacesMessage> expectedMessages) {
        assertFacesMessages(null, expectedMessages);
    }

    private void assertFacesMessages(String clientId, List<FacesMessage> expectedMessages) {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        if (clientId == null) {
            verify(getFacesContextMock(), atLeastOnce()).addMessage(any(), captor.capture());
        } else {
            verify(getFacesContextMock(), atLeastOnce()).addMessage(eq(clientId), captor.capture());
        }
        List<FacesMessage> messages = captor.getAllValues();
        for (FacesMessage expectedMessage : expectedMessages) {
            checkForMessage(expectedMessage, messages);
        }
    }

    private void checkForMessage(FacesMessage expectedMessage, List<FacesMessage> messages) {
        StringBuilder messagesAsString = new StringBuilder();
        for (FacesMessage msg : messages) {
            if (expectedMessage.getSeverity() == msg.getSeverity() && expectedMessage.getSummary().equals(msg.getSummary())) {
                return;
            }
            messagesAsString.append(getString(msg));
        }
        if (messages.size() > 1) {
            assertThat(messagesAsString).isEqualTo(getString(expectedMessage));
        } else {
            assertThat(messages.size()).isEqualTo(1);
            assertThat(messages.get(0).getSeverity()).isEqualTo(expectedMessage.getSeverity());
            assertThat(messages.get(0).getSummary()).isEqualTo(expectedMessage.getSummary());
        }
    }

	private String getString(FacesMessage msg) {
		return new StringBuilder().append("[FacesMessage[severity=").append(msg.getSeverity()).append(",\n\tsummary=").append(msg.getSummary())
				.append(",\n\t\tdetail=").append(msg.getDetail()).append("]]").toString();
	}

	protected FacesContext getFacesContextMock() {
		return facesContextMock;
	}

	protected MessageProvider getMessageProviderMock() {
		return messageProviderMock;
	}

	protected RequestContext getRequestContextMock() {
		return requestContextMock;
	}

	protected UserData getUserDataMock() {
		return userDataMock;
	}

	protected ConfigurableNavigationHandler getNavigationHandler() {
		return navigationHandler;
	}

	protected void stubResolveExpression(String expression, Object result) {
		FacesContext facesContext = getFacesContextMock();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);
		when(valueExp.getValue(elContext)).thenReturn(result);
	}

	protected ServletContainer getServletContainer() {
		return container;
	}

	protected Stubber optimisticLockException() {
		return doThrow(new EvoteException(ErrorCode.ERROR_CODE_0501_OPTIMISTIC_LOCK, null, null));
	}

	protected <T> T optimisticLockExceptionWhen(Class<T> cls) {
		return optimisticLockException().when(getInjectMock(cls));
	}

	protected Stubber evoteException() {
		return evoteException("EvoteException");
	}

	protected Stubber evoteException(String message) {
		return doThrow(new EvoteException(message));
	}

	protected <T> T evoteExceptionWhen(Class<T> cls) {
		return evoteException().when(getInjectMock(cls));
	}

	protected <T> T evoteExceptionWhen(Class<T> cls, String message) {
		return evoteException(message).when(getInjectMock(cls));
	}

	protected Stubber ioException() {
		return doThrow(new IOException("IOException"));
	}

	protected Stubber evoteNoRollbackException(ErrorCode errorCode) {
		return doThrow(new EvoteNoRollbackException(errorCode, null));
	}

	protected Stubber evoteNoRollbackException(String message) {
		return doThrow(new EvoteNoRollbackException(message, null));
	}

	protected Stubber validateException() {
		return doThrow(new ValidateException("@error@"));
	}

	protected <T> List<T> mockList(int size, Class<T> cls) {
		List<T> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			result.add(createMock(cls));
		}
		return result;
	}

	protected void isOverrideAccess() {
		isOverrideAccess(true);
	}

	protected void isOverrideAccess(boolean value) {
		hasAccess(Konfigurasjon_Overstyre, value);
		if (hasInjectMock(UserDataController.class)) {
			when(getInjectMock(UserDataController.class).isOverrideAccess()).thenReturn(value);
			when(getInjectMock(UserDataController.class).isOverrideAccess()).thenReturn(value);
		}
	}

	protected void hasAccess() {
		hasAccess(null);
	}

	protected void hasAccess(Accesses access) {
		if (access == null) {
			if (hasInjectMock(UserAccess.class)) {
                when(getInjectMock(UserAccess.class).hasAccess(any())).thenReturn(true);
				when(getInjectMock(UserAccess.class).isOverrideAccess()).thenReturn(true);
			}
			if (hasInjectMock(UserDataController.class)) {
                when(getInjectMock(UserDataController.class).getUserAccess().hasAccess(ArgumentMatchers.<Accesses[]>any())).thenReturn(true);
				when(getInjectMock(UserDataController.class).isOverrideAccess()).thenReturn(true);
			}
			if (hasInjectMock(PageAccess.class)) {
                when(getInjectMock(PageAccess.class).hasAccess(any(), any())).thenReturn(true);
			}
		} else {
			hasAccess(access, true);
		}
	}

	protected void hasAccess(Accesses access, boolean value) {
		if (hasInjectMock(UserAccess.class)) {
            when(getInjectMock(UserAccess.class).hasAccess(any())).thenReturn(value);
			when(getInjectMock(UserAccess.class).isOverrideAccess()).thenReturn(value);
		}
		if (hasInjectMock(UserDataController.class)) {
			when(getInjectMock(UserDataController.class).getUserAccess().hasAccess(containsAccess(access))).thenReturn(value);
			when(getInjectMock(UserDataController.class).isOverrideAccess()).thenReturn(value);
		}
		if (value && hasInjectMock(PageAccess.class)) {
            when(getInjectMock(PageAccess.class).hasAccess(any(UserData.class), any())).thenReturn(true);
		}
	}

	protected void verify_open(Dialog dialog) {
		verify(dialog).open();
	}

	protected void verify_setTitleAndOpen(Dialog dialog, String title) {
		verify(dialog).setTitleAndOpen(title);
	}

	protected void verify_open(String dialogWidgetVar) {
		verify(getRequestContextMock()).execute("PF('" + dialogWidgetVar + "').show()");
	}

	protected void verify_close(Dialog dialog) {
		verify(dialog).close();
	}

	protected void verify_closeAndUpdate(Dialog dialog, String... updateIds) {
		verify(dialog).closeAndUpdate(updateIds);
	}

	protected void verify_close(String dialogWidgetVar) {
		verify(getRequestContextMock()).execute("PF('" + dialogWidgetVar + "').hide()");
	}

	protected void verify_closeAndUpdate(String dialogWidgetVar, String... updateIds) {
		verify_close(dialogWidgetVar);
		verify(getRequestContextMock()).update(Arrays.asList(updateIds));
	}

    private Accesses containsAccess(Accesses access) {
		return argThat(new ContainsAccess(access));
	}

    private class ContainsAccess implements VarargMatcher, Serializable, ArgumentMatcher<Accesses> {

		private Accesses access;

		public ContainsAccess(Accesses access) {
			this.access = access;
		}

        public boolean matches(Accesses accesses) {
            return accesses.is(access);
		}
	}

	protected String formattedDate(LocalDateTime nowJavaTime) {
		return DateUtil.getFormattedDate(nowJavaTime.toLocalDate(), java.time.format.DateTimeFormatter.ofPattern(COMMON_DATE_PATTERN));
	}

	protected String formattedTime(LocalDateTime nowJavaTime) {
		return DateUtil.getFormattedTime(nowJavaTime.toLocalTime(), java.time.format.DateTimeFormatter.ofPattern(COMMON_DATE_TIME_PATTERN));
	}
}
