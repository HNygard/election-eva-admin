package no.evote.service.configuration;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;
import no.valg.eva.admin.configuration.domain.model.County;

import java.io.Serializable;

public interface CountyService extends Serializable {
	County create(UserData userData, County county);

	County update(UserData userData, County county);

	County updateScanningConfiguration(UserData userData, County county);

	void delete(UserData userData, County county);

	County findCountyById(UserData userData, Long countryPk, String id);

	County findByPk(UserData userData, Long pk);

	County findByPkWithScanningConfig(UserData userData, Long pk);

	County findByMunicipality(UserData userData, Long pk);

	CountyConfigStatus findCountyStatusByArea(UserData userData, AreaPath areaPath);

	CountyConfigStatus saveCountyConfigStatus(UserData userData, CountyConfigStatus countyConfigStatus);

	County reject(UserData userData, Long countyPk);

	County approve(UserData userData, Long countyPk);
}
