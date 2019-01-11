package no.valg.eva.admin.application.map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static java.util.Collections.emptyList;

@Builder
@Getter
@ToString
public class LocationSearchResult {
    @Builder.Default
    private List<Location> locations = emptyList();
    private int numberOfResults;
    private boolean moreResults;

}
