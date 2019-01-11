package no.evote.presentation.exceptions;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;
import no.valg.eva.admin.test.TestGroups;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.RESOURCES)
public class ResourceNotFoundServletTest extends BaseFrontendTest {

	private ResourceNotFoundServlet resourceNotFoundServlet;

	@BeforeMethod
	public void setup() throws Exception {
		resourceNotFoundServlet = initializeMocks(ResourceNotFoundServlet.class);
	}

	@Test
	public void goGet() throws Exception {
		ServletContainer container = getServletContainer();
		resourceNotFoundServlet.doGet(container.getRequestMock(), container.getResponseMock());

		Mockito.verify(container.getRequestMock()).getAttribute("javax.servlet.error.message");
	}
}
