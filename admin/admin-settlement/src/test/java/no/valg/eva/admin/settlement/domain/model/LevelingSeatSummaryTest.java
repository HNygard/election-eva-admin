package no.valg.eva.admin.settlement.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LevelingSeatSummaryTest {
	@DataProvider
	public static Object[][] incrementElectionSeatsTestData() {
		return new Object[][] {
				new Object[] { 0 },
				new Object[] { 1 },
				new Object[] { -1 }
		};
	}

	@Test(dataProvider = "incrementElectionSeatsTestData")
	public void incrementElectionSeats_givenIncrements_incrementsElectionSeats(int increment) throws Exception {
		LevelingSeatSummary levelingSeatSummary = new LevelingSeatSummary();
		levelingSeatSummary.setElectionSeats(1);
		levelingSeatSummary.incrementElectionSeats(increment);
		assertThat(levelingSeatSummary.getElectionSeats()).isEqualTo(1 + increment);
	}

	@Test
	public void setElectionSeats_givenElectionSeats_updatesLevelingSeats() throws Exception {
		LevelingSeatSummary levelingSeatSummary = new LevelingSeatSummary();
		levelingSeatSummary.setElectionSeats(1);
		assertThat(levelingSeatSummary.getLevelingSeats()).isEqualTo(1);
	}

	@Test
	public void setContestSeats_givenElectionSeats_updatesLevelingSeats() throws Exception {
		LevelingSeatSummary levelingSeatSummary = new LevelingSeatSummary();
		levelingSeatSummary.setContestSeats(1);
		assertThat(levelingSeatSummary.getLevelingSeats()).isEqualTo(-1);
	}

	@Test
	public void hasMoreContestSeatsThanElectionSeats_givenContestSeatsGreaterThanElectionSeats_returnsTrue() throws Exception {
		LevelingSeatSummary levelingSeatSummary = new LevelingSeatSummary();
		levelingSeatSummary.setContestSeats(2);
		levelingSeatSummary.setElectionSeats(1);
		assertThat(levelingSeatSummary.hasMoreContestSeatsThanElectionSeats()).isTrue();
	}

	@Test
	public void hasMoreContestSeatsThanElectionSeats_givenContestSeatsEqualsElectionSeats_returnsFalse() throws Exception {
		LevelingSeatSummary levelingSeatSummary = new LevelingSeatSummary();
		levelingSeatSummary.setContestSeats(1);
		levelingSeatSummary.setElectionSeats(1);
		assertThat(levelingSeatSummary.hasMoreContestSeatsThanElectionSeats()).isFalse();
	}

	@Test
	public void hasMoreContestSeatsThanElectionSeats_givenContestSeatsLessThanElectionSeats_returnsFalse() throws Exception {
		LevelingSeatSummary levelingSeatSummary = new LevelingSeatSummary();
		levelingSeatSummary.setContestSeats(1);
		levelingSeatSummary.setElectionSeats(2);
		assertThat(levelingSeatSummary.hasMoreContestSeatsThanElectionSeats()).isFalse();
	}
}
