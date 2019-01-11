package no.valg.eva.admin.application.map;

import lombok.Builder;
import lombok.Getter;
import no.valg.eva.admin.common.configuration.model.Municipality;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Builder
@Getter
public class AddressSearch {

    public static final int DEFAULT_MAX_RESULTS = 10;
    
    private String streetName;
    private String houseNumber;
    private String houseLetter;
    private String postalCode;
    private String postTown;
    private Municipality municipality;
    @Builder.Default
    private int maxResults = DEFAULT_MAX_RESULTS;

    public boolean valid() {
        return isNotBlank(streetName);
    }

}
