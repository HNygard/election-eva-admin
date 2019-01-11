package no.valg.eva.admin.frontend.configuration.converters;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import no.valg.eva.admin.common.configuration.model.local.Place;

public class PlaceConverter implements Converter {

	private PlaceConverterSource source;

	public PlaceConverter(PlaceConverterSource source) {
		this.source = source;
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value != null && value.trim().length() > 0) {
			List<? extends Place> places = source.getPlaces();
			for (Place place : places) {
				if (value.equals(place.getId())) {
					return place;
				}
			}
			throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Converter Error", "Not a valid Place."));
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value instanceof String) {
			return (String) value;
		} else if (value instanceof Place) {
			Place model = (Place) value;
			return model.getId();
		}
		return "";
	}
}
