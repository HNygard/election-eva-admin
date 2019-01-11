package no.valg.eva.admin.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PastLocalDateValidator.class)
@Documented
public @interface PastLocalDate {
	String message() default "must be in the past";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
