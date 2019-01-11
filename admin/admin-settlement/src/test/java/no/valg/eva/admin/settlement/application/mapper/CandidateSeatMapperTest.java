package no.valg.eva.admin.settlement.application.mapper;

import static java.math.BigDecimal.ONE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class CandidateSeatMapperTest extends MockUtilsTestCase {
	@Test
	public void candidateSeats_givenEntities_returnsDtos() throws Exception {
		Candidate candidate = createMock(Candidate.class);
		Affiliation affiliation = createMock(Affiliation.class);
		List<no.valg.eva.admin.common.settlement.model.CandidateSeat> candidateSeats = new CandidateSeatMapper()
				.candidateSeats(singletonList(candidateSeatEntity(candidate, affiliation)));
		assertThat(candidateSeats).containsExactly(candidateSeatDto(candidate, affiliation));
	}

	private CandidateSeat candidateSeatEntity(Candidate candidate, Affiliation affiliation) {
		CandidateSeat candidateSeat = createMock(CandidateSeat.class);
		when(candidateSeat.getCandidate()).thenReturn(candidate);
		when(candidateSeat.getAffiliation()).thenReturn(affiliation);
		when(candidateSeat.getSeatNumber()).thenReturn(1);
		when(candidateSeat.getQuotient()).thenReturn(ONE);
		when(candidateSeat.getDividend()).thenReturn(2);
		when(candidateSeat.getDivisor()).thenReturn(ONE.add(ONE));
		when(candidateSeat.isElected()).thenReturn(true);
		return candidateSeat;
	}

	private no.valg.eva.admin.common.settlement.model.CandidateSeat candidateSeatDto(Candidate candidate, Affiliation affiliation) {
		return new no.valg.eva.admin.common.settlement.model.CandidateSeat(candidate, affiliation, 1, ONE, 2, ONE.add(ONE), true);
	}
}
