package no.valg.eva.admin.configuration.domain.model;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

/**
 * Categories for separation of cast votes, e.g. ordinary early votes and early votes from other municipalities
 */
@Immutable
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.VotingCategory")
@Table(name = "voting_category", uniqueConstraints = @UniqueConstraint(columnNames = "voting_category_id"))
@AttributeOverride(name = "pk", column = @Column(name = "voting_category_pk"))
public class VotingCategory extends VersionedEntity implements java.io.Serializable {

    private String id;
    private boolean earlyVoting;
    private String name;

    @Column(name = "voting_category_id", nullable = false, length = 4)
    @StringNotNullEmptyOrBlanks
    @Size(max = 4)
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

    @Column(name = "voting_category_name", nullable = false, length = 50)
    @StringNotNullEmptyOrBlanks
    @Size(max = 50)
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Transient
    public no.valg.eva.admin.common.voting.VotingCategory votingCategoryById() {
        return no.valg.eva.admin.common.voting.VotingCategory.fromId(getId());
    }

}
