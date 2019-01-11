package no.evote.validation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static no.evote.constants.EvoteConstants.VALID_GPS_PATTERN;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

@ReportAsSingleViolation
@Pattern(regexp = VALID_GPS_PATTERN, message = Gps.MESSAGE)
@Constraint(validatedBy = GpsValidator.class)
@Target({ METHOD })
@Retention(RUNTIME)
public @interface Gps {

	String MESSAGE = "{@validation.gps}";

	String message() default MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
