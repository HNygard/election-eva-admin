package no.evote.presentation.validation;

import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.validation.StringNotNullEmptyOrBlanksValidator;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@SessionScoped
public class NotNullOrEmptyValidator implements Validator { 

	@Inject
	private MessageProvider messageProvider;

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) {
		String valueToValidate = value != null ? value.toString() : null;

		StringNotNullEmptyOrBlanksValidator validator = new StringNotNullEmptyOrBlanksValidator();
		
		if (!validator.isValid(valueToValidate, null)) {
			FacesMessage msg =	new FacesMessage(messageProvider.get("@validation.stringNotNullEmptyOrBlanks"));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
}
