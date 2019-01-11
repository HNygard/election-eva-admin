package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.local.ReportCountCategory;
import no.valg.eva.admin.common.configuration.service.ReportCountCategoryService;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ReportCountCategoriesConfigurationControllerTest extends BaseLocalConfigurationControllerTest {

	@Test
	public void init_withNonEditable_verifyState() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(false);

		ctrl.init();

		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
		assertThat(ctrl.getCurrentReportCountCategories()).hasSize(1);
		assertThat(ctrl.hasBoroughData()).isFalse();
		assertThat(ctrl.isBoroughDataSelected()).isFalse();
	}

	@Test
	public void init_withBoroughs_verifyState() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);
		when(ctrl.getMainController().isHasBoroughs()).thenReturn(true);
		stub_contestOrElectionByAreaPath();

		ctrl.init();

		assertThat(ctrl.getMode()).isSameAs(ConfigurationMode.READ);
		assertThat(ctrl.getCurrentReportCountCategories()).hasSize(1);
		assertThat(ctrl.hasBoroughData()).isTrue();
		assertThat(ctrl.isBoroughDataSelected()).isFalse();
		assertThat(ctrl.getContestInfoList()).hasSize(2);
		assertThat(ctrl.getContestInfoList().get(0).getElectionName()).isEqualTo("Kommunestyrevalg");
		assertThat(ctrl.getContestInfoList().get(1).getElectionName()).isEqualTo("Bydelsvalg");
	}

	@Test(dataProvider = "button")
	public void button_withDataProvider_verifyExpected(ButtonType buttonType, boolean isEditable, boolean isRendered, boolean isDisabled) throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(isEditable);

		Button button = ctrl.button(buttonType);

		assertThat(button.isRendered()).isEqualTo(isRendered);
		assertThat(button.isDisabled()).isEqualTo(isDisabled);
	}

	@DataProvider(name = "button")
	public Object[][] button() {
		return new Object[][] {
				{ ButtonType.DONE, false, true, true },
				{ ButtonType.DONE, true, true, false },
				{ ButtonType.APPROVE_TO_SETTLEMENT, false, false, true }
		};
	}

	@Test
	public void saveCount_withData_verifySave() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);
		stub_updateCountCategories(Arrays.asList(
				countCategory(VO, CENTRAL),
				countCategory(FS, CENTRAL_AND_BY_POLLING_DISTRICT),
				countCategory(VF, BY_POLLING_DISTRICT),
				countCategory(FO, BY_TECHNICAL_POLLING_DISTRICT)));

		ctrl.saveCount();

		verifySaveConfigStatus();
		assertThat(ctrl.getCurrentReportCountCategories()).hasSize(4);
		List<FacesMessage> expectedMessages = new ArrayList<>();
		expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_INFO, "[@report_count_category.voting_count_category.update_message, @vote_count_category[VO].name, "
				+ "@report_count_category.count_mode_select.central]", null));
		expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_INFO, "[@report_count_category.voting_count_category.update_message, @vote_count_category[FS].name, "
				+ "@report_count_category.count_mode_select.central_and_by_polling_district]", null));
		expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_INFO, "[@report_count_category.voting_count_category.update_message, @vote_count_category[VF].name, "
				+ "@report_count_category.count_mode_select.by_polling_district]", null));
		expectedMessages.add(new FacesMessage(FacesMessage.SEVERITY_INFO, "[@report_count_category.voting_count_category.update_message, @vote_count_category[FO].name, "
				+ "@report_count_category.count_mode_select.by_technical_polling_district]", null));
		assertFacesMessages(expectedMessages);
	}

	@Test
	public void getName_returnsCorrectName() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);

		assertThat(ctrl.getName()).isEqualTo("@config.local.accordion.report_count_categories.name");
	}

	@Test
	public void hasAccess_withAccess_returnsTrue() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);
		hasAccess(Konfigurasjon_Grunnlagsdata_Redigere);

		assertThat(ctrl.hasAccess()).isTrue();
	}

	@Test
	public void isDoneStatus_withCountCategoriesStatusTrue_returnsTrue() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);
		when(ctrl.getMunicipalityConfigStatus().isCountCategories()).thenReturn(true);

		assertThat(ctrl.isDoneStatus()).isTrue();
	}

	@Test
	public void setDoneStatus_withTrue_setsCountCategoriesStatus() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);

		ctrl.setDoneStatus(true);

		verify(ctrl.getMunicipalityConfigStatus()).setCountCategories(true);
	}

	@Test
	public void getView_returnsCountView() throws Exception {
		assertThat(ctrl(true).getView()).isSameAs(ConfigurationView.COUNT);
	}

	@Test
	public void canBeSetToDone_returnsTrue() throws Exception {
		assertThat(ctrl(true).canBeSetToDone()).isTrue();
	}

	@Test
	public void saveDone_withEditable_savesModelAndStatus() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);

		ctrl.saveDone();

		verifySaveConfigStatus();
		verify(getInjectMock(ReportCountCategoryService.class)).updateCountCategories(eq(getUserDataMock()), any(AreaPath.class), any(ElectionPath.class),
                any());
	}

	@Test
	public void isValgtingsstemmerSentraltSamlet_withUndone_returnsFalse() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);
		when(ctrl.getMainController().getMunicipalityStatus().isCountCategories()).thenReturn(false);

		assertThat(ctrl.isValgtingsstemmerSentraltSamlet()).isFalse();
	}

	@Test
	public void isValgtingsstemmerSentraltSamlet_withSentralSamlet_returnsTrue() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);
		when(ctrl.getMainController().getMunicipalityStatus().isCountCategories()).thenReturn(true);
		List<ReportCountCategory> list = new ArrayList<>();
		mockFieldValue("reportCountCategories", list);
		mockFieldValue("currentReportCountCategories", list);
		ctrl.getCurrentReportCountCategories().add(countCategory(VO, CountingMode.CENTRAL));

		assertThat(ctrl.isValgtingsstemmerSentraltSamlet()).isTrue();
	}

	@Test
	public void selectBoroughData_setsCurrentToBoroughData() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);
		when(ctrl.getMainController().isHasBoroughs()).thenReturn(true);
		stub_contestOrElectionByAreaPath();
		ctrl.init();

		ctrl.selectBoroughData();

		assertThat(ctrl.isBoroughDataSelected()).isTrue();
	}

	@Test
	public void unselectBoroughData_setsCurrentToDefaultData() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);
		when(ctrl.getMainController().isHasBoroughs()).thenReturn(true);
		stub_contestOrElectionByAreaPath();
		ctrl.init();
		ctrl.selectBoroughData();

		ctrl.unselectBoroughData();

		assertThat(ctrl.isBoroughDataSelected()).isFalse();
	}

	@Test
	public void getRequiresDoneBeforeEdit_withElectronicMarkoff_returnsElectronicMarkoffsConfigurationController() throws Exception {
		ReportCountCategoriesConfigurationController ctrl = ctrl(true);
		when(ctrl.getMainController().getElectionGroup().isElectronicMarkoffs()).thenReturn(true);

		assertThat(ctrl.getRequiresDoneBeforeEdit()).hasSize(1);
		assertThat(ctrl.getRequiresDoneBeforeEdit()[0]).isSameAs(ElectronicMarkoffsConfigurationController.class);
	}

	private ReportCountCategoriesConfigurationController ctrl(boolean isEditable) throws Exception {
		ReportCountCategoriesConfigurationController result = ctrl(initializeMocks(new ReportCountCategoriesConfigurationController() {
			@Override
			public boolean isEditable() {
				return isEditable;
			}
		}), MUNICIPALITY);
		stub_findCountCategoriesByArea(singletonList(countCategory(VO)));
		stub_findBoroughCountCategoriesByArea(singletonList(countCategory(VO)));
		when(result.getMainController().getElectionGroup().getElectionGroupPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonGrunnlagsdataGodkjenne()).thenReturn(isEditable);
		return result;

	}

	private ReportCountCategory countCategory(no.valg.eva.admin.common.counting.model.CountCategory category) {
		return new ReportCountCategory(category, new ArrayList<>());
	}

	private ReportCountCategory countCategory(no.valg.eva.admin.common.counting.model.CountCategory category, CountingMode mode) {
		ReportCountCategory result = new ReportCountCategory(category, new ArrayList<>());
		result.setCountingMode(mode);
		return result;
	}

	private List<ReportCountCategory> stub_findCountCategoriesByArea(List<ReportCountCategory> categories) {
		when(getInjectMock(ReportCountCategoryService.class).findCountCategoriesByArea(getUserDataMock(), MUNICIPALITY, ELECTION_PATH_ELECTION_GROUP))
				.thenReturn(categories);
		return categories;
	}

	private List<ReportCountCategory> stub_findBoroughCountCategoriesByArea(List<ReportCountCategory> categories) {
		when(getInjectMock(ReportCountCategoryService.class).findBoroughCountCategoriesByArea(getUserDataMock(), MUNICIPALITY)).thenReturn(categories);
		return categories;
	}

	@SuppressWarnings("unchecked")
	private List<ReportCountCategory> stub_updateCountCategories(List<ReportCountCategory> categories) {
        when(getInjectMock(ReportCountCategoryService.class).updateCountCategories(eq(getUserDataMock()), any(AreaPath.class), any(ElectionPath.class), any()))
						.thenReturn(categories);
		return categories;
	}

	private List<ContestInfo> stub_contestOrElectionByAreaPath() {
		List<ContestInfo> contestInfoList = Arrays.asList(
				contestInfo("Kommunestyrevalg", ElectionPath.from("150001.01.02.000301")),
				contestInfo("Bydelsvalg", ElectionPath.from("150001.01.03")));
		when(getInjectMock(ContestInfoService.class).contestOrElectionByAreaPath(any(AreaPath.class))).thenReturn(contestInfoList);
		return contestInfoList;
	}

	private ContestInfo contestInfo(String electionName, ElectionPath electionPath) {
		ContestInfo result = createMock(ContestInfo.class);
		when(result.getElectionName()).thenReturn(electionName);
		when(result.getAreaLevel()).thenReturn(AreaLevelEnum.MUNICIPALITY);
		when(result.getElectionPath()).thenReturn(electionPath);
		return result;
	}
}

