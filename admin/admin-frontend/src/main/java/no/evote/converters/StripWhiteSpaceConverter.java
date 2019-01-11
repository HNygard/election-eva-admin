package no.evote.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "whitespaceConverter")
public class StripWhiteSpaceConverter implements Converter {

	@Override
	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		if (value == null || value.length() == 0) {
			return null;
		}

		return value.replaceAll("\\s+", "");
	}

	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		return (String) getAsObject(context, component, (String) value);
	}
}
