package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.counting.model.RejectedBallotCount;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BaseFinalCountControllerTest extends BaseCountControllerTest {

    private static final ElectionPath DEFAULT_CONTEST_PATH = new ElectionPath("730001.01.01.000001");

	private UpdateCounts updateCounts;
	private BaseFinalCountController ctrlDefault;
	private boolean isApproved;
	private boolean isPreviousApproved;
	private boolean isNextApproved;

	private List<FinalCount> countsStub;

	@BeforeMethod
	public void setUp() throws Exception {
		updateCounts = new UpdateCounts();
		ctrlDefault = initializeMocks(getBaseFinalCountController());
		isApproved = false;
		isPreviousApproved = true;
		isNextApproved = false;

		when(ctrlDefault.startCountingController.getCountCategory()).thenReturn(ctx().getCategory());
		when(ctrlDefault.startCountingController.getContestPath()).thenReturn(ctx().getContestPath());
		when(getUserDataMock().getOperatorAreaPath().getLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
		when(getCountsMock().getContext()).thenReturn(ctx());

        FinalCount finalCount = mock(FinalCount.class);
        when(getCountingServiceMock().saveCount(any(UserData.class), any(CountContext.class),
                any(FinalCount.class))).thenReturn(finalCount);

		countsStub = new ArrayList<>();
		countsStub.add(mock(FinalCount.class, RETURNS_DEEP_STUBS));
		countsStub.add(mock(FinalCount.class, RETURNS_DEEP_STUBS));
		countsStub.add(mock(FinalCount.class, RETURNS_DEEP_STUBS));
		for (FinalCount stub : countsStub) {
			when(stub.getBallotCounts()).thenReturn(mockList(1, BallotCount.class));
		}
		for (FinalCount stub : countsStub) {
			when(stub.getRejectedBallotCounts()).thenReturn(mockList(1, RejectedBallotCount.class));
		}
	}

	@Test
    public void createNewManualFinalCount_withScanningPrevious_createsNewManualCount() {
		for (FinalCount stub : countsStub) {
			when(stub.isManualCount()).thenReturn(false);
		}

		FinalCount finalCount = ctrlDefault.createNewManualFinalCount();

		assertThat(finalCount.isManualCount()).isTrue();
	}

	@Test
    public void doInit_withDefaultValues() {
		ctrlDefault.doInit();
	}

	@Test
    public void saveCount_withValidateError_shouldDisplayErrorMessage() {
		validateException().when(ctrlDefault.getFinalCount()).validate();

		ctrlDefault.saveCount();

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void saveCount_withValidData_checkState() {

		ctrlDefault.saveCount();

		verify(getMessageProviderMock()).get("@count.isSaved");
        verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
		assertThat(updateCounts.getIndex()).isEqualTo(2);
		assertThat(updateCounts.getCount().toString()).contains("Mock for FinalCount");
	}

	@Test
    public void modifiedBallotProcessed_withValidData_checkState() {

		ctrlDefault.modifiedBallotProcessed();

		verify(ctrlDefault.getFinalCount()).setModifiedBallotsProcessed(true);
		verify(getMessageProviderMock()).get("@count.isSaved");
        verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
		assertThat(updateCounts.getIndex()).isEqualTo(2);
		assertThat(updateCounts.getCount().toString()).contains("Mock for FinalCount");
	}

	@Test
    public void saveCountAndRegisterCountCorrections_withValidData_checkState() {

		String url = ctrlDefault.saveCountAndRegisterCountCorrections();

		assertThat(url).isEqualTo("/secure/counting/modifiedBallotsStatus.xhtml?faces-redirect=true");
		assertThat(updateCounts.getIndex()).isEqualTo(2);
		assertThat(updateCounts.getCount().toString()).contains("Mock for FinalCount");
	}

	@Test
    public void isReferendum_returnsTrue() {
		assertThat(ctrlDefault.isReferendum()).isTrue();
	}

	@Test
    public void newFinalCount_checkState() {
		ctrlDefault.newFinalCount();

		
		assertThat(updateCounts.getIndex()).isEqualTo(3);
		
		assertThat(updateCounts.getCount().getId()).contains("EVF4");
		assertThat(updateCounts.getCount().getBallotCounts().size()).isEqualTo(1);
		assertThat(updateCounts.getCount().getRejectedBallotCounts().size()).isEqualTo(1);
	}

	@Test
	public void button_revoke_returnsNotRendered() {
		Button button = ctrlDefault.button(ButtonType.REVOKE);

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test
	public void button_approve_returnsNotRendered() {
		Button button = ctrlDefault.button(ButtonType.APPROVE);

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test
	public void button_registerCorrectionsAndNoAccess_returnsNotRendered() {
		hasAccess(Accesses.Opptelling_Rettelser_Rediger, false);

		Button button = ctrlDefault.button(ButtonType.REGISTER_CORRECTIONS);

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test
	public void button_registerCorrectionsAndNoCorrections_returnsNotRendered() {
		hasAccess(Accesses.Opptelling_Rettelser_Rediger);

		Button button = ctrlDefault.button(ButtonType.REGISTER_CORRECTIONS);

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test
	public void button_registerCorrectionsAndNoEditable_returnsNotRendered() {
		hasAccess(Accesses.Opptelling_Rettelser_Rediger);
		when(ctrlDefault.getFinalCount().getBallotCounts().get(0).getModifiedCount()).thenReturn(1);

		Button button = ctrlDefault.button(ButtonType.REGISTER_CORRECTIONS);

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test
	public void button_registerCorrectionsAndHunkyDory_returnsRenderedAndEnabled() {
		hasAccess(Accesses.Opptelling_Rettelser_Rediger);
		when(ctrlDefault.getFinalCount().getBallotCounts().get(0).getModifiedCount()).thenReturn(1);
		when(ctrlDefault.getFinalCount().isManualCount()).thenReturn(true);
		when(ctrlDefault.getFinalCount().isEditable()).thenReturn(true);
		when(ctrlDefault.getFinalCount().isModifiedBallotsProcessed()).thenReturn(false);
		isApproved = false;
		isPreviousApproved = true;

		Button button = ctrlDefault.button(ButtonType.REGISTER_CORRECTIONS);

		assertThat(button.isDisabled()).isFalse();
		assertThat(button.isRendered()).isTrue();
	}

	@Test
	public void button_modifiedBallotProcessedAndNoAccess_returnsNotRendered() {
		hasAccess(Opptelling_Forhånd_Rediger, false);

		Button button = ctrlDefault.button(ButtonType.MODIFIED_BALLOT_PROCESSED);

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test
	public void button_modifiedBallotProcessedAndCorrections_returnsNotRendered() {
		hasAccess(Opptelling_Forhånd_Rediger);
		when(ctrlDefault.getFinalCount().getBallotCounts().get(0).getModifiedCount()).thenReturn(1);

		Button button = ctrlDefault.button(ButtonType.MODIFIED_BALLOT_PROCESSED);

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test
	public void button_modifiedBallotProcessedAndNoEditable_returnsNotRendered() {
		hasAccess(Opptelling_Forhånd_Rediger);

		Button button = ctrlDefault.button(ButtonType.MODIFIED_BALLOT_PROCESSED);

		assertThat(button.isDisabled()).isTrue();
		assertThat(button.isRendered()).isFalse();
	}

	@Test
	public void button_modifiedBallotProcessedAndHunkyDory_returnsRenderedAndEnabled() {
		hasAccess(Opptelling_Forhånd_Rediger);
		when(ctrlDefault.getFinalCount().isManualCount()).thenReturn(true);
		when(ctrlDefault.getFinalCount().isEditable()).thenReturn(true);
		when(ctrlDefault.getFinalCount().isModifiedBallotsProcessed()).thenReturn(false);
		isApproved = false;
		isPreviousApproved = true;

		Button button = ctrlDefault.button(ButtonType.MODIFIED_BALLOT_PROCESSED);

		assertThat(button.isDisabled()).isFalse();
		assertThat(button.isRendered()).isTrue();
	}

	@Test
    public void isCountEditable_withSuperNotEditable_shouldReturnFalse() {
		assertThat(ctrlDefault.isCountEditable()).isFalse();
	}

	@Test
	public void isCountEditable_withModifiedBallotBatch_shouldReturnFalse() throws Exception {
		when(ctrlDefault.getFinalCount().isEditable()).thenReturn(true);
		isApproved = false;
		isPreviousApproved = true;
		isNextApproved = false;
		mockFieldValue("hasModifiedBallotBatchForCurrentCount", true);

		assertThat(ctrlDefault.isCountEditable()).isFalse();

	}

	@Test
	public void isCountEditable_withModifiedBallotBatch_shouldReturnTrue() throws Exception {
		when(ctrlDefault.getFinalCount().isEditable()).thenReturn(true);
		isApproved = false;
		isPreviousApproved = true;
		isNextApproved = false;
		mockFieldValue("hasModifiedBallotBatchForCurrentCount", false);

		assertThat(ctrlDefault.isCountEditable()).isTrue();

	}

	@Test
    public void updateHasModifiedBallotBatchForCurrentCount_withNewCount() {
		when(ctrlDefault.getFinalCount().isNew()).thenReturn(true);

		ctrlDefault.updateHasModifiedBallotBatchForCurrentCount();

		verify(getModifiedBallotBatchServiceMock(), never()).hasModifiedBallotBatchForBallotCountPks(eq(getUserDataMock()), anyList());
	}

	private BaseFinalCountController getBaseFinalCountController() {
		return new BaseFinalCountController() {
			@Override
			List<FinalCount> getFinalCounts() {
				return countsStub;
			}

			@Override
			int getFinalCountIndex() {
				return 2;
			}

			@Override
			void updateCounts(int index, FinalCount finalCount) {
				updateCounts.register(index, finalCount);
			}

			@Override
			public boolean isApproved() {
				return isApproved;
			}

			@Override
			public boolean isPreviousApproved() {
				return isPreviousApproved;
			}

			@Override
			public boolean isNextApproved() {
				return isNextApproved;
			}
		};
	}

	private CountContext ctx() {
		return new CountContext(DEFAULT_CONTEST_PATH, CountCategory.VF);
	}

	public class UpdateCounts {
		private int index;
		private FinalCount count;

		public void register(int index, FinalCount count) {
			this.index = index;
			this.count = count;
		}

		public int getIndex() {
			return index;
		}

		public FinalCount getCount() {
			return count;
		}

		public void reset() {
			this.index = -1;
			this.count = null;
		}
	}

}
