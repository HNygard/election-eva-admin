package no.evote.validation;

import java.io.Serializable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LettersValidator implements ConstraintValidator<Letters, String>, Serializable {

	private String extraChars;

	@Override
	public void initialize(final Letters constraintAnnotation) {
		extraChars = constraintAnnotation.extraChars();
	}

	public void initialize(final String extraChars) {
		this.extraChars = extraChars;
	}
	
	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (value == null || value.length() == 0) {
			return true;
		}

		for (int i = 0; i < value.length(); i++) {
			Character currentChar = value.charAt(i);

            if (Character.isLetter(currentChar) || extraChars.indexOf(currentChar) > -1) {
				continue;
			}
			return false;
		}

		return true;
	}

}
