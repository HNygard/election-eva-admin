package no.valg.eva.admin.frontend.counting.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.counting.ctrls.CompareCountsController;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CompareCountsSelectTest extends BaseFrontendTest {

	private CompareCountsSelect select;
	private CompareCountsController ctrlStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrlStub = createMock(CompareCountsController.class);
		select = new CompareCountsSelect(ctrlStub, "first");

	}

	@Test
	public void approve_shouldCallCtrlApproveWithThis() throws Exception {
		select.approve();

		verify(ctrlStub).approve(select);
	}

	@Test
	public void openConfirmApproveCountDialog_verifyCtrlExecutions() throws Exception {
		select.openConfirmApproveCountDialog();

		verify(ctrlStub).setCurrentCountsSelect(select);
		verify_open(ctrlStub.getApproveDialog());
	}

	@Test
	public void saveComment_shouldExecuteCtrlSaveComment() throws Exception {
		select.saveComment();

		verify(ctrlStub).saveComment(select);
	}

	@Test
	public void revoke_shouldExecuteCtrlReject() throws Exception {
		select.revoke();

		verify(ctrlStub).revoke(select);
	}

	@Test
	public void getFinalCount_shouldExecuteCtrlGetFinalCount() throws Exception {
		FinalCount stub = mock(FinalCount.class);
		when(ctrlStub.getCount(select)).thenReturn(stub);

		assertThat(select.getFinalCount()).isSameAs(stub);
	}

	@Test
	public void hasFinalCount_returnsTrue() throws Exception {
		when(ctrlStub.getCount(select)).thenReturn(mock(FinalCount.class));

		assertThat(select.hasFinalCount()).isTrue();
	}

	@Test
	public void isFinalCountApproved_returnsTrue() throws Exception {
		FinalCount stub = mock(FinalCount.class);
		when(ctrlStub.getCount(select)).thenReturn(stub);
		when(stub.isApproved()).thenReturn(true);

		assertThat(select.isFinalCountApproved()).isTrue();
	}

	@Test
	public void getApproveButton_shouldExecuteCtrlGetApproveButton() throws Exception {
		Button stub = mock(Button.class);
		when(ctrlStub.getApproveButton(select)).thenReturn(stub);

		assertThat(select.getApproveButton()).isSameAs(stub);
	}

	@Test
	public void getRevokeButton_shouldExecuteCtrlGetRevokeButton() throws Exception {
		Button stub = mock(Button.class);
		when(ctrlStub.getRevokeButton(select)).thenReturn(stub);

		assertThat(select.getRevokeButton()).isSameAs(stub);
	}

	@Test
	public void confirmConfirmApproveCountDialog_verifyApproveAndCloseDialog() throws Exception {
		select.confirmConfirmApproveCountDialog();

		verify(ctrlStub).approve(select);
		verify_closeAndUpdate(ctrlStub.getApproveDialog(), "countingForm");
	}

	@Test
	public void cancelConfirmApproveCountDialog_verifyCloseDialog() throws Exception {
		select.cancelConfirmApproveCountDialog();

		verify_close(ctrlStub.getApproveDialog());
	}
}
