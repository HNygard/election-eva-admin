package no.valg.eva.admin.configuration.repository;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class OpeningHoursRepository extends BaseRepository {
    public OpeningHoursRepository() {
    }

    public OpeningHoursRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public List<OpeningHours> findOpeningHoursForPollingPlace(Long pollingPlacePk) {
        TypedQuery<OpeningHours> query = getEm().createNamedQuery("OpeningHours.findOpeningHoursForPollingPlace", OpeningHours.class);
        query.setParameter("pollingPlacePk", pollingPlacePk);
        return query.getResultList();
    }

    public OpeningHours create(UserData userData, OpeningHours openingHours) {
        return super.createEntity(userData, openingHours);
    }
}
