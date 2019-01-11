package no.valg.eva.admin.common.settlement.service;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.settlement.model.LevelingSeatSettlementSummary;

public interface LevelingSeatSettlementService extends Serializable {

	LevelingSeatSettlementSummary distributeLevelingSeats(UserData userData);

	LevelingSeatSettlementSummary levelingSeatSettlementSummary(UserData userData);

	void deleteLevelingSeatSettlement(UserData userData);
}
