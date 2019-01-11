package no.evote.service.configuration;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.CountyAuditEventForCentralConfiguration;
import no.valg.eva.admin.common.auditlog.auditevents.CountyAuditEventForLocalConfiguration;
import no.valg.eva.admin.common.auditlog.auditevents.config.CountyConfigStatusAuditEvent;
import no.valg.eva.admin.common.configuration.model.local.CountyConfigStatus;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.application.CountyConfigStatusMapper;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.repository.CountyRepository;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.StatusChanged;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;
import static no.valg.eva.admin.common.auditlog.AuditedObjectSource.Parameters;
import static no.valg.eva.admin.common.auditlog.AuditedObjectSource.ReturnValue;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Konfigurasjon;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Godkjenne;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Oppheve;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "CountyService")
@Remote(CountyService.class)
public class CountyServiceEjb implements CountyService {
	@Inject
	private CountyServiceBean countyServiceBean;
	@Inject
	private CountyRepository countyRepository;
	@Inject
	private LocaleRepository localeRepository;

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = CountyAuditEventForCentralConfiguration.class, eventType = Create, objectSource = ReturnValue)
	public County create(final UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.COUNTRY) final County county) {
		return countyServiceBean.create(userData, county);
	}

	@Override
	@Security(accesses = { Konfigurasjon_Geografi }, type = WRITE)
	@AuditLog(eventClass = CountyAuditEventForCentralConfiguration.class, eventType = Update, objectSource = ReturnValue)
	public County update(final UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.COUNTY) final County county) {
		return countyRepository.update(userData, county);
	}

	@Override
	@Security(accesses = { Konfigurasjon_Grunnlagsdata_Redigere }, type = WRITE)
	@AuditLog(eventClass = CountyAuditEventForLocalConfiguration.class, eventType = Update, objectSource = ReturnValue)
	public County updateScanningConfiguration(final UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.COUNTY) final County county) {
		County dbCounty = countyRepository.findByPk(county.getPk()); // Dette hentes ut for å unngå at denne metoden skal brukes for å endre County-objektet
		dbCounty.setScanningConfig(county.getScanningConfig());
		return countyRepository.update(userData, dbCounty);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = CountyAuditEventForCentralConfiguration.class, eventType = Delete, objectSource = Parameters)
	public void delete(final UserData userData, County county) {
		countyRepository.delete(userData, county.getPk());
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public County findByPk(UserData userData, Long pk) {
		return countyRepository.findByPk(pk);
	}

	@Override
	@Security(accesses = { Konfigurasjon_Geografi, Konfigurasjon_Grunnlagsdata_Redigere }, type = READ)
	public County findByPkWithScanningConfig(UserData userData, Long pk) {
		return countyRepository.findByPkWithScanningConfig(pk);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public County findCountyById(final UserData userData, final Long countryPk, final String id) {
		return countyServiceBean.findCountyById(countryPk, id);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public County findByMunicipality(UserData userData, Long pk) {
		return countyRepository.findByMunicipality(pk);
	}

	@Override
	@Security(accesses = Aggregert_Konfigurasjon, type = READ)
	public CountyConfigStatus findCountyStatusByArea(UserData userData, AreaPath areaPath) {
		areaPath.assertLevel(AreaLevelEnum.COUNTY);
		County county = getCounty(userData, areaPath);
		return CountyConfigStatusMapper.toCountyConfigStatus(county);
	}

	@Override
	@Security(accesses = Aggregert_Konfigurasjon, type = WRITE)
	@AuditLog(eventClass = CountyConfigStatusAuditEvent.class, eventType = Save)
	public CountyConfigStatus saveCountyConfigStatus(UserData userData, CountyConfigStatus status) {
		County county = getCounty(userData, status.getCountyPath());
		Locale locale = localeRepository.findById(status.getLocaleId().getId());
		county.updateStatus(status);
		county.setLocale(locale);
		return findCountyStatusByArea(userData, status.getCountyPath());
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Oppheve, type = WRITE)
	@AuditLog(eventClass = CountyAuditEventForLocalConfiguration.class, eventType = StatusChanged, objectSource = ReturnValue)
	public County reject(final UserData userData, @SecureEntity(areaLevel = COUNTY) final Long countyPk) {
		County county = countyRepository.findByPk(countyPk);
		return countyRepository.update(userData, setCountyStatus(county, MunicipalityStatusEnum.LOCAL_CONFIGURATION.id()));
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Godkjenne, type = WRITE)
	@AuditLog(eventClass = CountyAuditEventForLocalConfiguration.class, eventType = StatusChanged, objectSource = ReturnValue)
	public County approve(final UserData userData, @SecureEntity(areaLevel = COUNTY) final Long countyPk) {
		County county = countyRepository.findByPk(countyPk);
		return countyRepository.update(userData, setCountyStatus(county, MunicipalityStatusEnum.APPROVED_CONFIGURATION.id()));
	}

	private County setCountyStatus(County county, Integer statusId) {
		CountyStatus countyStatus = countyRepository.findCountyStatusById(statusId);
		county.setCountyStatus(countyStatus);
		return county;
	}

	private County getCounty(UserData userData, AreaPath areaPath) {
		return countyRepository.countyByElectionEventAndId(userData.getElectionEventPk(), areaPath.getCountyId());
	}
}
