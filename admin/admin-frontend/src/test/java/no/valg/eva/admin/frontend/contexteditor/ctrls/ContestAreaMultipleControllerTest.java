package no.valg.eva.admin.frontend.contexteditor.ctrls;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.service.configuration.ContestAreaService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaMultipleController;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class ContestAreaMultipleControllerTest extends BaseFrontendTest {

	@Test
	public void doGetCreateContestArea_withContest_verifyState() throws Exception {
		ContestAreaMultipleController ctrl = getCtrl();

		verify(getMvAreaMultipleControllerMock()).loadAndReset();
		verify(getMvAreaMultipleControllerMock()).setSelectedAreaLevel(3);
		verify(getMvAreaMultipleControllerMock()).setCountryId("47");
		verify(getMvAreaMultipleControllerMock()).changeCountry();
		verify(getMvAreaMultipleControllerMock()).setCountyId(null);
		assertThat(ctrl.getCurrentContest()).isNotNull();
	}

	@Test
	public void doCreateContestArea_withCountyOnly_verifySkipsAndAdded() throws Exception {
		ContestAreaMultipleController ctrl = getCtrl();
		stubMunicipalityList();

		ctrl.doCreateContestArea();

		verify(getInjectMock(ContestAreaService.class), times(3)).create(eq(getUserDataMock()), any(ContestArea.class));
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.create.successful");
	}

	@Test
	public void doCreateContestArea_withSingleAreaExists_verifyExistErrorMessage() throws Exception {
		ContestAreaMultipleController ctrl = getCtrl();
		when(getMvAreaMultipleControllerMock().getMunicipalityId()).thenReturn("0505");
		when(getMvAreaMultipleControllerMock().getSelectedMvArea()).thenReturn(mvArea(1));
		stub_throwOnCreate(ErrorCode.ERROR_CODE_0450_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_AREA, "kommune", "municipalityName", "Ditt valg");

		ctrl.doCreateContestArea();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.create.CHOOSE_UNIQUE_ID, kommune, municipalityName, Ditt valg]");
	}

	@Test
	public void doCreateContestArea_withSingleAreaExistInOtherContest_verifyExistErrorMessage() throws Exception {
		ContestAreaMultipleController ctrl = getCtrl();
		when(getMvAreaMultipleControllerMock().getMunicipalityId()).thenReturn("0505");
		when(getMvAreaMultipleControllerMock().getSelectedMvArea()).thenReturn(mvArea(2));
		stub_throwOnCreate(ErrorCode.ERROR_CODE_0451_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_ELECTION, "kommune", "municipalityName", "Mitt valg");

		ctrl.doCreateContestArea();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.create.CHOOSE_UNIQUE_ID, kommune, municipalityName, Mitt valg]");
	}

	@Test
	public void doCreateContestArea_withSingleArea_verifyCreateAndInfoMessage() throws Exception {
		ContestAreaMultipleController ctrl = getCtrl();
		when(getMvAreaMultipleControllerMock().getMunicipalityId()).thenReturn("0505");
		when(getMvAreaMultipleControllerMock().getSelectedMvArea()).thenReturn(mvArea(3));

		ctrl.doCreateContestArea();

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.create.successful");
		verify(getInjectMock(ContestAreaService.class), times(1)).create(eq(getUserDataMock()), any(ContestArea.class));
	}

	private ContestAreaMultipleController getCtrl() throws Exception {
		ContestAreaMultipleController ctrl = initializeMocks(ContestAreaMultipleController.class);
		Contest contest = createMock(Contest.class);
		when(contest.getElection().getAreaLevel()).thenReturn(3);
		stub_mvAreaController();

		ctrl.doGetCreateContestArea(contest);

		return ctrl;
	}

	private void stub_mvAreaController() {
		SelectItem item = new SelectItem("47", "Norge");
        when(getMvAreaMultipleControllerMock().getCountryItems()).thenReturn(Collections.singletonList(item));
		when(getMvAreaMultipleControllerMock().getCountyId()).thenReturn("05");
	}

	private void stub_throwOnCreate(ErrorCode errorCode, String... params) {
		doThrow(new EvoteException(errorCode, null, params)).when(getInjectMock(ContestAreaService.class))
				.create(eq(getUserDataMock()), any(ContestArea.class));
	}

	private void stubMunicipalityList() {
		when(getMvAreaMultipleControllerMock().getMunicipalityList()).thenReturn(Arrays.asList(mvArea(1), mvArea(2), mvArea(3)));
	}

	private MvArea mvArea(long pk) {
		MvArea mvArea = new MvArea();
		mvArea.setPk(pk);
		mvArea.setAreaLevel(3);
		mvArea.setMunicipalityName("municipalityName");
		return mvArea;
	}

	private MvAreaMultipleController getMvAreaMultipleControllerMock() {
		return getInjectMock(MvAreaMultipleController.class);
	}
}

