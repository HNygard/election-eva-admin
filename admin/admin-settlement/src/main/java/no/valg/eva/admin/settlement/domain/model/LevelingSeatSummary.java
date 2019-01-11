package no.valg.eva.admin.settlement.domain.model;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Party;

@Entity
@Table(name = "leveling_seat_summary", uniqueConstraints = { @UniqueConstraint(columnNames = { "election_settlement_pk", "party_pk" }) })
@AttributeOverride(name = "pk", column = @Column(name = "leveling_seat_summary_pk"))
public class LevelingSeatSummary extends VersionedEntity {
	private ElectionSettlement electionSettlement;
	private Party party;
	private int electionSeats;
	private int contestSeats;
	private int levelingSeats;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "election_settlement_pk", nullable = false)
	public ElectionSettlement getElectionSettlement() {
		return electionSettlement;
	}

	public void setElectionSettlement(ElectionSettlement electionSettlement) {
		this.electionSettlement = electionSettlement;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "party_pk", nullable = false)
	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}

	@Column(name = "election_seats", nullable = false)
	public int getElectionSeats() {
		return electionSeats;
	}

	public void setElectionSeats(int electionSeats) {
		this.electionSeats = electionSeats;
		updateLevelingSeats();
	}

	@Transient
	public void incrementElectionSeats(int increment) {
		setElectionSeats(getElectionSeats() + increment);
	}

	@Column(name = "contest_seats", nullable = false)
	public int getContestSeats() {
		return contestSeats;
	}

	public void setContestSeats(int contestsSeats) {
		this.contestSeats = contestsSeats;
		updateLevelingSeats();
	}

	@Column(name = "leveling_seats", nullable = false)
	public int getLevelingSeats() {
		return levelingSeats;
	}

	private void setLevelingSeats(int levelingSeats) {
		// denne metoden er private fordi leveling seats beregnes i updateLevelingSeats()
		this.levelingSeats = levelingSeats;
	}

	@Transient
	public boolean hasMoreContestSeatsThanElectionSeats() {
		return levelingSeats < 0;
	}

	private void updateLevelingSeats() {
		setLevelingSeats(getElectionSeats() - getContestSeats());
	}
}
