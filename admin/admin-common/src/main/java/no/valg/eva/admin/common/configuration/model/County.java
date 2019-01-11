package no.valg.eva.admin.common.configuration.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class County {
    private String id;
    private String name;
}
