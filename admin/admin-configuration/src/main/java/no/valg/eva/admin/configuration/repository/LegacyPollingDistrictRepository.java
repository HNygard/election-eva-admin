package no.valg.eva.admin.configuration.repository;

import lombok.NoArgsConstructor;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.LegacyPollingDistrict;

import javax.persistence.EntityManager;
import java.util.List;

@NoArgsConstructor
public class LegacyPollingDistrictRepository extends BaseRepository {

    LegacyPollingDistrictRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public LegacyPollingDistrict create(UserData userData, LegacyPollingDistrict legacyPollingDistrict) {
        return createEntity(userData, legacyPollingDistrict);
    }

    public void updateLegacyPollingDistricts(UserData userData, List<LegacyPollingDistrict> legacyPollingDistricts) {
        updateEntities(userData, legacyPollingDistricts);
    }
}
