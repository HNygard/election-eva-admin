package no.valg.eva.admin.frontend.election.ctrls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.election.MvElectionPickerTable;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MvElectionPickerControllerTest extends BaseFrontendTest {

	@Test
	public void getIsEditable_withClosedElectionEvent_returnsFalse() throws Exception {
		MvElectionPickerController ctrl = initializeMocks(MvElectionPickerController.class);
		stub_isCurrentElectionEventDisabled(true);
		overrideAccess(true);

		assertThat(ctrl.getIsEditable(ElectionLevelEnum.ELECTION_GROUP.getLevel(), null)).isFalse();
	}

	@Test
	public void getIsEditable_withAllAccess_returnsTrue() throws Exception {
		MvElectionPickerController ctrl = initializeMocks(MvElectionPickerController.class);
		stub_isCurrentElectionEventDisabled(false);
		overrideAccess(true);

		assertThat(ctrl.getIsEditable(ElectionLevelEnum.ELECTION_GROUP.getLevel(), null)).isTrue();
	}

	@Test(dataProvider = "getIsEditable")
	public void getIsEditable_withDataProvider_verifyExpected(ElectionLevelEnum electionLevel, ElectionEventStatusEnum status, boolean hasAccess,
			boolean expected) throws Exception {
		MvElectionPickerController ctrl = initializeMocks(MvElectionPickerController.class);
		stub_isCurrentElectionEventDisabled(false);
		stub_getElectionEventStatus(status);
		overrideAccess(false);
		hasAccess(hasAccess);
		MvElectionMinimal mvElection = createMock(MvElectionMinimal.class);

		assertThat(ctrl.getIsEditable(electionLevel.getLevel(), mvElection)).isEqualTo(expected);
	}

	@DataProvider(name = "getIsEditable")
	public Object[][] getIsEditable() {
		return new Object[][] {
				{ ElectionLevelEnum.NONE, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, false, false },
				{ ElectionLevelEnum.NONE, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.NONE, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.NONE, ElectionEventStatusEnum.LOCAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.NONE, ElectionEventStatusEnum.FINISHED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.NONE, ElectionEventStatusEnum.APPROVED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.NONE, ElectionEventStatusEnum.CLOSED, true, false },
				{ ElectionLevelEnum.ELECTION_EVENT, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, false, false },
				{ ElectionLevelEnum.ELECTION_EVENT, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION_EVENT, ElectionEventStatusEnum.LOCAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION_EVENT, ElectionEventStatusEnum.FINISHED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION_EVENT, ElectionEventStatusEnum.APPROVED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION_EVENT, ElectionEventStatusEnum.CLOSED, true, false },
				{ ElectionLevelEnum.ELECTION_GROUP, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, false, false },
				{ ElectionLevelEnum.ELECTION_GROUP, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION_GROUP, ElectionEventStatusEnum.LOCAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION_GROUP, ElectionEventStatusEnum.FINISHED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION_GROUP, ElectionEventStatusEnum.APPROVED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION_GROUP, ElectionEventStatusEnum.CLOSED, true, false },
				{ ElectionLevelEnum.ELECTION, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, false, false },
				{ ElectionLevelEnum.ELECTION, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION, ElectionEventStatusEnum.LOCAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION, ElectionEventStatusEnum.FINISHED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION, ElectionEventStatusEnum.APPROVED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.ELECTION, ElectionEventStatusEnum.CLOSED, true, false },
				{ ElectionLevelEnum.CONTEST, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, false, false },
				{ ElectionLevelEnum.CONTEST, ElectionEventStatusEnum.CENTRAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.CONTEST, ElectionEventStatusEnum.LOCAL_CONFIGURATION, true, false },
				{ ElectionLevelEnum.CONTEST, ElectionEventStatusEnum.FINISHED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.CONTEST, ElectionEventStatusEnum.APPROVED_CONFIGURATION, true, false },
				{ ElectionLevelEnum.CONTEST, ElectionEventStatusEnum.CLOSED, true, false }
		};
	}
	
	@Test(dataProvider = "isRenderPicker")
	public void isRenderPicker_withDataProvider_verifyExpected(boolean error, String mvElectionSelectionLevel, MvElection selectedMvElection, boolean expected)
			throws Exception {
		MvElectionPickerController ctrl = initializeMocks(MvElectionPickerController.class);
		MockUtils.setPrivateField(ctrl, "error", error);
		stubResolveExpression("mvElectionSelectionLevel", mvElectionSelectionLevel);
		ctrl.setSelectedMvElection(selectedMvElection);
		
		assertThat(ctrl.isRenderPicker()).isEqualTo(expected);
	}

	@DataProvider(name = "isRenderPicker")
	public Object[][] isRenderPicker() {
		return new Object[][] {
			{false, null, null, false},
			{true, null, null, false},
			{false, "1", null, true},
			{true, "1", null, false},
			{false, null, createMock(MvElection.class), false},
			{true, null, createMock(MvElection.class), false},
			{false, "1", createMock(MvElection.class), false},
			{true, "1", createMock(MvElection.class), false}
		};
	}

	@Test(dataProvider = "getHeaderKey")
	public void getHeaderKey_withDataProvider_verifyExpected(String mvElectionAllSelectable, String expected)
		throws Exception {
		MvElectionPickerController ctrl = initializeMocks(MvElectionPickerController.class);
		stubResolveExpression("mvElectionAllSelectable", mvElectionAllSelectable);
		stubResolveExpression("mvElectionSelectionLevel", "3");

		assertThat(ctrl.getHeaderKey()).isEqualTo(expected);
	}

	@DataProvider(name = "getHeaderKey")
	public Object[][] getHeaderKey() {
		return new Object[][] {
			{"true", "@election.common.election_level"},
			{"false", "@election_level[3].name"}
		};
	}

	@Test(dataProvider = "isRenderButton")
	public void isRenderButton_withDataProvider_verifyExpected(String mvElectionAllSelectable, String mvElectionSelectionLevel, boolean expected)
		throws Exception {
		MvElectionPickerController ctrl = initializeMocks(MvElectionPickerController.class);
		stubResolveExpression("mvElectionAllSelectable", mvElectionAllSelectable);
		stubResolveExpression("mvElectionSelectionLevel", mvElectionSelectionLevel);
		MvElectionPickerTable table = createMock(MvElectionPickerTable.class);
		when(table.getLevel()).thenReturn(2);

		assertThat(ctrl.isRenderButton(table)).isEqualTo(expected);
	}

	@DataProvider(name = "isRenderButton")
	public Object[][] isRenderButton() {
		return new Object[][] {
			{"true", "1", true},
			{"false", "1", false},
			{"false", "2", true}
		};
	}

	@Test(dataProvider = "isDisabledButton")
	public void isDisabledButton_withDataProvider_verifyExpected(boolean disableSelect, MvElectionMinimal mvElection, boolean expected)
		throws Exception {
		MvElectionPickerController ctrl = initializeMocks(MvElectionPickerController.class);
		stubResolveExpression("disableSelect", disableSelect);
		MvElectionPickerTable table = createMock(MvElectionPickerTable.class);
		when(table.getSelectedMvElection()).thenReturn(mvElection);

		assertThat(ctrl.isDisabledButton(table)).isEqualTo(expected);
	}

	@DataProvider(name = "isDisabledButton")
	public Object[][] isDisabledButton() {
		return new Object[][] {
			{false, null, true},
			{true, null, true},
			{false, createMock(MvElectionMinimal.class), false},
			{true, createMock(MvElectionMinimal.class), true},
		};
	}

	private void overrideAccess(boolean value) {
		when(getInjectMock(UserDataController.class).isOverrideAccess()).thenReturn(value);
	}

	private void hasAccess(boolean value) {
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonValgValggruppe()).thenReturn(value);
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonValgValg()).thenReturn(value);
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonValgValgdistrikt()).thenReturn(value);
	}

	private void stub_isCurrentElectionEventDisabled(boolean value) {
		when(getInjectMock(UserDataController.class).isCurrentElectionEventDisabled()).thenReturn(value);
	}

	private void stub_getElectionEventStatus(ElectionEventStatusEnum status) {
		when(getInjectMock(UserDataController.class).getElectionEvent().getElectionEventStatus().getId()).thenReturn(status.id());
	}

	@Override
	protected void stubResolveExpression(String name, Object value) {
		super.stubResolveExpression("#{cc.attrs." + name + "}", value);
	}
}
