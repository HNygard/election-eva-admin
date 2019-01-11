package no.valg.eva.admin.common.rbac;

import lombok.Getter;
import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;

/**
 * RBAC: Role
 */
public class Role implements Serializable {

	@Getter @Setter private Long electionEventPk;
	@Getter @Setter private String id;
	@Getter @Setter private String name;
	@Getter @Setter private boolean mutuallyExclusive;
	@Getter @Setter private int securityLevel;
	@Getter @Setter private boolean active;
	@Getter @Setter private boolean isUserSupport;
	@Getter @Setter private boolean checkCandidateConflicts;

	@Getter @Setter private Set<Access> accesses = new HashSet<>();
	@Getter @Setter private Set<Role> includedRoles = new HashSet<>();

	@Getter private Set<AreaLevelEnum> assignableAreaLevels = new HashSet<>();
	@Getter @Setter private Long pk;

	@Getter private List<ElectionLevelEnum> electionLevels = asList(ELECTION_GROUP, ELECTION, CONTEST);
	@Getter @Setter private ElectionLevelEnum electionLevel;

	@Getter private boolean countyAreaLevel;
	@Getter private boolean municipalityAreaLevel;
	@Getter private boolean pollingDistrictAreaLevel;
	@Getter private boolean pollingPlaceAreaLevel;
	/**
	 * PollingPlaceType if role has pollingPlaceAreaLevel
	 * True:	pollingPlaceType.ELECTION_DAY_VOTING
	 * False:	pollingPlaceType.ADVANCE_VOTING
	 * null: 	pollingPlaceAreaLevel and electionDayVotingPollingPlaceType is not selected for this role
	 */
	@Getter @Setter private Boolean electionDayVotingPollingPlaceType;
	@Setter private boolean mapAccesses;

	public boolean includesRole(final Role role) {
		return includedRoles.contains(role);
	}

	/**
	 * Sets flag and updates set of assignable area levels
	 */
	public void setCountyAreaLevel(final boolean countyAreaLevel) {
		this.countyAreaLevel = countyAreaLevel;
		if (countyAreaLevel) {
			addAssignableAreaLevel(COUNTY);
		} else {
			removeAssignableAreaLevel(COUNTY);
		}
	}

	/**
	 * Sets flag and updates set of assignable area levels
	 */
	public void setMunicipalityAreaLevel(final boolean municipalityAreaLevel) {
		this.municipalityAreaLevel = municipalityAreaLevel;
		if (municipalityAreaLevel) {
			addAssignableAreaLevel(MUNICIPALITY);
		} else {
			removeAssignableAreaLevel(MUNICIPALITY);
		}
	}

	/**
	 * Sets flag and updates set of assignable area levels
	 */
	public void setPollingDistrictAreaLevel(final boolean pollingDistrictAreaLevel) {
		this.pollingDistrictAreaLevel = pollingDistrictAreaLevel;
		if (pollingDistrictAreaLevel) {
			addAssignableAreaLevel(POLLING_DISTRICT);
		} else {
			removeAssignableAreaLevel(POLLING_DISTRICT);
		}
	}

	/**
	 * Sets flag and updates set of assignable area levels
	 */
	public void setPollingPlaceAreaLevel(final boolean pollingPlaceAreaLevel) {
		this.pollingPlaceAreaLevel = pollingPlaceAreaLevel;
		if (pollingPlaceAreaLevel) {
			addAssignableAreaLevel(POLLING_PLACE);
		} else {
			removeAssignableAreaLevel(POLLING_PLACE);
			this.electionDayVotingPollingPlaceType = null;
		}
	}

	/**
	 * Adds role area level.
	 */
	public void addAssignableAreaLevel(final AreaLevelEnum areaLevel) {
		this.assignableAreaLevels.add(areaLevel);
	}

	/**
	 * Removes given area level.
	 */
	public void removeAssignableAreaLevel(final AreaLevelEnum areaLevelEnum) {
		this.assignableAreaLevels.remove(areaLevelEnum);
	}

	/**
	 * @return true if role can be assigned to the given area level
	 */
	public boolean canBeAssignedToAreaLevel(final AreaLevelEnum areaLevelEnum) {
		return this.assignableAreaLevels.contains(areaLevelEnum);
	}

	/**
	 * @return true if this contains Access instances that need to be mapped to an entity.
	 */
	public boolean containsAccessesToMap() {
		return mapAccesses;
	}
}
