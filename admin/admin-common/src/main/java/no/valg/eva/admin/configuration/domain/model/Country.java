package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.evote.validation.ID;
import no.evote.validation.Letters;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;

/**
 * Countries
 */
@Entity
@Table(name = "country", uniqueConstraints = @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "country_id" }))
@AttributeOverride(name = "pk", column = @Column(name = "country_pk"))
@NamedQueries({ @NamedQuery(name = "Country.findById", query = "SELECT c FROM Country c WHERE c.electionEvent.pk = :electionEventPk AND c.id = :id"),
		@NamedQuery(name = "Country.findByName", query = "SELECT c FROM Country c WHERE c.electionEvent.pk = :electionEventPk AND c.name = :name"),
		@NamedQuery(name = "Countries.findByElectionEvent", query = "SELECT c FROM Country c WHERE c.electionEvent.pk = :electionEventPk") })
public class Country extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	private ElectionEvent electionEvent;
	private String id;
	private String name;

	public Country() {
	}

	public Country(final String id, final String name, final ElectionEvent electionEvent) {
		this.id = id;
		this.name = name;
		this.electionEvent = electionEvent;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	@NotNull
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	@Column(name = "country_id", nullable = false, length = 2)
	@ID(size = 2)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "country_name", nullable = false, length = 50)
	@Letters(extraChars = "-. ")
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		if (level.equals(AreaLevelEnum.COUNTRY)) {
			return this.getPk();
		}
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.ELECTION_EVENT)) {
			return getElectionEvent().getPk();
		}
		return null;
	}

	public AreaPath areaPath() {
		return getElectionEvent().areaPath().add(getId());
	}

	@Transient
	public String electionEventId() {
		return getElectionEvent().getId();
	}
}
