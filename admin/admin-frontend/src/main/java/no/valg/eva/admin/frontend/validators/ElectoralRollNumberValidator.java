package no.valg.eva.admin.frontend.validators;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@SessionScoped
public class ElectoralRollNumberValidator implements Serializable, Validator {

    @Inject
    private MessageProvider messageProvider;

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o != null && !isValidElectoralRollNumber(o)) {
            generateErrorMessage();
        }
    }

    private boolean isValidElectoralRollNumber(Object o) {
        try {
            new Manntallsnummer(o.toString());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private void generateErrorMessage() {
        FacesMessage msg = new FacesMessage(messageProvider.get("@validation.manntallsnummer.ugyldig"));
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        throw new ValidatorException(msg);
    }

}
