package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CandidateRef;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.testng.annotations.Test;


public class WriteInAutoCompleteTest {

	private static final String PARTY_NAME_ID = "@party[something].name";
	private static final String PARTY_NAME = "SomethingParty";
	private static final String CANDIDATE_NAME = "CandidateName";

	@Test
	public void constructor_whenSomeWriteInsAreAlreadySpecified_theyAreInitializedIntoTheWrappedList() {
		List<Candidate> allWriteInCandidates = buildAllWriteInCandidates();
		List<Candidate> existingWriteIns = new ArrayList<>();
		Candidate candidatePerHansen = buildCandidatePerHansen();
		existingWriteIns.add(candidatePerHansen);
		int maxWriteIns = 3;
		
		WriteInAutoComplete writeInAutoComplete = new WriteInAutoComplete();
		writeInAutoComplete.fillWriteInAutoComplete(maxWriteIns, allWriteInCandidates, existingWriteIns, null);

		assertThat(writeInAutoComplete.wrappedWriteIns.size()).isEqualTo(maxWriteIns);
		assertThat(writeInAutoComplete.wrappedWriteIns.get(0).getValue()).isEqualTo(candidatePerHansen);
		assertThat(writeInAutoComplete.wrappedWriteIns.get(1).getValue()).isEqualTo(null);
	}
	
	@Test
	public void filterCandidates_whenASubstringHasBeenEntered_returnsMatchingCandidates() throws Exception {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallot();
		String filterString = "Hans";

		List<Candidate> candidates = writeInAutoComplete.filterCandidates(filterString);

		assertThat(candidates.size()).isEqualTo(2);
		assertThat(candidates).contains(buildCandidatePerHansen());
		assertThat(candidates).contains(buildCandidatePetraHansen());
	}

	@Test
	public void filterCandidates_whenASubstringWithDifferentCaseHasBeenEntered_returnsMatchingCandidates() throws Exception {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallot();
		String filterString = "ole";

		List<Candidate> candidates = writeInAutoComplete.filterCandidates(filterString);

		assertThat(candidates.size()).isEqualTo(1);
		assertThat(candidates).contains(buildCandidateOleNilsen());
	}
	
	@Test
	public void getWriteInsForCurrentBallot_whenSomeCandidatesHaveBeenSelected_returnsTheCandidatesInANewList() {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallot();
		Candidate candidatePerHansen = buildCandidatePerHansen();
		addCandidateToTheWrappedList(candidatePerHansen, writeInAutoComplete);
		
		Set<Candidate> mappedCandidates = writeInAutoComplete.getWriteInsFromAutoComplete();
		
		assertThat(mappedCandidates.size()).isEqualTo(1);
		assertThat(mappedCandidates.iterator().next()).isEqualTo(candidatePerHansen);
	}
	
	@Test(expectedExceptions = IllegalStateException.class)
	public void getWriteInsForCurrentBallot_whenAWriteInDoesNotExistInTheCandidates_anExceptionIsThrown() {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallot();
		Candidate candidateNonExistent = buildCandidateNotOnTheListOfCandidates();
		addCandidateToTheWrappedList(candidateNonExistent, writeInAutoComplete);
		
		writeInAutoComplete.getWriteInsFromAutoComplete();
	}
	
	private WriteInAutoComplete buildWriteInsForBallot(int maxWriteIns) {
		List<Candidate> allWriteInCandidates = buildAllWriteInCandidates();

		WriteInAutoComplete writeInAutoComplete = new WriteInAutoComplete();
		writeInAutoComplete.fillWriteInAutoComplete(maxWriteIns, allWriteInCandidates, new ArrayList<Candidate>(), null);
		return writeInAutoComplete;
	}

	private List<Candidate> buildAllWriteInCandidates() {
		List<Candidate> candidates = new ArrayList<>();
		candidates.add(buildCandidatePerHansen());
		candidates.add(buildCandidateOleNilsen());
		candidates.add(buildCandidatePetraHansen());
		return candidates;
	}

	private Candidate buildCandidatePetraHansen() {
		return new Candidate("Petra Hansen", new CandidateRef(1L), "AP", 0);
	}

	private Candidate buildCandidateOleNilsen() {
		return new Candidate("Ole Nilsen", new CandidateRef(2L), "AP", 0);
	}

	private Candidate buildCandidatePerHansen() {
		return new Candidate("Per Hansen", new CandidateRef(3L), "H", 0);
	}

	private Candidate buildCandidateNotOnTheListOfCandidates() {
		return new Candidate("Finnes-Ikke I Liste", new CandidateRef(4L), "FRP", 0);
	}
	
	private WriteInAutoComplete buildWriteInsForBallot() {
		return buildWriteInsForBallot(2);
	}

	private void addCandidateToTheWrappedList(Candidate candidate, WriteInAutoComplete writeInAutoComplete) {
		writeInAutoComplete.wrappedWriteIns.get(1).setValue(candidate);
	}

	@Test
	public void getCandidateDisplayName_whenNameAndPartyNameExists_returnsANicelyFormattedString() {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallotWithMessageProvider();
		Candidate candidate = buildCandidate();
		String expectedCandidateDisplayName = CANDIDATE_NAME + " (" + PARTY_NAME + ")";

		String candidateDisplayName = writeInAutoComplete.getCandidateDisplayName(candidate);

		assertThat(candidateDisplayName).isEqualTo(expectedCandidateDisplayName);
	}

	@Test
	public void getCandidateDisplayName_whenNameAndPartyIsNotSet_returnsEmptyString() {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallotWithMessageProvider();
		Candidate candidate = buildCandidateWithCandidateRefOnly();
		String expectedCandidateDisplayName = "";

		String candidateDisplayName = writeInAutoComplete.getCandidateDisplayName(candidate);

		assertThat(candidateDisplayName).isEqualTo(expectedCandidateDisplayName);
	}

	@Test
	public void getCandidateDisplayName_whenCandidateIsNull_returnsEmptyString() {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallotWithMessageProvider();
		String expectedCandidateDisplayName = "";

		String candidateDisplayName = writeInAutoComplete.getCandidateDisplayName(null);

		assertThat(candidateDisplayName).isEqualTo(expectedCandidateDisplayName);
	}

	private WriteInAutoComplete buildWriteInsForBallotWithMessageProvider() {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallot();
		writeInAutoComplete.messageProvider = mock(MessageProvider.class);
		when(writeInAutoComplete.messageProvider.get(PARTY_NAME_ID)).thenReturn(PARTY_NAME);
		return writeInAutoComplete;
	}

	private Candidate buildCandidate() {
		return new Candidate(CANDIDATE_NAME, new CandidateRef(1L), PARTY_NAME_ID, 0);
	}

	private Candidate buildCandidateWithCandidateRefOnly() {
		return new Candidate(null, new CandidateRef(1L), null, 0);
	}
	
	@Test
	public void isWriteInsEnabled_whenMoreThanZeroWriteInsArePermitted_returnsTrue() {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallot();
		
		boolean writeInsEnabled = writeInAutoComplete.isWriteInsEnabled();
		
		assertThat(writeInsEnabled).isTrue();
	}
	
	@Test
	public void isWriteInsEnabled_whenZeroWriteInsArePermitted_returnsFalse() {
		WriteInAutoComplete writeInAutoComplete = buildWriteInsForBallot();
		writeInAutoComplete.wrappedWriteIns.clear();
		
		boolean writeInsEnabled = writeInAutoComplete.isWriteInsEnabled();
		
		assertThat(writeInsEnabled).isFalse();
	}
}

