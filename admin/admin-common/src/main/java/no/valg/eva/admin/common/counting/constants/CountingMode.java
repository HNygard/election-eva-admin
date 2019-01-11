package no.valg.eva.admin.common.counting.constants;

import static java.lang.String.format;

/**
 * Counting configurations.
 */
public enum CountingMode {
	/** Lokalt - fordelt på krets */
	BY_POLLING_DISTRICT("@report_count_category.count_mode_select.by_polling_district", false, true, false),

	/** Sentralt - fordelt på teknisk krets */
	BY_TECHNICAL_POLLING_DISTRICT("@report_count_category.count_mode_select.by_technical_polling_district", true, false, true),

	/** Sentralt - samlet */
	CENTRAL("@report_count_category.count_mode_select.central", true, false, false),

	/** Sentralt - fordelt på krets */
	CENTRAL_AND_BY_POLLING_DISTRICT("@report_count_category.count_mode_select.central_and_by_polling_district", true, true, false);

	private final String description;
	private final boolean centralPreliminaryCount;
	private final boolean pollingDistrictCount;
	private final boolean technicalPollingDistrictCount;

	CountingMode(
		final String description,
		final boolean centralPreliminaryCount,
		final boolean pollingDistrictCount,
		final boolean technicalPollingDistrictCount) {

		this.description = description;
		this.centralPreliminaryCount = centralPreliminaryCount;
		this.pollingDistrictCount = pollingDistrictCount;
		this.technicalPollingDistrictCount = technicalPollingDistrictCount;
	}

	/**
	 * @return counting mode based on the three boolean parameters
	 */
	public static CountingMode getCountingMode(
			final boolean centralPreliminaryCount,
			final boolean pollingDistrictCount,
			final boolean technicalPollingDistrictCount) {

		if (centralPreliminaryCount) {
			if (!pollingDistrictCount && technicalPollingDistrictCount) {
				return CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
			}
			if (pollingDistrictCount && !technicalPollingDistrictCount) {
				return CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
			}
			if (!pollingDistrictCount) {
				return CountingMode.CENTRAL;
			}
		}
		if (pollingDistrictCount && !technicalPollingDistrictCount) {
			return CountingMode.BY_POLLING_DISTRICT;
		}
		String message = "unknown counting mode: centralPreliminaryCount=<%s>, pollingDistrictCount=<%s>, technicalPollingDistrictCount=<%s>";
		throw new IllegalArgumentException(format(message, centralPreliminaryCount, pollingDistrictCount, technicalPollingDistrictCount));
	}

	public String getDescription() {
		return description;
	}

	public boolean isCentralPreliminaryCount() {
		return centralPreliminaryCount;
	}

	public boolean isPollingDistrictCount() {
		return pollingDistrictCount;
	}

	public boolean isTechnicalPollingDistrictCount() {
		return technicalPollingDistrictCount;
	}

	public boolean isPollingDistrictOrTechnicalPollingDistrictCount() {
		return isPollingDistrictCount() || isTechnicalPollingDistrictCount();
	}
}
