package no.evote.service.rbac;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import no.evote.validation.OperatorValidationManual;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.OperatorRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public class OperatorServiceBean {
	private final Validator validator;
	@Inject
	private OperatorRoleServiceBean operatorRoleService;
	@Inject
	private OperatorRepository operatorRepository;

	public OperatorServiceBean() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	public List<String> validateOperator(Operator operator, ElectionEvent event) {
		List<String> validationFeedbackList = new ArrayList<>();
		Set<ConstraintViolation<Operator>> constraintViolations = validator.validate(operator, OperatorValidationManual.class);

		if (!constraintViolations.isEmpty()) {
			validationFeedbackList
					.addAll(constraintViolations.stream().filter(c -> c != null).map(ConstraintViolation::getMessage).collect(Collectors.toList()));
		}
		if (isOperatorIdInElectionEvent(operator, event)) {
			validationFeedbackList.add("@rbac.operator.duplicateID");
		}

		return validationFeedbackList;

	}

	private boolean isOperatorIdInElectionEvent(Operator operator, ElectionEvent event) {
		Operator existingOperator = operatorRepository.findByElectionEventsAndId(event.getPk(), operator.getId());
		return existingOperator != null && !existingOperator.getPk().equals(operator.getPk());
	}

	public Set<Operator> getCollisionsIfSetMutEx(Role currentRole) {
		Set<Operator> collidingOperators = new HashSet<>();
		for (Operator o : operatorRepository.findAll()) {
			for (OperatorRole or : operatorRoleService.getOperatorRoles(o)) {
				if (or.getRole().equals(currentRole) && operatorRoleService.getOperatorRoles(o).size() > 1) {
					collidingOperators.add(o);
				}
			}
		}
		return collidingOperators;
	}
}
