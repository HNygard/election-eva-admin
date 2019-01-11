package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolAndPreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.Collections;
import java.util.List;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Opphev_Foreløpig_Telling;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ProtocolAndPreliminaryCountControllerTest extends BaseCountControllerTest {

	private ProtocolAndPreliminaryCountController ctrl;
	private ProtocolAndPreliminaryCount countStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrl = initializeMocks(ProtocolAndPreliminaryCountController.class);
		countStub = mock(ProtocolAndPreliminaryCount.class);
		when(getCountsMock().getProtocolAndPreliminaryCount()).thenReturn(countStub);
        when(countStub.getProtocolCount()).thenReturn(mock(ProtocolCount.class));
        when(countStub.getPreliminaryCount()).thenReturn(mock(PreliminaryCount.class));
		when(getStartCountingControllerMock().getContestPath()).thenReturn(ElectionPath.from("111111.11.11.111111"));
		when(getStartCountingControllerMock().getCountCategory()).thenReturn(VO);
		ctrl.doInit();
	}

	@Test
    public void doInit_withCount_checkState() {
		ctrl.doInit();

		assertThat(ctrl.getProtocolAndPreliminaryCount()).isNotNull();
		assertThat(ctrl.getProtocolAndPreliminaryCount()).isSameAs(ctrl.getCount());
	}

	@Test
    public void saveCount_withValidateError_shouldDisplayErrorMessage() {
		validateException().when(countStub).validate();

		ctrl.saveCount();

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void saveCount_withValidData_checkState() {
		ProtocolAndPreliminaryCount response = mock(ProtocolAndPreliminaryCount.class);
		when(getCountingServiceMock().saveCount(any(UserData.class), any(CountContext.class), any(ProtocolAndPreliminaryCount.class))).thenReturn(response);

		ctrl.saveCount();

		verify(getCountsMock()).setProtocolAndPreliminaryCount(response);
		verify(getCountsMock()).setFirstProtocolCount(any(ProtocolCount.class));
		verify(getCountsMock()).setPreliminaryCount(any(PreliminaryCount.class));
		verify(getMessageProviderMock()).get("@count.isSaved");
        verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
	}

	@Test
    public void approveCount_withValidateError_shouldDisplayErrorMessage() {
		validateException().when(countStub).validateForApproval();

		ctrl.approveCount();

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void approveCount_withValidData_checkState() {
		ProtocolAndPreliminaryCount response = mock(ProtocolAndPreliminaryCount.class);
		when(getCountingServiceMock().approveCount(any(UserData.class), any(CountContext.class), any(ProtocolAndPreliminaryCount.class))).thenReturn(response);

		ctrl.approveCount();

		verify(getCountsMock()).setProtocolAndPreliminaryCount(response);
		verify(getCountsMock()).setFirstProtocolCount(any(ProtocolCount.class));
		verify(getCountsMock()).setPreliminaryCount(any(PreliminaryCount.class));
		verify(getMessageProviderMock()).get("@count.isApproved");
        verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
	}

	@Test
    public void revokeApprovedCount_withException_shouldDisplayErrorMessage() {
		validateException().when(getCountingServiceMock()).revokeCount(any(UserData.class), any(CountContext.class),
				any(ProtocolAndPreliminaryCount.class));

		ctrl.revokeApprovedCount();

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void revokeApprovedCount_withValidData_checkState() {
		ProtocolAndPreliminaryCount response = mock(ProtocolAndPreliminaryCount.class);
		when(getCountingServiceMock().revokeCount(any(UserData.class), any(CountContext.class), any(ProtocolAndPreliminaryCount.class))).thenReturn(response);

		ctrl.revokeApprovedCount();

		verify(getCountsMock()).setProtocolAndPreliminaryCount(response);
		verify(getCountsMock()).setFirstProtocolCount(any(ProtocolCount.class));
		verify(getCountsMock()).setPreliminaryCount(any(PreliminaryCount.class));
		verify(getMessageProviderMock()).get("@count.isNotApprovedAnymore");
        verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
	}

	@Test
    public void isApproved_withNoCount_returnsFalse() {
		when(getCountsMock().getProtocolAndPreliminaryCount()).thenReturn(null);

		assertThat(ctrl.isApproved()).isFalse();
	}

	@Test
    public void isApproved_withApprovedCount_returnsTrue() {
		when(countStub.isApproved()).thenReturn(true);

		assertThat(ctrl.isApproved()).isTrue();
	}

	@Test
    public void button_withSaveFalse_returnDisabled() {
		Button button = ctrl.button(ButtonType.SAVE);

		assertThat(button.isDisabled()).isTrue();
	}

	@Test
    public void button_withSaveTrue_returnEnabled() {
		hasAccess(Opptelling_Forhånd_Rediger);
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
		hasAccess(Opptelling_Forhånd_Rediger);
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
    public void getDailyMarkOffCounts_withCount_returnsMarkoffs() {
        when(countStub.getDailyMarkOffCounts()).thenReturn(new DailyMarkOffCounts(Collections.singletonList(new DailyMarkOffCount(LocalDate.now(), 0))));

		List<DailyMarkOffCount> list = ctrl.getDailyMarkOffCounts();

		assertThat(list).isNotNull();
		assertThat(list.size()).isEqualTo(1);
	}

	@Test
    public void getDailyMarkOffCounts_withNoCount_returnsEmptyList() {
		when(getCountsMock().getProtocolAndPreliminaryCount()).thenReturn(null);
		ctrl.doInit();

		List<DailyMarkOffCount> list = ctrl.getDailyMarkOffCounts();

		assertThat(list).isNotNull();
		assertThat(list.size()).isEqualTo(0);
	}

}
