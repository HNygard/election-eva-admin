package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.model.VersionedEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Konfigurasjon av skanning for fylker/kommuner
 * Ment til bruk for rapportering til VDIR, ved kontaktbehov
 */
@Entity
@Table(name = "scanning_config")
@AttributeOverride(name = "pk", column = @Column(name = "scanning_config_pk"))
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanningConfig extends VersionedEntity implements java.io.Serializable {
	
	@Setter private boolean scanning = false;
	@Setter private String responsibleFullName;
	@Setter private String responsiblePhoneNumber;
	@Setter private String responsibleEmail;
	@Setter private String vendor;
	@Setter private Boolean collaboration = false;
	@Setter private String collaborationParticpants;
	@Setter private String collaborationResponsible;
	@Setter private County county;
	@Setter private Municipality municipality;
	
	@Column(name = "is_scanning", nullable = false)
	public boolean isScanning() {
		return scanning;
	}

	@Column(name = "responsible_full_name", length = 152)
	public String getResponsibleFullName() {
		return responsibleFullName;
	}

	@Column(name = "responsible_telephone_number", length = 35)
	public String getResponsiblePhoneNumber() {
		return responsiblePhoneNumber;
	}

	@Column(name = "responsible_email", length = 129)
	public String getResponsibleEmail() {
		return responsibleEmail;
	}

	@Column(name = "vendor", length = 20)
	public String getVendor() {
		return vendor;
	}

	@Column(name = "collaboration")
	public Boolean getCollaboration() {
		return collaboration;
	}

	@Column(name = "collaboration_participants", length = 150)
	public String getCollaborationParticpants() {
		return collaborationParticpants;
	}

	@Column(name = "collaboration_responsible", length = 50)
	public String getCollaborationResponsible() {
		return collaborationResponsible;
	}

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "county_pk")
	public County getCounty() {
		return county;
	}

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "municipality_pk")
	public Municipality getMunicipality() {
		return municipality;
	}
}
