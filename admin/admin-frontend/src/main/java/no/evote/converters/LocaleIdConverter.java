package no.evote.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import no.valg.eva.admin.common.configuration.model.election.LocaleId;
import no.valg.eva.admin.frontend.LocaleController;

@FacesConverter(value = "localeIdConverter")
public class LocaleIdConverter implements Converter {

	@Inject
	private LocaleController localeController;

	@Override
	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		if (value == null || value.trim().length() == 0) {
			return null;
		}

		setLocaleController(context);
		return localeController.getLocaleIdMap().get(value);
	}

	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof LocaleId) {
            setLocaleController(context);
			return ((LocaleId) value).getId();
		} else {
			throw new IllegalArgumentException(
					"object " + value + " is of type " + value.getClass().getName() + "; expected type: LocaleId");
		}
	}

	private void setLocaleController(final FacesContext context) {
		if (localeController == null) {
			localeController = context.getApplication().evaluateExpressionGet(context, "#{localeController}", LocaleController.class);
		}
	}

}
