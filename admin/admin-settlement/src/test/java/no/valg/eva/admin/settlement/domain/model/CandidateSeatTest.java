package no.valg.eva.admin.settlement.domain.model;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Random;

import no.valg.eva.admin.settlement.domain.visitor.SettlementVisitor;

import org.testng.annotations.Test;


public class CandidateSeatTest {
	@Test
	public void constructor_given_ZeroDividendAndNullDivisor_doesNotSetQuotient() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat(null, null, 0, 0, null, false);
		assertThat(candidateSeat.getQuotient()).isNull();
	}

	@Test
	public void constructor_given_DividendAndNullDivisor_doesNotSetQuotient() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat(null, null, 0, 10, null, false);
		assertThat(candidateSeat.getQuotient()).isNull();
	}

	@Test
	public void constructor_given_DividendAndZeroDivisor_setZeroQuotient() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat(null, null, 0, 10, ZERO, false);
		assertThat(candidateSeat.getQuotient()).isEqualTo(new BigDecimal("0.000000"));
	}

	@Test
	public void constructor_given_DividendAndDivisor_setQuotient() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat(null, null, 0, 10, BigDecimal.valueOf(2), false);
		assertThat(candidateSeat.getQuotient()).isEqualTo(new BigDecimal("5.000000"));
	}

	@Test
	public void setDividend_givenDividend_updatesQuotient() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat(null, null, 0, 10, BigDecimal.valueOf(2), false);
		candidateSeat.setDividend(20);
		assertThat(candidateSeat.getQuotient()).isEqualTo(new BigDecimal("10.000000"));
	}

	@Test
	public void setDividend_givenZeroDividend_setZeroQuotient() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat(null, null, 0, 10, BigDecimal.valueOf(2), false);
		candidateSeat.setDividend(0);
		assertThat(candidateSeat.getQuotient()).isEqualTo(new BigDecimal("0.000000"));
	}

	@Test
	public void setDividend_givenDivisor_updatesQuotient() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat(null, null, 0, 10, BigDecimal.valueOf(2), false);
		candidateSeat.setDivisor(BigDecimal.valueOf(4));
		assertThat(candidateSeat.getQuotient()).isEqualTo(new BigDecimal("2.500000"));
	}

	@Test
	public void setDividend_givenNullDivisor_unsetQuotient() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat(null, null, 0, 10, BigDecimal.valueOf(2), false);
		candidateSeat.setDivisor(null);
		assertThat(candidateSeat.getQuotient()).isNull();
	}

	@Test
	public void setDividend_givenZeroDivisor_setZeroQuotient() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat(null, null, 0, 10, BigDecimal.valueOf(2), false);
		candidateSeat.setDivisor(ZERO);
		assertThat(candidateSeat.getQuotient()).isEqualTo(new BigDecimal("0.000000"));
	}

	@Test
	public void updateSeatNumberAndElectedState_givenSeatNumberLowerThanNumberOfPositionsInContest_setsSeatNumberAndElectedTrue() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat();
		candidateSeat.updateSeatNumberAndElectedState(1, 2);
		assertThat(candidateSeat.isElected()).isTrue();
	}

	@Test
	public void updateSeatNumberAndElectedState_givenSeatNumberEqualNumberOfPositionsInContest_setsSeatNumberAndElectedTrue() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat();
		candidateSeat.updateSeatNumberAndElectedState(1, 1);
		assertThat(candidateSeat.isElected()).isTrue();
	}

	@Test
	public void updateSeatNumberAndElectedState_givenSeatNumberGreaterThanNumberOfPositionsInContest_setsSeatNumberAndElectedTrue() throws Exception {
		CandidateSeat candidateSeat = new CandidateSeat();
		candidateSeat.updateSeatNumberAndElectedState(2, 1);
		assertThat(candidateSeat.isElected()).isFalse();
	}

	@Test
	public void accept_givenVisitor_callsVisitOnVisitor() throws Exception {
		SettlementVisitor visitor = mock(SettlementVisitor.class);
		CandidateSeat candidateSeat = new CandidateSeat();
		candidateSeat.setPk(new Random().nextLong());
		candidateSeat.accept(visitor);
		verify(visitor).visit(candidateSeat);
	}
}

