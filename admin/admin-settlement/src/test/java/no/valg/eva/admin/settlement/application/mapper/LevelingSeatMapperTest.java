package no.valg.eva.admin.settlement.application.mapper;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.settlement.domain.model.LevelingSeat;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;


public class LevelingSeatMapperTest extends MockUtilsTestCase {
	@Test
	public void levelingSeats_givenEntities_returnsDtos() throws Exception {
		List<LevelingSeat> levelingSeatEntities = singletonList(levelingSeatEntity());
		List<no.valg.eva.admin.common.settlement.model.LevelingSeat> levelingSeatDtos = new LevelingSeatMapper().levelingSeats(levelingSeatEntities);
		assertThat(levelingSeatDtos).containsExactly(levelingSeatDto());
	}

	private LevelingSeat levelingSeatEntity() {
		LevelingSeat levelingSeat = createMock(LevelingSeat.class);
		when(levelingSeat.getRankNumber()).thenReturn(1);
		when(levelingSeat.getSeatNumber()).thenReturn(2);
		when(levelingSeat.getLevelingSeatQuotient().getContest().getName()).thenReturn("contestName");
		when(levelingSeat.getLevelingSeatQuotient().getParty().getId()).thenReturn("partyId");
		when(levelingSeat.getCandidateSeat().getCandidateNameLine()).thenReturn("candidateName");
		when(levelingSeat.getCandidateSeat().getCandidateDisplayOrder()).thenReturn(3);
		return levelingSeat;
	}

	private no.valg.eva.admin.common.settlement.model.LevelingSeat levelingSeatDto() {
		return new no.valg.eva.admin.common.settlement.model.LevelingSeat(1, 2, "contestName", "partyId", "candidateName", 3);
	}
}

