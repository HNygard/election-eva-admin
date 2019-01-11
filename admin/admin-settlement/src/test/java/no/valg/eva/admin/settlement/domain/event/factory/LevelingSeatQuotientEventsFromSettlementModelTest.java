package no.valg.eva.admin.settlement.domain.event.factory;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.event.LevelingSeatQuotientEvent;
import no.valg.eva.admin.settlement.domain.event.listener.LevelingSeatQuotientEventListener;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LevelingSeatQuotientEventsFromSettlementModelTest extends MockUtilsTestCase {
	@Test
    public void visit_givenAffiliationVoteCount_fireEvent() {
		LevelingSeatQuotientEventListener listener = createMock(LevelingSeatQuotientEventListener.class);
		LevelingSeatQuotientEventsFromSettlementModel eventFactory = new LevelingSeatQuotientEventsFromSettlementModel();
		eventFactory.addEventListener(listener);
		Contest contest = createMock(Contest.class);
		Party party = createMock(Party.class);
		int partyVotes = 1;

		eventFactory.visit(affiliationVoteCount(contest, party, partyVotes));

		ArgumentCaptor<LevelingSeatQuotientEvent> argumentCaptor = ArgumentCaptor.forClass(LevelingSeatQuotientEvent.class);
		verify(listener, times(1)).levelingSeatQuotientDelta(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(levelingSeatQuotientEvent(contest, party, partyVotes, 0));
	}

	@Test
    public void visit_givenAffiliationVoteCountForBlankParty_doesNotFireEvent() {
		LevelingSeatQuotientEventListener listener = createMock(LevelingSeatQuotientEventListener.class);
		LevelingSeatQuotientEventsFromSettlementModel eventFactory = new LevelingSeatQuotientEventsFromSettlementModel();
		eventFactory.addEventListener(listener);
		Contest contest = createMock(Contest.class);
		Party party = createMock(Party.class);
		int partyVotes = 1;

		when(party.isBlank()).thenReturn(true);

		eventFactory.visit(affiliationVoteCount(contest, party, partyVotes));

		verify(listener, never()).levelingSeatQuotientDelta(any(LevelingSeatQuotientEvent.class));
	}

	@Test
    public void visit_gittAffiliationVoteCountForLokaltParti_fyrerAvEvent() {
		LevelingSeatQuotientEventListener listener = createMock(LevelingSeatQuotientEventListener.class);
		LevelingSeatQuotientEventsFromSettlementModel eventFactory = new LevelingSeatQuotientEventsFromSettlementModel();
		eventFactory.addEventListener(listener);
		Contest contest = createMock(Contest.class);
		Party party = createMock(Party.class);
		int partyVotes = 1;

		when(party.isLokaltParti()).thenReturn(true);

		eventFactory.visit(affiliationVoteCount(contest, party, partyVotes));

		verify(listener).levelingSeatQuotientDelta(any(LevelingSeatQuotientEvent.class));
	}

	@Test
    public void visit_givenCandidateSeat_fireEvent() {
		LevelingSeatQuotientEventListener listener = createMock(LevelingSeatQuotientEventListener.class);
		LevelingSeatQuotientEventsFromSettlementModel eventFactory = new LevelingSeatQuotientEventsFromSettlementModel();
		eventFactory.addEventListener(listener);
		Contest contest = createMock(Contest.class);
		Party party = createMock(Party.class);

		eventFactory.visit(candidateSeat(contest, party, true));

		ArgumentCaptor<LevelingSeatQuotientEvent> argumentCaptor = ArgumentCaptor.forClass(LevelingSeatQuotientEvent.class);
		verify(listener, times(1)).levelingSeatQuotientDelta(argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(levelingSeatQuotientEvent(contest, party, 0, 1));
	}

	@Test
    public void visit_givenCandidateSeatElectedFalse_doesNotFireEvent() {
		LevelingSeatQuotientEventListener listener = createMock(LevelingSeatQuotientEventListener.class);
		LevelingSeatQuotientEventsFromSettlementModel eventFactory = new LevelingSeatQuotientEventsFromSettlementModel();
		eventFactory.addEventListener(listener);
		Contest contest = createMock(Contest.class);
		Party party = createMock(Party.class);

		eventFactory.visit(candidateSeat(contest, party, false));

		verify(listener, never()).levelingSeatQuotientDelta(any(LevelingSeatQuotientEvent.class));
	}

	@Test
    public void visit_gittCandidateSeatForLokaltParti_fyrerAvEvent() {
		LevelingSeatQuotientEventListener listener = createMock(LevelingSeatQuotientEventListener.class);
		LevelingSeatQuotientEventsFromSettlementModel eventFactory = new LevelingSeatQuotientEventsFromSettlementModel();
		eventFactory.addEventListener(listener);
		Contest contest = createMock(Contest.class);
		Party party = createMock(Party.class);
		
		when(party.isLokaltParti()).thenReturn(true);

		eventFactory.visit(candidateSeat(contest, party, true));

		verify(listener).levelingSeatQuotientDelta(any(LevelingSeatQuotientEvent.class));
	}

	private AffiliationVoteCount affiliationVoteCount(Contest contest, Party party, int votes) {
		AffiliationVoteCount affiliationVoteCount = createMock(AffiliationVoteCount.class);
		when(affiliationVoteCount.getContest()).thenReturn(contest);
		when(affiliationVoteCount.getParty()).thenReturn(party);
		when(affiliationVoteCount.getVotes()).thenReturn(votes);
		return affiliationVoteCount;
	}

	private CandidateSeat candidateSeat(Contest contest, Party party, boolean elected) {
		CandidateSeat candidateSeat = createMock(CandidateSeat.class);
		when(candidateSeat.getContest()).thenReturn(contest);
		when(candidateSeat.getParty()).thenReturn(party);
		when(candidateSeat.isElected()).thenReturn(elected);
		return candidateSeat;
	}

	private LevelingSeatQuotientEvent levelingSeatQuotientEvent(Contest contest, Party party, int partyVotes, int partySeats) {
		return new LevelingSeatQuotientEvent(contest, party, partyVotes, partySeats);
	}
}
