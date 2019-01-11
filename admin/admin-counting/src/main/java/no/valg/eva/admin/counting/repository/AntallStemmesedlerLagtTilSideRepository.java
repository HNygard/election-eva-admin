package no.valg.eva.admin.counting.repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.counting.domain.model.AntallStemmesedlerLagtTilSide;

public class AntallStemmesedlerLagtTilSideRepository extends BaseRepository {
	@SuppressWarnings("unused")
	public AntallStemmesedlerLagtTilSideRepository() {
		// påkrevd av CDI
	}

	public AntallStemmesedlerLagtTilSideRepository(EntityManager entityManager) {
		// brukes til test
		super(entityManager);
	}

	public AntallStemmesedlerLagtTilSide create(UserData userData, AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide) {
		return createEntity(userData, antallStemmesedlerLagtTilSide);
	}

	public AntallStemmesedlerLagtTilSide findByMunicipalityAndElectionGroup(Municipality municipality, ElectionGroup electionGroup) {
		try {
			return getEm()
					.createNamedQuery("AntallStemmesedlerLagtTilSide.findByMunicipalityAndElectionGroup", AntallStemmesedlerLagtTilSide.class)
					.setParameter("municipalityPk", municipality.getPk())
					.setParameter("electionGroupPk", electionGroup.getPk())
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public AntallStemmesedlerLagtTilSide findByMunicipalityAndContest(Municipality municipality, Contest contest) {
		try {
			return getEm()
					.createNamedQuery("AntallStemmesedlerLagtTilSide.findByMunicipalityAndContest", AntallStemmesedlerLagtTilSide.class)
					.setParameter("municipalityPk", municipality.getPk())
					.setParameter("contestPk", contest.getPk())
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public boolean isAntallStemmerLagtTilSide(Municipality municipality, ElectionGroup electionGroup) {
		try {
			return !getEm()
					.createNamedQuery("AntallStemmesedlerLagtTilSide.findByMunicipalityAndElectionGroup", AntallStemmesedlerLagtTilSide.class)
					.setParameter("municipalityPk", municipality.getPk())
					.setParameter("electionGroupPk", electionGroup.getPk())
					.getResultList()
					.isEmpty();
		} catch (NoResultException e) {
			return false;
		}
	}
}
