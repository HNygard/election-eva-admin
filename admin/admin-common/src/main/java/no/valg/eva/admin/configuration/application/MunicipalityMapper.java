package no.valg.eva.admin.configuration.application;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import no.valg.eva.admin.common.configuration.model.Municipality;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MunicipalityMapper {

    public static Municipality toDto(no.valg.eva.admin.configuration.domain.model.Municipality domainModel) {
        return Municipality.builder()
                .pk(domainModel.getPk())
                .build();
    }
    
}
