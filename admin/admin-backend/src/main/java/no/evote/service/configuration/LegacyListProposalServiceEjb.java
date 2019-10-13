package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Listeforslag_Rediger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.evote.dto.ListProposalValidationData;
import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "LegacyListProposalService")


@Default
@Remote(LegacyListProposalService.class)
public class LegacyListProposalServiceEjb implements LegacyListProposalService {
	@Inject
	private CandidateServiceBean candidateService;
	@Inject
	private ProposerServiceBean proposerService;

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public ListProposalValidationData validateListProposalAndCheckAgainstRoll(final UserData userData, final Affiliation affiliation,
			final List<Candidate> candidateList, final List<Proposer> proposerList, final String electionEventId,
			final Set<MvArea> candidateMvAreaRestrictions, final Set<MvArea> proposerMvAreaRestrictions) {

		ListProposalValidationData validationData = new ListProposalValidationData(candidateList, proposerList, affiliation);

		validationData = candidateService.approveCandidates(userData, validationData, electionEventId, candidateMvAreaRestrictions, affiliation);
		validationData = candidateService.validateCandidates(validationData, false);
		validationData = proposerService.approveProposers(userData, validationData, electionEventId, proposerMvAreaRestrictions, affiliation);

		validationData = proposerService.isOcrProposersDataSet(userData, validationData);

		validationData = validateNumberOfCandidatesAndProposers(userData, validationData);

		validationData = mergeValidatedProposalPersons(userData, validationData);

		return validationData;
	}

	private ListProposalValidationData mergeValidatedProposalPersons(final UserData userData, final ListProposalValidationData validationData) {
		validationData.setCandidateList(candidateService.mergeAllCandidates(userData, validationData.getCandidateList()));
		validationData.setProposerList(proposerService.updateAll(userData, validationData.getProposerList()));
		return validationData;
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public ListProposalValidationData validateCandidatesAndNumberOfCandidatesAndProposers(UserData userData, ListProposalValidationData validationData) {
		validationData = candidateService.validateCandidates(validationData, true);
		return validateNumberOfCandidatesAndProposers(userData, validationData);
	}

	@Override
	@Security(accesses = Listeforslag_Rediger, type = READ)
	public ListProposalValidationData validateNumberOfCandidatesAndProposers(final UserData userData, final ListProposalValidationData validationData) {
		validationData.getAffiliation().clearValidationMessages();

		int minCandidates = validationData.getAffiliation().getBallot().getContest().getMinCandidates();
		int candidatesLength = validationData.getCandidateList().size();
		if (candidatesLength < minCandidates) {
			validationData.setApproved(false);

			int missing = minCandidates - candidatesLength;
			String validationMsg = missing == 1 ? "@listProposal.candidate.validation.min.singel" : "@listProposal.candidate.validation.min";
			validationData.getAffiliation().addValidationMessage(validationMsg, Integer.toString(missing));
		}

		int maxCandidates = validationData.getAffiliation().getBallot().getContest().getMaxCandidates();
		maxCandidates = maxCandidates > EvoteConstants.MAX_CANDIDATES_IN_AFFILIATION ? EvoteConstants.MAX_CANDIDATES_IN_AFFILIATION : maxCandidates;
		if (candidatesLength > maxCandidates) {
			validationData.setApproved(false);
			validationData.getAffiliation().addValidationMessage(
					"@listProposal.candidate.validation.max", Integer.toString(candidatesLength - maxCandidates));
		}
		if (candidatesLength >= minCandidates && candidatesLength <= maxCandidates) {
			validationData.setSufficientNumberOfCandidates(true);
		}

		int proposerLength = (validationData.getProposerList() == null ? 0 : (validationData.getProposerListMedKunUtfylteUnderskrifter()).size());
		int minProposers = validationData.getMinProposers();
		if (validationData.getProposerList() != null && proposerLength < minProposers) {
			int missing = minProposers - proposerLength;
			String validationMsg = missing == 1 ? "@listProposal.proposer.validation.min.singel" : "@listProposal.proposer.validation.min";
			validationData.setApproved(false);
			validationData.getAffiliation().addValidationMessage(validationMsg, Integer.toString(missing));
		}

		return validationData;
	}

}
