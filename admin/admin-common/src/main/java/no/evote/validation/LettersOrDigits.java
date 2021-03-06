package no.evote.validation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = LettersOrDigitsValidator.class)
@Target({ METHOD })
@Retention(RUNTIME)
public @interface LettersOrDigits {

	String extraChars() default " .-'/";

	String MESSAGE = "{@validation.lettersOrDigits}";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
