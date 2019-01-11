package no.evote.converters;

import no.evote.util.Wrapper;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CandidateRef;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "newCandidateConverter")
public class NewCandidateConverter implements Converter {
	@Override
	public Object getAsObject(final FacesContext fContext, final UIComponent component, final String value) {
		if (value == null || value.length() == 0) {
			return null;
		}
		try {
			return new Candidate(new CandidateRef(Long.parseLong(value)));
		} catch (NumberFormatException e) {
			String message = fContext.getApplication().evaluateExpressionGet(fContext, "#{msgs['@count.error.validation.writein_invalid']}", String.class);
			throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getAsString(final FacesContext context, final UIComponent component, final Object value) {
		if (value == null || (value instanceof Wrapper) && ((Wrapper<Candidate>) value).getValue() == null) {
			return null;
		}
		if (value instanceof Wrapper) {
			return String.format("%d", ((Wrapper<Candidate>) value).getValue().getCandidateRef().getPk());
		} else {
			return String.format("%d", ((Candidate) value).getCandidateRef().getPk());
		}
	}
}
