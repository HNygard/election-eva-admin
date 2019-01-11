package no.valg.eva.admin.rapport.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.rapport.domain.model.ElectionEventReport;

public class ElectionEventReportRepository extends BaseRepository {
	public ElectionEventReportRepository() {
		// brukes av CDI rammeverket
	}

	public ElectionEventReportRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<ElectionEventReport> findByElectionEventPath(ElectionPath electionEventPath) {
		electionEventPath.assertElectionEventLevel();
		TypedQuery<ElectionEventReport> query = getEm().createNamedQuery("ElectionEventReport.findByElectionEventId", ElectionEventReport.class);
		query.setParameter("electionEventId", electionEventPath.path());
		return query.getResultList();
	}

	public ElectionEventReport create(UserData userData, ElectionEventReport county) {
		return createEntity(userData, county);
	}

	public void deleteReports(UserData userData, List<ElectionEventReport> list) {
		deleteEntities(userData, list);
	}
}
