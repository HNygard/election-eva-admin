package no.valg.eva.admin.frontend.counting.ctrls;

import com.google.common.base.Predicate;
import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;
import no.valg.eva.admin.common.counting.service.ModifiedBallotService;
import org.mockito.ArgumentMatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static com.google.common.collect.Iterables.tryFind;
import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/*import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;*/


public class RegisterModifiedBallotsControllerTest extends ModifiedBallotsNavigationControllerTest {
    private static final int ONE_FOR_FIRST_GOTO_NEXT_AND_ONE_FOR_FINISH = 2;
    private static final int ONE_FOR_GOTO_PREVIOUS = 1;
	private RegisterModifiedBallotController registerModifiedBallotController;

	@Override
	@BeforeMethod
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void initializeMocksForController() throws Exception {
		super.initializeMocksForController();
		registerModifiedBallotController = initializeMocks(RegisterModifiedBallotController.class);
	}

	@Override
	protected ModifiedBallotsNavigationController getControllerUnderTest() {
		return registerModifiedBallotController;
	}

	@Test
    public void tickCandidateOneAndGotoNextBallot_shouldUpdateModifiedBallotWithRightCandidate() {
		start();

		addPersonVoteForCandidate(0);

		getControllerUnderTest().gotoNextBallot();

		verify(getInjectMock(ModifiedBallotService.class)).update(any(UserData.class), thatModifiedBallotIsUpdatedWithAPersonVoteFor(personVoteCandidate1));
	}

	@Test
    public void tickCandidateOneAndGotoPreviousBallot_shouldUpdateModifiedBallotWithRightCandidate() {
		start();

		addPersonVoteForCandidate(1);

		getControllerUnderTest().gotoPreviousBallot();

		verify(getInjectMock(ModifiedBallotService.class)).update(any(UserData.class), thatModifiedBallotIsUpdatedWithAPersonVoteFor(personVoteCandidate2));
	}

	@Test
    public void tickCandidateThreeAndFinish_shouldUpdateModifiedBallotWithRightCandidate() {
		start();

		addPersonVoteForCandidate(2);

		getControllerUnderTest().finished();

		verify(getInjectMock(ModifiedBallotService.class)).update(any(UserData.class), thatModifiedBallotIsUpdatedWithAPersonVoteFor(personVoteCandidate3));
	}

	@Test
    public void selectingWriteIn_shouldUpdateBallotWithRightCandidate() {
		start();

		addWriteInForCandidate(0, writeInCandidate1);

		getControllerUnderTest().finished();

		verify(getInjectMock(ModifiedBallotService.class)).update(any(UserData.class), thatModifiedBallotIsUpdatedWithAWriteInVoteFor(writeInCandidate1));
	}

	@Test
    public void selectingWriteIn_thenGotoNextAndBackShouldUpdateBallotWithRightCandidate() {
		start();

		addWriteInForCandidate(0, writeInCandidate1);
		gotoNext();

		gotoPrevious();

		getControllerUnderTest().finished();

		verify(getInjectMock(ModifiedBallotService.class), times(ONE_FOR_FIRST_GOTO_NEXT_AND_ONE_FOR_FINISH))
				.update(any(UserData.class), thatModifiedBallotIsUpdatedWithAWriteInVoteFor(writeInCandidate1));

		verify(getInjectMock(ModifiedBallotService.class), times(ONE_FOR_GOTO_PREVIOUS))
				.update(any(UserData.class), withPersonAndWriteInVotes(0, 0));
	}

	private ModifiedBallot withPersonAndWriteInVotes(final int expectedNumberOfPersonVotes, final int expectedNumberOfWriteInVotes) {
		final int[] actualNumberOfPersonVotes = new int[1];
		final int[] actualNumberOfWriteInVotes = new int[1];
        return argThat((new ArgumentMatcher<ModifiedBallot>() {
			@Override
            public boolean matches(ModifiedBallot item) {
                actualNumberOfPersonVotes[0] = item.personalVotes().size();
                actualNumberOfWriteInVotes[0] = item.getWriteIns().size();
				return actualNumberOfPersonVotes[0] == expectedNumberOfPersonVotes && actualNumberOfWriteInVotes[0] == expectedNumberOfWriteInVotes;
			}

            public String toString() {
                return format(
						"ModifiedBallot with %d person vote(s) and %d write in vote(s), but had %d and %d respectively",
						expectedNumberOfPersonVotes,
						expectedNumberOfWriteInVotes,
						actualNumberOfPersonVotes[0],
                        actualNumberOfWriteInVotes[0]);
			}
		}));
	}

	private ModifiedBallot thatModifiedBallotIsUpdatedWithAPersonVoteFor(final Candidate candidate) {
        return argThat(item -> item.personalVotes().contains(candidate));
	}

	private ModifiedBallot thatModifiedBallotIsUpdatedWithAWriteInVoteFor(final Candidate candidate) {
        return argThat(item -> item.getWriteIns().size() == 1 && item.getWriteIns().contains(candidate));
	}

	protected void addPersonVoteForCandidate(int candidateIndex) {
        new ArrayList<>(getControllerUnderTest().getPersonVotes().getCandidatesForPersonVotes()).get(candidateIndex).setPersonalVote(true);
	}

	private void addWriteInForCandidate(int position, final Candidate candidate) {
		getControllerUnderTest().getWriteInAutoComplete().getWrappedWriteIns().get(position)
				.setValue(tryFind(ballot.getCandidatesForWriteIn(), thatEquals(candidate)).get());
	}

	private Predicate<Candidate> thatEquals(final Candidate candidate) {
		return new Predicate<Candidate>() {
			@Override
			public boolean apply(Candidate input) {
				return input.equals(candidate);
			}
		};
	}
}

