package no.valg.eva.admin.counting.domain.filter;

import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.testng.annotations.Test;

public class ToSettlementVoteCountFilterTest {

	@Test
	public void filter_givenFinalVoteCountToSettlement_returnsTrue() throws Exception {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.getCountQualifierId()).thenReturn(FINAL.getId());
		when(voteCount.getCountStatus()).thenReturn(TO_SETTLEMENT);
		assertThat(ToSettlementVoteCountFilter.INSTANCE.filter(voteCount)).isTrue();
	}

	@Test
	public void filter_givenFinalVoteCountNotToSettlement_returnsFalse() throws Exception {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.getCountQualifierId()).thenReturn(FINAL.getId());
		when(voteCount.getCountStatus()).thenReturn(APPROVED);
		assertThat(ToSettlementVoteCountFilter.INSTANCE.filter(voteCount)).isFalse();
	}

	@Test
	public void filter_givenNotFinalVoteCount_returnsFalse() throws Exception {
		VoteCount voteCount = mock(VoteCount.class);
		when(voteCount.getCountQualifierId()).thenReturn(PRELIMINARY.getId());
		assertThat(ToSettlementVoteCountFilter.INSTANCE.filter(voteCount)).isFalse();
	}
}
