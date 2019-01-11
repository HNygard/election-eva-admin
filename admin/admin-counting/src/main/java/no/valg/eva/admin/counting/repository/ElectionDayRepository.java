package no.valg.eva.admin.counting.repository;

import lombok.NoArgsConstructor;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@NoArgsConstructor
public class ElectionDayRepository extends BaseRepository {

    ElectionDayRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<ElectionDay> findForPollingDistrict(long pollingDistrictPk) {
        Query query = getEm().createNamedQuery("ElectionDay.findForPollingDistrict");
        query.setParameter(1, pollingDistrictPk);
        return query.getResultList();
    }

    public List<ElectionDay> findForMunicipality(long municipalityPk) {
        Query query = getEm().createNamedQuery("ElectionDay.findForMunicipality");
        query.setParameter(1, municipalityPk);
        return query.getResultList();
    }

    public ElectionDay findByPk(long electionDayPk) {
        return super.findEntityByPk(ElectionDay.class, electionDayPk);
    }
}
