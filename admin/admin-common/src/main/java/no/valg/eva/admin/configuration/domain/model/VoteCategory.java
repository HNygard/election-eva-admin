package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 * Categories for separation of votes, e.g. normal votes and write-ins
 */
@Immutable
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.VoteCategory")
@Table(name = "vote_category", uniqueConstraints = @UniqueConstraint(columnNames = "vote_category_id"))
@AttributeOverride(name = "pk", column = @Column(name = "vote_category_pk"))
public class VoteCategory extends VersionedEntity implements java.io.Serializable {

	private String id;
	private String name;

	@Column(name = "vote_category_id", nullable = false, length = 12)
	@StringNotNullEmptyOrBlanks
	@Size(max = 12)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "vote_category_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return id;
	}

	public enum VoteCategoryValues {
		personal, renumber, strikeout, writein, baseline,
	}
}
