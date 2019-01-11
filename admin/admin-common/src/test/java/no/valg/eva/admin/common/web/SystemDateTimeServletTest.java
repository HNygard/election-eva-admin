package no.valg.eva.admin.common.web;

import static no.valg.eva.admin.common.web.SystemDateTimeServlet.DATE_TIME_FORMAT;
import static no.valg.eva.admin.common.web.SystemDateTimeServlet.PARAMETER_FIXED_TIME;
import static no.valg.eva.admin.common.web.SystemDateTimeServlet.PARAMETER_TIME_SETTING;
import static no.valg.eva.admin.common.web.SystemDateTimeServlet.VALUE_TIME_SETTING_FIXED;
import static no.valg.eva.admin.common.web.SystemDateTimeServlet.VALUE_TIME_SETTING_SYSTEM;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.test.MockUtilsTestCase;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SystemDateTimeServletTest extends MockUtilsTestCase {

	private SystemDateTimeServlet systemDateTimeServlet;
	private HttpServletRequest stubRequest;
	private HttpServletResponse stubResponse;
	private PrintWriter mockPrintWriter;

	@BeforeMethod
	public void setUp() throws Exception {
		systemDateTimeServlet = getSystemDateTimeServletWithChangePermission();
		stubRequest = createMock(HttpServletRequest.class);
		stubResponse = createMock(HttpServletResponse.class);
		mockPrintWriter = getMockPrintWriter(stubResponse);
	}

	protected SystemDateTimeServlet getSystemDateTimeServletWithChangePermission() {
		return new SystemDateTimeServlet(true);
	}

	public SystemDateTimeServlet getSystemDateTimeServletWithoutChangePermission() {
		return new SystemDateTimeServlet(false);
	}

	protected PrintWriter getMockPrintWriter(HttpServletResponse stubResponse) throws IOException {
		PrintWriter mockPrintWriter = createMock(PrintWriter.class);
		Mockito.when(stubResponse.getWriter()).thenReturn(mockPrintWriter);
		return mockPrintWriter;
	}

	@Test
	public void doGet_whenDateTimeChangeIsPermitted_createsPageWithForm() throws Exception {
		systemDateTimeServlet.doGet(null, stubResponse);
		
		Mockito.verify(mockPrintWriter).append(Matchers.matches(".*Use the following fixed time.*"));
	}

	@Test
	public void doGet_whenDateTimeChangeIsNotPermitted_returns404() throws Exception {
		systemDateTimeServlet = getSystemDateTimeServletWithoutChangePermission();

		systemDateTimeServlet.doGet(null, stubResponse);

		Mockito.verify(stubResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void doPost_whenDateTimeChangeIsNotPermitted_returns404() throws Exception {
		systemDateTimeServlet = getSystemDateTimeServletWithoutChangePermission();
		Mockito.when(stubRequest.getParameter(PARAMETER_TIME_SETTING)).thenReturn(VALUE_TIME_SETTING_FIXED);

		systemDateTimeServlet.doPost(stubRequest, stubResponse);

		Mockito.verify(stubResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void doPost_whenTimeSettingIsFixed_setsSystemTimeToFixedTime() throws Exception {
		Mockito.when(stubRequest.getParameter(PARAMETER_TIME_SETTING)).thenReturn(VALUE_TIME_SETTING_FIXED);
		String fixedDateTimeString = "2015-01-01T14:00";
		Mockito.when(stubRequest.getParameter(PARAMETER_FIXED_TIME)).thenReturn(fixedDateTimeString);

		systemDateTimeServlet.doPost(stubRequest, stubResponse);

		Mockito.verify(mockPrintWriter).append(Matchers.matches(".*has been changed to fixed value.*"));
		Mockito.verify(mockPrintWriter).append(Matchers.matches(".*" + fixedDateTimeString + ".*"));
		Assertions.assertThat(DateTime.now().toString(DATE_TIME_FORMAT)).isEqualTo(fixedDateTimeString);
	}

	@Test
	public void doPost_whenTimeSettingIsSystem_usesSystemTime() throws Exception {
		Mockito.when(stubRequest.getParameter(PARAMETER_TIME_SETTING)).thenReturn(VALUE_TIME_SETTING_SYSTEM);

		systemDateTimeServlet.doPost(stubRequest, stubResponse);

		Mockito.verify(mockPrintWriter).append(Matchers.matches(".*has been set to system time.*"));
		Assertions.assertThat(LocalDate.now()).isEqualTo(new LocalDate(new Date()));
	}

	@Test
	public void doPost_whenTimeSettingIsNotValid_returnsErrorMessage() throws Exception {
		Mockito.when(stubRequest.getParameter(PARAMETER_TIME_SETTING)).thenReturn("notAValidValue");

		systemDateTimeServlet.doPost(stubRequest, stubResponse);

		Mockito.verify(mockPrintWriter).append(Matchers.matches(".*Invalid value of parameter.*"));
	}

	@Test
	public void doPost_whenTryingToDoCrossSiteScripting_elementsAreEscaped() throws Exception {
		Mockito.when(stubRequest.getParameter(PARAMETER_TIME_SETTING)).thenReturn("<dangerousScript />");

		systemDateTimeServlet.doPost(stubRequest, stubResponse);

		Mockito.verify(mockPrintWriter).append(Matchers.matches(".*&lt;dangerousScript /&gt;.*"));
	}
}
