package no.evote.presentation.exceptions;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import no.valg.eva.admin.util.IOUtil;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.primefaces.json.JSONObject;

public final class ErrorPageRenderer {

	private static final Logger LOGGER = Logger.getLogger(ErrorPageRenderer.class);
	private static final int NONEXISTENT_CONVERSATION = 550;
	private static final int EXPIRED_VIEW = 551;
	private static final int EXPIRED_SESSION = 552;

	private ErrorPageRenderer() {
	}

	public static void renderError(final HttpServletRequest request, final HttpServletResponse response, Error error) {
		String message = isAjaxRequest(request) ? error.getAjaxMessage() : error.getMessage();
		renderError(request, response, error, message, "");
	}

	public static void renderError(final HttpServletRequest request, final HttpServletResponse response, Error error, String message, Throwable stacktrace) {
		renderError(request, response, error, message, getStackTraceAsString(stacktrace));
	}

	public static String md5(String message) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(message.getBytes(), 0, message.length());
			
			return new BigInteger(1, m.digest()).toString(16).substring(0, 8);
			
		} catch (NoSuchAlgorithmException exception) {
			LOGGER.fatal("Failed to get MD5 Algorithm", exception);
			return String.valueOf(System.currentTimeMillis());
		}
	}

	public static void renderError(final HttpServletRequest request, final HttpServletResponse response, Error error, String message, String stacktrace) {

		String uuid = null;

		if (message == null) {
			message = "No message supplied: " + DateTime.now().toString();
		}

		if (error.isServerError()) {

			uuid = md5(message);
			StringBuilder messageBuilder = new StringBuilder();

			messageBuilder
					.append("IncidentID: ")
					.append(uuid)
					.append(" ErrorCode: ")
					.append(error.getCode())
					.append("\n")
					.append(message)
					.append("\nRequest: ")
					.append(getRequestInfo(request))
					.append("\n")
					.append(stacktrace);

			if (error.code == EXPIRED_VIEW || error.code == EXPIRED_SESSION) {
				// Dette er forventet å skje en gang i blant, og er "brukerfeil"
				LOGGER.info(messageBuilder);
			} else {
				LOGGER.error(messageBuilder);
			}
		}

		if (isAjaxRequest(request)) {
			renderJSON(response, error, uuid);
		} else {
			renderPage(response, error, uuid);
		}
	}

	private static boolean isAjaxRequest(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}

	private static String getRequestInfo(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getRequestURI());
		if (request.getQueryString() != null) {
			sb.append("?").append(request.getQueryString());
		}
		sb.append(", Method: ").append(request.getMethod());
		HttpSession session = request.getSession(false);
		if (session != null) {
			sb.append(", Session: ").append(session.getId());
		}
		return sb.toString();
	}

	private static String getPageTemplate(Error error, String incidentCode) throws IOException {

		InputStream is = ErrorPageRenderer.class.getClassLoader().getResourceAsStream("errorpage.html");
		String title = error.getTitle();
		String message = error.getMessage();
		int statusCode = error.getCode();

		// Add exception message and stack trace to the error page
		String errorPage = new String(IOUtil.getBytes(is), "UTF-8");
		if (title != null) {
			errorPage = errorPage.replace("%title", StringEscapeUtils.escapeHtml4(title));
		}

		if (message != null) {
			errorPage = errorPage.replace("%message", message);
		}

		if (incidentCode == null || incidentCode.isEmpty()) {
			errorPage = errorPage.replace("%incident", "");
		} else {
			errorPage = errorPage.replace("%incident", StringEscapeUtils.escapeHtml4(incidentCode) + ":");
		}
		errorPage = errorPage.replace("%errorcode", Integer.toString(statusCode));

		return errorPage;
	}

	private static String getJSONTemplate(Error error, String incidentCode) {

		Map<String, Object> jsonResponse = new HashMap<>();

		jsonResponse.put("title", error.getTitle());
		jsonResponse.put("message", error.getAjaxMessage());
		if (incidentCode != null) {
			jsonResponse.put("incident", incidentCode);
		}
		jsonResponse.put("statusCode", error.getCode());

		return new JSONObject(jsonResponse).toString();
	}

	private static void renderPage(HttpServletResponse response, Error error, String incidentCode) {

		response.resetBuffer();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(error.getCode());

		try {
			Writer w = response.getWriter();
			w.write(ErrorPageRenderer.getPageTemplate(error, incidentCode));
			w.flush();
		} catch (IOException exception) {
			LOGGER.fatal("Unable to get response writer", exception);
		}

	}

	private static void renderJSON(final HttpServletResponse response, Error error, String incidentCode) {

		response.resetBuffer();
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=utf-8");
		response.setStatus(error.getCode());

		try {
			Writer w = response.getWriter();
			w.write(ErrorPageRenderer.getJSONTemplate(error, incidentCode));
			w.flush();
		} catch (IOException exception) {
			LOGGER.fatal("Unable to get response writer", exception);
		}

	}

	private static String getStackTraceAsString(final Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}

	public enum Error {

		NOT_FOUND(SC_NOT_FOUND),
		INTERNAL_SERVER_ERROR(SC_INTERNAL_SERVER_ERROR),
		UNAUTHORIZED(SC_UNAUTHORIZED),
		BAD_REQUEST(SC_BAD_REQUEST),
		NON_EXISTING_CONVERSATION(NONEXISTENT_CONVERSATION),
		VIEW_EXPIRED(EXPIRED_VIEW),
		SESSION_EXPIRED(EXPIRED_SESSION);

		private static final String ERROR_404_TITLE = "Siden finnes ikke.";
		private static final String ERROR_404_MESSAGE = "Siden du prøvde å gå til finnes ikke.";
		private static final String ERROR_500_TITLE = "En feil har oppstått.";
		private static final String ERROR_500_MESSAGE = "Det oppstod en feil. Vent litt og forsøk igjen. Kontakt support om problemet vedvarer.";
		private static final String ERROR_400_TITLE = "Ugyldig forespørsel.";
		private static final String ERROR_400_MESSAGE = "Ugyldig forespørsel.";
		private static final String ERROR_401_TITLE = "Ingen tilgang.";
		private static final String ERROR_401_MESSAGE = "Du har ikke tilgang til denne siden.";
		private static final String ERROR_NONEXISTENT_CONVERSATION_TITLE = "Operasjon feilet!";
		private static final String ERROR_NONEXISTENT_CONVERSATION_MESSAGE_AJAX = "Operasjon feilet! Gå til Min side og prøv på nytt.";
		private static final String ERROR_NONEXISTENT_CONVERSATION_MESSAGE = "Operasjon feilet! Gå til <a href=\"/secure/index.xhtml\">Min side</a> og prøv på nytt.";
		private static final String ERROR_VIEW_EXPIRED_TITLE = "Siden er utløpt";
		private static final String ERROR_VIEW_EXPIRED_MESSAGE_AJAX =
			"Siden du jobber i er utløpt. Gå til Min side og prøv på nytt. Unngå å ha flere vinduer oppe samtidig.";
		private static final String ERROR_VIEW_EXPIRED_MESSAGE =
			"Siden du jobber i er utløpt. Gå til <a href=\"/secure/index.xhtml\">Min side</a> og prøv på nytt. Unngå å ha flere vinduer oppe samtidig";
		private static final String ERROR_SESSION_EXPIRED_TITLE = "Siden er utløpt";
		private static final String ERROR_SESSION_EXPIRED_MESSAGE_AJAX =
			"Siden du jobber i er utløpt. Logg inn på nytt om nødvendig, gå til min side og prøv på nytt. Unngå å ha flere vinduer oppe samtidig.";
		private static final String ERROR_SESSION_EXPIRED_MESSAGE =
			"Siden du jobber i er utløpt. <a href=\"/\">Logg inn</a> på nytt om nødvendig, gå til <a href=\"/secure/index.xhtml\">Min side</a>" +
				" og prøv på nytt. Unngå å ha flere vinduer oppe samtidig";

		private int code;

		public static Error getErrorByCode(int code) {
			switch (code) {
			case SC_NOT_FOUND:
				return Error.NOT_FOUND;
			case SC_INTERNAL_SERVER_ERROR:
				return Error.INTERNAL_SERVER_ERROR;
			case SC_UNAUTHORIZED:
				return Error.UNAUTHORIZED;
			case NONEXISTENT_CONVERSATION:
				return Error.NON_EXISTING_CONVERSATION;
			case EXPIRED_VIEW:
				return Error.VIEW_EXPIRED;	
			case EXPIRED_SESSION:
				return Error.SESSION_EXPIRED;	

			default:
			case SC_BAD_REQUEST:
				return Error.BAD_REQUEST;
			}
		}

		private Error(int code) {
			this.code = code;
		}

		public String getMessage() {

			switch (this.code) {

			case SC_NOT_FOUND:
				return ERROR_404_MESSAGE;

			case SC_UNAUTHORIZED:
				return ERROR_401_MESSAGE;

			case SC_INTERNAL_SERVER_ERROR:
				return ERROR_500_MESSAGE;

			case SC_BAD_REQUEST:
				return ERROR_400_MESSAGE;

			case NONEXISTENT_CONVERSATION:
				return ERROR_NONEXISTENT_CONVERSATION_MESSAGE;
				
			case EXPIRED_VIEW:
				return ERROR_VIEW_EXPIRED_MESSAGE;
				
			case EXPIRED_SESSION:
				return ERROR_SESSION_EXPIRED_MESSAGE;

			default:
				return "";

			}
		}

		public String getAjaxMessage() {
			if (this.code == NONEXISTENT_CONVERSATION) {
				return ERROR_NONEXISTENT_CONVERSATION_MESSAGE_AJAX;
			} else if (this.code == EXPIRED_VIEW) {
				return ERROR_VIEW_EXPIRED_MESSAGE_AJAX;
			} else if (this.code == EXPIRED_SESSION) {
				return ERROR_SESSION_EXPIRED_MESSAGE_AJAX;
			}
			return getMessage();
		}

		public String getTitle() {
			switch (this.code) {

			case SC_NOT_FOUND:
				return ERROR_404_TITLE;

			case SC_UNAUTHORIZED:
				return ERROR_401_TITLE;

			case SC_INTERNAL_SERVER_ERROR:
				return ERROR_500_TITLE;

			case SC_BAD_REQUEST:
				return ERROR_400_TITLE;

			case NONEXISTENT_CONVERSATION:
				return ERROR_NONEXISTENT_CONVERSATION_TITLE;
				
			case EXPIRED_VIEW:
				return ERROR_VIEW_EXPIRED_TITLE;	

			case EXPIRED_SESSION:
				return ERROR_SESSION_EXPIRED_TITLE;	

			default:
				return "";

			}
		}

		public int getCode() {
			return this.code;
		}

		public boolean isServerError() {
			return this.code >= 500 && this.code < 600;
		}
	}
}
