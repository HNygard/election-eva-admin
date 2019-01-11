package no.valg.eva.admin.settlement.domain.event.factory;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.event.ElectionVoteCountEvent;
import no.valg.eva.admin.settlement.domain.event.listener.ElectionVoteCountEventListener;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElectionVoteCountEventsFromSettlementModelTest extends MockUtilsTestCase {
	@Test
    public void visit_givenAffiliationVoteCount_firesElectionVoteCountEvent() {
		
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = new ElectionVoteCountEventsFromSettlementModel();
		Affiliation affiliation = mock(Affiliation.class, RETURNS_DEEP_STUBS);
		AffiliationVoteCount affiliationVoteCount = new AffiliationVoteCount(affiliation, 100, 40, 40, 15, 60, 25, 100, 5, 15);
		ElectionVoteCountEventListener listener = mock(ElectionVoteCountEventListener.class);
		electionVoteCountEventsFromSettlementModel.addEventListener(listener);

		electionVoteCountEventsFromSettlementModel.visit(affiliationVoteCount);

		ArgumentCaptor<ElectionVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(ElectionVoteCountEvent.class);
		verify(listener).electionVoteCountDelta(argumentCaptor.capture());
		ElectionVoteCountEvent event = argumentCaptor.getValue();
		assertThat(event.getParty()).isSameAs(affiliation.getParty());
		assertThat(event.getEarlyVotingBallots()).isEqualTo(40);
		assertThat(event.getEarlyVotingModifiedBallots()).isEqualTo(15);
		assertThat(event.getElectionDayBallots()).isEqualTo(60);
		assertThat(event.getElectionDayModifiedBallots()).isEqualTo(25);
		assertThat(event.getBaselineVotes()).isEqualTo(100);
		assertThat(event.getAddedVotes()).isEqualTo(5);
		assertThat(event.getSubtractedVotes()).isEqualTo(15);
		
	}

	@Test
    public void visit_givenAffiliationVoteCountForBlankParty_doesNotFireElectionVoteCountEvent() {
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = new ElectionVoteCountEventsFromSettlementModel();
		AffiliationVoteCount affiliationVoteCount = createMock(AffiliationVoteCount.class);
		ElectionVoteCountEventListener listener = mock(ElectionVoteCountEventListener.class);
		electionVoteCountEventsFromSettlementModel.addEventListener(listener);

		when(affiliationVoteCount.getAffiliation().getParty().isBlank()).thenReturn(true);

		electionVoteCountEventsFromSettlementModel.visit(affiliationVoteCount);

		verify(listener, never()).electionVoteCountDelta(any(ElectionVoteCountEvent.class));
	}

	@Test
    public void visit_gittAffiliationVoteCountForLokaltParti_fyrerAvEvent() {
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = new ElectionVoteCountEventsFromSettlementModel();
		AffiliationVoteCount affiliationVoteCount = createMock(AffiliationVoteCount.class);
		ElectionVoteCountEventListener listener = mock(ElectionVoteCountEventListener.class);
		electionVoteCountEventsFromSettlementModel.addEventListener(listener);

		when(affiliationVoteCount.getAffiliation().getParty().isLokaltParti()).thenReturn(true);

		electionVoteCountEventsFromSettlementModel.visit(affiliationVoteCount);

		verify(listener).electionVoteCountDelta(any(ElectionVoteCountEvent.class));
	}

	@Test
    public void visit_givenElectedCandidateSeat_firesElectionVoteCountEvent() {
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = new ElectionVoteCountEventsFromSettlementModel();
		Candidate candidate = mock(Candidate.class, RETURNS_DEEP_STUBS);
		Affiliation affiliation = candidate.getAffiliation();
		CandidateSeat candidateSeat = new CandidateSeat(candidate, affiliation, 0, 0, ZERO, true);
		ElectionVoteCountEventListener listener = mock(ElectionVoteCountEventListener.class);
		electionVoteCountEventsFromSettlementModel.addEventListener(listener);

		electionVoteCountEventsFromSettlementModel.visit(candidateSeat);

		ArgumentCaptor<ElectionVoteCountEvent> argumentCaptor = ArgumentCaptor.forClass(ElectionVoteCountEvent.class);
		verify(listener).electionVoteCountDelta(argumentCaptor.capture());
		ElectionVoteCountEvent event = argumentCaptor.getValue();
		assertThat(event.getParty()).isSameAs(affiliation.getParty());
		assertThat(event.getContestSeats()).isEqualTo(1);
	}

	@Test
    public void visit_givenNotElectedCandidateSeat_doesNotFireElectionVoteCountEvent() {
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = new ElectionVoteCountEventsFromSettlementModel();
		CandidateSeat candidateSeat = createMock(CandidateSeat.class);
		ElectionVoteCountEventListener listener = mock(ElectionVoteCountEventListener.class);
		electionVoteCountEventsFromSettlementModel.addEventListener(listener);

		when(candidateSeat.isElected()).thenReturn(false);

		electionVoteCountEventsFromSettlementModel.visit(candidateSeat);

		verify(listener, never()).electionVoteCountDelta(any(ElectionVoteCountEvent.class));
	}

	@Test
    public void visit_gittElectedCandidateSeatForLokaltParti_fyrerAvEvent() {
		ElectionVoteCountEventsFromSettlementModel electionVoteCountEventsFromSettlementModel = new ElectionVoteCountEventsFromSettlementModel();
		CandidateSeat candidateSeat = createMock(CandidateSeat.class);
		ElectionVoteCountEventListener listener = mock(ElectionVoteCountEventListener.class);
		electionVoteCountEventsFromSettlementModel.addEventListener(listener);

		when(candidateSeat.isElected()).thenReturn(true);
		when(candidateSeat.getParty().isLokaltParti()).thenReturn(true);

		electionVoteCountEventsFromSettlementModel.visit(candidateSeat);

		verify(listener).electionVoteCountDelta(any(ElectionVoteCountEvent.class));
	}
}
