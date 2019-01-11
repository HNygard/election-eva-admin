package no.valg.eva.admin.configuration.domain.model;

import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.common.configuration.model.ballot.PartyData;
import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Candidate lists / ballots (an approved candidate list represents a ballot)
 */
@NamedQueries({
	@NamedQuery(name = "Ballot.findByContest", query = "select b from Ballot b where b.contest.pk = :pk ORDER BY b.displayOrder ASC"),
	@NamedQuery(
		name = "Ballot.findApprovedByContest",
		query = "select b from Ballot b where b.contest.pk = :pk AND b.approved = true ORDER BY b.displayOrder ASC"),
	@NamedQuery(name = "Ballot.CountByContest", query = "select count(b) from Ballot b where b.contest.pk = :pk"),
	@NamedQuery(name = "Ballot.findByContestAndId", query = "select b from Ballot b where b.contest.pk = :pk AND b.id = :id"),
	@NamedQuery(name = "Ballot.findPkByContestAndId", query = "select b.pk from Ballot b where b.contest.pk = :pk AND b.id = :id"),
	@NamedQuery(name = "findByContestAndOrder", query = "select b from Ballot b where b.contest.pk = :pk AND b.displayOrder = :order") })
@NamedNativeQueries({
	@NamedNativeQuery(
			name = "Ballot.partiesQuery", 
			query = "SELECT "
				+ "p.party_id AS p_kod, "
				+ "coalesce(lt.locale_text, p.party_name) AS p_navn, "
				+ "pc.party_category_id, "
				+ "a.affiliation_pk "
				+ "FROM admin.ballot b "
				+ "JOIN admin.ballot_status bs "
				+ "  ON (bs.ballot_status_pk = b.ballot_status_pk "
				+ "  AND bs.ballot_status_id = 3) "
				+ "JOIN admin.affiliation a "
				+ "  ON (a.ballot_pk = b.ballot_pk) "
				+ "JOIN admin.party p "
				+ "  ON (p.party_pk = a.party_pk "
				+ "  AND p.party_id != 'BLANK') "
				+ "JOIN admin.party_category pc "
				+ "  ON (pc.party_category_pk = p.party_category_pk) "
				+ "JOIN admin.locale l "
				+ "  ON (l.locale_pk = b.locale_pk) "
				+ "LEFT JOIN admin.text_id t "
				+ "  ON (coalesce(t.election_event_pk, 0) = p.election_event_pk "
				+ "  AND t.text_id = p.party_name) "
				+ "LEFT JOIN admin.locale_text lt "
				+ "  ON (lt.locale_pk = l.locale_pk "
				+ "  AND lt.text_id_pk = t.text_id_pk) "
				+ "WHERE b.contest_pk = ?1 "
				+ "ORDER BY a.display_order",
			resultSetMapping = "PartyData"),
	@NamedNativeQuery(
			name = "Ballot.findBallotsWithoutVotesByVoteCount", 
			query = "WITH vote_count_contest AS ( "
				+ "    SELECT "
				+ "      contest_pk, "
				+ "      vote_count_pk "
				+ "    FROM vote_count vc "
				+ "      JOIN contest_report cr USING (contest_report_pk) "
				+ "    WHERE vc.vote_count_id = ?1 "
				+ "), parties_with_votes AS ( "
				+ "    SELECT DISTINCT "
				+ "      b.ballot_pk, "
				+ "      vcc.contest_pk "
				+ "    FROM vote_count_contest vcc "
				+ "      JOIN ballot_count bc ON vcc.vote_count_pk = bc.vote_count_pk "
				+ "      JOIN ballot b ON (b.ballot_pk = bc.ballot_pk) "
				+ ") "
				+ "SELECT b.* "
				+ "FROM vote_count_contest vcc "
				+ "  JOIN ballot b USING (contest_pk) "
				+ "  LEFT JOIN parties_with_votes pwv ON (b.ballot_pk = pwv.ballot_pk AND pwv.contest_pk = vcc.contest_pk) "
				+ "WHERE pwv.ballot_pk IS NULL;",
			resultClass = Ballot.class)
})
@SqlResultSetMappings({
	@SqlResultSetMapping(
		name = "PartyData",
		classes = {
			@ConstructorResult(
				targetClass = PartyData.class,
				columns = {
					@ColumnResult(name = "p_kod"),
					@ColumnResult(name = "p_navn"),
					@ColumnResult(name = "party_category_id"),
					@ColumnResult(name = "affiliation_pk")
				})
		}) })
@Entity
@Table(name = "ballot", uniqueConstraints =
	{ @UniqueConstraint(columnNames = { "contest_pk", "ballot_id" }), @UniqueConstraint(columnNames = { "contest_pk", "display_order" }) })
@AttributeOverride(name = "pk", column = @Column(name = "ballot_pk"))
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Access(AccessType.FIELD)
public class Ballot extends VersionedEntity implements java.io.Serializable, ContextSecurable, Comparable<Ballot> {

	@Getter @Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "locale_pk", nullable = false)
	private Locale locale;

	@Getter @Setter
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ballot_status_pk", nullable = false)
	private BallotStatus ballotStatus;

	@Getter @Setter
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "contest_pk", nullable = false)
	private Contest contest;

	@Getter @Setter
	@Column(name = "ballot_id", nullable = false, length = 10)
	private String id;

	/**
	 * Note: Ballot.displayOrder is only used for referendums.
	 * For the display order of list proposals or ballots in an election, the Affiliation.displayOrder property is used
	 */
	@Getter @Setter
	@Column(name = "display_order")
	private Integer displayOrder;

	@Getter @Setter
	@Column(name = "approved", nullable = false)
	private boolean approved;

	@Getter @Setter
	@OneToOne(mappedBy = "ballot", cascade = CascadeType.DETACH)
	private Affiliation affiliation;
	
	public Ballot(final Contest contest, final String id, final boolean approved) {
		super();
		this.contest = contest;
		this.id = id;
		this.approved = approved;
	}

	public Ballot(Ballot ballot) {
		super();
		this.contest = ballot.getContest();
		this.id = ballot.getId();
		this.locale = ballot.getLocale();
		this.approved = ballot.isApproved();
		this.ballotStatus = ballot.getBallotStatus();
		this.displayOrder = ballot.getDisplayOrder();
	}

	@Transient
	public Set<Candidate> getAffiliationCandidates() {
		return affiliation.getCandidates();
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level == ElectionLevelEnum.CONTEST) {
			return contest.getPk();
		}
		return null;
	}

	@Override
	public int compareTo(final Ballot other) {
		Integer one = affiliation == null || affiliation.getDisplayOrder() == null ? Integer.MAX_VALUE : affiliation.getDisplayOrder();
		Integer two = other.getAffiliation() == null || other.getAffiliation().getDisplayOrder() == null ? Integer.MAX_VALUE : other.getAffiliation().getDisplayOrder();
		return one.compareTo(two);
	}

	public String partyName() {
		Affiliation theAffiliation = getAffiliation();
		return theAffiliation != null ? theAffiliation.partyName() : null;
	}

	public void accept(ConfigurationVisitor configurationVisitor) {
		if (configurationVisitor.include(this)) {
			configurationVisitor.visit(this);
			getAffiliation().accept(configurationVisitor);
		}
	}
	
	@Transient
	public boolean isBlank() {
		return EvoteConstants.BALLOT_BLANK.equals(getId());
	}
}
