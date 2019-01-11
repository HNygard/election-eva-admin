package no.valg.eva.admin.configuration.domain.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.evote.validation.ID;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.AreaPath;

/**
 * Boroughs
 */
@Entity
@Table(name = "borough", uniqueConstraints = @UniqueConstraint(columnNames = { "municipality_pk", "borough_id" }))
@AttributeOverride(name = "pk", column = @Column(name = "borough_pk"))
@NamedQueries({
		@NamedQuery(name = "Borough.findById", query = "SELECT b FROM Borough b WHERE b.municipality.pk = :municipalityPk AND b.id = :id"),
		@NamedQuery(
				name = "Borough.findMunicipality1",
				query = "SELECT b FROM Borough b WHERE b.municipality.pk = :municipalityPk AND b.municipality1 IS TRUE"),
		@NamedQuery(name = "Borough.findMunicipality", query = "SELECT b FROM Borough b WHERE b.municipality.pk = :municipalityPk"),
		@NamedQuery(name = "Borough.findCountByMunicipality", query = "SELECT COUNT(b) FROM Borough b WHERE b.municipality.pk = :municipalityPk"),
		@NamedQuery(name = "Borough.findByCountry", query = "SELECT b FROM Borough b, Municipality m, County c WHERE b.municipality.pk = m.pk AND "
				+ "m.county.pk = c.pk AND c.country.pk = :countryPk"),
		@NamedQuery(
				name = "Borough.findWithoutPollingDistrictsByCountry",
				query = "SELECT b FROM Borough b, Municipality m, County c WHERE b.municipality.pk = m.pk AND "
						+ "m.county.pk = c.pk AND c.country.pk = :countryPk AND NOT EXISTS (SELECT pd FROM PollingDistrict pd WHERE pd.borough.pk = b.pk)") })
public class Borough extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	private Municipality municipality;
	private String id;
	private boolean municipality1;
	private String name;
	private Set<PollingDistrict> pollingDistricts = new HashSet<>();

	public Borough() {
	}

	/**
	 * Used in unit tests.
	 */
	public Borough(final String id, final String name, final Municipality municipality) {
		this.id = id;
		this.name = name;
		this.municipality = municipality;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "municipality_pk", nullable = false)
	@NotNull
	public Municipality getMunicipality() {
		return this.municipality;
	}

	public void setMunicipality(final Municipality municipality) {
		this.municipality = municipality;
	}

	@Column(name = "borough_id", nullable = false, length = 6)
	@ID(size = 6)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@Column(name = "municipality", nullable = false)
	public boolean isMunicipality1() {
		return this.municipality1;
	}

	public void setMunicipality1(final boolean municipality1) {
		this.municipality1 = municipality1;
	}

	@Column(name = "borough_name", nullable = false, length = 50)
	@LettersOrDigits
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@OneToMany(mappedBy = "borough", fetch = FetchType.LAZY)
	public Set<PollingDistrict> getPollingDistricts() {
		return pollingDistricts;
	}

	public void setPollingDistricts(Set<PollingDistrict> pollingDistricts) {
		this.pollingDistricts = pollingDistricts;
	}

	@Transient
	public PollingDistrict getMunicipalityPollingDistrict() {
		for (PollingDistrict pollingDistrict : pollingDistricts) {
			if (pollingDistrict.isMunicipality()) {
				return pollingDistrict;
			}
		}
		return null;
	}

	/**
	 * @return return technical polling district with the lowest id, null if none exists
	 */
	public PollingDistrict findFirstTechnicalPollingDistrict() {
		PollingDistrict firstTechnicalPollingDistrict = null;
		for (PollingDistrict pollingDistrict : pollingDistricts) {
			if (!pollingDistrict.isTechnicalPollingDistrict()) {
				continue;
			}
			if (firstTechnicalPollingDistrict == null || pollingDistrict.before(firstTechnicalPollingDistrict)) {
				firstTechnicalPollingDistrict = pollingDistrict;
			}
		}
		return firstTechnicalPollingDistrict;
	}

	public Collection<PollingDistrict> technicalPollingDistricts() {
		Collection<PollingDistrict> technicalPollingDistricts = new ArrayList<>();
		for (PollingDistrict pollingDistrict : getPollingDistricts()) {
			if (!pollingDistrict.isMunicipality() && pollingDistrict.isTechnicalPollingDistrict()) {
				technicalPollingDistricts.add(pollingDistrict);
			}
		}
		return technicalPollingDistricts;
	}

	public boolean hasRegularPollingDistricts() {
		for (PollingDistrict pollingDistrict : getPollingDistricts()) {
			if (pollingDistrict.isRegularPollingDistrict()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasParentPollingDistricts() {
		for (PollingDistrict pollingDistrict : getPollingDistricts()) {
			if (pollingDistrict.isParentPollingDistrict()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		switch (level) {
		case BOROUGH:
			return this.getPk();
		case MUNICIPALITY:
			return municipality.getPk();
		default:
			return null;
		}
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		return null;
	}

	public AreaPath areaPath() {
		return getMunicipality().areaPath().add(getId());
	}
}
