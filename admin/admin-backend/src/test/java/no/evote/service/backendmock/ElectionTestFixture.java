package no.evote.service.backendmock;

import no.evote.constants.CountingHierarchy;
import no.evote.constants.EvoteConstants;
import no.evote.constants.VotingHierarchy;
import no.evote.service.security.LegacyUserDataServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.service.ElectionEventDomainService;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.Assert;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;


public class ElectionTestFixture extends BaseTestFixture {

	private Validator validator;

	private ElectionEvent electionEvent;
	private ElectionDay electionDay;
	private ElectionGroup electionGroup;
	private Election election;
	private Contest contest;

	private ElectionEventDomainService electionEventServiceBean;
	private ElectionGroupRepository electionGroupRepository;
	private ElectionRepository electionRepository;
	private ContestRepository contestRepository;
	private LocaleRepository localeRepository;
	private ElectionEventRepository electionEventRepository;

	public ElectionTestFixture(LegacyUserDataServiceBean userDataService, AccessRepository accessRepository,
							   ElectionGroupRepository electionGroupRepository, ElectionRepository electionRepository,
                               ContestRepository contestRepository,
                               LocaleRepository localeRepository,
                               ElectionEventRepository electionEventRepository, ElectionEventDomainService electionEventServiceBean) {
		super(userDataService, accessRepository);

		this.electionGroupRepository = electionGroupRepository;
		this.electionRepository = electionRepository;
		this.contestRepository = contestRepository;
		this.localeRepository = localeRepository;
		this.electionEventRepository = electionEventRepository;
		this.electionEventServiceBean = electionEventServiceBean;
	}

	public ElectionTestFixture(BackendContainer backend) {
		this(backend.getUserDataService(), backend.getAccessRepository(), backend.getElectionGroupRepository(),
				backend.getElectionRepository(), backend.getContestRepository(), backend.getLocaleRepository(),
				backend.getElectionEventRepository(), backend.getElectionEventService());
	}

	public void init() {
		super.init();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	public ElectionEvent buildElectionEvent() {
		ElectionEvent electionEvent = new ElectionEvent();
		electionEvent.setId("209901");
		electionEvent.setName("Valg 2099");
		electionEvent.setElectionEventStatus(electionEventRepository.findElectionEventStatusById(ElectionEventStatusEnum.LOCAL_CONFIGURATION.id()));

		electionEvent.setElectoralRollCutOffDate(LocalDate.now());
		electionEvent.setVotingCardDeadline(LocalDate.now());
		electionEvent.setVotingCardElectoralRollDate(LocalDate.now());
		electionEvent.setVoterNumbersAssignedDate(LocalDate.now());

		Locale locale = localeRepository.findById(LOCALE_NB_NO);
		electionEvent.setLocale(locale);

		Set<ConstraintViolation<ElectionEvent>> constraintViolations = validator.validate(electionEvent);
		Assert.assertEquals(constraintViolations.size(), 0);

		return electionEvent;
	}

	public ElectionDay buildElectionDay(final ElectionEvent electionEvent) {
		ElectionDay electionDay = new ElectionDay();
		electionDay.setElectionEvent(electionEvent);
		electionDay.setDate(LocalDate.now());
		electionDay.setStartTime(LocalTime.now());
		electionDay.setEndTime(LocalTime.now());
		return electionDay;
	}

	public ElectionGroup buildElectionGroup(final ElectionEvent electionEvent) {
		ElectionGroup electionGroup = new ElectionGroup();
		electionGroup.setId("68");
		electionGroup.setName("Election group 1");
		electionGroup.setElectionEvent(electionEvent);

		Set<ConstraintViolation<ElectionGroup>> constraintViolations = validator.validate(electionGroup);
		Assert.assertEquals(constraintViolations.size(), 0);

		return electionGroup;
	}

	public Election buildElection(final ElectionGroup electionGroup) {
		Election election = new Election();
		election.setId("35");
		election.setName("Election 1");
		election.setCandidateRankVoteShareThreshold(BigDecimal.ZERO);
		election.setSettlementFirstDivisor(BigDecimal.valueOf(1.4));
		election.setLevelingSeatsVoteShareThreshold(BigDecimal.ZERO);
		election.setMaxCandidateNameLength(25);

		election.setEndDateOfBirth(LocalDate.now().minusYears(18));

		ElectionType electionType = electionRepository.findElectionTypeById(EvoteConstants.ELECTION_TYPE_REFERENDUM);
		election.setElectionType(electionType);

		election.setElectionGroup(electionGroup);

		Set<ConstraintViolation<Election>> constraintViolations = validator.validate(election);

		Assert.assertEquals(constraintViolations.size(), 0);

		return election;
	}

	public Contest buildContest(final Election election) {
		Contest contest = new Contest();
		contest.setId("999999");
		contest.setName("Contest 1");
		contest.setElection(election);

		Set<ConstraintViolation<Contest>> constraintViolations = validator.validate(contest);
		Assert.assertEquals(constraintViolations.size(), 0);

		return contest;
	}

	public ElectionEvent createElectionEvent(final ElectionEvent electionEvent) {
		ElectionEvent localElectionEvent = electionEventServiceBean.create(getSysAdminUserData(), electionEvent, false, VotingHierarchy.NONE,
				CountingHierarchy.NONE, null, null);

		// Asserts
		Assert.assertNotNull(localElectionEvent);
		Assert.assertNotNull(localElectionEvent.getPk());
		Assert.assertTrue(localElectionEvent.getPk() > 0);
		return localElectionEvent;
	}

	public ElectionEvent createElectionEventCopyElectoralRoll(final ElectionEvent electionEvent) {
		VotingHierarchy votingHierarchy = VotingHierarchy.getVotingHierarchy(true, true, true, false);
		CountingHierarchy countingHierarchy = CountingHierarchy.getCountingHierarchy(true, true, false, false, false, true, false);

		return electionEventServiceBean.create(getSysAdminUserData(), electionEvent, false, votingHierarchy, countingHierarchy, electionEventRepository
				.findById("200701"), null);
	}

	public void createElectionDay(final ElectionDay electionDay) {
		electionEventRepository.createElectionDay(getUserData(), electionDay);

		// Asserts
		Assert.assertNotNull(electionDay);
		Assert.assertTrue(electionDay.getPk() > 0);
	}

	public void createElectionGroup(final ElectionGroup electionGroup) {
		electionGroupRepository.create(getUserData(), electionGroup);

		// Asserts
		Assert.assertNotNull(electionGroup);
		Assert.assertNotNull(electionGroup.getPk());
		Assert.assertTrue(electionGroup.getPk() > 0);
	}

	public void createElection(final Election election) {
		electionRepository.create(getUserData(), election);

		// Asserts
		Assert.assertNotNull(election);
		Assert.assertNotNull(election.getPk());
		Assert.assertTrue(election.getPk() > 0);
	}

	public void createContest(final Contest contest) {
		contestRepository.create(getUserData(), contest);

		// Asserts
		Assert.assertNotNull(contest);
		Assert.assertNotNull(contest.getPk());
		Assert.assertTrue(contest.getPk() > 0);
	}

	public Validator getValidator() {
		return validator;
	}

	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	public ElectionDay getElectionDay() {
		return electionDay;
	}

	public void setElectionDay(final ElectionDay electionDay) {
		this.electionDay = electionDay;
	}

	public ElectionGroup getElectionGroup() {
		return electionGroup;
	}

	public void setElectionGroup(final ElectionGroup electionGroup) {
		this.electionGroup = electionGroup;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(final Election election) {
		this.election = election;
	}

	public Contest getContest() {
		return contest;
	}

	public void setContest(final Contest contest) {
		this.contest = contest;
	}

}

