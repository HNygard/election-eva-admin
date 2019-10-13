package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import no.evote.constants.AreaLevelEnum;
import no.evote.dto.ListProposalValidationData;
import no.evote.exception.CandidateValidationException;
import no.evote.security.UserData;
import no.valg.eva.admin.util.DateUtil;
import no.evote.validation.FoedselsNummerValidator;
import no.evote.validation.ProposalValidationManual;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.LocaleText;
import no.valg.eva.admin.configuration.domain.model.MaritalStatus;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.ProposalPerson;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

@Default
@ApplicationScoped
public class CandidateServiceBean {
	private static final Logger LOGGER = Logger.getLogger(CandidateServiceBean.class);
	private static final String EMPTY = "";

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	@Inject
	private CandidateRepository candidateRepository;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private LocaleTextRepository localeTextRepository;

	public List<Candidate> mergeAllCandidates(UserData userData, List<Candidate> candidateList) {
		List<Candidate> mergedCandidates = new ArrayList<>();
		for (Candidate candidate : candidateList) {
			Candidate mergedCandidate = candidateRepository.updateCandidate(userData, candidate);
			/** Set validation message on merged proposer */
			if (candidate.isInvalid()) {
				mergedCandidate.setValidationMessageList(candidate.getValidationMessageList());
			}
			mergedCandidates.add(mergedCandidate);
		}
		return mergedCandidates;
	}

	public Candidate createNewCandidate(Affiliation affiliation) {
		MaritalStatus maritalStatus = candidateRepository.findMaritalStatusById(MaritalStatus.UOPPGITT);
		return new CandidateFileRowConverter().createNewCandidate(affiliation, maritalStatus);
	}

	/**
	 * Converting excel row strings to candidate objects
	 */
	public List<Candidate> convertRowsToCandidateList(List<String[]> rowCandidates, Affiliation affiliation) {
		int initialDisplayOrder = candidateRepository.findByAffiliation(affiliation.getPk()).size() + 1;
		MaritalStatus maritalStatus = candidateRepository.findMaritalStatusById(MaritalStatus.UOPPGITT);
		return new CandidateFileRowConverter().convertRowsToCandidateList(rowCandidates, affiliation, initialDisplayOrder, maritalStatus);
	}

	public void createAllBelow(UserData userData, List<Candidate> importedCandidates, Long affiliationPk, Long ballotPk) {
		int displayOrder = candidateRepository.findByAffiliation(affiliationPk).size() + 1;
		Map<String, Candidate> idList = new HashMap<>();
		for (Candidate candidate : importedCandidates) {
			if (candidate.getPk() == null) {
				setMockIdForEmptyId(candidate, ballotPk, idList);
				candidate.setDisplayOrder(displayOrder);
				candidate.setNameLine();
				candidateRepository.createCandidate(userData, candidate);
				displayOrder++;
			}
		}
	}

	public Candidate setMockIdForEmptyId(Candidate candidate, Long ballotPk, Map<String, Candidate> existingIds) {
		int id = 0;
		LocalDate dateOfBirth = candidate.getDateOfBirth();
		String birth;
		if (dateOfBirth == null) {
			birth = "000000";
		} else {
			birth = DateUtil.getFormattedShortIdDate(dateOfBirth);
		}
		do {
			String nr = String.format("%05d", id++);
			candidate.setId(birth + nr);
		} while (!isValidMockId(candidate, ballotPk, existingIds));
		if (existingIds != null) {
			existingIds.put(candidate.getId(), candidate);
		}
		return candidate;
	}

	private boolean isValidMockId(Candidate candidate, Long ballotPk, Map<String, Candidate> existingIds) {
		boolean presentInIdList = existingIds != null && existingIds.containsKey(candidate.getId());
		boolean presentInPersistedCandidates = candidateRepository.findCandidateByBallotAndId(ballotPk, candidate.getId()) != null;
		boolean isValidSSN = FoedselsNummerValidator.isFoedselsNummerValid(candidate.getId());
		return !presentInIdList && !presentInPersistedCandidates && !isValidSSN;
	}

	public ListProposalValidationData approveCandidates(UserData userData,
			ListProposalValidationData approved,
			String electionEventId,
			Set<MvArea> mvAreas,
			Affiliation affiliation) {
		Map<String, Candidate> idList = new HashMap<>();

		List<Candidate> copiedCandidateList = new ArrayList<>(approved.getCandidateList());
		sortCandidatesOnIfIdentified(copiedCandidateList);

		for (Candidate candidate : copiedCandidateList) {
			candidate.clearValidationMessages();

			if (isInElectoralRoll(candidate, electionEventId, mvAreas)) {
				if (isCandidateInOtherAffiliations(userData, candidate, affiliation)) {
					setApprovedFalse(approved, candidate);
				}
			} else {
				setApprovedFalse(approved, candidate);
			}

			if (affiliation.isShowCandidateResidence() && fieldIsEmpty(candidate.getResidence())) {
				candidate.addValidationMessage(new UserMessage("@listProposal.approve.candidateResidence"));
				setApprovedFalse(approved, candidate);
			}

			if (affiliation.isShowCandidateProfession() && fieldIsEmpty(candidate.getProfession())) {
				candidate.addValidationMessage(new UserMessage("listProposal.approve.candidateProfession"));
				setApprovedFalse(approved, candidate);
			}

			/** Find duplicates */
			if (candidate.isIdSet() && idList.containsKey(candidate.getId())) {
				setDuplicateMessage(candidate, idList.get(candidate.getId()));
				setMockIdForEmptyId(candidate, affiliation.getBallot().getPk(), idList);
				setApprovedFalse(approved, candidate);
			} else if (candidate.isIdSet()) {
				idList.put(candidate.getId(), candidate);
			}
		}

		return approved;
	}

	public ListProposalValidationData validateCandidates(ListProposalValidationData approved, boolean clearValidationMessages) {
		List<Candidate> copiedCandidateList = new ArrayList<>(approved.getCandidateList());
		sortCandidatesOnIfIdentified(copiedCandidateList);

		for (Candidate candidate : copiedCandidateList) {
			if (clearValidationMessages) {
				candidate.clearValidationMessages();
			}
			try {
				validateCandidate(candidate, clearValidationMessages);
			} catch (CandidateValidationException e) {
				setApprovedFalse(approved, candidate);
			}
		}
		return approved;
	}

	private boolean isInElectoralRoll(Candidate candidate, String electionEventId, Set<MvArea> mvAreas) {
		if (!candidate.isIdSet()) {
			if (hasOneHitInRollAndIsConvertedFromVoter(candidate, electionEventId, mvAreas)) {
				return searchInElectoralRoll(candidate, electionEventId, mvAreas);
			}
		} else {
			return searchInElectoralRoll(candidate, electionEventId, mvAreas);
		}
		return false;
	}

	private boolean hasOneHitInRollAndIsConvertedFromVoter(Candidate candidate, String electionEventId, Set<MvArea> mvAreas) {
		List<Voter> voterResult = searchVoter(candidate, electionEventId, mvAreas);
		boolean uniqueIdInElectoralRoll = (voterResult.size() == 1);

		if (uniqueIdInElectoralRoll) {
			convertVoterToCandidate(candidate, voterResult.get(0));
			return true;
		}

		boolean toManyResult = (voterResult.size() > 1);
		boolean noResult = voterResult.isEmpty();
		if (toManyResult) {
			candidate.addValidationMessage(new UserMessage("@listProposal.approve.manyInRoll"));
		} else if (noResult) {
			candidate.addValidationMessage(new UserMessage("@listProposal.approve.notInRoll"));
		}
		return false;
	}

	private boolean searchInElectoralRoll(Candidate candidate, String electionEventId, Set<MvArea> mvAreas) {
		List<Voter> voterResult = getApprovedVoters(
				voterRepository.findByElectionEventAndId(electionEventRepository.findById(electionEventId).getPk(), candidate.getId()));

		boolean inElectoralRoll = (!voterResult.isEmpty());

		if (inElectoralRoll) {
			if (!mvAreas.isEmpty()) {
				Voter voter = voterResult.get(0);
				return isInContest(candidate, voter.getMunicipalityId(), mvAreas);
			}
			return true;
		} else {
			candidate.addValidationMessage(new UserMessage("@listProposal.approve.notInRoll"));
			return false;
		}
	}

	private boolean isInContest(ProposalPerson proposalPerson, String municipalityId, Set<MvArea> mvAreas) {
		Boolean isInCounty = true;
		Boolean isInMunicipality = true;

		for (MvArea mvArea : mvAreas) {
			MvArea mvAreaMunicipality = mvAreaRepository.findSingleByPath(buildMunicipalityPath(mvArea, municipalityId));

			if (mvAreaMunicipality != null) {
				Municipality proposalPersonMunicipality = mvAreaMunicipality.getMunicipality();

				if (mvArea.getMunicipalityId() == null && mvArea.getCountyId() == null) {
					return true;
				} else if (mvArea.getMunicipalityId() == null) {
					if (isInCounty(mvArea, proposalPersonMunicipality)) {
						return true;
					} else {
						isInCounty = false;
					}
				} else {
					if (isInMunicipality(mvArea, municipalityId)) {
						return true;
					} else {
						isInMunicipality = false;
					}
				}
			}
		}
		if (!isInCounty) {
			proposalPerson.addValidationMessage(new UserMessage("@listProposal.approve.inRollNotInCounty"));
		} else if (!isInMunicipality) {
			proposalPerson.addValidationMessage(new UserMessage("@listProposal.approve.inRollNotInMunicipality"));
		} else {
			proposalPerson.addValidationMessage(new UserMessage("@listProposal.approve.notInRoll"));
		}
		return false;
	}

	private boolean isInCounty(MvArea mvArea, Municipality candidateMunicipality) {
		return mvArea.getCounty().getId().equals(candidateMunicipality.getCounty().getId());
	}

	private boolean isInMunicipality(MvArea mvArea, String municipalityId) {
		return mvArea.getMunicipalityId().equals(municipalityId.trim());
	}

	private AreaPath buildMunicipalityPath(MvArea mvArea, String municipalityId) {
		return AreaPath.from(mvArea.getElectionEventId(), mvArea.getCountryId(), municipalityId.substring(0, 2), municipalityId);
	}

	public List<Voter> searchVoter(Candidate candidate, String electionEventId, Set<MvArea> mvAreas) {
		Voter voter = convertCandidateToNewVoter(candidate, electionEventRepository.findById(electionEventId));

		List<Voter> voterResult = new ArrayList<>();
		if (mvAreas.isEmpty()) {
			voterResult.addAll(searchVoterBasedOnAreaRestrictions(voter, candidate, null));
		} else {
			for (MvArea mvArea : mvAreas) {
				voterResult.addAll(searchVoterBasedOnAreaRestrictions(voter, candidate, mvArea));
			}
		}

		return getApprovedVoters(voterResult);
	}

	private List<Voter> searchVoterBasedOnAreaRestrictions(Voter voter, ProposalPerson proposalPerson, MvArea mvArea) {
		String municipalityId = null;
		String countyId = null;
		String boroughId = null;

		if (mvArea != null) {
			if (mvArea.getAreaLevel() == AreaLevelEnum.COUNTY.getLevel()) {
				countyId = mvArea.getCountyId();
			} else if (mvArea.getAreaLevel() == AreaLevelEnum.BOROUGH.getLevel()) {
				boroughId = mvArea.getBoroughId();
			} else if (mvArea.getAreaLevel() >= AreaLevelEnum.MUNICIPALITY.getLevel()) {
				municipalityId = mvArea.getMunicipalityId();
			}
		}

		
		List<Voter> voterResult = proposalPerson.hasSearchInformation()
				? voterRepository.searchVoter(voter, countyId, municipalityId, boroughId, 10, true, voter.getElectionEvent().getPk())
				: new ArrayList<Voter>();
		

		/** Try to search without address if first result is empty */
		if (proposalPerson.hasSearchInformation() && !StringUtils.isEmpty(voter.getAddressLine1()) && voterResult.isEmpty()) {
			voter.setAddressLine1("");
			
			voterResult = voterRepository.searchVoter(voter, countyId, municipalityId, 10, true, voter.getElectionEvent().getPk());
			
		}
		return voterResult;
	}

	private List<Voter> getApprovedVoters(List<Voter> voters) {
		List<Voter> approvedVoters = new ArrayList<>();
		for (Voter voter : voters) {
			if (voter.isApproved()) {
				approvedVoters.add(voter);
			}
		}
		return approvedVoters;
	}

	private boolean fieldIsEmpty(String field) {
		return field == null || field.trim().equals(EMPTY);
	}

	private void sortCandidatesOnIfIdentified(List<Candidate> candidateList) {
		Collections.sort(candidateList, new Comparator<Candidate>() {
			@Override
			public int compare(Candidate o1, Candidate o2) {
				if (o1.isIdSet() && !o2.isIdSet()) {
					return -1;
				} else if (!o1.isIdSet() && o2.isIdSet()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
	}

	private void setApprovedFalse(ListProposalValidationData approved, Candidate candidate) {
		approved.setApproved(false);
		candidate.setApproved(false);
	}

	private void setDuplicateMessage(Candidate candidate, Candidate duplicatedCandidate) {
		candidate.addValidationMessage(new UserMessage("@listProposal.validation.duplicatedIdFromRoll", duplicatedCandidate.getDisplayOrder()));
	}

	public ListProposalValidationData isCandidatesValid(List<Candidate> candidateList, Long ballotPk, int maximumBaselineVotes) {
		boolean isLastAVoteSet = false;

		ListProposalValidationData validationData = new ListProposalValidationData(candidateList, null, null);

		for (Candidate candidate : candidateList) {
			/** Check max affiliation votes */
			if (candidate.getDisplayOrder() > maximumBaselineVotes) {
				candidate.setBaselineVotes(false);
			}

			/** Set affiliation votes in correct order */
			if (!isLastAVoteSet && !candidate.isBaselineVotes()) {
				isLastAVoteSet = true;
			} else if (isLastAVoteSet && candidate.isBaselineVotes()) {
				candidate.setBaselineVotes(false);
			}

			if (validate(candidate, ballotPk).isInvalid()) {
				validationData.setApproved(false);
			}
		}
		return validationData;
	}

	public Candidate create(UserData userData, Candidate candidate) {
		validateCandidate(candidate, true);
		return candidateRepository.createCandidate(userData, candidate);
	}

	public Candidate update(UserData userData, Candidate candidate) {
		validateCandidate(candidate, true);
		return candidateRepository.updateCandidate(userData, candidate);
	}

	private void validateCandidate(Candidate candidate, boolean clearValidationMessages) {
		if (candidate.getBallot() != null && candidate.getBallot().getPk() != null) {
			candidate = validate(candidate, candidate.getBallot().getPk(), clearValidationMessages);
			if (!candidate.getValidationMessageList().isEmpty()) {
				throw new CandidateValidationException(candidate.getValidationMessageList());
			}
		}
	}

	public Candidate validate(Candidate candidate, Long ballotPk) {
		return validate(candidate, ballotPk, true);
	}

	public Candidate validate(Candidate candidate, Long ballotPk, boolean clearValidationMessages) {
		if (clearValidationMessages) {
			candidate.clearValidationMessages();
		}
		Set<ConstraintViolation<Candidate>> constraintViolations = validator.validate(candidate, ProposalValidationManual.class);

		if (constraintViolations.isEmpty()) {
			validateProfessionAndRecidence(candidate);
			validateNameCorrectLength(candidate);
			if (candidate.isIdSet()) {
				if (!isDateOfBirthAndSnnSame(candidate)) {
					candidate.addValidationMessage(new UserMessage("@listProposal.validation.bithDateSNNMismatch"));
				}

				ProposalPerson otherIdCandidate = candidateRepository.findCandidateByBallotAndId(ballotPk, candidate.getId());
				if (otherIdCandidate != null && !otherIdCandidate.getPk().equals(candidate.getPk())) {
					candidate.addValidationMessage(new UserMessage("@listProposal.validation.duplicatedId",
							otherIdCandidate.getDisplayOrder()));
				}
			}
		} else {
			constraintViolations.stream().map(c -> new UserMessage(c.getMessage())).forEach(candidate::addValidationMessage);
		}
		return candidate;
	}

	private void validateProfessionAndRecidence(Candidate candidate) {
		if (candidate.getAffiliation() == null || candidate.getBallot() == null || candidate.getBallot().getContest() == null
				|| candidate.getBallot().getContest().getElection() == null) {
			return;
		}
		StringBuilder professionAndRecidence = new StringBuilder();
		if (candidate.getAffiliation().isShowCandidateProfession() && candidate.getProfession() != null) {
			professionAndRecidence.append(candidate.getProfession());
		}
		if (candidate.getAffiliation().isShowCandidateResidence() && candidate.getResidence() != null) {
			professionAndRecidence.append(candidate.getResidence());
		}
		int maxCandidateResidenceProfessionLength = candidate.getBallot().getContest().getElection().getMaxCandidateResidenceProfessionLength();
		if (professionAndRecidence.length() > maxCandidateResidenceProfessionLength) {
			candidate
					.addValidationMessage(new UserMessage("@listProposal.candidate.error.professionAndRecidenceLength", maxCandidateResidenceProfessionLength));
		}
	}

	private void validateNameCorrectLength(Candidate candidate) {
		if (candidate.getBallot() == null || candidate.getBallot().getContest() == null || candidate.getBallot().getContest().getElection() == null) {
			return;
		}
		int nameLength = candidate.getFirstName().length() + 1 + candidate.getLastName().length();
		if (candidate.getMiddleName() != null && candidate.getMiddleName().length() > 0) {
			nameLength += 1 + candidate.getMiddleName().length();
		}
		int maxNameLength = candidate.getBallot().getContest().getElection().getMaxCandidateNameLength();
		if (nameLength > maxNameLength) {
			candidate.addValidationMessage(new UserMessage("@listProposal.candidate.validation.namelength", maxNameLength));
		}
	}

	private boolean isDateOfBirthAndSnnSame(final ProposalPerson proposalPerson) {
		String dateOfBirth = DateUtil.getFormattedShortIdDate(proposalPerson.getDateOfBirth());
		
		String ssnId = proposalPerson.getId().substring(0, 6);
		
		return dateOfBirth.equals(ssnId);
	}

	public List<Candidate> swapDisplayOrder(UserData userData, Candidate candidateOver, Candidate candidateUnder) {
		int displayOrderOver = candidateOver.getDisplayOrder();
		int displayOrderUnder = candidateUnder.getDisplayOrder();

		
		candidateUnder.setDisplayOrder(99);
		Candidate candidateUnderUpdated = candidateRepository.updateCandidate(userData, candidateUnder);
		candidateOver.setDisplayOrder(98);
		Candidate candidateOverUpdated = candidateRepository.updateCandidate(userData, candidateOver);
		

		candidateUnderUpdated.setDisplayOrder(displayOrderOver);
		candidateOverUpdated.setDisplayOrder(displayOrderUnder);

		boolean affiliationVoteUnder = candidateUnder.isBaselineVotes();
		candidateUnderUpdated.setBaselineVotes(candidateOverUpdated.isBaselineVotes());
		candidateOverUpdated.setBaselineVotes(affiliationVoteUnder);

		List<Candidate> swapped = new ArrayList<>();
		swapped.add(candidateRepository.updateCandidate(userData, candidateOverUpdated));
		swapped.add(candidateRepository.updateCandidate(userData, candidateUnderUpdated));
		return swapped;
	}

	public List<Candidate> changeDisplayOrder(Candidate candidate, int fromPosition, int toPosition) {
		if (fromPosition == toPosition) {
			throw new IllegalArgumentException("changeDisplayOrder displayOrderFrom[" + fromPosition + "] equal to displayOrderTo[" + toPosition + "]");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("changeDisplayOrder[" + candidate + ", from " + fromPosition + ", to " + toPosition);
		}

		// Get the affected candidates
		boolean fromTopToBottom = fromPosition < toPosition;
		int low = fromTopToBottom ? fromPosition : toPosition;
		int high = fromTopToBottom ? toPosition : fromPosition;
		List<Candidate> list = candidateRepository.findCandidateByBallotAndDisplayOrderRange(candidate.getBallot().getPk(), low, high);
		if (list.size() != (high - low + 1)) {
			throw new IllegalArgumentException(
					"changeDisplayOrder from/to [" + fromPosition + "/" + toPosition + "] does not match actual size " + list.size());
		}

		// Make the physical change
		if (fromTopToBottom) {
			list.add(list.remove(0));
		} else {
			list.add(0, list.remove(list.size() - 1));
		}

		// Correct all the display order values (also find how many baselines we have).
		int baselines = 0;
		int currentDisplayOrder = low;
		for (Candidate c : list) {
			c.setDisplayOrder(currentDisplayOrder++);
			if (c.isBaselineVotes()) {
				baselines++;
			}
		}
		// Correct baseline votes
		for (Candidate c : list) {
			c.setBaselineVotes(baselines-- > 0);
		}

		return candidateRepository.updateCandidates(list);
	}

	public Voter convertCandidateToNewVoter(Candidate candidate, ElectionEvent electionEvent) {
		Voter voter = new Voter();
		voter.setDateOfBirth(candidate.getDateOfBirth());
		voter.setFirstName(candidate.getFirstName());
		voter.setMiddleName(candidate.getMiddleName());
		voter.setLastName(candidate.getLastName());
		voter.updateNameLine();
		voter.setAddressLine1(candidate.getAddressLine1());
		voter.setPostalCode(candidate.getPostalCode());
		voter.setElectionEvent(electionEvent);
		return voter;
	}

	public Candidate convertVoterToCandidate(Candidate candidate, Voter voter) {
		candidate.setFirstName(voter.getFirstName().trim());
		candidate.setMiddleName(voter.getMiddleName() != null ? voter.getMiddleName().trim() : null);
		candidate.setLastName(voter.getLastName().trim());
		candidate.setNameLine();
		candidate.setId(voter.getId());
		if (voter.getDateOfBirth() != null) {
			candidate.setDateOfBirth(voter.getDateOfBirth());
		}
		if (!StringUtils.isEmpty(voter.getAddressLine1())) {
			candidate.setAddressLine1(voter.getAddressLine1().trim());
		} else {
			candidate.setAddressLine1(EMPTY);
		}
		if (!StringUtils.isEmpty(voter.getPostalCode())) {
			candidate.setPostalCode(voter.getPostalCode().trim());
		} else {
			candidate.setPostalCode(EMPTY);
		}
		if (!StringUtils.isEmpty(voter.getPostTown())) {
			candidate.setPostTown(voter.getPostTown().trim());
		} else {
			candidate.setPostTown(EMPTY);
		}

		return candidate;
	}

	private boolean isCandidateInOtherAffiliations(UserData userData, Candidate candidate, Affiliation affiliation) {
		Ballot ballot = affiliation.getBallot();
		ElectionGroup electionGroup = ballot.getContest().getElection().getElectionGroup();
		List<Candidate> candidatesFromOtherListProposal = candidateRepository.findByIdInAnotherElection(candidate.getId(), ballot.getPk(),
				electionGroup.getPk());
		if (!candidatesFromOtherListProposal.isEmpty()) {
			Candidate candidateFromList = candidatesFromOtherListProposal.get(0);

			Party party = candidateFromList.getAffiliation().getParty();
			LocaleText localeText = localeTextRepository.findByElectionEventLocaleAndTextId(party.getElectionEvent().getPk(), userData.getLocale().getPk(),
					party.getName());
			String existInPartyName = localeText != null ? localeText.getLocaleText() : null;
			Contest contest = candidateFromList.getBallot().getContest();
			String contestName = contest.getName();
			String election = contest.getElection().getName();
			String existInContestName = contestName + " (" + election + ")";
			candidate.addValidationMessage(new UserMessage("@listProposal.approve.candidate", existInPartyName, existInContestName));
			return true;
		}
		return false;
	}

	public void deleteAndReorder(UserData userData, Candidate candidate, Long ballotPk) {
		candidateRepository.deleteCandidate(userData, candidate.getPk());

		List<Candidate> candidatesBelow = candidateRepository.findByBelowDisplayOrder(ballotPk, candidate.getDisplayOrder());

		setBaseLineVoteToNextCandidate(candidate, candidatesBelow);

		for (Candidate aCandidate : candidatesBelow) {
			aCandidate.setDisplayOrder(aCandidate.getDisplayOrder() - 1);
			candidateRepository.updateCandidate(userData, aCandidate);
		}
	}

	private void setBaseLineVoteToNextCandidate(Candidate candidate, List<Candidate> candidatesBelow) {
		if (!candidatesBelow.isEmpty()) {
			if (candidate.isBaselineVotes()) {
				for (Candidate candidateBelow : candidatesBelow) {
					if (!candidateBelow.isBaselineVotes()) {
						candidateBelow.setBaselineVotes(candidate.isBaselineVotes());
						break;
					}
				}
			}
		}
	}
}
