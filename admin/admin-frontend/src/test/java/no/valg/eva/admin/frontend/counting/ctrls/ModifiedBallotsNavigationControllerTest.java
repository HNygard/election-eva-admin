package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.model.election.ModifiedBallotConfiguration;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.modifiedballots.Ballot;
import no.valg.eva.admin.common.counting.model.modifiedballots.BallotId;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CandidateRef;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallots;
import no.valg.eva.admin.common.counting.service.ModifiedBallotService;
import no.valg.eva.admin.frontend.counting.view.WriteInAutoComplete;
import org.mockito.ArgumentMatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static no.valg.eva.admin.frontend.counting.ctrls.CreateModifiedBallotBatchController.BALLOT_COUNT_REF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;


public class ModifiedBallotsNavigationControllerTest extends BaseFrontendTest {
    private static final String BALLOT_COUNT = "1";
    private static final int A_LARGE_NUMBER_OF_TIMES = 100;
    private static final int A_HANDFUL_TIMES = 10;
    private static final Integer NO_OF_PERSONAL_VOTE_CANDIDATES = 3;
    private static final int MAX_RENUMBER = 10;
    private final BallotId ballotId = new BallotId("200");
	protected Ballot ballot = new Ballot(ballotId, createModifiedBallotConfiguration(MAX_RENUMBER));

	private ModifiedBallotConfiguration createModifiedBallotConfiguration(int maxRenumber) {
		return new ModifiedBallotConfiguration(true, true, true, true, true, 1, maxRenumber);
	}

	protected ModifiedBallotsNavigationController modifiedBallotsNavigationController;
	protected ModifiedBallot modifiedBallot1;
	protected ModifiedBallot modifiedBallot2;
	protected ModifiedBallot modifiedBallot3;
	protected Candidate personVoteCandidate1 = new Candidate("Candidate One", new CandidateRef(1), "PARTY", 1);
	protected Candidate personVoteCandidate2 = new Candidate("Candidate Two", new CandidateRef(2), "PARTY", 2);
	protected Candidate personVoteCandidate3 = new Candidate("Candidate Three", new CandidateRef(3), "PARTY", 3);
	protected Candidate writeInCandidate1 = new Candidate("WriteIn Candidate One", new CandidateRef(4), "WRITE IN PARTY", 1);
	protected Candidate writeInCandidate2 = new Candidate("WriteIn Candidate Two", new CandidateRef(5), "WRITE IN PARTY", 2);
	protected Candidate writeInCandidate3 = new Candidate("WriteIn Candidate Three", new CandidateRef(6), "WRITE IN PARTY", 3);
	protected ModifiedBallots modifiedBallots;
	private BatchId batchId = new BatchId("100_1_2");

	@BeforeMethod
	public void setUp() throws Exception {
		initializeMocksForController();
		modifiedBallot1 = modifiedBallot(1);
		modifiedBallot2 = modifiedBallot(2);
		modifiedBallot3 = modifiedBallot(3);
		ballot.addForPersonalVote(personVoteCandidate1);
		ballot.addForPersonalVote(personVoteCandidate2);
		ballot.addForPersonalVote(personVoteCandidate3);
		ballot.getCandidatesForWriteIn().addAll(of(writeInCandidate1, writeInCandidate2, writeInCandidate3));
		modifiedBallots = new ModifiedBallots(newArrayList(modifiedBallot1, modifiedBallot2, modifiedBallot3), ballot);
		getServletContainer().setRequestParameter(BALLOT_COUNT_REF, BALLOT_COUNT);
		when(getInjectMock(ModifiedBallotService.class).modifiedBallotsFor(any(UserData.class), any(BallotCountRef.class),
                any())).thenReturn(modifiedBallots);
		when(getInjectMock(ModifiedBallotService.class).load(any(UserData.class), argThat(modifiedBallotMatcher(modifiedBallot1)))).thenReturn(modifiedBallot1,
				modifiedBallot1);
		when(getInjectMock(ModifiedBallotService.class).load(any(UserData.class), argThat(modifiedBallotMatcher(modifiedBallot2)))).thenReturn(modifiedBallot2,
				modifiedBallot2);
		when(getInjectMock(ModifiedBallotService.class).load(any(UserData.class), argThat(modifiedBallotMatcher(modifiedBallot3)))).thenReturn(modifiedBallot3,
				modifiedBallot3);
		getControllerUnderTest().writeInAutoComplete = new WriteInAutoComplete();
	}

	protected void initializeMocksForController() throws Exception {
		modifiedBallotsNavigationController = initializeMocks(ModifiedBallotsNavigationController.class);
	}

	@Test
    public void when_onPageUpdate_modifiedBallotsAreLoaded() {
		start();
		assertThat(getControllerUnderTest().getCurrentModifiedBallot()).isEqualTo(modifiedBallot1);
	}

	@Test
    public void gotoNextBallot_nextBallotIsLoaded() {
		start();
		assertThat(getControllerUnderTest().getCurrentModifiedBallot()).isEqualTo(modifiedBallot1);
		gotoNextAndCheckThatIs(modifiedBallot2);
		gotoNextAndCheckThatIs(modifiedBallot3);
	}

	@Test
    public void excessiveGotoNextBallot_stopsAtLastBallot() {
		start();
		for (int i = 0; i < A_LARGE_NUMBER_OF_TIMES; i++) {
			gotoNext();
		}
		gotoNextAndCheckThatIs(modifiedBallot3);
	}

	@Test
    public void gotoPreviousBallot_previousBallotIsLoaded() {
		start();
		gotoNext();
		gotoNext();
		gotoPreviousAndCheckThatIs(modifiedBallot2);
	}

	@Test
    public void excessiveGotoPreviousBallot_stopsAtFirstBallot() {
		start();
		for (int i = 0; i < A_HANDFUL_TIMES; i++) {
			gotoNext();
		}
		for (int i = 0; i < A_LARGE_NUMBER_OF_TIMES; i++) {
			gotoPrevious();
		}
		gotoPreviousAndCheckThatIs(modifiedBallot1);
	}

	@Test
	public void getNoOfRenumberPositions_returnsNumberOfCandidates() {
		start();
		assertThat(getControllerUnderTest().getNoOfRenumberPositions()).isEqualTo(NO_OF_PERSONAL_VOTE_CANDIDATES);
	}
    
	@Test
	public void renumberPositions_maxRenumberLimitSetToValueHigherThanNoOfCandidates_returnsListOfPositions() {
        ModifiedBallotsNavigationController controller = new ModifiedBallotsNavigationController();
        Ballot ballot = new Ballot(ballotId, createModifiedBallotConfiguration(MAX_RENUMBER));
        ballot.addForPersonalVote(personVoteCandidate1);
        ballot.addForPersonalVote(personVoteCandidate2);
        ballot.addForPersonalVote(personVoteCandidate3);
        controller.setModifiedBallots(new ModifiedBallots(newArrayList(modifiedBallot1, modifiedBallot2, modifiedBallot3),
                ballot));

		assertThat(controller.getRenumberPositions())
                .isEqualTo(Arrays.asList("1", "2", NO_OF_PERSONAL_VOTE_CANDIDATES.toString()));
	}

	@Test
    public void renumberPositions_maxRenumberLimitSetToValueLowerThanNoOfCandidates_returnsPositionsUpToMaxRenumberLimit() {
        
        ModifiedBallotsNavigationController controller = new ModifiedBallotsNavigationController();
        Ballot ballot = new Ballot(ballotId, createModifiedBallotConfiguration(2));
        ballot.addForPersonalVote(personVoteCandidate1);
        ballot.addForPersonalVote(personVoteCandidate2);
        ballot.addForPersonalVote(personVoteCandidate3);
        controller.setModifiedBallots(new ModifiedBallots(newArrayList(modifiedBallot1, modifiedBallot2, modifiedBallot3),
                ballot));

        assertThat(controller.getRenumberPositions()).isEqualTo(Arrays.asList("1", "2"));
	}

    @Test
    public void disableRenumberButton_positionIsEqualToCandidateDisplayOrder_returnsTrue() {
        start();
        assertThat(getControllerUnderTest().disableRenumberButton("1", personVoteCandidate1)).isTrue();
    }

    @Test
    public void disableRenumberButton_positionIsNotEqualToCandidateDisplayOrder_returnsFalse() {
        start();
        assertThat(getControllerUnderTest().disableRenumberButton("1", personVoteCandidate2)).isFalse();
    }

	protected void gotoPrevious() {
		getControllerUnderTest().gotoPreviousBallot();
	}

	private void gotoNextAndCheckThatIs(ModifiedBallot expectedModifiedBallot) {
		getControllerUnderTest().gotoNextBallot();
		assertThat(getControllerUnderTest().getCurrentModifiedBallot()).isEqualTo(expectedModifiedBallot);
	}

	protected void gotoNext() {
		getControllerUnderTest().gotoNextBallot();
	}

	private void gotoPreviousAndCheckThatIs(ModifiedBallot expectedModifiedBallot) {
		getControllerUnderTest().gotoPreviousBallot();
		assertThat(getControllerUnderTest().getCurrentModifiedBallot()).isEqualTo(expectedModifiedBallot);
	}

	private ModifiedBallot modifiedBallot(int serialNumber) {
		return new ModifiedBallot(batchId, serialNumber, "H", ballotId, false);
	}

	protected void start() {
		getControllerUnderTest().onPageUpdate(null);
	}

	protected ModifiedBallotsNavigationController getControllerUnderTest() {
		return modifiedBallotsNavigationController;
	}

	protected void gotoLastModifiedBallot() {
		for (int i = 0; i < 10; i++) {
			gotoNext();
		}
	}

	protected void checkPreviousButtonIsDisabled() {
		assertThat(getControllerUnderTest().isPreviousButtonDisabled()).isTrue();
	}

	protected void checkPreviousButtonIsEnabled() {
		assertThat(getControllerUnderTest().isPreviousButtonDisabled()).isFalse();
	}

	protected void checkNextButtonIsDisabled() {
		assertThat(getControllerUnderTest().isNextButtonDisabled()).isTrue();
	}

	protected void checkNextButtonIsEnabled() {
		assertThat(getControllerUnderTest().isNextButtonDisabled()).isFalse();
	}

	protected void checkFinishedButtonIsDisabled() {
		assertThat(getControllerUnderTest().isFinishedButtonDisabled()).isTrue();
	}

	protected void checkFinishedButtonIsEnabled() {
		assertThat(getControllerUnderTest().isFinishedButtonDisabled()).isFalse();
	}

    private ArgumentMatcher<ModifiedBallot> modifiedBallotMatcher(final ModifiedBallot modifiedBallot) {
        return item -> item != null
                && item.getBallotId().equals(modifiedBallot.getBallotId())
                && item.getSerialNumber() == modifiedBallot.getSerialNumber();
	}

}

