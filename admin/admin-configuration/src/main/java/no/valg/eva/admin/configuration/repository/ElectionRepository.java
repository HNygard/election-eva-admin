package no.valg.eva.admin.configuration.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.MvElection;

public class ElectionRepository extends BaseRepository {
	private static final String FIELD_ELECTION_GROUP_PK = "electionGroupPk";
	private static final String FIELD_ID = "id";
	private static final String ELECTION_EVENT_PK = "electionEventPk";

	public ElectionRepository() {
	}

	public ElectionRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public Election create(UserData userData, Election election) {
		return super.createEntity(userData, election);
	}

	public Election update(UserData userData, Election election) {
		return super.updateEntity(userData, election);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, Election.class, pk);
	}

	public Election findByPk(Long pk) {
		return super.findEntityByPk(Election.class, pk);
	}

	public Election findElectionByElectionGroupAndId(Long electionGroupPk, String id) {
		List<Election> result = getEm()
				.createNamedQuery("Election.findById", Election.class)
				.setParameter(FIELD_ELECTION_GROUP_PK, electionGroupPk)
				.setParameter(FIELD_ID, id)
				.getResultList();
		if (result != null && !result.isEmpty()) {
			return result.get(0);
		} else {
			return null;
		}

	}

	public List<Election> findElectionsByElectionGroup(Long electionGroupPk) {
		return getEm().createNamedQuery("Election.findByElectionGroup", Election.class).setParameter("electionGroupPk", electionGroupPk).getResultList();
	}

	public Election findElectionInEvent(String electionId, String electionEventId) {
		List<Election> result = getEm()
				.createNamedQuery("Election.findElectionInElectionEvent", Election.class)
				.setParameter("electionId", electionId)
				.setParameter("electionEventId", electionEventId)
				.getResultList();
		if (result != null && !result.isEmpty()) {
			return result.get(0);
		} else {
			return null;
		}
	}

	public List<Election> getElectionsWithoutContests(Long electionEventPk) {
		List<Election> withoutContests = new ArrayList<>();
		TypedQuery<Election> query = getEm().createNamedQuery("Election.findByEvent", Election.class);
		query.setParameter(ELECTION_EVENT_PK, electionEventPk);

		List<Election> elections = query.getResultList();
		Query findContestsQuery = getEm().createNamedQuery("Contest.countByElection");
		for (Election election : elections) {
			findContestsQuery.setParameter("electionPk", election.getPk());
			if (((Long) findContestsQuery.getSingleResult()) == 0) {
				withoutContests.add(election);
			}
		}

		return withoutContests;
	}

	public ElectionType findElectionTypeById(String electionTypeId) {
		return super.findEntityById(ElectionType.class, electionTypeId);
	}

	public Election findSingleByPath(ElectionPath electionPath) {
		electionPath.assertElectionLevel();
		try {
			return getEm()
					.createNamedQuery("MvElection.findByPath", MvElection.class)
					.setParameter("path", electionPath.path())
					.getSingleResult().getElection();
		} catch (NoResultException e) {
			return null;
		}
	}

}
