package no.evote.presentation.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@SessionScoped
public class PostalCodeValidator implements Validator {

    @Inject
    private MessageProvider messageProvider;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        String valueToValidate = value.toString();
        Pattern p = Pattern.compile("([0-9]{4})?");
        Matcher m = p.matcher(valueToValidate);
        if (!m.matches()) {
            FacesMessage msg = new FacesMessage(messageProvider.get("@validation.postalCode.regex"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }

    }
}
