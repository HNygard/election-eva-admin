package no.valg.eva.admin.counting.domain.validation;

import static java.lang.String.format;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.configuration.domain.model.MvArea;

/**
 * Validates protocol count.
 * <p/>
 * This class enforces integrity constraints. Validate user inputs using {@link ProtocolCount#validate()}.
 */
public class ProtocolCountValidator implements CountValidator<ProtocolCount> {
	
	private final Set<CountingMode> validCountingModes = new HashSet<>();
	{
		validCountingModes.add(CountingMode.BY_POLLING_DISTRICT);
		validCountingModes.add(CountingMode.CENTRAL);
		validCountingModes.add(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
	}
	
	@Override
	public void applyValidationRules(ProtocolCount value, CountContext context, MvArea countingArea, CountingMode countingMode, ReportingUnitTypeId reportingUnitTypeId) {

		countingArea.validateAreaLevel(POLLING_DISTRICT);
				
		if (!countingArea.getMunicipality().isRequiredProtocolCount()) {
			throw new ValidateException("Protocol count should be required for municipality with id: " + countingArea.getMunicipality().getId());
		}

		if (!validCountingModes.contains(countingMode)) {
			throw new ValidateException(format("illegal count mode for protocol count: <%s>", countingMode.getDescription()));
		}

		if (isNullOrEmpty(value.getDailyMarkOffCountsForOtherContests()) && value.getBallotCountForOtherContests() != null) {
			throw new ValidateException("Cannot contain ballot count for other contests, when not borough contest");
		}

		if (!isNullOrEmpty(value.getDailyMarkOffCountsForOtherContests()) && value.getBallotCountForOtherContests() == null) {
			throw new ValidateException("Ballot count for other contests is required when borough contest");
		}
	}

	private boolean isNullOrEmpty(Collection c) {
		return c == null || c.isEmpty();
	}
}
