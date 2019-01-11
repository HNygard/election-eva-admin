package no.valg.eva.admin.counting.repository;

import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;

public class ModifiedBallotBatchTestSupport extends AbstractJpaTestBase {
	protected static final int A_LOT = 500;
	protected static final int NEARLY_ALL = 497;
	protected static final int WANTED_MODIFIED_BALLOTS_IN_BATCH = 10;
	protected static final int SOME = 3;
	protected UserData userData;
	protected ContestReportRepository contestReportRepository;
	protected CountingCodeValueRepository countingCodeValueRepository;
	protected ElectionEventRepository electionEventRepository;
	protected VoteCountCategoryRepository voteCountCategoryRepository;
	protected BallotRepository ballotRepository;
	protected MvAreaRepository mvAreaRepository;
	protected int contestPk;
	protected BallotCount ballotCount;
	protected String ballotId;
	protected ModifiedBallotBatchRepository modifiedBallotBatchRepository;
	protected Operator operator;
	protected GenericTestRepository genericTestRepository;
	private OperatorRepository operatorRepository;
	private ElectionEvent electionEvent;

	protected void setupTestData() {
		electionEvent = electionEventRepository.findAll().get(0);

		operator = operatorRepository.findAll().get(0);

		userData = new UserData();
		OperatorRole operatorRole = new OperatorRole();
		Role udRole = new Role();
		udRole.setUserSupport(false);
		udRole.setElectionEvent(electionEvent);
		operatorRole.setRole(udRole);
		operator.setElectionEvent(electionEvent);
		operatorRole.setOperator(operator);
		userData.setOperatorRole(operatorRole);

		contestPk = (int) getEntityManager().createNativeQuery("select min(contest_pk) from contest").getSingleResult();

		ballotId = "A";
		ballotCount = createBallotCount(ballotId, contestPk);
		ballotCount.setModifiedBallots(A_LOT);
	}

	protected CastBallot createModifiedCastBallot(String id, BallotCount ballotCount) {
		CastBallot castBallot = new CastBallot(ballotCount, MODIFIED);
		castBallot.setId(id);
		return castBallot;
	}

	protected BallotCount createBallotCount(String ballotId, long contestPk) {
		Ballot ballot = ballotRepository.findByContest(contestPk).get(0);
		BallotCount ballotCount = new BallotCount();
		ballotCount.setVoteCount(createVoteCount(ballotId));
		ballotCount.setBallot(ballot);
		return genericTestRepository.createEntity(ballotCount);
	}

	private VoteCount createVoteCount(String ballotId) {
		VoteCount voteCount = new VoteCount();
		voteCount.setId(ballotId);
		voteCount.setMvArea(mvAreaRepository.findRoot(electionEvent.getPk()));
		voteCount.setApprovedBallots(0);
		voteCount.setCountQualifier(countingCodeValueRepository.findCountQualifierById("F"));
		voteCount.setVoteCountCategory(voteCountCategoryRepository.findById("VO"));
		voteCount.setVoteCountStatus(countingCodeValueRepository.findVoteCountStatusById(0));
		int contestReportPk = (int) getEntityManager().createNativeQuery("select min(contest_report_pk) from contest_report").getSingleResult();
		voteCount.setContestReport(contestReportRepository.findByPk(contestReportPk));
		voteCount = genericTestRepository.createEntity(voteCount);
		return voteCount;
	}

	public void setUpRepositories() {
		contestReportRepository = new ContestReportRepository(getEntityManager());
		countingCodeValueRepository = new CountingCodeValueRepository(getEntityManager());
		mvAreaRepository = new MvAreaRepository(getEntityManager());
		electionEventRepository = new ElectionEventRepository(getEntityManager());
		voteCountCategoryRepository = new VoteCountCategoryRepository(getEntityManager());
		ballotRepository = new BallotRepository(getEntityManager());
		modifiedBallotBatchRepository = new ModifiedBallotBatchRepository(getEntityManager());
		operatorRepository = new OperatorRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());
		contestReportRepository = new ContestReportRepository(getEntityManager());
	}
}
