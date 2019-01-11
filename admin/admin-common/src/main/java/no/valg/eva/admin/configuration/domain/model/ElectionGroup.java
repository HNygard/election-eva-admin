package no.valg.eva.admin.configuration.domain.model;

import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.evote.validation.ID;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.ElectionPath;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Grouping of elections within an election event, facilitating RBAC access to a subset of all elections via a single role
 */
@Entity
@Table(name = "election_group", uniqueConstraints = @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "election_group_id" }) )
@AttributeOverride(name = "pk", column = @Column(name = "election_group_pk") )
@NamedQueries({ @NamedQuery(
		name = "ElectionGroup.findById",
		query = "SELECT eg FROM ElectionGroup eg WHERE eg.electionEvent.pk = :electionEventPk AND eg.id = :id") })
@NoArgsConstructor
public class ElectionGroup extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	public static final String JADIRA_LOCAL_DATE = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate";

	@Setter private ElectionEvent electionEvent;
	@Setter private String id;
	@Setter private String name;
	@Setter private boolean electronicMarkoffs;
	@Setter private boolean advanceVoteInBallotBox;
	@Setter private boolean scanningPermitted;
	@Setter private boolean validateRoleAndListProposal;
	@Setter private boolean validatePollingPlaceElectoralBoardAndListProposal;

	private Set<Election> elections = new HashSet<>();

	public ElectionGroup(final String id, final String name, final ElectionEvent electionEvent) {
		this.id = id;
		this.name = name;
		this.electionEvent = electionEvent;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	@NotNull
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	@Column(name = "election_group_id", nullable = false, length = 2)
	@ID(size = 2)
	public String getId() {
		return this.id;
	}

	@Column(name = "election_group_name", nullable = false, length = 100)
	@LettersOrDigits
	@StringNotNullEmptyOrBlanks
	@Size(max = 100)
	public String getName() {
		return this.name;
	}

	@OneToMany(mappedBy = "electionGroup", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	public Set<Election> getElections() {
		return elections;
	}

	void setElections(Set<Election> elections) {
		this.elections = elections;
	}

	@Column(name = "electronic_markoffs", nullable = false)
	public boolean isElectronicMarkoffs() {
		return electronicMarkoffs;
	}

	@Column(name = "advance_vote_in_ballot_box", nullable = false)
	public boolean isAdvanceVoteInBallotBox() {
		return advanceVoteInBallotBox;
	}

	@Column(name = "scanning_permitted", nullable = false) 
	public boolean isScanningPermitted() { 
		return scanningPermitted;
	}

	@Column(name = "validate_role_and_list_proposal", nullable = false)
	public boolean isValidateRoleAndListProposal() {
		return validateRoleAndListProposal;
	}
	
	@Column(name = "validate_polling_place_electoral_board_and_list_proposal", nullable = false)
	public boolean isValidatePollingPlaceElectoralBoardAndListProposal() {
		return validatePollingPlaceElectoralBoardAndListProposal;
	}
	
	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		switch (level) {
		case ELECTION_EVENT:
			return electionEvent.getPk();
		case ELECTION_GROUP:
			return getPk();
		default:
			return null;
		}
	}

	public ElectionPath electionPath() {
		return getElectionEvent().electionPath().add(getId());
	}

	public boolean hasElectionWithId(String id) {
		for (Election election : elections) {
			if (election.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasElectionOnBoroughLevel() {
		return getElections().stream().anyMatch(Election::isOnBoroughLevel);
	}
}
