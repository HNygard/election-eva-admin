package no.valg.eva.admin.configuration.domain.model;

import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.MUNICIPALITY;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.PARENT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.TECHNICAL;
import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
@Cache(usage = READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.PollingDistrictDigest")
@Table(name = "polling_district")
@AttributeOverride(name = "pk", column = @Column(name = "polling_district_pk"))
public class PollingDistrictDigest extends BaseEntity {

	private String id;
	private String navn;
	private boolean kommuneStemmekrets;
	private boolean tellekrets;
	private boolean tekniskKrets;
	private PollingDistrictDigest parent;

	public PollingDistrictType type() {
		if (isKommuneStemmekrets()) {
			return MUNICIPALITY;
		}
		if (isTekniskKrets()) {
			return TECHNICAL;
		}
		if (isTellekrets()) {
			return PARENT;
		}
		if (getParent() != null) {
			return CHILD;
		}
		return REGULAR;
	}

	@Column(name = "polling_district_id", nullable = false, length = 4)
	@ID(size = 4)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "polling_district_name", nullable = false, length = 50)
	@LettersOrDigits(extraChars = " .,-'/()")
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getNavn() {
		return navn;
	}

	public void setNavn(String navn) {
		this.navn = navn;
	}

	@Column(name = "municipality", nullable = false)
	public boolean isKommuneStemmekrets() {
		return kommuneStemmekrets;
	}

	public void setKommuneStemmekrets(boolean kommuneStemmekrets) {
		this.kommuneStemmekrets = kommuneStemmekrets;
	}

	@Column(name = "parent_polling_district", nullable = false)
	public boolean isTellekrets() {
		return tellekrets;
	}

	public void setTellekrets(boolean tellekrets) {
		this.tellekrets = tellekrets;
	}

	@Column(name = "technical_polling_district", nullable = false)
	@NotNull
	public boolean isTekniskKrets() {
		return tekniskKrets;
	}

	public void setTekniskKrets(boolean tekniskKrets) {
		this.tekniskKrets = tekniskKrets;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_polling_district_pk")
	public PollingDistrictDigest getParent() {
		return parent;
	}

	public void setParent(PollingDistrictDigest parent) {
		this.parent = parent;
	}
}
