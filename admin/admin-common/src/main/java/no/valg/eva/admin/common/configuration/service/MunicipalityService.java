package no.valg.eva.admin.common.configuration.service;

import no.evote.dto.ConfigurationDto;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.local.MunicipalityConfigStatus;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;

import java.io.Serializable;
import java.util.List;

public interface MunicipalityService extends Serializable {

	Municipality findMunicipalityById(UserData userData, Long countyPk, String municipalityId);

	Municipality create(UserData userData, Municipality municipality);

	Municipality update(UserData userData, Municipality municipality);

	Municipality updateScanningConfiguration(UserData userData, Municipality municipality);

	void delete(UserData userData, Municipality municipality);

	Municipality findByPk(Long municipalityPk);
	
	Municipality findByPkWithScanningConfig(Long municipalityPk);

	Locale getLocale(UserData userData, Municipality currentMunicipality);

	MunicipalityStatusEnum getStatus(UserData userData, KommuneSti kommuneSti);

	List<ConfigurationDto> findVotersWithoutPollingDistricts(UserData userData, KommuneSti kommuneSti);

	Municipality reject(UserData userData, Long municipalityPk);

	Municipality approve(UserData userData, Long municipalityPk);

	void setRequiredProtocolCountForElectionEvent(UserData userData, Long electionEventPk, boolean requiredProtocolCount);

	boolean getRequiredProtocolCountForElectionEvent(UserData userData, Long electionEventPk);

	MunicipalityConfigStatus findMunicipalityStatusByArea(UserData userData, AreaPath areaPath);

	MunicipalityConfigStatus saveMunicipalityConfigStatus(UserData userData, MunicipalityConfigStatus municipalityConfigStatus, ElectionPath electionGroupPath);

	void markerAvkryssningsmanntallKjort(UserData userData, AreaPath areaPath, boolean kjort);

    List<OpeningHours> getOpeningHours(UserData userData, Municipality municipality);

	void saveOpeningHours(UserData userData, Municipality municipality,
                          List<OpeningHours> openingHours, boolean overwriteExisting, AreaPath selectedAreaPath);
}
