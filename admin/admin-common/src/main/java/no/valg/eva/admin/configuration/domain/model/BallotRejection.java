package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 * Reasons for rejection of ballots
 */
@Immutable
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.BallotRejection")
@Table(name = "ballot_rejection", uniqueConstraints = @UniqueConstraint(columnNames = "ballot_rejection_id"))
@AttributeOverride(name = "pk", column = @Column(name = "ballot_rejection_pk"))
@NamedQueries({
		@NamedQuery(name = "BallotRejection.BallotRejectionByEarlyVoting", query = "select br from BallotRejection br where br.earlyVoting = :ev order by id"),
		@NamedQuery(name = "BallotRejection.findAll", query = "select br from BallotRejection br order by id") })
public class BallotRejection extends VersionedEntity implements java.io.Serializable, Comparable<BallotRejection> {
	private String id;
	private boolean earlyVoting;
	private String name;

	public BallotRejection() {
	}

	public BallotRejection(String id, boolean earlyVoting, String name) {
		this.id = id;
		this.earlyVoting = earlyVoting;
		this.name = name;
	}

	@Column(name = "ballot_rejection_id", nullable = false, length = 6)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "early_voting", nullable = false)
	public boolean isEarlyVoting() {
		return this.earlyVoting;
	}

	public void setEarlyVoting(final boolean earlyVoting) {
		this.earlyVoting = earlyVoting;
	}

	@Column(name = "ballot_rejection_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public int compareTo(final BallotRejection other) {
		return getId().compareTo(other.getId());
	}
}
