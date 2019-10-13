package no.valg.eva.admin.valgnatt.repository;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.voting.domain.model.Stemmegivningsstatistikk;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;

@Default
@ApplicationScoped
public class StemmegivningsstatistikkRepository extends BaseRepository {

	public StemmegivningsstatistikkRepository() {
		// for CDI rammeverket
	}

	public StemmegivningsstatistikkRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public Stemmegivningsstatistikk finnForOmrådeOgValg(ValggeografiSti valggeografiSti, boolean papirmanntall) {
		Query query = getEm().createNamedQuery("Voting.findStemmegivninger");
		query.setParameter("areaPath", valggeografiSti.areaPath().path());
		query.setParameter("xim", !papirmanntall);
		return (Stemmegivningsstatistikk) query.getSingleResult();
	}

	public int finnVOStemmegivninger(ValggeografiSti valggeografiSti, Long contestPk) {
		Query query = getEm().createNamedQuery("ManualContestVoting.findVOStemmegivninger");
		query.setParameter("areaPath", valggeografiSti.areaPath().path());
		query.setParameter("contest_pk", contestPk);
		BigDecimal bd = (BigDecimal) query.getSingleResult();
		return bd == null ? 0 : bd.intValue();
	}

	public int numberOfRejectedVotings(ValggeografiSti valggeografiSti, String votingRejectionId) {
		String areaPath = valggeografiSti.areaPath().path();
		return ((BigInteger) getEm()
				.createNamedQuery("Voting.findRejectedVotingCount")
				.setParameter("areaPath", areaPath)
				.setParameter("votingRejectionId", votingRejectionId)
				.getSingleResult()).intValue();
	}
}
