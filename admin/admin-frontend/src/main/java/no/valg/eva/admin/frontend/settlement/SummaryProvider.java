package no.valg.eva.admin.frontend.settlement;

import no.valg.eva.admin.common.settlement.model.BallotCountSummary;
import no.valg.eva.admin.common.settlement.model.SettlementSummary;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;

public interface SummaryProvider {

	String getContestPath();

	void prevSettlementSummaryPage();

	void nextSettlementSummaryPage();

	Button button(ButtonType type);

	SettlementSummary getSettlementSummary();

	SummaryRow getSummaryRow(final BallotCountSummary ballotCountSummary);
}
