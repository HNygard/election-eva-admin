package no.evote.converters;

import java.util.Locale;

import javax.faces.convert.FacesConverter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@FacesConverter(value = "dateTimeConverter")
public class DateTimeConverter extends DateConverter {

	public DateTimeConverter() {
		super("@common.date.date_time_pattern", "yyyy-MM-dd HH:mm");
	}

	@Override
	protected Object parseValue(String value, Locale locale, DateTimeFormatter dateFormat) {
		if (fallbackPattern == null) {
			return dateFormat.parseDateTime(value);
		} else {
			return parseWithFallback(locale, dateFormat, value);
		}
	}

	private DateTime parseWithFallback(final Locale locale, final DateTimeFormatter dateFormat, final String value) {
		try {
			return dateFormat.parseDateTime(value);
		} catch (IllegalArgumentException e) { 
			DateTimeFormatter fallbackDateFormat = DateTimeFormat.forPattern(fallbackPattern).withLocale(locale);
			return fallbackDateFormat.parseDateTime(value);
		}
	}
}
