package no.evote.converters;

import java.text.MessageFormat;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.convert.FacesConverter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@FacesConverter(value = "localTimeConverter")
public class LocalTimeConverter extends AbstractDateOrTimeConverter {

	public LocalTimeConverter() {
		super("@common.date.time_pattern");
	}

	@Override
	protected String generateSummaryErrorMessage(final UIComponent component, final String value, final DateTimeFormatter dateFormat) {
		String summary = getMessageProvider().get("javax.faces.converter.DateTimeConverter.TIME");
		return MessageFormat.format(summary, value, "", component.getAttributes().get("label"));
	}

	@Override
	protected String generateDetailErrorMessage(final UIComponent component, final String value, final DateTimeFormatter dateFormat) {
		String detail = getMessageProvider().get("javax.faces.converter.DateTimeConverter.TIME_detail");
		return MessageFormat.format(detail, value, dateFormat.print(DateTime.now()), component.getAttributes().get("label"));
	}

	@Override
	protected Object parseValue(String value, Locale locale, DateTimeFormatter dateFormat) {
		try {
			return dateFormat.parseLocalTime(value);
		} catch (IllegalArgumentException e) {
			DateTimeFormatter fallbackDateFormat = DateTimeFormat.forPattern(fallbackPattern).withLocale(locale);
			return fallbackDateFormat.parseLocalTime(value);
		}
	}
}
