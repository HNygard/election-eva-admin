package no.evote.util;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.test.BaseTakeTimeTest;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class EvotePropertiesTest extends BaseTakeTimeTest {

	@BeforeTest
	public void setUp() {
		System.setProperty("EVOTE_PROPERTIES", "../admin-backend/src/test/resources/evote.properties");
	}

	@Test
	public void getProperty_forAnExistingFile_readsContent() {
		EvotePropertiesTestUtil.reinitializeProperties();
		String deployUrl = EvoteProperties.getProperty(EvoteProperties.DEPLOY_URL);

		assertThat(deployUrl).isNotNull();
	}
	
	@Test
	public void getBooleanProperty_forAnUndefinedProperty_returnsDefaultValue() {
		EvotePropertiesTestUtil.reinitializeProperties();
		boolean actualValue = EvoteProperties.getBooleanProperty("an.undefined.property", false);

		assertThat(actualValue).isFalse();
	}

	@Test
	public void getBooleanProperty_forADefinedProperty_returnsTheValue() {
		EvotePropertiesTestUtil.reinitializeProperties();
		EvoteProperties.setProperty(EvoteProperties.TEST_CAN_CHANGE_TIME, "true");
		boolean actualValue = EvoteProperties.getBooleanProperty(EvoteProperties.TEST_CAN_CHANGE_TIME, false);

		assertThat(actualValue).isTrue();
	}

}
