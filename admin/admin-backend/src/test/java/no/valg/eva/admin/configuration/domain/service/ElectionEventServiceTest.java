package no.valg.eva.admin.configuration.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.ElectionVoteCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.ProposerRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;


public class ElectionEventServiceTest extends MockUtilsTestCase {

	private ElectionEventDomainService electionEventService;
	private ElectionEvent electionEventFrom, electionEventTo;
	private Election electionFrom, electionTo;
	private UserData userData;
	private ElectionGroup electionGroupFrom, electionGroupTo;
	private Municipality municipalityFrom, municipalityTo;

	@BeforeMethod(alwaysRun = true)
	public void setUpForAllTests() throws Exception {
		electionEventService = initializeMocks(ElectionEventDomainService.class);
		electionEventFrom = new ElectionEvent();
		electionEventFrom.setId("100001");
		electionEventFrom.setName("Valg 2099");
		electionEventFrom.setPk(1L);

		electionEventTo = new ElectionEvent();
		electionEventTo.setId("100002");
		electionEventTo.setPk(2L);

		electionTo = new Election();
		electionTo.setId("01");
		electionTo.setPk(3L);

		electionFrom = new Election();
		electionFrom.setId("01");
		electionFrom.setPk(4L);

		userData = new UserData();
		userData.setUid("03011700143");

		MvArea mvAreaFrom = new MvArea();
		mvAreaFrom.setAreaPath("100001.47.19.1938.193800.0005");

		MvArea mvAreaTo = new MvArea();
		mvAreaTo.setAreaPath("100002.47.19.1938.193800.0005");

		Voter voter = new Voter();
		voter.setId("1");
		voter.setMvArea(mvAreaFrom);
		voter.setElectionEvent(electionEventFrom);

		List<Voter> electoralRoll = new ArrayList<>();
		electoralRoll.add(voter);

		when(getInjectMock(MvAreaRepository.class).findSingleByPath(anyString())).thenReturn(mvAreaTo);

		electionGroupFrom = new ElectionGroup();
		electionGroupFrom.setId("01");
		electionGroupFrom.setPk(1L);
		List<ElectionGroup> electionGroupList = new ArrayList<>();
		electionGroupList.add(electionGroupFrom);

		electionGroupTo = new ElectionGroup();
		electionGroupTo.setId("01");
		electionGroupTo.setPk(2L);

		municipalityFrom = new Municipality();
		municipalityFrom.setId("0106");
		municipalityFrom.setPk(1L);

		municipalityTo = new Municipality();
		municipalityTo.setId("0106");
		municipalityTo.setPk(2L);

		Country country = new Country();
		country.setPk(1L);
		country.setId("47");

		County county = new County();
		county.setPk(1L);
		county.setId("01");

		municipalityFrom.setCounty(county);
		county.setCountry(country);

		when(getInjectMock(ElectionGroupRepository.class).getElectionGroupsSorted(electionEventFrom.getPk())).thenReturn(electionGroupList);
	}

	@Test
	public void testCopyElectionReportCountCategoryCreatesElectionVoteCountCategoryForNewElectionEvent() {
		ElectionVoteCountCategory electionVoteCountCategory = new ElectionVoteCountCategory();
		createElectionVoteCountCategoryList(electionVoteCountCategory);

		electionEventService.copyElectionReportCountCategory(userData, electionEventFrom, electionEventTo);
		verify(getInjectMock(ElectionVoteCountCategoryRepository.class), times(1)).update(any(UserData.class), any(List.class));
	}

	@Test
	public void testCopyElectionReportCountCategorySetsCorrectElectionGroupPkAndId() {
		ElectionVoteCountCategory electionVoteCountCategory = new ElectionVoteCountCategory();
		when(getInjectMock(ElectionGroupRepository.class).findElectionGroupById(electionEventTo.getPk(), electionGroupFrom.getId())).thenReturn(electionGroupTo);

		createElectionVoteCountCategoryList(electionVoteCountCategory);

		electionEventService.copyElectionReportCountCategory(userData, electionEventFrom, electionEventTo);
		ArgumentCaptor<List> electionVoteCountCategoryListCaptor = ArgumentCaptor.forClass(List.class);

		verify(getInjectMock(ElectionVoteCountCategoryRepository.class)).update(any(UserData.class), electionVoteCountCategoryListCaptor.capture());
		ElectionGroup electionGroup = ((ElectionVoteCountCategory) electionVoteCountCategoryListCaptor.getValue().get(0)).getElectionGroup();
		assertEquals(electionGroup.getPk(), electionGroupTo.getPk());
		assertEquals(electionGroup.getId(), electionGroupFrom.getId());
	}

	@Test
	public void testCopyElectionReportCountCategoryCopiesCorrectElectionVoteCountCategory() {
		ElectionVoteCountCategory electionVoteCountCategory = new ElectionVoteCountCategory();
		electionVoteCountCategory.setCountCategoryEditable(true);
		electionVoteCountCategory.setCountCategoryEnabled(true);
		when(getInjectMock(ElectionGroupRepository.class).findElectionGroupById(electionEventTo.getPk(), electionGroupFrom.getId())).thenReturn(electionGroupTo);

		createElectionVoteCountCategoryList(electionVoteCountCategory);

		electionEventService.copyElectionReportCountCategory(userData, electionEventFrom, electionEventTo);
		ArgumentCaptor<List> electionVoteCountCategoryListCaptor = ArgumentCaptor.forClass(List.class);
		verify(getInjectMock(ElectionVoteCountCategoryRepository.class)).update(any(UserData.class), electionVoteCountCategoryListCaptor.capture());
		ElectionVoteCountCategory electionVoteCountCategoryCopied = ((ElectionVoteCountCategory) electionVoteCountCategoryListCaptor.getValue().get(0));
		assertEquals(electionVoteCountCategoryCopied.isCountCategoryEditable(), electionVoteCountCategory.isCountCategoryEditable());
		assertEquals(electionVoteCountCategoryCopied.isCountCategoryEnabled(), electionVoteCountCategory.isCountCategoryEnabled());
	}

	@Test
	public void testCopyElectionReportCountCategoryPkIsNullBeforeCreates() {
		ElectionVoteCountCategory electionVoteCountCategory = new ElectionVoteCountCategory();
		when(getInjectMock(ElectionGroupRepository.class).findElectionGroupById(electionEventTo.getPk(), electionGroupFrom.getId())).thenReturn(electionGroupTo);

		createElectionVoteCountCategoryList(electionVoteCountCategory);

		electionEventService.copyElectionReportCountCategory(userData, electionEventFrom, electionEventTo);
		ArgumentCaptor<List> electionVoteCountCategoryListCaptor = ArgumentCaptor.forClass(List.class);
		verify(getInjectMock(ElectionVoteCountCategoryRepository.class)).update(any(UserData.class), electionVoteCountCategoryListCaptor.capture());
		ElectionVoteCountCategory electionVoteCountCategoryCopied = ((ElectionVoteCountCategory) electionVoteCountCategoryListCaptor.getValue().get(0));
		assertNull(electionVoteCountCategoryCopied.getPk());
	}

	private void createElectionVoteCountCategoryList(ElectionVoteCountCategory electionVoteCountCategory) {
		List<ElectionVoteCountCategory> electionVoteCountCategoryList = new ArrayList<>();
		electionVoteCountCategoryList.add(electionVoteCountCategory);
		when(getInjectMock(ElectionVoteCountCategoryRepository.class).findElectionVoteCountCategories(eq(electionGroupFrom), anyBoolean()))
				.thenReturn(electionVoteCountCategoryList);
	}

	@Test
	public void testCopyReportCountCategoriesSetsCorrectMunicipality() {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		reportCountCategory.setMunicipality(municipalityFrom);
		reportCountCategory.setElectionGroup(electionGroupTo);
		List<ReportCountCategory> reportCountCategoriesList = new ArrayList<>();
		reportCountCategoriesList.add(reportCountCategory);

		String countryId = reportCountCategory.getMunicipality().getCounty().getCountry().getId();
		String countyId = reportCountCategory.getMunicipality().getCounty().getId();
		String municipalityId = reportCountCategory.getMunicipality().getId();

		when(getInjectMock(ElectionGroupRepository.class).findElectionGroupById(electionEventTo.getPk(), reportCountCategory.getElectionGroup().getId()))
				.thenReturn(electionGroupTo);
		when(getInjectMock(ReportCountCategoryRepository.class).findAllReportCountCategoriesForElectionEvent(eq(electionEventFrom.getPk()), anyBoolean()))
				.thenReturn(reportCountCategoriesList);
		when(getInjectMock(MunicipalityRepository.class).findUniqueMunicipalityByElectionEvent(electionEventTo.getPk(), countryId, countyId, municipalityId))
				.thenReturn(municipalityTo);

		ArgumentCaptor<ReportCountCategory> reportCountCategoryCaptor = ArgumentCaptor.forClass(ReportCountCategory.class);
		electionEventService.copyReportCountCategories(userData, electionEventFrom, electionEventTo);
		verify(getInjectMock(ReportCountCategoryRepository.class)).create(any(UserData.class), reportCountCategoryCaptor.capture());
		assertThat(reportCountCategoryCaptor.getValue().getMunicipality().getPk()).isEqualTo(municipalityTo.getPk());
	}

	@Test
	public void testCopyBallotAndProposalList() {
		List<ElectionGroup> electionGroupListFrom = new ArrayList<>();
		List<Election> electionListTo = new ArrayList<>();
		List<Ballot> ballotListFrom = new ArrayList<>();
		List<Contest> contestListFrom = new ArrayList<>();
		List<Contest> contestListTo = new ArrayList<>();

		List<Proposer> pListTo = new ArrayList<>();
		List<Candidate> candidateListFrom = new ArrayList<>();

		Affiliation fromAffiliation = mockAffiliation(3L);
		Affiliation toAffiliation = mockAffiliation(null);
		Contest fromContest = mockContest(1L);
		Contest toContest = mockContest(null);
		Ballot fromBallot = mockBallot(10L);
		Ballot toBallot = mockBallot(null);
		toBallot.setContest(toContest);
		Candidate fromCandidate = mockCandidate(2L);
		Proposer toProposer = mockProposer(null);
		toProposer.setBallot(toBallot);

		electionGroupListFrom.add(electionGroupFrom);
		electionListTo.add(electionTo);
		contestListFrom.add(fromContest);
		contestListTo.add(toContest);
		candidateListFrom.add(fromCandidate);
		ballotListFrom.add(fromBallot);
		pListTo.add(toProposer);

		when(getInjectMock(ElectionGroupRepository.class).getElectionGroupsSorted(electionEventFrom.getPk())).thenReturn(electionGroupListFrom);
		when(getInjectMock(ElectionGroupRepository.class).findElectionGroupById(electionEventTo.getPk(), electionGroupFrom.getId())).thenReturn(electionGroupTo);
		when(getInjectMock(ElectionRepository.class).findElectionsByElectionGroup(electionGroupTo.getPk())).thenReturn(electionListTo);
		when(getInjectMock(ElectionRepository.class).findElectionByElectionGroupAndId(electionGroupFrom.getPk(), electionFrom.getId())).thenReturn(electionFrom);
		when(getInjectMock(ContestRepository.class).findByElectionPk(electionFrom.getPk())).thenReturn(contestListFrom);
		when(getInjectMock(ContestRepository.class).findContestById(electionTo.getPk(), toContest.getId())).thenReturn(toContest);
		when(getInjectMock(ContestRepository.class).findByElectionPk(electionTo.getPk())).thenReturn(contestListTo);
		when(getInjectMock(ContestRepository.class).findContestById(electionFrom.getPk(), fromContest.getId())).thenReturn(fromContest);
		when(getInjectMock(BallotRepository.class).findByContest(fromContest.getPk())).thenReturn(ballotListFrom);
		when(getInjectMock(ProposerRepository.class).findByBallot(fromBallot.getPk())).thenReturn(pListTo);
		when(getInjectMock(CandidateRepository.class).findByAffiliation(fromAffiliation.getPk())).thenReturn(candidateListFrom);
		when(getInjectMock(AffiliationRepository.class).findByBallot(fromBallot.getPk())).thenReturn(fromAffiliation);
		when(getInjectMock(PartyRepository.class).findPartyByIdAndEvent(anyString(), anyLong())).thenReturn(mockParty());

		electionEventService.copyProposerLists(userData, electionEventFrom, electionEventTo);

		verify(getInjectMock(AffiliationRepository.class), times(1)).createAffiliation(userData, toAffiliation);
		verify(getInjectMock(ProposerRepository.class), times(1)).createProposer(any(UserData.class), any(Proposer.class));
		verify(getInjectMock(CandidateRepository.class), times(1)).createCandidate(any(UserData.class), any(Candidate.class));
	}

	private Proposer mockProposer(Long pk) {
		Proposer proposer = new Proposer();
		proposer.setPk(pk);
		proposer.setId("pr123");
		return proposer;
	}

	private Candidate mockCandidate(Long pk) {
		Candidate candidate = new Candidate();
		candidate.setPk(pk);
		candidate.setId("ca123");
		return candidate;
	}

	private Affiliation mockAffiliation(Long pk) {
		Affiliation affiliation = new Affiliation();
		affiliation.setPk(pk);
		affiliation.setParty(mockParty());
		return affiliation;
	}

	private Contest mockContest(Long pk) {
		Contest contest = new Contest();
		contest.setPk(pk);
		contest.setId("c123");
		return contest;
	}

	private Party mockParty() {
		Party party = new Party();
		party.setPk(4L);
		party.setId("p123");
		return party;
	}

	private Ballot mockBallot(Long pk) {
		Ballot ballot = new Ballot();
		ballot.setPk(pk);
		ballot.setId("b123");
		return ballot;
	}
}

