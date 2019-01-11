package no.valg.eva.admin.opptelling.repository;

import java.util.List;

import javax.persistence.Query;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.counting.domain.model.VoteCountDigest;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

public class VoteCountRepository extends BaseRepository {
	public void slettOpptellinger(ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti, Integer countCategoryPk, Styretype styretype) {
		String queryString = "/* NO LOAD BALANCE */select delete_vote_counts(?, ?, ?, ?)";
		Query query = getEm().createNativeQuery(queryString);
		query.setParameter(1, valghierarkiSti.toString());
		query.setParameter(2, valggeografiSti.toString());
		
		if (countCategoryPk == null) {
			query.setParameter(3, 0);
		} else {
			query.setParameter(3, countCategoryPk);
		}
		if (styretype == null) {
			query.setParameter(4, -1);
		} else {
			query.setParameter(4, styretype.id());
		}
		
		query.getSingleResult();
	}

	public List<VoteCountDigest> findDigestsByContestAndMunicipalityReportingArea(Contest contest, Municipality municipality) {
		return getEm()
				.createNamedQuery("VoteCount.findDigestsByContestAndMunicipalityReportingArea", VoteCountDigest.class)
				.setParameter(0, contest.getPk())
				.setParameter(1, municipality.getPk())
				.getResultList();
	}

	public List<VoteCountDigest> findDigestsByContestAndCountyReportingAreaAndMunicipalityCountingArea(Contest contest, County county, Municipality municipality) {
		return getEm()
				.createNamedQuery("VoteCount.findDigestsByContestAndCountyReportingAreaAndMunicipalityCountingArea", VoteCountDigest.class)
				.setParameter(0, contest.getPk())
				.setParameter(1, county.getPk())
				.setParameter(2, municipality.getPk())
				.getResultList();
	}

	public List<VoteCountDigest> findDigestsByContestAndMunicipalityCountingArea(Contest contest, Municipality municipality) {
		return getEm()
				.createNamedQuery("VoteCount.findDigestsByContestAndMunicipalityCountingArea", VoteCountDigest.class)
				.setParameter(0, contest.getPk())
				.setParameter(1, municipality.getPk())
				.getResultList();
	}
}
