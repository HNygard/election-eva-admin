package no.evote.presentation.validation;

import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.validation.LettersOrDigitsValidator;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@SessionScoped
public class OnlyLettersOrDigitsValidator implements Validator {

    private static final String EXTRA_CHARS = " .-'/";

    @Inject
    private MessageProvider messageProvider;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        String valueToValidate = value.toString();

        LettersOrDigitsValidator validator = new LettersOrDigitsValidator();
        validator.initialize(EXTRA_CHARS);

        if (!validator.isValid(valueToValidate, null)) {
            FacesMessage msg = new FacesMessage(messageProvider.get("@validation.lettersOrDigits") + " " + EXTRA_CHARS);
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
}
