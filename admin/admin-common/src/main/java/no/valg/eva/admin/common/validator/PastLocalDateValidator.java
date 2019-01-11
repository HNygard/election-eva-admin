package no.valg.eva.admin.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.joda.time.LocalDate;

public class PastLocalDateValidator implements ConstraintValidator<PastLocalDate, LocalDate> {
	public void initialize(PastLocalDate past) { }

	public boolean isValid(LocalDate localDate, ConstraintValidatorContext context) {
		return localDate == null || localDate.isBefore(LocalDate.now());
	}
}
