package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.PollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;
import no.valg.eva.admin.common.configuration.service.PollingDistrictService;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.event.ValueChangeEvent;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class StemmestyreConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_withTwoPollingDistricts_verifyState() throws Exception {
		StemmestyreConfigurationController ctrl = ctrl();
		stub_findRegularPollingDistrictsByArea(2);

		ctrl.init();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.STEMMESTYRE);
		assertThat(ctrl.getPlaces()).hasSize(2);
		assertThat(ctrl.getPollingDistrict()).isNotNull();
		assertThat(ctrl.getStyreAreaPath()).isSameAs(ctrl.getPollingDistrict().getPath());
		assertThat(ctrl.getResponsibleOfficers()).isNotNull();
		assertThat(ctrl.getPollingDistrict().isHasResponsibleOffiers()).isTrue();
	}

	@Test
	public void saveResponsibleOfficer_withPollingDistrict_verifySave() throws Exception {
		StemmestyreConfigurationController ctrl = ctrl();
		stub_findRegularPollingDistrictsByArea(2);
		ctrl.init();
		mockField("selectedResponsibleOfficer", ResponsibleOfficer.class);

		ctrl.saveResponsibleOfficer();

		assertThat(ctrl.getPollingDistrict().isHasResponsibleOffiers()).isTrue();
	}

	@Test
	public void confirmDelete_withPollingDistrict_verifyDelete() throws Exception {
		StemmestyreConfigurationController ctrl = ctrl();
		stub_findRegularPollingDistrictsByArea(2);
		ctrl.init();
		mockField("selectedResponsibleOfficer", ResponsibleOfficer.class);

		ctrl.confirmDelete();

		assertThat(ctrl.getPollingDistrict().isHasResponsibleOffiers()).isTrue();
	}

	@Test
	public void districtSelected_withDistrict_verifyChange() throws Exception {
		StemmestyreConfigurationController ctrl = ctrl();
		PollingDistrict pollingDistrict = createMock(PollingDistrict.class);
		ValueChangeEvent event = createMock(ValueChangeEvent.class);
		when(event.getNewValue()).thenReturn(pollingDistrict);

		ctrl.districtSelected(event);

		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
		assertThat(ctrl.getPollingDistrict()).isSameAs(pollingDistrict);
		verify(ctrl.getPollingDistrict(), atLeastOnce()).setHasResponsibleOffiers(true);
	}

	@Test(dataProvider = "canBeSetToDone")
	public void canBeSetToDone_withDataProvider_verifyExpected(List<PollingDistrict> pollingDistricts, boolean expected) throws Exception {
		StemmestyreConfigurationController ctrl = ctrl();
		mockFieldValue("pollingDistricts", pollingDistricts);

		assertThat(ctrl.canBeSetToDone()).isEqualTo(expected);
	}

	@DataProvider(name = "canBeSetToDone")
	public Object[][] canBeSetToDone() {
		return new Object[][] {
				{ null, false },
				{ stub_findRegularPollingDistrictsByArea(3, false), false },
				{ stub_findRegularPollingDistrictsByArea(3, true), true }
		};
	}

	@Test
	public void getName_returnsStemmestyre() throws Exception {
		StemmestyreConfigurationController ctrl = ctrl();

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.stemmestyre.name");
	}

	@Test
	public void setDoneStatus_withStatus_setsFylkesstatus() throws Exception {
		StemmestyreConfigurationController ctrl = ctrl();

		ctrl.setDoneStatus(true);

		verify(ctrl.getMainController().getMunicipalityStatus()).setReportingUnitStemmestyre(true);
	}

	@Test
	public void isDoneStatus_withStatus_returnsFylkesStatus() throws Exception {
		StemmestyreConfigurationController ctrl = ctrl();
		when(ctrl.getMainController().getMunicipalityStatus().isReportingUnitStemmestyre()).thenReturn(false);

		assertThat(ctrl.isDoneStatus()).isFalse();
	}

	@Test
	public void getRequiresDoneBeforeDone_verifyResult() throws Exception {
		StemmestyreConfigurationController ctrl = ctrl();

		Class<? extends ConfigurationController>[] classes = ctrl.getRequiresDoneBeforeDone();

		assertThat(classes).hasSize(1);
		assertThat(classes[0].isAssignableFrom(ElectionDayPollingPlacesConfigurationController.class)).isTrue();
	}

	private StemmestyreConfigurationController ctrl() throws Exception {
		return ctrl(StemmestyreConfigurationController.class, MUNICIPALITY);
	}

	private PollingDistrictService getPollingDistrictService() throws Exception {
		return getPrivateField("pollingDistrictService", PollingDistrictService.class);
	}

	private List<PollingDistrict> stub_findRegularPollingDistrictsByArea(int size) {
		return stub_findRegularPollingDistrictsByArea(size, true);
	}

	private List<PollingDistrict> stub_findRegularPollingDistrictsByArea(int size, boolean isHasResponsibleOffiers) {
		List<PollingDistrict> list = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			PollingDistrict pollingDistrict = regularPollingDistrict("1000", PollingDistrictType.REGULAR);
			pollingDistrict.setHasResponsibleOffiers(isHasResponsibleOffiers);
			list.add(pollingDistrict);
		}
		try {
			when(getPollingDistrictService().findRegularPollingDistrictsByArea(eq(getUserDataMock()), any(AreaPath.class), anyBoolean())).thenReturn(list);
		} catch (Exception e) {
		}

		return list;
	}
}

