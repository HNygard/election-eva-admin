package no.valg.eva.admin.common.configuration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Municipality implements Serializable {

    private static final long serialVersionUID = 5056363215113724152L;

    @EqualsAndHashCode.Include
    private long pk;
    
    private String id;
    
    private String name;

    private List<OpeningHours> openingHours;
}
