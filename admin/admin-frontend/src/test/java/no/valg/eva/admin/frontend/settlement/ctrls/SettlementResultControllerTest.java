package no.valg.eva.admin.frontend.settlement.ctrls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import javax.faces.application.FacesMessage;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.configuration.domain.model.MvElection;

import org.testng.annotations.Test;

public class SettlementResultControllerTest extends BaseFrontendTest {

	@Test
	public void initView_withWithNoSettlement_shouldAddNoSettlementErrorMessage() throws Exception {
		SettlementResultController ctrl = initializeMocks(SettlementResultController.class);
		setMvElection(false, false);

		ctrl.initView();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@settlement.error.no_settlement");
	}

	@Test
	public void initView_withWithSettlement_verifySettlementData() throws Exception {
		SettlementResultController ctrl = initializeMocks(SettlementResultController.class);
		setContestInfo();
		setMvElection(false, false);
		setSettlementDone();

		ctrl.initView();

		assertThat(ctrl.getAffiliationVoteCounts()).isNotNull();
		assertThat(ctrl.getCandidateSeats()).isNotNull();
		assertThat(ctrl.getMandates()).isNotNull();
	}

	@Test
	public void backToSelectContest_returnsRedirectURL() throws Exception {
		SettlementResultController ctrl = initializeMocks(SettlementResultController.class);

		assertThat(ctrl.backToSelectContest()).isEqualTo("settlementResult.xhtml?faces-redirect=true");
	}

	@Test
	public void isWritein_withNoWriteIn_returnsFalse() throws Exception {
		SettlementResultController ctrl = initializeMocks(SettlementResultController.class);
		setMvElection(false, false);

		assertThat(ctrl.isWritein()).isFalse();
	}

	private void setContestInfo() throws Exception {
		mockFieldValue("contestInfo", createMock(ContestInfo.class));
	}

	private void setMvElection(boolean isReferendum, boolean isWritein) throws Exception {
		MvElection mvElectionStub = createMock(MvElection.class);
		mockFieldValue("mvElection", mvElectionStub);
		when(mvElectionStub.getElection().isReferendum()).thenReturn(isReferendum);
		when(mvElectionStub.getElection().isWritein()).thenReturn(isWritein);
	}

	private void setSettlementDone() throws Exception {
		mockFieldValue("settlementDone", true);
	}
}
