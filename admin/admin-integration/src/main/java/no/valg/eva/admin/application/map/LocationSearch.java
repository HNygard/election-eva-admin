package no.valg.eva.admin.application.map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.valg.eva.admin.common.configuration.model.County;
import no.valg.eva.admin.common.configuration.model.Municipality;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Builder
@Getter
public class LocationSearch {

    private static final int DEFAULT_MAX_RESULTS_PER_PAGE = 50;
    public static final int FIRST_PAGE = 0;

    private String locationName;
    @Builder.Default
    private List<Municipality> municipalities = emptyList();
    @Builder.Default
    private List<County> counties = emptyList();
    @Builder.Default
    private int maxResultsPerPage = DEFAULT_MAX_RESULTS_PER_PAGE;
    @Builder.Default
    @Setter
    private int pageNumber = FIRST_PAGE;

    public boolean valid() {
        return isNotBlank(locationName);
    }

}
