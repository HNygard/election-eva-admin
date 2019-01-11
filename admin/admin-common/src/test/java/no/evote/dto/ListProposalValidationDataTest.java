package no.evote.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class ListProposalValidationDataTest extends MockUtilsTestCase {

	private static final int MIN_PROPOSERS_OLD_PARTY = 2;
	private static final int MIN_PROPOSERS_NEW_PARTY = 5;
	private static final int NO_OF_SIGNED_PROPOSERS = 3;
	private static final LocalDate DATE_OF_BIRTH = new LocalDate(1976, 12, 31);

	@Test
	public void isPartyReadyForApproval_notSufficientNumberOfCandidates_returnsFalse() {
		List<Candidate> candidateList = new ArrayList<>();
		List<Proposer> proposerList = new ArrayList<>();
		ListProposalValidationData listProposalValidationData = new ListProposalValidationData(candidateList, proposerList, null);
		listProposalValidationData.setSufficientNumberOfCandidates(false);

		assertThat(listProposalValidationData.isPartyReadyForApproval()).isFalse();
	}

	@Test
	public void isPartyReadyForApproval_sufficientNumberOfCandidatesButNotSufficientNumberOfProposers_returnsFalse() {
		List<Candidate> candidateList = new ArrayList<>();
		List<Proposer> proposerList = makeProposerListMedTreUtfylteOgTreIkkeUtfylteUnderskrifter();
		Affiliation fakeAffiliation = mock(Affiliation.class, RETURNS_DEEP_STUBS);
		Contest contest = makeContest();
		when(fakeAffiliation.getBallot().getContest()).thenReturn(contest);
		Party party = makeParty(false);
		when(fakeAffiliation.getParty()).thenReturn(party);
		ListProposalValidationData listProposalValidationData = new ListProposalValidationData(candidateList, proposerList, fakeAffiliation);
		listProposalValidationData.setSufficientNumberOfCandidates(true);

		assertThat(listProposalValidationData.isPartyReadyForApproval()).isFalse();
	}

	@Test
	public void isPartyReadyForApproval_sufficientNumberOfCandidatesAndSufficientNumberOfProposersForForenkletBehandling_returnsTrue() {
		List<Candidate> candidateList = new ArrayList<>();
		List<Proposer> proposerList = makeProposerListMedTreUtfylteOgTreIkkeUtfylteUnderskrifter();
		Affiliation fakeAffiliation = mock(Affiliation.class, RETURNS_DEEP_STUBS);
		Contest contest = makeContest();
		when(fakeAffiliation.getBallot().getContest()).thenReturn(contest);
		Party party = makeParty(true);
		when(fakeAffiliation.getParty()).thenReturn(party);
		ListProposalValidationData listProposalValidationData = new ListProposalValidationData(candidateList, proposerList, fakeAffiliation);
		listProposalValidationData.setSufficientNumberOfCandidates(true);

		assertThat(listProposalValidationData.isPartyReadyForApproval()).isTrue();
	}

	@Test
	public void isPartyReadyForApproval_medKandidatFeil_returnererFalse() throws Exception {
		List<Candidate> candidateList = new ArrayList<>();
		Candidate candidate = createMock(Candidate.class);
		when(candidate.isInvalid()).thenReturn(true);
		candidateList.add(candidate);
		ListProposalValidationData listProposalValidationData = new ListProposalValidationData(candidateList, new ArrayList<>(), createMock(Affiliation.class));

		assertThat(listProposalValidationData.isPartyReadyForApproval()).isFalse();
	}

	@Test
	public void getProposerListMedKunUtfylteUnderskrifter_returnsFilteredList() {
		List<Candidate> candidateList = new ArrayList<>();
		List<Proposer> proposerList = makeProposerListMedTreUtfylteOgTreIkkeUtfylteUnderskrifter();
		ListProposalValidationData listProposalValidationData = new ListProposalValidationData(candidateList, proposerList, null);

		assertThat(listProposalValidationData.getProposerListMedKunUtfylteUnderskrifter()).hasSize(NO_OF_SIGNED_PROPOSERS);
	}

	private List<Proposer> makeProposerListMedTreUtfylteOgTreIkkeUtfylteUnderskrifter() {
		List<Proposer> proposerList = new ArrayList<>();
		proposerList.add(makeProposer(true));
		proposerList.add(makeProposer(true));
		proposerList.add(makeProposer(true));
		proposerList.add(makeProposer(false));
		proposerList.add(makeProposer(false));
		proposerList.add(makeProposer(false));
		return proposerList;
	}

	private Proposer makeProposer(boolean isUtfylt) {
		Proposer proposer = new Proposer();
		if (isUtfylt) {
			proposer.setFirstName("Fornavn");
			proposer.setLastName("Etternavn");
			proposer.setDateOfBirth(DATE_OF_BIRTH);
		}
		return proposer;
	}

	private Party makeParty(boolean simplifiedTreatment) {
		Party party = new Party();
		party.setForenkletBehandling(simplifiedTreatment);
		return party;
	}

	private Contest makeContest() {
		Contest contest = new Contest();
		contest.setMinProposersOldParty(MIN_PROPOSERS_OLD_PARTY);
		contest.setMinProposersNewParty(MIN_PROPOSERS_NEW_PARTY);
		return contest;
	}
}
