package no.valg.eva.admin.settlement.domain.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

import java.io.Serializable;
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
import no.valg.eva.admin.configuration.domain.model.Election;

/**
 * LevelingSeatSettlement for election.
 */
@Entity
@Table(name = "leveling_seat_settlement", uniqueConstraints = { @UniqueConstraint(columnNames = "election_pk") })
@AttributeOverride(name = "pk", column = @Column(name = "leveling_seat_settlement_pk"))

public class LevelingSeatSettlement extends VersionedEntity implements Serializable {
	private Election election;
	private List<ElectionVoteCount> electionVoteCounts = new ArrayList<>();
	private List<ElectionSettlement> electionSettlements = new ArrayList<>();
	private List<LevelingSeatQuotient> levelingSeatQuotients = new ArrayList<>();
	private List<LevelingSeat> levelingSeats = new ArrayList<>();

	public LevelingSeatSettlement() {
	}

	public LevelingSeatSettlement(Election election) {
		this.election = election;
	}

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "election_pk", nullable = false)
	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	@OneToMany(fetch = LAZY, mappedBy = "levelingSeatSettlement", cascade = ALL)
	public List<ElectionVoteCount> getElectionVoteCounts() {
		return electionVoteCounts;
	}

	public void setElectionVoteCounts(List<ElectionVoteCount> electionVoteCounts) {
		this.electionVoteCounts = electionVoteCounts;
	}

	public void addElectionVoteCount(ElectionVoteCount electionVoteCount) {
		electionVoteCount.setLevelingSeatSettlement(this);
		electionVoteCounts.add(electionVoteCount);
	}

	@OneToMany(fetch = LAZY, mappedBy = "levelingSeatSettlement", cascade = ALL)
	public List<ElectionSettlement> getElectionSettlements() {
		return electionSettlements;
	}

	public void setElectionSettlements(List<ElectionSettlement> electionSettlements) {
		this.electionSettlements = electionSettlements;
	}

	public void addElectionSettlement(ElectionSettlement electionSettlement) {
		electionSettlement.setLevelingSeatSettlement(this);
		electionSettlements.add(electionSettlement);
	}

	@OneToMany(fetch = LAZY, mappedBy = "levelingSeatSettlement", cascade = ALL)
	public List<LevelingSeatQuotient> getLevelingSeatQuotients() {
		return levelingSeatQuotients;
	}

	public void setLevelingSeatQuotients(List<LevelingSeatQuotient> levelingSeatQuotients) {
		this.levelingSeatQuotients = levelingSeatQuotients;
	}

	public void addLevelingSeatQuotient(LevelingSeatQuotient levelingSeatQuotient) {
		levelingSeatQuotient.setLevelingSeatSettlement(this);
		levelingSeatQuotients.add(levelingSeatQuotient);
	}

	@OneToMany(fetch = LAZY, mappedBy = "levelingSeatSettlement", cascade = ALL)
	public List<LevelingSeat> getLevelingSeats() {
		return levelingSeats;
	}

	public void setLevelingSeats(List<LevelingSeat> levelingSeats) {
		this.levelingSeats = levelingSeats;
	}

	public void addLevelingSeat(LevelingSeat levelingSeat) {
		levelingSeat.setLevelingSeatSettlement(this);
		levelingSeats.add(levelingSeat);
	}
}
