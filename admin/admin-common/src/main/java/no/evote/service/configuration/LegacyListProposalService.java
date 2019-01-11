package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import no.evote.dto.ListProposalValidationData;
import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Proposer;

public interface LegacyListProposalService extends Serializable {
	ListProposalValidationData validateListProposalAndCheckAgainstRoll(UserData userData, Affiliation affiliation, List<Candidate> candidateList,
			List<Proposer> proposerList, String electionEventId, Set<MvArea> candidateMvAreaRestrictions, Set<MvArea> proposerMvAreaRestrictions);

	ListProposalValidationData validateCandidatesAndNumberOfCandidatesAndProposers(UserData userData, ListProposalValidationData validationData);

	ListProposalValidationData validateNumberOfCandidatesAndProposers(UserData userData, ListProposalValidationData validationData);
}
