package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Listeforslag;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Rediger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.ElectionLevelEnum;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ProposerAuditEvent;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.ProposerRole;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.ProposerRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "ProposerService")
@Remote(ProposerService.class)
public class ProposerServiceEjb implements ProposerService {
	@Inject
	private ProposerServiceBean proposerService;
	@Inject
	private ProposerRepository proposerRepository;

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = ProposerAuditEvent.class, eventType = AuditEventTypes.Create)
	public Proposer create(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Proposer proposer) {
		return proposerRepository.createProposer(userData, proposer);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = ProposerAuditEvent.class, eventType = AuditEventTypes.Update)
	public Proposer update(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Proposer proposer) {
		return proposerRepository.updateProposer(userData, proposer);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	public Proposer createNewProposer(UserData userData, Ballot ballot) {
		return proposerService.createNewProposer(ballot);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	public void createDefaultProposers(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Ballot ballot) {
		proposerService.createDefaultProposers(userData, ballot);
	}

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	public List<Proposer> findByBallot(UserData userData, Long ballotPk) {
		return proposerRepository.findByBallot(ballotPk);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public Proposer convertVoterToProposer(UserData userData, Proposer proposer, Voter voter) {
		return proposerService.convertVoterToProposer(proposer, voter);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public Proposer findByBallotAndOrder(UserData userData, Long ballotPk, int displayOrder) {
		return proposerRepository.findByBallotAndOrder(ballotPk, displayOrder);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public List<ProposerRole> findSelectiveProposerRoles(UserData userData) {
		return proposerRepository.findSelectiveProposerRoles();
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public ProposerRole findProposerRoleByPk(UserData userData, Long pk) {
		return proposerRepository.findProposerRoleByPk(pk);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = ProposerAuditEvent.class, eventType = AuditEventTypes.DisplayOrderChanged)
	public List<Proposer> changeDisplayOrder(UserData userData, Proposer proposer, int fromPosition, int toPosition) {
		return proposerService.changeDisplayOrder(proposer, fromPosition, toPosition);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	public Proposer setMockIdForEmptyId(UserData userData, Proposer proposer, Long ballotPk, Map<String, Proposer> existingIds) {
		return proposerService.setMockIdForEmptyId(proposer, ballotPk, existingIds);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public Proposer validate(UserData userData, Proposer proposer, Long ballotPk) {
		return proposerService.validate(userData, proposer, ballotPk);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public List<Voter> searchVoter(UserData userData, Proposer proposer, String electionEventId, Set<MvArea> mvAreas) {
		return proposerService.searchVoter(proposer, electionEventId, mvAreas);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = ProposerAuditEvent.class, eventType = AuditEventTypes.Delete)
	public void deleteAndReorder(UserData userData,
			@SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Proposer proposer,
			@SecureEntity(electionLevel = ElectionLevelEnum.CONTEST, entity = Ballot.class) Long ballotPk) {
		proposerService.deleteAndReorder(userData, proposer, ballotPk);
	}
}
