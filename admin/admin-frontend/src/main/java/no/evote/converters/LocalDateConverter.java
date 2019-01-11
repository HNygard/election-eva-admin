package no.evote.converters;

import java.util.Locale;

import javax.faces.convert.FacesConverter;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@FacesConverter(value = "localDateConverter")
public class LocalDateConverter extends DateConverter {

	public LocalDateConverter() {  // JSF krever public constructor
		super("@common.date.date_pattern", "yyyy-MM-dd");
	}
	
	@Override
	protected Object parseValue(String value, Locale locale, DateTimeFormatter dateFormat) {
		if (fallbackPattern == null) {
			return dateFormat.parseLocalDate(value);
		} else {
			return parseWithFallback(locale, dateFormat, value);
		}
	}

	private LocalDate parseWithFallback(final Locale locale, final DateTimeFormatter dateFormat, final String value) {
		try {
			return dateFormat.parseLocalDate(value);
		} catch (IllegalArgumentException e) { 
			DateTimeFormatter fallbackDateFormat = DateTimeFormat.forPattern(fallbackPattern).withLocale(locale);
			return fallbackDateFormat.parseLocalDate(value);
		}
	}
}
