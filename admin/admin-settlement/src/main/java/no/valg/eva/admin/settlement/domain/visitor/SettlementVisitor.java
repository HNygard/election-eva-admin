package no.valg.eva.admin.settlement.domain.visitor;

import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.Settlement;

public interface SettlementVisitor {
	void visit(Settlement settlement);

	void visit(AffiliationVoteCount affiliationVoteCount);

	void visit(CandidateSeat candidateSeat);
}
