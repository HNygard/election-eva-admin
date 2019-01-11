package no.evote.constants;

import static no.evote.constants.VoteCountStatusEnum.APPROVED;
import static no.evote.constants.VoteCountStatusEnum.COUNTING;
import static no.evote.constants.VoteCountStatusEnum.NONE;
import static no.evote.constants.VoteCountStatusEnum.REJECTED;
import static no.evote.constants.VoteCountStatusEnum.TO_APPROVAL;
import static no.evote.constants.VoteCountStatusEnum.TO_SETTLEMENT;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class VoteCountStatusEnumTest {

	private static final int INVALID_STATUS = 573;

	@Test
	public void lowerThan_givenHigherStatus_returnsTrue() throws Exception {
		assertThat(COUNTING.lowerThan(APPROVED)).isTrue();
	}

	@Test
	public void lowerThan_givenEqualStatus_returnsFalse() throws Exception {
		assertThat(COUNTING.lowerThan(COUNTING)).isFalse();
	}

	@Test
	public void lowerThan_givenLowerStatus_returnsFalse() throws Exception {
		assertThat(APPROVED.lowerThan(COUNTING)).isFalse();
	}

	@Test
	public void getStatus_givenAStatusCode_returnsCorrespondingEnumValue() {
		assertThat(VoteCountStatusEnum.getStatus(COUNTING.getStatus())).isEqualTo(COUNTING);
		assertThat(VoteCountStatusEnum.getStatus(TO_APPROVAL.getStatus())).isEqualTo(TO_APPROVAL);
		assertThat(VoteCountStatusEnum.getStatus(APPROVED.getStatus())).isEqualTo(APPROVED);
		assertThat(VoteCountStatusEnum.getStatus(TO_SETTLEMENT.getStatus())).isEqualTo(TO_SETTLEMENT);
		assertThat(VoteCountStatusEnum.getStatus(REJECTED.getStatus())).isEqualTo(REJECTED);
	}

	@Test
	public void getStatus_givenAnInvalidStatusCode_returnsNoneValue() {
		assertThat(VoteCountStatusEnum.getStatus(NONE.getStatus())).isEqualTo(NONE);
		assertThat(VoteCountStatusEnum.getStatus(INVALID_STATUS)).isEqualTo(NONE);
	}
	
}
