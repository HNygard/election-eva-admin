package no.evote.validation;

import java.io.Serializable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GpsValidator implements ConstraintValidator<Gps, String>, Serializable {

	@Override
	public void initialize(final Gps constraint) {
		// do nothing
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
		return true;
	}
}
