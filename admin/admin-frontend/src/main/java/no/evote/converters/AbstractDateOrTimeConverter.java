package no.evote.converters;

import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Abstract converter for converting to/from Date and/or Time objects. Pattern and error messages should be defined in subclasses.
 * 
 * Earlier, when we used the java.util.Date library, this class was capable of being the parent converter for all date/time converters.
 * With the introduction of JodaTime, this no longer works the same way.
 */
abstract class AbstractDateOrTimeConverter implements Converter {
	private String patternVar = null;
	protected String fallbackPattern = null;
	private MessageProvider messageProvider = null;

	AbstractDateOrTimeConverter(final String patternVar) {
		this.patternVar = patternVar;
	}

	AbstractDateOrTimeConverter(final String patternVar, final String fallbackPattern) {
		this.patternVar = patternVar;
		this.fallbackPattern = fallbackPattern;
	}

	public void setMessageProvider(final MessageProvider messageProvider) {
		this.messageProvider = messageProvider;
	}

	protected MessageProvider getMessageProvider() {
		if (messageProvider == null) {
			FacesContext context = getFacesContext();
			messageProvider = context.getApplication().evaluateExpressionGet(context, "#{messageProvider}", MessageProvider.class);
		}
		return messageProvider;
	}

	@Override
	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		if (value != null && value.trim().length() > 0 && !"__.__.____".equals(value)) {
			boolean required = "true".equals(component.getAttributes().get("required"));

			// Find the correct date pattern
			Application app = context.getApplication();

			String pattern = (String) component.getAttributes().get("pattern"); // Pattern set as attribute on component
			if (pattern == null || pattern.length() == 0) {
				pattern = app.evaluateExpressionGet(context, "#{msgs['" + patternVar + "']}", String.class); // Get pattern from faces parameter
			}

			Locale locale = context.getViewRoot().getLocale();
			DateTimeFormatter dateFormat = DateTimeFormat.forPattern(pattern).withLocale(locale);

			// Try to parse the date, and issue a converter error if it is invalid
			try {
				return parseValue(value, locale, dateFormat);
			} catch (IllegalArgumentException e) {
				if (required || !"".equals(value)) {
					throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, generateSummaryErrorMessage(component, value, dateFormat),
							generateDetailErrorMessage(component, value, dateFormat)), e);
				}
			}
		}
		return null;
	}

	protected abstract Object parseValue(String value, Locale locale, DateTimeFormatter dateFormat);

	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		if (value == null) {
			return "";
		} else if (value instanceof String) {
			return (String) value;
		} else {
			// When converting the date to a string, there should be no need to set the formatter to non-lenient
			Application app = context.getApplication();

			String pattern = (String) component.getAttributes().get("pattern"); // Pattern set as attribute on component
			if (pattern == null || pattern.length() == 0) {
				pattern = app.evaluateExpressionGet(context, "#{msgs['" + patternVar + "']}", String.class); // Get pattern from faces parameter
			}

			Locale locale = context.getViewRoot().getLocale();
			DateTimeFormatter dateFormat = DateTimeFormat.forPattern(pattern).withLocale(locale);
			if (value instanceof ReadableInstant) {
				return dateFormat.print((ReadableInstant) value);
			} else {
				return dateFormat.print((ReadablePartial) value);
			}
		}
	}

	public void setPatternVar(final String patternVar) {
		this.patternVar = patternVar;
	}

	public String getPatternVar() {
		return patternVar;
	}

	protected abstract String generateSummaryErrorMessage(final UIComponent component, final String value, DateTimeFormatter dateFormat);

	protected abstract String generateDetailErrorMessage(final UIComponent component, final String value, DateTimeFormatter dateFormat);

	/** For testing pursposes */
	FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}
}
