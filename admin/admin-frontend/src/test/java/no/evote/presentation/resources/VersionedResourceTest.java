package no.evote.presentation.resources;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.frontend.util.VersionProperties;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Test(groups = TestGroups.RESOURCES)
public class VersionedResourceTest extends BaseFrontendTest {

	@Test
	public void getWrapped_withResource_returnsResource() throws Exception {
		Resource resource = createMock(Resource.class);

		VersionedResource vResource = new VersionedResource(resource);

		assertThat(vResource.getWrapped()).isSameAs(resource);

	}

	@Test
	public void getContentType_withResource_returnsTextHtml() throws Exception {
		Resource resource = createMock(Resource.class);
		when(resource.getContentType()).thenReturn("text/html");

		VersionedResource vResource = new VersionedResource(resource);

		assertThat(vResource.getContentType()).isEqualTo("text/html");
	}

	@Test(dataProvider = "getRequestPath")
	public void getRequestPath_withDataProvider_verifyExected(String requestPath, String expected) throws Exception {
		Resource resource = createMock(Resource.class);
		when(resource.getRequestPath()).thenReturn(requestPath);
		VersionedResource vResource = new VersionedResource(resource);

		String path = vResource.getRequestPath();

		assertThat(path).isEqualTo(expected);
	}

	@DataProvider(name = "getRequestPath")
	public Object[][] getRequestPath() {
		return new Object[][] {
				{ "/my/path", "/my/path?rv=" + new VersionProperties().getVersion() },
				{ "/my/path?test=1", "/my/path?test=1&rv=" + new VersionProperties().getVersion() }
		};
	}
}
