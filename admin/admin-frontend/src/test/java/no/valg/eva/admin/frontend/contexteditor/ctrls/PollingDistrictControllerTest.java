package no.valg.eva.admin.frontend.contexteditor.ctrls;

import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.LegacyPollingDistrictService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PollingDistrictControllerTest extends BaseFrontendTest {

	private static final String EDIT_POLLING_DISTRICT_HIDE_DIALOG = "editPollingDistrictHideDialog";

	private PollingDistrictController ctrl;
	private PollingDistrict newPollingDistrictMock;

	@DataProvider(name = "pollingDistrictTypeTextData")
	public static Object[][] pollingDistrictTypeTextData() {
		return new Object[][] {
				{ buildPollingDistrict(true, false, false, false), PollingDistrictController.POLLING_DISTRICT_TYPE_KEY_MUNICIPALITY },
				{ buildPollingDistrict(false, true, false, false), PollingDistrictController.POLLING_DISTRICT_TYPE_KEY_TECHNICAL },
				{ buildPollingDistrict(false, false, false, false), PollingDistrictController.POLLING_DISTRICT_TYPE_KEY_ORDINARY },
				{ buildPollingDistrict(false, false, true, false), PollingDistrictController.POLLING_DISTRICT_TYPE_KEY_PARENT },
				{ buildPollingDistrict(false, false, false, true), PollingDistrictController.POLLING_DISTRICT_TYPE_KEY_CHILD }
		};
	}

	private static PollingDistrict buildPollingDistrict(boolean isMunicipality, boolean isTechnicalPollingDistrict,
			boolean isParentPollingDistrict, boolean isChildPollingDistrict) {
		PollingDistrict pollingDistrict = new PollingDistrict();
		pollingDistrict.setMunicipality(isMunicipality);
		pollingDistrict.setTechnicalPollingDistrict(isTechnicalPollingDistrict);
		pollingDistrict.setParentPollingDistrict(isParentPollingDistrict);
		pollingDistrict.setChildPollingDistrict(isChildPollingDistrict);
		if (isChildPollingDistrict) {
			pollingDistrict.setPollingDistrict(new PollingDistrict());
		}
		return pollingDistrict;
	}

	@DataProvider(name = "setMvArea")
	public static Object[][] setMvArea() {
		return new Object[][] {
				{ true, true },
				{ false, false }
		};
	}

	@BeforeMethod
	public void setUp() throws Exception {
		ctrl = buildPollingDistrictController();

		// Setup mocks
		MvArea mvAreaStub = mock(MvArea.class, RETURNS_DEEP_STUBS);
		newPollingDistrictMock = mock(PollingDistrict.class, RETURNS_DEEP_STUBS);

		// Default mock behaviour
		when(mvAreaStub.getBorough().getPk()).thenReturn(1L);
		when(mvAreaStub.getBoroughName()).thenReturn("St Hanshaugen");
		when(mvAreaStub.getAreaPath()).thenReturn("100001.47.19.1938.193800.0005");
		when(mvAreaStub.getMunicipalityName()).thenReturn("Oslo");
		when(newPollingDistrictMock.getPk()).thenReturn(1L);
		when(newPollingDistrictMock.getId()).thenReturn("12.12");
		when(newPollingDistrictMock.getName()).thenReturn("Oslo");
		ctrl.setParentMvArea(mvAreaStub);
		setIdExists(false);
	}

	private PollingDistrictController buildPollingDistrictController() throws Exception {
		return initializeMocks(PollingDistrictController.class);
	}

	@Test
	public void getLevel5DialogHeader_withEnabledSaveButton_returnsStemmekrets() throws Exception {
		PollingDistrict pollingDistrict = mockField("pollingDistrict", PollingDistrict.class);
		when(pollingDistrict.isEditableCentrally()).thenReturn(true);
		enableSaveButton();

		assertThat(ctrl.getLevel5DialogHeader()).isEqualTo("@common.redact @area_level[5].name");
	}

	@Test
	public void doCreatePollingDistrict_withIdExists_checkMessagesAndInit() {
		setIdExists(true);
		ctrl.setPollingDistrictType(1);
		ctrl.setPollingDistrict(newPollingDistrictMock);

		ctrl.doCreatePollingDistrict();

		verifyAddMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.create.CHOOSE_UNIQUE_ID, @area_level[5].nummer, 12.12, St Hanshaugen]");
		verify(getRequestContextMock()).addCallbackParam("createPollingDistrictHideDialog", false);
	}

	@Test
	public void doCreatePollingDistrict_withEJBException_returnsErrorMessage() {
		when(getPollingDistrictServiceMock().create(getUserDataMock(), newPollingDistrictMock)).thenThrow(new EJBException());
		ctrl.setPollingDistrict(newPollingDistrictMock);

		ctrl.doCreatePollingDistrict();

		assertFacesMessage(SEVERITY_ERROR, "[@common.error.unexpected, a75da75b]");
	}

	@Test
	public void doCreatePollingDistrict_withEvoteException_checkMessages() {
		when(getPollingDistrictServiceMock().create(getUserDataMock(), newPollingDistrictMock)).thenThrow(new EvoteException("@hello"));
		ctrl.setPollingDistrict(newPollingDistrictMock);

		ctrl.doCreatePollingDistrict();

		verifyAddMessage(FacesMessage.SEVERITY_ERROR, "@hello");
	}

	@Test(dataProvider = "setMvArea")
	public void setMvArea_withDataProvider_checkState(boolean isParentPollingDistrict, boolean isCurrentParentPollingDistrict) {
		MvArea mvAreaMock = initSetMvArea(isParentPollingDistrict);

		assertThat(ctrl.getPollingDistrict()).isSameAs(mvAreaMock.getPollingDistrict());
		if (isParentPollingDistrict) {
			assertThat(ctrl.getPollingDistrictsForParentList()).isNotNull();
		} else {
			assertThat(ctrl.getPollingDistrictsForParentList()).isNull();
		}
		verify(mvAreaMock.getPollingDistrict()).setChildPollingDistrict(true);
	}

	@Test
	public void doDeletePollingDistrict_withNotRemoveable_checkMessage() {
		setCurrentRemovable(false);

		ctrl.doDeletePollingDistrict(newPollingDistrictMock);

		verifyAddMessage(FacesMessage.SEVERITY_INFO, "[@common.message.remove.not_allowed, Oslo]");
		verify(getRequestContextMock()).addCallbackParam(EDIT_POLLING_DISTRICT_HIDE_DIALOG, false);
	}

	@Test
	public void getPollingDistrictTypes() {
		List<SelectItem> items = ctrl.getPollingDistrictTypes();

		assertThat(items.size()).isEqualTo(2);
	}

	@Test(dataProvider = "pollingDistrictTypeTextData")
	public void getPollingDistrictTypeText_forAGivenPollingDistrict_returnsATranslatedStringExplainingItsType(PollingDistrict pollingDistrict,
			String expectedTranslation) throws Exception {
		PollingDistrictController pollingDistrictController = buildPollingDistrictController();

		String translation = pollingDistrictController.getPollingDistrictTypeText(pollingDistrict);

		assertThat(translation).isEqualTo(expectedTranslation);
	}

	@Test
	public void getPollingDistrictTypeText_forANullParameter_returnsEmptyString() throws Exception {
		PollingDistrictController pollingDistrictController = buildPollingDistrictController();

		String translation = pollingDistrictController.getPollingDistrictTypeText(null);

		assertThat(translation).isEqualTo("");
	}

	private void setCurrentRemovable(boolean removable) {
		when(getUserDataControllerMock().getElectionEvent().getElectionEventStatus().getId()).thenReturn(removable ? EvoteConstants.FREEZE_LEVEL_AREA - 1
				: EvoteConstants.FREEZE_LEVEL_AREA);
		PollingDistrict pd = mock(PollingDistrict.class);
		when(pd.isChildPollingDistrict()).thenReturn(!removable);
		ctrl.setPollingDistrict(pd);
	}

	private MvArea initSetMvArea(boolean isParentPollingDistrict) {
		MvArea mvAreaMock = mock(MvArea.class, RETURNS_DEEP_STUBS);
		when(mvAreaMock.getPollingDistrict().isParentPollingDistrict()).thenReturn(isParentPollingDistrict);
		if (isParentPollingDistrict) {
			when(getPollingDistrictServiceMock().findPollingDistrictsForParent(any(UserData.class), any(PollingDistrict.class)))
					.thenReturn(new ArrayList<>());
			List<PollingDistrict> list = new ArrayList<>();
			list.add(mock(PollingDistrict.class));
			list.add(mock(PollingDistrict.class));
			when(list.get(0).isChildPollingDistrict()).thenReturn(true);
			when(list.get(0).getPk()).thenReturn(100L);
			when(list.get(1).isChildPollingDistrict()).thenReturn(false);
			when(list.get(1).getPk()).thenReturn(101L);
		}

		ctrl.setMvArea(mvAreaMock);
		return mvAreaMock;
	}

	private void setIdExists(boolean idExists) {
		when(getPollingDistrictServiceMock().findPollingDistrictById(any(UserData.class), anyLong(), anyString())).thenReturn(
				idExists ? new PollingDistrict() : null);
	}

	private void verifyAddMessage(FacesMessage.Severity severity, String summary) {
		assertFacesMessage(severity, summary);
	}

	private void enableSaveButton() {
		when(getUserDataControllerMock().getUserAccess().isKonfigurasjonGeografi()).thenReturn(true);

		/*
		boolean hasCurrentPollingDistrictWhichIsParent = userDataController.getUserAccess().isKonfigurasjonGeografi()
				&& currentPollingDistrict != null && currentPollingDistrict.erForelderstemmekrets();
		return !hasCurrentPollingDistrictWhichIsParent || isReadOnly();
		 */
	}

	private UserDataController getUserDataControllerMock() {
		return getInjectMock(UserDataController.class);
	}

	private LegacyPollingDistrictService getPollingDistrictServiceMock() {
		return getInjectMock(LegacyPollingDistrictService.class);
	}

}

