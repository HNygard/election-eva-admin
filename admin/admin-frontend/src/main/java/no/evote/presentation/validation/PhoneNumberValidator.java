package no.evote.presentation.validation;

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

/**
 * Validates email format, but accepts empty value
 */
@Named
@SessionScoped
public class PhoneNumberValidator implements Validator {
    private static final Pattern VALID_PHONE_NO_PATTERN = Pattern.compile("\\+?([0-9]{3,14})?");
    @Inject
    private MessageProvider messageProvider;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        String valueToValidate = value.toString();
        if (!VALID_PHONE_NO_PATTERN.matcher(valueToValidate).matches()) {
            FacesMessage msg =
                    new FacesMessage(messageProvider.get("@validation.tlf.regex"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
}
