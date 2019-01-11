package no.evote.presentation.validation;

import no.evote.validation.LettersValidator;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Named
@SessionScoped
public class OnlyLettersAndSeparatorsValidator implements Validator {

    private static final String EXTRACHARS = " .-'/";
    
    @Inject
    private MessageProvider messageProvider;

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) {
        String valueToValidate = value.toString();

        LettersValidator validator = new LettersValidator();
        validator.initialize(getExtrachars(component));

        if (!validator.isValid(valueToValidate, null)) {
            FacesMessage msg = new FacesMessage(messageProvider.get("@validation.letters") + " " + getExtrachars(component));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
    
    private String getExtrachars(UIComponent component) {
        String extracharsAttribute = (String) component.getAttributes().get("extrachars");
        if (isEmpty(extracharsAttribute)) {
            return EXTRACHARS;
        } else {
            return EXTRACHARS + extracharsAttribute;
        }
    }
    
}
