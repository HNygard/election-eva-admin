package no.valg.eva.admin.configuration.domain.model;

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
@Cache(usage = READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.BoroughDigest")
@Table(name = "borough")
@AttributeOverride(name = "pk", column = @Column(name = "borough_pk"))
public class BoroughDigest extends BaseEntity {

	private String id;
	private String navn;
	private boolean kommuneBydel;

	@Column(name = "borough_id", nullable = false, length = 6)
	@ID(size = 6)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "municipality", nullable = false)
	public boolean isKommuneBydel() {
		return this.kommuneBydel;
	}

	public void setKommuneBydel(boolean kommuneBydel) {
		this.kommuneBydel = kommuneBydel;
	}

	@Column(name = "borough_name", nullable = false, length = 50)
	@LettersOrDigits
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getNavn() {
		return this.navn;
	}

	public void setNavn(String navn) {
		this.navn = navn;
	}
}
