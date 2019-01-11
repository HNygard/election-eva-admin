package no.evote.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import no.valg.eva.admin.voting.domain.model.Voting;

public class VotingPollingPlaceValidator implements ConstraintValidator<VotingPollingPlace, Voting> {

	@Override
	public void initialize(final VotingPollingPlace constraintAnnotation) {
		// No initialization needed
	}

	/**
	 * Polling place cannot be null.
	 */
	@Override
	public boolean isValid(final Voting voting, final ConstraintValidatorContext context) {
		return voting.getPollingPlace() != null;
	}

}
