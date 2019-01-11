package no.valg.eva.admin.settlement.domain.consumer;

import no.valg.eva.admin.settlement.domain.model.ElectionSettlement;

public interface ElectionSettlementConsumer extends EntityConsumer {
	void consume(ElectionSettlement electionSettlement);
}
