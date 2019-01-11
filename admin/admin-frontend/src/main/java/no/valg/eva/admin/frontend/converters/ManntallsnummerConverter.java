package no.valg.eva.admin.frontend.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import no.valg.eva.admin.common.configuration.model.Manntallsnummer;

@FacesConverter(value = "manntallsnummerConverter")
public class ManntallsnummerConverter implements Converter {

	@Override
	public Object getAsObject(final FacesContext fContext, final UIComponent component, final String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {

		if (value == null) {
			return null;
		}
		if (value instanceof Manntallsnummer) {
			Manntallsnummer manntallsnummer = (Manntallsnummer) value;
			return manntallsnummer.getKortManntallsnummerMedZeroPadding() + " " + manntallsnummer.getSluttsifre();
		} else {
			throw new IllegalArgumentException("Objektet " + value + " er av type " + value.getClass().getName() + ". Forventet type: " + Manntallsnummer.class);
		}
	}

}
