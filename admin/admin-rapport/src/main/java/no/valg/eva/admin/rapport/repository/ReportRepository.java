package no.valg.eva.admin.rapport.repository;

import java.util.List;

import javax.persistence.EntityManager;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.rapport.domain.model.Report;

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
