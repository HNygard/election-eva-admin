package no.valg.eva.admin.common.counting.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CountQualifierTest {

	public static final String COUNT_QUALIFIER_TEST_DATA = "countQualifierTestData";

	@Test(dataProvider = COUNT_QUALIFIER_TEST_DATA)
	public void fromId_translatesToExpectedCountQualifier(String id, CountQualifier expectedCountQualifier) throws Exception {
		CountQualifier qualifier = CountQualifier.fromId(id);
		assertThat(qualifier).isSameAs(expectedCountQualifier);
	}
	
	@DataProvider(name = COUNT_QUALIFIER_TEST_DATA)
	public Object[][] countQualifierTestData() {
		return new Object[][] {
			{ "P", CountQualifier.PROTOCOL },
			{ "F", CountQualifier.PRELIMINARY },
			{ "E", CountQualifier.FINAL }
		};
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void fromId_withInvalidQualifierId_throwsException() throws Exception {
		CountQualifier.fromId("XX");
	}

	@Test(dataProvider = COUNT_QUALIFIER_TEST_DATA )
	public void getId_translatesFromCountQualifier(String expectedId, CountQualifier countQualifier) throws Exception {
		String id = countQualifier.getId();
		assertThat(id).isSameAs(expectedId);
	}

	@Test(dataProvider = COUNT_QUALIFIER_TEST_DATA )
	public void getName_translatesFromCountQualifier(String expectedNameSubstring, CountQualifier countQualifier) throws Exception {
		String name = countQualifier.getName();
		assertThat(name).isEqualTo("@count_qualifier[" + expectedNameSubstring + "].name");
	}
}
