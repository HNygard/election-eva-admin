package no.evote.service.configuration;

import static no.evote.service.backendmock.ListProposalTestFixture.TEST_POSTAL_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Event;

import no.evote.dto.ListProposalValidationData;
import no.evote.model.views.CandidateAudit;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ListProposalTestFixture;
import no.evote.service.backendmock.RBACTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ProposerRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.SLOW, TestGroups.REPOSITORY })
public class CandidateServiceBeanTest extends AbstractJpaTestBase {
	private static final String TLF = "Tlf";
	private static final String FIRST_NAME = "Firstname";
	private static final String MIDDLE_NAME = "Middlename";
	private static final String LAST_NAME = "Lastname";
	private static final String POSITION = "1";

	private CandidateServiceBean candidateService;
	private CandidateRepository candidateRepository;
	private ProposerServiceBean proposerService;
	private ProposerRepository proposerRepository;
	private RBACTestFixture rbacTestFixture;
	private ElectionEventRepository electionEventRepository;
	private ListProposalTestFixture listProposalTestFixture;
	private MvElectionRepository mvElectionRepository;
	private BallotRepository ballotRepository;

	@BeforeMethod(alwaysRun = true)
	public void initDependencies() throws Exception {
		BackendContainer backend = new BackendContainer(getEntityManager(), mock(Event.class));
		backend.initServices();

		candidateService = backend.getCandidateService();
		candidateRepository = backend.getCandidateRepository();
		proposerService = backend.getProposerServiceBean();
		proposerRepository = backend.getProposerRepository();
		electionEventRepository = backend.getElectionEventRepository();
		mvElectionRepository = backend.getMvElectionRepository();
		ballotRepository = backend.getBallotRepository();

		rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
		rbacTestFixture.init();
		listProposalTestFixture = new ListProposalTestFixture(backend);
		listProposalTestFixture.init();

	}

	@Test
	public void testCreateNewCandidate() {
		Candidate newCandidate = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		Assert.assertEquals(newCandidate.getFirstName(), "");
		Assert.assertEquals(newCandidate.getLastName(), "");
		Assert.assertEquals(newCandidate.getNameLine(), "");
		Assert.assertTrue(newCandidate.getMaritalStatus().getPk().equals(1L));

		Assert.assertEquals(newCandidate.isBaselineVotes(), false);
		Assert.assertEquals(newCandidate.isApproved(), false);
		Assert.assertEquals(newCandidate.getId(), "");
		Assert.assertEquals(newCandidate.isApproved(), false);
		Assert.assertEquals(newCandidate.getDisplayOrder(), 0);
		Assert.assertTrue(newCandidate.getAffiliation().getPk().equals(listProposalTestFixture.getTestAffiliation().getPk()));
		Assert.assertTrue(newCandidate.getAffiliation().getBallot().getPk().equals(listProposalTestFixture.getTestAffiliation().getBallot().getPk()));
		Assert.assertNull(newCandidate.getPk());
	}

	@Test
	public void testSwapDisplayOrder() {
		Candidate candidateOver = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidateOver.setDisplayOrder(1);
		candidateOver.setId("11111111111");
		Candidate candidateUnder = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidateUnder.setDisplayOrder(2);
		candidateUnder.setId("22222222222");

		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidateUnder);
		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidateOver);

		candidateService.swapDisplayOrder(rbacTestFixture.getUserData(), candidateOver, candidateUnder);

		candidateUnder = candidateRepository.findCandidateByPk(candidateUnder.getPk());
		candidateOver = candidateRepository.findCandidateByPk(candidateOver.getPk());

		Assert.assertEquals(1, candidateUnder.getDisplayOrder());
		Assert.assertEquals(2, candidateOver.getDisplayOrder());

		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidateOver.getPk());
		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidateUnder.getPk());

	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testConvertRowsToCandidateList_shouldFail() {
		List<String[]> rowCandidates1 = new ArrayList<>();

		String[] rowCandidate = { POSITION, FIRST_NAME, MIDDLE_NAME, LAST_NAME, "01.01.1990", "Residence", "Addr", "597", "PostTown", TLF, "E-mail", "Comment",
				"Profession", "Ok" };
		rowCandidates1.add(rowCandidate);

		List<Candidate> candidates =
				candidateService.convertRowsToCandidateList(rowCandidates1, listProposalTestFixture.getTestAffiliation());
		Assert.assertEquals(candidates.get(0).getFirstName(), FIRST_NAME);
		Assert.assertEquals(candidates.get(0).getMiddleName(), MIDDLE_NAME);
		Assert.assertEquals(candidates.get(0).getLastName(), LAST_NAME);
		Assert.assertEquals(candidates.get(0).getDateOfBirth(), new LocalDate(1990, 1, 1));

		// Test if all columns are set (even "")
		String[] rowCandidate2 = { "Lastname2", "Firstname2" };
		rowCandidates1.add(rowCandidate2);

		candidateService.convertRowsToCandidateList(rowCandidates1, listProposalTestFixture.getTestAffiliation());
	}

	@Test
	public void testConvertRowsToCandidateList_1() {
		candidateService.convertRowsToCandidateList(null, listProposalTestFixture.getTestAffiliation());
	}

	@Test
	public void testConvertRowsToCandidateList_2() {
		List<String[]> rowCandidates;
		rowCandidates = new ArrayList<>();
		candidateService.convertRowsToCandidateList(rowCandidates, listProposalTestFixture.getTestAffiliation());
	}

	@Test
	public void testCreateAllBelow() {
		Candidate candidate = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidate.setDisplayOrder(1);
		candidate.setId("11111111111");
		Candidate candidate2 = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidate2.setDisplayOrder(90);
		candidate2.setId("22222222222");

		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidate);

		List<Candidate> importedCandidates = new ArrayList<>();
		importedCandidates.add(candidate2);

		candidateService.createAllBelow(rbacTestFixture.getUserData(), importedCandidates, listProposalTestFixture.getTestAffiliation().getPk(),
				listProposalTestFixture.getTestBallotPk());

		List<Candidate> candidateList = candidateRepository.findByAffiliation(listProposalTestFixture.getTestAffiliation().getPk());

		Assert.assertEquals(1, candidateList.get(0).getDisplayOrder());
		Assert.assertEquals(2, candidateList.get(1).getDisplayOrder());

		candidateRepository.deleteAllCandidates(rbacTestFixture.getUserData(), candidateList);
	}

	@Test
	public void testSearchVoter() {

		Candidate candidate = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidate.setFirstName(listProposalTestFixture.getVoterFirstname());
		candidate.setLastName(listProposalTestFixture.getVoterLastName());
		candidate.setNameLine(candidate.getLastName() + " " + candidate.getLastName());

		Assert.assertTrue(candidateService
				.searchVoter(candidate, listProposalTestFixture.getElectionEvent().getId(),
						listProposalTestFixture.getMvAreasSet()).get(0).getId()
				.equals(listProposalTestFixture.getVoterId()));

		candidate.setAddressLine1("Wrong address");
		Assert.assertTrue(candidateService
				.searchVoter(candidate, listProposalTestFixture.getElectionEvent().getId(),
						listProposalTestFixture.getMvAreasSet()).get(0).getId()
				.equals(listProposalTestFixture.getVoterId()));

		candidate = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidate.setFirstName(listProposalTestFixture.getVoterFirstname() + "aaa");
		candidate.setLastName(listProposalTestFixture.getVoterLastName());
		candidate.setAddressLine1("");
		Assert.assertTrue(candidateService.searchVoter(candidate, listProposalTestFixture.getElectionEvent().getId(),
				listProposalTestFixture.getMvAreasSet()).isEmpty());
	}

	@Test
	public void searchVoter_withCountyMvAreasFilter_returnsNonEmptyVoterList() throws Exception {

		ElectionEvent electionEvent = electionEventRepository.findByPk(2L);

		Candidate candidate = new Candidate();
		candidate.setLastName("Pettersen");
		String electionEventId = electionEvent.getId();
		Set<MvArea> mvAreas = new HashSet<>(Arrays.asList(getMvArea(electionEventId + ".47.01")));

		List<Voter> result = candidateService.searchVoter(candidate, electionEventId, mvAreas);
		assertThat(result).hasSize(11);
	}

	@Test
	public void searchVoter_withMunicipalityMvAreasFilter_returnsNonEmptyVoterList() throws Exception {

		ElectionEvent electionEvent = electionEventRepository.findByPk(2L);

		Candidate candidate = new Candidate();
		candidate.setLastName("Pettersen");
		String electionEventId = electionEvent.getId();
		Set<MvArea> mvAreas = new HashSet<>(Arrays.asList(getMvArea(electionEventId + ".47.01.0101")));

		List<Voter> result = candidateService.searchVoter(candidate, electionEventId, mvAreas);
		assertThat(result).hasSize(11);
	}

	@Test
	public void searchVoter_withBoroughMvAreasFilter_returnsNonEmptyVoterList() throws Exception {

		ElectionEvent electionEvent = electionEventRepository.findByPk(2L);

		Candidate candidate = new Candidate();
		candidate.setLastName("Pettersen");
		String electionEventId = electionEvent.getId();
		Set<MvArea> mvAreas = new HashSet<>(Arrays.asList(getMvArea(electionEventId + ".47.01.0101.010100")));

		List<Voter> result = candidateService.searchVoter(candidate, electionEventId, mvAreas);
		assertThat(result).hasSize(11);
	}

	private MvArea getMvArea(String path) {
		MvArea mvArea = new MvArea();
		AreaPath areaPath = AreaPath.from(path);
		mvArea.setElectionEventId(areaPath.getElectionEventId());
		mvArea.setCountryId(areaPath.getCountryId());
		mvArea.setCountyId(areaPath.getCountyId());
		if (areaPath.getMunicipalityId() != null) {
			mvArea.setMunicipalityId(areaPath.getMunicipalityId());
		}
		if (areaPath.getBoroughId() != null) {
			mvArea.setBoroughId(areaPath.getBoroughId());
		}
		mvArea.setAreaLevel(areaPath.getLevel().getLevel());
		return mvArea;
	}

	@Test
	public void testCreateUpdateAndDeleteCandidate() {
		Candidate candidate = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidate.setFirstName(listProposalTestFixture.getVoterFirstname());
		candidate.setLastName(listProposalTestFixture.getVoterLastName());
		candidate.setDisplayOrder(1);
		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidate);
		Assert.assertNotNull(candidate.getPk());
		candidate.setId("12345678900");
		Candidate dbCandidate = candidateRepository.updateCandidate(rbacTestFixture.getUserData(), candidate);
		Assert.assertEquals(candidate.getId(), dbCandidate.getId());
		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), dbCandidate.getPk());
	}

	public void testFindByAffiliation() {
		Candidate candidate = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidate.setFirstName(listProposalTestFixture.getVoterFirstname());
		candidate.setLastName(listProposalTestFixture.getVoterLastName());
		candidate.setNameLine(candidate.getLastName() + " " + candidate.getLastName());
		candidate.setDisplayOrder(1);
		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidate);

		Candidate dbCandidate = candidateRepository.findByAffiliation(listProposalTestFixture.getTestAffiliation().getPk()).get(0);

		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), dbCandidate.getPk());
	}

	@Test
	public void testValidateCandidates() {
		Candidate candidateEmpty = createEmptyCandidate();

		Candidate candidateWrongData = createCAndidateWrongData();

		Candidate candidateWrongDisplayOrder = new Candidate();
		candidateWrongDisplayOrder.setId(listProposalTestFixture.getVoterId2());
		candidateWrongDisplayOrder.setDisplayOrder(100);
		candidateWrongDisplayOrder.setFirstName(FIRST_NAME);
		candidateWrongDisplayOrder.setLastName(LAST_NAME);
		candidateWrongDisplayOrder.setAddressLine1("AddressLine1");
		candidateWrongDisplayOrder.setPostalCode(TEST_POSTAL_CODE);
		candidateWrongDisplayOrder.setBaselineVotes(true);
		candidateWrongDisplayOrder.setEmail("email@email.com");
		candidateWrongDisplayOrder.setTelephoneNumber("123456");
		candidateWrongDisplayOrder.setDateOfBirth(new LocalDate(1980, 1, 1));
		candidateWrongDisplayOrder.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());

		Candidate candidateCorrectData = new Candidate();
		candidateCorrectData.setDisplayOrder(1);
		candidateCorrectData.setId(listProposalTestFixture.getVoterId2());
		candidateCorrectData.setFirstName(FIRST_NAME);
		candidateCorrectData.setLastName(LAST_NAME);
		candidateCorrectData.setAddressLine1("AddressLine1");
		candidateCorrectData.setPostalCode(TEST_POSTAL_CODE);
		candidateCorrectData.setEmail("email@email.com");
		candidateCorrectData.setTelephoneNumber("123456");
		candidateCorrectData.setDateOfBirth(new LocalDate(1960, 1, 10));
		candidateCorrectData.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());

		Candidate candidateCorrectDataDuplicate = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidateCorrectDataDuplicate.setDisplayOrder(1);
		candidateCorrectDataDuplicate.setId(listProposalTestFixture.getVoterId2());
		candidateCorrectDataDuplicate.setFirstName(FIRST_NAME);
		candidateCorrectDataDuplicate.setLastName(LAST_NAME);
		candidateCorrectDataDuplicate.setDateOfBirth(new LocalDate(1980, 1, 1));
		candidateCorrectDataDuplicate.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());

		List<Candidate> candidates = new ArrayList<>();
		candidates.add(candidateEmpty);
		candidates.add(candidateWrongData);
		candidates.add(candidateWrongDisplayOrder);
		candidates.add(candidateCorrectData);

		Assert.assertTrue(candidateService.validate(candidateEmpty, listProposalTestFixture.getTestBallotPk()).isInvalid());
		Assert.assertTrue(candidateService.validate(candidateWrongData, listProposalTestFixture.getTestBallotPk()).isInvalid());
		Assert.assertTrue(candidateService.validate(candidateWrongDisplayOrder, listProposalTestFixture.getTestBallotPk()).isInvalid());
		Assert.assertFalse(candidateService.validate(candidateCorrectData, listProposalTestFixture.getTestBallotPk()).isInvalid());
		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidateCorrectDataDuplicate);
		Assert.assertTrue(candidateService.validate(candidateCorrectData, listProposalTestFixture.getTestBallotPk()).isInvalid());

		ListProposalValidationData validationData = candidateService.isCandidatesValid(candidates,
				listProposalTestFixture.getTestBallotPk(), 2);

		Assert.assertFalse(validationData.isApproved());
		Assert.assertFalse(candidateWrongData.isBaselineVotes());
		Assert.assertFalse(candidateWrongDisplayOrder.isBaselineVotes());

		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidateCorrectDataDuplicate.getPk());

	}

	@Test
	public void testApproveCandidates() {
		Candidate candidateInOtherList = createCandidate(listProposalTestFixture.getVoterId());
		candidateInOtherList.setAffiliation(listProposalTestFixture.getTestAffiliation2());

		Proposer proposerInOtherList = proposerService.createNewProposer(listProposalTestFixture.getTestAffiliation()
				.getBallot());
		proposerInOtherList.setId(listProposalTestFixture.getVoterId2());

		Candidate candidateDuplicate = createCandidate(listProposalTestFixture.getVoterId());
		Candidate proposerDuplicate = createCandidate(listProposalTestFixture.getVoterId2());

		List<Candidate> candidateList = new ArrayList<>();
		candidateList.add(candidateDuplicate);
		candidateList.add(proposerDuplicate);

		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidateInOtherList);
		proposerRepository.createProposer(rbacTestFixture.getUserData(), proposerInOtherList);

		ListProposalValidationData validationData = new ListProposalValidationData(candidateList, null, listProposalTestFixture.getTestAffiliation());

		Assert.assertFalse(candidateService.approveCandidates(rbacTestFixture.getUserData(), validationData,
				listProposalTestFixture.getElectionEvent().getId(), listProposalTestFixture.getMvAreasSet(), listProposalTestFixture.getTestAffiliation())
				.isApproved());

		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidateInOtherList.getPk());
		proposerRepository.deleteProposer(rbacTestFixture.getUserData(), proposerInOtherList.getPk());

		Assert.assertEquals(candidateDuplicate.isInvalid(), true);
		Assert.assertEquals(candidateDuplicate.getValidationMessageList().size(), 1);
		Assert.assertEquals(proposerDuplicate.isInvalid(), false);
		Assert.assertEquals(proposerDuplicate.getValidationMessageList().size(), 0);
	}

	@Test
	public void testNotApproveCandidateInDifferentMunThanAllowed() {
		Candidate candidate = createCandidate(listProposalTestFixture.getVoterMun3Id());
		candidate.setAffiliation(listProposalTestFixture.getTestAffiliation());

		List<Candidate> candidateList = new ArrayList<>();
		candidateList.add(candidate);

		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidate);

		Set<MvArea> newMvAreasSet = new HashSet<>();
		MvArea newMvArea = listProposalTestFixture.getNewMvArea();
		newMvArea.setMunicipality(listProposalTestFixture.getTestMunicipality2());
		newMvArea.setMunicipalityId(listProposalTestFixture.getTestMunicipality2().getId());

		newMvAreasSet.add(newMvArea);

		ListProposalValidationData validationData = new ListProposalValidationData(candidateList, null, listProposalTestFixture.getTestAffiliation());

		Assert.assertFalse(candidateService.approveCandidates(rbacTestFixture.getUserData(), validationData,
				listProposalTestFixture.getElectionEvent().getId(), newMvAreasSet, listProposalTestFixture.getTestAffiliation())
				.isApproved());

		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidate.getPk());

		Assert.assertEquals(candidate.isInvalid(), true);
		Assert.assertEquals(candidate.getValidationMessageList().size(), 1);
	}

	@Test
	public void testApproveCandidatesWhenMultipleAreaRestrictions() {
		Candidate candidate1 = createCandidate(listProposalTestFixture.getVoterId());
		candidate1.setAffiliation(listProposalTestFixture.getTestAffiliation());

		Candidate candidate2 = createCandidate(listProposalTestFixture.getVoterMun2Id());
		candidate2.setDisplayOrder(98);
		candidate2.setAffiliation(listProposalTestFixture.getTestAffiliation());

		Candidate candidate3 = createCandidate(listProposalTestFixture.getVoterMun3Id());
		candidate3.setDisplayOrder(97);
		candidate3.setAffiliation(listProposalTestFixture.getTestAffiliation());

		List<Candidate> candidateList = new ArrayList<>();
		candidateList.add(candidate1);
		candidateList.add(candidate2);
		candidateList.add(candidate3);

		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidate1);
		listProposalTestFixture.updateTestAffiliation();
		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidate2);
		listProposalTestFixture.updateTestAffiliation();
		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidate3);

		Set<MvArea> newMvAreasSet = new HashSet<>();
		MvArea newMvArea1 = listProposalTestFixture.getNewMvArea();
		newMvArea1.setPk(1L);
		newMvArea1.setMunicipality(listProposalTestFixture.getTestMunicipality2());
		newMvArea1.setMunicipalityId(listProposalTestFixture.getTestMunicipality2().getId());
		MvArea newMvArea2 = listProposalTestFixture.getNewMvArea();
		newMvArea2.setPk(2L);
		newMvArea2.setMunicipality(listProposalTestFixture.getTestMunicipality3());
		newMvArea2.setMunicipalityId(listProposalTestFixture.getTestMunicipality3().getId());

		newMvAreasSet.add(newMvArea1);
		newMvAreasSet.add(newMvArea2);

		ListProposalValidationData validationData = new ListProposalValidationData(candidateList, null, listProposalTestFixture.getTestAffiliation());
		Assert.assertFalse(candidateService.approveCandidates(rbacTestFixture.getUserData(), validationData,
				listProposalTestFixture.getElectionEvent().getId(), newMvAreasSet, listProposalTestFixture.getTestAffiliation())
				.isApproved());

		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidate1.getPk());
		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidate2.getPk());
		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidate3.getPk());

		Assert.assertEquals(candidate1.isInvalid(), true);
		Assert.assertEquals(candidate1.getValidationMessageList().size(), 1);
		Assert.assertEquals(candidate2.isInvalid(), false);
		Assert.assertEquals(candidate2.getValidationMessageList().size(), 0);
		Assert.assertEquals(candidate3.isInvalid(), false);
		Assert.assertEquals(candidate3.getValidationMessageList().size(), 0);
	}

	@Test
	public void testGetIdFromRollOnApprove() {
		Candidate candidate = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidate.setFirstName(listProposalTestFixture.getVoterFirstname());
		candidate.setLastName(listProposalTestFixture.getVoterLastName());
		candidate.setNameLine(candidate.getLastName() + " " + candidate.getLastName());

		Map<String, Candidate> idList = new HashMap<>();
		candidateService.setMockIdForEmptyId(candidate, listProposalTestFixture.getTestBallotPk(), idList);

		List<Candidate> candidateList = new ArrayList<>();
		candidateList.add(candidate);

		ListProposalValidationData validationData = new ListProposalValidationData(candidateList, null, listProposalTestFixture.getTestAffiliation());
		Assert.assertTrue(candidateService.approveCandidates(rbacTestFixture.getUserData(), validationData, listProposalTestFixture.getElectionEvent().getId(),
				listProposalTestFixture.getMvAreasSet(), listProposalTestFixture.getTestAffiliation())
				.isApproved());

		Assert.assertTrue(candidate.isIdSet());
	}

	@Test
	public void testApproveCandidatesDuplicate() {
		Candidate candidate = createCandidate(listProposalTestFixture.getVoterId());
		candidate.setAffiliation(listProposalTestFixture.getTestAffiliation2());

		Candidate candidateDuplicate = createCandidate(listProposalTestFixture.getVoterId());
		candidateDuplicate.setAffiliation(listProposalTestFixture.getTestAffiliation2());

		List<Candidate> candidateList = new ArrayList<>();
		candidateList.add(candidate);
		candidateList.add(candidateDuplicate);

		ListProposalValidationData validationData = new ListProposalValidationData(candidateList, null, listProposalTestFixture.getTestAffiliation());

		candidateService.approveCandidates(rbacTestFixture.getUserData(), validationData, listProposalTestFixture.getElectionEvent().getId(),
				listProposalTestFixture.getMvAreasSet(), listProposalTestFixture.getTestAffiliation());

		Assert.assertEquals(candidate.isInvalid(), false);
		Assert.assertEquals(candidateDuplicate.isInvalid(), true);
		Assert.assertEquals(candidateDuplicate.getValidationMessageList().size(), 1);
		Assert.assertNotSame(candidateDuplicate.getId(), candidate.getId());
	}

	@Test
	public void testFindByIdInOtherAffiliation() {
		Candidate candidateInOtherList = createCandidate(listProposalTestFixture.getVoterId());
		candidateInOtherList.setAffiliation(listProposalTestFixture.getTestAffiliation2());

		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidateInOtherList);
		List<Candidate> candidatesFromOtherListProposal = candidateRepository.findByIdInOtherBallot(
				listProposalTestFixture.getVoterId(),
				listProposalTestFixture.getTestAffiliation().getBallot().getPk(),
				listProposalTestFixture.getElection().getPk());
		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidateInOtherList.getPk());

		Assert.assertEquals(listProposalTestFixture.getVoterId(), candidatesFromOtherListProposal.get(0).getId());
		Assert.assertFalse(listProposalTestFixture.getTestAffiliation().getPk().equals(candidatesFromOtherListProposal.get(0).getAffiliation().getPk()));
		Assert.assertTrue(listProposalTestFixture.getTestAffiliation2().getPk().equals(candidatesFromOtherListProposal.get(0).getAffiliation().getPk()));

	}

	@Test
	public void testConvertVoterToCandidate() {
		Candidate candidate = new Candidate();
		Voter voter = new Voter();
		voter.setFirstName("f");
		voter.setLastName("l");
		voter.setId("1");
		voter.setDateOfBirth(new LocalDate(1980, 1, 1));
		voter.setMunicipalityId(listProposalTestFixture.getTestMunicipality().getId());
		voter.setAddressLine1("a");
		voter.setPostalCode(TEST_POSTAL_CODE);
		voter.setPostTown("t");
		voter.setEmail("e");
		voter.setTelephoneNumber("11");
		voter.setElectionEvent(listProposalTestFixture.getElectionEvent());
		candidateService.convertVoterToCandidate(candidate, voter);
		Assert.assertEquals(candidate.getFirstName(), voter.getFirstName());
		Assert.assertEquals(candidate.getLastName(), voter.getLastName());
		Assert.assertEquals(candidate.getId(), voter.getId());
		Assert.assertEquals(candidate.getDateOfBirth(), voter.getDateOfBirth());

	}

	@Test
	public void getCandiateAuditByBallot() {
		List<CandidateAudit> candidateAuditList = candidateRepository.getCandidateAuditByBallot(listProposalTestFixture.getTestAffiliation().getBallot()
				.getPk());
		Assert.assertTrue(candidateAuditList.isEmpty());
	}

	@Test
	public void testSetMockIdForEmptyId() {
		Candidate candidate = new Candidate();
		candidate.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());
		candidate.setId("");
		candidate.setDateOfBirth(new LocalDate(1980, 10, 10));

		Map<String, Candidate> idList = new HashMap<>();
		candidateService.setMockIdForEmptyId(candidate, listProposalTestFixture.getTestBallotPk(), idList);

		Assert.assertEquals(candidate.getId(), "10108000000");

		Assert.assertTrue(idList.containsKey("10108000000"));
		Assert.assertEquals(idList.get("10108000000"), candidate);

	}

	@Test
	public void testSetMockIdForEmptyIdWithEmptyIdList() {
		Candidate candidate = new Candidate();
		candidate.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());
		candidate.setId("");
		candidate.setDateOfBirth(new LocalDate(1980, 10, 10));

		candidateService.setMockIdForEmptyId(candidate, listProposalTestFixture.getTestBallotPk(), null);

		Assert.assertEquals(candidate.getId(), "10108000000");
	}

	@Test
	public void testSetMockIdForDuplicates() {
		Candidate candidate1 = new Candidate();
		candidate1.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());
		candidate1.setId("");
		candidate1.setDateOfBirth(new LocalDate(1980, 10, 10));

		Candidate candidate2 = new Candidate();
		candidate2.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());
		candidate2.setId("");
		candidate2.setDateOfBirth(new LocalDate(1980, 10, 10));

		Map<String, Candidate> idList = new HashMap<>();
		candidateService.setMockIdForEmptyId(candidate1, listProposalTestFixture.getTestBallotPk(), idList);

		Assert.assertEquals(candidate1.getId(), "10108000000");

		candidateService.setMockIdForEmptyId(candidate2, listProposalTestFixture.getTestBallotPk(), idList);
		Assert.assertEquals(candidate2.getId(), "10108000001");
	}

	@Test
	public void testCandidateInformationString() {
		Candidate candidateEmpty = new Candidate();
		Assert.assertEquals(candidateEmpty.getInformationString(true, true), StringUtils.EMPTY);
		Candidate candidateInfo = createCandidate("1");
		Map<String, Candidate> idList = new HashMap<>();
		candidateService.setMockIdForEmptyId(candidateInfo, listProposalTestFixture.getTestBallotPk(), idList);
		candidateInfo.setResidence("Residencse");
		candidateInfo.setAddressLine1("Adr");
		candidateInfo.setTelephoneNumber(TLF);
		candidateInfo.setEmail("email");
		candidateInfo.setPostalCode("0000");
		candidateInfo.setPostTown("Town");
		candidateInfo.setProfession("Proffesion");
		candidateInfo.setInfoText("Info");
		Assert.assertEquals(candidateInfo.getInformationString(true, true), "1 1, 1980, Proffesion, Residencse");
		Assert.assertEquals(candidateInfo.getInformationString(false, false), "1 1, 1980");
		Assert.assertEquals(candidateInfo.getInformationString(true, false), "1 1, 1980, Proffesion");
		Assert.assertEquals(candidateInfo.getInformationString(false, true), "1 1, 1980, Residencse");

	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "changeDisplayOrder displayOrderFrom\\[1\\] equal to displayOrderTo\\[1\\]")
	public void changeDisplayOrder_withEqualFromTo_throwsException() throws Exception {

		Candidate candidateMock = mock(Candidate.class, RETURNS_DEEP_STUBS);
		when(candidateMock.getBallot().getPk()).thenReturn(5L);

		candidateService.changeDisplayOrder(candidateMock, 1, 1);
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "changeDisplayOrder from/to \\[1/1000\\] does not match actual size 37")
	public void changeDisplayOrder_withOutOfBoundsFromTo_throwsException() throws Exception {
		Candidate candidateMock = mock(Candidate.class, RETURNS_DEEP_STUBS);
		when(candidateMock.getBallot().getPk()).thenReturn(ballotPkForContest1AndBallotSp());

		candidateService.changeDisplayOrder(candidateMock, 1, 1000);
	}

	@Test
	public void changeDisplayOrder_withLowFromAndHighTo_returnsResult() throws Exception {

		Candidate candidateMock = mock(Candidate.class, RETURNS_DEEP_STUBS);
		when(candidateMock.getBallot().getPk()).thenReturn(ballotPkForContest1AndBallotSp());

		List<Candidate> result = candidateService.changeDisplayOrder(candidateMock, 1, 5);

		assertThat(result.size()).isEqualTo(5);
		assertThat(result.get(0).getFirstName()).isEqualTo("Roar");
		assertThat(result.get(0).getDisplayOrder()).isEqualTo(1);
		assertThat(result.get(1).getFirstName()).isEqualTo("Anne-Kari");
		assertThat(result.get(1).getDisplayOrder()).isEqualTo(2);
		assertThat(result.get(2).getFirstName()).isEqualTo("Borghild-Johanne");
		assertThat(result.get(2).getDisplayOrder()).isEqualTo(3);
		assertThat(result.get(3).getFirstName()).isEqualTo("Hans");
		assertThat(result.get(3).getDisplayOrder()).isEqualTo(4);
		assertThat(result.get(4).getFirstName()).isEqualTo("Per Inge");
		assertThat(result.get(4).getDisplayOrder()).isEqualTo(5);
	}

	@Test
	public void changeDisplayOrder_withHighFromAndLowTo_returnsResult() throws Exception {

		Candidate candidateMock = mock(Candidate.class, RETURNS_DEEP_STUBS);
		when(candidateMock.getBallot().getPk()).thenReturn(ballotPkForContest1AndBallotSp());

		List<Candidate> result = candidateService.changeDisplayOrder(candidateMock, 5, 3);

		assertThat(result.size()).isEqualTo(3);
		assertThat(result.get(0).getFirstName()).isEqualTo("Hans");
		assertThat(result.get(0).getDisplayOrder()).isEqualTo(3);
		assertThat(result.get(1).getFirstName()).isEqualTo("Anne-Kari");
		assertThat(result.get(1).getDisplayOrder()).isEqualTo(4);
		assertThat(result.get(2).getFirstName()).isEqualTo("Borghild-Johanne");
		assertThat(result.get(2).getDisplayOrder()).isEqualTo(5);
	}

	private long ballotPkForContest1AndBallotSp() {
		Long contestPk = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("200701", "01", "01", "000001")).getContest().getPk();
		return ballotRepository.findPkByContestAndId(contestPk, "SP");
	}

	private Candidate createCandidate(final String id) {
		Candidate candidate1 = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation());
		candidate1.setId(id);
		candidate1.setFirstName(id);
		candidate1.setLastName(id);
		candidate1.setDateOfBirth(new LocalDate(1980, 10, 10));
		candidate1.setDisplayOrder(99);
		return candidate1;
	}

	private Candidate createEmptyCandidate() {
		Candidate candidateEmpty = new Candidate();
		candidateEmpty.setId("not_unique");
		candidateEmpty.setFirstName("");
		candidateEmpty.setLastName("");
		candidateEmpty.setDateOfBirth(null);
		candidateEmpty.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());
		return candidateEmpty;
	}

	private Candidate createCAndidateWrongData() {
		Candidate candidateWrongData = new Candidate();
		candidateWrongData.setId("not_unique");
		candidateWrongData.setFirstName("1");
		candidateWrongData.setLastName("2");
		candidateWrongData.setAddressLine1("1");
		candidateWrongData.setPostalCode("12345");
		candidateWrongData.setEmail("1");
		candidateWrongData.setTelephoneNumber(TLF);
		candidateWrongData.setDateOfBirth(new LocalDate(3011, 1, 1));
		candidateWrongData.setBaselineVotes(true);
		candidateWrongData.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());
		return candidateWrongData;
	}

	@Test
	public void convertVoterShouldAlsoConvertMiddleName() {
		Candidate candidate = new Candidate();
		Voter voter = new Voter();
		voter.setFirstName(FIRST_NAME);
		voter.setMiddleName(MIDDLE_NAME);
		voter.setLastName(LAST_NAME);
		Candidate result = candidateService.convertVoterToCandidate(candidate, voter);
		assertEquals(result.getFirstName(), FIRST_NAME);
		assertEquals(result.getMiddleName(), MIDDLE_NAME);
		assertEquals(result.getLastName(), LAST_NAME);
	}

	@Test
	public void convertProposalPersonShouldAlsoConvertMiddleName() {
		Candidate candidate = new Candidate();
		candidate.setFirstName(FIRST_NAME);
		candidate.setMiddleName(MIDDLE_NAME);
		candidate.setLastName(LAST_NAME);
		Voter voter = candidateService.convertCandidateToNewVoter(candidate, null);
		assertEquals(voter.getFirstName(), FIRST_NAME);
		assertEquals(voter.getMiddleName(), MIDDLE_NAME);
		assertEquals(voter.getLastName(), LAST_NAME);
		assertEquals(voter.getNameLine(), FIRST_NAME + " " + MIDDLE_NAME + " " + LAST_NAME);
	}
}

