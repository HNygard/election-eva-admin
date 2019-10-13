package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;

@Default
@ApplicationScoped
public class CountryRepository extends BaseRepository {
	private static final String PARAM_ELECTION_EVENT_PK = "electionEventPk";
	private static final String ID = "id";

	public Country create(UserData userData, Country country) {
		return createEntity(userData, country);
	}

	public Country update(UserData userData, Country country) {
		return updateEntity(userData, country);
	}

	public void deleteByPk(UserData userData, Long countryPk) {
		deleteEntity(userData, Country.class, countryPk);
	}

	public Country findByPk(Long pk) {
		return findEntityByPk(Country.class, pk);
	}

	public Country findCountryById(Long electionEventPk, String id) {
		TypedQuery<Country> query = getEm().createNamedQuery("Country.findById", Country.class);
		setCacheHint(query);
		query.setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk);
		query.setParameter(ID, id.trim());

		List<Country> result = query.getResultList();
		if (result != null && !result.isEmpty()) {
			return result.get(0);
		} else {
			return null;
		}
	}

	public List<Country> getCountriesForElectionEvent(Long electionEventPk) {
		TypedQuery<Country> query = getEm().createNamedQuery("Countries.findByElectionEvent", Country.class);
		query.setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk);
		return query.getResultList();
	}

	public List<County> getCountiesByStatus(Long electionEventPk, Integer status) {
		return getEm()
				.createNamedQuery("County.findByElectionEventAndStatus", County.class)
				.setParameter("countyStatusId", status)
				.setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk)
				.getResultList();
	}
}
