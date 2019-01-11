package no.valg.eva.admin.integration.kartverket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KartverketAddress {

    private String adressenavn;
    private String husnr;
    private String bokstav;
    private String postnr;
    private String poststed;
    private String kommunenr;
    private String kommunenavn;
    private String nord;
    private String aust;
}
