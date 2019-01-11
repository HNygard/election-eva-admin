package no.valg.eva.admin.common.configuration.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ElectionEvent implements Serializable {

    private long pk;
}
