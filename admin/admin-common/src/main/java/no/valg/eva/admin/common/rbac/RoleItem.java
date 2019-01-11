package no.valg.eva.admin.common.rbac;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;

/**
 * Value object with basic attributes for Role
 */
public class RoleItem implements Serializable {
	private final String roleId;
	private final String roleName;
	private final boolean userSupport;
	private final String translatedName;
	private final ElectionLevelEnum electionLevel;
	private final List<AreaLevelEnum> permittedAreaLevels;

	public RoleItem(String roleId, String roleName, boolean userSupport, ElectionLevelEnum electionLevel, List<AreaLevelEnum> permittedAreaLevels) {
		this(roleId, roleName, null, userSupport, electionLevel, permittedAreaLevels);
	}

	public RoleItem(String roleId, String roleName, String translatedName, boolean userSupport, ElectionLevelEnum electionLevel,
			List<AreaLevelEnum> permittedAreaLevels) {
		this.translatedName = translatedName;
		this.roleId = requireNonNull(roleId);
		this.roleName = requireNonNull(roleName);
		this.userSupport = userSupport;
		this.electionLevel = electionLevel;
		this.permittedAreaLevels = permittedAreaLevels;
	}

	public String getRoleId() {
		return roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public boolean isUserSupport() {
		return userSupport;
	}

	public String getTranslatedName() {
		return translatedName;
	}

	public ElectionLevelEnum getElectionLevel() {
		return electionLevel;
	}

	public List<AreaLevelEnum> getPermittedAreaLevels() {
		return permittedAreaLevels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RoleItem)) {
			return false;
		}

		RoleItem roleItem = (RoleItem) o;

		return roleId.equals(roleItem.roleId) && roleName.equals(roleItem.roleName) && userSupport == roleItem.userSupport;

	}

	@Override
	public int hashCode() {
		int result = roleId.hashCode();
		result = 31 * result + roleName.hashCode();
		if (!userSupport) {
			return result * -1;
		}
		return result;
	}
}
