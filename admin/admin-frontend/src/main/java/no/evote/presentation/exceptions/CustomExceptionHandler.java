package no.evote.presentation.exceptions;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import javax.el.ELException;
import javax.enterprise.context.NonexistentConversationException;
import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.evote.exception.EvoteException;
import no.evote.exception.EvoteSecurityException;

import org.apache.log4j.Logger;

/**
 * Catches all exceptions and redirects to error page.
 */
@SuppressWarnings("deprecation")
public class CustomExceptionHandler extends ExceptionHandlerWrapper {
	private static final Logger LOGGER = Logger.getLogger(CustomExceptionHandler.class);

	private final ExceptionHandler wrapped;

	public CustomExceptionHandler(ExceptionHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return this.wrapped;
	}

	@Override
	public void handle() {
		Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();
		if (i.hasNext()) {
			// Default error handling renders the errorpage.html file found in /resources.
			FacesContext facesContext = getFacesContext();
			ExternalContext externalContext = facesContext.getExternalContext();
			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

			// Get the exception
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) i.next().getSource();
			Throwable exception = context.getException();
			String stackTrace = getStackTraceAsString(exception);
			String message = exception.getMessage();

			if ((exception = isJPAException(context.getException())) != null) {
				stackTrace = "";
				message = exception.getMessage();
			} else if ((isEvoteSecurityException(context.getException())) != null) {
				String messsage = "User is unauthorized to view " + request.getRequestURI();
				ErrorPageRenderer.renderError(request, response, ErrorPageRenderer.Error.UNAUTHORIZED, messsage, messsage);
				return;
			} else if ((exception = isEvoteException(context.getException())) != null) {
				stackTrace = getStackTraceAsString(exception);
				message = exception.getMessage();
			} else if (isNotFoundException(context.getException()) != null) {
				ErrorPageRenderer.renderError(request, response, ErrorPageRenderer.Error.NOT_FOUND);
				facesContext.responseComplete();
				return;
			} else if ((exception = isNonexistentConversationException(context.getException())) != null) {
				stackTrace = getStackTraceAsString(exception);
				message = exception.getMessage();
				ErrorPageRenderer.renderError(request, response, ErrorPageRenderer.Error.NON_EXISTING_CONVERSATION, message, stackTrace);
				facesContext.responseComplete();
				return;
			} else if ((exception = isViewExpiredException(context.getException())) != null) {
				stackTrace = getStackTraceAsString(exception);
				message = exception.getMessage();
				ErrorPageRenderer.renderError(request, response, ErrorPageRenderer.Error.VIEW_EXPIRED, message, stackTrace);
				facesContext.responseComplete();
				return;
			} else if ((exception = isSessionExpiredException(context.getException())) != null) {
				stackTrace = getStackTraceAsString(exception);
				message = exception.getMessage();
				ErrorPageRenderer.renderError(request, response, ErrorPageRenderer.Error.SESSION_EXPIRED, message, stackTrace);
				facesContext.responseComplete();
				return;
			} else {
				exception = context.getException();
			}

			if (!externalContext.isResponseCommitted()) {
				ErrorPageRenderer.renderError(request, response, ErrorPageRenderer.Error.INTERNAL_SERVER_ERROR, message, stackTrace);
				facesContext.responseComplete();
				return;
			} else {
				// If the response has been committed (i.e. HTTP headers have been written, etc.), there's not much we can do
				LOGGER.fatal(exception.getMessage(), exception);
			}
		}

		getWrapped().handle();
	}

	private Throwable isNotFoundException(Throwable t) {
		if (t == null) {
			return null;
		}
		if (t instanceof FileNotFoundException) {
			return t;
		}
		return isNotFoundException(t.getCause());
	}

	private Throwable isEvoteSecurityException(Throwable t) {
		if (t == null) {
			return null;
		}
		if (t instanceof EvoteSecurityException) {
			return t;
		}
		if (t instanceof FacesException
				&& (t.getCause() instanceof EvaluationException || t.getCause() instanceof ELException) && t.getCause().getCause() instanceof EvoteSecurityException) {
			return t.getCause().getCause();
		}
		return isEvoteException(t.getCause());
	}

	private Throwable isEvoteException(Throwable t) {
		if (t == null) {
			return null;
		}
		if (t instanceof FacesException
				&& (t.getCause() instanceof EvaluationException || t.getCause() instanceof ELException) && t.getCause().getCause() instanceof EvoteException) {
			return t.getCause().getCause();
		}
		return isEvoteException(t.getCause());
	}

	private Throwable isJPAException(Throwable t) {
		if (t == null) {
			return null;
		}
		if (t instanceof EvoteException
				&& (t.getCause() instanceof OptimisticLockException || t.getCause() instanceof EntityNotFoundException)) {
			return t.getCause();
		}
		return isJPAException(t.getCause());
	}

	private Throwable isNonexistentConversationException(Throwable t) {
		if (t == null) {
			return null;
		}
		if (NonexistentConversationException.class.isAssignableFrom(t.getClass())) {
			return t;
		}
		return isNonexistentConversationException(t.getCause());
	}

	private Throwable isViewExpiredException(Throwable t) {
		if (t == null) {
			return null;
		}
		if (ViewExpiredException.class.isAssignableFrom(t.getClass())) {
			return t;
		}
		return isViewExpiredException(t.getCause());
	}

	private Throwable isSessionExpiredException(Throwable t) {
		if (t == null) {
			return null;
		}
		if (IllegalStateException.class.isAssignableFrom(t.getClass()) && t.getMessage().contains("Session not found")) {
			return t;
		}
		return isSessionExpiredException(t.getCause());
	}

	private String getStackTraceAsString(final Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	/** For testing purposes */
	FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}
}
