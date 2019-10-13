package no.valg.eva.admin.rapport.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.rapport.domain.model.Report;

@Default
@ApplicationScoped
public class ReportRepository extends BaseRepository {

	public ReportRepository() {
		// brukes av CDI rammeverket
	}

	public ReportRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<Report> findAll() {
		return super.findAllEntities(Report.class);
	}
}
