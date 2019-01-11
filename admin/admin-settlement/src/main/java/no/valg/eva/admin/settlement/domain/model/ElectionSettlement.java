package no.valg.eva.admin.settlement.domain.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;

@Entity
@Table(name = "election_settlement", uniqueConstraints = { @UniqueConstraint(columnNames = { "leveling_seat_settlement_pk", "settlement_number" }) })
@AttributeOverride(name = "pk", column = @Column(name = "election_settlement_pk"))
public class ElectionSettlement extends VersionedEntity {
	private LevelingSeatSettlement levelingSeatSettlement;
	private int settlementNumber;
	private List<ElectionSeat> electionSeats = new ArrayList<>();
	private List<LevelingSeatSummary> levelingSeatSummaries = new ArrayList<>();

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "leveling_seat_settlement_pk", nullable = false)
	public LevelingSeatSettlement getLevelingSeatSettlement() {
		return levelingSeatSettlement;
	}

	public void setLevelingSeatSettlement(LevelingSeatSettlement levelingSeatSettlement) {
		this.levelingSeatSettlement = levelingSeatSettlement;
	}

	@Column(name = "settlement_number", nullable = false)
	public int getSettlementNumber() {
		return settlementNumber;
	}

	public void setSettlementNumber(int settlementNumber) {
		this.settlementNumber = settlementNumber;
	}

	@OneToMany(fetch = LAZY, mappedBy = "electionSettlement", cascade = ALL)
	public List<ElectionSeat> getElectionSeats() {
		return electionSeats;
	}

	public void setElectionSeats(List<ElectionSeat> electionSeats) {
		this.electionSeats = electionSeats;
	}

	@OneToMany(fetch = LAZY, mappedBy = "electionSettlement", cascade = ALL)
	public List<LevelingSeatSummary> getLevelingSeatSummaries() {
		return levelingSeatSummaries;
	}

	public void setLevelingSeatSummaries(List<LevelingSeatSummary> levelingSeatSummaries) {
		this.levelingSeatSummaries = levelingSeatSummaries;
	}
}
