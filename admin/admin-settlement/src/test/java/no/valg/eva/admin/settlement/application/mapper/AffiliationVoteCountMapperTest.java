package no.valg.eva.admin.settlement.application.mapper;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;


public class AffiliationVoteCountMapperTest extends MockUtilsTestCase {
	@Test
	public void affiliationVoteCounts_givenEntities_returnsDtos() throws Exception {
		Affiliation affiliation = createMock(Affiliation.class);
		List<no.valg.eva.admin.common.settlement.model.AffiliationVoteCount> affiliationVoteCountDtos = new AffiliationVoteCountMapper()
				.affiliationVoteCounts(singletonList(affiliationVoteCountEntity(affiliation)));
		assertThat(affiliationVoteCountDtos).containsExactly(affiliationVoteCountDtos(affiliation));
	}

	private AffiliationVoteCount affiliationVoteCountEntity(Affiliation affiliation) {
		AffiliationVoteCount affiliationVoteCount = createMock(AffiliationVoteCount.class);
		when(affiliationVoteCount.getAffiliation()).thenReturn(affiliation);
		when(affiliationVoteCount.getEarlyVotingBallots()).thenReturn(2);
		when(affiliationVoteCount.getEarlyVotingModifiedBallots()).thenReturn(1);
		when(affiliationVoteCount.getElectionDayBallots()).thenReturn(4);
		when(affiliationVoteCount.getElectionDayModifiedBallots()).thenReturn(3);
		when(affiliationVoteCount.getBaselineVotes()).thenReturn(6);
		when(affiliationVoteCount.getAddedVotes()).thenReturn(0);
		when(affiliationVoteCount.getSubtractedVotes()).thenReturn(1);
		return affiliationVoteCount;
	}

	private no.valg.eva.admin.common.settlement.model.AffiliationVoteCount affiliationVoteCountDtos(Affiliation affiliation) {
		return new no.valg.eva.admin.common.settlement.model.AffiliationVoteCount(affiliation, 2, 1, 4, 3, 6, 0, 1);
	}
}

