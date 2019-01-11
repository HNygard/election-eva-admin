package no.evote.service.configuration;

import static no.evote.constants.AreaLevelEnum.COUNTRY;
import static no.evote.constants.ElectionLevelEnum.ELECTION_EVENT;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;
import static no.valg.eva.admin.common.auditlog.AuditedObjectSource.Parameters;
import static no.valg.eva.admin.common.auditlog.AuditedObjectSource.ReturnValue;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.CountryAuditEvent;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.repository.CountryRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "CountryService")
@Remote(CountryService.class)
public class CountryServiceEjb implements CountryService {
	@Inject
	private CountryServiceBean countryServiceBean;
	@Inject
	private CountryRepository countryRepository;

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = CountryAuditEvent.class, eventType = Create, objectSource = ReturnValue)
	public Country create(final UserData userData, @SecureEntity(electionLevel = ELECTION_EVENT) final Country country) {
		return countryServiceBean.create(userData, country);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = CountryAuditEvent.class, eventType = Update, objectSource = ReturnValue)
	public Country update(final UserData userData, @SecureEntity(areaLevel = COUNTRY) final Country country) {
		return countryRepository.update(userData, country);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = CountryAuditEvent.class, eventType = Delete, objectSource = Parameters)
	public void delete(final UserData userData, Country country) {
		countryServiceBean.deleteByPk(userData, country.getPk());
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public Country findByPk(UserData userData, Long pk) {
		return countryServiceBean.findByPk(pk);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public Country findCountryById(final UserData userData, final Long electionEventPk, final String id) {
		return countryServiceBean.findCountryById(electionEventPk, id);
	}
}
