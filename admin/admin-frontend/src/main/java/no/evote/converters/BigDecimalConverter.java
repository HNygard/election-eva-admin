package no.evote.converters;

import java.math.BigDecimal;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import javax.faces.convert.NumberConverter;

@FacesConverter(value = "bigDecimalConverter")
public class BigDecimalConverter extends NumberConverter {

	@Override
	public Object getAsObject(final FacesContext context, final UIComponent component, final String string) {
		Object value = super.getAsObject(context, component, string);
		if (value instanceof Long) {
			return BigDecimal.valueOf((Long) value);
		}
		if (value instanceof Double) {
			return BigDecimal.valueOf((Double) value);
		}
		return value;
	}
}
