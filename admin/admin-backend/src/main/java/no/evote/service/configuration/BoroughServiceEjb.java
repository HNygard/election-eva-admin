package no.evote.service.configuration;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Update;
import static no.valg.eva.admin.common.auditlog.AuditedObjectSource.Parameters;
import static no.valg.eva.admin.common.auditlog.AuditedObjectSource.ReturnValue;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Listeforslag;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.BoroughAuditEvent;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.repository.BoroughRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "BoroughService")
@Remote(BoroughService.class)
public class BoroughServiceEjb implements BoroughService {
	@Inject
	private BoroughServiceBean boroughServiceBean;
	@Inject
	private BoroughRepository boroughRepository;

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = BoroughAuditEvent.class, eventType = Create, objectSource = ReturnValue)
	public Borough create(UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.MUNICIPALITY) Borough borough) {
		return boroughServiceBean.create(userData, borough);
	}

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	@AuditLog(eventClass = BoroughAuditEvent.class, eventType = Update, objectSource = ReturnValue)
	public Borough update(UserData userData, @SecureEntity(areaLevel = AreaLevelEnum.BOROUGH) Borough borough) {
		return boroughServiceBean.update(userData, borough);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = WRITE)
	@AuditLog(eventClass = BoroughAuditEvent.class, eventType = Delete, objectSource = Parameters)
	public void delete(UserData userData, Borough borough) {
		boroughRepository.deleteBorough(userData, borough.getPk());
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public Borough findBoroughById(UserData userData, Long municipalityPk, String id) {
		return boroughRepository.findBoroughById(municipalityPk, id);
	}

	@Override
	@Security(accesses = Konfigurasjon_Geografi, type = READ)
	public Borough findByPk(UserData userData, Long boroughPk) {
		return boroughRepository.findBoroughByPk(boroughPk);
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public List<Borough> findByMunicipality(UserData userData, Long municipalityPk) {
		return boroughRepository.findByMunicipality(municipalityPk);
	}

}
