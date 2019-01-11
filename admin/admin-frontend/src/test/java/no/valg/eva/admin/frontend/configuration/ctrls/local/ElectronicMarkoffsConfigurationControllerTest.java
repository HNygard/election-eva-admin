package no.valg.eva.admin.frontend.configuration.ctrls.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.faces.application.FacesMessage;

import no.valg.eva.admin.frontend.configuration.ConfigurationView;

import org.testng.annotations.Test;

public class ElectronicMarkoffsConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_verifyState() throws Exception {
		ElectronicMarkoffsConfigurationController ctrl = ctrl();

		assertThat(ctrl.isElectronicMarkoffs()).isTrue();
	}

	@Test
	public void getView_returnsElectronicMarkoffsView() throws Exception {
		ElectronicMarkoffsConfigurationController ctrl = ctrl();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.ELECTRONIC_MARKOFFS);
	}

	@Test
	public void getName_returnsElectronicMarkoffsName() throws Exception {
		ElectronicMarkoffsConfigurationController ctrl = ctrl();

		assertThat(ctrl.getName()).isSameAs("@config.local.accordion.electronic_markoffs.name");
	}

	@Test
	public void hasAccess_withCorrectLevel_returnsTrue() throws Exception {
		ElectronicMarkoffsConfigurationController ctrl = ctrl();
		when(ctrl.getMainController().getElectionGroup().isElectronicMarkoffs()).thenReturn(true);

		assertThat(ctrl.hasAccess()).isTrue();
	}

	@Test
	public void isDoneStatus_withCountCategoriesStatusTrue_returnsTrue() throws Exception {
		ElectronicMarkoffsConfigurationController ctrl = ctrl();
		when(ctrl.getMunicipalityConfigStatus().isElectronicMarkoffs()).thenReturn(true);

		assertThat(ctrl.isDoneStatus()).isTrue();
	}

	@Test
	public void setDoneStatus_withTrue_setsCountCategoriesStatus() throws Exception {
		ElectronicMarkoffsConfigurationController ctrl = ctrl();

		ctrl.setDoneStatus(true);

		verify(ctrl.getMunicipalityConfigStatus()).setElectronicMarkoffs(true);
	}

	@Test
	public void canBeSetToDone_returnsTrue() throws Exception {
		ElectronicMarkoffsConfigurationController ctrl = ctrl();

		assertThat(ctrl.canBeSetToDone()).isTrue();
	}

	@Test
	public void saveDone_withEditable_alsoSavesData() throws Exception {
		ElectronicMarkoffsConfigurationController ctrl = ctrl();
		ctrl.setElectronicMarkoffs(true);

		ctrl.saveDone();

		verifySaveConfigStatus();
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@config.local.electronic_markoffs.saved.true, Municipality 4444]");
	}

	private ElectronicMarkoffsConfigurationController ctrl() throws Exception {
		return ctrl(true);
	}

	private ElectronicMarkoffsConfigurationController ctrl(boolean isEditable) throws Exception {
		ElectronicMarkoffsConfigurationController result = ctrl(initializeMocks(new ElectronicMarkoffsConfigurationController() {
			@Override
			public boolean isEditable() {
				return isEditable;
			}
		}), MUNICIPALITY);
		when(result.getMunicipalityConfigStatus().isUseElectronicMarkoffs()).thenReturn(true);
		result.init();
		return result;
	}
}
