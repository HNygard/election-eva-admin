package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.evote.dto.ListProposalValidationData;
import no.evote.model.views.CandidateAudit;
import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;

public interface CandidateService extends Serializable {
	Candidate createNewCandidate(UserData userData, Affiliation affiliation);

	List<Candidate> convertRowsToCandidateList(UserData userData, List<String[]> rowCandidates, Affiliation affiliation, int maxVotes, MvArea mvArea);

	List<Candidate> findByAffiliation(UserData userData, Long affiliationPk);

	List<Candidate> changeDisplayOrder(UserData userData, Candidate candidate, int fromPosition, int toPosition);

	List<CandidateAudit> getCandidateAuditByBallot(UserData userData, Long bPk);

	ListProposalValidationData isCandidatesValid(UserData userData, List<Candidate> importedCandidates, Long ballotPk, int maximumBaselineVotes);

	void createAllBelow(UserData userData, List<Candidate> importedCandidates, Long affiliationPk, Long ballotPk);

	void deleteAll(UserData userData, List<Candidate> candidateList);

	Candidate convertVoterToCandidate(UserData userData, Candidate candidate, Voter voter);

	Candidate create(UserData userData, Candidate candidate, Long ballotPk);

	Candidate update(UserData userData, Candidate proposalPerson);

	Candidate setMockIdForEmptyId(UserData userData, Candidate proposalPerson, Long ballotPk, Map<String, Candidate> existingIds);

	void deleteAndReorder(UserData userData, Candidate candidate, Long ballotPk);

	List<Voter> searchVoter(UserData userData, Candidate candidate, String electionEventId, Set<MvArea> mvAreas);
}
