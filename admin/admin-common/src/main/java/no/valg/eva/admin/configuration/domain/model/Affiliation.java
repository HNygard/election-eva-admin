package no.valg.eva.admin.configuration.domain.model;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Affiliations on candidate lists / ballots
 */
@Entity
@Table(name = "affiliation", uniqueConstraints = { @UniqueConstraint(columnNames = { "ballot_pk", "display_order" }),
		@UniqueConstraint(columnNames = { "ballot_pk", "party_pk" }) })
@AttributeOverride(name = "pk", column = @Column(name = "affiliation_pk"))
@NamedQueries({
		@NamedQuery(name = "Affiliation.findByBallotStatusAndContest", query = "select a from Affiliation a where a.ballot.contest.pk = :contestPk "
				+ "AND a.ballot.ballotStatus.id = :statusId ORDER BY a.displayOrder ASC"),
		@NamedQuery(name = "Affiliation.findByContest", query = "select a from Affiliation a where a.ballot.contest.pk = :pk ORDER BY a.displayOrder ASC"),
		@NamedQuery(name = "Affiliation.findApprovedByContest", query = "select a from Affiliation a where a.ballot.contest.pk = :pk AND a.approved = true"
				+ " ORDER BY a.displayOrder ASC"),
		@NamedQuery(name = "Affiliation.findApprovedByElectionEvent", query = "select a from Affiliation a where a.approved = true and"
				+ " a.ballot.contest.election.electionGroup.electionEvent.pk = :electionEventPk"),
		@NamedQuery(name = "Affiliation.findByBallot", query = "select a from Affiliation a where a.ballot.pk = :ballotPk"),
		@NamedQuery(
				name = "Affiliation.findByName",
				query = "select a from Affiliation a where a.party.id = :id and a.party.electionEvent.pk = :electionEventPk"),
		@NamedQuery(
				name = "Affiliation.findByNameAndContest",
				query = "select a from Affiliation a where a.party.id = :id AND a.ballot.contest.pk = :contestPk"),
		@NamedQuery(name = "Affiliation.findAffiliationByContestAndDisplayOrderRange",
			query = "select a from Affiliation a where a.ballot.contest.pk = :cpk AND"
				+ " a.displayOrder >= :displayOrderFrom AND a.displayOrder <= :displayOrderTo ORDER BY a.displayOrder")})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Affiliation extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	private final List<String[]> validationMessages = new ArrayList<>();
	private Party party;
	private Ballot ballot;
	private Integer displayOrder;
	private boolean showCandidateResidence;
	private boolean showCandidateProfession;
	private boolean approved;
	private Set<Candidate> candidates = new LinkedHashSet<>();

	public Affiliation(final Affiliation affiliation) {
		super();
		party = affiliation.getParty();
		ballot = affiliation.getBallot();
		displayOrder = affiliation.getDisplayOrder();
		showCandidateResidence = affiliation.isShowCandidateResidence();
		showCandidateProfession = affiliation.isShowCandidateProfession();
		approved = affiliation.isApproved();
	}

	@ManyToOne(fetch = EAGER)
	@JoinColumn(name = "party_pk", nullable = false)
	public Party getParty() {
		return this.party;
	}

	public void setParty(final Party party) {
		this.party = party;
	}
	
	@OneToOne
	@JoinColumn(name = "ballot_pk", nullable = false)
	public Ballot getBallot() {
		return this.ballot;
	}

	public void setBallot(final Ballot ballot) {
		this.ballot = ballot;
	}

	@Column(name = "display_order")
	public Integer getDisplayOrder() {
		return this.displayOrder;
	}

	public void setDisplayOrder(final Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	@Column(name = "show_candidate_residence", nullable = false)
	public boolean isShowCandidateResidence() {
		return this.showCandidateResidence;
	}

	public void setShowCandidateResidence(final boolean showCandidateResidence) {
		this.showCandidateResidence = showCandidateResidence;
	}

	@Column(name = "show_candidate_profession", nullable = false)
	public boolean isShowCandidateProfession() {
		return this.showCandidateProfession;
	}

	public void setShowCandidateProfession(final boolean showCandidateProfession) {
		this.showCandidateProfession = showCandidateProfession;
	}

	@Column(name = "approved", nullable = false)
	public boolean isApproved() {
		return this.approved;
	}

	public void setApproved(final boolean approved) {
		this.approved = approved;
	}

	@Override
	public String toString() {
		return party.getId();
	}

	@Transient
	public void addValidationMessage(String... validationMessageAndParams) {
		validationMessages.add(validationMessageAndParams);
	}

	@Transient
	public void clearValidationMessages() {
		validationMessages.clear();
	}

	@Transient
	public List<String[]> getValidationMessageList() {
		return validationMessages;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPk() == null) ? 0 : getPk().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.isEquals();
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.CONTEST)) {
			return this.getBallot().getContest().getPk();
		}
		return null;
	}

	/**
	 * @return true if municipality election, else false
	 */
	@Transient
	public boolean isMunicipalityElection() {
		return getBallot().getContest().getElection().isMunicipalityElection();
	}

	public String partyName() {
		return getParty().getName();
	}

	@OneToMany(mappedBy = "affiliation", fetch = LAZY)
	public Set<Candidate> getCandidates() {
		return candidates;
	}

	public void setCandidates(Set<Candidate> candidates) {
		this.candidates = candidates;
	}

	public void accept(ConfigurationVisitor configurationVisitor) {
		if (configurationVisitor.include(this)) {
			configurationVisitor.visit(this);
			for (Candidate candidate : getCandidates()) {
				candidate.accept(configurationVisitor);
			}
		}
	}
}
