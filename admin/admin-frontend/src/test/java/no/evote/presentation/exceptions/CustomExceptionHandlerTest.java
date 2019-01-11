package no.evote.presentation.exceptions;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.frontend.i18n.ResourceBundleManager;
import no.valg.eva.admin.frontend.i18n.ValidationResourceBundleLocator;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = TestGroups.RESOURCES)
public class CustomExceptionHandlerTest extends BaseFrontendTest {

	private CustomExceptionHandler handler;
	private ExceptionHandler wrappedMock;
	private Iterator<ExceptionQueuedEvent> eventsMock;
	private FacesContext facesContextMock;

	@BeforeMethod
	public void setUp() throws Exception {
		handler = getCustomExceptionHandler(false);
	}

	@Test
	public void handle_withNoQueuedEvents_verifyWrappedHandle() throws Exception {
		handler.handle();

		verify(wrappedMock, times(1)).handle();
	}

	@Test
	public void handle_withUnsupportedException_verifyWrappedHandle() throws Exception {
		mockEvent(new Exception("@exception"));

		handler.handle();

		verify(facesContextMock).responseComplete();
	}

	@Test
	public void handle_withCommitedResponse_shouldLogError() throws Exception {
		handler = getCustomExceptionHandler(true);
		mockEvent(new EvoteException("@exception"));

		handler.handle();
	}

	@Test
	public void handle_withOptimisticLockException_verifyResponseComplete() throws Exception {
		mockEvent(new EvoteException("@evoteException", new OptimisticLockException("@optimisticLockException")));

		handler.handle();

		verify(facesContextMock).responseComplete();
	}

	@Test
	public void handle_withEvoteException_verifyResponseComplete() throws Exception {
		mockEvent(new FacesException("@facesException", new ELException("@elException", new EvoteException("@evoteException"))));

		handler.handle();

		verify(facesContextMock).responseComplete();
	}

	@Test
	public void handle_withFileNotFoundException_verifyResponseComplete() throws Exception {
		mockEvent(new FileNotFoundException("@fileNotFoundException"));

		handler.handle();

		verify(facesContextMock).responseComplete();
	}

	private ExceptionQueuedEvent mockEvent(Throwable e) {
		when(eventsMock.hasNext()).thenReturn(true);
		ExceptionQueuedEvent event = createMock(ExceptionQueuedEvent.class);
		when(eventsMock.next()).thenReturn(event);
		ExceptionQueuedEventContext ctx = createMock(ExceptionQueuedEventContext.class);
		when(event.getSource()).thenReturn(ctx);
		when(ctx.getException()).thenReturn(e);
		return event;
	}

	private CustomExceptionHandler getCustomExceptionHandler(boolean committed) {
		Iterable<ExceptionQueuedEvent> iterable = (Iterable<ExceptionQueuedEvent>) createMock(Iterable.class);
		eventsMock = (Iterator<ExceptionQueuedEvent>) createMock(Iterator.class);
		wrappedMock = createMock(ExceptionHandler.class);
		facesContextMock = createMock(FacesContext.class);
		when(wrappedMock.getUnhandledExceptionQueuedEvents()).thenReturn(iterable);
		when(iterable.iterator()).thenReturn(eventsMock);
		when(facesContextMock.getExternalContext().getRequest()).thenReturn(createMock(HttpServletRequest.class));
		when(facesContextMock.getExternalContext().getResponse()).thenReturn(createMock(HttpServletResponse.class));
		when(facesContextMock.getExternalContext().isResponseCommitted()).thenReturn(committed);
		ValidationResourceBundleLocator fakeValidationResourceBundleLocator = mock(ValidationResourceBundleLocator.class);
		when(fakeValidationResourceBundleLocator.getResourceBundleManager()).thenReturn(createMock(ResourceBundleManager.class));
		return new CustomExceptionHandler(wrappedMock) {
			@Override
			FacesContext getFacesContext() {
				return facesContextMock;
			}
		};
	}

}
