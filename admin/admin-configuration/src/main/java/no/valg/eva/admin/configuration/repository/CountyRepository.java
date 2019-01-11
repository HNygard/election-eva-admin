package no.valg.eva.admin.configuration.repository;

import lombok.NoArgsConstructor;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.configuration.status.CountyStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyStatus;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

@NoArgsConstructor
public class CountyRepository extends BaseRepository {
	private static final String PARAM_COUNTRY_PK = "countryPk";
    private static final String PARAM_MUNICIPALITY_PK = "municipalityPk";
	private static final String PARAM_ELECTION_EVENT_PK = "electionEventPk";
	private static final String PARAM_ID = "id";

	public CountyRepository(EntityManager entityManager) {
		super(entityManager);
	}
	
	public County create(UserData userData, County county) {
		return createEntity(userData, county);
	}

	public County update(UserData userData, County county) {
		return updateEntity(userData, county);
	}

	public void delete(UserData userData, Long pk) {
		deleteEntity(userData, County.class, pk);
	}

	public County findByPk(Long pk) {
		return findEntityByPk(County.class, pk);
	}

	public County findByPkWithScanningConfig(Long pk) {
		return getEm()
			.createNamedQuery("County.findByPkWithScanningConfig", County.class)
			.setParameter("pk", pk)
			.getSingleResult();
	}
	
	public List<County> findByElectionEventWithScanningConfig(Long electionEventPk) {
		return getEm()
				.createNamedQuery("County.findByElectionEventWithScanningConfig", County.class)
                .setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk)
				.getResultList();
	}
	
	public County countyByElectionEventAndId(long electionEventPk, String countyId) {
		TypedQuery<County> query = getEm().createNamedQuery("County.findByElectionEventAndId", County.class);
		query.setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk);
		query.setParameter(PARAM_ID, countyId.trim());

		List<County> result = query.getResultList();
		if (result != null && !result.isEmpty()) {
			return result.get(0);
		} else {
			return null;
		}
	}

	public County findCountyById(Long countryPk, String id) {
		TypedQuery<County> query = getEm().createNamedQuery("County.findById", County.class);
		setCacheHint(query);
		query.setParameter(PARAM_COUNTRY_PK, countryPk);
		query.setParameter(PARAM_ID, id);
		List<County> result = query.getResultList();
		if (result != null && !result.isEmpty()) {
			return result.get(0);
		} else {
			return null;
		}
	}

	public County findByMunicipality(Long pk) {
		TypedQuery<County> query = getEm().createNamedQuery("County.findByMunicipality", County.class);
        query.setParameter(PARAM_MUNICIPALITY_PK, pk);
		return query.getSingleResult();
	}

	public List<County> getCountiesByCountry(Long countryPk) {
		TypedQuery<County> query = getEm().createNamedQuery("County.findByCountry", County.class);
        query.setParameter(PARAM_COUNTRY_PK, countryPk);
		return query.getResultList();
	}

	public boolean hasCounties(final Country country) {
		Query query = getEm().createNamedQuery("County.findCountByCountry");
        query.setParameter(PARAM_COUNTRY_PK, country.getPk());
		return ((Long) query.getSingleResult()) != 0;
	}

	public CountyStatus findCountyStatusById(Integer countyStatusId) {
		return super.findEntityById(CountyStatus.class, countyStatusId);
	}

	public void updateStatusOnCounties(Long electionEventPk, CountyStatusEnum from, CountyStatusEnum to) {
		if (from == to) {
			return;
		}
		CountyStatus countyStatusFrom = findCountyStatusById(from.id());
		CountyStatus countyStatusTo = findCountyStatusById(to.id());

		// @formatter:off
		String sqlQuery = "UPDATE county c SET county_status_pk = ?1 "
			+ "FROM country a "
			+ "WHERE a.country_pk = c.country_pk AND a.election_event_pk = ?2 "
			+ "AND county_status_pk = ?3 ";
		// @formatter:on
		
		getEm().createNativeQuery(sqlQuery)
				.setParameter(1, countyStatusTo.getPk())
			.setParameter(2, electionEventPk)
				.setParameter(3, countyStatusFrom.getPk())
				.executeUpdate();
		
	}

}
