package no.valg.eva.admin.settlement.domain.model;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.testng.annotations.Test;


public class LevelingSeatQuotientTest {

	public static final int SCALE_SIX_DECIMALS = 6;

	@Test
	public void incrementPartyVotes_givenIncrement_incrementsPartyVotes() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setPartyVotes(1);
		levelingSeatQuotient.incrementPartyVotes(1);
		assertThat(levelingSeatQuotient.getPartyVotes()).isEqualTo(2);
	}

	@Test
	public void incrementPartySeats_givenIncrement_incrementsPartySeats() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setPartySeats(1);
		levelingSeatQuotient.incrementPartySeats(1);
		assertThat(levelingSeatQuotient.getPartySeats()).isEqualTo(2);
	}

	@Test
	public void setPartyVotes_givenPartyVotes_calculatesDividend() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setPartyVotes(1);
		assertThat(levelingSeatQuotient.getDividend()).isEqualTo(new BigDecimal("1.000000"));
	}

	@Test
	public void setPartySeats_givenPartySeats_calculatesDividend() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setPartySeats(1);
		assertThat(levelingSeatQuotient.getDividend()).isEqualTo(new BigDecimal("0.000000"));
	}

	@Test
	public void setPartyVotesAndPartySeats_givenInput_calculatesDividend() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setPartyVotes(71);
		levelingSeatQuotient.setPartySeats(3);
		assertThat(levelingSeatQuotient.getDividend()).isEqualTo(dividend());
	}

	@Test
	public void setContestVotes_givenContestVotes_calculatesDivisor() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setContestVotes(1);
		assertThat(levelingSeatQuotient.getDivisor()).isEqualTo(ZERO);
	}

	@Test
	public void setContestSeats_givenContestSeats_calculatesDivisor() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setContestSeats(1);
		assertThat(levelingSeatQuotient.getDivisor()).isEqualTo(ZERO);
	}

	@Test
	public void setContestVotesAndContestSeats_givenInput_calculatesDivisor() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setContestVotes(211);
		levelingSeatQuotient.setContestSeats(11);
		assertThat(levelingSeatQuotient.getDivisor()).isEqualTo(divisor());
	}

	@Test
	public void setContestVotes_givenContestVotes_calculatesQuotient() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setContestVotes(1);
		assertThat(levelingSeatQuotient.getQuotient()).isEqualTo(ZERO);
	}

	@Test
	public void setContestSeats_givenContestSeats_calculatesQuotient() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setContestSeats(1);
		assertThat(levelingSeatQuotient.getQuotient()).isEqualTo(ZERO);
	}

	@Test
	public void setVotesAndSeats_givenInput_calculatesQuotient() throws Exception {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setPartyVotes(71);
		levelingSeatQuotient.setPartySeats(3);
		levelingSeatQuotient.setContestVotes(211);
		levelingSeatQuotient.setContestSeats(11);
		BigDecimal quotient = dividend().divide(divisor(), SCALE_SIX_DECIMALS, HALF_UP);
		assertThat(levelingSeatQuotient.getQuotient()).isEqualTo(quotient);
	}

	public BigDecimal dividend() {
		return new BigDecimal("71").divide(new BigDecimal("7"), SCALE_SIX_DECIMALS, HALF_UP);
	}

	public BigDecimal divisor() {
		return new BigDecimal("211").divide(new BigDecimal("11"), SCALE_SIX_DECIMALS, HALF_UP);
	}
}

