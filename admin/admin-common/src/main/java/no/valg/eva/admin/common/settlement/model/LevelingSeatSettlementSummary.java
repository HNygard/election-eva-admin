package no.valg.eva.admin.common.settlement.model;

import java.io.Serializable;
import java.util.List;

public class LevelingSeatSettlementSummary implements Serializable {
	private final Status status;
	private final List<LevelingSeat> levelingSeats;

	public LevelingSeatSettlementSummary(Status status) {
		this.status = status;
		this.levelingSeats = null;
	}

	public LevelingSeatSettlementSummary(Status status, List<LevelingSeat> levelingSeats) {
		this.status = status;
		this.levelingSeats = levelingSeats;
	}

	public Status getStatus() {
		return status;
	}

	public List<LevelingSeat> getLevelingSeats() {
		return levelingSeats;
	}

	public enum Status {
		NOT_READY, READY, DONE
	}
}
