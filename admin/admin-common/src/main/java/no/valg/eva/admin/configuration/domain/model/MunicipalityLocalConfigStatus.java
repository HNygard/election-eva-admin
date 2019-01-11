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
@Table(name = "municipality_local_config_status", uniqueConstraints = @UniqueConstraint(columnNames = { "municipality_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "municipality_local_config_status_pk"))
@NamedQueries({
		@NamedQuery(
				name = "MunicipalityLocalConfigStatus.findByMunicipality",
				query = "select m from MunicipalityLocalConfigStatus m WHERE m.municipality.pk = " + ":municipalityPk")
})
@NoArgsConstructor
public class MunicipalityLocalConfigStatus extends VersionedEntity implements java.io.Serializable {

	@Setter private Municipality municipality;
	@Setter private boolean advancePollingPlaces;
	@Setter private boolean electionPollingPlaces;
	@Setter private boolean electionCard;
	@Setter private boolean pollingDistricts;
	@Setter private boolean techPollingDistricts;
	@Setter private boolean reportingUnitStemmestyre;
	@Setter private boolean reportingUnitValgstyre;
	@Setter private boolean pollingStations;
	@Setter private boolean language;
	@Setter private boolean countCategories;
	@Setter private boolean listProposals;
	@Setter private boolean electronicMarkoffs;
	@Setter private boolean scanning;

	public MunicipalityLocalConfigStatus(Municipality municipality) {
		this.municipality = municipality;
	}

	@OneToOne
	@JoinColumn(name = "municipality_pk", nullable = false)
	@NotNull
	public Municipality getMunicipality() {
		return municipality;
	}

	@Column(name = "advance_polling_places", nullable = false)
	@NotNull
	public boolean isAdvancePollingPlaces() {
		return advancePollingPlaces;
	}

	@Column(name = "election_polling_places", nullable = false)
	@NotNull
	public boolean isElectionPollingPlaces() {
		return electionPollingPlaces;
	}

	@Column(name = "election_card", nullable = false)
	@NotNull
	public boolean isElectionCard() {
		return electionCard;
	}

	@Column(name = "polling_districts", nullable = false)
	@NotNull
	public boolean isPollingDistricts() {
		return pollingDistricts;
	}

	@Column(name = "tech_polling_districts", nullable = false)
	@NotNull
	public boolean isTechPollingDistricts() {
		return techPollingDistricts;
	}

	@Column(name = "reporting_unit_stemmestyre", nullable = false)
	@NotNull
	public boolean isReportingUnitStemmestyre() {
		return reportingUnitStemmestyre;
	}

	@Column(name = "reporting_unit_valgstyre", nullable = false)
	@NotNull
	public boolean isReportingUnitValgstyre() {
		return reportingUnitValgstyre;
	}

	@Column(name = "polling_stations", nullable = false)
	@NotNull
	public boolean isPollingStations() {
		return pollingStations;
	}

	@Column(name = "language", nullable = false)
	@NotNull
	public boolean isLanguage() {
		return language;
	}

	@Column(name = "count_categories", nullable = false)
	@NotNull
	public boolean isCountCategories() {
		return countCategories;
	}

	@Column(name = "list_proposals", nullable = false)
	@NotNull
	public boolean isListProposals() {
		return listProposals;
	}

	@Column(name = "electronic_markoffs", nullable = false)
	@NotNull
	public boolean isElectronicMarkoffs() {
		return electronicMarkoffs;
	}

	@Column(name = "scanning", nullable = false)
	@NotNull
	public boolean isScanning() {
		return scanning;
	}
}
