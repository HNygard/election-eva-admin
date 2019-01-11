package no.evote.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PartyNameValidator extends ValidatorBase implements ConstraintValidator<PartyNameCharacters, String> {
	
	@Override
	public void initialize(final PartyNameCharacters constraintAnnotation) {
		extraChars = constraintAnnotation.extraChars();
	}

	@Override
    public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
        return isValid(string);
	}

}
