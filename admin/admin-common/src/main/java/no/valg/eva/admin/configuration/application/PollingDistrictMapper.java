package no.valg.eva.admin.configuration.application;

import java.util.Collections;

import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.PlaceSortById;
import no.valg.eva.admin.common.configuration.model.local.RegularPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;

public final class PollingDistrictMapper {

	private PollingDistrictMapper() {
	}

	public static no.valg.eva.admin.common.configuration.model.local.PollingDistrict toPollingDistrict(PollingDistrict dbPollingDistrict) {
		if (dbPollingDistrict.type() == PollingDistrictType.TECHNICAL) {
			return toTechnicalPollingDistrict(dbPollingDistrict);
		} else if (dbPollingDistrict.type() == PollingDistrictType.PARENT) {
			return toParentPollingDistrict(dbPollingDistrict);
		}
		return toRegularPollingDistrict(dbPollingDistrict);
	}

	public static TechnicalPollingDistrict toTechnicalPollingDistrict(PollingDistrict dbPollingDistrict) {
		if (dbPollingDistrict == null) {
			return null;
		}
		return addDefaultValues(dbPollingDistrict, new TechnicalPollingDistrict(dbPollingDistrict.areaPath(), dbPollingDistrict.getAuditOplock()));
	}

	public static RegularPollingDistrict toRegularPollingDistrict(PollingDistrict dbPollingDistrict) {
		return addDefaultValues(dbPollingDistrict, new RegularPollingDistrict(dbPollingDistrict.areaPath(), dbPollingDistrict.type(),
				dbPollingDistrict.getAuditOplock()));
	}

	public static ParentPollingDistrict toParentPollingDistrict(PollingDistrict dbPollingDistrict) {
		ParentPollingDistrict result = addDefaultValues(dbPollingDistrict,
				new ParentPollingDistrict(dbPollingDistrict.areaPath(), dbPollingDistrict.getAuditOplock()));
		for (PollingDistrict child : dbPollingDistrict.getChildPollingDistricts()) {
			result.getChildren().add(toRegularPollingDistrict(child));
		}
		Collections.sort(result.getChildren(), new PlaceSortById<>());
		return result;
	}

	public static PollingDistrict toPollingDistrict(PollingDistrict dbDistrict, no.valg.eva.admin.common.configuration.model.local.PollingDistrict district) {
		dbDistrict.setId(district.getId());
		dbDistrict.setName(district.getName());
		dbDistrict.setTechnicalPollingDistrict(district.getType() == PollingDistrictType.TECHNICAL);
		dbDistrict.setParentPollingDistrict(district.getType() == PollingDistrictType.PARENT);
		dbDistrict.setMunicipality(district.getType() == PollingDistrictType.MUNICIPALITY);
		dbDistrict.setChildPollingDistrict(district.getType() == PollingDistrictType.CHILD);
		return dbDistrict;
	}

	private static <T extends no.valg.eva.admin.common.configuration.model.local.PollingDistrict> T addDefaultValues(PollingDistrict db, T pollingDistrict) {
		pollingDistrict.setPk(db.getPk());
		pollingDistrict.setId(db.getId());
		pollingDistrict.setName(db.getName());
		pollingDistrict.setBorough(BoroughMapper.toBorough(db.getBorough()));
		return pollingDistrict;
	}
}
