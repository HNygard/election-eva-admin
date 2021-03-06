package no.valg.eva.admin.settlement.application.mapper;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import no.valg.eva.admin.common.settlement.model.LevelingSeat;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatQuotient;

public class LevelingSeatMapper {
	public List<LevelingSeat> levelingSeats(List<no.valg.eva.admin.settlement.domain.model.LevelingSeat> levelingSeatEntities) {
		return levelingSeatEntities.stream().map(this::levelingSeat).collect(toList());
	}

	private LevelingSeat levelingSeat(no.valg.eva.admin.settlement.domain.model.LevelingSeat levelingSeat) {
		CandidateSeat candidateSeat = levelingSeat.getCandidateSeat();
		String candidateName = optional(candidateSeat).map(CandidateSeat::getCandidateNameLine).orElse(null);
		Integer displayOrder = optional(candidateSeat).map(CandidateSeat::getCandidateDisplayOrder).orElse(null);
		LevelingSeatQuotient levelingSeatQuotient = levelingSeat.getLevelingSeatQuotient();
		Contest contest = levelingSeatQuotient.getContest();
		Party party = levelingSeatQuotient.getParty();
		return new LevelingSeat(levelingSeat.getRankNumber(), levelingSeat.getSeatNumber(), contest.getName(), party.getId(), candidateName, displayOrder);
	}

	private <T> Optional<T> optional(T t) {
		return Optional.ofNullable(t);
	}
}
