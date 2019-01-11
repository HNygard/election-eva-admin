package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.Counts;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.common.counting.service.CountingService;
import no.valg.eva.admin.common.counting.service.ModifiedBallotBatchService;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.enterprise.context.Conversation;
import javax.faces.context.ExternalContext;
import java.lang.reflect.InvocationTargetException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseCountControllerTest extends BaseFrontendTest {
	private StartCountingController startCountingControllerMock;
	private UserDataController userDataControllerMock;
	private Counts counts;

	@Override
	public <T> T initializeMocks(Class<T> cls) throws NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException {
		T result = super.initializeMocks(cls);
		return initializeCountMocks(result);
	}

	@Override
	public <T> T initializeMocks(T testObject) throws NoSuchFieldException, IllegalAccessException {
		testObject = super.initializeMocks(testObject);
		return initializeCountMocks(testObject);
	}

	private <T> T initializeCountMocks(T testObject) {
		counts = mock(Counts.class, RETURNS_DEEP_STUBS);
		if (hasInjectMock(CountingService.class)) {
			when(getCountingServiceMock().getCounts(any(UserData.class), any(CountContext.class), any(AreaPath.class))).thenReturn(counts);
		}
		if (testObject instanceof CountController) {
			startCountingControllerMock = mock(StartCountingController.class, RETURNS_DEEP_STUBS);
			when(startCountingControllerMock.getMessageProvider()).thenReturn(getMessageProviderMock());
			when(startCountingControllerMock.getCounts()).thenReturn(counts);
			((CountController) testObject).setStartCountingController(startCountingControllerMock);
			userDataControllerMock = mock(UserDataController.class, RETURNS_DEEP_STUBS);
			((CountController) testObject).setUserDataController(userDataControllerMock);
		}

		ExternalContext externalContext = getFacesContextMock().getExternalContext();
		when(externalContext.encodeActionURL(anyString())).thenAnswer(answerArgument(0));
		when(getFacesContextMock().getApplication().getViewHandler().getActionURL(eq(getFacesContextMock()), anyString())).thenAnswer(answerArgument(1));
		getServletContainer().setRequestParameter("category", "VO");
		return testObject;
	}

	protected CountingService getCountingServiceMock() {
		return getInjectMock(CountingService.class);
	}

	protected Conversation getConversationMock() {
		return getInjectMock(Conversation.class);
	}

	protected FinalCountController getFinalCountControllerMock() {
		return getInjectMock(FinalCountController.class);
	}

	protected CountyFinalCountController getCountyFinalCountControllerMock() {
		return getInjectMock(CountyFinalCountController.class);
	}

	protected ModifiedBallotBatchService getModifiedBallotBatchServiceMock() {
		return getInjectMock(ModifiedBallotBatchService.class);
	}

	protected PreliminaryCountController getPreliminaryCountControllerMock() {
		return getInjectMock(PreliminaryCountController.class);
	}

	protected ContestInfoService getContestInfoServiceMock() {
		return getInjectMock(ContestInfoService.class);
	}

	protected CompareCountsController getCompareCountsControllerMock() {
		return getInjectMock(CompareCountsController.class);
	}

	protected Counts getCountsMock() {
		return counts;
	}

	protected StartCountingController getStartCountingControllerMock() {
		return startCountingControllerMock;
	}

	public UserDataController getUserDataControllerMock() {
		return userDataControllerMock;
	}

	private Answer<String> answerArgument(final int index) {
		return new Answer<String>() {
			@Override
			public String answer(final InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				return (String) args[index];
			}
		};
	}
}
