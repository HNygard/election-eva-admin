package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.List;

import no.evote.dto.ListProposalValidationData;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.test.TestGroups;

import org.joda.time.LocalDate;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { TestGroups.REPOSITORY })

public class LegacyListProposalServiceTest extends ListProposalBaseTest {

	private static final LocalDate DATE_OF_BIRTH = new LocalDate(1975, 6, 30);
	private static final String LAST_NAME = "LastName";
	private static final String FIRST_NAME = "FirstName";
	
	private LegacyListProposalService listProposalService;

	@Override
	@BeforeMethod(alwaysRun = true)
	public void init() {
		super.init();
		listProposalService = backend.getListProposalService();
	}

	@Test
	public void testApproveIsBetweenInterval() {
		getContest().setMinCandidates(1);
		getContest().setMaxCandidates(-1);
		getContest().setMinProposersNewParty(1);
		getContest().setMinProposersOldParty(1);

		ListProposalValidationData validationData = listProposalService.validateListProposalAndCheckAgainstRoll(rbacTestFixture.getUserData(),
				getTestAffiliation(),
				new ArrayList<>(), new ArrayList<>(), getElectionEvent().getId(), getMvAreasSet(), getMvAreasSet());
		Assert.assertFalse(validationData.isApproved());
		Assert.assertEquals(3, validationData.getAffiliation().getValidationMessageList().size());
	}

	@Test
	public void validationFailsIfTooFewCandidates() {
		setTestValuesForContest();
		ListProposalValidationData validationData = new ListProposalValidationData(getTestCandidateList(), getTestProposerList(), getTestAffiliation());

		validationData = listProposalService.validateNumberOfCandidatesAndProposers(rbacTestFixture.getUserData(), validationData);

		Assert.assertFalse(validationData.isApproved());
	}

	@Test
	public void validationOkIfApprovedPartyAndSufficientCandidatesAndProposers() {
		setTestValuesForContest();
		getContest().setMinCandidates(1);
		Affiliation affiliation = getTestAffiliation();
		affiliation.getParty().setApproved(true);
		affiliation.getParty().setForenkletBehandling(true);
		List<Proposer> proposerList = getTestProposerList();
		ListProposalValidationData validationData = new ListProposalValidationData(getTestCandidateList(), proposerList, affiliation);

		validationData = listProposalService.validateNumberOfCandidatesAndProposers(rbacTestFixture.getUserData(), validationData);

		Assert.assertTrue(validationData.isApproved());
	}

	private void setTestValuesForContest() {
		getContest().setMinCandidates(2);
		getContest().setMaxCandidates(10);
		getContest().setMinProposersNewParty(5);
		getContest().setMinProposersOldParty(1);
	}

	private List<Proposer> getTestProposerList() {
		Proposer p = new Proposer(FIRST_NAME, LAST_NAME, DATE_OF_BIRTH);
		List<Proposer> proposerList = new ArrayList<>();
		proposerList.add(p);
		return proposerList;
	}

	private List<Candidate> getTestCandidateList() {
		Candidate c = new Candidate();
		List<Candidate> candidateList = new ArrayList<>();
		candidateList.add(c);
		return candidateList;
	}

}

