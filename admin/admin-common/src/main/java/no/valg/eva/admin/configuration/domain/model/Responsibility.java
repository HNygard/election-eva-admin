package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

/**
 * Possible responsibilities for officers in reporting units
 */
@Entity
@Table(name = "responsibility", uniqueConstraints = @UniqueConstraint(columnNames = "responsibility_id"))
@AttributeOverride(name = "pk", column = @Column(name = "responsibility_pk"))
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Responsibility extends VersionedEntity implements java.io.Serializable {

	@Setter private String id;
	@Setter private String name;

	@Column(name = "responsibility_id", nullable = false, length = 4)
	public String getId() {
		return this.id;
	}

	@Column(name = "responsibility_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

}
