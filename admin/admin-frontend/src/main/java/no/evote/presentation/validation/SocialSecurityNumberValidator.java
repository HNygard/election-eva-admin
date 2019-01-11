package no.evote.presentation.validation;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.validation.FoedselsNummerValidator;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@SessionScoped
public class SocialSecurityNumberValidator implements Serializable, Validator {
	@Inject
	private MessageProvider messageProvider;

	@Override
	public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) {
		if (o != null && !FoedselsNummerValidator.isFoedselsNummerValid(o.toString())) {
			FacesMessage msg =
					new FacesMessage(messageProvider.get("@validation.invalid.id"));
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}

}
