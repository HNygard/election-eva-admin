package no.evote.presentation.validation;

import static no.evote.constants.EvoteConstants.VALID_GPS_PATTERN;

import java.io.Serializable;
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
public class GpsCoordinatesValidator implements Serializable, Validator {

    private static final Pattern GPS_PATTERN = Pattern.compile(VALID_GPS_PATTERN);

    @Inject
    private MessageProvider messageProvider;

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o != null && !isValidGpsCoordinate(o.toString())) {
            FacesMessage msg = new FacesMessage(messageProvider.get("@validation.gps"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

    public static final boolean isValidGpsCoordinate(String value) {
        return GPS_PATTERN.matcher(value).matches();
    }

}
