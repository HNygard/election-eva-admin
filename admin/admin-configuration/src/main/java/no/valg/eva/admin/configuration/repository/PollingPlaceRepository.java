package no.valg.eva.admin.configuration.repository;

import no.evote.model.views.PollingPlaceVoting;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class PollingPlaceRepository extends BaseRepository {
    private static final String ID = "id";
    private static final String PARAM_POLLING_DISTRICT_PK = "pollingDistrictPk";
    private static final String PARAM_ELECTION_EVENT_PK = "electionEventPk";
    private static final String PARAM_POLLING_PLACE_PK = "pollingPlacePk";
    private static final String ADVANCE_VOTE_IN_BALLOT_BOX = "advanceVoteInBallotBox";
    
    PollingPlaceRepository() {
        // Brukes av CDI?
    }

    public PollingPlaceRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public void refresh(PollingPlace pollingPlace) {
        getEm().refresh(pollingPlace);
    }

    public PollingPlace findPollingPlaceByElectionDayVoting(Long pollingDistrictPk) {
        TypedQuery<PollingPlace> query = getEm().createNamedQuery("PollingPlace.findByElectionDayVoting", PollingPlace.class);
        query.setParameter(PARAM_POLLING_DISTRICT_PK, pollingDistrictPk);

        List<PollingPlace> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public PollingPlace create(UserData userData, PollingPlace pollingPlace) {
        return createEntity(userData, pollingPlace);
    }

    public PollingPlace update(UserData userData, PollingPlace pollingPlace) {
        return updateEntity(userData, pollingPlace);
    }

    public void delete(UserData userData, Long pk) {
        deleteEntity(userData, PollingPlace.class, pk);
    }

    public PollingPlace findByPk(Long pk) {
        return findEntityByPk(PollingPlace.class, pk);
    }

    public PollingPlace findPollingPlaceById(Long pollingDistrictPk, String id) {
        TypedQuery<PollingPlace> query = getEm().createNamedQuery("PollingPlace.findById", PollingPlace.class);
        query.setParameter(PARAM_POLLING_DISTRICT_PK, pollingDistrictPk);
        query.setParameter(ID, id);

        List<PollingPlace> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public List<PollingPlaceVoting> findAdvancedPollingPlaceByMunicipality(Long electionEventPk, String municipalityId) {
        return getEm()
                .createNamedQuery("PollingPlaceVoting.findAdvancedPollingPlaceByMunicipality", PollingPlaceVoting.class)
                .setParameter("electionEventPk", electionEventPk)
                .setParameter("municipalityId", municipalityId)
                .getResultList();
    }

    public PollingPlace findFirstPollingPlace(long pollingDistrictPk) {
        TypedQuery<PollingPlace> query = getEm().createNamedQuery("PollingPlace.findFirstPollingPlace", PollingPlace.class)
                .setParameter(PARAM_POLLING_DISTRICT_PK, pollingDistrictPk);
        query.setMaxResults(1);
        List<PollingPlace> pollingPlaces = query.getResultList();
        if (pollingPlaces.isEmpty()) {
            return null;
        } else {
            return pollingPlaces.get(0);
        }
    }

    public PollingPlace findFirstByPollingDistrictPkAndAdvanceVoteInBallotBox(long pollingDistrictPk, boolean advanceVoteInBallotBox) {
        TypedQuery<PollingPlace> query = getEm().createNamedQuery("PollingPlace.findFirstByPollingDistrictPkAndAdvanceVoteInBallotBox", PollingPlace.class)
                .setParameter(PARAM_POLLING_DISTRICT_PK, pollingDistrictPk)
                .setParameter(ADVANCE_VOTE_IN_BALLOT_BOX, advanceVoteInBallotBox);
        query.setMaxResults(1);
        List<PollingPlace> pollingPlaces = query.getResultList();
        if (pollingPlaces.isEmpty()) {
            return null;
        } else {
            return pollingPlaces.get(0);
        }
    }

    public List<PollingPlace> findByPollingDistrict(Long pollingDistrictPk) {
        return getEm()
                .createNamedQuery("PollingPlace.findByPollingDistrict", PollingPlace.class)
                .setParameter(PARAM_POLLING_DISTRICT_PK, pollingDistrictPk)
                .getResultList();
    }

    public List<PollingPlace> findPollingPlacesWithOpeningHours(ElectionEvent electionEvent) {
        return getEm()
                .createNamedQuery("PollingPlace.findPollingPlacesWithOpeningHours", PollingPlace.class)
                .setParameter(PARAM_ELECTION_EVENT_PK, electionEvent.getPk())
                .getResultList();
    }

    public PollingPlace findPollingPlaceWithOpeningHours(long pk) {
        return getEm()
                .createNamedQuery("PollingPlace.findPollingPlaceWithOpeningHours", PollingPlace.class)
                .setParameter(PARAM_POLLING_PLACE_PK, pk)
                .getSingleResult();
    }

    public PollingPlace getReference(PollingPlace pollingPlace) {
        return getReference(PollingPlace.class, pollingPlace.getPk());
    }

    public void deleteOpeningHours(UserData userData, PollingPlace pollingPlace) {
        PollingPlace domainPollingPlace = getReference(pollingPlace);
        domainPollingPlace.removeAllOpeningHours();

        update(userData, domainPollingPlace);
    }
}
