package no.valg.eva.admin.configuration.domain.model;

import lombok.Getter;

public enum VoterStatus {
    
    RESIDENT(1),
    EMIGRATED(3),
    DECEASED(5),
    ANNULLED(8);

    @Getter
    private final int statusCode;

    VoterStatus(int statusCode){
        this.statusCode = statusCode;
    }
}
