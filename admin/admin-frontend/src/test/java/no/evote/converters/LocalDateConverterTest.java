package no.evote.converters;

import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Locale;

import javax.faces.convert.ConverterException;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LocalDateConverterTest extends BaseConverterTest {

	private static final String TEST_DATE = "08.12.1977";
	private static final String ISO_TEST_DATE = "1977-12-08";
	private static final String INVALID_TEST_DATE = "0800.12.1977";
	private static final String DATE_PATTERN = "dd.MM.yyyy";

	private LocalDateConverter converter;

	@BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
		converter = new LocalDateConverter();
		converter.setMessageProvider(createMock(MessageProvider.class));
		when(converter.getMessageProvider().get("@common.date.invalid_date.summary")).thenReturn("date");
		when(converter.getMessageProvider().get("@common.date.invalid_date.detail")).thenReturn("date detail");
		mockEvaluateExpressionGet("#{msgs['@common.date.date_pattern']}", String.class, DATE_PATTERN);
		mockUIComponentAttribute("pattern", null);
		mockUIComponentAttribute("required", true);
		mockUIComponentAttribute("label", "somelabel");
	}

	@Test
	public void testValidObject() throws ParseException {
		LocalDate actual = (LocalDate) converter.getAsObject(facesContextMock, uiComponentMock, TEST_DATE);
		Assert.assertEquals(actual, DateTimeFormat.forPattern(DATE_PATTERN).withLocale(Locale.ENGLISH).parseLocalDate(TEST_DATE));
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = "date")
	public void testFailingRequired() throws ParseException {
		converter.getAsObject(facesContextMock, uiComponentMock, "qweqwe");
	}

	@Test
	public void testValidFallbackObject() throws ParseException {
		LocalDate actual = (LocalDate) converter.getAsObject(facesContextMock, uiComponentMock, ISO_TEST_DATE);
		Assert.assertEquals(actual, DateTimeFormat.forPattern(DATE_PATTERN).withLocale(Locale.ENGLISH).parseLocalDate(TEST_DATE));
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = "date")
	public void testInvalidObject() {
		converter.getAsObject(facesContextMock, uiComponentMock, INVALID_TEST_DATE);
	}

	@Test
	public void testGetAsString() throws ParseException {
		String actual = converter.getAsString(facesContextMock, uiComponentMock,
				DateTimeFormat.forPattern(DATE_PATTERN).withLocale(Locale.ENGLISH).parseDateTime(TEST_DATE));
		Assert.assertEquals(actual, TEST_DATE);
	}
}
