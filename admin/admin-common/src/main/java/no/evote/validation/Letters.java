package no.evote.validation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = LettersValidator.class)
@Target({ METHOD })
@Retention(RUNTIME)
public @interface Letters {

	String extraChars() default " .-'/";

	String MESSAGE = "{@validation.letters}";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
