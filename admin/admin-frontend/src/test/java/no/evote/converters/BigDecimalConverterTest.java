package no.evote.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class BigDecimalConverterTest extends BaseConverterTest {

	private BigDecimalConverter converter;

	@BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
		converter = new BigDecimalConverter();
	}

	@Test
	public void getAsObject_withNull_returnsNull() throws Exception {
		Object result = converter.getAsObject(facesContextMock, uiComponentMock, null);

		assertThat(result).isNull();
	}

	@Test
	public void getAsObject_withLong_returnsValue() throws Exception {
		Object result = converter.getAsObject(facesContextMock, uiComponentMock, "10");

		assertThat(result).isEqualTo(BigDecimal.valueOf(10L));
	}

	@Test
	public void getAsObject_withDouble_returnsValue() throws Exception {
		Object result = converter.getAsObject(facesContextMock, uiComponentMock, "10.73");

		assertThat(result).isEqualTo(BigDecimal.valueOf(10.73));
	}

}

