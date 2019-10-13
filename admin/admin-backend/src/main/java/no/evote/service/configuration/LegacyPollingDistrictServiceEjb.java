package no.evote.service.configuration;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Oversikt_Manntallsavvik;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.dto.ConfigurationDto;
import no.evote.exception.EvoteException;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.PollingDistrictAuditEvent;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "LegacyPollingDistrictService")



@Default
@Remote(LegacyPollingDistrictService.class)
public class LegacyPollingDistrictServiceEjb implements LegacyPollingDistrictService {

	@Inject
	private PollingDistrictRepository pollingDistrictRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private MunicipalityRepository municipalityRepository;

	/**
	 * @deprecated Replaced by
	 *             {@link no.valg.eva.admin.common.configuration.service.PollingDistrictService#saveTechnicalPollingDistrict(UserData, TechnicalPollingDistrict)}
	 */
	@Deprecated
	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = PollingDistrictAuditEvent.class, eventType = Create)
	public PollingDistrict create(final UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.BOROUGH) final PollingDistrict pollingDistrict) {
		return pollingDistrictRepository.create(userData, pollingDistrict);
	}

	/**
	 * @deprecated Replaced by
	 *             {@link no.valg.eva.admin.common.configuration.service.PollingDistrictService#saveTechnicalPollingDistrict(UserData, TechnicalPollingDistrict)}
	 *             .admin.common.configuration.model.local.TechnicalPollingDistrict)}
	 */
	@Deprecated
	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = PollingDistrictAuditEvent.class, eventType = Update)
	public PollingDistrict update(final UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.POLLING_DISTRICT) final PollingDistrict pollingDistrict) {
		return pollingDistrictRepository.update(userData, pollingDistrict);
	}

	/**
	 * @deprecated Replaced by
	 *             {@link no.valg.eva.admin.common.configuration.service.PollingDistrictService#deleteTechnicalPollingDistrict(UserData, TechnicalPollingDistrict)}
	 */
	@Deprecated
	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = PollingDistrictAuditEvent.class, eventType = Delete)
	public void delete(final UserData userData, PollingDistrict pollingDistrict) {
		// Check first that the polling district has no parents
		PollingDistrict pd = pollingDistrictRepository.findByPk(pollingDistrict.getPk());
		if (pd.getPollingDistrict() != null) {
			throw new EvoteException("Not possible to delete polling districts that have parents.");
		}
		pollingDistrictRepository.delete(userData, pollingDistrict.getPk());
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public PollingDistrict findByPk(final UserData userData, final Long pk) {
		return pollingDistrictRepository.findByPk(pk);
	}

	@Override
	@SuppressWarnings(EvoteConstants.WARNING_UNCHECKED)
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public PollingDistrict findPollingDistrictById(final UserData userData, final Long boroughPk, final String id) {
		return pollingDistrictRepository.findPollingDistrictById(boroughPk, id);
	}

	/**
	 * Retrieves list of polling districts that has this specific polling district as parent
	 */
	@Override
	@SuppressWarnings(EvoteConstants.WARNING_UNCHECKED)
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public List<PollingDistrict> findPollingDistrictsForParent(final UserData userData, final PollingDistrict pollingDistrictParent) {
		return pollingDistrictRepository.findPollingDistrictsForParent(pollingDistrictParent);
	}

	/**
	 * Determines if a polling district is municipality specific. These polling districts should ideally have an id of 0000.
	 */
	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public Boolean municipalityProxyExists(final UserData userData, final Long municipalityPk) {
		return pollingDistrictRepository.municipalityProxyExists(municipalityPk);
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Oversikt_Manntallsavvik, type = READ)
	public List<ConfigurationDto> getPollingDistrictsMissingVoters(UserData userData, KommuneSti kommuneSti) {
		Municipality municipality = getMunicipality(userData, kommuneSti.areaPath());
		return pollingDistrictToLocalConfiguration(getPollingDistrictsMissingVoters(municipality.getPk()));
	}

	private List<PollingDistrict> getPollingDistrictsMissingVoters(Long municipalityPk) {
		List<PollingDistrict> districts = new ArrayList<>();
		List<PollingDistrict> tmp = pollingDistrictRepository.getPollingDistrictsByMunicipality(municipalityPk);
		for (PollingDistrict pd : tmp) {
			Long mvAreaPk = mvAreaRepository.findSingleByPollingDistrictIdAndMunicipalityPk(pd.getId(), municipalityPk).getPk();
			if (!pd.isParentPollingDistrict() && !pd.isMunicipality() && !pd.isTechnicalPollingDistrict() && !voterRepository.hasVoters(mvAreaPk)) {
				districts.add(pd);
			}
		}
		return districts;
	}

	private List<ConfigurationDto> pollingDistrictToLocalConfiguration(final List<PollingDistrict> districts) {
		List<ConfigurationDto> configData = new ArrayList<>();
		for (PollingDistrict district : districts) {
			configData.add(new ConfigurationDto(district.getBorough().getId() + "." + district.getId(), district.getBorough().getName() + ", "
					+ district.getName()));
		}
		return configData;
	}

	private Municipality getMunicipality(UserData userData, AreaPath areaPath) {
		return municipalityRepository.municipalityByElectionEventAndId(userData.getElectionEventPk(), areaPath.getMunicipalityId());
	}
}
