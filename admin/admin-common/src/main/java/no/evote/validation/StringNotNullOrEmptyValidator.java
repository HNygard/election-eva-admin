package no.evote.validation;

import java.io.Serializable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StringNotNullOrEmptyValidator implements ConstraintValidator<StringNotNullOrEmpty, String>, Serializable {

	@Override
	public void initialize(final StringNotNullOrEmpty constraint) {
		// Do nothing. Exists to satisfy ConstraintValidator interface.
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
        return value != null && value.length() > 0;
	}
}
