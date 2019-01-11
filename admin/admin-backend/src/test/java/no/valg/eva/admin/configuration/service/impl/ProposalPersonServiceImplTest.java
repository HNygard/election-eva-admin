package no.valg.eva.admin.configuration.service.impl;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;

import no.evote.dto.ListProposalValidationData;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ListProposalTestFixture;
import no.evote.service.backendmock.RBACTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.evote.service.configuration.CandidateServiceBean;
import no.evote.service.configuration.ProposerServiceBean;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.ProposerRole;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.ProposerRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = { TestGroups.SLOW, TestGroups.REPOSITORY })
public class ProposalPersonServiceImplTest extends AbstractJpaTestBase {
	private ProposerServiceBean proposerService;
	private ProposerRepository proposerRepository;
	private CandidateServiceBean candidateService;
	private CandidateRepository candidateRepository;
	private RBACTestFixture rbacTestFixture;
	private ListProposalTestFixture listProposalTestFixture;

	@BeforeMethod(alwaysRun = true)
	public void init() throws Exception {
		BackendContainer backend = new BackendContainer(getEntityManager(), mock(Event.class));
		backend.initServices();

		candidateService = backend.getCandidateService();
		candidateRepository = backend.getCandidateRepository();
		proposerService = backend.getProposerServiceBean();
		proposerRepository = backend.getProposerRepository();

		rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
		rbacTestFixture.init();
		listProposalTestFixture = new ListProposalTestFixture(backend);
		listProposalTestFixture.init();
	}

	@Test
	public void testCreateNewProposer() {
		Proposer newProposer = proposerService.createNewProposer(listProposalTestFixture.getTestAffiliation().getBallot());
		assertEquals(newProposer.getFirstName(), "");
		assertEquals(newProposer.getLastName(), "");
		assertEquals(newProposer.getNameLine(), "");

		assertEquals(newProposer.isApproved(), false);
		assertEquals(newProposer.getId(), "");
		assertEquals(newProposer.isApproved(), false);
		assertEquals(newProposer.getDisplayOrder(), 0);
		assertTrue(newProposer.getBallot().getPk().equals(listProposalTestFixture.getTestAffiliation().getBallot().getPk()));
		assertNull(newProposer.getPk());
	}

	@Test
	public void testSearchVoter() {
		Proposer proposer = proposerService.createNewProposer(listProposalTestFixture.getTestAffiliation().getBallot());
		proposer.setFirstName(listProposalTestFixture.getVoterFirstname());
		proposer.setLastName(listProposalTestFixture.getVoterLastName());
		proposer.setNameLine(proposer.getFirstName() + " " + proposer.getLastName());
		assertTrue(proposerService.searchVoter(proposer, listProposalTestFixture.getElectionEvent().getId(),
						listProposalTestFixture.getMvAreasSet()).get(0).getId().equals(listProposalTestFixture.getVoterId()));

		proposer = proposerService.createNewProposer(listProposalTestFixture.getTestAffiliation().getBallot());
		proposer.setFirstName(listProposalTestFixture.getVoterFirstname() + "aaa");
		proposer.setLastName(listProposalTestFixture.getVoterLastName());
		proposer.setNameLine(proposer.getFirstName() + " " + proposer.getLastName());
		assertEquals(proposerService.searchVoter(proposer, listProposalTestFixture.getElectionEvent().getId(),
						listProposalTestFixture.getMvAreasSet()).size(), 0);
	}

	@Test
	public void testCreateUpdateAndDeleteProposer() {
		Proposer proposer = proposerService.createNewProposer(listProposalTestFixture.getTestAffiliation().getBallot());
		proposer.setFirstName(listProposalTestFixture.getVoterFirstname());
		proposer.setLastName(listProposalTestFixture.getVoterLastName());
		proposer.setNameLine(proposer.getLastName() + " " + proposer.getLastName());
		proposer.setDisplayOrder(1);
		proposerRepository.createProposer(rbacTestFixture.getUserData(), proposer);
		assertNotNull(proposer.getPk());
		proposer.setId("12345678900");
		Proposer dbProposer = proposerRepository.updateProposer(rbacTestFixture.getUserData(), proposer);
		assertEquals(proposer.getId(), dbProposer.getId());
		proposerRepository.deleteProposer(rbacTestFixture.getUserData(), dbProposer.getPk());
	}

	@Test
	public void testDeleteAndReorder() {
		Proposer proposer1 = createProposer("11111111111");
		proposer1.setDisplayOrder(1);
		proposerRepository.createProposer(rbacTestFixture.getUserData(), proposer1);
		assertNotNull(proposer1.getPk());
		Proposer proposer2 = createProposer("22222222222");
		proposer2.setDisplayOrder(2);
		proposerRepository.createProposer(rbacTestFixture.getUserData(), proposer2);
		assertNotNull(proposer2.getPk());
		Proposer proposer3 = createProposer("33333333333");
		proposer3.setDisplayOrder(3);
		proposerRepository.createProposer(rbacTestFixture.getUserData(), proposer3);
		assertNotNull(proposer3.getPk());

		proposerService.deleteAndReorder(rbacTestFixture.getUserData(), proposer2, listProposalTestFixture.getTestBallotPk());

		assertEquals(1, proposerRepository.findProposerByPk(proposer1.getPk()).getDisplayOrder());
		assertEquals(2, proposerRepository.findProposerByPk(proposer3.getPk()).getDisplayOrder());

		assertNull(proposerRepository.findProposerByPk(proposer2.getPk()));

		proposerRepository.deleteProposer(rbacTestFixture.getUserData(), proposer1.getPk());
		proposerRepository.deleteProposer(rbacTestFixture.getUserData(), proposer3.getPk());
	}

	@Test
	public void testCreateDefaultProposers() {
		proposerService.createDefaultProposers(rbacTestFixture.getUserData(), listProposalTestFixture.getTestAffiliation().getBallot());
		List<Proposer> proposerList = proposerRepository.findByBallot(listProposalTestFixture.getTestAffiliation().getBallot().getPk());

		assertEquals(2, proposerList.size());

		assertEquals("00000000001", proposerList.get(0).getId());
		assertEquals("00000000002", proposerList.get(1).getId());

		proposerRepository.deleteProposer(rbacTestFixture.getUserData(), proposerList.get(0).getPk());
		proposerRepository.deleteProposer(rbacTestFixture.getUserData(), proposerList.get(1).getPk());

	}

	@Test
	public void testIsOcrProposersDataSet() {
		List<Proposer> proposerList = new ArrayList<>();
		Proposer proposer = new Proposer();
		proposerList.add(proposer);

		// Tester at lite innfylt data ikke godkjennes
		ListProposalValidationData validationData = proposerService.isOcrProposersDataSet(rbacTestFixture.getUserData(),
			new ListProposalValidationData(null, proposerList, null));
		assertFalse(validationData.isApproved());

		// Tester at ikke utfylte navn ikke godkjennes
		proposerList.get(0).setDateOfBirth(new LocalDate(1980, 1, 20));
		validationData.setApproved(true);
		validationData = proposerService.isOcrProposersDataSet(rbacTestFixture.getUserData(), new ListProposalValidationData(null, proposerList, null));
		assertFalse(validationData.isApproved());

		// Tester med alt innfylt
		proposer.setFirstName("FirstName");
		proposer.setLastName("LastName");
		validationData = proposerService.isOcrProposersDataSet(rbacTestFixture.getUserData(), new ListProposalValidationData(null, proposerList, null));
		assertTrue(validationData.isApproved());
	}

	@Test
	public void testValidateProposers() {
		Proposer proposerEmpty = new Proposer();
		proposerEmpty.setId("not_unique");
		proposerEmpty.setFirstName("");
		proposerEmpty.setLastName("");
		proposerEmpty.setNameLine("Lastname Firstname");
		proposerEmpty.setDateOfBirth(null);
		proposerEmpty.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());

		Proposer proposerWrongData = new Proposer();
		proposerWrongData.setId("not_unique");
		proposerWrongData.setFirstName("1");
		proposerWrongData.setLastName("2");
		proposerWrongData.setNameLine("Lastname Firstname");
		proposerWrongData.setAddressLine1("1");
		proposerWrongData.setPostalCode("12345");
		proposerWrongData.setEmail("1");
		proposerWrongData.setTelephoneNumber("Tlf");
		proposerWrongData.setDateOfBirth(new LocalDate(3011, 1, 1));
		proposerWrongData.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());

		Proposer proposerCorrectData = new Proposer();
		proposerCorrectData.setId(listProposalTestFixture.getVoterId2());
		proposerCorrectData.setFirstName("Firstname");
		proposerCorrectData.setLastName("Lastname");
		proposerCorrectData.setNameLine("Lastname Firstname");
		proposerCorrectData.setAddressLine1("AddressLine1");
		proposerCorrectData.setPostalCode(ListProposalTestFixture.TEST_POSTAL_CODE);
		proposerCorrectData.setEmail("email@email.com");
		proposerCorrectData.setTelephoneNumber("123456");
		proposerCorrectData.setDateOfBirth(new LocalDate(1960, 1, 10));
		proposerCorrectData.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());

		assertTrue(proposerService.validate(rbacTestFixture.getUserData(), proposerEmpty, listProposalTestFixture.getTestBallotPk()).isInvalid());
		assertTrue(proposerService.validate(rbacTestFixture.getUserData(), proposerWrongData, listProposalTestFixture.getTestBallotPk()).isInvalid());
		assertFalse(proposerService.validate(rbacTestFixture.getUserData(), proposerCorrectData, listProposalTestFixture.getTestBallotPk()).isInvalid());

	}

	@Test
	public void testApproveCandidates() {
		Proposer proposerInOtherList = createProposer(listProposalTestFixture.getVoterId());
		proposerInOtherList.setBallot(listProposalTestFixture.getTestAffiliation2().getBallot());

		Candidate candidateInOtherList = candidateService.createNewCandidate(listProposalTestFixture.getTestAffiliation2());
		candidateInOtherList.setFirstName(listProposalTestFixture.getVoterId2());
		candidateInOtherList.setLastName(listProposalTestFixture.getVoterId2());
		candidateInOtherList.setId(listProposalTestFixture.getVoterId2());
		candidateInOtherList.setDisplayOrder(10);

		Proposer proposerDuplicate = createProposer(listProposalTestFixture.getVoterId());
		Proposer candidateDuplicate = createProposer(listProposalTestFixture.getVoterId2());

		List<Proposer> proposerList = new ArrayList<>();
		proposerList.add(proposerDuplicate);
		proposerList.add(candidateDuplicate);

		proposerRepository.createProposer(rbacTestFixture.getUserData(), proposerInOtherList);
		candidateRepository.createCandidate(rbacTestFixture.getUserData(), candidateInOtherList);

		ListProposalValidationData validationData = new ListProposalValidationData(null, proposerList, listProposalTestFixture.getTestAffiliation());
		assertFalse(proposerService.approveProposers(rbacTestFixture.getUserData(), validationData, listProposalTestFixture.getElectionEvent().getId(),
				listProposalTestFixture.getMvAreasSet(), listProposalTestFixture.getTestAffiliation()).isApproved());

		proposerRepository.deleteProposer(rbacTestFixture.getUserData(), proposerInOtherList.getPk());
		candidateRepository.deleteCandidate(rbacTestFixture.getUserData(), candidateInOtherList.getPk());

		// 1. Proposer in other proposer list, 2. Proposer in candidate list
		assertEquals(proposerDuplicate.isInvalid(), true);
		assertEquals(proposerDuplicate.getValidationMessageList().size(), 1);
		assertEquals(candidateDuplicate.isInvalid(), true);
		assertEquals(candidateDuplicate.getValidationMessageList().size(), 1);
	}

	@Test
	public void testConvertVoterToCandidate() {
		Proposer proposer = new Proposer();
		Voter voter = new Voter();
		voter.setFirstName("f");
		voter.setLastName("l");
		voter.setId("1");
		voter.setDateOfBirth(new LocalDate(1980, 1, 1));
		voter.setMunicipalityId(listProposalTestFixture.getTestMunicipality().getId());
		voter.setAddressLine1("a");
		voter.setPostalCode(ListProposalTestFixture.TEST_POSTAL_CODE);
		voter.setPostTown("t");
		voter.setEmail("e");
		voter.setTelephoneNumber("11");
		proposerService.convertVoterToProposer(proposer, voter);
		assertEquals(proposer.getFirstName(), voter.getFirstName());
		assertEquals(proposer.getLastName(), voter.getLastName());
		assertEquals(proposer.getId(), voter.getId());
		assertEquals(proposer.getDateOfBirth(), voter.getDateOfBirth());
		assertEquals(proposer.getAddressLine1(), voter.getAddressLine1());
		assertEquals(proposer.getPostalCode(), voter.getPostalCode());
		assertEquals(proposer.getPostTown(), voter.getPostTown());
	}

	@Test
	public void testSetMockIdForEmptyId() {
		Proposer proposer = new Proposer();
		proposer.setBallot(listProposalTestFixture.getTestAffiliation().getBallot());
		proposer.setId("");
		proposer.setDateOfBirth(new LocalDate(1980, 10, 10));

		Map<String, Proposer> proposerIdList = new HashMap<>();
		proposerService.setMockIdForEmptyId(proposer, listProposalTestFixture.getTestBallotPk(), proposerIdList);

		assertEquals(proposer.getId(), "10108000000");
		assertTrue(proposerIdList.containsKey("10108000000"));
		assertEquals(proposerIdList.get("10108000000"), proposer);
	}

	@Test
	public void testSelectiveProposerRole() {
		List<ProposerRole> roles = proposerRepository.findSelectiveProposerRoles();
		assertEquals(2, roles.size());
		assertEquals("TILU", roles.get(0).getId());
		assertEquals("VARU", roles.get(1).getId());
	}

	private Proposer createProposer(final String id) {
		Proposer proposer = proposerService.createNewProposer(listProposalTestFixture.getTestAffiliation().getBallot());
		proposer.setId(id);
		proposer.setFirstName(id);
		proposer.setLastName(id);
		return proposer;
	}
}

