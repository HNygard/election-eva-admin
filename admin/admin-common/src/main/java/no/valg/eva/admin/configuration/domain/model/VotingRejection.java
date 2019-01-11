package no.valg.eva.admin.configuration.domain.model;

import lombok.Setter;
import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

/**
 * Reasons for rejection of votings
 */
@Immutable
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.VotingRejection")
@Table(name = "voting_rejection", uniqueConstraints = @UniqueConstraint(columnNames = "voting_rejection_id"))
@AttributeOverride(name = "pk", column = @Column(name = "voting_rejection_pk"))
@NamedQueries({ @NamedQuery(
		name = "VotingRejection.findByEarly",
		query = "SELECT vr FROM VotingRejection vr WHERE vr.earlyVoting = :earlyVoting") })
public class VotingRejection extends VersionedEntity implements java.io.Serializable {

	@Setter private String id;
	@Setter private boolean earlyVoting;
	@Setter private String name;
	@Setter private String suggestedRejectionName;

	@Column(name = "voting_rejection_id", nullable = false, length = 8)
	@StringNotNullEmptyOrBlanks
	@Size(max = 8)
	public String getId() {
		return this.id;
	}

	@Column(name = "early_voting", nullable = false)
	public boolean isEarlyVoting() {
		return this.earlyVoting;
	}

	@Column(name = "voting_rejection_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	@Column(name = "suggested_voting_rejection_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getSuggestedRejectionName() {
		return this.suggestedRejectionName;
	}

}
