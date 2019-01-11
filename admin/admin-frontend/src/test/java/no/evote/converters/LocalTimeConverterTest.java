package no.evote.converters;

import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Locale;

import javax.faces.convert.ConverterException;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LocalTimeConverterTest extends BaseConverterTest {
	private static final String TEST_TIME = "13.17";
	private static final String INVALID_TEST_TIME = "13-37";
	private static final String TIME_PATTERN = "HH.mm";

	private LocalTimeConverter converter;

	@BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
		converter = new LocalTimeConverter();
		converter.setMessageProvider(createMock(MessageProvider.class));
		when(converter.getMessageProvider().get("javax.faces.converter.DateTimeConverter.TIME")).thenReturn("time");
		when(converter.getMessageProvider().get("javax.faces.converter.DateTimeConverter.TIME_detail")).thenReturn("time detail");
		mockEvaluateExpressionGet("#{msgs['@common.date.time_pattern']}", String.class, TIME_PATTERN);
		mockUIComponentAttribute("pattern", null);
		mockUIComponentAttribute("required", true);
		mockUIComponentAttribute("label", "somelabel");
	}

	@Test
	public void testValidObject() throws Exception {
		LocalTime actual = (LocalTime) converter.getAsObject(facesContextMock, uiComponentMock, TEST_TIME);

		Assert.assertEquals(actual, DateTimeFormat.forPattern(TIME_PATTERN).withLocale(Locale.ENGLISH).parseLocalTime(TEST_TIME));
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = "time")
	public void testInvalidObject() throws Exception {
		converter.getAsObject(facesContextMock, uiComponentMock, INVALID_TEST_TIME);
	}

	@Test
	public void testGetAsString() throws ParseException {
		String actual = converter.getAsString(facesContextMock, uiComponentMock,
				DateTimeFormat.forPattern(TIME_PATTERN).withLocale(Locale.ENGLISH).parseDateTime(TEST_TIME));
		Assert.assertEquals(actual, TEST_TIME);
	}
}
