package no.evote.converters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StripWhiteSpaceConverterTest extends BaseConverterTest {

	private StripWhiteSpaceConverter converter;

	@BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
		converter = new StripWhiteSpaceConverter();
	}

	@Test
	public void getAsObject_withNull_returnsNull() throws Exception {
		Object result = converter.getAsObject(facesContextMock, uiComponentMock, null);

		assertThat(result).isNull();
	}

	@Test
	public void getAsObject_withWhitespace_returnsValue() throws Exception {
		Object result = converter.getAsObject(facesContextMock, uiComponentMock, "10 10 10 10");

		assertThat(result).isEqualTo("10101010");
	}

	@Test
	public void getAsString_withWhitespace_returnsValue() throws Exception {
		Object result = converter.getAsString(facesContextMock, uiComponentMock, "10 10 10 10");

		assertThat(result).isEqualTo("10101010");
	}
}
