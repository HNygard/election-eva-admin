package no.valg.eva.admin.configuration.domain.model;

import static no.valg.eva.admin.util.EqualsHashCodeUtil.genericEquals;
import static no.valg.eva.admin.util.EqualsHashCodeUtil.genericHashCode;
import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import no.evote.model.BaseEntity;
import no.evote.validation.ID;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

/**
 * Inneholder basis info for et område.
 */
@Entity
@Immutable
@Cache(usage = READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.MunicipalityDigest")
@Table(name = "municipality")
@AttributeOverride(name = "pk", column = @Column(name = "municipality_pk"))
public class MunicipalityDigest extends BaseEntity {

	private String id;
	private String navn;
	private boolean electronicMarkoffs;

	@Column(name = "municipality_id", nullable = false, length = 6)
	@ID(size = 6)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "electronic_markoffs", nullable = false)
	public boolean isElectronicMarkoffs() {
		return this.electronicMarkoffs;
	}

	public void setElectronicMarkoffs(boolean electronicMarkoffs) {
		this.electronicMarkoffs = electronicMarkoffs;
	}

	@Column(name = "municipality_name", nullable = false, length = 50)
	@LettersOrDigits
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getNavn() {
		return this.navn;
	}

	public void setNavn(String navn) {
		this.navn = navn;
	}

	@Override
	public int hashCode() {
		return genericHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return genericEquals(this, obj);
	}
}
