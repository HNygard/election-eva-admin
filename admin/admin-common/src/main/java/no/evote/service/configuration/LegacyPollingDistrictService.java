package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.dto.ConfigurationDto;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;

public interface LegacyPollingDistrictService extends Serializable {

	List<PollingDistrict> findPollingDistrictsForParent(UserData userData, PollingDistrict pollingDistrictParent);

	Boolean municipalityProxyExists(UserData userData, Long municipalityPk);

	PollingDistrict findPollingDistrictById(UserData userData, Long boroughPk, String id);

	/**
	 * @deprecated Replaced by
	 *             {@link no.valg.eva.admin.common.configuration.service.PollingDistrictService#saveTechnicalPollingDistrict(UserData, TechnicalPollingDistrict)}
	 */
	@Deprecated
	PollingDistrict create(UserData userData, PollingDistrict pollingDistrict);

	/**
	 * @deprecated Replaced by
	 *             {@link no.valg.eva.admin.common.configuration.service.PollingDistrictService#saveTechnicalPollingDistrict(UserData, TechnicalPollingDistrict)}
	 */
	@Deprecated
	PollingDistrict update(UserData userData, PollingDistrict pollingDistrict);

	/**
	 * @deprecated Replaced by
	 *             {@link no.valg.eva.admin.common.configuration.service.PollingDistrictService#deleteTechnicalPollingDistrict(UserData, TechnicalPollingDistrict)}
	 */
	@Deprecated
	void delete(UserData userData, PollingDistrict pollingDistrict);

	PollingDistrict findByPk(UserData userData, Long pk);

	List<ConfigurationDto> getPollingDistrictsMissingVoters(UserData userData, KommuneSti kommuneSti);
}
