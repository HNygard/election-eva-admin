package no.valg.eva.admin.settlement.domain.model.factory;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.settlement.domain.consumer.ElectionSeatConsumer;
import no.valg.eva.admin.settlement.domain.consumer.ElectionSettlementConsumer;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatSummaryConsumer;
import no.valg.eva.admin.settlement.domain.model.ElectionSeat;
import no.valg.eva.admin.settlement.domain.model.ElectionSettlement;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSummary;

public class ElectionSettlementFactory extends EntityFactory<ElectionSettlementFactory, ElectionSettlementConsumer>
		implements ElectionSeatConsumer, LevelingSeatSummaryConsumer {
	private List<ElectionSeat> electionSeats = new ArrayList<>();
	private List<LevelingSeatSummary> levelingSeatSummaries = new ArrayList<>();
	private ElectionSettlement electionSettlement;
	private int settlementNumber;

	@Override
	public void consume(ElectionSeat electionSeat) {
		electionSeats.add(electionSeat);
	}

	@Override
	public void consume(LevelingSeatSummary levelingSeatSummary) {
		levelingSeatSummaries.add(levelingSeatSummary);
	}

	@Override
	protected void updateConsumer(ElectionSettlementConsumer electionSettlementConsumer) {
		electionSettlementConsumer.consume(electionSettlement);
	}

	@Override
	protected ElectionSettlementFactory self() {
		return this;
	}

	public void buildElectionSettlement() {
		electionSettlement = new ElectionSettlement();
		electionSeats.forEach(electionSeat -> electionSeat.setElectionSettlement(electionSettlement));
		electionSettlement.setElectionSeats(electionSeats);
		levelingSeatSummaries.forEach(levelingSeatSummary -> levelingSeatSummary.setElectionSettlement(electionSettlement));
		electionSettlement.setLevelingSeatSummaries(levelingSeatSummaries);
		electionSettlement.setSettlementNumber(++settlementNumber);
		updateConsumers();
		electionSeats = new ArrayList<>();
		levelingSeatSummaries = new ArrayList<>();
	}
}
