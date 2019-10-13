package no.valg.eva.admin.configuration.repository.valgnatt;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;
import no.valg.eva.admin.configuration.domain.model.valgnatt.IntegerWrapper;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ReportConfiguration;

/**
 * Finds electoral roll per polling district, and report configuration. In DDD terms, this would probably not be considered a repository, but named so here for
 * consistency.
 */
@Default
@ApplicationScoped
public class ValgnattElectoralRollRepository extends BaseRepository {

	private static final int PARAM_3 = 3;
	private static final long ZERO = 0;

	public ValgnattElectoralRollRepository() {
		// for CDI rammeverket
	}

	ValgnattElectoralRollRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public List<ElectoralRollCount> valgnattElectoralRoll(ElectionPath electionPath) {
		TypedQuery<ElectoralRollCount> query = getEm().createNamedQuery("MvArea.electoralRollForPollingDistrictQuery", ElectoralRollCount.class);
		query.setParameter(1, electionPath.getElectionEventId());
		return query.getResultList();
	}

	public List<ReportConfiguration> valgnattReportConfiguration(MvElection mvElection) {
		return valgnattReportConfiguration(mvElection, makeMuncipality());
	}

	private Municipality makeMuncipality() {
		Municipality municipality = new Municipality();
		municipality.setPk(ZERO);
		return municipality;
	}

	public List<ReportConfiguration> valgnattReportConfiguration(MvElection mvElection, Municipality municipality) {
		TypedQuery<ReportConfiguration> query = getEm().createNamedQuery("MvArea.reportConfigurationQuery", ReportConfiguration.class);
		query.setParameter(1, mvElection.getElection().getPk());
		query.setParameter(2, mvElection.getAreaLevel());
		query.setParameter(PARAM_3, municipality.getPk());
		return query.getResultList();
	}

	public List<IntegerWrapper> pollingDistrictPkListeForBarneOmråder(ElectionEvent valghendelse, Integer contestPk) {
		TypedQuery<IntegerWrapper> query = getEm().createNamedQuery("MvArea.pollingDistrictPkListForContestAreaChild", IntegerWrapper.class);
		query.setParameter(1, valghendelse.getPk());
		query.setParameter(2, contestPk);
		return query.getResultList();
	}
}
