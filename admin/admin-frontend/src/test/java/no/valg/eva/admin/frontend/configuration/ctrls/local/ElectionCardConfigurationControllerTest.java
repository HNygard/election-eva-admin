package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.model.local.ReportingUnit;
import no.valg.eva.admin.common.configuration.service.ElectionCardConfigService;
import no.valg.eva.admin.felles.bakgrunnsjobb.service.BakgrunnsjobbService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.configuration.models.ElectionCardModel;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.READ;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.UPDATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElectionCardConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_verifyState() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();

		assertThat(ctrl.getRootTreeNode()).isNotNull();
		assertThat(ctrl.getRootTreeNode().getChildren()).hasSize(1);
		ElectionCardModel parent = (ElectionCardModel) ctrl.getRootTreeNode().getChildren().get(0).getData();
		assertThat(parent).isNotNull();
		assertThat(parent.getReportingUnit()).isNotNull();
		assertThat(parent.getPollingPlace()).isNull();
		assertThat(ctrl.getRootTreeNode().getChildren().get(0).getChildren()).hasSize(2);
		ElectionCardModel child = (ElectionCardModel) ctrl.getRootTreeNode().getChildren().get(0).getChildren().get(0).getData();
		assertThat(child).isNotNull();
		assertThat(child.getReportingUnit()).isNotNull();
		assertThat(child.getPollingPlace()).isNotNull();
		assertThat(ctrl.getSelected()).isNotNull();
	}

	@Test
	public void init_medManntallsnummerKjort_girMelding() throws Exception {
		ctrl(true);

		assertFacesMessage(SEVERITY_ERROR, "@config.local.manntallsnummerErGenerert");
	}

	@Test
	public void isEditable_medManntallsnummerKjort_returnererFalse() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl(null, false, null, true);

		assertThat(ctrl.isEditable()).isFalse();
	}

	@Test
	public void getView_returnsElectionCardView() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.ELECTION_CARD);
	}

	@Test
	public void getName_returnsElectionCardName() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.election_card.name");
	}

	@Test
	public void setDoneStatus_verifySetStatus() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();

		ctrl.setDoneStatus(true);

		verify(ctrl.getMunicipalityConfigStatus()).setElectionCard(true);
	}

	@Test
	public void isDoneStatus_withMunicipalityStatusTrue_returnsTrue() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();
		when(ctrl.getMunicipalityConfigStatus().isElectionCard()).thenReturn(true);

		assertThat(ctrl.isDoneStatus()).isTrue();
	}

	@Test(dataProvider = "canBeSetToDone")
    public void canBeSetToDone_withDataProvider_verifyExpected(ElectionCardConfigurationController ctrl, boolean expected) {

		assertThat(ctrl.canBeSetToDone()).isEqualTo(expected);
	}

	@DataProvider(name = "canBeSetToDone")
	public Object[][] canBeSetToDone() throws Exception {
		return new Object[][] {
				{ ctrl(true, true), false },
				{ ctrl(true, false), true },
				{ ctrl(true, false, true, true), true }
		};
	}

	@Test
	public void isAddressEditable_withSelectedParent_returnsTrue() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();
		ctrl.setMode(UPDATE);

		assertThat(ctrl.isAddressEditable()).isTrue();
	}

	@Test(dataProvider = "button")
	public void button_withDataProvider_verifyExpected(ButtonType buttonType, ConfigurationMode mode,
			boolean isEditable, boolean isRendered, boolean isDisabled) throws Exception {
		ElectionCardConfigurationController ctrl = ctrl(isEditable, false, true, true);
		ctrl.setMode(mode);

		Button button = ctrl.button(buttonType);

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	@DataProvider(name = "button")
	public Object[][] button() {
		return new Object[][] {
				{ ButtonType.UPDATE, UPDATE, true, false, true },
				{ ButtonType.UPDATE, READ, false, true, true },
				{ ButtonType.UPDATE, READ, true, true, false },
				{ ButtonType.EXECUTE_UPDATE, READ, true, false, true },
				{ ButtonType.EXECUTE_UPDATE, UPDATE, false, true, true },
				{ ButtonType.EXECUTE_UPDATE, UPDATE, true, true, false },
				{ ButtonType.CANCEL, READ, true, false, true },
				{ ButtonType.CANCEL, UPDATE, false, true, true },
				{ ButtonType.CANCEL, UPDATE, true, true, false },
				{ ButtonType.DONE, READ, true, true, false }
		};
	}

	@Test
	public void saveDone_withEditable_cardsShouldBeSavedWithParentInfoText() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();

		ctrl.saveDone();

		verifySaveConfigStatus();
		TreeNode parentNode = ctrl.getRootTreeNode().getChildren().get(0);
		ElectionCardModel childWithCustomInfoText = ctrl.getElectionCardModel(parentNode.getChildren().get(1));
		assertThat(childWithCustomInfoText.getElectionCardInfoText()).isEqualTo("Info text other");
		verify(getInjectMock(ElectionCardConfigService.class)).save(eq(getUserDataMock()), any(ElectionCardConfig.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.electioncardconfig null]");
	}

	@Test
    public void infoText_semicolonIsInvalid() {
		ElectionCardModel ecm = createMock(ElectionCardModel.class);
		when(ecm.getElectionCardInfoText()).thenReturn("My semi;colon text");

		ElectionCardConfigurationController ctrl = spy(new ElectionCardConfigurationController());
		when(ctrl.getElectionCardModel(any())).thenReturn(ecm);

		assertThat(ctrl.electionTextIsInvalid()).isTrue();
	}

	@Test
    public void infoText_nullTextIsValid() {
		ElectionCardModel ecm = createMock(ElectionCardModel.class);
		when(ecm.getElectionCardInfoText()).thenReturn(null);

		ElectionCardConfigurationController ctrl = spy(new ElectionCardConfigurationController());
		when(ctrl.getElectionCardModel(any())).thenReturn(ecm);

		assertThat(ctrl.electionTextIsInvalid()).isFalse();
	}

	@Test
    public void infoText_textWithoutSemicolonIsValid() {
		ElectionCardModel ecm = createMock(ElectionCardModel.class);
		when(ecm.getElectionCardInfoText()).thenReturn("olaLa this text be: clean!");

		ElectionCardConfigurationController ctrl = spy(new ElectionCardConfigurationController());
		when(ctrl.getElectionCardModel(any())).thenReturn(ecm);

		assertThat(ctrl.electionTextIsInvalid()).isFalse();
	}

	@Test
	public void saveAddress_updatesFormsAndClosesDialog() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();
		when(ctrl.getMunicipalityConfigStatus().isElectionCard()).thenReturn(false);

		ctrl.saveAddress();

		verify_closeAndUpdate(ctrl.getEditAddressDialog(), "configurationPanel:0:electionCard:form:electionCardWidget:areaTableContainer");
	}

	@Test
	public void cancelSaveAddress_updatesFormAndClosesDialog() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();
		ElectionCardModel model = createMock(ElectionCardModel.class);
		TreeNode selected = ctrl.getSelectedTreeNode();
		((DefaultTreeNode) selected).setData(model);

		ctrl.cancelSaveAddress();

		verify_closeAndUpdate(ctrl.getEditAddressDialog(), "configurationPanel:0:electionCard:form");
	}

	@Test
	public void saveChanges_withChangesToParent_updatesChildTextsAndSaves() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();
		changeSelected(ctrl, true);
		// Selected first child
		ctrl.setSelectedTreeNode(ctrl.getRootTreeNode().getChildren().get(0).getChildren().get(0));
		changeSelected(ctrl, false);

		ctrl.saveChanges();

		verifySaveConfigStatus(false);
		verify(getInjectMock(ElectionCardConfigService.class)).save(eq(getUserDataMock()), any(ElectionCardConfig.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.electioncardconfig null]");
		verify(getInjectMock(ElectionCardConfigService.class), atLeastOnce()).findElectionCardByArea(getUserDataMock(), MUNICIPALITY);
	}

	@Test
	public void saveChanges_withCustomInfoTexts_opensConfirmDialog() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();
		ctrl.setSelectedTreeNode(ctrl.getRootTreeNode().getChildren().get(0).getChildren().get(0));
		changeSelected(ctrl, true);
		ctrl.setSelectedTreeNode(ctrl.getRootTreeNode().getChildren().get(0));
		changeSelected(ctrl, false, true);

		ctrl.saveChanges();

		verify(getRequestContextMock()).execute("PF('confirmElectionCardInfoTextOverwrite').show()");
	}

	@Test
	public void viewModel_withLeafNode_expandsParent() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();
		NodeSelectEvent event = createMock(NodeSelectEvent.class);
		when(event.getTreeNode().isLeaf()).thenReturn(true);

		ctrl.viewModel(event);

		verify(event.getTreeNode().getParent()).setExpanded(true);
	}

	@Test
	public void isDirty_withNoRoot_returnsFalse() throws Exception {
		ElectionCardConfigurationController ctrl = initializeMocks(ElectionCardConfigurationController.class);

		assertThat(ctrl.isDirty()).isFalse();
	}

	@Test
	public void isDirty_withDirtyParent_returnsTrue() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl(true, null);
		ctrl.getSelected().setElectionCardInfoText("New text");

		assertThat(ctrl.isDirty()).isTrue();
	}

	@Test
	public void isDirty_withDirtyChild_returnsTrue() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl(true, null);
		ctrl.setSelectedTreeNode(ctrl.getRootTreeNode().getChildren().get(0).getChildren().get(0));
		ctrl.getSelected().setElectionCardInfoText("New text");

		assertThat(ctrl.isDirty()).isTrue();
	}

	@Test
	public void getRequiresDoneBeforeDone_verifyResult() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl(true, null);

		Class<? extends ConfigurationController>[] classes = ctrl.getRequiresDoneBeforeDone();

		assertThat(classes).hasSize(1);
		assertThat(classes[0].isAssignableFrom(ElectionDayPollingPlacesConfigurationController.class)).isTrue();
	}

	@Test
	public void isRenderInfoText_withOpptellingsvalgstyre_returnsFalse() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl(true, null);
		when(getUserDataMock().isOpptellingsvalgstyret()).thenReturn(true);

		assertThat(ctrl.isRenderInfoText()).isFalse();
	}

	@Test
	public void isOpptellingsvalgstyret_withOpptellingsvalgstyre_returnsTrue() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl(true, null);
		when(getUserDataMock().isOpptellingsvalgstyret()).thenReturn(true);

		assertThat(ctrl.isOpptellingsvalgstyret()).isTrue();
	}

	@Test
	public void isInfoTextEditable_withUpdateMode_returnsTrue() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl(true, null);
		ctrl.setMode(UPDATE);

		assertThat(ctrl.isInfoTextEditable()).isTrue();
	}

	@Test
	public void prepareUpdate_verifyState() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl(true, null);

		ctrl.prepareUpdate();

		assertThat(ctrl.getMode()).isSameAs(UPDATE);
		assertThat(ctrl.isOverwriteCustomInfoText()).isFalse();
	}

	@Test
	public void keepCustom_verifySaveAndDialogClose() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();
		when(ctrl.getElectionCard().getInfoText()).thenReturn("Changed text");

		ctrl.keepCustom();

		verify_confirm(ctrl, false);
	}

	@Test
	public void overwriteCustom_verifySaveAndDialogClose() throws Exception {
		ElectionCardConfigurationController ctrl = ctrl();
		when(ctrl.getElectionCard().getInfoText()).thenReturn("Changed text");

		ctrl.overwriteCustom();

		verify_confirm(ctrl, true);
	}

    private void verify_confirm(ElectionCardConfigurationController ctrl, boolean overwrite) {
		ElectionCardModel child = (ElectionCardModel) ctrl.getRootTreeNode().getChildren().get(0).getChildren().get(0).getData();
		verify(child.getPollingPlace()).setInfoText("Changed text");
		child = (ElectionCardModel) ctrl.getRootTreeNode().getChildren().get(0).getChildren().get(1).getData();
		verify(child.getPollingPlace(), overwrite ? times(1) : never()).setInfoText("Changed text");
		verify(getInjectMock(ElectionCardConfigService.class)).save(eq(getUserDataMock()), any(ElectionCardConfig.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.electioncardconfig null]");
		verify(getRequestContextMock()).execute("PF('confirmElectionCardInfoTextOverwrite').hide()");
		verify(getRequestContextMock()).update(asList("configurationPanel", "approve-form"));
	}

	private ElectionCardModel changeSelected(ElectionCardConfigurationController ctrl, boolean isChangedText) {
		return changeSelected(ctrl, isChangedText, false);
	}

	private ElectionCardModel changeSelected(ElectionCardConfigurationController ctrl, boolean isChangedText, boolean isRoot) {
		TreeNode selected = ctrl.getSelectedTreeNode();
		ElectionCardModel model = createMock(ElectionCardModel.class);
		when(model.isInfoTextChanged()).thenReturn(isChangedText);
		when(model.isRoot()).thenReturn(isRoot);
		((DefaultTreeNode) selected).setData(model);
		return model;
	}

	private ElectionCardConfigurationController ctrl() throws Exception {
		return ctrl(true, false);
	}

	private ElectionCardConfigurationController ctrl(boolean isVoterNumbersGenerated) throws Exception {
		return ctrl(true, false, true, isVoterNumbersGenerated);
	}

	private ElectionCardConfigurationController ctrl(boolean isEditable, Boolean isDirty) throws Exception {
		return ctrl(isEditable, isDirty, true, false);
	}

	private ElectionCardConfigurationController ctrl(Boolean isEditable, Boolean isDirty, Boolean isValid, boolean isVoterNumbersGenerated) throws Exception {
		ElectionCardConfigurationController result = ctrl(initializeMocks(new ElectionCardConfigurationController() {

			private Dialog dialog = createMock(Dialog.class);

			@Override
			public boolean isEditable() {
				return isEditable == null ? super.isEditable() : isEditable;
			}

			@Override
			boolean isDirty() {
				return isDirty == null ? super.isDirty() : isDirty;
			}

			@Override
			boolean isValid() {
				return isValid == null ? super.isValid() : isValid;
			}

			@Override
			public Dialog getEditAddressDialog() {
				return dialog;
			}
		}), MUNICIPALITY);

		stub_findElectionCardByArea(electionCard());
		stub_isVoterNumbersGenerated(isVoterNumbersGenerated);
		result.init();
		result.setSelectedTreeNode(result.getRootTreeNode().getChildren().get(0));
		return result;
	}

	private void stub_isVoterNumbersGenerated(boolean isVoterNumbersGenerated) {
		when(getInjectMock(BakgrunnsjobbService.class).erManntallsnummergenereringStartetEllerFullfort(eq(getUserDataMock()))).thenReturn(isVoterNumbersGenerated);
	}

	private ElectionCardConfig stub_findElectionCardByArea(ElectionCardConfig electionCard) {
		when(getInjectMock(ElectionCardConfigService.class).findElectionCardByArea(getUserDataMock(), MUNICIPALITY)).thenReturn(electionCard);
		return electionCard;
	}

	private ElectionCardConfig electionCard() {
		ElectionCardConfig result = createMock(ElectionCardConfig.class);
		when(result.getInfoText()).thenReturn("Info text");
		when(result.getReportingUnit()).thenReturn(reportingUnit());
		List<ElectionDayPollingPlace> places = places();
		when(result.getPlaces()).thenReturn(places);
		return result;
	}

	private ReportingUnit reportingUnit() {
		return createMock(ReportingUnit.class);
	}

	private List<ElectionDayPollingPlace> places() {
		List<ElectionDayPollingPlace> result = new ArrayList<>();
		result.add(place("Info text"));
		result.add(place("Info text other"));
		return result;
	}

	private ElectionDayPollingPlace place(String infoText) {
		ElectionDayPollingPlace result = createMock(ElectionDayPollingPlace.class);
		when(result.getInfoText()).thenReturn(infoText);
		return result;
	}

}
