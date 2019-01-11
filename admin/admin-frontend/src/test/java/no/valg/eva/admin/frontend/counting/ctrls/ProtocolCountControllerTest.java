package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Opphev_Foreløpig_Telling;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Rediger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ProtocolCountControllerTest extends BaseCountControllerTest {

	private ProtocolCountController ctrl;
	private ProtocolCount countStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrl = initializeMocks(ProtocolCountController.class);
		countStub = mock(ProtocolCount.class, RETURNS_DEEP_STUBS);
		when(getCountsMock().getFirstProtocolCount()).thenReturn(countStub);
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		when(getStartCountingControllerMock().getContestPath()).thenReturn(contestPath);
		CountCategory countCategory = VO;
		when(getStartCountingControllerMock().getCountCategory()).thenReturn(countCategory);
		when(getStartCountingControllerMock().getCountContext()).thenReturn(new CountContext(contestPath, countCategory));
	}

	@Test
	public void doInit_withStartCountingController_checkState() {
		ctrl.initCountController();

		assertThat(ctrl.getCountContext()).isNotNull();
	}

	@Test
	public void saveCount_withValidateError_shouldDisplayErrorMessage() {
		validateException().when(countStub).validate();

		ctrl.saveCount();

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
	public void saveCount_withValidData_checkState() {
		ProtocolCount response = mock(ProtocolCount.class);
		when(getCountingServiceMock().saveCount(any(UserData.class), any(CountContext.class), any(ProtocolCount.class))).thenReturn(response);

		ctrl.saveCount();

		verify(getCountsMock()).setFirstProtocolCount(response);
		verify(getMessageProviderMock()).get("@count.isSaved");
		verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
	}

	@Test
	public void approveCount_withValidateError_shouldDisplayErrorMessage() {
		validateException().when(countStub).validateForApproval();

		ctrl.approveCount();

		verify(getMessageProviderMock()).get("@error@", null);
		verify_closeAndUpdate(Dialogs.CONFIRM_APPROVE_PROTOCOL_COUNT.getId(), "countingForm");
	}

	@Test
	public void approveCount_withValidData_checkState() {
		ProtocolCount response = mock(ProtocolCount.class);
		when(getCountingServiceMock().approveCount(any(UserData.class), any(CountContext.class), any(ProtocolCount.class))).thenReturn(response);

		ctrl.approveCount();

		verify(getCountsMock()).setFirstProtocolCount(response);
		verify(getMessageProviderMock()).get("@count.isApproved");
		verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
	}

	@Test
	public void revokeApprovedCount_withException_shouldDisplayErrorMessage() {
		validateException().when(getCountingServiceMock()).revokeCount(any(UserData.class), any(CountContext.class),
				any(ProtocolCount.class));

		ctrl.revokeApprovedCount();

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
	public void revokeApprovedCount_withValidData_checkState() {
		ProtocolCount response = mock(ProtocolCount.class);
		when(getCountingServiceMock().revokeCount(any(UserData.class), any(CountContext.class), any(ProtocolCount.class))).thenReturn(response);

		ctrl.revokeApprovedCount();

		verify(getCountsMock()).setFirstProtocolCount(response);
		verify(getMessageProviderMock()).get("@count.isNotApprovedAnymore");
		verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
	}

	@Test
	public void isApproved_withNoCount_returnsFalse() {
		when(getCountsMock().getProtocolCounts()).thenReturn(emptyList());
		assertThat(ctrl.isApproved()).isFalse();
	}

	@Test
	public void isApproved_withTwoCountsWhereOnlyOneIsApproved_returnsFalse() {
		ProtocolCount count1 = mock(ProtocolCount.class);
		when(count1.isApproved()).thenReturn(true);
		ProtocolCount count2 = mock(ProtocolCount.class);
		when(count2.isApproved()).thenReturn(false);
		when(getCountsMock().getProtocolCounts()).thenReturn(asList(count1, count2));
		assertThat(ctrl.isApproved()).isFalse();
	}

	@Test
	public void isApproved_withTwoApprovedCounts_returnsFalse() {
		ProtocolCount count1 = mock(ProtocolCount.class);
		when(count1.isApproved()).thenReturn(true);
		ProtocolCount count2 = mock(ProtocolCount.class);
		when(count2.isApproved()).thenReturn(true);
		when(getCountsMock().getProtocolCounts()).thenReturn(asList(count1, count2));
		assertThat(ctrl.isApproved()).isTrue();
	}

	@Test
	public void isApproved_withApprovedCount_returnsTrue() {
		when(countStub.isApproved()).thenReturn(true);

		assertThat(ctrl.isApproved()).isTrue();
	}

	@Test
	public void getProtocolCount_withPrelimSelected_returnsFromPrelim() {
		when(getPreliminaryCountControllerMock().isSelectedProtocolCount()).thenReturn(true);
		when(getPreliminaryCountControllerMock().getSelectedProtocolCountIndex()).thenReturn(1);
		ProtocolCount secondStub = mock(ProtocolCount.class);
		when(getCountsMock().getProtocolCounts()).thenReturn(asList(mock(ProtocolCount.class), secondStub));

		assertThat(ctrl.getProtocolCount()).isSameAs(secondStub);
	}

	@Test
	public void setProtocolCount_withPrelimSelected_verify() {
		when(getPreliminaryCountControllerMock().isSelectedProtocolCount()).thenReturn(true);
		when(getPreliminaryCountControllerMock().getSelectedProtocolCountIndex()).thenReturn(1);
		ProtocolCount stub = mock(ProtocolCount.class);
		List<ProtocolCount> list = mockList(2, ProtocolCount.class);
		when(getCountsMock().getProtocolCounts()).thenReturn(list);

		ctrl.setProtocolCount(stub);

		assertThat(list.get(1)).isSameAs(stub);
	}

	@Test
	public void button_withBackFalse_returnDisabled() {
		Button button = ctrl.button(ButtonType.BACK);

		assertThat(button.isDisabled()).isTrue();
	}

	@Test
	public void button_withBackTrue_returnEnabled() {
		when(getPreliminaryCountControllerMock().isSelectedProtocolCount()).thenReturn(true);

		Button button = ctrl.button(ButtonType.BACK);

		assertThat(button.isDisabled()).isFalse();
	}

	@Test
	public void button_withSaveFalse_returnDisabled() {
		Button button = ctrl.button(ButtonType.SAVE);

		assertThat(button.isDisabled()).isTrue();
	}

	@Test
	public void button_withSaveTrue_returnEnabled() {
		hasAccess(Opptelling_Forhånd_Rediger);
		when(getCountsMock().getProtocolCounts()).thenReturn(singletonList(countStub));
		when(countStub.isEditable()).thenReturn(true);

		Button button = ctrl.button(ButtonType.SAVE);

		assertThat(button.isDisabled()).isFalse();
	}

	@Test
	public void button_withApproveFalse_returnDisabled() {
		Button button = ctrl.button(ButtonType.APPROVE);

		assertThat(button.isDisabled()).isTrue();
	}

	@Test
	public void button_withApproveTrue_returnEnabled() {
		hasAccess(Opptelling_Valgting_Rediger);
		when(getCountsMock().getProtocolCounts()).thenReturn(singletonList(countStub));
		when(countStub.isEditable()).thenReturn(true);

		Button button = ctrl.button(ButtonType.APPROVE);

		assertThat(button.isDisabled()).isFalse();
	}

	@Test
	public void button_withRevokeFalse_returnDisabled() {
		Button button = ctrl.button(ButtonType.REVOKE);

		assertThat(button.isDisabled()).isTrue();
	}

	@Test
	public void button_withRevokeTrue_returnEnabled() {
		hasAccess(Opptelling_Opphev_Foreløpig_Telling);
		when(countStub.isApproved()).thenReturn(true);

		Button button = ctrl.button(ButtonType.REVOKE);

		assertThat(button.isDisabled()).isFalse();
	}

	@Test
	public void getDisplayAreaName_withNoSelected_returnsPreliminaryCountControllerDisplayAreaName() {
		when(getCountsMock().getProtocolCounts()).thenReturn(mockList(2, ProtocolCount.class));
		when(getInjectMock(PreliminaryCountController.class).getDisplayAreaName()).thenReturn("areaName");

		assertThat(ctrl.getDisplayAreaName()).isEqualTo("areaName");
	}

	@Test
	public void getDisplayAreaName_withSelected_returnsPollingAndArea() {
		when(getPreliminaryCountControllerMock().isSelectedProtocolCount()).thenReturn(true);
		when(getPreliminaryCountControllerMock().getSelectedProtocolCountIndex()).thenReturn(1);
		ProtocolCount secondStub = mock(ProtocolCount.class, RETURNS_DEEP_STUBS);
		when(getCountsMock().getProtocolCounts()).thenReturn(asList(mock(ProtocolCount.class), secondStub));
		when(secondStub.getAreaPath().getPollingDistrictId()).thenReturn("districtId");
		when(secondStub.getAreaName()).thenReturn("areaName");

		assertThat(ctrl.getDisplayAreaName()).isEqualTo("districtId areaName");
	}

	@Test
	public void back_setsPrelimToNull() {
		ctrl.back();

		verify(getPreliminaryCountControllerMock()).selectProtocolCount(null);
	}

	@Test
	public void getProtocolBallotCountsHeader_whenNotHavingBallotCountForOtherContests_checkText() {
		when(countStub.getBallotCountForOtherContests()).thenReturn(null);

		String header = ctrl.getProtocolBallotCountsHeader();

		assertThat(header).isEqualTo("@count.votes.contentsPolls");
		verify(getMessageProviderMock()).get("@count.votes.contentsPolls");
	}

	@Test
	public void getProtocolBallotCountsHeader_whenHavingBallotCountForOtherContests_checkText() {
		when(countStub.getBallotCountForOtherContests()).thenReturn(1);

		String header = ctrl.getProtocolBallotCountsHeader();

		assertThat(header).isEqualTo("@count.votes.contentsPolls (@area_level[4].name null)");
		verify(getMessageProviderMock()).get("@count.votes.contentsPolls");
		verify(getMessageProviderMock()).get("@area_level[4].name");
	}

	@Test
	public void getProtocolBallotCounts_gittUrnetelling_girForventetResultat() {
		when(countStub.getBlankBallotCount()).thenReturn(0);

		List<ProtocolCountController.ProtocolBallotCount> result = ctrl.getProtocolBallotCounts();

		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0) instanceof ProtocolCountController.OrdinaryCount).isTrue();
		assertThat(result.get(0).getTitle()).isEqualTo("@count.votes.ordinary");
		assertThat(result.get(1) instanceof ProtocolCountController.QuestionableCount).isTrue();
		assertThat(result.get(1).getTitle()).isEqualTo("@count.votes.questionable");
	}

	@Test
	public void getBallotCountForOtherContests_whenNotHavingBallotCountForOtherContests_returnsEmptyList() {
		when(countStub.getBallotCountForOtherContests()).thenReturn(null);

		List<ProtocolCount> result = ctrl.getBallotCountForOtherContests();

		assertThat(result.size()).isEqualTo(0);
	}

	@Test
	public void getBallotCountForOtherContests_whenHavingBallotCountForOtherContests_returnsProtocolInList() {
		when(countStub.getBallotCountForOtherContests()).thenReturn(1);

		List<ProtocolCount> result = ctrl.getBallotCountForOtherContests();

		assertThat(result.size()).isEqualTo(1);
	}
}
