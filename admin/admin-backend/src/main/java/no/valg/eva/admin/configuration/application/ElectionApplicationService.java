package no.valg.eva.admin.configuration.application;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Valghierarki;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valg_Valg;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.evote.service.configuration.ElectionServiceBean;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ElectionAuditEvent;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.service.ElectionService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.repository.ElectionRepository;

@Stateless(name = "ElectionService")
@Remote(ElectionService.class)
public class ElectionApplicationService implements ElectionService {

	@Inject
	private ElectionServiceBean electionServiceBean;
	@Inject
	private ElectionRepository electionRepository;

	@Override
	@Security(accesses = Aggregert_Valghierarki, type = READ)
	public Election get(UserData userData, ElectionPath electionPath) {
		return electionServiceBean.get(electionPath);
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valg, type = WRITE)
	@AuditLog(eventClass = ElectionAuditEvent.class, eventType = AuditEventTypes.Save)
	public SaveElectionResponse save(UserData userData, Election election) {
		if (election.getElectionRef() == null) {
			return electionServiceBean.create(userData, election);
		} else {
			return electionServiceBean.update(userData, election);
		}
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valg, type = WRITE)
	@AuditLog(eventClass = ElectionAuditEvent.class, eventType = AuditEventTypes.Delete)
	public void delete(UserData userData, ElectionPath electionPath) {
		electionRepository.delete(userData, electionRepository.findSingleByPath(electionPath).getPk());
	}

	@Override
	@SecurityNone
	public ElectionType findElectionTypeById(String id) {
		return electionRepository.findElectionTypeById(id);
	}
}
