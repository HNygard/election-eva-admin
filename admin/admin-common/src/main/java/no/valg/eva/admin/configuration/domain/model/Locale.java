package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.util.EqualsHashCodeUtil;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.configuration.model.election.LocaleId;

/**
 * Locales (languages) for text
 */
@Entity
@Table(name = "locale", uniqueConstraints = @UniqueConstraint(columnNames = "locale_id"))
@AttributeOverride(name = "pk", column = @Column(name = "locale_pk"))
public class Locale extends VersionedEntity implements java.io.Serializable {

	public static final String NN_NO = "nn-NO";
	
	private String id;
	private String name;

	@Column(name = "locale_id", nullable = false, length = 5)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "locale_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return EqualsHashCodeUtil.genericHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsHashCodeUtil.genericEquals(this, obj);
	}

	@Override
	public String toString() {
		return id + " " + name;
	}

	@Transient
	public boolean isNynorsk() {
		return NN_NO.equals(getId());
	}
	
	/**
	 * Convert an Evote Locale instance to a Java locale
	 * 
	 * @return Java locale
	 */
	@Transient
	public java.util.Locale toJavaLocale() {
        int indexOfDash = id.indexOf('-');
        String language = id.substring(0, indexOfDash);
        String country = id.substring(indexOfDash + 1);
        return new java.util.Locale(language, country);
	}

	@Transient
	public LocaleId toLocaleId() {
		return new LocaleId(getId());
	}
}
