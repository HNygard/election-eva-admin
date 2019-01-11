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
@Cache(usage = READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.PollingPlaceDigest")
@Table(name = "polling_place")
@AttributeOverride(name = "pk", column = @Column(name = "polling_place_pk"))
public class PollingPlaceDigest extends BaseEntity {

	private String id;
	private String navn;
	private boolean valgting;

	@Column(name = "polling_place_id", nullable = false, length = 4)
	@ID(size = 4)
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "polling_place_name", nullable = false, length = 50)
	@LettersOrDigits(extraChars = " .,-'/")
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getNavn() {
		return navn;
	}

	public void setNavn(final String navn) {
		this.navn = navn;
	}

	@Column(name = "election_day_voting", nullable = false)
	public boolean isValgting() {
		return valgting;
	}

	public void setValgting(final boolean valgting) {
		this.valgting = valgting;
	}
}
