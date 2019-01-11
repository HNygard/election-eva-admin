package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Listeforslag;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Opprett_Eksisterende_Parti;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Opprett_Nytt_Parti;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Rediger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.auditevents.AffiliationAuditEvent;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.application.party.PartyMapper;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "AffiliationService")
@Remote(AffiliationService.class)
public class AffiliationServiceEjb implements AffiliationService {
	@Inject
	private AffiliationServiceBean affiliationService;
	@Inject
	private AffiliationRepository affiliationRepository;
	@Inject
	private PartyMapper partyMapper;
	@Inject
	private ContestRepository contestRepository;

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	public Affiliation findByPk(UserData userData, Long affiliationPk) {
		return affiliationRepository.findAffiliationByPk(affiliationPk);
	}

	@Override
	@Security(accesses = Listeforslag_Opprett_Eksisterende_Parti, type = WRITE)
	@AuditLog(eventClass = AffiliationAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ReturnValue)
	public Affiliation createNewAffiliation(UserData userData, Contest contest, Parti parti, Locale locale, int ballotStatus) {
		return affiliationService.createNewAffiliation(userData, contest, partyMapper.toParty(parti, userData.electionEvent()), locale, ballotStatus);
	}

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	public List<Affiliation> findByContest(UserData userData, Long contestPk) {
		return affiliationRepository.findByContest(contestPk);
	}

	@Override
	@Security(accesses = Listeforslag_Opprett_Nytt_Parti, type = WRITE)
	@AuditLog(eventClass = AffiliationAuditEvent.class, eventType = AuditEventTypes.Create, objectSource = AuditedObjectSource.ReturnValue)
	public Affiliation createNewPartyAndAffiliation(UserData userData, Contest detachedContest, Parti newParty, Locale locale) {
		Contest currentContest = contestRepository.findByPk(detachedContest.getPk());
		newParty.leggTilOmraadeHvisLokalt(currentContest.getFirstContestArea().getAreaPath());
		return affiliationService.createNewPartyAndAffiliation(userData, currentContest, partyMapper.toParty(newParty, userData.electionEvent()), locale);
	}

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	public Affiliation findByBallot(UserData userData, Long ballotPk) {
		return affiliationRepository.findByBallot(ballotPk);
	}

	/**
	 * Saves columns(residence/profession) shown on ballot.
	 */
	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = AffiliationAuditEvent.class, eventType = AuditEventTypes.Update)
	public Affiliation saveColumns(UserData userData, Affiliation affiliation) {
		return affiliationService.saveColumns(userData, affiliation);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = AffiliationAuditEvent.class, eventType = AuditEventTypes.DisplayOrderChanged)
	public List<Affiliation> changeDisplayOrder(UserData userData, Affiliation affiliation, int fromPosition, int toPosition) {
		return affiliationService.changeDisplayOrder(userData, affiliation, fromPosition, toPosition);
	}

}
