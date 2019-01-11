package no.valg.eva.admin.counting.builder;

import static no.valg.eva.admin.common.mockups.BallotMockups.BALLOT_ID_DEM;
import static no.valg.eva.admin.common.mockups.PartyMockups.PARTY_NAME_DEM;
import static no.valg.eva.admin.counting.mockup.BallotCountMockups.MODIFIED_BALLOT_COUNT_DEM;
import static no.valg.eva.admin.counting.mockup.BallotCountMockups.UNMODIFIED_BALLOT_COUNT_DEM;
import static no.valg.eva.admin.counting.mockup.BallotCountMockups.demModelBallotCount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.configuration.domain.model.Ballot;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BallotCountBuilderTest {
	private static final int ZERO = 0;
	private BallotCountBuilder builder;

	@BeforeMethod
	public void setUp() throws Exception {
		builder = new BallotCountBuilder();
	}

	@Test
	public void buildFromEntity() throws Exception {
		BallotCount ballotCount = builder
				.applyEntity(demModelBallotCount(true, null))
				.build();

		assertThat(ballotCount).isEqualTo(expectedBallotCountWithCount());
	}

	private BallotCount expectedBallotCountWithCount() {
		return new BallotCount(BALLOT_ID_DEM, PARTY_NAME_DEM, UNMODIFIED_BALLOT_COUNT_DEM + MODIFIED_BALLOT_COUNT_DEM, 0);
	}

	private BallotCount expectedBallotCountWithoutCount() {
		return new BallotCount(BALLOT_ID_DEM, PARTY_NAME_DEM, ZERO, ZERO);
	}

	@Test
	public void buildFromBlank() throws Exception {
		BallotCount ballotCount = builder.build();

		BallotCount expectedBallotCount = new BallotCount();
		assertThat(ballotCount).isEqualTo(expectedBallotCount);
	}

	@Test
	public void applyBallot_givenBallot_buildsBallotCount() throws Exception {
		BallotCount ballotCount = builder.applyBallot(ballot()).build();
		assertThat(ballotCount).isEqualTo(expectedBallotCountWithoutCount());
	}

	private Ballot ballot() {
		Ballot ballot = stub(Ballot.class);
		when(ballot.getId()).thenReturn(BALLOT_ID_DEM);
		when(ballot.getAffiliation().getBallot()).thenReturn(ballot);
		when(ballot.getAffiliation().getParty().getName()).thenReturn(PARTY_NAME_DEM);
		return ballot;
	}

	private <T> T stub(Class<T> type) {
		return mock(type, RETURNS_DEEP_STUBS);
	}
}
