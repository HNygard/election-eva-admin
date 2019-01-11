package no.valg.eva.admin.common.settlement.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import no.evote.dto.CandidateVoteCountDto;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.settlement.model.AffiliationVoteCount;
import no.valg.eva.admin.common.settlement.model.CandidateSeat;
import no.valg.eva.admin.common.settlement.model.SettlementStatus;
import no.valg.eva.admin.common.settlement.model.SettlementSummary;

public interface SettlementService extends Serializable {
	void createSettlement(UserData userData, ElectionPath contestPath);

	void deleteSettlements(UserData userData, ElectionPath electionPath, AreaPath areaPath);

	Map<CountCategory, SettlementStatus> settlementStatusMap(UserData userData, ElectionPath boroughContestPath);

	SettlementSummary settlementSummary(UserData userData, ElectionPath boroughContestPath);

	boolean hasSettlementForContest(UserData userData, ElectionPath contestPath);

	List<AffiliationVoteCount> findAffiliationVoteCountsBySettlement(UserData userData, ElectionPath contestPath);

	List<Integer> findMandatesBySettlement(UserData userData, ElectionPath contestPath);

	Map<Long, List<CandidateVoteCountDto>> findCandidateVoteCountsBySettlement(UserData userData, ElectionPath contestPath);

	List<CandidateSeat> findAffiliationCandidateSeatsBySettlement(UserData userData, ElectionPath contestPath);
}
