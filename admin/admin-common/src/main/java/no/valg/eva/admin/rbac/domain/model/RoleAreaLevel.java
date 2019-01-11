package no.valg.eva.admin.rbac.domain.model;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.constants.PollingPlaceType;
import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;

import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "role_area_level", uniqueConstraints = @UniqueConstraint(columnNames = { "role_pk", "area_level_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "role_area_level_pk"))
@NamedQueries({
		@NamedQuery(name = "RoleAreaLevel.findRoleAreaLevelsByRolePk", query = "SELECT ral FROM RoleAreaLevel ral WHERE ral.role.pk = :rolePk"),
		@NamedQuery(
				name = "RoleAreaLevel.findRoleAreaLevelByRoleAndAreaLevel",
				query = "SELECT ral FROM RoleAreaLevel ral WHERE ral.role.pk = :rolePk AND ral.areaLevel.pk = :areaLevelPk") })
public class RoleAreaLevel extends VersionedEntity implements Serializable {

	private Role role;

	private AreaLevel areaLevel;

	private PollingPlaceType pollingPlaceType;

	public RoleAreaLevel() {
		super();
	}

	public RoleAreaLevel(final Role role, final AreaLevel areaLevel, PollingPlaceType pollingPlaceType) {
		this.role = role;
		this.areaLevel = areaLevel;
		this.pollingPlaceType = pollingPlaceType;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_pk", nullable = false)
	public Role getRole() {
		return this.role;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "area_level_pk", nullable = false)
	public AreaLevel getAreaLevel() {
		return this.areaLevel;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public void setAreaLevel(AreaLevel areaLevel) {
		this.areaLevel = areaLevel;
	}

	@Column(name = "polling_place_type", nullable = true)
	@Enumerated(EnumType.STRING)
	public PollingPlaceType getPollingPlaceType() {
		return pollingPlaceType;
	}

	public void setPollingPlaceType(PollingPlaceType pollingPlaceType) {
		this.pollingPlaceType = pollingPlaceType;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RoleAreaLevel)) {
			return false;
		}

		RoleAreaLevel that = (RoleAreaLevel) o;

		if (getPk() != null && that.getPk() != null) {
			return getPk().equals(that.getPk());
		}
		
		if (!areaLevel.equals(that.areaLevel)) {
			return false;
        }
		if (!role.equals(that.role)) {
			return false;
        }
		return true;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(role).append(areaLevel).toHashCode();
	}
}
