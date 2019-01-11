package no.evote.service.rbac;

import no.evote.security.UserData;
import no.evote.service.LocaleTextServiceBean;
import no.evote.service.TranslationServiceBean;
import no.evote.service.util.RoleExporterImporter;
import no.evote.validation.RoleValidationManual;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.FindByIdRequest;
import no.valg.eva.admin.common.rbac.CircularReferenceCheckRequest;
import no.valg.eva.admin.common.rbac.PersistRoleResponse;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.TextId;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleServiceBean {
	private static final Logger LOGGER = Logger.getLogger(RoleServiceBean.class);
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Inject
	private OperatorServiceBean operatorService;
	@Inject
	private LocaleTextServiceBean localeTextService;
	@Inject
	private TranslationServiceBean translationService;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private LegacyAccessServiceBean accessService;
	@Inject
	private RoleExporterImporter roleExporterImporter;
	@Inject
	private RoleRepository roleRepository;

	public List<no.valg.eva.admin.common.rbac.Role> findAllRolesWithoutAccessesForView(final UserData userData) {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setPk(userData.getElectionEventPk());
		List<Role> allRolesInElectionEvent = roleRepository.findAllRolesInElectionEvent(electionEvent);
		List<no.valg.eva.admin.common.rbac.Role> rolesForView = new ArrayList<>();
		for (Role roleEntity : allRolesInElectionEvent) {
			rolesForView.add(roleEntity.toViewObject(false));
		}
		return rolesForView;
	}

	public Role create(UserData userData, Role role, boolean electionEventSpecific) {
		Role localRole = fixNameTranslation(userData, role, electionEventSpecific);
		return roleRepository.create(userData, localRole);
	}

	private Role fixNameTranslation(final UserData userData, final Role role, final boolean electionEventSpecific) {
		ElectionEvent electionEvent = electionEventRepository.findByPk(role.getElectionEvent().getPk());
		String name = role.getName();
		String id = role.getId();
		String textId = String.format("@role[%s].name", id);

		if (electionEventSpecific) {
			localeTextService.createLocaleTextToAllLocaleForEvent(userData, textId, name, electionEvent);
		} else {
			localeTextService.createLocaleTextToAllLocale(userData, textId, name, electionEvent);
		}

		role.setName(textId);
		return role;
	}

	public List<String> updateRole(UserData userData, no.valg.eva.admin.common.rbac.Role roleViewObject) {
		Role role = toRoleEntity(userData, roleViewObject, createAllAccessMap(), createAllAreaLevelsMap());
		List<String> validationFeedback = this.validateRole(role);
		boolean valid = validationFeedback.isEmpty();
		if (!valid) {
			return validationFeedback;
		}
		if (role.isMutuallyExclusive() && !operatorService.getCollisionsIfSetMutEx(role).isEmpty()) {
			Set<Operator> collidingOperators = operatorService.getCollisionsIfSetMutEx(role);

			StringBuilder s = new StringBuilder();
			for (Operator o : collidingOperators) {
				s.append("\n");
				s.append(o.getId());
			}
			validationFeedback.add("@rbac.role.MutExErrorTitle");
			validationFeedback.add("@rbac.role.MutExErrorMessage");
			validationFeedback.add(s.toString());
			valid = false;
		}
		ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());
		Role existingRole = roleRepository.findByElectionEventAndId(electionEvent, role.getId());
		if (existingRole != null && !role.getPk().equals(existingRole.getPk())) {
			validationFeedback.add("@rbac.role.duplicateID");
			valid = false;
		}
		if (valid) {
			roleRepository.update(userData, role);
		}
		return validationFeedback;
	}

	public PersistRoleResponse persistRole(UserData userData, no.valg.eva.admin.common.rbac.Role roleViewObject) {
		Map<String, Access> accessMap = createAllAccessMap();
		Role role = toRoleEntity(userData, roleViewObject, accessMap, createAllAreaLevelsMap());
		List<String> validationFeedback = this.validateRole(role);
		boolean valid = validationFeedback.isEmpty();
		if (!valid) {
			return new PersistRoleResponse(validationFeedback, role);
		}
		if (role.isMutuallyExclusive() && !operatorService.getCollisionsIfSetMutEx(role).isEmpty()) {
			Set<Operator> collidingOperators = operatorService.getCollisionsIfSetMutEx(role);

			StringBuilder s = new StringBuilder();
			for (Operator o : collidingOperators) {
				s.append("\n");
				s.append(o.getId());
			}
			validationFeedback.add("@rbac.role.MutExErrorTitle");
			validationFeedback.add("@rbac.role.MutExErrorMessage");
			validationFeedback.add(s.toString());
			valid = false;
		}
		ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());
		Role existingRole = roleRepository.findByElectionEventAndId(electionEvent, role.getId());
		if (existingRole != null) {
			validationFeedback.add("@rbac.role.duplicateID");
			valid = false;
		}
		if (valid) {
			return new PersistRoleResponse(validationFeedback, this.create(userData, role, true));
		}
		return new PersistRoleResponse(validationFeedback, role);
	}

	private Map<AreaLevel, AreaLevel> createAllAreaLevelsMap() {
		Map<AreaLevel, AreaLevel> allAreaLevels = new HashMap<>();
		for (AreaLevel areaLevel : mvAreaRepository.findAllAreaLevels()) {
			allAreaLevels.put(areaLevel, areaLevel);
		}
		return allAreaLevels;
	}

	private Map<String, Access> createAllAccessMap() {
		Map<String, Access> allAccessMap = new HashMap<>();
		for (Access access : accessService.findAll()) {
			allAccessMap.put(access.getPath(), access);
		}

		return allAccessMap;
	}

	public Role toRoleEntity(UserData userData, no.valg.eva.admin.common.rbac.Role rvo, Map<String, Access> allAccesses,
			final Map<AreaLevel, AreaLevel> allAreaLevels) {
		Role role = rvo.getPk() != null ? roleRepository.findByPk(rvo.getPk()) : new Role();

		if (rvo.containsAccessesToMap()) {
			role.setAccesses(toAccessEntities(rvo.getAccesses(), allAccesses));
		}

		role.setId(rvo.getId());
		role.setName(rvo.getName());
		role.setPk(rvo.getPk());
		role.setElectionEvent(new ElectionEvent(rvo.getElectionEventPk()));
		role.setActive(rvo.isActive());
		role.setIncludedRoles(includedRoles(userData, rvo.getIncludedRoles(), allAccesses, allAreaLevels));
		role.setUserSupport(rvo.isUserSupport());
		role.setCheckCandidateConflicts(rvo.isCheckCandidateConflicts());
		role.setMutuallyExclusive(rvo.isMutuallyExclusive());
		role.setSecurityLevel(rvo.getSecurityLevel());
		role.setElectionLevel(rvo.getElectionLevel());

		role.updateAssignableAreaLevels(rvo.getAssignableAreaLevels(), allAreaLevels, rvo.getElectionDayVotingPollingPlaceType());

		return role;
	}

	private Set<Role> includedRoles(
			UserData userData, Set<no.valg.eva.admin.common.rbac.Role> includedRoles, Map<String, Access> allAccesses,
			Map<AreaLevel, AreaLevel> allAreaLevels) {
		Set<Role> includedRoleEntities = new HashSet<>();
		for (no.valg.eva.admin.common.rbac.Role rvo : includedRoles) {
			includedRoleEntities.add(toRoleEntity(userData, rvo, allAccesses, allAreaLevels));
		}
		return includedRoleEntities;
	}

	private Set<Access> toAccessEntities(Set<no.valg.eva.admin.common.rbac.Access> accessesOnRole, Map<String, Access> allAccesses) {
		Set<Access> accessEntities = new HashSet<>();
		for (no.valg.eva.admin.common.rbac.Access avo : accessesOnRole) {
			accessEntities.add(allAccesses.get(avo.getPath()));
		}
		return accessEntities;
	}

	/**
	 * This metod deletes a role, and removes the role entity from the objects that depend on it.
	 */
	public void delete(UserData userData, Long pk) {
		Role r = roleRepository.findByPk(pk);
		TextId textId = translationService.findTextIdByElectionEvent(electionEventRepository.findByPk(r.getElectionEvent().getPk()), r.getName());
		translationService.deleteTextId(userData, textId);
		roleRepository.delete(userData, r);
	}

	public no.valg.eva.admin.common.rbac.Role findRoleWithAccessesForView(UserData userData, FindByIdRequest findByIdRequest) {
		Role role = roleRepository.findByElectionEventAndId(userData.electionEvent(), findByIdRequest.getId());
		return role.toViewObject(true);
	}

	public Set<no.valg.eva.admin.common.rbac.Role> findIncludedRolesForView(UserData userData, FindByIdRequest findByIdRequest) {
		Role role = roleRepository.findByElectionEventAndId(userData.electionEvent(), findByIdRequest.getId());
		Set<no.valg.eva.admin.common.rbac.Role> includedRoles = new HashSet<>();
		for (Role roleEntity : roleRepository.getIncludedRoles(role)) {
			includedRoles.add(roleEntity.toViewObject(false));
		}
		return includedRoles;
	}

	private List<String> validateRole(Role role) {
		List<String> validationFeedbackList = new ArrayList<>();
		Set<ConstraintViolation<Role>> constraintViolations = validator.validate(role, Default.class, RoleValidationManual.class);

		if (!constraintViolations.isEmpty()) {
			validationFeedbackList
					.addAll(constraintViolations.stream()
							.filter(Objects::nonNull)
							.map(ConstraintViolation::getMessage)
							.collect(Collectors.toList()));
		}
		return validationFeedbackList;
	}

	public String exportRoles(UserData userData, boolean excludeDefaultRoles) {
		List<Role> roles = roleRepository.findAllRolesInElectionEvent(userData.getOperatorRole().getMvElection().getElectionEvent());
		return roleExporterImporter.exportRoles(roles, excludeDefaultRoles);
	}

	public int importRoles(UserData userData, String importData, boolean deleteExistingRoles) {
		ElectionEvent electionEvent = userData.getOperatorRole().getMvElection().getElectionEvent();
		if (deleteExistingRoles) {
			roleRepository.deleteExistingRoles(electionEvent);
		}
		List<Role> rolesToImport = roleExporterImporter.buildRoleListFromImportData(electionEvent, importData);
		persistImportedRoles(userData, rolesToImport);
		return rolesToImport.size();
	}

	private void persistImportedRoles(UserData userData, List<Role> rolesToImport) {
		LOGGER.debug("Importing roles: ");
		for (Role role : rolesToImport) {
            logDebugMessageForRoleImport(role);
			if (role.getPk() != null) {
				roleRepository.update(userData, role);
			} else {
				roleRepository.create(userData, role);
			}
		}
	}

	private void logDebugMessageForRoleImport(Role role) {
		if (LOGGER.isDebugEnabled()) {
			StringBuilder message = new StringBuilder();
			message.append("\t").append(role.getId());
			if (!role.getIncludedRoles().isEmpty()) {
				message.append(" (");
				for (Role includedRole : role.getIncludedRoles()) {
					message.append(includedRole.getId()).append(", ");
				}
				message.append(")");
			}
			LOGGER.debug(message.toString());
		}
    }

    public boolean isCircularReference(UserData userData, CircularReferenceCheckRequest circularReferenceCheckRequest) {
		Role parentRole = roleRepository.findByPk(circularReferenceCheckRequest.getRole().getPk());
		Role includedRole = roleRepository.findByElectionEventAndId(userData.electionEvent(), circularReferenceCheckRequest.getNewIncludedRoleId());
		for (Role r : roleRepository.getRoleWithIncludedRoles(includedRole)) {
			if (r.equals(parentRole)) {
				return true;
			}
		}
		return false;
	}
}
