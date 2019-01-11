package no.valg.eva.admin.settlement.domain.model.strategy;

import java.util.Collection;
import java.util.List;

import no.valg.eva.admin.settlement.domain.model.CandidateRank;

public interface ProcessCandidateRanksStrategy {
	List<CandidateRank> processCandidateRanks(Collection<CandidateRank> candidateRanks);
}
