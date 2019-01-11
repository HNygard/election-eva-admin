package no.valg.eva.admin.frontend.listproposal.ctrls;

import no.evote.dto.ListProposalValidationData;
import no.evote.security.UserData;
import no.evote.service.configuration.CandidateService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.Party;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CandidateUploadControllerTest extends BaseFrontendTest {
	private static final String CANDIDATE_LIST = "H Bergen.xls";
	private static final String CANDIDATE_LIST_WITH_ERRORS = "CandidatesWithErrors.xlsx";
	private static final String LIST_PROPOSAL_CANDIDATE_LIST_INVALID_FORMAT_IN_CELL = "@listProposal.candidateList.invalidFormat.in.cell";
	private CandidateUploadController candidateUploadController;

	@BeforeMethod
	public void setUp() throws Exception {
		candidateUploadController = initializeMocks(CandidateUploadController.class);
		Affiliation affiliation = new Affiliation();
		affiliation.setParty(new Party());
		Contest contest = new Contest();
		Election election = new Election();
		election.setSingleArea(false);
		contest.setElection(election);
		affiliation.setBallot(new Ballot(contest, "id", true));
		mockFieldValue("currentAffiliation", affiliation);
		mockFieldValue("candidateList", new ArrayList<>());

		ListProposalValidationData fakeValidationData = mock(ListProposalValidationData.class);
		when(getInjectMock(CandidateService.class).isCandidatesValid(any(UserData.class), anyList(), anyLong(), anyInt())).thenReturn(fakeValidationData);
		when(fakeValidationData.isApproved()).thenReturn(true);
        when(fakeValidationData.getCandidateList()).thenReturn(new ArrayList<>());

		getServletContainer().setRequestParameter("category", "VO");
	}

    @Test
    public void testFileImport() {
        UploadedFile file = getUploadedFile(CANDIDATE_LIST);
        FileUploadEvent event = new FileUploadEvent(mock(UIComponent.class, RETURNS_DEEP_STUBS), file);
        stub_convertRowsToCandidateList(0);

        candidateUploadController.fileImport(event);

        verify(getInjectMock(CandidateService.class)).convertRowsToCandidateList(any(), argThat(argument -> argument != null && argument.size() == 73),
                any(), anyInt(), any());

	}

	@Test
    public void testFileImport_withIllegalFile_producesErrorMessages() {

		UploadedFile file = getUploadedFile(CANDIDATE_LIST_WITH_ERRORS);
		FileUploadEvent event = new FileUploadEvent(mock(UIComponent.class, RETURNS_DEEP_STUBS), file);

		candidateUploadController.fileImport(event);
		verify(getMessageProviderMock()).get(LIST_PROPOSAL_CANDIDATE_LIST_INVALID_FORMAT_IN_CELL, "D4");
		verify(getMessageProviderMock()).get(LIST_PROPOSAL_CANDIDATE_LIST_INVALID_FORMAT_IN_CELL, "B5");
		verify(getMessageProviderMock()).get(LIST_PROPOSAL_CANDIDATE_LIST_INVALID_FORMAT_IN_CELL, "E6");
		verify(getMessageProviderMock()).get(LIST_PROPOSAL_CANDIDATE_LIST_INVALID_FORMAT_IN_CELL, "D7, E7");
		verify(getMessageProviderMock()).get("@listProposal.candidateList.couldNotRead");
	}

	@Test
	public void fileImport_withInvalidCandidates_returnsErrorMessage() throws Exception {
		CandidateUploadController ctrl = initializeMocks(CandidateUploadController.class);
		FileUploadEvent event = defaultSetup();
		stub_convertRowsToCandidateList(73);
		stub_isCandidatesValid(false, 5);

		ctrl.fileImport(event);

        List<FacesMessage> expectedMessages = new ArrayList<>();
		for (int i = 2; i < 7; i++) {
            expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, "@listProposal.candidateList.excelRow " + i + "# invalid", null));
        }
        assertFacesMessages(expectedMessages);
	}

	@Test(dataProvider = "fileImport")
	public void fileImport_withDataProvider_verifyExpected(int size, String expectedMessage) throws Exception {
		CandidateUploadController ctrl = initializeMocks(CandidateUploadController.class);
		FileUploadEvent event = defaultSetup();
		stub_convertRowsToCandidateList(size);
		stub_isCandidatesValid(true, size);

		ctrl.fileImport(event);

		assertFacesMessage(FacesMessage.SEVERITY_INFO, expectedMessage);
		verify_closeAndUpdate(getInjectMock(CandidateController.class).getUploadCandidatesDialog(), "editListProposalForm:msg",
				"editListProposalForm:tabs:candidatesDataTable");
	}

	@DataProvider(name = "fileImport")
	public Object[][] fileImport() {
		return new Object[][] {
				{ 1, "1 @listProposal.candidateListImported.singular" },
				{ 10, "10 @listProposal.candidateListImported.plural" }
		};
	}

	private FileUploadEvent defaultSetup() throws Exception {
		UploadedFile file = getUploadedFile(CANDIDATE_LIST);
		FileUploadEvent event = new FileUploadEvent(createMock(UIComponent.class), file);
		mockField("currentAffiliation", Affiliation.class);
		mockFieldValue("candidateList", new ArrayList<Candidate>());
		return event;
	}

	private UploadedFile getUploadedFile(final String candidateList) {
		return new UploadedFile() {
			@Override
			public String getFileName() {
				return CANDIDATE_LIST;
			}

			@Override
            public InputStream getInputstream() {
				return getClass().getResourceAsStream("/" + candidateList);
			}

			@Override
			public long getSize() {
				return 0;
			}

			@Override
			public byte[] getContents() {
				return new byte[0];
			}

			@Override
			public String getContentType() {
				return null;
			}

			@Override
            public void write(String s) {
				// Ignore
			}
		};
	}

	private void stub_convertRowsToCandidateList(int size) {
		when(getInjectMock(CandidateService.class).convertRowsToCandidateList(
				eq(getUserDataMock()),
                anyList(), any(), anyInt(), any()))
						.thenReturn(mockList(size, Candidate.class));
	}

	private void stub_isCandidatesValid(boolean approved, int size) {
		ListProposalValidationData data = createMock(ListProposalValidationData.class);
		when(data.isApproved()).thenReturn(approved);
		List<Candidate> candidates = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Candidate candidate = createMock(Candidate.class);
			when(candidate.getPk()).thenReturn(null);
			when(candidate.isInvalid()).thenReturn(!approved);
            when(candidate.getValidationMessageList()).thenReturn(singletonList(new UserMessage("invalid")));
			candidates.add(candidate);
		}
		when(data.getCandidateList()).thenReturn(candidates);
		when(getInjectMock(CandidateService.class).isCandidatesValid(eq(getUserDataMock()), anyList(), anyLong(), anyInt())).thenReturn(data);
	}
}

