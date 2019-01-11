package no.evote.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import no.valg.eva.admin.frontend.util.FacesUtil;
import no.evote.service.configuration.ProposerService;
import no.valg.eva.admin.util.ServiceLookupUtil;
import no.valg.eva.admin.configuration.domain.model.ProposerRole;

@FacesConverter(value = "proposerRoleConverter")
public class ProposerRoleConverter implements Converter {

	private final ProposerService proposerService = ServiceLookupUtil.lookupService(ProposerService.class);

	@Override
	public Object getAsObject(final FacesContext fContext, final UIComponent component, final String value) {
		if (value == null || value.length() == 0) {
			return null;
		}

		return proposerService.findProposerRoleByPk(FacesUtil.getUserData(), Long.parseLong(value));
	}

	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {

		if (value == null) {
			return "";
		}
		if (value instanceof ProposerRole) {
			final ProposerRole proposerRole = (ProposerRole) value;
			return proposerRole.getPk().toString();
		} else {
			return "";
		}
	}

}
