package no.evote.service.configuration;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.TransactionSynchronizationRegistry;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteSecurityException;
import no.evote.security.UserData;
import no.valg.eva.admin.util.StringUtil;
import no.valg.eva.admin.backend.rbac.RBACAuthenticator;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.ContestAreaRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;

import org.apache.commons.lang3.StringUtils;

public class ContestServiceBean {

	@Inject
	private BallotRepository ballotRepository;
	@Inject
	private ContestRepository contestRepository;
	@Inject
	private AffiliationServiceBean affiliationService;
	@Inject
	private AffiliationRepository affiliationRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private ContestAreaRepository contestAreaRepository;
	@Inject
	private RBACAuthenticator rbacAuthenticator;
	@Inject
	private PartyRepository partyRepository;
	@Inject
	private PartyCategoryRepository partyCategoryRepository;

	public Contest create(UserData userData, Contest contest, MvArea area) {
		return create(userData, contest, area, false, false);
	}

	public Contest create(UserData userData, Contest contest, MvArea area, boolean parentArea, boolean childArea) {
		checkAreaLevel(contest, area);

		contestRepository.create(userData, contest);
		if (contest.getElection().getElectionType().isPropotionalRepresentation()) {
			createBallotAffiliationBlank(userData, contest, area.getElectionEvent());
		}

		// This MvElection is created with trigger on contest insert. If areaLevel on the newly created MvElection does not have
		// the same areaLevel as the incoming MvArea, update it. See bug EVA-902
		MvElection mvElection = mvElectionRepository.findByContest(contest);
		if (mvElection.getActualAreaLevel() == AreaLevelEnum.COUNTY && area.getActualAreaLevel() == AreaLevelEnum.MUNICIPALITY) {
			mvElection.setAreaLevel(area.getAreaLevel());
			mvElectionRepository.update(userData, mvElection);
		}

		ContestArea contestArea = new ContestArea();
		contestArea.setContest(contest);
		contestArea.setMvArea(area);
		contestArea.setParentArea(parentArea);
		contestArea.setChildArea(childArea);
		contestArea = contestAreaRepository.create(userData, contestArea);

		contest.getContestAreaSet().add(contestArea);
		return contest;
	}

	public void createContestsForElection(UserData userData, Election election) {
		List<MvArea> mvAreaList = mvAreaRepository.findByPathAndLevel(election.getElectionGroup().getElectionEvent().getId(), election.getAreaLevel());
		for (MvArea mv : mvAreaList) {
			Contest contest = new Contest();
			contest.setElection(election);
			AreaLevelEnum level = AreaLevelEnum.getLevel(election.getAreaLevel());
			
			switch (level) {
			case COUNTRY:
				contest.setId(StringUtil.prefixString(mv.getCountryId(), 6, '0'));
				contest.setName(mv.getCountryName());
				break;
			case COUNTY:
				contest.setId(StringUtil.prefixString(mv.getCountyId(), 6, '0'));
				contest.setName(mv.getCountyName());
				break;
			case MUNICIPALITY:
				contest.setId(StringUtil.prefixString(mv.getMunicipalityId(), 6, '0'));
				contest.setName(mv.getMunicipalityName());
				break;
			case BOROUGH:
				if (!mv.getBorough().isMunicipality1()) {
					contest.setId(StringUtil.prefixString(mv.getBoroughId(), 6, '0'));
					contest.setName(mv.getBoroughName());
				}
				break;
			default:
				break;
			}
			
			if (!StringUtils.isEmpty(contest.getId()) && !StringUtils.isEmpty(contest.getName())) {
				this.create(userData, contest, mv);
			}
		}
	}

	public List<Contest> getContestsByStatus(Long electionEventPk, Integer contestStatusId) {
		return contestRepository.findContestsByStatus(electionEventPk, no.valg.eva.admin.common.configuration.status.ContestStatus.fromId(contestStatusId));
	}

	/**
	 * Checks user's access rights and updates contest.
	 * @param userData contains access rights
	 * @param contest contest to update
	 * @return updated contest
	 */
	public Contest update(UserData userData, Contest contest, TransactionSynchronizationRegistry registry) {
		checkAccessToContestAreas(userData, contest, registry);
		return contestRepository.update(userData, contest);
	}

	private List<ContestArea> checkAccessToContestAreas(UserData userData, Contest contest, TransactionSynchronizationRegistry registry) {
		List<ContestArea> contestAreas = contestAreaRepository.findContestAreasForContest(contest.getPk());

		for (ContestArea contestArea : contestAreas) {
			if (!rbacAuthenticator.hasAccess(userData, contestArea, registry)) {
				throw new EvoteSecurityException("User does not have access to area " + contestArea.getMvArea().getAreaPath());
			}
		}
		return contestAreas;
	}

	public void delete(UserData userData, Long pk, TransactionSynchronizationRegistry registry) {
		List<ContestArea> contestAreas = checkAccessToContestAreas(userData, contestRepository.findByPk(pk), registry);
		Contest contest = contestRepository.findByPk(pk);
		if (!isStatusCentralConfig(contest)) {
			throw new EvoteSecurityException("Cannot delete contest on electionEvent with other status than Central config: " + contest.getPk());
		}

		if (!contestAreas.isEmpty()) {
			ContestArea parent = null;
			for (ContestArea contestArea : contestAreas) {
				if (contestArea.isParentArea()) {
					parent = contestArea;
				} else {
					contestAreaRepository.delete(userData, contestArea.getPk());
				}
			}
			if (parent != null) {
				contestAreaRepository.delete(userData, parent.getPk());
			}
		}

		// Any affiliations and ballots or referendum options are deleted because of cascading
		contestRepository.delete(userData, pk);
	}

	private boolean isStatusCentralConfig(Contest contest) {
		int configLevel = contest.getElection().getElectionGroup().getElectionEvent().getElectionEventStatus().getId();
		return configLevel == ElectionEventStatusEnum.CENTRAL_CONFIGURATION.id();
	}

	private void createBallotAffiliationBlank(UserData userData, Contest contest, ElectionEvent electionEvent) {
		Party blankParty = partyRepository.findPartyByIdAndEvent(EvoteConstants.PARTY_ID_BLANK, electionEvent.getPk());
		if (blankParty == null) {
			// If BLANK party is not defined for election event - create it
			blankParty = new Party();
			blankParty.setPartyCategory(partyCategoryRepository.findPartyCategoryByPk(1L));
			blankParty.setId(EvoteConstants.PARTY_ID_BLANK);
			
			blankParty.setElectionEvent(electionEvent);
			blankParty.setBlank(true);
			blankParty.setShortCode(9999);
			blankParty.setApproved(true);
			blankParty = partyRepository.create(userData, blankParty);
			
		}
		Affiliation affiliation = affiliationService.createNewAffiliation(userData, contest, blankParty, userData.getLocale(),
				BallotStatus.BallotStatusValue.APPROVED.getId());
		Ballot ballot = affiliation.getBallot();
		affiliation.setDisplayOrder(1);
		affiliation.setApproved(true);
		ballot.setDisplayOrder(1);
		ballot.setApproved(true);

		affiliationRepository.updateAffiliation(userData, affiliation);
		ballotRepository.updateBallot(userData, ballot);
	}

	private void checkAreaLevel(Contest contest, MvArea mvArea) {
		AreaLevelEnum electionAreaLevel = AreaLevelEnum.getLevel(contest.getElection().getAreaLevel());
		AreaLevelEnum areaLevel = mvArea.getActualAreaLevel();
		boolean isSameLevel = electionAreaLevel == areaLevel;
		boolean isAreaLevelIsCountyOrMunicipality = areaLevel == AreaLevelEnum.COUNTY || areaLevel == AreaLevelEnum.MUNICIPALITY;
		if (isSameLevel || (electionAreaLevel == AreaLevelEnum.COUNTY && isAreaLevelIsCountyOrMunicipality)) {
			return;
		}
		throw new IllegalArgumentException("Invalid areaLevel for contest. ElectionAreaLevel=" + electionAreaLevel + ", areaLevel=" + areaLevel);
	}

}
