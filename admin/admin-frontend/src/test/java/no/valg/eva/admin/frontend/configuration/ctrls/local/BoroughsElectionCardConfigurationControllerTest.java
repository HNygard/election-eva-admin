package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.model.local.ReportingUnit;
import no.valg.eva.admin.common.configuration.service.ElectionCardConfigService;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.configuration.models.BoroughsElectionCardModel;
import no.valg.eva.admin.frontend.configuration.models.ElectionCardModel;
import org.primefaces.model.TreeNode;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BoroughsElectionCardConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void getView_returnsElectionCardView() throws Exception {
		BoroughsElectionCardConfigurationController ctrl = ctrl();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.BOROUGHS_ELECTION_CARD);
	}

	@Test
	public void saveDone_withEditable_cardsShouldBeSavedWithParentInfoText() throws Exception {
		BoroughsElectionCardConfigurationController ctrl = ctrl();

		ctrl.saveDone();

		verifySaveConfigStatus();
		TreeNode parentNode = ctrl.getRootTreeNode().getChildren().get(0);
		ElectionCardModel childWithCustomInfoText = ctrl.getElectionCardModel(parentNode.getChildren().get(1).getChildren().get(0));
		assertThat(childWithCustomInfoText.getElectionCardInfoText()).isEqualTo("Info text other");
		verify(getInjectMock(ElectionCardConfigService.class)).save(eq(getUserDataMock()), any(ElectionCardConfig.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.electioncardconfig null]");
	}

	@Test
	public void getRootTreeNode_withBoroughs_returnsSortedByBoroughsTree() throws Exception {
		BoroughsElectionCardConfigurationController ctrl = ctrl();

		TreeNode node = ctrl.getRootTreeNode();

		List<TreeNode> children = node.getChildren().get(0).getChildren();
		assertThat(children).hasSize(2);
		assertThat(((BoroughsElectionCardModel) children.get(0).getData()).getLabel()).isEqualTo("Borough 0101");
		assertThat(((BoroughsElectionCardModel) children.get(1).getData()).getLabel()).isEqualTo("Borough 0201");
	}

	private BoroughsElectionCardConfigurationController ctrl() throws Exception {
		return ctrl(true, false, true);
	}

	private BoroughsElectionCardConfigurationController ctrl(boolean isEditable, Boolean isDirty, Boolean isValid) throws Exception {
		BoroughsElectionCardConfigurationController result = ctrl(initializeMocks(new BoroughsElectionCardConfigurationController() {

			private Dialog dialog = createMock(Dialog.class);

			@Override
			public boolean isEditable() {
				return isEditable;
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

			@Override
			boolean isHasBoroughs() {
				return true;
			}
		}), MUNICIPALITY);

		stub_findElectionCardByArea(electionCard());
		result.init();
		result.setSelectedTreeNode(result.getRootTreeNode().getChildren().get(0));
		return result;
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
		result.add(place("0101", "Info text"));
		result.add(place("0201", "Info text other"));
		return result;
	}

	private ElectionDayPollingPlace place(String id, String infoText) {
		ElectionDayPollingPlace result = createMock(ElectionDayPollingPlace.class);
		when(result.getId()).thenReturn(id);
		when(result.getInfoText()).thenReturn(infoText);
		Borough borough = borough(id + "01", "Borough " + id);
		when(borough.getPath()).thenReturn(AreaPath.from(MUNICIPALITY + "." + id + "01"));
		when(result.getBorough()).thenReturn(borough);
		return result;
	}

}
