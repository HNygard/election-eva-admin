package no.valg.eva.admin.common.rbac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.PollingPlaceArea;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class Operator extends Person {

	private boolean active;
	private String keySerialNumber;

	public static final Comparator<RoleAssociation> ROLE_ASSOCIATION_COMPARATOR = new Comparator<RoleAssociation>() {
		@Override
		public int compare(RoleAssociation ra1, RoleAssociation ra2) {
			return idAndNameOf(ra1.getArea()).compareTo(idAndNameOf(ra2.getArea()));
		}

		private String idAndNameOf(PollingPlaceArea ra1Area) {
			return ra1Area.getLeafIdForAreaLevels(AreaLevelEnum.POLLING_DISTRICT, AreaLevelEnum.POLLING_PLACE) + " " + ra1Area.getName();
		}
	};
	private final List<RoleAssociation> roleAssociations = new ArrayList<>();

	public Operator(String operatorId, String firstName, String lastName, String email, String telephoneNumber, boolean active) {
		super(new PersonId(operatorId), null, firstName, null, lastName, null);
		this.email = email;
		this.telephoneNumber = telephoneNumber;
		this.active = active;
	}

	public Operator(Person person) {
		super(person);
		if (person instanceof Operator) {
			active = ((Operator) person).isActive();
			keySerialNumber = ((Operator) person).getKeySerialNumber();
		}
	}

	public String name() {
		return nameLine();
	}

	public String getName() {
		return name();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getKeySerialNumber() {
		return keySerialNumber;
	}

	public void setKeySerialNumber(String keySerialNumber) {
		this.keySerialNumber = keySerialNumber;
	}

	public List<RoleAssociation> getRoleAssociations() {
		Collections.sort(roleAssociations, ROLE_ASSOCIATION_COMPARATOR);
		List<RoleAssociation> result = new ArrayList<>();
		result.addAll(roleAssociations);
		return result;
	}

	public void addRoleAssociation(RoleAssociation roleAssociation) {
		roleAssociations.add(roleAssociation);
	}

	/**
	 * @param roleId getId of role
	 * @return true if role with role getId is associated with operator, else false
	 */
	public boolean hasRole(String roleId) {
		for (RoleAssociation ra : roleAssociations) {
			if (ra.getRole().getRoleId().equals(roleId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param areaPathFilter areaPath for Role
	 * @return true if there is a role on given area
	 */
	public boolean hasRoleOnArea(String areaPathFilter) {
		for (RoleAssociation ra : roleAssociations) {
			if (ra.getArea().getAreaPath().path().equals(areaPathFilter)) {
				return true;
			}
		}
		return false;
	}

	public List<RoleAssociation> getRoleAssociations(final String roleId, final AreaPath areaPath) {
		return new ArrayList<>(Collections2.filter(roleAssociations, new Predicate<RoleAssociation>() {
			@Override
			public boolean apply(RoleAssociation association) {
				boolean roleMatch = roleId == null || association.getRole().getRoleId().equals(roleId);
				boolean pathMatch = areaPath == null || areaPath.equals(association.getArea().getAreaPath());
				pathMatch = pathMatch || isPollingPlaceLevelAndMatchOnPollingDistrictLevel(areaPath, association);
				pathMatch = pathMatch || isPollingDistrictLevelAndMatchOnPollingPlaceLevel(areaPath, association);
				return roleMatch && pathMatch;
			}
		}));
	}

	private boolean isPollingPlaceLevelAndMatchOnPollingDistrictLevel(AreaPath areaPath, RoleAssociation association) {
		if (areaPath != null && areaPath.isPollingPlaceLevel()) {
			return areaPath.toPollingDistrictPath().equals(association.getArea().getAreaPath());
		}
		return false;

	}

	private boolean isPollingDistrictLevelAndMatchOnPollingPlaceLevel(AreaPath areaPath, RoleAssociation association) {
		if (areaPath != null && areaPath.isPollingDistrictLevel()) {
			AreaPath associationPath = association.getArea().getAreaPath();
			boolean isAssociationOnPollingPlaceLevel = associationPath.isPollingPlaceLevel();
			return isAssociationOnPollingPlaceLevel && associationPath.toPollingDistrictPath().equals(areaPath);
		}
		return false;
	}

	public void clearRoleAssociations() {
		roleAssociations.clear();
	}

	public void addAllRoleAssociations(List<RoleAssociation> roleAssociations) {
		this.roleAssociations.addAll(roleAssociations);
	}
}
