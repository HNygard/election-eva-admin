package no.valg.eva.admin.counting.domain.validation;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.configuration.domain.model.MvArea;

/**
 * Validates generic count.
 * @param <T>
 */
public interface CountValidator<T> {
	void applyValidationRules(T count, CountContext context, MvArea countingArea, CountingMode countingMode, ReportingUnitTypeId reportingUnitTypeId);
}
