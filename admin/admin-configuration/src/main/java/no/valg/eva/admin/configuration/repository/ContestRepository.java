package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.status.ContestStatus;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;

public class ContestRepository extends BaseRepository {
	private static final String ELECTION_EVENT_PK = "electionEventPk";
	private static final String AREA_PATH = "areaPath";
	private static final String ELECTION_PK = "electionPk";
	private static final String ID = "id";
	private static final String CONTEST_STATUS_ID = "contestStatusId";
	private static final String MV_AREA_PK = "mvAreaPk";

	public ContestRepository(EntityManager entityManager) {
		super(entityManager);
	}

	@SuppressWarnings("unused")
	private ContestRepository() {
	}

	public List<Contest> findBoroughContestsInMunicipality(Municipality municipality) {
		TypedQuery<Contest> query = getEm().createNamedQuery("Contest.findBoroughContestsInMunicipality", Contest.class);
		query.setParameter(1, municipality);
		return query.getResultList();
	}

	public List<Contest> findByElectionEventAndArea(long electionEventPk, AreaPath areaPath) {
		TypedQuery<Contest> query = getEm().createNamedQuery("Contest.findByElectionEventAndArea", Contest.class);
		query.setParameter(ELECTION_EVENT_PK, electionEventPk);
		query.setParameter(AREA_PATH, areaPath.path());
		return query.getResultList();
	}

	public Contest findByPk(Long contestPk) {
		return super.findEntityByPk(Contest.class, contestPk);
	}

	public List<Contest> findByElectionPk(Long electionPk) {
		return getEm().createNamedQuery("Contest.findByElection", Contest.class).setParameter("electionPk", electionPk).getResultList();
	}

	public Contest findContestById(Long electionPk, String id) {
		TypedQuery<Contest> query = getEm().createNamedQuery("Contest.findById", Contest.class);
		query.setParameter(ELECTION_PK, electionPk);
		query.setParameter(ID, id);

		List<Contest> result = query.getResultList();
		if (result != null && !result.isEmpty()) {
			return result.get(0);
		} else {
			return null;
		}
	}

	public List<Contest> findContestsByStatus(Long electionEventPk, ContestStatus contestStatus) {
		TypedQuery<Contest> query = getEm().createNamedQuery("Contest.findByElectionEventAndStatus", Contest.class);
		query.setParameter(CONTEST_STATUS_ID, contestStatus.id());
		query.setParameter(ELECTION_EVENT_PK, electionEventPk);
		return query.getResultList();
	}

	public Contest findByElectionAndArea(Election election, MvArea area) {
		return getEm().createNamedQuery("Contest.findByElectionAndArea", Contest.class)
				.setParameter(MV_AREA_PK, area.getPk())
				.setParameter(ELECTION_PK, election.getPk())
				.getSingleResult();
	}

	public Contest update(UserData userData, Contest contest) {
		return super.updateEntity(userData, contest);
	}

	public Contest create(UserData userData, Contest contest) {
		return super.createEntity(userData, contest);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, Contest.class, pk);
	}

	public Contest findSingleByPath(ElectionPath contestPath) {
		contestPath.assertContestLevel();
		try {
			return getEm()
					.createNamedQuery("MvElection.findByPath", MvElection.class)
					.setParameter("path", contestPath.path())
					.getSingleResult().getContest();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public int antallMultiomraadedistrikter(ElectionEvent valghendelse) {
		return getEm()
			.createNamedQuery("Contest.finnMultiomraadedistrikter", Contest.class)
			.setParameter("electionEventPk", valghendelse.getPk())
			.getResultList().size();
	}
}
