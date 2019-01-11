package no.valg.eva.admin.common;

import java.io.Serializable;
import java.util.List;

/**
 * Generic response object for service requests.
 */
public abstract class ServiceResponse implements Serializable {

    private final List<String> validationErrors;

    public ServiceResponse(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

}
