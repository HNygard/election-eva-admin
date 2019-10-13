package no.valg.eva.admin.configuration.repository;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MunicipalityStatus;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for accessing Municipality aggregates.
 */
@Default
@ApplicationScoped
public class MunicipalityRepository extends BaseRepository {
    private static final Logger LOG = Logger.getLogger(MunicipalityRepository.class);

    private static final String PARAM_ELECTION_EVENT_PK = "electionEventPk";
    private static final String PARAM_COUNTY_PK = "countyPk";
    private static final String PARAM_COUNTRY_PK = "countryPk";
    private static final String PARAM_MUNICIPALITY_PK = "municipalityPk";
    private static final String PARAM_ID = "id";

    MunicipalityRepository() {
    }

    public MunicipalityRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    /**
     * @return Municipality with given primary key
     */
    Municipality municipalityByPk(final long municipalityPk) {
        return findEntityByPk(Municipality.class, municipalityPk);
    }

    public Municipality municipalityByElectionEventAndId(long electionEventPk, String municipalityId) {
        TypedQuery<Municipality> query = getEm().createNamedQuery("Municipality.findByElectionEventAndId", Municipality.class);
        query.setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk);
        query.setParameter(PARAM_ID, municipalityId.trim());

        List<Municipality> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public Municipality findByPk(Long municipalityPk) {
        return findEntityByPk(Municipality.class, municipalityPk);
    }

    public Municipality findByPkWithScanningConfig(Long municipalityPk) {
        return getEm()
                .createNamedQuery("Municipality.findByPkWithScanningConfig", Municipality.class)
                .setParameter("pk", municipalityPk)
                .getSingleResult();
    }

    public List<Municipality> findByElectionEventWithScanningConfig(Long electionEventPk) {
        return getEm()
                .createNamedQuery("Municipality.findByElectionEventWithScanningConfig", Municipality.class)
                .setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk)
                .getResultList();
    }

    public Municipality create(UserData userData, Municipality municipality) {
        return createEntity(userData, municipality);
    }

    public Municipality update(UserData userData, Municipality municipality) {
        return updateEntity(userData, municipality);
    }

    public void delete(UserData userData, Long pk) {
        deleteEntity(userData, Municipality.class, pk);
    }

    public Municipality findMunicipalityById(Long countyPk, String id) {
        TypedQuery<Municipality> query = getEm().createNamedQuery("Municipality.findById", Municipality.class);
        setCacheHint(query);
        query.setParameter(PARAM_COUNTY_PK, countyPk);
        query.setParameter(PARAM_ID, id);

        List<Municipality> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public List<Municipality> getMunicipalitiesWithPollingPlacesWithoutPollingStations(UserData userData) {
        return getEm()
                .createNamedQuery("Municipality.findWihoutConfiguredPollingStations", Municipality.class)
                .setParameter("areaPath", userData.getOperatorRole().getMvArea().getAreaPath())
                .getResultList();
    }

    public List<Municipality> findByCounty(Long countyPk) {
        return getEm()
                .createNamedQuery("Municipality.findByCounty", Municipality.class)
                .setParameter(PARAM_COUNTY_PK, countyPk)
                .getResultList();
    }

    public void setRequiredProtocolCountForElectionEvent(Long electionEventPk, boolean requiredProtocolCount) {
        getEm().createNativeQuery("UPDATE municipality m SET required_protocol_count = :requiredProtocolCount "
                + "FROM county c "
                + "JOIN country a ON c.country_pk = a.country_pk "
                + "WHERE m.county_pk = c.county_pk  AND a.election_event_pk = :electionEventPk")
                .setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk)
                .setParameter("requiredProtocolCount", requiredProtocolCount).executeUpdate();
    }

    public boolean getRequiredProtocolCountForElectionEvent(Long electionEventPk) {
        Query query = getEm().createQuery("SELECT m.requiredProtocolCount FROM Municipality m " +
                "WHERE m.county.country.electionEvent.pk = :electionEventPk");
        query.setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk);
        query.setMaxResults(1);
        return (Boolean) query.getResultList().get(0);
    }

    public Municipality findUniqueMunicipalityByElectionEvent(Long pk, String countryId, String countyId, String municipalityId) {
        Query query = getEm().createNativeQuery("SELECT m.* FROM municipality m "
                + "JOIN county ON m.county_pk = county.county_pk "
                + "JOIN country ON country.country_pk = county.country_pk "
                + "WHERE m.municipality_id= :municipalityId AND county.county_id = :countyId AND country.country_id = :countryId "
                + "AND country.election_event_pk = :electionEventPk", Municipality.class);
        query.setParameter("municipalityId", municipalityId);
        query.setParameter("countyId", countyId);
        query.setParameter("countryId", countryId);
        query.setParameter(PARAM_ELECTION_EVENT_PK, pk);

        Municipality result = (Municipality) query.getSingleResult();

        if (result != null) {
            return result;
        } else {
            LOG.debug("I findUniqueMunicipalityByElectionEvent - returnerer 0 - municipality har ikke blitt opprettet for nye electionevent?");
            return null;
        }
    }

    public List<Municipality> getMunicipalitiesByStatus(Long electionEventPk, Integer status) {
        return getEm()
                .createNamedQuery("Municipality.findByElectionEventAndStatus", Municipality.class)
                .setParameter("municipalityStatusId", status)
                .setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk)
                .getResultList();
    }

    public List<Municipality> getMunicipalitiesWithoutEncompassingBoroughs(Long electionEventPk) {
        return getEm()
                .createNamedQuery("Municipality.findWithoutEncompassingBoroughs", Municipality.class)
                .setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk)
                .getResultList();
    }

    public List<Municipality> getMunicipalitiesWithoutEncompassingPollingDistricts(Long electionEventPk) {
        return getEm()
                .createNamedQuery("Municipality.findWithoutEncompassingPollingDistricts", Municipality.class)
                .setParameter(PARAM_ELECTION_EVENT_PK, electionEventPk)
                .getResultList();
    }

    public List<Municipality> findWithoutBoroughsByCountries(List<Country> countries) {
        List<Municipality> municipalities = new ArrayList<>();
        TypedQuery<Municipality> query = getEm().createNamedQuery("Municipality.withoutBoroughsByCountry", Municipality.class);
        for (Country country : countries) {
            query.setParameter(PARAM_COUNTRY_PK, country.getPk());
            municipalities.addAll(query.getResultList());
        }
        return municipalities;
    }

    public List<Municipality> findMunicipalitiesByCountryPk(Long countryPk) {
        return getEm()
                .createNamedQuery("Municipality.findByCountry", Municipality.class)
                .setParameter(PARAM_COUNTRY_PK, countryPk)
                .getResultList();
    }

    public Locale getLocale(Municipality municipality) {
        try {
            return (Locale) getEm()
                    .createNamedQuery("Municipality.findLocale")
                    .setParameter(PARAM_MUNICIPALITY_PK, municipality.getPk())
                    .getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null;
        }
    }

    public MunicipalityStatus getStatus(Long municipalityPk) {
        return findEntityByPk(MunicipalityStatus.class, findEntityByPk(Municipality.class, municipalityPk).getMunicipalityStatus().getPk());
    }

    public boolean hasMunicipalities(County county) {
        return (Long) getEm()
                .createNamedQuery("Municipality.findCountByCounty")
                .setParameter(PARAM_COUNTY_PK, county.getPk())
                .getSingleResult() > 0;
    }

    public MunicipalityStatus findMunicipalityStatusById(Integer municipalityStatusId) {
        return super.findEntityById(MunicipalityStatus.class, municipalityStatusId);
    }

    public Municipality getReference(Municipality municipality) {
        return getReference(Municipality.class, municipality.getPk());
    }

    public void deleteOpeningHours(UserData userData, Municipality municipality) {
        Municipality domainMunicipality = getReference(municipality);

        domainMunicipality.getOpeningHours().clear();
        update(userData, domainMunicipality);
    }
}
