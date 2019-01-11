package no.evote.service.configuration;

import java.math.BigDecimal;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import no.evote.constants.EvoteConstants;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.configuration.model.election.GenericElectionType;
import no.valg.eva.admin.common.configuration.service.ElectionGroupService;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;

import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;


public abstract class ElectionBaseTest extends AbstractJpaTestBase {
	protected static final String LOCALE_NB_NO = "nb-NO";
	protected ContestRepository contestRepository;
	protected ServiceBackedRBACTestFixture rbacTestFixture;
	protected BackendContainer backend;
	private Validator validator;
	private ElectionEvent electionEvent;
	private ElectionDay electionDay;
	private ElectionGroup electionGroup;
	private Election election;
	private Contest contest;
	private ElectionEventRepository electionEventRepository;
	private ElectionGroupServiceBean electionGroupServiceBean;
	private ElectionGroupRepository electionGroupRepository;
	private ElectionServiceBean electionServiceBean;
	private ElectionRepository electionRepository;
	private ContestServiceBean contestService;
	private LocaleRepository localeRepository;
	private MvElectionRepository mvElectionRepository;
	private MvAreaRepository mvAreaRepository;
	private ElectionGroupService electionGroupService;

	public void init() {
		setElectionEvent(buildElectionEvent());
		setElectionGroup(buildElectionGroup(getElectionEvent()));
		getElectionEvent().getElectionGroups().add(getElectionGroup());
		setElection(buildElection(getElectionGroup()));
		getElectionGroup().getElections().add(getElection());
		createElectionEvent(getElectionEvent());
	}

	@BeforeMethod(alwaysRun = true)
	public void initDependencies() {
		backend = new BackendContainer(getEntityManager());
		backend.initServices();

		electionEventRepository = backend.getElectionEventRepository();
		electionGroupServiceBean = backend.getElectionGroupServiceBean();
		electionGroupRepository = backend.getElectionGroupRepository();
		electionServiceBean = backend.getElectionServiceBean();
		electionGroupService = backend.getElectionGroupService();
		electionRepository = backend.getElectionRepository();
		contestService = backend.getContestServiceBean();
		contestRepository = backend.getContestRepository();
		localeRepository = backend.getLocaleRepository();
		mvElectionRepository = backend.getMvElectionRepository();
		mvAreaRepository = backend.getMvAreaRepository();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();

		rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
		rbacTestFixture.init();
	}

	protected ElectionEvent buildElectionEvent() {
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

	protected ElectionGroup buildElectionGroup(final ElectionEvent electionEvent) {
		ElectionGroup electionGroup = new ElectionGroup();
		electionGroup.setId("68");
		electionGroup.setName("Election group 1");
		electionGroup.setElectionEvent(electionEvent);

		Set<ConstraintViolation<ElectionGroup>> constraintViolations = validator.validate(electionGroup);
		Assert.assertEquals(constraintViolations.size(), 0);

		return electionGroup;
	}

	protected Election buildElection(final ElectionGroup electionGroup) {
		Election election = new Election();
		election.setId("35");
		election.setName("Election 1");
		election.setCandidateRankVoteShareThreshold(BigDecimal.ZERO);
		election.setSettlementFirstDivisor(BigDecimal.valueOf(1.4));
		election.setLevelingSeatsVoteShareThreshold(BigDecimal.ZERO);

		election.setEndDateOfBirth(LocalDate.now().minusYears(18));

		ElectionType electionType = electionRepository.findElectionTypeById(EvoteConstants.ELECTION_TYPE_REFERENDUM);
		election.setElectionType(electionType);

		election.setElectionGroup(electionGroup);

		Set<ConstraintViolation<Election>> constraintViolations = validator.validate(election);

		Assert.assertEquals(constraintViolations.size(), 0);

		return election;
	}

	protected no.valg.eva.admin.common.configuration.model.election.Election buildCommonElection(final ElectionGroup electionGroup) {
		no.valg.eva.admin.common.configuration.model.election.Election election = new no.valg.eva.admin.common.configuration.model.election.Election(
				electionGroup.electionPath());
		election.setId("35");
		election.setName("Election 1");
		election.setCandidateRankVoteShareThreshold(BigDecimal.ZERO);
		election.setSettlementFirstDivisor(BigDecimal.valueOf(1.4));
		election.setLevelingSeatsVoteShareThreshold(BigDecimal.ZERO);

		election.setEndDateOfBirth(LocalDate.now().minusYears(18));

		election.setGenericElectionType(GenericElectionType.R);

		Set<ConstraintViolation<no.valg.eva.admin.common.configuration.model.election.Election>> constraintViolations = validator.validate(election);

		Assert.assertEquals(constraintViolations.size(), 0);

		return election;
	}

	protected Contest buildContest(final Election election) {
		Contest contest = new Contest();
		contest.setId("999999");
		contest.setName("Contest 1");
		contest.setElection(election);

		Set<ConstraintViolation<Contest>> constraintViolations = validator.validate(contest);
		Assert.assertEquals(constraintViolations.size(), 0);

		return contest;
	}

	protected ElectionEvent createElectionEvent(final ElectionEvent electionEvent) {
		ElectionEvent localElectionEvent = electionEventRepository.create(rbacTestFixture.getSysAdminUserData(), electionEvent);

		Assert.assertNotNull(localElectionEvent);
		Assert.assertNotNull(localElectionEvent.getPk());
		Assert.assertTrue(localElectionEvent.getPk() > 0);
		return localElectionEvent;
	}

	protected void createElectionGroup(final ElectionGroup electionGroup) {
		electionGroupRepository.create(rbacTestFixture.getUserData(), electionGroup);

		Assert.assertNotNull(electionGroup);
		Assert.assertNotNull(electionGroup.getPk());
		Assert.assertTrue(electionGroup.getPk() > 0);
	}

	protected Election createElection(final Election election) {
		electionRepository.create(rbacTestFixture.getUserData(), election);

		Assert.assertNotNull(election);
		Assert.assertNotNull(election.getPk());
		Assert.assertTrue(election.getPk() > 0);
		return election;
	}

	protected Contest createContest(final Contest contest) {
		contestRepository.create(rbacTestFixture.getUserData(), contest);

		Assert.assertNotNull(contest);
		Assert.assertNotNull(contest.getPk());
		Assert.assertTrue(contest.getPk() > 0);
		return contest;
	}

	protected Validator getValidator() {
		return validator;
	}

	protected ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	protected void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	protected ElectionDay getElectionDay() {
		return electionDay;
	}

	protected void setElectionDay(final ElectionDay electionDay) {
		this.electionDay = electionDay;
	}

	protected ElectionGroup getElectionGroup() {
		return electionGroup;
	}

	protected void setElectionGroup(final ElectionGroup electionGroup) {
		this.electionGroup = electionGroup;
	}

	protected Election getElection() {
		return election;
	}

	protected void setElection(final Election election) {
		this.election = election;
	}

	protected Contest getContest() {
		return contest;
	}

	protected void setContest(final Contest contest) {
		this.contest = contest;
	}

	protected ElectionGroupRepository getElectionGroupRepository() {
		return electionGroupRepository;
	}

	protected ElectionGroupServiceBean getElectionGroupServiceBean() {
		return electionGroupServiceBean;
	}

	protected ElectionServiceBean getElectionServiceBean() {
		return electionServiceBean;
	}

	protected ElectionRepository getElectionRepository() {
		return electionRepository;
	}

	protected ContestServiceBean getContestService() {
		return contestService;
	}

	protected MvAreaRepository getMvAreaRepository() {
		return mvAreaRepository;
	}

	protected MvElectionRepository getMvElectionRepository() {
		return mvElectionRepository;
	}

	protected ElectionEventRepository getElectionEventRepository() {
		return electionEventRepository;
	}

	protected ElectionGroupService getElectionGroupService() {
		return electionGroupService;
	}
}

