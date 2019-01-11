package no.evote.presentation.resources;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Test(groups = TestGroups.RESOURCES)
public class CustomResourceHandlerTest extends BaseFrontendTest {

	@Test(dataProvider = "createResource")
	public void createResource_withDataProvider_verifyExpected(String resourceName, String libraryName, Class<Resource> cls) throws Exception {
		ResourceHandler wrapped = createMock(ResourceHandler.class);
		Resource resource = createMock(Resource.class);
		when(resource.getRequestPath()).thenReturn(resourceName);
		when(wrapped.createResource(resourceName, libraryName)).thenReturn(resource);
		CustomResourceHandler handler = new CustomResourceHandler(wrapped);

		Resource created = handler.createResource(resourceName, libraryName);

		assertThat(created).isInstanceOf(cls);
	}

	@DataProvider(name = "createResource")
	public Object[][] createResource() {
		return new Object[][] {
				{ "my.html", "html", Resource.class },
				{ "my.css", "css", VersionedResource.class },
				{ "my.js", "javascript", VersionedResource.class },
				{ "/javascript/my.js", "unknown", VersionedResource.class },
		};
	}
}
