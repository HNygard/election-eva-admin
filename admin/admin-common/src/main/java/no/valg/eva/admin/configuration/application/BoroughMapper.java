package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.common.configuration.model.local.Borough;

public final class BoroughMapper {

	private BoroughMapper() {
	}

	public static Borough toBorough(no.valg.eva.admin.configuration.domain.model.Borough dbBorough) {
		if (dbBorough == null) {
			return null;
		}
		Borough result = new Borough(dbBorough.areaPath(), dbBorough.getAuditOplock());
		result.setPk(dbBorough.getPk());
		result.setId(dbBorough.getId());
		result.setName(dbBorough.getName());
		return result;
	}
}
