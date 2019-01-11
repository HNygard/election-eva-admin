package no.valg.eva.admin.application.map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static java.util.Collections.emptyList;

@Builder
@Getter
@ToString
public class AddressSearchResult {

    @Builder.Default
    private List<Address> addresses = emptyList();
}
