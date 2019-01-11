package no.evote.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = VotingPollingPlaceValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
public @interface VotingPollingPlace {
	String message() default "{@validation.voting.pollingplace}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
