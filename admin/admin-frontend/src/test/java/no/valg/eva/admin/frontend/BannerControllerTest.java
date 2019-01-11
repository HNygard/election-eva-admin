package no.valg.eva.admin.frontend;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class BannerControllerTest extends MockUtilsTestCase {

	@Test
	public void testBannerIsNotEnabled() throws Exception {

		BannerController controller = initializeMocks(BannerController.class);

		when(getInjectMock(BannerProperties.class).isEnabled()).thenReturn(false);

		controller.initialize();

		assertFalse(controller.isEnabled());
		assertEquals(controller.getBanner(), null);
		assertEquals(controller.getBackgroundColor(), null);
		assertEquals(controller.getTextColor(), null);
	}

	@Test
	public void testBannerIsEnabled() throws Exception {
		BannerController controller = initializeMocks(BannerController.class);

		when(getInjectMock(BannerProperties.class).isEnabled()).thenReturn(true);
		when(getInjectMock(BannerProperties.class).getBannerText()).thenReturn("TEST");
		when(getInjectMock(BannerProperties.class).getBannerBackgroundColor()).thenReturn("FFF");
		when(getInjectMock(BannerProperties.class).getBannerTextColor()).thenReturn("000");

		controller.initialize();

		assertTrue(controller.isEnabled());
		assertEquals(controller.getBanner(), "TEST");
		assertEquals(controller.getBackgroundColor(), "#FFF");
		assertEquals(controller.getTextColor(), "#000");
	}
}
