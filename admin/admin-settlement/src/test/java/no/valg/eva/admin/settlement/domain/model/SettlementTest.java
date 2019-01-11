package no.valg.eva.admin.settlement.domain.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Random;

import no.valg.eva.admin.settlement.domain.visitor.SettlementVisitor;

import org.testng.annotations.Test;

public class SettlementTest {
	@Test
	public void accept_givenVisitor_callsVisitOnVisitor() throws Exception {
		SettlementVisitor visitor = mock(SettlementVisitor.class);
		Settlement settlement = new Settlement();
		settlement.setPk(new Random().nextLong());
		settlement.accept(visitor);
		verify(visitor).visit(settlement);
	}

	@Test
	public void accept_givenVisitor_callsAcceptOnAffiliationVoteCounts() throws Exception {
		SettlementVisitor visitor = mock(SettlementVisitor.class);
		Settlement settlement = new Settlement();
		settlement.setPk(new Random().nextLong());
		AffiliationVoteCount affiliationVoteCount = mock(AffiliationVoteCount.class);
		settlement.addAffiliationVoteCount(affiliationVoteCount);
		settlement.accept(visitor);
		verify(affiliationVoteCount).accept(visitor);
	}

	@Test
	public void accept_givenVisitor_callsAcceptOnCandidateSeats() throws Exception {
		SettlementVisitor visitor = mock(SettlementVisitor.class);
		Settlement settlement = new Settlement();
		settlement.setPk(new Random().nextLong());
		CandidateSeat candidateSeat = mock(CandidateSeat.class);
		settlement.addCandidateSeat(candidateSeat);
		settlement.accept(visitor);
		verify(candidateSeat).accept(visitor);
	}
}
