package no.evote.validation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

@ReportAsSingleViolation
@Pattern(regexp = "([0-9]{4})?", message = PostalCode.MESSAGE)
@Constraint(validatedBy = PostalCodeValidator.class)
@Target({ METHOD })
@Retention(RUNTIME)
public @interface PostalCode {

	String MESSAGE = "{@validation.postalCode.regex}";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
