package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.util.EqualsHashCodeUtil;
import no.valg.eva.admin.common.AreaPath;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "party_contest_area")
@AttributeOverride(name = "pk", column = @Column(name = "party_contest_area_pk"))
@ToString
@NoArgsConstructor // Needed by Hibernate
@AllArgsConstructor
public class PartyContestArea extends VersionedEntity implements java.io.Serializable {

	@Setter private Party party;
	@Setter private String countyId;
	@Setter private String municipalityId;
	@Setter private String boroughId;

	//           Party.partyContestArea-relasjonen (via mappedBy) da ikke fungerer som den burde
	//           (dette er sannsynligvis en bug i Hibernate eller noe)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "party_pk", nullable = false)
	public Party getParty() {
		return party;
	}

	@Column(name = "county_id", length = 2)
	public String getCountyId() {
		return countyId;
	}

	@Column(name = "municipality_id", length = 4)
	public String getMunicipalityId() {
		return municipalityId;
	}

	@Column(name = "borough_id", length = 6)
	public String getBoroughId() {
		return boroughId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof PartyContestArea) {
			return EqualsHashCodeUtil.genericEquals(obj, this);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return EqualsHashCodeUtil.genericHashCode(this);
	}

	public AreaPath areaPath(String electionEventId) {
		if (getCountyId() != null) {
			return AreaPath.from(electionEventId, AreaPath.COUNTRY_ID, getCountyId());
		} else if (getMunicipalityId() != null) {
			String munId = getMunicipalityId();
			String cntId = munId.substring(0, 2);
			return AreaPath.from(electionEventId, AreaPath.COUNTRY_ID, cntId, munId);
		} else {
			String borId = getBoroughId();
			String munId = borId.substring(0, 4);
			String cntId = munId.substring(0, 2);
			return AreaPath.from(electionEventId, AreaPath.COUNTRY_ID, cntId, munId, borId);
		}

	}
}
