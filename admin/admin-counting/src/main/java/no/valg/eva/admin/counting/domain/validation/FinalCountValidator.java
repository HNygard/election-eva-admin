package no.valg.eva.admin.counting.domain.validation;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.Set;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.configuration.domain.model.MvArea;

/**
 * Validates preliminary counts.
 */
public class FinalCountValidator implements CountValidator<FinalCount> {

	private final Set<CountingMode> validCountingModesForValgstyret = new HashSet<>();
	{
		validCountingModesForValgstyret.add(CountingMode.CENTRAL);
		validCountingModesForValgstyret.add(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		validCountingModesForValgstyret.add(CountingMode.BY_POLLING_DISTRICT);
		validCountingModesForValgstyret.add(CountingMode.BY_TECHNICAL_POLLING_DISTRICT);
	}

	@Override
	public void applyValidationRules(FinalCount count, CountContext context, MvArea countingArea, CountingMode countingMode,
			ReportingUnitTypeId reportingUnitTypeId) {
		if (reportingUnitTypeId == ReportingUnitTypeId.VALGSTYRET && !validCountingModesForValgstyret.contains(countingMode)) {
			throw new ValidateException(format(
					"illegal count mode for final count: <%s> when reporting unit is <%s>",
					countingMode.getDescription(),
					reportingUnitTypeId.name()));
		}
	}
}
