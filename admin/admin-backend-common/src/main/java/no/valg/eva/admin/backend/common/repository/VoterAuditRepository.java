package no.valg.eva.admin.backend.common.repository;

import no.evote.model.views.VoterAudit;
import org.joda.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import java.util.List;

@Default
@ApplicationScoped
public class VoterAuditRepository extends BaseRepository {
	public VoterAuditRepository() {
	}

	public VoterAuditRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<VoterAudit> getHistoryForMunicipality(
			String municipalityId, char endringstype, LocalDate startDate, LocalDate endDate, Long electionEventPk, String selectedSearchMode,
			Boolean searchOnlyApproved) {
		StringBuilder searchString = new StringBuilder("SELECT va FROM VoterAudit va ");
		searchString.append("WHERE va.electionEventPk = :electionEventPk ");
		searchString.append("AND va.municipalityId = :municipalityId ");
		searchString.append("AND va.id.auditTimestamp BETWEEN :startDate AND :endDate ");
		if (endringstype != ' ') {
			searchString.append("AND endringstype = '").append(endringstype).append("'");
		} else {
			searchString.append("AND endringstype IS NOT NULL ");
		}
		if (searchOnlyApproved) {
			searchString.append("AND va.id.voterPk IN (SELECT v.pk FROM Voter v WHERE v.approved = TRUE) ");
		}
		if (selectedSearchMode.equalsIgnoreCase("M")) {
			searchString.append("AND va.importBatchNumber IS NULL ");
		} else if (selectedSearchMode.equalsIgnoreCase("A")) {
			searchString.append("AND va.importBatchNumber > 0 ");
		} else if (selectedSearchMode.equalsIgnoreCase(" ")) {
			searchString.append("AND (va.importBatchNumber > 0 OR va.importBatchNumber IS NULL) ");
		}
		searchString.append("ORDER BY va.id.auditTimestamp ASC");
		return getEm()
				.createQuery(searchString.toString(), VoterAudit.class)
				.setParameter("electionEventPk", electionEventPk)
				.setParameter("municipalityId", municipalityId)
				.setParameter("startDate", startDate.toDateTimeAtStartOfDay())
				.setParameter("endDate", endDate.toDateTimeAtStartOfDay().plusDays(1))
				.getResultList();
	}

    public List<VoterAudit> getHistoryForVoter(long voterPk) {
        StringBuilder searchString = new StringBuilder("SELECT va FROM VoterAudit va ");
        searchString.append("WHERE va.id.voterPk = :voterPk ");
        searchString.append("ORDER BY va.id.auditTimestamp ASC");
        return getEm()
                .createQuery(searchString.toString(), VoterAudit.class)
                .setParameter("voterPk", voterPk)
                .getResultList();
    }

    public List<VoterAudit> getHistoryForVoter(String voterId) {
		String searchString = "SELECT va FROM VoterAudit va " + "WHERE va.voterId = :voterId " +
				"ORDER BY va.id.auditTimestamp ASC";
		return getEm()
				.createQuery(searchString, VoterAudit.class)
                .setParameter("voterId", voterId)
                .getResultList();
    }
}
