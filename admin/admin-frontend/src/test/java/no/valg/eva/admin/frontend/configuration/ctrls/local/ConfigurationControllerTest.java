package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import javax.faces.application.FacesMessage;

import no.evote.exception.ReadOnlyPrivilegeException;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.AdvancePollingPlace;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void getIcon_withDoneStatus_returnsCheckmark() throws Exception {
		ConfigurationController ctrl = ctrl();
		ctrl.setDoneStatus(true);

		assertThat(ctrl.getIcon()).isEqualTo("eva-icon-checkmark completed");
	}

	@Test
	public void getIcon_withUndoneStatus_returnsWarning() throws Exception {
		ConfigurationController ctrl = ctrl();
		ctrl.setDoneStatus(false);

		assertThat(ctrl.getIcon()).isEqualTo("eva-icon-warning");
	}

	@Test
	public void getIcon_withRootAreaLevel_returnsEmptyString() throws Exception {
		ConfigurationController ctrl = ctrlWithAreaPath(ROOT);

		assertThat(ctrl.getIcon()).isEmpty();
	}

	@Test(dataProvider = "saveDone")
	public void saveDone_withDataProvider_verifyExpected(MvAreaBuilder mvArea) throws Exception {
		ConfigurationController ctrl = ctrl(mvArea);

		ctrl.saveDone();

		verifySaveConfigStatus();
	}

	@Test
	public void saveDone_withError_returnsFalse() throws Exception {
		MyConfigurationController ctrl = (MyConfigurationController) ctrl();
		ctrl.setDoneStatus(true);
		doThrow(new ReadOnlyPrivilegeException("")).when(ctrl.getMainController()).saveConfigStatuses();

		assertThat(ctrl.saveDone(false)).isFalse();
		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@common.message.evote_application_exception.READ_ONLY_PRIVILEGE");
	}

	@DataProvider(name = "saveDone")
	public Object[][] saveDone() {
		return new Object[][] {
				{ mvArea(COUNTY) },
				{ mvArea(MUNICIPALITY) }
		};
	}

	@Test
	public void saveDone_withUnchangedValue_verifyNoUpdateOperation() throws Exception {
		ConfigurationController ctrl = ctrl();
		ctrl.setDoneStatus(true);

		ctrl.saveDone(true);

		verifySaveConfigStatus(false);
	}

	@Test
	public void saveDone_withReOpenAndClosedChildren_verifyReopenChildren() throws Exception {
		ConfigurationController ctrl = ctrl();
		ctrl.setDoneStatus(true);
		when(ctrl.getController(AdvancePollingPlacesConfigurationController.class).isDoneStatus()).thenReturn(true);
		when(ctrl.getController(ElectionDayPollingPlacesConfigurationController.class).isDoneStatus()).thenReturn(true);

		ctrl.saveDone(false);

		verifySaveConfigStatus();
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@config.local.childrenReopened, ChildConfigurationController]");
	}

	@Test(dataProvider = "isWriteMode")
	public void isWriteMode_withDataProvider_verifyExpected(ConfigurationMode mode, boolean expected) throws Exception {
		ConfigurationController ctrl = ctrl();

		ctrl.setMode(mode);

		assertThat(ctrl.isWriteMode()).isEqualTo(expected);
	}

	@DataProvider(name = "isWriteMode")
	public Object[][] isWriteMode() {
		return new Object[][] {
				{ ConfigurationMode.READ, false },
				{ ConfigurationMode.CREATE, true },
				{ ConfigurationMode.UPDATE, true },
				{ ConfigurationMode.DELETE, false }
		};
	}

	@Test(dataProvider = "isReadMode")
	public void isReadMode_withDataProvider_verifyExpected(ConfigurationMode mode, boolean expected) throws Exception {
		ConfigurationController ctrl = ctrl();

		ctrl.setMode(mode);

		assertThat(ctrl.isReadMode()).isEqualTo(expected);
	}

	@DataProvider(name = "isReadMode")
	public Object[][] isReadMode() {
		return new Object[][] {
				{ ConfigurationMode.READ, true },
				{ ConfigurationMode.CREATE, false },
				{ ConfigurationMode.UPDATE, false },
				{ ConfigurationMode.DELETE, false }
		};
	}

	@Test
	public void cancelWrite_setsModeToRead() throws Exception {
		ConfigurationController ctrl = ctrl();

		ctrl.cancelWrite();

		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
	}

	@Test(dataProvider = "isEditable")
	public void isEditable_withDataProvider_verifyExpected(MvAreaBuilder mvArea, IsEditableTest test, boolean expected) throws Exception {
		ConfigurationController ctrl = ctrl(mvArea);
		stub_isApproved(ctrl, test.isApproved());
		stub_isLocalConfigurationStatus(ctrl, test.isLocalConfigurationStatus());
		stub_requiresDoneBeforeEditOK(ctrl, test.isRequiresDoneBeforeEditOK());
		when(getInjectMock(UserDataController.class).isOverrideAccess()).thenReturn(test.isOverrideAccess());
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataRedigere()).thenReturn(true);

		assertThat(ctrl.isEditable()).isEqualTo(expected);
	}

	static final IsEditableTest WITH_OVERRIDE_ACCESS_AND_NOT_APPROVED = new IsEditableTest(false, true, false, true);
	static final IsEditableTest WITH_OVERRIDE_ACCESS_AND_NOT_APPROVED_AND_NOT_REQUIRES_DONE = new IsEditableTest(false, true, false, false);
	static final IsEditableTest WITH_NOT_LOCAL_STATUS_AND_NOT_REQUIRES_DONE = new IsEditableTest(false, false, false, false);
	static final IsEditableTest WITH_LOCAL_STATUS_AND_NOT_REQUIRES_DONE = new IsEditableTest(false, false, true, false);
	static final IsEditableTest WITH_LOCAL_STATUS_AND_REQUIRES_DONE_OK = new IsEditableTest(false, false, true, true);

	@DataProvider(name = "isEditable")
	public Object[][] isEditable() {
		return new Object[][] {
				{ mvArea(COUNTY), WITH_OVERRIDE_ACCESS_AND_NOT_APPROVED, true },
				{ mvArea(COUNTY), WITH_OVERRIDE_ACCESS_AND_NOT_APPROVED_AND_NOT_REQUIRES_DONE, false },
				{ mvArea(MUNICIPALITY), WITH_NOT_LOCAL_STATUS_AND_NOT_REQUIRES_DONE, false },
				{ mvArea(MUNICIPALITY), WITH_LOCAL_STATUS_AND_NOT_REQUIRES_DONE, false },
				{ mvArea(MUNICIPALITY), WITH_LOCAL_STATUS_AND_REQUIRES_DONE_OK, true }

		};
	}

	@Test(dataProvider = "button")
	public void button_withDataProvider_verifyExpected(ButtonType buttonType, ButtonTest test, boolean isRendered, boolean isEnabled)
			throws Exception {
		ConfigurationController ctrl = ctrl();
		stub_isEditable(ctrl, test.isEditable());
		stub_requiresDoneBeforeDoneOK(ctrl, test.isRequiresDoneBeforeDoneOk());
		ctrl.setDoneStatus(test.isDoneStatus());

		Button button = ctrl.button(buttonType);

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(!isEnabled);
	}

	static final ButtonTest WITH_NO_EDITABLE = new ButtonTest(false, false, false);
	static final ButtonTest WITH_DONE_STATUS = new ButtonTest(true, true, false);
	static final ButtonTest WITH_REQUIRES_DONE_NOT_OK = new ButtonTest(true, false, false);
	static final ButtonTest WITH_POSITIVES = new ButtonTest(true, false, true);

	@DataProvider(name = "button")
	public Object[][] button() {
		return new Object[][] {
				{ ButtonType.DONE, WITH_NO_EDITABLE, true, false },
				{ ButtonType.DONE, WITH_DONE_STATUS, true, false },
				{ ButtonType.DONE, WITH_REQUIRES_DONE_NOT_OK, true, false },
				{ ButtonType.DONE, WITH_POSITIVES, true, true },
				{ ButtonType.APPROVE, WITH_POSITIVES, false, false }
		};
	}

	@Test
	public void getSelectId_withPlace_returnsAbbreviatedIdAndName() throws Exception {
		ConfigurationController ctrl = ctrl();
		AdvancePollingPlace place = new AdvancePollingPlace(MUNICIPALITY);
		place.setId("0101");
		place.setName("a very very very very very very long name");

		assertThat(ctrl.getSelectId(place)).isEqualTo("0101-a very very very very very ...");
	}

	@Test
	public void isEditable_withNoArea_returnsFalse() throws Exception {
		ConfigurationController ctrl = ctrl(null);

		assertThat(ctrl.isEditable()).isFalse();
	}

	@Test
	public void getAreaPath_withMvArea_returnsCorrectPath() throws Exception {
		ConfigurationController ctrl = ctrl();

		assertThat(ctrl.getAreaPath()).isEqualTo(MUNICIPALITY);
	}

	@Test
	public void unsupportedLevel_withMvArea_returnsException() throws Exception {
		ConfigurationController ctrl = ctrl(new MvAreaBuilder(MUNICIPALITY.toCountyPath()));

		IllegalStateException e = ctrl.unsupportedLevel();

		assertThat(e.getMessage()).isEqualTo("Invalid level 2 found in MyConfigurationController");
	}

	@Test
	public void getConfirmDeleteMessage_withNull_returnsQuestionMark() throws Exception {
		ConfigurationController ctrl = ctrl();

		assertThat(ctrl.getConfirmDeleteMessage(null)).isEqualTo("?");
	}

	@Test
	public void getConfirmDeleteMessage_withDisplayable_returnsQuestionMark() throws Exception {
		ConfigurationController ctrl = ctrl();

		assertThat(ctrl.getConfirmDeleteMessage(() -> "Hello")).isEqualTo(
				"[@common.displayable.deleteConfirm, @common.displayable.configurationcontrollertest Hello]");
	}

	@Test
	public void isUseElectronicMarkoffsConfigured_withDoneElectronicMarkoff_returnsTrue() throws Exception {
		ConfigurationController ctrl = ctrl();
		when(ctrl.getMainController().getMunicipalityStatus().isUseElectronicMarkoffs()).thenReturn(true);
		when(ctrl.getMainController().getMunicipalityStatus().isElectronicMarkoffs()).thenReturn(true);

		assertThat(ctrl.isUseElectronicMarkoffsConfigured()).isTrue();
	}

	@Test
	public void getController_withValidController_returnsControllerInstance() throws Exception {
		ConfigurationController ctrl = ctrl();

		assertThat(ctrl.getController(AdvancePollingPlacesConfigurationController.class)).isNotNull();
	}

	@Test
	public void getController_withInvalidController_returnsNull() throws Exception {
		ConfigurationController ctrl = ctrl();

		assertThat(ctrl.getController(LanguageConfigurationController.class)).isNull();
	}

	@Test
	public void checkRequiresDoneBeforeEdit_withMessagesAndNotOk_returnsMessage() throws Exception {
		ConfigurationController ctrl = ctrl();
		AdvancePollingPlacesConfigurationController parent = ctrl.getController(AdvancePollingPlacesConfigurationController.class);
		when(parent.isDoneStatus()).thenReturn(false);

		ctrl.checkRequiresDoneBeforeEdit();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@config.local.checkRequiresDoneBeforeEdit, @config.local.accordion.advance_polling_place.name, "
				+ "MyConfigurationController]");
	}

	@Test
	public void checkRequiresDoneBeforeDone_withMessagesAndNotOk_returnsMessage() throws Exception {
		ConfigurationController ctrl = ctrl();
		ElectionDayPollingPlacesConfigurationController parent = ctrl.getController(ElectionDayPollingPlacesConfigurationController.class);
		when(parent.isDoneStatus()).thenReturn(false);

		ctrl.checkRequiresDoneBeforeDone();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@config.local.checkRequiresDoneBeforeDone, @config.local.accordion.election_day_polling_place.name, "
				+ "MyConfigurationController]");
	}

	@Test
	public void getBaseId_withActiveIndex0_verifyResult() throws Exception {
		ConfigurationController ctrl = ctrl();

		assertThat(ctrl.getWithBaseId("test")).isEqualTo("configurationPanel:0:test");
	}

	@Test
	public void setUpdateMode_modeIsSetToUpdate() throws Exception {
		ConfigurationController ctrl = ctrl();

		ctrl.setUpdateMode();

		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.UPDATE);
	}

	private void stub_isLocalConfigurationStatus(ConfigurationController ctrl, boolean isLocalConfigurationStatus) {
		when(getInjectMock(UserDataController.class).isLocalConfigurationStatus()).thenReturn(isLocalConfigurationStatus);
		if (ctrl.isCountyLevel()) {
			when(ctrl.getMvArea().getCounty().isLocalConfigurationStatus()).thenReturn(isLocalConfigurationStatus);
		}
		if (ctrl.isMunicipalityLevel()) {
			when(ctrl.getMvArea().getMunicipality().isLocalConfigurationStatus()).thenReturn(isLocalConfigurationStatus);
		}
	}

	private void stub_requiresDoneBeforeEditOK(ConfigurationController ctrl, boolean requiresDoneBeforeEditOK) {
		AdvancePollingPlacesConfigurationController placeCtrl = ctrl.getController(AdvancePollingPlacesConfigurationController.class);
		when(placeCtrl.isDoneStatus()).thenReturn(requiresDoneBeforeEditOK);
	}

	private void stub_requiresDoneBeforeDoneOK(ConfigurationController ctrl, boolean requiresDoneBeforeDoneOK) {
		ElectionDayPollingPlacesConfigurationController placeCtrl = ctrl.getController(ElectionDayPollingPlacesConfigurationController.class);
		when(placeCtrl.isDoneStatus()).thenReturn(requiresDoneBeforeDoneOK);
	}

	private void stub_isApproved(ConfigurationController ctrl, boolean isApproved) {
		if (ctrl.isCountyLevel()) {
			when(ctrl.getMvArea().getCounty().isApprovedConfigurationStatus()).thenReturn(isApproved);
		}
		if (ctrl.isMunicipalityLevel()) {
			when(ctrl.getMvArea().getMunicipality().isApprovedConfigurationStatus()).thenReturn(isApproved);
		}
	}

	private void stub_isEditable(ConfigurationController ctrl, boolean isEditable) {
		stub_isApproved(ctrl, isEditable);
		stub_isLocalConfigurationStatus(ctrl, isEditable);
		stub_requiresDoneBeforeEditOK(ctrl, isEditable);
		when(getInjectMock(UserDataController.class).isOverrideAccess()).thenReturn(isEditable);
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataGodkjenne()).thenReturn(isEditable);
	}

	private ConfigurationController ctrl() throws Exception {
		return ctrlWithAreaPath(MUNICIPALITY);
	}

	private ConfigurationController ctrlWithAreaPath(AreaPath areaPath) throws Exception {
		return setupParents(ctrl(initializeMocks(new MyConfigurationController()), areaPath));
	}

	private ConfigurationController ctrl(MvAreaBuilder mvAreaBuilder) throws Exception {
		if (mvAreaBuilder == null) {
			MvArea mvArea = null;
			return setupParents(ctrl(localConfigurationController(mvArea), initializeMocks(new MyConfigurationController())));
		}
		return setupParents(ctrl(localConfigurationController(mvAreaBuilder.getValue()), initializeMocks(new MyConfigurationController())));
	}

	private ConfigurationController setupParents(ConfigurationController ctrl) {
		addControllerToMainController(ctrl, AdvancePollingPlacesConfigurationController.class);
		addControllerToMainController(ctrl, ElectionDayPollingPlacesConfigurationController.class);
		addControllerToMainController(ctrl, new ChildConfigurationController());
		when(ctrl.getController(AdvancePollingPlacesConfigurationController.class).getName())
				.thenReturn(new AdvancePollingPlacesConfigurationController().getName());
		when(ctrl.getController(ElectionDayPollingPlacesConfigurationController.class).getName())
				.thenReturn(new ElectionDayPollingPlacesConfigurationController().getName());
		addControllerToMainController(ctrl, ElectionDayPollingPlacesConfigurationController.class);
		return ctrl;
	}

	class MyConfigurationController extends ConfigurationController {

		private boolean doneStatus;

		@Override
		public void init() {

		}

		@Override
		public String getName() {
			return "MyConfigurationController";
		}

		@Override
		public boolean hasAccess() {
			return false;
		}

		@Override
		public boolean isDoneStatus() {
			return doneStatus;
		}

		@Override
		public void setDoneStatus(boolean status) {
			this.doneStatus = status;
		}

		@Override
		public boolean canBeSetToDone() {
			return true;
		}

		@Override
		public ConfigurationView getView() {
			return ConfigurationView.ADVANCE_POLLING_PLACES;
		}

		@Override
		Class<? extends ConfigurationController>[] getRequiresDoneBeforeEdit() {
			return new Class[] { AdvancePollingPlacesConfigurationController.class };
		}

		@Override
		Class<? extends ConfigurationController>[] getRequiresDoneBeforeDone() {
			return new Class[] { ElectionDayPollingPlacesConfigurationController.class };
		}
	}

	class ChildConfigurationController extends ConfigurationController {
		@Override
		public void init() {

		}

		@Override
		boolean saveDone(boolean finished) {
			return true;
		}

		@Override
		public ConfigurationView getView() {
			return null;
		}

		@Override
		public String getName() {
			return "ChildConfigurationController";
		}

		@Override
		public boolean hasAccess() {
			return false;
		}

		@Override
		public void setDoneStatus(boolean value) {

		}

		@Override
		public boolean isDoneStatus() {
			return true;
		}

		@Override
		public boolean canBeSetToDone() {
			return false;
		}

		@Override
		Class<? extends ConfigurationController>[] getRequiresDoneBeforeDone() {
			return new Class[] { MyConfigurationController.class };
		}
	}

	static class ButtonTest {
		private boolean editable;
		private boolean doneStatus;
		private boolean requiresDoneBeforeDoneOk;

		public ButtonTest(boolean editable, boolean doneStatus, boolean requiresDoneBeforeDoneOk) {
			this.editable = editable;
			this.doneStatus = doneStatus;
			this.requiresDoneBeforeDoneOk = requiresDoneBeforeDoneOk;
		}

		public boolean isEditable() {
			return editable;
		}

		public boolean isDoneStatus() {
			return doneStatus;
		}

		public boolean isRequiresDoneBeforeDoneOk() {
			return requiresDoneBeforeDoneOk;
		}
	}

	static class IsEditableTest {
		private boolean isApproved;
		private boolean isOverrideAccess;
		private boolean isLocalConfigurationStatus;
		private boolean requiresDoneBeforeEditOK;

		IsEditableTest(boolean isApproved, boolean isOverrideAccess, boolean isLocalConfigurationStatus, boolean requiresDoneBeforeEditOK) {
			this.isApproved = isApproved;
			this.isOverrideAccess = isOverrideAccess;
			this.isLocalConfigurationStatus = isLocalConfigurationStatus;
			this.requiresDoneBeforeEditOK = requiresDoneBeforeEditOK;
		}

		public boolean isApproved() {
			return isApproved;
		}

		public boolean isOverrideAccess() {
			return isOverrideAccess;
		}

		public boolean isLocalConfigurationStatus() {
			return isLocalConfigurationStatus;
		}

		public boolean isRequiresDoneBeforeEditOK() {
			return requiresDoneBeforeEditOK;
		}
	}
}
