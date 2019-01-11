package no.evote.presentation.exceptions;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;
import no.valg.eva.admin.test.TestGroups;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = TestGroups.RESOURCES)
public class ErrorPageRendererTest extends BaseFrontendTest {

	private PrintWriter writer;
	private ServletContainer container;

	@BeforeMethod
	public void setUp() throws Exception {
		initializeMocks();
		container = getServletContainer();
		writer = mock(PrintWriter.class);
		when(container.getResponseMock().getWriter()).thenReturn(writer);
	}

	@Test
    public void renderError_withRequestResponseError_verifyResponse() {
		container.setQueryString("a=b");
		Enumeration<String> enumeration = new Enumeration<String>() {
			private int count = 0;
			@Override
			public boolean hasMoreElements() {
				return count < 3;
			}

			@Override
			public String nextElement() {
				return "count" + count++;
			}
		};
		when(container.getRequestMock().getAttributeNames()).thenReturn(enumeration);
		when(container.getRequestMock().getAttribute(anyString())).thenReturn("attrValue");
		ErrorPageRenderer.renderError(container.getRequestMock(), container.getResponseMock(), ErrorPageRenderer.Error.INTERNAL_SERVER_ERROR);

		verify(container.getResponseMock()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(writer).write(captor.capture());
		assertThat(captor.getValue()).contains("En feil har oppst&aring;tt.", "<h1>500</h1>", "class=\"incident\"");
	}

	@Test
    public void renderError_withRequestResponseErrorAndAjax_verifyResponse() {
		container.turnOnAjax();

		ErrorPageRenderer.renderError(container.getRequestMock(), container.getResponseMock(), ErrorPageRenderer.Error.INTERNAL_SERVER_ERROR);

		verify(container.getResponseMock()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(writer).write(captor.capture());
		assertThat(captor.getValue()).contains("\"statusCode\":500", "\"incident\":");
	}

	@Test
    public void renderError_withRequestResponseErrorThrowable_verifyResponse() {
		ErrorPageRenderer.renderError(container.getRequestMock(), container.getResponseMock(), ErrorPageRenderer.Error.BAD_REQUEST, "Some error", new Exception());

		verify(container.getResponseMock()).setStatus(HttpServletResponse.SC_BAD_REQUEST);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(writer).write(captor.capture());
		assertThat(captor.getValue()).contains("Ugyldig foresp&oslash;rsel.", "<h1>400</h1>", "class=\"incident\"");
	}

	@Test(dataProvider = "getErrorByCode")
    public void getErrorByCode_verifyExpected(int code, ErrorPageRenderer.Error expected) {
		assertThat(ErrorPageRenderer.Error.getErrorByCode(code)).isSameAs(expected);
	}

	@Test(dataProvider = "getMessage")
    public void getMessage_verifyExpected(ErrorPageRenderer.Error error, String expected) {
		assertThat(error.getMessage()).contains(expected);
	}

	@Test(dataProvider = "getTitle")
    public void getTitle_verifyExpected(ErrorPageRenderer.Error error, String expected) {
		assertThat(error.getTitle()).contains(expected);
	}

	@DataProvider(name = "getErrorByCode")
	public static Object[][] getErrorByCode() {
		return new Object[][] {
				{ 1000, ErrorPageRenderer.Error.BAD_REQUEST },
				{ 500, ErrorPageRenderer.Error.INTERNAL_SERVER_ERROR },
				{ 404, ErrorPageRenderer.Error.NOT_FOUND },
				{ 401, ErrorPageRenderer.Error.UNAUTHORIZED }
		};
	}

	@DataProvider(name = "getMessage")
	public static Object[][] getMessage() {
		return new Object[][] {
				{ ErrorPageRenderer.Error.UNAUTHORIZED, "Du har ikke tilgang til denne siden." },
				{ ErrorPageRenderer.Error.NOT_FOUND, "Siden du prøvde å gå til finnes ikke." },
				{ ErrorPageRenderer.Error.INTERNAL_SERVER_ERROR, "Det oppstod en feil. Vent litt og forsøk igjen. Kontakt support om problemet vedvarer." },
				{ ErrorPageRenderer.Error.BAD_REQUEST, "Ugyldig forespørsel." }
		};
	}

	@DataProvider(name = "getTitle")
	public static Object[][] getTitle() {
		return new Object[][] {
				{ ErrorPageRenderer.Error.UNAUTHORIZED, "Ingen tilgang." },
				{ ErrorPageRenderer.Error.NOT_FOUND, "Siden finnes ikke." },
				{ ErrorPageRenderer.Error.INTERNAL_SERVER_ERROR, "En feil har oppstått." },
				{ ErrorPageRenderer.Error.BAD_REQUEST, "Ugyldig forespørsel." }
		};
	}

}

