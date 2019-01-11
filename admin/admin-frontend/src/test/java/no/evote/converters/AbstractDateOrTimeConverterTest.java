package no.evote.converters;

import static org.assertj.core.api.Assertions.assertThat;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AbstractDateOrTimeConverterTest extends BaseConverterTest {

	private static final String DATE_STR = "12.12.2014 12:00";
	private static final String DATE_PATTERN = "dd.MM.yyyy";
	private static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm";
	private AbstractDateOrTimeConverter converter;

	@BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
		MessageProvider messageProviderMock = createMock(MessageProvider.class);
		mockUIComponentAttribute("pattern", null);
		mockEvaluateExpressionGet("#{msgs['@common.date.date_pattern']}", String.class, DATE_PATTERN);
		mockEvaluateExpressionGet("#{msgs['@common.date.date_time_pattern']}", String.class, DATE_TIME_PATTERN);
		converter = getDateTimeConverter();
		converter.setMessageProvider(messageProviderMock);
	}

	@Test
	public void getAsObject_withNull_returnsNull() throws Exception {
		Object result = converter.getAsObject(facesContextMock, uiComponentMock, null);

		assertThat(result).isNull();
	}

	@Test(expectedExceptions = ConverterException.class)
	public void getAsObject_withInvalidDateStr_throwsConverterException() throws Exception {
		converter.getAsObject(facesContextMock, uiComponentMock, "dsgsgadfgf");
	}

	@Test
	public void getAsObject_withValidDateStr_returnsDate() throws Exception {
		mockUIComponentAttribute("pattern", DATE_TIME_PATTERN);
		DateTime result = (DateTime) converter.getAsObject(facesContextMock, uiComponentMock, DATE_STR);

		assertThat(DateTimeFormat.forPattern(DATE_TIME_PATTERN).print(result)).isEqualTo(DATE_STR);
	}

	@Test
	public void getAsObject_dateTimewithFallback_returnsDate() throws Exception {
		DateTime result = (DateTime) converter.getAsObject(facesContextMock, uiComponentMock, "2014-12-12 00:00");

		assertThat(DateTimeFormat.forPattern(DATE_TIME_PATTERN).print(result)).isEqualTo("12.12.2014 00:00");
	}

	@Test
	public void getAsObject_localDateWithFallback_returnsDate() throws Exception {
		converter = getLocalDateConverter();
		LocalDate result = (LocalDate) converter.getAsObject(facesContextMock, uiComponentMock, "2014-12-12");

		assertThat(DateTimeFormat.forPattern(DATE_PATTERN).print(result)).isEqualTo("12.12.2014");
	}

	@Test
	public void getAsString_withNull_returnsEmptyString() throws Exception {
		assertThat(converter.getAsString(facesContextMock, uiComponentMock, null)).isEqualTo("");
	}

	@Test
	public void getAsString_withString_returnsString() throws Exception {
		assertThat(converter.getAsString(facesContextMock, uiComponentMock, "s")).isEqualTo("s");
	}

	@Test
	public void getAsString_withDate_returnsString() throws Exception {
		DateTime in = DateTimeFormat.forPattern(DATE_TIME_PATTERN).parseDateTime(DATE_STR);
		String result = converter.getAsString(facesContextMock, uiComponentMock, in);

		assertThat(result).isEqualTo(DATE_STR);
	}

	@Test
	public void getMessageProvider_withNoMessageProvider_returnsMock() throws Exception {
		MessageProvider ms = converter.getMessageProvider();
		converter.setMessageProvider(null);
		mockEvaluateExpressionGet("#{messageProvider}", MessageProvider.class, ms);

		MessageProvider result = converter.getMessageProvider();

		assertThat(result).isSameAs(ms);
	}
	
	private AbstractDateOrTimeConverter getDateTimeConverter() {
		return new DateTimeConverter() {
			@Override
			protected String generateSummaryErrorMessage(UIComponent component, String value, DateTimeFormatter dateFormat) {
				return "generateSummaryErrorMessage";
			}

			@Override
			protected String generateDetailErrorMessage(UIComponent component, String value, DateTimeFormatter dateFormat) {
				return "generateDetailErrorMessage";
			}

			@Override
			FacesContext getFacesContext() {
				return facesContextMock;
			}
		};
	}

	private AbstractDateOrTimeConverter getLocalDateConverter() {
		return new LocalDateConverter() {
			@Override
			protected String generateSummaryErrorMessage(UIComponent component, String value, DateTimeFormatter dateFormat) {
				return "generateSummaryErrorMessage";
			}

			@Override
			protected String generateDetailErrorMessage(UIComponent component, String value, DateTimeFormatter dateFormat) {
				return "generateDetailErrorMessage";
			}

			@Override
			FacesContext getFacesContext() {
				return facesContextMock;
			}
		};
	}

}
