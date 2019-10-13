package no.valg.eva.admin.configuration.repository;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.counting.constants.ResponsibilityId;
import no.valg.eva.admin.configuration.domain.model.Responsibility;
import no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

@Default
@ApplicationScoped
public class ResponsibleOfficerRepository extends BaseRepository {
	public ResponsibleOfficerRepository() {
	}

	public ResponsibleOfficerRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public ResponsibleOfficer create(UserData userData, ResponsibleOfficer responsibleOfficer) {
		return super.createEntity(userData, responsibleOfficer);
	}

	public ResponsibleOfficer update(UserData userData, ResponsibleOfficer responsibleOfficer) {
		return super.updateEntity(userData, responsibleOfficer);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, ResponsibleOfficer.class, pk);
	}

	public List<ResponsibleOfficer> findResponsibleOfficersForReportingUnit(Long reportingUnitPk) {
		TypedQuery<ResponsibleOfficer> query = getEm().createNamedQuery("ResponsibleOfficer.findResponsibleOfficersForReportingUnit", ResponsibleOfficer.class);
		query.setParameter("reportingUnitPk", reportingUnitPk);
		return query.getResultList();
	}

	public boolean hasResponsibleOfficersForReportingUnit(Long reportingUnitPk) {
		Query query = getEm().createNamedQuery("ResponsibleOfficer.countResponsibleOfficersForReportingUnit", Long.class);
		query.setParameter("reportingUnitPk", reportingUnitPk);
		return ((Long) query.getSingleResult()) != 0;
	}

	public Integer findNextDisplayOrder(Long reportingUnitPk) {
		TypedQuery<Integer> query = getEm().createNamedQuery("ResponsibleOfficer.findNextDisplayOrder", Integer.class);
		query.setParameter("reportingUnitPk", reportingUnitPk);
		if (query.getSingleResult() != null) {
			return query.getSingleResult() + 1;
		} else {
			return 1;
		}
	}

	public Responsibility findResponsibilityById(ResponsibilityId id) {
		return super.findEntityById(Responsibility.class, id.getId());
	}

	public ResponsibleOfficer findByPk(Long pk) {
		return super.findEntityByPk(ResponsibleOfficer.class, pk);
	}
    
    public List<ResponsibleOfficer> findResponsibleOfficersMatchingName(Long areaPk, String nameLine) {
		return getEm()
				.createNamedQuery("ResponsibleOfficer.findResponsibleOfficersMatchingNameByContest", ResponsibleOfficer.class)
				.setParameter("areaPk", areaPk)
				.setParameter("nameLine", nameLine)
				.getResultList();
	}
}
