package no.valg.eva.admin.configuration.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PollingDistrictAreaId {

    private String countryId;

    private String countyId;

    private String municipalityId;

    private String boroughId;

    private String pollingDistrictId;
}
