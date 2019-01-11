package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.util.MockUtils;
import no.valg.eva.admin.common.configuration.model.local.ReportCountCategory;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;
import no.valg.eva.admin.common.configuration.service.PollingDistrictService;
import no.valg.eva.admin.common.configuration.service.ReportCountCategoryService;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Geografi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TechnicalPollingDistrictConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void getView_returnsTechnicalPollingDistrict() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();

		assertThat(ctrl.getView()).isSameAs(ConfigurationView.TECHNICAL_POLLING_DISTRICT);
	}

	@Test
	public void collectPollingPlaces_verifyLoad() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();

		ctrl.collectPollingPlaces();

		verify(getInjectMock(PollingDistrictService.class)).findTechnicalPollingDistrictsByArea(getUserDataMock(), ctrl.getAreaPath());
	}

	@Test
	public void collectPollingPlace_withId_verifyLoad() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();
		stub_findTechnicalPollingDistrictsByArea(ctrl);

		assertThat(ctrl.collectPollingPlace("1102")).isNotNull();
	}

	@Test
	public void save_withDistrict_verifySave() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();
		TechnicalPollingDistrict district = new TechnicalPollingDistrict(MUNICIPALITY);

		ctrl.save(district);

		verify(getInjectMock(PollingDistrictService.class)).saveTechnicalPollingDistrict(getUserDataMock(), district);
	}

	@Test
	public void canBeSetToDone_returnsTrue() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();

		assertThat(ctrl.canBeSetToDone()).isTrue();
	}

	@Test
	public void initCreate_verifyState() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();

		ctrl.initCreate();

		assertThat(ctrl.getPlace()).isNotNull();
		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.CREATE);
	}

	@Test
	public void confirmDelete_withPlace_verifyDelete() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();
		TechnicalPollingDistrict district = new TechnicalPollingDistrict(MUNICIPALITY);
		district.setId("1102");
		district.setName("Name");
		ctrl.setPlace(district);
		stub_findTechnicalPollingDistrictsByArea(ctrl);

		ctrl.confirmDelete();

		verify(getInjectMock(PollingDistrictService.class)).deleteTechnicalPollingDistrict(getUserDataMock(), district);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.deleted, @common.displayable.technicalpollingdistrict Name]");
		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
		assertThat(ctrl.getPlace().getId()).isEqualTo("1100");
	}

	@Test
	public void getName_returnsCorrectName() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.technical_polling_district.name");
	}

	@Test
	public void isDoneStatus_withElectionPollingPlaceTrue_returnsTrue() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();
		when(ctrl.getMunicipalityConfigStatus().isTechPollingDistricts()).thenReturn(true);

		assertThat(ctrl.isDoneStatus()).isTrue();
	}

	@Test
	public void setDoneStatus_withTrue_setsElectionPollingPlaceStatus() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();

		ctrl.setDoneStatus(true);

		verify(ctrl.getMunicipalityConfigStatus()).setTechPollingDistricts(true);
	}

	@Test
	public void hasAccess_withAccessAndCorrectLevel_returnsTrue() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();
		ReportCountCategory rcc = mock(ReportCountCategory.class);
		when(rcc.getCountingMode()).thenReturn(CountingMode.BY_TECHNICAL_POLLING_DISTRICT);
		when(getInjectMock(ReportCountCategoryService.class).findFirstByAreaAndCountCategory(any(), any(), any(), any())).thenReturn(rcc);
		hasAccess(Konfigurasjon_Geografi);

		assertThat(ctrl.hasAccess()).isTrue();
	}

	@Test
	public void canBeSetToDone_withValidPlaces_returnsTrue() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();
		stub_findTechnicalPollingDistrictsByArea(ctrl);
		ctrl.init();

		assertThat(ctrl.canBeSetToDone()).isTrue();
	}

	@Test
	public void canBeSetToDone_withInvalidDistricts_returnsFalse() throws Exception {
		TechnicalPollingDistrictConfigurationController ctrl = ctrl();
        List<TechnicalPollingDistrict> places = Collections.singletonList(
                district("1000", false));
		MockUtils.setPrivateField(ctrl, "places", places);

		assertThat(ctrl.canBeSetToDone()).isFalse();
	}

	private TechnicalPollingDistrictConfigurationController ctrl() throws Exception {
		return ctrl(TechnicalPollingDistrictConfigurationController.class, MUNICIPALITY);
	}

	private List<TechnicalPollingDistrict> stub_findTechnicalPollingDistrictsByArea(TechnicalPollingDistrictConfigurationController ctrl) {
		List<TechnicalPollingDistrict> result = Arrays.asList(district("1100"), district("1101"), district("1102"));
		when(getInjectMock(PollingDistrictService.class).findTechnicalPollingDistrictsByArea(getUserDataMock(), ctrl.getAreaPath())).thenReturn(result);
		return result;
	}

	private TechnicalPollingDistrict district(String id) {
		return district(id, true);
	}

	private TechnicalPollingDistrict district(String id, boolean isValid) {
		TechnicalPollingDistrict result = createMock(TechnicalPollingDistrict.class);
		when(result.isValid()).thenReturn(isValid);
		when(result.display()).thenReturn("TechnicalPollingDistrict " + id);
		when(result.getId()).thenReturn(id);
		when(result.getName()).thenReturn("Name " + id);
		when(result.getPk()).thenReturn(Long.parseLong(id));
		when(result.getPath()).thenReturn(MUNICIPALITY);
		return result;
	}

}

