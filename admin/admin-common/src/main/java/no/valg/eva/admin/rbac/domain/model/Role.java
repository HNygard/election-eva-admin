package no.valg.eva.admin.rbac.domain.model;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.evote.validation.LettersOrDigits;
import no.evote.validation.RoleValidationManual;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.POLLING_PLACE;

/**
 * RBAC: Role
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "no.valg.eva.admin.rbac.domain.model.Role")
@Table(name = "role", uniqueConstraints = @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "role_id" }))
@AttributeOverride(name = "pk", column = @Column(name = "role_pk"))
@NamedQueries({
		@NamedQuery(name = "Role.getIncludedRoles", query = "SELECT ri.roleByIncludedRolePk FROM RoleInclude ri WHERE ri.roleByRolePk = :role "
				+ "ORDER BY ri.roleByIncludedRolePk.name ASC"),
		@NamedQuery(name = "Role.getOperatorRoles", query = "SELECT opr FROM OperatorRole opr WHERE opr.role = :" + Role.ROLE_PARAMETER),
		@NamedQuery(name = "Role.getThisRoleIncludes", query = "SELECT ri FROM RoleInclude ri WHERE ri.roleByRolePk = :" + Role.ROLE_PARAMETER),
		@NamedQuery(name = "Role.getOthersRoleIncludes", query = "SELECT ri FROM RoleInclude ri WHERE ri.roleByIncludedRolePk = :" + Role.ROLE_PARAMETER),
		@NamedQuery(name = "Role.getRoleAccesses", query = "SELECT ra FROM RoleAccess ra WHERE ra.role = :" + Role.ROLE_PARAMETER),
		@NamedQuery(name = "Role.getAccesses", query = "SELECT ra.access FROM RoleAccess ra WHERE ra.role = :" + Role.ROLE_PARAMETER),
		@NamedQuery(name = "Role.getElectionEvent", query = "SELECT r.electionEvent FROM Role r WHERE r = :" + Role.ROLE_PARAMETER),
		@NamedQuery(name = "Role.findAllInElectionEvent", query = "SELECT r FROM Role r WHERE r.electionEvent = :event ORDER BY r.name"),
		@NamedQuery(name = "Role.findByElectionEventAndId", query = "SELECT r FROM Role r WHERE r.electionEvent = :event AND r.id = :id"),
        @NamedQuery(name = "Role.deleteAllExcept", query = "DELETE FROM Role r WHERE r.electionEvent = :electionEvent AND r.id NOT IN (:ids)"),
        @NamedQuery(
                name = "Role.findByCheckCandidateConflict",
                query = "select r from Role r where r.checkCandidateConflicts = true and r.electionEvent = :electionEvent")
})
// @formatter:off
@NamedNativeQueries({
		@NamedNativeQuery(
				name = "Role.getRoleWithIncludedRoles",
				query = "SELECT r.* FROM role r JOIN role_include_all ri ON r.role_pk = ri.included_role_pk WHERE ri.role_pk = ?1",
				resultClass = Role.class),
		@NamedNativeQuery(
				name = "Role.getAssignableRoles",
				query = "SELECT DISTINCT r.* FROM operator_role opr	JOIN role_inferior_role ri ON (ri.role_pk = opr.role_pk) JOIN "
						+ "role r on (r.role_pk = ri.inferior_role_pk) WHERE opr.operator_pk = ?1 AND r.active",
				resultClass = Role.class),
		@NamedNativeQuery(
				name = "Role.getIncludedAccesses",
				query = "SELECT a.* FROM access a JOIN role_access_all ra1 ON a.access_pk = ra1.access_pk WHERE ra1.role_pk = ?1",
				resultClass = Access.class),
		@NamedNativeQuery(
				name = "Role.getIncludedAccessesNoTask",
				query = "SELECT a.* FROM access a JOIN role_access_all ra1 ON a.access_pk = ra1.access_pk WHERE ra1.role_pk = ?1 "
						+ "AND NOT (text2ltree(a.access_path) <@ text2ltree('t')) ",
				resultClass = Access.class),
		@NamedNativeQuery(
				name = "Role.assignableRolesForArea",
				query = "SELECT r.* FROM role r JOIN role_area_level ral ON r.role_pk = ral.role_pk JOIN area_level al on ral.area_level_pk = al.area_level_pk "
						+ " WHERE al.area_level_id = :areaLevelId and r.election_event_pk = :electionEventPk",
				resultClass = Role.class)
})
// @formatter:on
@SqlResultSetMappings({ @SqlResultSetMapping(name = "roleResult", entities = @EntityResult(entityClass = Role.class)) })
public class Role extends VersionedEntity implements java.io.Serializable, Comparable<Role>, ContextSecurable {

	public static final String ROLE_PARAMETER = "role";
	private ElectionEvent electionEvent;
	private String id;
	private String name;
	private boolean mutuallyExclusive;
	private int securityLevel;
	private boolean active;
	private boolean isUserSupport;
	private boolean checkCandidateConflicts;

	private Set<Access> accesses = new HashSet<>();
	private Set<Role> includedRoles = new HashSet<>();

	private ElectionLevelEnum electionLevel;
	private Set<RoleAreaLevel> roleAreaLevels = new HashSet<>();
	private String translatedName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	@LettersOrDigits(extraChars = "._-", message = "@validation.role.id.lettersOrDigits")
	@Length(max = 25, groups = { RoleValidationManual.class })
	@NotEmpty(message = "@validation.role.id.notEmpty", groups = { RoleValidationManual.class })
	@Column(name = "role_id", nullable = false)
	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	@LettersOrDigits(extraChars = "@[] .-'_")
	@NotEmpty(message = "@validation.role.name.notEmpty", groups = { RoleValidationManual.class })
	@Column(name = "role_name", nullable = false, length = 50)
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "mutually_exclusive", nullable = false)
	public boolean isMutuallyExclusive() {
		return this.mutuallyExclusive;
	}

	public void setMutuallyExclusive(final boolean mutuallyExclusive) {
		this.mutuallyExclusive = mutuallyExclusive;
	}

	@Min(message = "@validation.role.secLevel.min", value = 3, groups = { RoleValidationManual.class })
	@Max(message = "@validation.role.secLevel.max", value = 4, groups = { RoleValidationManual.class })
	@NotNull(message = "@validation.role.secLevel.notNull", groups = { RoleValidationManual.class })
	@Column(name = "security_level", nullable = false)
	public int getSecurityLevel() {
		return this.securityLevel;
	}

	public void setSecurityLevel(final int securityLevel) {
		this.securityLevel = securityLevel;
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "role_access", joinColumns = { @JoinColumn(name = "role_pk") }, inverseJoinColumns = { @JoinColumn(name = "access_pk") })
	public Set<Access> getAccesses() {
		return accesses;
	}

	public void setAccesses(final Set<Access> accesses) {
		this.accesses = accesses;
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "role_include", joinColumns = { @JoinColumn(name = "role_pk") }, inverseJoinColumns = { @JoinColumn(name = "included_role_pk") })
	public Set<Role> getIncludedRoles() {
		return includedRoles;
	}

	public void setIncludedRoles(final Set<Role> includedRoles) {
		this.includedRoles = includedRoles;
	}

	@Column(name = "active", nullable = false)
	public boolean isActive() {
		return this.active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	@Column(name = "election_level")
	@Enumerated(EnumType.STRING)
	public ElectionLevelEnum getElectionLevel() {
		return electionLevel;
	}

	public void setElectionLevel(ElectionLevelEnum electionLevel) {
		this.electionLevel = electionLevel;
	}

	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	public Set<RoleAreaLevel> getRoleAreaLevels() {
		return roleAreaLevels;
	}

	public void setRoleAreaLevels(final Set<RoleAreaLevel> roleAreaLevels) {
		this.roleAreaLevels = roleAreaLevels;
	}

	@Override
	public String toString() {
		return getPk() == null ? "" : this.getPk().toString();
	}

	@Override
	public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() == obj.getClass()) {
			return getElectionEvent().getPk().equals(((Role) obj).getElectionEvent().getPk()) && getId().equals(((Role) obj).getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getElectionEvent().getPk().hashCode() * getId().hashCode();
	}

	@Override
	public int compareTo(final Role o) {
		return this.getName().compareTo(o.getName());
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.ELECTION_EVENT)) {
			return electionEvent.getPk();
		}
		return null;
	}

	@Transient
	public boolean includesRole(final Role role) {
		return includedRoles.contains(role);
	}

	@Column(name = "is_user_support", nullable = false)
	public boolean isUserSupport() {
		return this.isUserSupport;
	}

	public void setUserSupport(final boolean isUserSupport) {
		this.isUserSupport = isUserSupport;
	}

	@Column(name = "check_candidate_conflicts", nullable = false)
	public boolean isCheckCandidateConflicts() {
		return this.checkCandidateConflicts;
	}
	
	public void setCheckCandidateConflicts(final boolean checkCandidateConflicts) {
		this.checkCandidateConflicts = checkCandidateConflicts;
	}
	
	public List<AreaLevelEnum> levelsAsEnums() {
		List<AreaLevelEnum> result = new ArrayList<>();
		for (RoleAreaLevel roleAreaLevel : getRoleAreaLevels()) {
			result.add(AreaLevelEnum.getLevel(roleAreaLevel.getAreaLevel().getId()));
		}
		return result;
	}

	/**
	 * Adds role area level if it doesn't exist.
	 */
	void addOrUpdateAssignableAreaLevel(final AreaLevel areaLevel, final PollingPlaceType pollingPlaceType) {
		RoleAreaLevel newRoleAreaLevel = new RoleAreaLevel(this, areaLevel, pollingPlaceType);

		// handle update of polling place which can be advance or election day
		boolean updated = false;
		if (AreaLevelEnum.POLLING_PLACE == AreaLevelEnum.getLevel(areaLevel.getId())) {
			for (RoleAreaLevel roleAreaLevel : getRoleAreaLevels()) {
				if (AreaLevelEnum.POLLING_PLACE == AreaLevelEnum.getLevel(roleAreaLevel.getAreaLevel().getId())) {
					roleAreaLevel.setAreaLevel(areaLevel);
					roleAreaLevel.setPollingPlaceType(pollingPlaceType);
					updated = true;
				}
			}
		}

		if (!updated && !roleAreaLevels.contains(newRoleAreaLevel)) {
			roleAreaLevels.add(newRoleAreaLevel);
		}
	}

	/**
	 * Removes given area level.
	 */
	void removeAreaLevel(final AreaLevelEnum areaLevelEnum) {
		Set<RoleAreaLevel> roleAreaLevelsToRemove = new HashSet<>();
		for (RoleAreaLevel roleAreaLevel : roleAreaLevels) {
			if (roleAreaLevel.getAreaLevel().getId() == areaLevelEnum.getLevel()) {
				roleAreaLevelsToRemove.add(roleAreaLevel);
			}
		}
		roleAreaLevels.removeAll(roleAreaLevelsToRemove);
	}

	/**
	 * @return true if role can be assigned to the given area level
	 */
	public boolean canBeAssignedToAreaLevel(final AreaLevelEnum areaLevelEnum) {
		for (RoleAreaLevel roleAreaLevel : roleAreaLevels) {
			if (roleAreaLevel.getAreaLevel().getId() == areaLevelEnum.getLevel()) {
				return true;
			}
		}
		return false;
	}

	public boolean canBeAssignedToArea(AreaLevelEnum areaLevelEnum, PollingPlaceType pollingPlaceType) {
		return canBeAssignedToAreaLevel(areaLevelEnum) && (areaLevelEnum != POLLING_PLACE || canBeAssignedToPollingPlace(pollingPlaceType));
	}

	/**
	 * @return true if role can be assigned to election day polling place, else false
	 */
	boolean canBeAssignedToPollingPlace(PollingPlaceType pollingPlaceType) {
		for (RoleAreaLevel roleAreaLevel : roleAreaLevels) {
			if (roleAreaLevel.getAreaLevel().getId() == AreaLevelEnum.POLLING_PLACE.getLevel()) {
				return roleAreaLevel.getPollingPlaceType() == pollingPlaceType;
			}
		}
		return false;
	}

	/**
	 * @return this as view object with or without access objects
	 */
	public no.valg.eva.admin.common.rbac.Role toViewObject(final boolean includeAccesses) {
		no.valg.eva.admin.common.rbac.Role roleForView = new no.valg.eva.admin.common.rbac.Role();
		roleForView.setPk(getPk());
		roleForView.setId(id);
		roleForView.setName(name);
		if (includeAccesses) {
			roleForView.setAccesses(accessViewObjects());
		}
		roleForView.setActive(active);
		roleForView.setElectionEventPk(electionEvent.getPk());
		roleForView.setUserSupport(isUserSupport);
		roleForView.setCheckCandidateConflicts(checkCandidateConflicts);
		roleForView.setMutuallyExclusive(mutuallyExclusive);
		roleForView.setSecurityLevel(securityLevel);
		roleForView.setElectionLevel(electionLevel);
		roleForView.setIncludedRoles(includedRolesViewObjects(includeAccesses));

		roleForView.setCountyAreaLevel(canBeAssignedToAreaLevel(COUNTY));
		roleForView.setMunicipalityAreaLevel(canBeAssignedToAreaLevel(MUNICIPALITY));
		roleForView.setPollingDistrictAreaLevel(canBeAssignedToAreaLevel(POLLING_DISTRICT));
		boolean canBeAssignedToPollingPlace = canBeAssignedToAreaLevel(POLLING_PLACE);
		roleForView.setPollingPlaceAreaLevel(canBeAssignedToPollingPlace);
		if (canBeAssignedToPollingPlace) {
			roleForView.setElectionDayVotingPollingPlaceType(canBeAssignedToPollingPlace(PollingPlaceType.ELECTION_DAY_VOTING));
		}

		return roleForView;
	}

	private Set<no.valg.eva.admin.common.rbac.Role> includedRolesViewObjects(final boolean includeAccesses) {
		Set<no.valg.eva.admin.common.rbac.Role> includedRolesViewObjects = new HashSet<>();
		for (Role includedRoleEntity : getIncludedRoles()) {
			includedRolesViewObjects.add(includedRoleEntity.toViewObject(includeAccesses));
		}
		return includedRolesViewObjects;
	}

	private Set<no.valg.eva.admin.common.rbac.Access> accessViewObjects() {
		Set<no.valg.eva.admin.common.rbac.Access> accessViewObjects = new HashSet<>();
		for (Access accessEntity : getAccesses()) {
			accessViewObjects.add(accessEntity.toViewObject());
		}
		return accessViewObjects;
	}

	/**
	 * Updates this with assignable role area levels. Removes role area levels that are not in set of assignable area levels.
	 * @param assignableAreaLevels
	 *            set of assignable area levels
	 * @param allAreaLevels
	 *            map with all persisted area levels (needed by hibernate - else we must persist manually)
	 * @param electionDayPollingPlaceType
	 *            flag for PollingPlaceType for AreaLevel of type POLLING_PLACE.
	 */
	public void updateAssignableAreaLevels(final Set<AreaLevelEnum> assignableAreaLevels, final Map<AreaLevel, AreaLevel> allAreaLevels,
			Boolean electionDayPollingPlaceType) {

		Set<AreaLevelEnum> currentLevels = currentAreaLevels();

		for (AreaLevelEnum areaLevelEnum : assignableAreaLevels) {
			PollingPlaceType pollingPlaceType = findPollingPlaceType(electionDayPollingPlaceType, areaLevelEnum);
			addOrUpdateAssignableAreaLevel(allAreaLevels.get(new AreaLevel(areaLevelEnum)), pollingPlaceType);
			currentLevels.remove(areaLevelEnum);
		}

		for (AreaLevelEnum areaLevelEnum : currentLevels) {
			removeAreaLevel(areaLevelEnum);
		}
	}

	/**
	 * Setter pollingPlaceType for område/AreaLevel stemmested/POLLING_PLACE ved hjelp av flagget electionDayPollingPlaceType. For alle andre områder/AreaLevels
	 * vil pollingPlacType være irrelevant/NOT_APPLICABLE.
	 *
	 * @param electionDayPollingPlaceType
	 *            Flagg som indikerer stemmestedtype på stemmested, om det er angitt.
	 * @param areaLevelEnum
	 *            område
	 */
	private PollingPlaceType findPollingPlaceType(Boolean electionDayPollingPlaceType, AreaLevelEnum areaLevelEnum) {
		if (!AreaLevelEnum.POLLING_PLACE.equals(areaLevelEnum) || electionDayPollingPlaceType == null) {
			return PollingPlaceType.NOT_APPLICABLE;
		}

		return electionDayPollingPlaceType ? PollingPlaceType.ELECTION_DAY_VOTING : PollingPlaceType.ADVANCE_VOTING;
	}

	private Set<AreaLevelEnum> currentAreaLevels() {
		Set<AreaLevelEnum> currentAreaLevels = new HashSet<>();
		for (RoleAreaLevel roleAreaLevel : roleAreaLevels) {
			currentAreaLevels.add(AreaLevelEnum.getLevel(roleAreaLevel.getAreaLevel().getId()));
		}
		return currentAreaLevels;
	}

	/**
	 * Returns the highest security level of this role and it included roles
	 */
	@Transient
	public int getAccumulatedSecLevel() {
		List<Role> allIncludedRoles = new ArrayList<>();
		addAllIncludedRoles(this, allIncludedRoles);
        allIncludedRoles.sort((r1, r2) -> Integer.compare(r2.getSecurityLevel(), r1.getSecurityLevel()));

		return allIncludedRoles.get(0).getSecurityLevel();
	}

	private void addAllIncludedRoles(Role role, List<Role> allIncludedRoles) {
		allIncludedRoles.add(role);
		for (Role includedRole : role.getIncludedRoles()) {
			addAllIncludedRoles(includedRole, allIncludedRoles);
		}
	}

	public void setTranslatedName(String translatedName) {
		this.translatedName = translatedName;
	}

	@Transient
	public String getTranslatedName() {
		return translatedName;
	}
}
