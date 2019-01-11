package no.valg.eva.admin.frontend.common;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;


public class PageTitleMetaModelTest {

	@Test
	public void testGetLabel() throws Exception {
		String labelFixture = "foo";
		PageTitleMetaModel model = new PageTitleMetaModel(labelFixture, null);
		assertEquals(labelFixture, model.getLabel());
		assertEquals(null, model.getValue());
		assertFalse(model.isLink());
	}

	@Test
	public void testGetValue() throws Exception {
		String valueFixture = "bar";
		PageTitleMetaModel model = new PageTitleMetaModel(null, valueFixture);
		assertEquals(valueFixture, model.getValue());
		assertEquals(null, model.getLabel());
		assertFalse(model.isLink());
	}

	@Test
	public void testGetValueAndLabel() throws Exception {
		String labelFixture = "foo";
		String valueFixture = "bar";
		PageTitleMetaModel model = new PageTitleMetaModel(labelFixture, valueFixture);
		assertEquals(valueFixture, model.getValue());
		assertEquals(labelFixture, model.getLabel());
		assertFalse(model.isLink());
	}
	
	@Test
	public void testGetStyleClassIsNull() throws Exception {
		
		PageTitleMetaModel model = new PageTitleMetaModel("foo", "bar");
		
		assertTrue(!model.isStyleClassSet());
		assertFalse(model.isLink());
	}

	@Test
	public void testGetStyleClassIsSet() throws Exception {
		
		PageTitleMetaModel model = new PageTitleMetaModel("foo", "bar", "my-foo");
		
		assertTrue(model.isStyleClassSet());
		assertFalse(model.isLink());
	}
}
