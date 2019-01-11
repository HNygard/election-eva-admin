package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MunicipalityLocalConfigStatus;

public final class MunicipalityConfigStatusMapper {

	private MunicipalityConfigStatusMapper() {
	}

	public static MunicipalityConfigStatus toMunicipalityConfigStatus(Municipality municipality) {
		MunicipalityConfigStatus result = new MunicipalityConfigStatus(municipality.areaPath(), municipality.getName());
		if (municipality.getLocalConfigStatus() != null) {
			result = new MunicipalityConfigStatus(municipality.areaPath(), municipality.getName(), municipality.getLocalConfigStatus().getAuditOplock());
			result.setAdvancePollingPlaces(municipality.getLocalConfigStatus().isAdvancePollingPlaces());
			result.setElectionPollingPlaces(municipality.getLocalConfigStatus().isElectionPollingPlaces());
			result.setElectionCard(municipality.getLocalConfigStatus().isElectionCard());
			result.setPollingDistricts(municipality.getLocalConfigStatus().isPollingDistricts());
			result.setTechPollingDistricts(municipality.getLocalConfigStatus().isTechPollingDistricts());
			result.setReportingUnitStemmestyre(municipality.getLocalConfigStatus().isReportingUnitStemmestyre());
			result.setReportingUnitValgstyre(municipality.getLocalConfigStatus().isReportingUnitValgstyre());
			result.setPollingStations(municipality.getLocalConfigStatus().isPollingStations());
			result.setLanguage(municipality.getLocalConfigStatus().isLanguage());
			result.setCountCategories(municipality.getLocalConfigStatus().isCountCategories());
			result.setListProposals(municipality.getLocalConfigStatus().isListProposals());
			result.setElectronicMarkoffs(municipality.getLocalConfigStatus().isElectronicMarkoffs());
			result.setScanning(municipality.getLocalConfigStatus().isScanning());
		}
		result.setUseElectronicMarkoffs(municipality.isElectronicMarkoffs());
		result.setLocaleId(municipality.getLocale().toLocaleId());
		return result;
	}

	public static MunicipalityLocalConfigStatus toMunicipalityLocalConfigStatus(MunicipalityLocalConfigStatus dbStatus, MunicipalityConfigStatus status) {
		dbStatus.setAdvancePollingPlaces(status.isAdvancePollingPlaces());
		dbStatus.setElectionPollingPlaces(status.isElectionPollingPlaces());
		dbStatus.setElectionCard(status.isElectionCard());
		dbStatus.setPollingDistricts(status.isPollingDistricts());
		dbStatus.setTechPollingDistricts(status.isTechPollingDistricts());
		dbStatus.setReportingUnitStemmestyre(status.isReportingUnitStemmestyre());
		dbStatus.setReportingUnitValgstyre(status.isReportingUnitValgstyre());
		dbStatus.setPollingStations(status.isPollingStations());
		dbStatus.setLanguage(status.isLanguage());
		dbStatus.setCountCategories(status.isCountCategories());
		dbStatus.setListProposals(status.isListProposals());
		dbStatus.setElectronicMarkoffs(status.isElectronicMarkoffs());
		dbStatus.setScanning(status.isScanning());
		return dbStatus;
	}
}
