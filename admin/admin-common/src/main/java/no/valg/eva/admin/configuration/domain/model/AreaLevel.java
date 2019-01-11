package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.evote.constants.AreaLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

/**
 * Description of levels in mv_area
 */
@Immutable
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.AreaLevel")
@Table(name = "area_level", uniqueConstraints = @UniqueConstraint(columnNames = "area_level_id"))
@AttributeOverride(name = "pk", column = @Column(name = "area_level_pk"))
public class AreaLevel extends VersionedEntity implements java.io.Serializable {

	private int id;
	private String name;

	public AreaLevel() {
	}

	public AreaLevel(final AreaLevelEnum areaLevelEnum) {
		id = areaLevelEnum.getLevel();
		name = areaLevelEnum.getName();
	}

	@Column(name = "area_level_id", unique = true, nullable = false)
	@NotNull
	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Column(name = "area_level_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AreaLevel)) {
			return false;
		}

		AreaLevel that = (AreaLevel) o;

		if (id != that.id) {
			return false;
		}
		if (!name.equals(that.name)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(id)
				.append(name)
				.toHashCode();
	}

}
