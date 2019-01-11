package no.evote.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LettersOrDigitsValidator extends ValidatorBase implements ConstraintValidator<LettersOrDigits, String> {

	public void initialize(final String extraChars) {
		this.extraChars = extraChars;
	}

	@Override
	public void initialize(final LettersOrDigits constraintAnnotation) {
		extraChars = constraintAnnotation.extraChars();
	}

	@Override
	public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
		return isValid(string);
	}

}
