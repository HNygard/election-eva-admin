package no.valg.eva.admin.configuration.repository;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.Affiliation;

@Default
@ApplicationScoped
public class AffiliationRepository extends BaseRepository {
	public AffiliationRepository() {
	}

	public AffiliationRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public Affiliation getAffiliationById(String affiliationId, Long contestPk) {
		try {
			final Query query = getEm().createNamedQuery("Affiliation.findByNameAndContest").setParameter("id", affiliationId).setParameter("contestPk", contestPk);
			return (Affiliation) query.getSingleResult();
		} catch (final NoResultException e) {
			return null;
		}
	}

	public Affiliation createAffiliation(UserData userData, Affiliation affiliation) {
		return createEntity(userData, affiliation);
	}

	public Affiliation updateAffiliation(UserData userData, Affiliation affiliation) {
		return updateEntity(userData, affiliation);
	}

	public Affiliation findAffiliationByPk(Long affiliationPk) {
		return findEntityByPk(Affiliation.class, affiliationPk);
	}

	public List<Affiliation> findByContest(Long contestPk) {
		TypedQuery<Affiliation> query = getEm().createNamedQuery("Affiliation.findByContest", Affiliation.class).setParameter("pk", contestPk);
		return query.getResultList();
	}

	public List<Affiliation> findApprovedByContest(Long contestPk) {
		TypedQuery<Affiliation> query = getEm().createNamedQuery("Affiliation.findApprovedByContest", Affiliation.class).setParameter("pk", contestPk);
		return query.getResultList();
	}

	public List<Affiliation> findByBallotStatusAndContest(Long contestPk, int ballotStatus) {
		TypedQuery<Affiliation> query = getEm().createNamedQuery("Affiliation.findByBallotStatusAndContest", Affiliation.class)
				.setParameter("contestPk", contestPk).setParameter("statusId", ballotStatus);
		return query.getResultList();

	}

	public Affiliation findByBallot(Long ballotPk) {
		TypedQuery<Affiliation> query = getEm().createNamedQuery("Affiliation.findByBallot", Affiliation.class).setParameter("ballotPk", ballotPk);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public boolean hasAffiliationPartyId(String partyId, Long electionEventPk) {
		return !findByPartyId(partyId, electionEventPk).isEmpty();
	}

	public List<Affiliation> findByPartyId(String partyId, Long electionEventPk) {
		TypedQuery<Affiliation> query = getEm().createNamedQuery("Affiliation.findByName", Affiliation.class).setParameter("id", partyId)
															.setParameter("electionEventPk", electionEventPk);
		return query.getResultList();
	}

	public List<Affiliation> findAffiliationByContestAndDisplayOrderRange(Long contestPk, int displayOrderFrom, int displayOrderTo) {
		TypedQuery<Affiliation> query = getEm().createNamedQuery("Affiliation.findAffiliationByContestAndDisplayOrderRange", Affiliation.class)
			.setParameter("cpk", contestPk).setParameter("displayOrderFrom", displayOrderFrom).setParameter("displayOrderTo", displayOrderTo);
		return query.getResultList();
	}

	public List<Affiliation> updateAffiliations(List<Affiliation> candidates) {
		List<Affiliation> updatedEntities = new ArrayList<>();
		for (final Affiliation entity : candidates) {
			Affiliation updatedEntity = getEm().merge(entity);
			updatedEntities.add(updatedEntity);
		}
		return updatedEntities;
	}

	public void delete(UserData userData, Long affiliationPk) {
		super.deleteEntity(userData, Affiliation.class, affiliationPk);
	}

	public Affiliation findByPk(Long pk) {
		return super.findEntityByPk(Affiliation.class, pk);
	}
}
