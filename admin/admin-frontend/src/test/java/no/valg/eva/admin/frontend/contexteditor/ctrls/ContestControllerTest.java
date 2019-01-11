package no.valg.eva.admin.frontend.contexteditor.ctrls;

import no.evote.dto.MvElectionMinimal;
import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.ContestAreaService;
import no.evote.service.configuration.MvElectionService;
import no.evote.util.MockUtils;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.Contest;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.service.ContestService;
import no.valg.eva.admin.common.configuration.service.ElectionService;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaMultipleController;
import no.valg.eva.admin.frontend.election.ctrls.MvElectionPickerController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ContestControllerTest extends BaseFrontendTest {

	@Test
	public void createContest_withEvoteException_returnsErrorMessageAndOpAndErrorCallbackParams() throws Exception {
		ContestController ctrl = getContestController("05", true, true);
		doThrow(new EvoteException(ErrorCode.ERROR_CODE_0503_CONSTRAINT_VIOLATION, null, null)).when(getInjectMock(ContestService.class))
				.save(eq(getUserDataMock()), any(Contest.class));
		ctrl.getCurrentContest().setId("000005");

		ctrl.createContest();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.create.CHOOSE_UNIQUE_ID, @election_level[3].name, 000005, ElectionName]");
		verify(getRequestContextMock()).addCallbackParam("op", "createContest");
		verify(getRequestContextMock()).addCallbackParam("error", true);
	}

	@Test
	public void createContest_withSingleArea_verifyState() throws Exception {
		ContestController ctrl = getContestController("05", true, true);

		ctrl.createContest();

		verifyCommonCreateContest(ctrl);
		assertThat(ctrl.getCurrentContest().getId()).isEmpty();
	}

	@Test
	public void createContest_withNotSingleArea_verifyState() throws Exception {
		ContestController ctrl = getContestController("05", false, true);

		ctrl.createContest();

		verifyCommonCreateContest(ctrl);
		verify(getRequestContextMock()).addCallbackParam("edit", "000005");
	}

	@Test
	public void updateContest_withEvoteException_returnsErrorMessageAndOpAndErrorCallbackParams() throws Exception {
		ContestController ctrl = getContestController("05", true, false);
		doThrow(new EvoteException(ErrorCode.ERROR_CODE_0503_CONSTRAINT_VIOLATION, null, null)).when(getInjectMock(ContestService.class))
				.save(eq(getUserDataMock()), any(Contest.class));

		ctrl.updateContest();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.create.CHOOSE_UNIQUE_ID, @election_level[3].name, 000005, ElectionName]");
		verify(getRequestContextMock()).addCallbackParam("op", "updateContest");
		verify(getRequestContextMock()).addCallbackParam("error", true);
	}

	@Test
	public void updateContest_withValidContest_verifyUpdateAndMessage() throws Exception {
		ContestController ctrl = getContestController("05", true, false);

		ctrl.updateContest();

		// assertThat(ctrl.getAreaLevelMessageKey()).isEqualTo("@area_level[0].name");
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.update.successful");
		verify(getRequestContextMock()).addCallbackParam("op", "updateContest");
	}

	@Test
	public void deleteContest_withEvoteException_returnsErrorMessageAndOpAndErrorCallbackParams() throws Exception {
		ContestController ctrl = getContestController("05", true, false);
		doThrow(new EvoteException("@evote.exception")).when(getInjectMock(ContestService.class))
				.delete(eq(getUserDataMock()), any(ElectionPath.class));

		ctrl.deleteContest();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@evote.exception");
		verify(getRequestContextMock()).addCallbackParam("op", "deleteContest");
		verify(getRequestContextMock()).addCallbackParam("error", true);
	}

	@Test
	public void deleteContest_withValidContest_verifyUpdateAndMessage() throws Exception {
		ContestController ctrl = getContestController("05", true, false);
		ctrl.getCurrentContest().setName("ContestName");

		ctrl.deleteContest();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.message.sub_delete.successful, ContestName, ElectionName]");
		verify(getRequestContextMock()).addCallbackParam("op", "deleteContest");
	}

	@Test
	public void doGetCreateContestArea_withCurrentContest_verifyContestAreaMultipleControllerDelegation() throws Exception {
		ContestController ctrl = getContestController("05", true, false);

		ctrl.doGetCreateContestArea();

		verify(getInjectMock(ContestAreaMultipleController.class)).doGetCreateContestArea(any(no.valg.eva.admin.configuration.domain.model.Contest.class));
	}

	@Test
	public void doGetCreateContestArea_withPreviousCounty_verifyCountyAndMunicipalitySetup() throws Exception {
		ContestController ctrl = getContestController("05", true, false);
		when(getInjectMock(MvAreaMultipleController.class).getCountyId()).thenReturn("01");
		when(getInjectMock(MvAreaMultipleController.class).getCountyItems()).thenReturn(Collections.singletonList(item("01", "Somehing")));

		ctrl.doGetCreateContestArea();

		verify(getInjectMock(MvAreaMultipleController.class)).setCountyId("01");
		verify(getInjectMock(MvAreaMultipleController.class)).changeCounty();
	}

	private SelectItem item(String value, String label) {
		return new SelectItem(value, label);
	}

	@Test
	public void doCreateContestArea_withCreateError_reloadsContestAreaList() throws Exception {
		ContestController ctrl = getContestController("05", true, false);
		doThrow(new RuntimeException("RuntimeException")).when(getInjectMock(ContestAreaMultipleController.class)).doCreateContestArea();

		ctrl.doCreateContestArea();
		assertFacesMessage(SEVERITY_ERROR, "[@common.error.unexpected, 15452a9b]");
		verify_findContestAreasForContestPath();
	}

	@Test
	public void doCreateContestArea_withNoException_reloadsContestAreaList() throws Exception {
		ContestController ctrl = getContestController("05", true, false);

		ctrl.doCreateContestArea();

		verify(getInjectMock(ContestAreaMultipleController.class)).doCreateContestArea();
		verify_findContestAreasForContestPath();
	}

	@Test
	public void hasElectionContestAccess_withoutAccess_returnsFalse() throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);
		when(getInjectMock(UserDataController.class).getUserAccess().isKonfigurasjonValgValgdistrikt()).thenReturn(false);

		assertThat(ctrl.isKonfigurasjonValgValgdistrikt()).isFalse();
	}

	@Test(dataProvider = "changePenultimateRecount")
	public void changePenultimateRecount_withDataProvider_verifyExpected(int value, Boolean expected) throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);
		Contest contest = mockField("currentContest", Contest.class);
		ValueChangeEvent event = createMock(ValueChangeEvent.class);
		when(event.getNewValue()).thenReturn(value);

		ctrl.changePenultimateRecount(event);

		verify(contest).setPenultimateRecount(expected);
	}

	@DataProvider(name = "changePenultimateRecount")
	public Object[][] changePenultimateRecount() {
		return new Object[][] {
				{ 0, null },
				{ 1, true },
				{ 2, false }
		};
	}

	@Test
	public void getIsCurrentRemovable_withCentralConfigStatus_returnsTrue() throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);
		when(getInjectMock(UserDataController.class).isCentralConfigurationStatus()).thenReturn(true);

		assertThat(ctrl.getIsCurrentRemovable()).isTrue();
	}

	@Test
	public void updateContestArea_withChildContestArea_verifyUpdate() throws Exception {
		ContestController ctrl = getContestController("05", true, false);
		ctrl.setSelectedContestAreaType(ContestController.ContestAreaType.CHILD);
		ContestArea selectedContestArea = mockField("selectedContestArea", ContestArea.class);

		ctrl.updateContestArea();

		verify(selectedContestArea).setParentArea(false);
		verify(selectedContestArea).setChildArea(true);
		verify_findContestAreasForContestPath();
		assertThat(ctrl.getSelectedContestArea()).isNull();
		assertThat(ctrl.getSelectedContestAreaType()).isNull();
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.update.successful");

	}

	@Test
	public void deleteSelectedContestArea_withContestArea_verifyDelete() throws Exception {
		ContestController ctrl = getContestController("05", true, false);
		mockField("selectedContestArea", ContestArea.class);

		ctrl.deleteSelectedContestArea();

		verify(getInjectMock(ContestAreaService.class)).delete(eq(getUserDataMock()), anyLong());
		assertThat(ctrl.getSelectedContestArea()).isNull();
		assertThat(ctrl.getSelectedContestAreaType()).isNull();
		verify_findContestAreasForContestPath();
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.delete.successful");

	}

	@Test
	public void getCurrentContestAreaList_withNoList_loadsList() throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);
		Contest currentContest = mockField("currentContest", Contest.class);
		when(currentContest.getId()).thenReturn("01");
		stub_findContestAreasForContestPath();

		ctrl.getCurrentContestAreaList();

		verify_findContestAreasForContestPath();
	}

	@Test(dataProvider = "isDisabled")
	public void isDisabled_withDataProvider_verifyExpected(boolean isCurrentElectionEventDisabled, boolean isReadOnly, boolean override,
			boolean expected) throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);
		ctrl.setReadOnly(isReadOnly);
		when(getInjectMock(UserDataController.class).isCurrentElectionEventDisabled()).thenReturn(isCurrentElectionEventDisabled);
		when(getInjectMock(UserDataController.class).isOverrideAccess()).thenReturn(override);

		assertThat(ctrl.isDisabled()).isEqualTo(expected);
	}

	@DataProvider(name = "isDisabled")
	public Object[][] isDisabled() {
		return new Object[][] {
				{ false, false, false, false },
				{ true, false, false, true },
				{ false, true, true, false },
				{ true, true, false, true },
				{ true, false, true, true },
				{ false, true, true, false },
				{ true, true, true, true }

		};
	}

	@Test(dataProvider = "setSelectedContestArea")
	public void setSelectedContestArea_withDataProvider_verifyExpected(ContestArea contestArea, ContestController.ContestAreaType contestAreaType)
			throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);

		ctrl.setSelectedContestArea(contestArea);

		assertThat(ctrl.getSelectedContestArea()).isSameAs(contestArea);
		assertThat(ctrl.getSelectedContestAreaType()).isEqualTo(contestAreaType);
		if (contestAreaType != null) {
			assertThat(ctrl.getSelectedContestAreaType().getLabel()).isEqualTo("@election.contest.contest_area[" + ctrl.getSelectedContestAreaType()
					+ "].name");
		}
	}

	@DataProvider(name = "setSelectedContestArea")
	public Object[][] setSelectedContestArea() {
		return new Object[][] {
				{ null, null },
				{ contestArea(false, false), ContestController.ContestAreaType.ORDINARY },
				{ contestArea(true, false), ContestController.ContestAreaType.PARENT },
				{ contestArea(false, true), ContestController.ContestAreaType.CHILD },
				{ contestArea(true, true), ContestController.ContestAreaType.PARENT }
		};
	}

	@Test
	public void isEdited_withSameArea_returnsTrue() throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);
		ContestArea selectedContestArea = mockField("selectedContestArea", ContestArea.class);
		when(selectedContestArea.getPk()).thenReturn(1L);

		assertThat(ctrl.isEdited(selectedContestArea)).isTrue();
	}

	@Test
	public void getCountyItems_withAlreadyFullyPopulatedCounty_returnsNoElement() throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);
		MockUtils.setPrivateField(ctrl, "currentContest", contest("1"));
		MockUtils.setPrivateField(ctrl, "existingContestAreas", Collections.singletonList(contestArea("0101")));
		when(getInjectMock(MvAreaMultipleController.class).getCountyItems()).thenReturn(Collections.singletonList(item("01", "Something")));
		List<MvArea> mvAreas = Collections.singletonList(mvArea("0101"));
		when(getInjectMock(MvAreaMultipleController.class).getMunicipalitiesForCounty("01")).thenReturn(mvAreas);

		List<SelectItem> items = ctrl.getCountyItems();

		assertThat(items).isEmpty();
	}

	@Test
	public void getMunicipalityItems_withExistingMunicipality_returnsOneElement() throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);
		MockUtils.setPrivateField(ctrl, "existingContestAreas", Collections.singletonList(contestArea("0101")));
		when(getInjectMock(MvAreaMultipleController.class).getMunicipalityItems()).thenReturn(Arrays.asList(item("0101", "Something"), item("0303", "Other")));

		List<SelectItem> items = ctrl.getMunicipalityItems();

		assertThat(items).hasSize(1);
	}

	private ContestArea contestArea(boolean parent, boolean child) {
		ContestArea result = new ContestArea();
		result.setParentArea(parent);
		result.setChildArea(child);
		return result;
	}

	private ContestArea contestArea(String areaId) {
		ContestArea result = createMock(ContestArea.class);
		when(result.getMvArea().getAreaId()).thenReturn(areaId);
		return result;
	}

	private MvArea mvArea(String areaId) {
		MvArea result = createMock(MvArea.class);
		when(result.getAreaId()).thenReturn(areaId);
		return result;
	}

	private Contest contest(String id) {
		Contest result = new Contest(new Election(ELECTION_PATH_ELECTION_GROUP));
		result.setId(id);
		return result;
	}

	private void verifyCommonCreateContest(ContestController ctrl) {
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.message.sub_create.successful, AreaName, ElectionName]");
		verify(getInjectMock(MvElectionPickerController.class)).update(3, "111111.22.33.000005");
		verify(getRequestContextMock()).addCallbackParam("op", "createContest");
		assertThat(ctrl.getCurrentContest()).isNotNull();
		assertThat(ctrl.getContestStatus()).isNull();
		assertThat(ctrl.getPenultimateRecount()).isEqualTo(ContestController.NOT_SELECTED_PENULTIMATE_RECOUNT);
	}

	private ContestController getContestController(String selectedAreaId, boolean isSingleArea, boolean create) throws Exception {
		ContestController ctrl = initializeMocks(ContestController.class);
		MvArea mvArea = new MvAreaBuilder(AreaPath.from("111111.22." + selectedAreaId)).getValue();
		when(getAreaControllerMock().getSelectedMvArea()).thenReturn(mvArea);
		when(getAreaControllerMock().getCountryItems()).thenReturn(Collections.singletonList(new SelectItem("47", "Norge")));

		Election election = createMock(Election.class);
		when(election.getName()).thenReturn("ElectionName");
		when(election.isSingleArea()).thenReturn(isSingleArea);
		when(election.getElectionPath()).thenReturn(ElectionPath.from("111111.22.33"));
		stub_electionService_get(election);

		MvElection parentMvElection = createMock(MvElection.class);
		stub_findContestAreasForContestPath();
		if (create) {
			ctrl.prepareForCreate(parentMvElection);
			ctrl.getCurrentContest().setId("000005");
			ctrl.getCurrentContest().setName("AreaName");
		} else {
			when(getInjectMock(MvElectionService.class).findByPk(anyLong())).thenReturn(parentMvElection);
			MvElectionMinimal mvElection = createMock(MvElectionMinimal.class);
			when(mvElection.getPk()).thenReturn(10L);
			ctrl.prepareForUpdate(mvElection);
			when(ctrl.getCurrentContest().getId()).thenReturn("000005");
			when(ctrl.getCurrentContest().getName()).thenReturn("ContestName");
		}

		Contest currentContest = ctrl.getCurrentContest();
		when(getInjectMock(ContestService.class).save(eq(getUserDataMock()), any(Contest.class))).thenReturn(currentContest);
		return ctrl;
	}

	private MvAreaController getAreaControllerMock() {
		return getInjectMock(MvAreaController.class);
	}

	private void verify_findContestAreasForContestPath() {
		verify(getInjectMock(ContestAreaService.class), atLeastOnce()).findContestAreasForContestPath(any(UserData.class), any(ElectionPath.class));
	}

	private void stub_findContestAreasForContestPath() {
		when(getInjectMock(ContestAreaService.class).findContestAreasForContestPath(any(UserData.class), any(ElectionPath.class)))
				.thenReturn(new ArrayList<>());
	}

	private Election stub_electionService_get(Election election) {
		when(getInjectMock(ElectionService.class).get(eq(getUserDataMock()), any(ElectionPath.class))).thenReturn(election);
		return election;
	}
}

