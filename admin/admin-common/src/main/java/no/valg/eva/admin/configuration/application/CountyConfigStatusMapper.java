package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyLocalConfigStatus;

public final class CountyConfigStatusMapper {

	private CountyConfigStatusMapper() {
	}

	public static CountyConfigStatus toCountyConfigStatus(County county) {
		CountyConfigStatus result = new CountyConfigStatus(county.areaPath(), county.getName());
		if (county.getLocalConfigStatus() != null) {
			result = new CountyConfigStatus(county.areaPath(), county.getName(), county.getLocalConfigStatus().getAuditOplock());
			result.setReportingUnitFylkesvalgstyre(county.getLocalConfigStatus().isReportingUnitFylkesvalgstyre());
			result.setListProposals(county.getLocalConfigStatus().isListProposals());
			result.setLanguage(county.getLocalConfigStatus().isLanguage());
			result.setScanning(county.getLocalConfigStatus().isScanning());
		}
		result.setLocaleId(county.getLocale().toLocaleId());
		return result;
	}

	public static CountyLocalConfigStatus toCountyLocalConfigStatus(CountyLocalConfigStatus dbStatus, CountyConfigStatus status) {
		dbStatus.setReportingUnitFylkesvalgstyre(status.isReportingUnitFylkesvalgstyre());
		dbStatus.setListProposals(status.isListProposals());
		dbStatus.setLanguage(status.isLanguage());
		dbStatus.setScanning(status.isScanning());
		return dbStatus;
	}
}
