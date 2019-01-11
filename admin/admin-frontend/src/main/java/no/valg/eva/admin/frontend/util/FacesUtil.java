package no.valg.eva.admin.frontend.util;

import no.evote.model.BinaryData;
import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CastBallotBinaryData;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

public final class FacesUtil {
	private static final Logger LOG = Logger.getLogger(FacesUtil.class);
	private static final String DEFAULT_CONTENTTYPE = "application/force-download";
	private static FacesContext context;
	private static RequestContext requestContext;

	private FacesUtil() {
	}

	public static Object resolveExpression(final String expression) {
		FacesContext facesContext = getContext();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);
		return valueExp.getValue(elContext);
	}

	public static UserData getUserData() {
		FacesContext context = getContext();
		return context.getApplication().evaluateExpressionGet(context, "#{userDataProducer.userData}", UserData.class);
	}

	public static void sendFile(final String filename, final byte[] data) throws IOException {
		sendFile(filename, data, DEFAULT_CONTENTTYPE);
	}

	public static void sendFile(final String filename, final byte[] data, final String contentType) throws IOException {
		FacesContext facesContext = getContext();
		ExternalContext context = facesContext.getExternalContext();
		context.responseReset();
		HttpServletResponse response = (HttpServletResponse) context.getResponse();
		response.setContentType(contentType);
		response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		ServletOutputStream out = response.getOutputStream();
		response.setContentLength(data.length);
		out.write(data);
		out.close();

		facesContext.responseComplete();
	}

	public static void sendFile(final BinaryData binaryData) throws IOException {
		sendFile(binaryData.getFileName(), binaryData.getBinaryData(), binaryData.getMimeType());
	}

	public static void sendFile(CastBallotBinaryData castBallotBinaryData) throws IOException {
		sendFile(castBallotBinaryData.fileName(), castBallotBinaryData.bytes(), castBallotBinaryData.mimeType());
	}

	public static void setSessionAttribute(final String key, final Object value) {
		getSession().setAttribute(key, value);
	}

	private static HttpSession getSession() {
		return ((HttpSession) getContext().getExternalContext().getSession(false));
	}

	public static Object getSessionAttribute(final String key) {
		return getSession().getAttribute(key);
	}

	public static void redirect(final String urlString, final boolean encode) {
		FacesContext ctx = getContext();
		ExternalContext extContext = ctx.getExternalContext();
		String url;
		if (encode) {
			url = extContext.encodeActionURL(ctx.getApplication().getViewHandler().getActionURL(ctx, urlString));
		} else {
			url = urlString;
		}
		LOG.info("Got " + urlString + ",  Redirecting to " + url);

		try {
			extContext.redirect(url);
		} catch (IOException ioe) {
			throw new FacesException(ioe);
		}
	}

	/**
	 * Given an object, test if it's String or Integer and either return the parsed integer value or cast it to Integer
	 *
	 * @param object
	 *            An object that is either an String representation of an integer, or an Integer
	 * @return The integer value of the object
	 */
	public static int getIntFromStringOrInteger(final Object object) {
		if (object instanceof String) {
			return Integer.parseInt((String) object);
		} else if (object instanceof Integer) {
			return (Integer) object;
		} else if (object == null) {
			throw new IllegalStateException("Object is null, unable to convert to integer value");
		} else {
			throw new IllegalStateException("Object is of " + object.getClass().getName() + " type, unable to convert to integer value");
		}
	}

	public static ServletContext getServletContext() {
		FacesContext facesContext = getContext();
		ExternalContext externalContext = facesContext.getExternalContext();
		return (ServletContext) externalContext.getContext();
	}

	public static FacesContext getContext() {
		if (context != null) {
			return context;
		}
		return FacesContext.getCurrentInstance();
	}

	/*
	 * Setters only used for mocking in tests
	 */
	public static void setContext(final FacesContext context) {
		FacesUtil.context = context;
	}

	private static RequestContext getRequestContext() {
		if (requestContext != null) {
			return requestContext;
		}
		return RequestContext.getCurrentInstance();
	}

	public static void setRequestContext(final RequestContext context) {
		FacesUtil.requestContext = context;
	}

	public static void updateDom(String name) {
		getRequestContext().update(name);
	}

	public static void executeJS(String js) {
		getRequestContext().execute(js);
	}

	public static void updateDom(Collection<String> names) {
		getRequestContext().update(names);
	}

	public static void addCallbackParam(String key, Object value) {
		getRequestContext().addCallbackParam(key, value);
	}

	public static void scrollTo(String id) {
		getRequestContext().scrollTo(id);
	}
}
