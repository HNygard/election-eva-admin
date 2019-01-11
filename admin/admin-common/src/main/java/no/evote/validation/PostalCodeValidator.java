package no.evote.validation;

import java.io.Serializable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PostalCodeValidator implements ConstraintValidator<PostalCode, String>, Serializable {

	public static final int POSTAL_CODE_LENGTH = 4;

	@Override
	public void initialize(final PostalCode constraint) {
		// Intentionally empty
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
		if (value == null || value.trim().isEmpty()) {
			return true;
		}

		if (value.length() != POSTAL_CODE_LENGTH) {
			return false;
		}
		return true;
	}
}
