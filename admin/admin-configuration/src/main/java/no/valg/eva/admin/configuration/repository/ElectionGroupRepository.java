package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import no.evote.constants.EvoteConstants;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvElection;

public class ElectionGroupRepository extends BaseRepository {

	public static final String FIELD_ELECTION_EVENT_PK = "electionEventPk";
	public static final String FIELD_ID = "id";

	public ElectionGroupRepository() {
	}

	protected ElectionGroupRepository(final EntityManager entityManager) {
		super(entityManager);
	}

	public List<ElectionGroup> getElectionGroupsSorted(final Long electionEventPk) {
		return getEm()
				.createNamedQuery("ElectionEvent.getElectionGroupSorted", ElectionGroup.class)
				.setParameter("electionEventPk", electionEventPk)
				.getResultList();
	}

	@SuppressWarnings(EvoteConstants.WARNING_UNCHECKED)
	public ElectionGroup findElectionGroupById(Long electionEventPk, String id) {
		Query query = getEm().createNamedQuery("ElectionGroup.findById");

		query.setParameter(FIELD_ELECTION_EVENT_PK, electionEventPk);
		query.setParameter(FIELD_ID, id);

		List<ElectionGroup> result = query.getResultList();
		if (result != null && !result.isEmpty()) {
			return result.get(0);
		} else {
			return null;
		}
	}

	public ElectionGroup create(UserData userData, ElectionGroup electionGroup) {
		return super.createEntity(userData, electionGroup);
	}

	public ElectionGroup update(UserData userData, ElectionGroup electionGroup) {
		return super.updateEntity(userData, electionGroup);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, ElectionGroup.class, pk);
	}

	public ElectionGroup findByPk(Long pk) {
		return super.findEntityByPk(ElectionGroup.class, pk);
	}

	public ElectionGroup findSingleByPath(ElectionPath electionGroupPath) {
		electionGroupPath.assertElectionGroupLevel();
		try {
			return getEm()
					.createNamedQuery("MvElection.findByPath", MvElection.class)
					.setParameter("path", electionGroupPath.path())
					.getSingleResult().getElectionGroup();
		} catch (NoResultException e) {
			return null;
		}
	}
}
