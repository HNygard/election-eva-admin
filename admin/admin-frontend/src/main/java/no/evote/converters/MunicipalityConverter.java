package no.evote.converters;

import no.valg.eva.admin.util.ServiceLookupUtil;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.configuration.domain.model.Municipality;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "municipalityConverter")
public class MunicipalityConverter implements Converter {

	private final MunicipalityService municipalityService = ServiceLookupUtil.lookupService(MunicipalityService.class);

	@Override
	public Object getAsObject(final FacesContext fContext, final UIComponent component, final String value) {
		if (value == null || value.length() == 0) {
			return null;
		}

        return municipalityService.findByPk(Long.parseLong(value));
    }

    @Override
    public String getAsString(final FacesContext context, final UIComponent component, final Object value) {

        if (value == null) {
            return null;
        }
        if (value instanceof Municipality) {
            final Municipality municipality = (Municipality) value;
            return municipality.getPk().toString();
        } else {
            throw new IllegalArgumentException("object " + value + " is of type " + value.getClass().getName() + "; expected type: " + Municipality.class);
        }
    }

}
