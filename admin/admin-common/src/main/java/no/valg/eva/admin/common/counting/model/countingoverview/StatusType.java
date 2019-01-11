package no.valg.eva.admin.common.counting.model.countingoverview;

import static java.lang.String.format;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.FINAL;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static no.valg.eva.admin.common.counting.model.countingoverview.CountingStatus.Value.COUNT_NOT_REQUIRED;

import java.util.function.BiFunction;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.counting.model.CountQualifier;

public enum StatusType {
	PROTOCOL_COUNT_STATUS("@count.overview.protocol", (qualifier, reportingUnitAreaLevel) -> qualifier == PROTOCOL),
	PRELIMINARY_COUNT_STATUS("@count.overview.preliminary", (qualifier, reportingUnitAreaLevel) -> qualifier == PRELIMINARY),
	FINAL_COUNT_STATUS("@count.overview.final",	(qualifier, reportingUnitAreaLevel) -> qualifier == FINAL),
	MUNICIPALITY_FINAL_COUNT_STATUS("@count.overview.final_municipality",
			(qualifier, reportingUnitAreaLevel) -> qualifier == FINAL && reportingUnitAreaLevel == MUNICIPALITY),
	COUNTY_FINAL_COUNT_STATUS("@count.overview.final", (qualifier, reportingUnitAreaLevel) -> qualifier == FINAL && reportingUnitAreaLevel == COUNTY),
	REJECTED_BALLOTS_STATUS("@count.overview.rejected", (qualifier, reportingUnitAreaLevel) -> qualifier == FINAL),
	COUNTY_REJECTED_BALLOTS_STATUS("@count.overview.rejected", (qualifier, reportingUnitAreaLevel) -> qualifier == FINAL && reportingUnitAreaLevel == COUNTY);

	private String header;
	private BiFunction<CountQualifier, AreaLevelEnum, Boolean> qualifierAndReportingUnitAreaLevelFilter;

	StatusType(String header, BiFunction<CountQualifier, AreaLevelEnum, Boolean> qualifierAndReportingUnitAreaLevelFilter) {
		this.header = header;
		this.qualifierAndReportingUnitAreaLevelFilter = qualifierAndReportingUnitAreaLevelFilter;
	}

	public String getHeader() {
		return header;
	}

	public Status defaultStatus() {
		switch (this) {
		case PROTOCOL_COUNT_STATUS:
		case PRELIMINARY_COUNT_STATUS:
		case FINAL_COUNT_STATUS:
		case MUNICIPALITY_FINAL_COUNT_STATUS:
		case COUNTY_FINAL_COUNT_STATUS:
			return new CountingStatus(this);
		case REJECTED_BALLOTS_STATUS:
			return new RejectedBallotsStatus(false);
		case COUNTY_REJECTED_BALLOTS_STATUS:
			return new RejectedBallotsStatus(true);
		default:
			throw new IllegalStateException(format("unknown status type: %s", this));
		}
	}

	public Status countNotRequiredStatus() {
		return new CountingStatus(this, COUNT_NOT_REQUIRED);
	}

	public boolean filter(CountQualifier qualifier, AreaLevelEnum reportingUnitAreaLevel) {
		return qualifierAndReportingUnitAreaLevelFilter.apply(qualifier, reportingUnitAreaLevel);
	}
}
