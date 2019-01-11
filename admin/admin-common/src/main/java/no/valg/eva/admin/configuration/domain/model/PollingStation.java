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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.evote.validation.ID;
import no.evote.validation.Letters;
import no.valg.eva.admin.common.AreaPath;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.NoArgsConstructor;

/**
 * Polling stations
 */
@Entity
@Table(name = "polling_station", uniqueConstraints = { @UniqueConstraint(columnNames = { "polling_station_pk", "polling_station_id" }) })
@AttributeOverride(name = "pk", column = @Column(name = "polling_station_pk"))
@NamedQueries({
		@NamedQuery(name = "PollingStation.findByPollingPlace", query = "SELECT ps FROM PollingStation ps WHERE ps.pollingPlace.pk = :pollingPlacePk"),
		@NamedQuery(name = "PollingStation.countByPollingPlace", query = "SELECT count(ps) FROM PollingStation ps WHERE ps.pollingPlace.pk = :pollingPlacePk")
})
@NoArgsConstructor // For Hibernate/JPA
public class PollingStation extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	private PollingPlace pollingPlace;
	private String id;
	private String first;
	private String last;

	public PollingStation(String first, String last) {
		this.id = first + " - " + last;
		this.first = first;
		this.last = last;
	}
	
	public PollingStation(String id, String first, String last) {
		this(null, id, first, last);
	}
	
	public PollingStation(Long pk, String id, String first, String last) {
		this.setPk(pk);
		this.id = id;
		this.first = first;
		this.last = last;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "polling_place_pk", nullable = false)
	@NotNull
	public PollingPlace getPollingPlace() {
		return this.pollingPlace;
	}

	public void setPollingPlace(final PollingPlace pollingPlace) {
		this.pollingPlace = pollingPlace;
	}

	@Column(name = "polling_station_id", nullable = false, length = 2)
	@ID(size = 2)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "polling_station_first", length = 2)
	@Letters
	@Size(max = 2)
	public String getFirst() {
		return this.first;
	}

	public void setFirst(final String first) {
		this.first = first;
	}

	@Column(name = "polling_station_last", length = 2)
	@Letters
	@Size(max = 2)
	public String getLast() {
		return this.last;
	}

	public void setLast(final String last) {
		this.last = last;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		switch (level) {
		case POLLING_STATION:
			return this.getPk();
		case POLLING_PLACE:
			return pollingPlace.getPk();
		default:
			return null;
		}
	}

	public AreaPath areaPath() {
		return getPollingPlace().areaPath().add(getId());
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		PollingStation rhs = (PollingStation) obj;
		return new EqualsBuilder()
				.appendSuper(super.equals(obj))
				.append(this.pollingPlace, rhs.pollingPlace)
				.append(this.id, rhs.id)
				.append(this.first, rhs.first)
				.append(this.last, rhs.last)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(pollingPlace)
				.append(id)
				.append(first)
				.append(last)
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("pollingPlace", pollingPlace)
				.append("id", id)
				.append("first", first)
				.append("last", last)
				.toString();
	}
}
