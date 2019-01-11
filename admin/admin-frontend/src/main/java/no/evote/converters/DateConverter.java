package no.evote.converters;

import java.text.MessageFormat;

import javax.faces.component.UIComponent;

import org.joda.time.format.DateTimeFormatter;

/**
 * Felles funksjonalitet for Converter-klasser som konverterer datoer
 */
public abstract class DateConverter extends AbstractDateOrTimeConverter {

	public DateConverter(final String patternVar, final String fallbackPattern) {
		super(patternVar, fallbackPattern);
	}

	@Override
	protected String generateSummaryErrorMessage(final UIComponent component, final String value, final DateTimeFormatter dateFormat) {
		return getMessageProvider().get("@common.date.invalid_date.summary");
	}

	@Override
	protected String generateDetailErrorMessage(final UIComponent component, final String value, final DateTimeFormatter dateFormat) {
		String detail = getMessageProvider().get("@common.date.invalid_date.detail");
		return MessageFormat.format(detail, value);
	}
}
