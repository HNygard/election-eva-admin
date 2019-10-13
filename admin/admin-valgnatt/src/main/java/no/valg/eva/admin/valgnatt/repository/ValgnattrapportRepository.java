package no.valg.eva.admin.valgnatt.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering.ValgnattrapportPk;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.ValgnattrapportAntall;

@Default
@ApplicationScoped
public class ValgnattrapportRepository extends BaseRepository {

	public ValgnattrapportRepository() {
		// brukes av CDI rammeverket
	}

	public ValgnattrapportRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public ValgnattrapportAntall countReportableByContestAndMunicipality(Contest contest, Municipality municipality) {
		return getEm().createNamedQuery("Valgnattrapport.countByContestAndMunicipality", ValgnattrapportAntall.class)
				.setParameter(0, contest.getPk())
				.setParameter(1, municipality.getPk())
				.getSingleResult();
	}
	
	public List<Valgnattrapport> byContestAndMunicipality(Contest contest, Municipality municipality) {
		TypedQuery<Valgnattrapport> query = getEm().createNamedQuery("Valgnattrapport.byContestAndMunicipality", Valgnattrapport.class);
		query.setParameter("contestPk", contest.getPk());
		query.setParameter("municipalityPk", municipality.getPk());
		return query.getResultList();
	}

	public List<Valgnattrapport> byContestAndReportType(Contest contest, ReportType reportType) {
		TypedQuery<Valgnattrapport> query = getEm().createNamedQuery("Valgnattrapport.byContestAndReportType", Valgnattrapport.class);
		query.setParameter("contestPk", contest.getPk());
		query.setParameter("reportType", reportType);
		return query.getResultList();
	}

	public List<Valgnattrapport> finnFor(Election valg, ReportType rapportType) {
		TypedQuery<Valgnattrapport> query = getEm().createNamedQuery("Valgnattrapport.byElectionAndReportType", Valgnattrapport.class);
		query.setParameter("electionPk", valg.getPk());
		query.setParameter("reportType", rapportType);
		return query.getResultList();
	}

	public Valgnattrapport byContestReportTypeAndMvArea(Contest contest, ReportType reportType, MvArea mvArea) {
		TypedQuery<Valgnattrapport> query = getEm().createNamedQuery("Valgnattrapport.byContestReportTypeAndMvArea", Valgnattrapport.class);
		query.setParameter("contestPk", contest.getPk());
		query.setParameter("reportType", reportType);
		query.setParameter("mvAreaPk", mvArea.getPk());
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Valgnattrapport byElectionAndReportType(Election election, ReportType reportType) {
		List<Valgnattrapport> resultList = getEm()
				.createNamedQuery("Valgnattrapport.byElectionAndReportType", Valgnattrapport.class)
				.setParameter("electionPk", election.getPk())
				.setParameter("reportType", reportType)
				.getResultList();
		return resultList.isEmpty() ? null : resultList.get(0);
	}

	public Valgnattrapport create(Valgnattrapport valgnattrapport) {
		getEm().persist(valgnattrapport);
		return valgnattrapport;
	}

    public Valgnattrapport byPk(ValgnattrapportPk valgnattrapportPk) {
        return findEntityByPk(Valgnattrapport.class, valgnattrapportPk.getValgnattrapportPk());
    }
}
