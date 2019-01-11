package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import no.evote.service.voting.VotingService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RejectedVotingsReportControllerTest extends BaseFrontendTest {

	@Test
	public void doInit_withVotings_verifyState() throws Exception {
		RejectedVotingsReportController ctrl = initializeMocks(RejectedVotingsReportController.class);
		stub_getRejectedVotingsByElectionGroupAndMunicipality();
		setUserDataElection(ELECTION_PATH_ELECTION_GROUP);
		setUserDataArea(AREA_PATH_MUNICIPALITY);

		ctrl.init();

		assertThat(ctrl.getRejectedVotingsList()).hasSize(1);
		assertThat(ctrl.getSelectedRejectedVotings()).isNotNull();
	}

	@Test
	public void removeRejectionForVotings_withNoSelectedVoting_returnsRequiredPersonErrorMessage() throws Exception {
		RejectedVotingsReportController ctrl = initializeMocks(RejectedVotingsReportController.class);

		ctrl.removeRejectionForVotings();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.required, @common.person]");
	}

	@Test
	public void removeRejectionForVotings_withSelectedVoting_verifyState() throws Exception {
		RejectedVotingsReportController ctrl = initializeMocks(RejectedVotingsReportController.class);
		setUserDataElection(ELECTION_PATH_ELECTION_GROUP);
		setUserDataArea(AREA_PATH_MUNICIPALITY);
		stub_getRejectedVotingsByElectionGroupAndMunicipality();
		ctrl.init();
		Voting voting = ctrl.getSelectedRejectedVotings();

		ctrl.removeRejectionForVotings();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@voting.approveBallot.undoRejectionResponse");
        verify(voting).setVotingRejection(any());
        verify(voting).setValidationTimestamp(any());
		verify(getInjectMock(VotingService.class)).update(getUserDataMock(), voting);

	}

	private void stub_getRejectedVotingsByElectionGroupAndMunicipality() {
		when(getInjectMock(VotingService.class).getRejectedVotingsByElectionGroupAndMunicipality(
                eq(getUserDataMock()), any(ValggruppeSti.class), any())).thenReturn(mockList(1, Voting.class));
	}
}
