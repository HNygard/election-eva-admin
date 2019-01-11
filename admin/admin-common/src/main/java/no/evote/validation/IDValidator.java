package no.evote.validation;

import java.io.Serializable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IDValidator implements ConstraintValidator<ID, String>, Serializable {

	private ID id;

	@Override
	public void initialize(final ID constraint) {
		this.id = constraint;
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext constraintValidatorContext) {
		if (value == null) {
			return false;
		}

		return value.length() == id.size();
	}
}
