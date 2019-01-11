package no.valg.eva.admin.common.configuration.model.local;

import lombok.Getter;
import lombok.Setter;
import no.valg.eva.admin.common.AreaPath;

import static no.valg.eva.admin.util.StringUtil.isSet;

@Getter
@Setter
public abstract class PollingPlace extends Place {

    private static final long serialVersionUID = 5943044390148920299L;

    private Borough borough;
    private String address;
    private String postalCode;
    private String postTown;
    private String gpsCoordinates;

    protected PollingPlace(AreaPath path, int version) {
        super(path, version);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && isValidAddressFields();
    }

    private boolean isValidAddressFields() {
        return !isAddressFieldsRequired() || isSet(address, postalCode, postTown, gpsCoordinates);
    }

    boolean isAddressFieldsRequired() {
        return true;
    }
}
