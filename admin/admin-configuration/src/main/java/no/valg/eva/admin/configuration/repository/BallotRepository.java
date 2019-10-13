package no.valg.eva.admin.configuration.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.configuration.model.ballot.PartyData;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Locale;

@Default
@ApplicationScoped
public class BallotRepository extends BaseRepository {
	private static final String PK = "pk";

	public BallotRepository() {
	}

	public BallotRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public Ballot createBallot(UserData userData, Ballot ballot) {
		return createEntity(userData, ballot);
	}

	public Ballot updateBallot(UserData userData, Ballot ballot) {
		return updateEntity(userData, ballot);
	}

	public Ballot findBallotByPk(Long ballotPk) {
		return findEntityByPk(Ballot.class, ballotPk);
	}

	public void deleteBallot(UserData userData, Ballot ballot) {
		deleteEntity(userData, Ballot.class, ballot.getPk());
	}

	public List<Ballot> findByContest(Long contestPk) {
		return getEm()
				.createNamedQuery("Ballot.findByContest", Ballot.class)
				.setParameter(PK, contestPk)
				.getResultList();
	}

	public List<Ballot> findApprovedByContest(Long contestPk) {
		return getEm()
				.createNamedQuery("Ballot.findApprovedByContest", Ballot.class)
				.setParameter(PK, contestPk)
				.getResultList();
	}

	public Ballot findByContestAndId(Long contestPk, String ballotId) {
		return getEm()
				.createNamedQuery("Ballot.findByContestAndId", Ballot.class)
				.setParameter(PK, contestPk)
				.setParameter("id", ballotId)
				.getSingleResult();
	}

	public Long findPkByContestAndId(Long contestPk, String ballotId) {
		return getEm()
				.createNamedQuery("Ballot.findPkByContestAndId", Long.class)
				.setParameter(PK, contestPk)
				.setParameter("id", ballotId)
				.getSingleResult();
	}

	public BallotStatus findBallotStatusById(int id) {
		return super.findEntityById(BallotStatus.class, id);
	}

	public Ballot createNewBallot(Contest contest, String ballotId, Locale locale, int ballotStatus) {
		Ballot newBallot = new Ballot(contest, ballotId, false);
		newBallot.setLocale(locale);
		newBallot.setBallotStatus(findBallotStatusById(ballotStatus));
		return newBallot;
	}

	public Ballot findByPk(Long pk) {
		return super.findEntityByPk(Ballot.class, pk);
	}
	
	public List<PartyData> partiesForContest(Contest contest) {
		return getEm()
				.createNamedQuery("Ballot.partiesQuery", PartyData.class)
				.setParameter(1, contest.getPk())
				.getResultList();
	}

	public List<Ballot> findBallotsWithoutVotes(String voteCountId) {
		return getEm()
				.createNamedQuery("Ballot.findBallotsWithoutVotesByVoteCount", Ballot.class)
				.setParameter(1, voteCountId)
				.getResultList();
	}
}
