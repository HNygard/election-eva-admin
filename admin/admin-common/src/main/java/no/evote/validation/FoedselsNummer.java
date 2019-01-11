package no.evote.validation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

import org.hibernate.validator.constraints.NotEmpty;

@NotEmpty
@Documented
@ReportAsSingleViolation
@Constraint(validatedBy = FoedselsNummerValidator.class)
@Target({ METHOD })
@Retention(RUNTIME)
public @interface FoedselsNummer {
	String MESSAGE = "{@validation.id.regex}";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
