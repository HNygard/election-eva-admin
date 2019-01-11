package no.valg.eva.admin.configuration.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Country;

public class BoroughRepository extends BaseRepository {
	private static final String PARAM_MUNICIPALITY_PK = "municipalityPk";
	private static final String ID = "id";

	public BoroughRepository() {
	}

	public BoroughRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public Borough createBorough(UserData userData, Borough borough) {
		return createEntity(userData, borough);
	}

	public Borough updateBorough(UserData userData, Borough borough) {
		return updateEntity(userData, borough);
	}

	public void deleteBorough(UserData userData, Long pk) {
		deleteEntity(userData, Borough.class, pk);
	}

	public Borough findBoroughById(Long municipalityPk, String id) {
		TypedQuery<Borough> query = getEm().createNamedQuery("Borough.findById", Borough.class);
		setCacheHint(query);
		query.setParameter(PARAM_MUNICIPALITY_PK, municipalityPk);
		query.setParameter(ID, id);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public boolean isBoroughForWholeMunicipalityAlreadyExisting(Long municipalityPk) {
		TypedQuery<Borough> query = getEm().createNamedQuery("Borough.findMunicipality1", Borough.class);
		query.setParameter(PARAM_MUNICIPALITY_PK, municipalityPk);
		List<Borough> result = query.getResultList();
		return result != null && !result.isEmpty();
	}

	public Borough findBoroughByPk(Long boroughPk) {
		return findEntityByPk(Borough.class, boroughPk);
	}

	public List<Borough> findWithoutPollingDistricts(List<Country> countries) {
		List<Borough> boroughs = new ArrayList<>();
		TypedQuery<Borough> query = getEm().createNamedQuery("Borough.findWithoutPollingDistrictsByCountry", Borough.class);
		for (Country country : countries) {
			query.setParameter("countryPk", country.getPk());
			boroughs.addAll(query.getResultList());
		}
		return boroughs;
	}

	public List<Borough> findByMunicipality(Long municipalityPk) {
		TypedQuery<Borough> query = getEm().createNamedQuery("Borough.findMunicipality", Borough.class);
		query.setParameter("municipalityPk", municipalityPk);
		return query.getResultList();
	}
}
