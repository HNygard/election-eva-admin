package no.evote.service.configuration;

import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Listeforslag;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Partier;
import static no.valg.eva.admin.common.rbac.Accesses.Parti_Opprett;
import static no.valg.eva.admin.common.rbac.Accesses.Parti_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Parti_Slett;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.PartyAuditEvent;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.application.party.PartyMapper;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "PartyService")



@Default
@Remote(PartyService.class)
public class PartyServiceEjb implements PartyService {
	@Inject
	private PartyServiceBean partyService;
	@Inject
	private PartyRepository partyRepository;
	@Inject
	private PartyMapper partyMapper;

	@Override
	@Security(accesses = Parti_Opprett, type = WRITE)
	@AuditLog(eventClass = PartyAuditEvent.class, eventType = AuditEventTypes.Create)
	public Parti create(UserData userData, Parti parti) {
		return partyMapper.toParti(userData, partyService.create(userData, partyMapper.toParty(parti, userData.electionEvent())));
	}

	@Override
	@Security(accesses = Parti_Slett, type = WRITE)
	@AuditLog(eventClass = PartyAuditEvent.class, eventType = AuditEventTypes.Delete)
	public void delete(UserData userData, Parti parti) {
		partyService.delete(userData, partyMapper.toParty(parti, userData.electionEvent()));
	}

	@Override
	@Security(accesses = Parti_Rediger, type = WRITE)
	@AuditLog(eventClass = PartyAuditEvent.class, eventType = AuditEventTypes.Update)
	public Parti update(UserData userData, Parti parti) {
		return partyMapper.toParti(userData, partyService.update(userData, parti));
	}

	@Override
	@Security(accesses = { Aggregert_Partier, Aggregert_Listeforslag }, type = READ)
	public List<Parti> findAllPartiesButNotBlank(UserData userData, Long electionEventPk) {
		return partyRepository.findAllButNotBlank(electionEventPk).stream().map(party -> partyMapper.toParti(userData, party)).collect(toList());
	}

	@Override
	@Security(accesses = { Aggregert_Partier, Aggregert_Listeforslag }, type = READ)
	public List<String> validateParty(UserData userData, Parti parti) {
		return partyService.validateParty(userData, parti);
	}

	/**
	 * Only allow to delete party with no affiliation and operator relation
	 */
	@Override
	@Security(accesses = Parti_Slett, type = READ)
	public List<String> validatePartyForDelete(UserData userData, Parti parti) {
		return partyService.validatePartyForDelete(userData, partyMapper.toParty(parti, userData.electionEvent()));
	}

}
