package no.valg.eva.admin.configuration.repository;

import static no.valg.eva.admin.configuration.domain.model.BallotStatus.BallotStatusValue.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.List;

import javax.persistence.RollbackException;

import no.evote.model.views.CandidateAudit;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.ObjectAssert;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)

public class CandidateRepositoryTest extends AbstractJpaTestBase {

	private static final int NO_OF_CANDIDATES_ON_BALLOTS_OTHER_THAN_AP_IN_OSTFOLD = 301;
	private static final String ELECTION_PATH_OSTFOLD = "200701.01.01.000001";
	private static final String BALLOT_ID_AP = "A";
	private CandidateRepository candidateRepository;
	private BallotRepository ballotRepository;
	private UserData userDataMock;

	@BeforeMethod(alwaysRun = true)
	public void init() {
		candidateRepository = new CandidateRepository(getEntityManager());
		ballotRepository = new BallotRepository(getEntityManager());
		userDataMock = mock(UserData.class, RETURNS_DEEP_STUBS);
	}

	@Test
	public void updateCandidates_moveTopToBottom_success() throws Exception {
		List<Candidate> list = candidateRepository.findCandidateByBallotAndDisplayOrderRange(5L, 1, 3);
		// Move top to bottom and reorder
		list.add(list.remove((0)));
		int count = 1;
		for (Candidate c : list) {
			c.setDisplayOrder(count++);
			c.setBaselineVotes(count == 1);
		}

		List<Candidate> result = candidateRepository.updateCandidates(list);
		getEntityManager().flush();

		ObjectAssert.assertThat(result.size()).isEqualTo(list.size());
	}

	@Test(expectedExceptions = RollbackException.class)
	public void updateCandidates_withInvalidDisplayOrder_shouldFailWithConstraintException() throws Exception {
		List<Candidate> list = candidateRepository.findCandidateByBallotAndDisplayOrderRange(5L, 1, 3);
		for (Candidate c : list) {
			c.setDisplayOrder(1);
		}
	
		candidateRepository.updateCandidates(list);
		getEntityManager().getTransaction().commit(); // Try to commit. Should fail.
	}

	@Test
	public void findCandidatesForOtherBallotsInSameContest_whenThereAreSeveralBallots_returnsCandidatesInTheOtherBallots_butOnlyFromApprovedBallots() {
		Long ballotPk = findBallotPkForApInOstfold();

		List<Candidate> candidatesInOtherContests = candidateRepository.findCandidatesForOtherApprovedBallotsInSameContest(ballotPk);

		assertThat(candidatesInOtherContests.size()).isEqualTo(NO_OF_CANDIDATES_ON_BALLOTS_OTHER_THAN_AP_IN_OSTFOLD);
		Ballot ballotOfFirstCandidate = ballotRepository.findByPk(candidatesInOtherContests.get(0).getBallot().getPk());
		ballotOfFirstCandidate.setBallotStatus(ballotRepository.findBallotStatusById(PENDING.getId()));
		ballotRepository.updateBallot(userDataMock, ballotOfFirstCandidate);
		List<Candidate> candidatesInOtherContestsAfterOneBallotWasUnapproved = candidateRepository.findCandidatesForOtherApprovedBallotsInSameContest(ballotPk);
		assertThat(candidatesInOtherContestsAfterOneBallotWasUnapproved.size()).isLessThan(candidatesInOtherContests.size());
	}

	@Test
	public void getCandidateAuditByBallot_returnsCandidateAuditListWithoutThrowingClassCastException() {
		Long ballotPk = findBallotPkForApInOstfold();

		List<CandidateAudit> candidateAuditByBallotList = candidateRepository.getCandidateAuditByBallot(ballotPk);

		assertThat(candidateAuditByBallotList).isNotEmpty();
	}

	private Long findBallotPkForApInOstfold() {
		Integer ballotPk = (Integer) getEntityManager().createNativeQuery("select b.ballot_pk from admin.ballot b "
			+ " left join admin.mv_election mve on b.contest_pk = mve.contest_pk"
			+ " where mve.election_path = '" + ELECTION_PATH_OSTFOLD + "' and b.ballot_id = '" + BALLOT_ID_AP + "'").getSingleResult();
		return (long) ballotPk;
	}

	@Test(dataProvider = "candidatesTestData")
	public void findCandidatesMatchingName_givenAreaPathAndNameLine_findsAllSimilarNamesFromBallotsInArea(String areaPath,
																										  String nameToSearchFor, int expectedNumberOfMatches) {
		List<Candidate> actualCandidates = candidateRepository.findCandidatesMatchingName(AreaPath.from(areaPath), nameToSearchFor);
		assertThat(actualCandidates.size()).isEqualTo(expectedNumberOfMatches);
	}
	
	@DataProvider
	private Object[][] candidatesTestData() {
		return new Object[][] {
				{ "200701.47.01.0101", "Inger Torp", 1 },		
				{ "200701.47.01.0101", "Per", 13 }		
		};
	}
}

