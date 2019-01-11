package no.valg.eva.admin.configuration.domain.model;

import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.model.VersionedEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * Status for local configuration for county
 */
@Entity
@Table(name = "county_local_config_status", uniqueConstraints = @UniqueConstraint(columnNames = { "county_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "county_local_config_status_pk"))
@NamedQueries({
		@NamedQuery(name = "CountyLocalConfigStatus.findByCounty", query = "select c from CountyLocalConfigStatus c WHERE c.county.pk = :countyPk")
})
@NoArgsConstructor
public class CountyLocalConfigStatus extends VersionedEntity implements java.io.Serializable {

	@Setter private County county;
	@Setter private boolean language;
	@Setter private boolean reportingUnitFylkesvalgstyre;
	@Setter private boolean listProposals;
	@Setter private boolean scanning;

	public CountyLocalConfigStatus(County county) {
		this.county = county;
	}

	@OneToOne
	@JoinColumn(name = "county_pk", nullable = false)
	@NotNull
	public County getCounty() {
		return this.county;
	}

	@Column(name = "language", nullable = false)
	@NotNull
	public boolean isLanguage() {
		return language;
	}

	@Column(name = "reporting_unit_fylkesvalgstyre", nullable = false)
	@NotNull
	public boolean isReportingUnitFylkesvalgstyre() {
		return reportingUnitFylkesvalgstyre;
	}

	@Column(name = "list_proposals", nullable = false)
	@NotNull
	public boolean isListProposals() {
		return listProposals;
	}
	
	@Column(name = "scanning", nullable = false)
	@NotNull
	public boolean isScanning() {
		return scanning;
	}
}
