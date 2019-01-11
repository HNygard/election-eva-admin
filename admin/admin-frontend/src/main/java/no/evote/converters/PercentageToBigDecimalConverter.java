package no.evote.converters;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

@FacesConverter(value = "percentageConverter")
public class PercentageToBigDecimalConverter implements Converter {
	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
	private MessageProvider messageProvider = null;

	protected MessageProvider getMessageProvider() {
		if (messageProvider == null) {
			FacesContext context = FacesContext.getCurrentInstance();
			messageProvider = context.getApplication().evaluateExpressionGet(context, "#{messageProvider}", MessageProvider.class);
		}
		return messageProvider;
	}

	@Override
	public Object getAsObject(final FacesContext fContext, final UIComponent component, final String value) {
		if (value == null || value.length() == 0) {
			return BigDecimal.ZERO;
		}

		
		try {
			return BigDecimal.valueOf(Double.valueOf(value) / 100.0);
		} catch (NumberFormatException e) {
			throw new ConverterException(new FacesMessage(MessageFormat.format(getMessageProvider().get("javax.faces.converter.NumberConverter.NUMBER"), value)
					.substring(5)), e);
		}
		
	}

	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		if (value == null) {
			return "0";
		}

		BigDecimal bdValue = BigDecimal.class.cast(value);
		return bdValue.multiply(HUNDRED).toBigInteger().toString();
	}
}
