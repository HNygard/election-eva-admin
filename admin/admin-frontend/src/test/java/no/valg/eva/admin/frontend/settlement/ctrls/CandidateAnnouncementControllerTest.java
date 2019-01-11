package no.valg.eva.admin.frontend.settlement.ctrls;

import no.evote.dto.CandidateVoteCountDto;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.settlement.model.AffiliationVoteCount;
import no.valg.eva.admin.common.settlement.service.SettlementService;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import org.primefaces.event.SelectEvent;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CandidateAnnouncementControllerTest extends BaseFrontendTest {

	@Test
	public void initView_withWithNoSettlement_shouldAddNoSettlementErrorMessage() throws Exception {
		CandidateAnnouncementController ctrl = initializeMocks(CandidateAnnouncementController.class);
		setMvElection(false, false);

		ctrl.initView();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@settlement.error.no_settlement");
	}

	@Test
	public void initView_withWithSettlement_verifySettlementData() throws Exception {
		CandidateAnnouncementController ctrl = initializeMocks(CandidateAnnouncementController.class);
		setContestInfo();
		setMvElection(false, false);
		setSettlementDone();
		List<AffiliationVoteCount> affiliationVoteCounts = mockList(2, AffiliationVoteCount.class);
		Map<Long, List<CandidateVoteCountDto>> longListMap = new HashMap<>();
		longListMap.put(1L, mockList(1, CandidateVoteCountDto.class));
		longListMap.put(2L, mockList(1, CandidateVoteCountDto.class));
		stub_settlementService_findAffiliationVoteCountsBySettlement(affiliationVoteCounts);
		stub_settlementService_findCandidateVoteCountsBySettlement(longListMap);
		when(affiliationVoteCounts.get(0).getAffiliation().getPk()).thenReturn(1L);
		when(affiliationVoteCounts.get(1).getAffiliation().getPk()).thenReturn(2L);

		ctrl.initView();

		assertThat(ctrl.getMandates()).isNotNull();
		assertThat(ctrl.getAffiliations()).hasSize(2);
		assertThat(ctrl.getCandidateVoteCounts()).hasSize(1);
		assertThat(ctrl.getSelectedAffiliation()).isSameAs(affiliationVoteCounts.get(0).getAffiliation());
	}

	@Test
	public void onRowSelect_withAffiliation_returnsSameSelectedAffiliation() throws Exception {
		CandidateAnnouncementController ctrl = initializeMocks(CandidateAnnouncementController.class);
		SelectEvent selectEventStub = createMock(SelectEvent.class);
		Affiliation affiliationStub = createMock(Affiliation.class);
		Map<Long, List<CandidateVoteCountDto>> m = new HashMap<>();
		m.put(1L, new ArrayList<>());
		when(selectEventStub.getObject()).thenReturn(affiliationStub);
		when(affiliationStub.getPk()).thenReturn(1L);
		mockFieldValue("candidateVoteCountMap", m);

		ctrl.onRowSelect(selectEventStub);

		assertThat(ctrl.getSelectedAffiliation()).isSameAs(affiliationStub);
		assertThat(ctrl.getCandidateVoteCounts()).hasSize(0);
	}

	@Test
	public void backToSelectContest_with_should() throws Exception {
		CandidateAnnouncementController ctrl = initializeMocks(CandidateAnnouncementController.class);

		assertThat(ctrl.backToSelectContest()).isEqualTo("candidateAnnouncement.xhtml?faces-redirect=true");
	}

	@Test
	public void isBaselineConfigured_withZeroBaselineVoteFactor_returnsTrue() throws Exception {
		CandidateAnnouncementController ctrl = initializeMocks(CandidateAnnouncementController.class);
		MvElection mvElectionStub = createMock(MvElection.class);
		when(mvElectionStub.getElection().getBaselineVoteFactor()).thenReturn(BigDecimal.ZERO);
		mockFieldValue("mvElection", mvElectionStub);

		assertThat(ctrl.isBaselineConfigured()).isTrue();
	}

	@Test
	public void hasCandidateRanking_withPersonalElection_returnsTrue() throws Exception {
		CandidateAnnouncementController ctrl = initializeMocks(CandidateAnnouncementController.class);
		MvElection mvElectionStub = createMock(MvElection.class);
		when(mvElectionStub.getElection().isPersonal()).thenReturn(true);
		mockFieldValue("mvElection", mvElectionStub);

		assertThat(ctrl.hasCandidateRanking()).isTrue();
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

	private void stub_settlementService_findAffiliationVoteCountsBySettlement(List<AffiliationVoteCount> list) {
		when(getInjectMock(SettlementService.class).findAffiliationVoteCountsBySettlement(eq(getUserDataMock()), any(ElectionPath.class)))
				.thenReturn(list);
	}

	private void stub_settlementService_findCandidateVoteCountsBySettlement(Map<Long, List<CandidateVoteCountDto>> m) {
		when(getInjectMock(SettlementService.class).findCandidateVoteCountsBySettlement(eq(getUserDataMock()), any(ElectionPath.class))).thenReturn(m);
	}
}
