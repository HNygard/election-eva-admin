package no.valg.eva.admin.configuration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.enterprise.event.Event;

import no.evote.security.UserData;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.ListProposalTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.evote.service.configuration.AffiliationServiceBean;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.BallotStatus;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = { TestGroups.SLOW, TestGroups.REPOSITORY })
public class AffiliationServiceBeanTest extends AbstractJpaTestBase {
	
	private AffiliationServiceBean affiliationService;
	private AffiliationRepository affiliationRepository;
	private ServiceBackedRBACTestFixture rbacTestFixture;
	private ListProposalTestFixture listProposalTestFixture;
	private PartyCategoryRepository partyCategoryRepository;

	@BeforeMethod(alwaysRun = true)
	public void initDependencies() throws Exception {
		BackendContainer backend = new BackendContainer(getEntityManager(), mock(Event.class));
		backend.initServices();
		affiliationService = backend.getAffiliationService();
		affiliationRepository = backend.getAffiliationRepository();
		partyCategoryRepository = backend.getPartyCategoryRepository();
		rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
		listProposalTestFixture = new ListProposalTestFixture(backend);
		rbacTestFixture.init();
		listProposalTestFixture.init();
	}

	@Test
	public void testCreateNewAffiliationNull() {
		Assert.assertNull(affiliationService.createNewAffiliation(rbacTestFixture.getUserData(), null, null, null,
				BallotStatus.BallotStatusValue.PENDING.getId()));
	}

	@Test
	public void testCreateUpdateAndDeleteNewPartyAndAffiliation() {
		Party newParty = new Party();
		newParty.setPartyCategory(partyCategoryRepository.findPartyCategoryByPk(1L));
		newParty.setId("_TESTID");
		newParty.setTranslatedPartyName("Name");
		newParty.setElectionEvent(listProposalTestFixture.getElectionEvent());
		Affiliation newAffiliation = affiliationService.createNewPartyAndAffiliation(rbacTestFixture.getUserData(), listProposalTestFixture.getContest(),
				newParty, rbacTestFixture.getUserData().getLocale());
		Assert.assertNull(newAffiliation.getDisplayOrder());
		Assert.assertNull(newAffiliation.getBallot().getDisplayOrder());
		Assert.assertEquals(newAffiliation.isShowCandidateProfession(), false);
		Assert.assertEquals(newAffiliation.isShowCandidateResidence(), false);

		newAffiliation.setShowCandidateProfession(true);
		newAffiliation.setShowCandidateResidence(true);
		affiliationRepository.updateAffiliation(rbacTestFixture.getUserData(), newAffiliation);

		Affiliation updatedAffiliation = affiliationRepository.findAffiliationByPk(newAffiliation.getPk());

		Assert.assertEquals(updatedAffiliation.isShowCandidateProfession(), true);
		Assert.assertEquals(updatedAffiliation.isShowCandidateResidence(), true);
	}

	@Test
	public void hasAffiliationPartyId() {
		Assert.assertTrue(affiliationRepository.hasAffiliationPartyId(listProposalTestFixture.getTestAffiliation().getParty().getId(),
			listProposalTestFixture.getElectionEvent().getPk()));
	}

	@Test
	public void testSaveColumns() {
		listProposalTestFixture.getTestAffiliation().setShowCandidateProfession(true);
		listProposalTestFixture.getTestAffiliation().setShowCandidateResidence(true);

		Affiliation updatedAffiliation = affiliationService.saveColumns(rbacTestFixture.getUserData(), listProposalTestFixture.getTestAffiliation());

		Assert.assertEquals(updatedAffiliation.isShowCandidateProfession(), true);
		Assert.assertEquals(updatedAffiliation.isShowCandidateResidence(), true);

		updatedAffiliation.setShowCandidateProfession(false);
		updatedAffiliation.setShowCandidateResidence(false);

		updatedAffiliation = affiliationService.saveColumns(rbacTestFixture.getUserData(), updatedAffiliation);

		Assert.assertEquals(updatedAffiliation.isShowCandidateProfession(), false);
		Assert.assertEquals(updatedAffiliation.isShowCandidateResidence(), false);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
		  expectedExceptionsMessageRegExp = "changeDisplayOrder displayOrderFrom\\[1\\] equal to displayOrderTo\\[1\\]")
	public void changeDisplayOrder_withEqualFromTo_throwsException() throws Exception {
		Affiliation affiliationMock = mock(Affiliation.class, RETURNS_DEEP_STUBS);
		UserData userDataMock = mock(UserData.class, RETURNS_DEEP_STUBS);
		when(affiliationMock.getBallot().getContest().getPk()).thenReturn(20L);

		affiliationService.changeDisplayOrder(userDataMock, affiliationMock, 1, 1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
		  expectedExceptionsMessageRegExp = "changeDisplayOrder from/to \\[1/1000\\] does not match actual size 12")
	public void changeDisplayOrder_withOutOfBoundsFromTo_throwsException() throws Exception {
		Affiliation affiliationMock = mock(Affiliation.class, RETURNS_DEEP_STUBS);
		UserData userDataMock = mock(UserData.class, RETURNS_DEEP_STUBS);
		when(affiliationMock.getBallot().getContest().getPk()).thenReturn(20L);

		affiliationService.changeDisplayOrder(userDataMock, affiliationMock, 1, 1000);
	}

	@Test
	public void changeDisplayOrder_withLowFromAndHighTo_returnsResult() throws Exception {
		Affiliation affiliationMock = mock(Affiliation.class, RETURNS_DEEP_STUBS);
		UserData userDataMock = mock(UserData.class, RETURNS_DEEP_STUBS);
		when(affiliationMock.getBallot().getContest().getPk()).thenReturn(20L);

		List<Affiliation> result = affiliationService.changeDisplayOrder(userDataMock, affiliationMock, 1, 5);

		assertThat(result.size()).isEqualTo(5);
		assertThat(result.get(0).getDisplayOrder()).isEqualTo(1);
		assertThat(result.get(0).getParty().getId()).isEqualTo("SV");
		assertThat(result.get(1).getDisplayOrder()).isEqualTo(2);
		assertThat(result.get(1).getParty().getId()).isEqualTo("SP");
		assertThat(result.get(2).getDisplayOrder()).isEqualTo(3);
		assertThat(result.get(2).getParty().getId()).isEqualTo("KRF");
		assertThat(result.get(3).getDisplayOrder()).isEqualTo(4);
		assertThat(result.get(3).getParty().getId()).isEqualTo("V");
		assertThat(result.get(4).getDisplayOrder()).isEqualTo(5);
		assertThat(result.get(4).getParty().getId()).isEqualTo("A");
	}

	@Test
	public void changeDisplayOrder_withHighFromAndLowTo_returnsResult() throws Exception {
		Affiliation affiliationMock = mock(Affiliation.class, RETURNS_DEEP_STUBS);
		UserData userDataMock = mock(UserData.class, RETURNS_DEEP_STUBS);
		when(affiliationMock.getBallot().getContest().getPk()).thenReturn(20L);

		List<Affiliation> result = affiliationService.changeDisplayOrder(userDataMock, affiliationMock, 5, 3);

		assertThat(result.size()).isEqualTo(3);
		assertThat(result.get(0).getDisplayOrder()).isEqualTo(3);
		assertThat(result.get(0).getParty().getId()).isEqualTo("V");
		assertThat(result.get(1).getDisplayOrder()).isEqualTo(4);
		assertThat(result.get(1).getParty().getId()).isEqualTo("SP");
		assertThat(result.get(2).getDisplayOrder()).isEqualTo(5);
		assertThat(result.get(2).getParty().getId()).isEqualTo("KRF");
	}
}

