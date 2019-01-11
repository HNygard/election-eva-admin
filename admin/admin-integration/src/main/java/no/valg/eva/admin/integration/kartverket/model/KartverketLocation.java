package no.valg.eva.admin.integration.kartverket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KartverketLocation {
    private String navnetype;
    private String kommunenavn;
    private String fylkesnavn;
    private String stedsnavn;
    private String nord;
    private String aust;
    private String epsgKode;
}
