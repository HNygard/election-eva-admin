package no.valg.eva.admin.common.web;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.evote.util.EvoteProperties;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.format.DateTimeFormat;

/**
 * Interface for allowing system time to be manipulated.
 * - This interface should only be accessible in test environments (set test.can.change.time to true in evote.properties)
 * - Note also that calling this servlet only affects the code in the same VM. If several VMs are employed, this service
 *   must be called on all of them
 */
@WebServlet(urlPatterns = "/systemDateTime")
public class SystemDateTimeServlet extends HttpServlet {
	static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm";
	static final String PARAMETER_TIME_SETTING = "timeSetting";
	static final String PARAMETER_FIXED_TIME = "fixedTime";
	static final String VALUE_TIME_SETTING_FIXED = "fixed";
	static final String VALUE_TIME_SETTING_SYSTEM = "system";
	
	private final Logger logger = Logger.getLogger(SystemDateTimeServlet.class);
	private final Boolean canChangeTime;
	
	public SystemDateTimeServlet() {
		this.canChangeTime = EvoteProperties.getBooleanProperty(EvoteProperties.TEST_CAN_CHANGE_TIME, false);
	}
	
	SystemDateTimeServlet(boolean canChangeTime) {
		this.canChangeTime = canChangeTime;
	}
	
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		Writer w = getOutputWriter(resp);

		if (canChangeTime()) {
			w.append(buildSetDateTimeFormHtml());
		} else {
			sendNotFoundResponse(resp);
		}
	}

	private Writer getOutputWriter(HttpServletResponse resp) throws IOException {
		Writer w = resp.getWriter();
		resp.setContentType("text/html");
		return w;
	}

	private boolean canChangeTime() {
		return canChangeTime;
	}

	private String buildSetDateTimeFormHtml() {
		return wrapInHtmlHeaders(getCurrentDateTimeString()
			+ "<form method=\"POST\">"
			+ "<input type='radio' name='" + PARAMETER_TIME_SETTING + "' value='" + VALUE_TIME_SETTING_FIXED + "'/> "
			+ "Use the following fixed time <input type='text' name='" + PARAMETER_FIXED_TIME + "' /> (format " + DATE_TIME_FORMAT + ")<br/>"
			+ "<input type='radio' name='" + PARAMETER_TIME_SETTING + "' value='" + VALUE_TIME_SETTING_SYSTEM + "'/> Use system time<br/>"
			+ "<input type='submit' value='Update'/>"
			+ "</form>");
	}

	private String wrapInHtmlHeaders(String html) {
		return "<html><head></head><body>"
			+ html
			+ "</body></html>";
	}

	private void sendNotFoundResponse(HttpServletResponse resp) throws IOException {
		logger.warn("An attempt was made to access the servlet for changing date/time. This functionality is turned off, so a call to this page indicates "
			+ "either wrong configuration or a malicious attempt to change time");
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	private String getCurrentDateTimeString() {
		return "<p>Current date/time is " + DateTime.now().toString(DATE_TIME_FORMAT) + "</p>";
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (canChangeTime()) {
			handleSettingOfDateTime(req, resp);
		} else {
			sendNotFoundResponse(resp);
		}
	}

	private void handleSettingOfDateTime(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Writer w = getOutputWriter(resp);
		String timeSetting = req.getParameter(PARAMETER_TIME_SETTING);
		String timeSettingForOutput = StringEscapeUtils.escapeHtml4(timeSetting);
		String fixedDateTimeString = req.getParameter(PARAMETER_FIXED_TIME);
		String fixedDateTimeStringForOutput = StringEscapeUtils.escapeHtml4(fixedDateTimeString);

		logger.info("Date/time change has been requested through the SystemDateTimeServlet, with parameters " + PARAMETER_TIME_SETTING + "=" + timeSetting
			+ " and " + PARAMETER_FIXED_TIME + "=" + fixedDateTimeString);
		if (VALUE_TIME_SETTING_FIXED.equals(timeSetting)) {
			DateTime fixedTime = DateTimeFormat.forPattern(DATE_TIME_FORMAT).parseDateTime(fixedDateTimeString);
			setTime(fixedTime);
			w.append(wrapInHtmlHeaders("Date/time has been changed to fixed value " + fixedDateTimeStringForOutput + getLinkToSystemDateTimePage()));
		} else if (VALUE_TIME_SETTING_SYSTEM.equals(timeSetting)) {
			setSystemTime();
			w.append(wrapInHtmlHeaders("Date/time has been set to system time. " + getCurrentDateTimeString() + getLinkToSystemDateTimePage()));
		} else {
			w.append(wrapInHtmlHeaders("Invalid value of parameter 'timeSetting': " + timeSettingForOutput + ". " + getLinkToSystemDateTimePage()));
		}
	}

	private String getLinkToSystemDateTimePage() {
		return "<p>Go to <a href='systemDateTime'>date/time configuration page</a></p>";
	}

	private void setSystemTime() {
		DateTimeUtils.setCurrentMillisSystem();
	}
	
	private void setTime(DateTime fixedDateTime) {
		DateTimeUtils.setCurrentMillisFixed(fixedDateTime.getMillis());
	}
}
