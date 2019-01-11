package no.valg.eva.admin.example;

import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletResponse;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.Test;

public class ExampleServletTest extends BaseFrontendTest {

	@Test
	public void doGet_withMissingParam_returnsBadRequest() throws Exception {
		ExampleServlet servlet = initializeMocks(ExampleServlet.class);

		// FEATURE: You always have mocks for HttpServletRequest and HttpServletResponse
		servlet.doGet(getServletContainer().getRequestMock(), getServletContainer().getResponseMock());

		verify(getServletContainer().getResponseMock()).sendError(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void doGet_withCreateParam_redirectsToSomewhere() throws Exception {
		ExampleServlet servlet = initializeMocks(ExampleServlet.class);
		// FEATURE: Stub a request parameter value. The stubbing is performed at several levels (see implementation).
		getServletContainer().setRequestParameter("op", "create");
		// FEATURE: Stub request uri (see ServletContainer for more request method stubbing.)
		getServletContainer().setRequestURI("/error");

		servlet.doGet(getServletContainer().getRequestMock(), getServletContainer().getResponseMock());

		verify(getServletContainer().getResponseMock()).sendRedirect("/somewhere");
	}

	@Test
	public void doGet_withCreateParamAndAjaxRequest_returnsAjax() throws Exception {
		ExampleServlet servlet = initializeMocks(ExampleServlet.class);
		getServletContainer().setRequestParameter("op", "create");
		getServletContainer().setRequestURI("hello");
		// FEATURE: Short for setHeader("X-Requested-With", "XMLHttpRequest");
		getServletContainer().turnOnAjax();
		// FEATURE: Short for setHeader("User-Agent", str);
		getServletContainer().setUserAgent("Chrome");

		servlet.doGet(getServletContainer().getRequestMock(), getServletContainer().getResponseMock());

		// FEATURE: Theres always a HttpSession mock available.
		verify(getServletContainer().getHttpSessionMock()).setAttribute("foo", "bar");
		verify(getServletContainer().getResponseMock().getWriter()).print("ajax");
	}

}
