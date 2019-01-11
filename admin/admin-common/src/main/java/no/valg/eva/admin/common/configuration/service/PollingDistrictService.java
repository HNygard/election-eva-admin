package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistricts;
import no.valg.eva.admin.common.configuration.model.local.PollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;

public interface PollingDistrictService extends Serializable {

	List<PollingDistrict> findRegularPollingDistrictsByArea(UserData userData, AreaPath areaPath, boolean includeParents);

	List<TechnicalPollingDistrict> findTechnicalPollingDistrictsByArea(UserData userData, AreaPath areaPath);

	TechnicalPollingDistrict findTechnicalPollingDistrictByAreaAndId(UserData userData, AreaPath areaPath, String id);

	ParentPollingDistricts findParentPollingDistrictsByArea(UserData userData, AreaPath areaPath);

	TechnicalPollingDistrict saveTechnicalPollingDistrict(UserData userData, TechnicalPollingDistrict technicalPollingDistrict);

	ParentPollingDistrict saveParentPollingDistrict(UserData userData, ParentPollingDistrict parentPollingDistrict);

	void deleteTechnicalPollingDistrict(UserData userData, TechnicalPollingDistrict technicalPollingDistrict);

	void deleteParentPollingDistrict(UserData userData, ParentPollingDistrict parentPollingDistrict);

}
