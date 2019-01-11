package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.PollingStation;
import no.valg.eva.admin.configuration.domain.model.Voter;

public class PollingStationRepository extends BaseRepository {
	public PollingStationRepository() {
	}

	public PollingStationRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<PollingStation> findByPollingPlace(Long pollingPlacePk) {
		TypedQuery<PollingStation> query = getEm()
				.createNamedQuery("PollingStation.findByPollingPlace", PollingStation.class)
				.setParameter("pollingPlacePk", pollingPlacePk);
		return query.getResultList();
	}

	public long countByPollingPlace(Long pollingPlacePk) {
		Query countByPollingPlace = getEm().createNamedQuery("PollingStation.countByPollingPlace")
				.setParameter("pollingPlacePk", pollingPlacePk);
		return (Long) countByPollingPlace.getSingleResult();
	}

	public List<Voter> getElectoralRollForPollingPlace(PollingPlace pollingPlace) {
		TypedQuery<Voter> query = getEm()
				.createNamedQuery("PollingPlace.getElectoralRollForPollingPlace", Voter.class)
				.setParameter("pollingDistrictPk", pollingPlace.getPollingDistrict().getPk());
		return query.getResultList();
	}

	public void delete(UserData userData, List<PollingStation> pollingStations) {
		super.deleteEntities(userData, pollingStations);
	}

	public PollingStation create(UserData userData, PollingStation pollingStation) {
		return super.createEntity(userData, pollingStation);
	}

	public void setEm(EntityManager em) {
		super.setEm(em);
	}
}
