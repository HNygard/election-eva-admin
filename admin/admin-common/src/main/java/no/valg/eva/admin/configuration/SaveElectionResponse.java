package no.valg.eva.admin.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.valg.eva.admin.common.ServiceResponse;
import no.valg.eva.admin.common.VersionedObject;

public final class SaveElectionResponse extends ServiceResponse {

    private static final String ID_NOT_UNIQUE = "ID_NOT_UNIQUE";
    private VersionedObject versionedObject;

    private SaveElectionResponse(List<String> validationErrors) {
        super(validationErrors);
    }
    
    public static SaveElectionResponse ok() {
        return new SaveElectionResponse(Collections.EMPTY_LIST);
    }

    public static SaveElectionResponse withIdNotUniqueError() {
        List<String> validationErrors = new ArrayList<>();
        validationErrors.add(ID_NOT_UNIQUE);
        return new SaveElectionResponse(validationErrors);
    }
    
    public boolean idNotUniqueError() {
        return getValidationErrors().contains(ID_NOT_UNIQUE);
    }

    public SaveElectionResponse setVersionedObject(VersionedObject versionedObject) {
        this.versionedObject = versionedObject;
        return this;
    }

    public VersionedObject getVersionedObject() {
        return versionedObject;
    }
}
