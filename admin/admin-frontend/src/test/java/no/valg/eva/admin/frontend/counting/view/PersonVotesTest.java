package no.valg.eva.admin.frontend.counting.view;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CandidateRef;

import org.testng.annotations.Test;

public class PersonVotesTest {

	private static final Candidate CANDIDATE_ONE = new Candidate("Candidate One", new CandidateRef(1L), "Silly Party", 1);
    public static final int EXPECTED = 3;

    static {
        CANDIDATE_ONE.setPersonalVote(true);
    }
    private static final Candidate CANDIDATE_TWO = new Candidate("Candidate Two", new CandidateRef(2L), "Silly Party", 2);
    private static final Candidate CANDIDATE_THREE = new Candidate("Candidate Three", new CandidateRef(3L), "Silly Party", 3);

	@Test
	public void personVotes_shouldHaveOneVoteInitially() throws Exception {
		PersonVotes personVotes = new PersonVotes(newArrayList(CANDIDATE_ONE, CANDIDATE_TWO, CANDIDATE_THREE), newHashSet(CANDIDATE_ONE));

        assertThat(personVotes.getCandidatesForPersonVotes().size()).isEqualTo(EXPECTED);
        assertThat(personVotes.getCandidatesForPersonVotes().iterator().next().hasPersonalVote()).isTrue();
	}
}
