package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Listeforslag;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Rediger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.ElectionLevelEnum;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.BallotAuditEvent;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.repository.BallotRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "BallotService")
@Remote(BallotService.class)
public class BallotServiceEjb implements BallotService {
	@Inject
	private BallotServiceBean ballotService;
	@Inject
	private BallotRepository ballotRepository;

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	public Ballot findByPk(UserData userData, Long ballotPk) {
		return ballotRepository.findBallotByPk(ballotPk);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = BallotAuditEvent.class, eventType = AuditEventTypes.Delete)
	public void delete(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST, entity = Ballot.class) Ballot ballot) {
		ballotRepository.deleteBallot(userData, ballot);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = BallotAuditEvent.class, eventType = AuditEventTypes.StatusChanged)
	public void updateBallotStatus(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Affiliation affiliation,
			BallotStatus ballotStatus) {
		ballotService.updateBallotStatus(userData, affiliation, ballotStatus);
	}

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	public BallotStatus findBallotStatusById(UserData userData, int id) {
		return ballotRepository.findBallotStatusById(id);
	}
}
