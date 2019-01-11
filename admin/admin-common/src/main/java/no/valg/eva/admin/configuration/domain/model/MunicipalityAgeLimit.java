package no.valg.eva.admin.configuration.domain.model;


import java.util.Date;

import no.valg.eva.admin.common.MunicipalityId;

import org.joda.time.LocalDate;

/**
 * Value object intended to hold the (derived) age limit for deciding eligibility for a voter
 */
public class MunicipalityAgeLimit {
	
	private final MunicipalityId municipalityId;
	private final LocalDate mustBeBornBefore;

	public MunicipalityAgeLimit(String municipalityId, Date mustBeBornBefore) {
		this.municipalityId = new MunicipalityId(municipalityId);
		this.mustBeBornBefore = new LocalDate(mustBeBornBefore);
	}

	public MunicipalityId getMunicipalityId() {
		return municipalityId;
	}

	public LocalDate getMustBeBornBefore() {
		return mustBeBornBefore;
	}
}
