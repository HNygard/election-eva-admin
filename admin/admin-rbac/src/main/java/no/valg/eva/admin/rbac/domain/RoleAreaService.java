package no.valg.eva.admin.rbac.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.RoleRepository;

/**
 * Domain service handling logic related to role and area.
 */
@Default
@ApplicationScoped
public class RoleAreaService {

	static final boolean DO_NOT_INCLUDE_POLLING_DISTRICTS = false;
	static final boolean INCLUDE_POLLING_DISTRICTS = true;
	static final String MUNICIPALITY_POLLING_DISTRICT = "0000";

	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private RoleRepository roleRepository;

	public RoleAreaService(){

	}

	/**
	 * If area path is path for a county, this county is returned.  If area path is path for a municipality,
	 * this municipality and all polling places in municipality is returned.
	 * <p/>
	 * @param areaPath path defining area
	 * @return list of areas
	 */
	public List<PollingPlaceArea> selectableSubAreasForArea(AreaPath areaPath) {
		return subAreasForArea(areaPath, DO_NOT_INCLUDE_POLLING_DISTRICTS);
	}

	private List<PollingPlaceArea> subAreasForArea(AreaPath areaPath, boolean includePollingDistricts) {
		Set<PollingPlaceArea> areas = new HashSet<>();

		MvArea mvArea = mvAreaRepository.findSingleByPath(areaPath);
		if (areaPath.isCountyLevel()) {
			areas.add(mvArea.toViewObject());
		} else if (areaPath.isMunicipalityLevel()) {
			areas.add(mvArea.toViewObject());
			for (PollingPlace pollingPlace : mvArea.getMunicipality().pollingPlaces()) {
				PollingPlaceType type = pollingPlace.isElectionDayVoting() ? PollingPlaceType.ELECTION_DAY_VOTING : PollingPlaceType.ADVANCE_VOTING;
				areas.add(new PollingPlaceArea(pollingPlace.areaPath(), pollingPlace.getName(), type));
				PollingDistrict pollingDistrict = pollingPlace.getPollingDistrict();
				PollingDistrict parentPollingDistrict = pollingDistrict.getPollingDistrict();
				if (parentPollingDistrict != null) {
					// Tellekrets
					areas.add(new PollingPlaceArea(parentPollingDistrict.areaPath(), parentPollingDistrict.getName(),
							PollingPlaceType.ELECTION_DAY_VOTING));
				}
				if (includePollingDistricts) {
					areas.add(new PollingPlaceArea(pollingDistrict.areaPath(), pollingDistrict.getName()));
				}
			}
		}
		return new ArrayList<>(areas);
	}

	/**
	 * Finds roles that can be assigned to given area. If area is county, roles for county are returned. Else roles for municipality, polling district and
	 * polling place are returned.
	 * 
	 * @param areaPath gives area
	 * @param electionEventPk pk of election event
	 * @return collection of role items
	 */
	public Collection<RoleItem> assignableRolesForArea(AreaPath areaPath, Long electionEventPk) {
		if (areaPath.isCountyLevel()) {
			return roleRepository.assignableRolesForArea(areaPath.getLevel(), electionEventPk);
		}

		Collection<RoleItem> assignableRoles = new HashSet<>();
		assignableRoles.addAll(roleRepository.assignableRolesForArea(AreaLevelEnum.MUNICIPALITY, electionEventPk));
		assignableRoles.addAll(roleRepository.assignableRolesForArea(AreaLevelEnum.POLLING_DISTRICT, electionEventPk));
		assignableRoles.addAll(roleRepository.assignableRolesForArea(AreaLevelEnum.POLLING_PLACE, electionEventPk));
		return assignableRoles;
	}

	public Collection<RoleItem> findAssignableRolesForOperatorRole(OperatorRole operatorRole) {
		List<Role> roles = roleRepository.findAssignableRolesForOperatorRole(operatorRole);
		List<RoleItem> result = new ArrayList<>();
		for (Role role : roles) {
			result.add(new RoleItem(role.getId(), role.getName(), role.isUserSupport(), role.getElectionLevel(), role.levelsAsEnums()));
		}
		return result;
	}

	/**
	 * Finds roles that can be assigned to a given area or subareas defined by areaPath. In addition, subareas each role can be valid for is returned.
	 *
	 * @param areaPath path defining area
	 * @param electionEvent election event
	 * @return map of role to area lists
	 */
	public Map<RoleItem, List<PollingPlaceArea>> areasForRole(AreaPath areaPath, ElectionEvent electionEvent) {

		Map<RoleItem, List<PollingPlaceArea>> roleAreaMap = new HashMap<>();

		List<PollingPlaceArea> subAreasForArea = subAreasForArea(areaPath, INCLUDE_POLLING_DISTRICTS);
		Collection<RoleItem> roleItems = this.assignableRolesForArea(areaPath, electionEvent.getPk());
		for (RoleItem roleItem : roleItems) {
			Role role = roleRepository.findByElectionEventAndId(electionEvent, roleItem.getRoleId());

			List<PollingPlaceArea> areasForRole = new ArrayList<>();
			for (PollingPlaceArea subArea : subAreasForArea) {
				AreaLevelEnum areaLevel = subArea.getAreaPath().getLevel();
				PollingPlaceType pollingPlaceType = pollingPlaceType(subArea.getAreaPath());
				if (role.canBeAssignedToArea(areaLevel, pollingPlaceType)
						&& !isMunicipalityPollingDistrict(subArea.getAreaPath())
						&& !subArea.getAreaPath().isCentralEnvelopeRegistrationPollingPlace()) {
					areasForRole.add(subArea);
				}
			}
			roleAreaMap.put(roleItem, areasForRole);
		}
		return roleAreaMap;
	}

	private boolean isMunicipalityPollingDistrict(AreaPath areaPath) {
		return (areaPath.isPollingDistrictLevel() && areaPath.isMunicipalityPollingDistrict());
	}

	private PollingPlaceType pollingPlaceType(AreaPath areaPath) {
		if (areaPath.getPollingPlaceId() == null) {
			return PollingPlaceType.NOT_APPLICABLE;
		}
		if (areaPath.getPollingDistrictId().equals(MUNICIPALITY_POLLING_DISTRICT)) {
			return PollingPlaceType.ADVANCE_VOTING;
		}
		return PollingPlaceType.ELECTION_DAY_VOTING;
	}
}
