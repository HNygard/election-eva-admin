package no.valg.eva.admin.settlement.application.mapper;

import java.math.BigDecimal;
import java.util.List;

import no.valg.eva.admin.common.mapper.Mapper;
import no.valg.eva.admin.common.settlement.model.CandidateSeat;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Default
@ApplicationScoped
public class CandidateSeatMapper extends Mapper {

	public CandidateSeatMapper() {

	}

	public List<CandidateSeat> candidateSeats(List<no.valg.eva.admin.settlement.domain.model.CandidateSeat> candidateSeatEntities) {
		return map(candidateSeatEntities, this::candidateSeat);
	}

	private CandidateSeat candidateSeat(no.valg.eva.admin.settlement.domain.model.CandidateSeat candidateSeatEntity) {
		Candidate candidate = candidateSeatEntity.getCandidate();
		Affiliation affiliation = candidateSeatEntity.getAffiliation();
		int seatNumber = candidateSeatEntity.getSeatNumber();
		BigDecimal quotient = candidateSeatEntity.getQuotient();
		int dividend = candidateSeatEntity.getDividend();
		BigDecimal divisor = candidateSeatEntity.getDivisor();
		boolean elected = candidateSeatEntity.isElected();
		return new CandidateSeat(candidate, affiliation, seatNumber, quotient, dividend, divisor, elected);
	}
}
