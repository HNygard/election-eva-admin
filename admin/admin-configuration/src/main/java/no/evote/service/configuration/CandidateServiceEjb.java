package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Listeforslag;
import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Importere_Kandidater;
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
import no.evote.dto.ListProposalValidationData;
import no.evote.model.views.CandidateAudit;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.CandidateAuditEvent;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.CandidateRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "CandidateService")
@Remote(CandidateService.class)
public class CandidateServiceEjb implements CandidateService {
	@Inject
	private CandidateServiceBean candidateService;
	@Inject
	private CandidateRepository candidateRepository;

	@Override
	@Security(accesses = { Listeforslag_Rediger, Listeforslag_Importere_Kandidater }, type = WRITE)
	@AuditLog(eventClass = CandidateAuditEvent.class, eventType = AuditEventTypes.DeleteAll)
	public void deleteAll(UserData userData, List<Candidate> candidateList) {
		candidateRepository.deleteAllCandidates(userData, candidateList);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	public Candidate createNewCandidate(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Affiliation affiliation) {
		return candidateService.createNewCandidate(affiliation);
	}

	@Override
	@Security(accesses = Listeforslag_Importere_Kandidater, type = READ)
	public List<Candidate> convertRowsToCandidateList(UserData userData,
			List<String[]> rowCandidates,
			@SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Affiliation affiliation,
			int maximumBaselineVotes,
			MvArea mvArea) {
		return candidateService.convertRowsToCandidateList(rowCandidates, affiliation);
	}

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = READ)
	public List<Candidate> findByAffiliation(UserData userData, Long affiliationPk) {
		return candidateRepository.findByAffiliation(affiliationPk);
	}

	@Override
	@Security(accesses = Listeforslag_Importere_Kandidater, type = WRITE)
	@AuditLog(eventClass = CandidateAuditEvent.class, eventType = AuditEventTypes.CreateAll)
	public void createAllBelow(UserData userData, List<Candidate> importedCandidates, Long affiliationPk, Long ballotPk) {
		candidateService.createAllBelow(userData, importedCandidates, affiliationPk, ballotPk);
	}

	@Override
	@Security(accesses = Listeforslag_Importere_Kandidater, type = READ)
	public ListProposalValidationData isCandidatesValid(UserData userData, List<Candidate> candidateList, Long ballotPk, int maximumBaselineVotes) {
		return candidateService.isCandidatesValid(candidateList, ballotPk, maximumBaselineVotes);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = CandidateAuditEvent.class, eventType = AuditEventTypes.DisplayOrderChanged)
	public List<Candidate> changeDisplayOrder(UserData userData, Candidate candidate, int fromPosition, int toPosition) {
		return candidateService.changeDisplayOrder(candidate, fromPosition, toPosition);
	}

	@Override
	@Security(accesses = Aggregert_Listeforslag, type = WRITE)
	public Candidate convertVoterToCandidate(UserData userData, Candidate candidate, Voter voter) {
		return candidateService.convertVoterToCandidate(candidate, voter);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public List<CandidateAudit> getCandidateAuditByBallot(UserData userData, Long ballotPk) {
		return candidateRepository.getCandidateAuditByBallot(ballotPk);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = CandidateAuditEvent.class, eventType = AuditEventTypes.Create)
	public Candidate create(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Candidate candidate, Long ballotPk) {
		return candidateService.create(userData, candidate);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = CandidateAuditEvent.class, eventType = AuditEventTypes.Update)
	public Candidate update(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Candidate candidate) {
		return candidateService.update(userData, candidate);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	public Candidate setMockIdForEmptyId(UserData userData, Candidate candidate, Long ballotPk, Map<String, Candidate> existingIds) {
		return candidateService.setMockIdForEmptyId(candidate, ballotPk, existingIds);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = WRITE)
	@AuditLog(eventClass = CandidateAuditEvent.class, eventType = AuditEventTypes.Delete)
	public void deleteAndReorder(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Candidate candidate,
			@SecureEntity(electionLevel = ElectionLevelEnum.CONTEST, entity = Ballot.class) Long ballotPk) {
		candidateService.deleteAndReorder(userData, candidate, ballotPk);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public List<Voter> searchVoter(UserData userData, Candidate candidate, String electionEventId, Set<MvArea> mvAreas) {
		return candidateService.searchVoter(candidate, electionEventId, mvAreas);
	}
}
