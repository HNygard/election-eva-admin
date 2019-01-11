package no.evote.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.frontend.LocaleController;

/**
 * @deprecated Use LocaleIdConverter
 */
@FacesConverter(value = "localeConverter")
@Deprecated
public class LocaleConverter implements Converter {

	@Inject
	private LocaleController localeController;

	@Override
	public Object getAsObject(final FacesContext context, final UIComponent component, final String value) {
		if (value == null || value.trim().length() == 0) {
			return null;
		}

		setLocaleController(context);
		return localeController.getLocaleMap().get(value);
	}

	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Locale) {
			setLocaleController(context);
			return ((Locale) value).getId();
		} else {
			throw new IllegalArgumentException(
					"object " + value + " is of type " + value.getClass().getName() + "; expected type: no.valg.eva.admin.configuration.domain.model.Locale");
		}
	}

	private void setLocaleController(final FacesContext context) {
		if (localeController == null) {
			localeController = context.getApplication().evaluateExpressionGet(context, "#{localeController}", LocaleController.class);
		}
	}

}
