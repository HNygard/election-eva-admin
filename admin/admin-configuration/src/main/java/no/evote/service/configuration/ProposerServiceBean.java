package no.evote.service.configuration;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
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
import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.ListProposalValidationData;
import no.evote.security.SecureEntity;
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
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.ProposerRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

@ApplicationScoped
@Default
public class ProposerServiceBean {
	private static final Logger LOGGER = Logger.getLogger(ProposerServiceBean.class);
	private static final String EMPTY = "";

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	@Inject
	private LocaleTextRepository localeTextRepository;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private VoterRepository voterRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private CandidateRepository candidateRepository;
	@Inject
	private AffiliationRepository affiliationRepository;
	@Inject
	private ProposerRepository proposerRepository;

	public ProposerServiceBean() {

	}

	public List<Proposer> updateAll(UserData userData, List<Proposer> proposerList) {
		List<Proposer> updatedProposers = new ArrayList<>();
		for (Proposer proposer : proposerList) {
			Proposer updatedProposer = proposerRepository.updateProposer(userData, proposer);
			/** Set validationmessage on merged proposer */
			if (proposer.isInvalid()) {
				updatedProposer.setValidationMessageList(proposer.getValidationMessageList());
			}
			updatedProposers.add(updatedProposer);
		}
		return updatedProposers;
	}

	public Proposer createNewProposer(Ballot ballot) {
		Proposer proposer = new Proposer();
		proposer.setId(EMPTY);
		proposer.setFirstName(EMPTY);
		proposer.setLastName(EMPTY);
		proposer.setNameLine(EMPTY);
		proposer.setBallot(ballot);

		return proposer;
	}

	/**
	 * All list proposals have minimum two proposers ('Tillitsvalgt' and 'Varatillitsvalgt').
	 */
	public void createDefaultProposers(UserData userData, @SecureEntity(electionLevel = ElectionLevelEnum.CONTEST) Ballot ballot) {
		Proposer proposer = createNewProposer(ballot);
		Proposer proposer2 = createNewProposer(ballot);
		proposer.setProposerRole(proposerRepository.findProposerRoleById("TILV"));
		proposer.setDisplayOrder(1);
		proposer.setId("00000000001");
		proposer2.setProposerRole(proposerRepository.findProposerRoleById("VARV"));
		proposer2.setDisplayOrder(2);
		proposer2.setId("00000000002");
		proposerRepository.createProposer(userData, proposer);
		proposerRepository.createProposer(userData, proposer2);
	}

	public ListProposalValidationData isOcrProposersDataSet(UserData userData, ListProposalValidationData validationData) {
		for (Proposer proposer : validationData.getProposerList()) {
			validate(proposer.getDateOfBirth() != null, "@listProposal.proposer.ocr.dateFailed", proposer, validationData);
			validate(isNotBlank(proposer.getFirstName()), "@listProposal.proposer.ocr.noFirstName", proposer, validationData);
			validate(isNotBlank(proposer.getLastName()), "@listProposal.proposer.ocr.noLastName", proposer, validationData);
		}
		return validationData;
	}

	private void validate(boolean condition, String message, Proposer proposer, ListProposalValidationData validationData) {
		if (!condition) {
			proposer.addValidationMessage(new UserMessage(message));
			setApprovedFalse(validationData, proposer);
		}
	}

	public ListProposalValidationData approveProposers(
			UserData userData, ListProposalValidationData approved, String electionEventId, Set<MvArea> mvAreas, Affiliation affiliation) {
		Map<String, Proposer> idList = new HashMap<>();

		for (Proposer proposer : approved.getProposerList()) {
			proposer.clearValidationMessages();

			if (isInElectoralRoll(proposer, electionEventId, mvAreas)) {
				Long ballotPk = affiliation.getBallot().getPk();
				Long electionPk = affiliation.getBallot().getContest().getElection().getPk();
				if (isProposerInOtherBallots(userData, proposer, ballotPk, electionPk)) {
					setApprovedFalse(approved, proposer);
				} else if (isProposerAsCandidateInOtherBallots(userData, proposer, ballotPk, electionPk)) {
					setApprovedFalse(approved, proposer);
				}
			} else {
				setApprovedFalse(approved, proposer);
			}

			/** Find duplicates */
			if (proposer.isIdSet() && idList.containsKey(proposer.getId())) {
				setDuplicateMessage(proposer, idList.get(proposer.getId()));
				setMockIdForEmptyId(proposer, affiliation.getBallot().getPk(), idList);
				setApprovedFalse(approved, proposer);
			}
			idList.put(proposer.getId(), proposer);
		}

		return approved;
	}

	public Proposer setMockIdForEmptyId(Proposer proposer, Long ballotPk, Map<String, Proposer> existingIds) {
		int id = 0;
		LocalDate dateOfBirth = proposer.getDateOfBirth();
		String birth;
		if (dateOfBirth == null) {
			birth = "000000";
		} else {
			birth = DateUtil.getFormattedShortIdDate(dateOfBirth);
		}
		do {
			String nr = String.format("%05d", id++);
			proposer.setId(birth + nr);
		} while (!isValidMockId(proposer, ballotPk, existingIds));
		if (existingIds != null) {
			existingIds.put(proposer.getId(), proposer);
		}
		return proposer;
	}

	/**
	 * Checks if mockId is an invalid SSN and that no other person on that ballot has that id.
	 */
	private boolean isValidMockId(Proposer proposer, Long ballotPk, Map<String, Proposer> existingIds) {
		boolean presentInIdList = existingIds != null && existingIds.containsKey(proposer.getId());
		boolean presentInPersistedProposalPersons = proposerRepository.findProposerByBallotAndId(ballotPk, proposer.getId()) != null;
		boolean isValidSSN = FoedselsNummerValidator.isFoedselsNummerValid(proposer.getId());

		return !presentInIdList && !presentInPersistedProposalPersons && !isValidSSN;
	}

	private void setDuplicateMessage(Proposer proposer, Proposer duplicatedProposer) {
		proposer.addValidationMessage(new UserMessage("@listProposal.validation.duplicatedIdFromRoll", duplicatedProposer.getDisplayOrder()));
	}

	private void setApprovedFalse(ListProposalValidationData approved, Proposer proposer) {
		approved.setApproved(false);
		proposer.setApproved(false);
	}

	private boolean isInElectoralRoll(Proposer proposer, String electionEventId, Set<MvArea> mvAreas) {
		if (!proposer.isIdSet()) {
			if (hasOneHitInRollAndIsConvertedFromVoter(proposer, electionEventId, mvAreas)) {
				return searchInElectoralRoll(proposer, electionEventId, mvAreas);
			}
		} else {
			return searchInElectoralRoll(proposer, electionEventId, mvAreas);
		}
		return false;
	}

	private boolean searchInElectoralRoll(Proposer proposer, String electionEventId, Set<MvArea> mvAreas) {
		List<Voter> voterResult = getApprovedVoters(
				voterRepository.findByElectionEventAndId(electionEventRepository.findById(electionEventId).getPk(), proposer.getId()));

		boolean inElectoralRoll = !voterResult.isEmpty();

		if (inElectoralRoll) {
			if (!mvAreas.isEmpty()) {
				Voter voter = voterResult.get(0);
				return isInContest(proposer, voter.getMunicipalityId(), mvAreas);
			}
			return true;
		} else {
			proposer.addValidationMessage(new UserMessage("@listProposal.approve.notInRoll"));
			return false;
		}
	}

	private boolean isInContest(Proposer proposer, String municipalityId, Set<MvArea> mvAreas) {
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
			proposer.addValidationMessage(new UserMessage("@listProposal.approve.inRollNotInCounty"));
		} else if (!isInMunicipality) {
			proposer.addValidationMessage(new UserMessage("@listProposal.approve.inRollNotInMunicipality"));
		} else {
			proposer.addValidationMessage(new UserMessage("@listProposal.approve.notInRoll"));
		}
		return false;
	}

	private boolean isInCounty(MvArea mvArea, Municipality proposerMunicipality) {
		return mvArea.getCounty().getId().equals(proposerMunicipality.getCounty().getId());
	}

	private boolean isInMunicipality(MvArea mvArea, String municipalityId) {
		return mvArea.getMunicipalityId().equals(municipalityId.trim());
	}

	private AreaPath buildMunicipalityPath(MvArea mvArea, String municipalityId) {
		return AreaPath.from(mvArea.getElectionEventId(), mvArea.getCountryId(), municipalityId.substring(0, 2), municipalityId);
	}

	private boolean hasOneHitInRollAndIsConvertedFromVoter(Proposer proposer, String electionEventId, Set<MvArea> mvAreas) {
		List<Voter> voterResult = searchVoter(proposer, electionEventId, mvAreas);
		boolean uniqueIdInElectoralRoll = (voterResult.size() == 1);

		if (uniqueIdInElectoralRoll) {
			convertVoterToProposer(proposer, voterResult.get(0));
			return true;
		}

		boolean toManyResult = (voterResult.size() > 1);
		boolean noResult = voterResult.isEmpty();
		if (toManyResult) {
			proposer.addValidationMessage(new UserMessage("@listProposal.approve.manyInRoll"));
		} else if (noResult) {
			proposer.addValidationMessage(new UserMessage("@listProposal.approve.notInRoll"));
		}
		return false;
	}

	public List<Voter> searchVoter(Proposer proposer, String electionEventId, Set<MvArea> mvAreas) {
		Voter voter = convertProposerToNewVoter(proposer, electionEventRepository.findById(electionEventId));

		List<Voter> voterResult = new ArrayList<>();
		if (mvAreas.isEmpty()) {
			voterResult.addAll(searchVoterBasedOnAreaRestrictions(voter, proposer, null));
		} else {
			for (MvArea mvArea : mvAreas) {
				voterResult.addAll(searchVoterBasedOnAreaRestrictions(voter, proposer, mvArea));
			}
		}

		return getApprovedVoters(voterResult);
	}

	private List<Voter> searchVoterBasedOnAreaRestrictions(Voter voter, Proposer proposer, MvArea mvArea) {
		String municipalityId = null;
		String countyId = null;

		if (mvArea != null) {
			if (mvArea.getAreaLevel() == 2) {
				countyId = mvArea.getCountyId();
			} else if (mvArea.getAreaLevel() >= AreaLevelEnum.MUNICIPALITY.getLevel()) {
				municipalityId = mvArea.getMunicipalityId();
			}
		}

		
		List<Voter> voterResult = proposer.hasSearchInformation()
				? voterRepository.searchVoter(voter, countyId, municipalityId, 10, true, voter.getElectionEvent().getPk()) : new ArrayList<Voter>();

		/** Try to search without address if first result is empty */
		if (proposer.hasSearchInformation() && !StringUtils.isEmpty(voter.getAddressLine1()) && voterResult.isEmpty()) {
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

	public Voter convertProposerToNewVoter(Proposer proposer, ElectionEvent electionEvent) {
		Voter voter = new Voter();
		voter.setDateOfBirth(proposer.getDateOfBirth());
		voter.setNameLine(getNamelineWithoutWildCard(proposer.getFirstName(), proposer.getLastName()));
		voter.setElectionEvent(electionEvent);
		return voter;
	}

	public Proposer convertVoterToProposer(Proposer proposer, Voter voter) {
		proposer.setFirstName(voter.getFirstName());
		proposer.setLastName(voter.getLastName());
		proposer.setNameLine();
		proposer.setId(voter.getId());
		proposer.setDateOfBirth(voter.getDateOfBirth());
		proposer.setAddressLine1(voter.getAddressLine1());
		proposer.setPostalCode(voter.getPostalCode());
		proposer.setPostTown(voter.getPostTown());

		return proposer;
	}

	private String getNamelineWithoutWildCard(String firstName, String lastName) {
		StringBuilder nameLine = new StringBuilder(EMPTY);
		if (!firstName.contains("*")) {
			nameLine.append(firstName);
			nameLine.append(" ");
		}
		if (!lastName.contains("*")) {
			nameLine.append(lastName);
		}
		return nameLine.toString();
	}

	private boolean isProposerInOtherBallots(UserData userData, Proposer proposer, Long ballotPk, Long electionPk) {
		List<Proposer> proposersFromOtherListproposal = proposerRepository.findByIdInOtherBallot(proposer.getId(), ballotPk, electionPk);
		if (!proposersFromOtherListproposal.isEmpty()) {
			Ballot ballot = proposersFromOtherListproposal.get(0).getBallot();
			Affiliation affiliation = affiliationRepository.findByBallot(ballot.getPk());
			Long electionEventPk = affiliation.getParty().getElectionEvent().getPk();
			String existInPartyName = localeTextRepository
					.findByElectionEventLocaleAndTextId(electionEventPk, userData.getLocale().getPk(), affiliation.getParty().getName()).getLocaleText();
			String existInContestName = proposersFromOtherListproposal.get(0).getBallot().getContest().getName();
			proposer.addValidationMessage(new UserMessage("@listProposal.approve.proposer", existInPartyName, existInContestName));
			return true;
		}
		return false;
	}

	private boolean isProposerAsCandidateInOtherBallots(UserData userData, Proposer proposer, Long ballotPk, Long electionPk) {
		List<Candidate> candidateFromOtherListProposal = candidateRepository.findByIdInOtherBallot(proposer.getId(), ballotPk, electionPk);
		if (!candidateFromOtherListProposal.isEmpty()) {
			Long electionEventPk = candidateFromOtherListProposal.get(0).getAffiliation().getParty().getElectionEvent().getPk();
			String existInPartyId = localeTextRepository.findByElectionEventLocaleAndTextId(electionEventPk, userData.getLocale().getPk(),
					candidateFromOtherListProposal.get(0).getAffiliation().getParty().getName()).getLocaleText();
			String existInContestName = candidateFromOtherListProposal.get(0).getBallot().getContest().getName();

			proposer.addValidationMessage(new UserMessage("@listProposal.approve.proposerCandidate", existInPartyId, existInContestName));

			return true;
		}
		return false;
	}

	public List<Proposer> changeDisplayOrder(Proposer proposer, int fromPosition, int toPosition) {
		if (fromPosition == toPosition) {
			throw new IllegalArgumentException("changeDisplayOrder displayOrderFrom[" + fromPosition + "] equal to displayOrderTo[" + toPosition + "]");
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("changeDisplayOrder[" + proposer + ", from " + fromPosition + ", to " + toPosition);
		}

		// Get the affected candidates
		boolean fromTopToBottom = fromPosition < toPosition;
		int low = fromTopToBottom ? fromPosition : toPosition;
		int high = fromTopToBottom ? toPosition : fromPosition;
		List<Proposer> list = proposerRepository.findProposerByBallotAndDisplayOrderRange(proposer.getBallot().getPk(), low, high);
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

		// Correct all the display order values
		int currentDisplayOrder = low;
		for (Proposer p : list) {
			p.setDisplayOrder(currentDisplayOrder++);
		}
		return proposerRepository.updateProposers(list);
	}

	public Proposer validate(UserData userData, Proposer proposer, Long ballotPk) {
		proposer.clearValidationMessages();
		Set<ConstraintViolation<Proposer>> constraintViolations = validator.validate(proposer, ProposalValidationManual.class);

		if (constraintViolations.isEmpty()) {
			if (proposer.isIdSet()) {
				if (!isDateOfBirthAndSnnSame(proposer)) {
					proposer.addValidationMessage(new UserMessage("@listProposal.validation.bithDateSNNMismatch"));
				}

				Proposer otherIdCandidate = proposerRepository.findProposerByBallotAndId(ballotPk, proposer.getId());
				if (otherIdCandidate != null && !otherIdCandidate.getPk().equals(proposer.getPk())) {
					proposer.addValidationMessage(new UserMessage("@listProposal.validation.duplicatedId",
							otherIdCandidate.getDisplayOrder()));
				}
			}
		} else {
			constraintViolations.stream().map(c -> new UserMessage(c.getMessage())).forEach(proposer::addValidationMessage);
		}
		return proposer;
	}

	private boolean isDateOfBirthAndSnnSame(Proposer proposer) {
		String dateOfBirth = DateUtil.getFormattedShortIdDate(proposer.getDateOfBirth());
		
		String ssnId = proposer.getId().substring(0, 6);
		
		return dateOfBirth.equals(ssnId);
	}

	public void deleteAndReorder(UserData userData, Proposer proposer, Long ballotPk) {
		proposerRepository.deleteProposer(userData, proposer.getPk());
		List<Proposer> proposersBelow = proposerRepository.findByBelowDisplayOrder(ballotPk, proposer.getDisplayOrder());
		for (Proposer dbProposer : proposersBelow) {
			dbProposer.setDisplayOrder(dbProposer.getDisplayOrder() - 1);
			proposerRepository.updateProposer(userData, dbProposer);
		}
	}

}
