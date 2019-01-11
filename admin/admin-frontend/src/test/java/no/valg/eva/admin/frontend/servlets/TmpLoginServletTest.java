package no.valg.eva.admin.frontend.servlets;

import no.evote.security.SecurityLevel;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;
import no.valg.eva.admin.frontend.security.TmpLoginDetector;
import no.valg.eva.admin.frontend.security.TmpLoginForm;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TmpLoginServletTest extends BaseFrontendTest {

	@Test
	public void doGet_withTmpLoginDisabled_returnsNotFound() throws Exception {
		TmpLoginServlet servlet = initializeMocks(new TestTmpLoginServlet());
		ServletContainer container = getServletContainer();
		when(servlet.getTmpLoginDetector().isTmpLoginEnabled()).thenReturn(false);

		servlet.doGet(container.getRequestMock(), container.getResponseMock());

		verify(container.getResponseMock()).sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	public void doGet_withTmpLoginEnabledNotScanningLogin_verifySessionInvalidatedAndResponse() throws Exception {
		TmpLoginServlet servlet = initializeMocks(new TestTmpLoginServlet());
		ServletContainer container = getServletContainer();
		when(servlet.getTmpLoginDetector().isTmpLoginEnabled()).thenReturn(true);
		HttpSession session = container.getHttpSessionMock();

		servlet.doGet(container.getRequestMock(), container.getResponseMock());

		verify(session).invalidate();
		verify(container.getResponseMock()).setContentType("text/html");
		verify(container.getResponseMock().getWriter()).append("<html><head><link type=\"text/css\" "
				+ "href=\"/javax.faces.resource/all.css.xhtml?ln=css&rv=4.0%20(90e94bd)\" "
				+ "rel=\"stylesheet\"></head><body id=\"page-tmplogin\"> "
				+ "<form method=\"POST\" action=\"/tmpLogin?scanning=false\">Bruker ID: <input type=\"text\" name=\"username\" autofocus=\"autofocus\"/> <br/>Security "
				+ "level: <input type=\"text\" name=\"secLevel\" value=\"3\"/> <br/>"
				+ "<input type=\"submit\" value=\"Login\"/></form></body></html>");
	}

	@Test
	public void doPost_withUsernameAndSecLevel_verifyAttributesSetAndRedirect() throws Exception {
		TmpLoginServlet servlet = initializeMocks(new TestTmpLoginServlet());
		ServletContainer container = getServletContainer();
		HttpSession session = container.getHttpSessionMock();
		container.setRequestParameter("username", "12345");
		container.setRequestParameter("secLevel", "3");
		container.setRequestParameter("scanning", "true");

		servlet.doPost(container.getRequestMock(), container.getResponseMock());

		ArgumentCaptor<TmpLoginForm> captor = ArgumentCaptor.forClass(TmpLoginForm.class);
		verify(session).setAttribute(eq(TmpLoginForm.class.getName()), captor.capture());
		assertThat(captor.getValue()).isEqualToComparingFieldByField(new TmpLoginForm("12345", SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC, true));
		verify(container.getResponseMock()).sendRedirect("/secure/selectRole.xhtml");
	}

	class TestTmpLoginServlet extends TmpLoginServlet {
		private TmpLoginDetector tmpLoginDetector;

		TestTmpLoginServlet() {
			tmpLoginDetector = createMock(TmpLoginDetector.class);
		}

		@Override
		TmpLoginDetector getTmpLoginDetector() {
			return tmpLoginDetector;
		}
	}
}
