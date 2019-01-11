package no.valg.eva.admin.configuration.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.ProposerRole;

public class ProposerRepository extends BaseRepository {

	private static final String BPK = "bpk";

	protected ProposerRepository(final EntityManager entityManager) {
		super(entityManager);
	}

	@SuppressWarnings("unused")
	private ProposerRepository() {
	}

	public void deleteProposer(UserData userData, Long proposerPk) {
		super.deleteEntity(userData, Proposer.class, proposerPk);
	}

	public Proposer findProposerByPk(Long pk) {
		return super.findEntityByPk(Proposer.class, pk);
	}

	public Proposer findProposerByBallotAndId(Long ballotPk, String id) {
		try {
			final Query query = getEm().createNamedQuery("Proposer.findByBallotAndId").setParameter(BPK, ballotPk).setParameter("id", id);
			return (Proposer) query.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;
		}
	}

	public List<ProposerRole> findSelectiveProposerRoles() {
		TypedQuery<ProposerRole> query = getEm().createNamedQuery("ProposerRole.findNotSingle", ProposerRole.class);
		setCacheHint(query);
		return query.getResultList();
	}

	public ProposerRole findProposerRoleById(String id) {
		return super.findEntityById(ProposerRole.class, id);
	}

	public ProposerRole findProposerRoleByPk(Long pk) {
		return super.findEntityByPk(ProposerRole.class, pk);
	}

	public Proposer createProposer(UserData userData, Proposer proposer) {
		return super.createEntity(userData, proposer);
	}

	public Proposer updateProposer(UserData userData, Proposer proposer) {
		return super.updateEntity(userData, proposer);
	}

	public List<Proposer> findByBallot(Long ballotPk) {
		TypedQuery<Proposer> query = getEm().createNamedQuery("Proposer.findByBallot", Proposer.class).setParameter("pk", ballotPk);
		return query.getResultList();
	}

	public Proposer findByBallotAndOrder(Long ballotPk, int displayOrder) {
		try {
			final Query query = getEm().createNamedQuery("Proposer.findByBallotAndOrder").setParameter(BPK, ballotPk).setParameter("order", displayOrder);
			return (Proposer) query.getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;
		}
	}

	public List<Proposer> findByIdInOtherBallot(String proposerId, Long ballotPk, Long electionPk) {
		TypedQuery<Proposer> query = getEm()
				.createNamedQuery("Proposer.findByIdInOtherBallot", Proposer.class)
				.setParameter("id", proposerId)
				.setParameter(BPK, ballotPk)
				.setParameter("epk", electionPk);
		return query.getResultList();
	}

	public List<Proposer> findProposerByBallotAndDisplayOrderRange(Long ballotPk, int displayOrderFrom, int displayOrderTo) {
		TypedQuery<Proposer> query = getEm().createNamedQuery("Proposer.findProposerByBallotAndDisplayOrderRange", Proposer.class).setParameter(BPK, ballotPk)
				.setParameter("displayOrderFrom", displayOrderFrom).setParameter("displayOrderTo", displayOrderTo);
		return query.getResultList();
	}

	public List<Proposer> updateProposers(List<Proposer> candidates) {
		List<Proposer> updatedEntities = new ArrayList<>();
		for (final Proposer entity : candidates) {
			Proposer updatedEntity = getEm().merge(entity);
			updatedEntities.add(updatedEntity);
		}
		return updatedEntities;
	}

	public List<Proposer> findByBelowDisplayOrder(Long ballotPk, int displayOrder) {
		TypedQuery<Proposer> query = getEm().createNamedQuery("Proposer.findByBelowDisplayOrder", Proposer.class)
				.setParameter("ballotPk", ballotPk)
				.setParameter("displayOrder", displayOrder);
		return query.getResultList();
	}

	public void delete(UserData userData, List<Proposer> proposers) {
		super.deleteEntities(userData, proposers);
	}
}
