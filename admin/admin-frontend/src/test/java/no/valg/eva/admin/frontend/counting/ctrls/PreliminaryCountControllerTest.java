package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.CountQualifier;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.counting.validator.ApprovePreliminaryCountValidator;
import no.valg.eva.admin.common.counting.validator.ApprovePreliminaryCountValidatorForOtherCategories;
import no.valg.eva.admin.common.counting.validator.ApprovePreliminaryCountValidatorForVo;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.counting.view.MarkOffCountsModelForAllPollingDistricts;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.Arrays;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Rediger;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Opphev_Foreløpig_Telling;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Rediger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class PreliminaryCountControllerTest extends BaseCountControllerTest {

	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	private PreliminaryCountController ctrl;
	private PreliminaryCount preliminaryCountStub;

	@BeforeMethod
	public void setUp() throws Exception {
		ctrl = initializeMocks(PreliminaryCountController.class);
		preliminaryCountStub = mock(PreliminaryCount.class);
		when(getCountsMock().getPreliminaryCount()).thenReturn(preliminaryCountStub);
	}

	@Test
    public void saveCount_withValidateFail_shouldDisplayErrorMessage() {
		validateException().when(preliminaryCountStub).validate();

		ctrl.saveCount();

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void saveCount_withValidData_checkState() {
		ctrl.saveCount();

		assertThat(ctrl.getPreliminaryCount().getId()).isEqualTo(preliminaryCountStub.getId());
		verify(getMessageProviderMock()).get("@count.isSaved");
        verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
	}

	@Test
    public void approveCount_withValidateForApprovalFail_shouldDisplayErrorMessage() {
		validateException().when(preliminaryCountStub).validateForApproval();

		ctrl.approveCount();

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void approveCount_withValidData_checkState() {
		ctrl.approveCount();

		assertThat(ctrl.getPreliminaryCount().getId()).isEqualTo(preliminaryCountStub.getId());
		verify(getMessageProviderMock()).get("@count.isApproved");
        verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
	}

	@Test
    public void revokeApprovedCount_withException_shouldDisplayErrorMessage() {
		validateException().when(getCountingServiceMock()).revokeCount(any(UserData.class), any(CountContext.class),
				any(PreliminaryCount.class));

		ctrl.revokeApprovedCount();

		verify(getMessageProviderMock()).get("@error@", null);
	}

	@Test
    public void revokeApprovedCount_withValidData_checkState() {
		ctrl.revokeApprovedCount();

		assertThat(ctrl.getPreliminaryCount().getId()).isEqualTo(preliminaryCountStub.getId());
		verify(getMessageProviderMock()).get("@count.isNotApprovedAnymore");
        verify(getFacesContextMock()).addMessage(any(), any(FacesMessage.class));
	}

	@Test
    public void isApproved_withNoCount_returnsFalse() {
		when(getCountsMock().getPreliminaryCount()).thenReturn(null);

		assertThat(ctrl.isApproved()).isFalse();
	}

	@Test
    public void isApproved_withApprovedCount_returnsTrue() {
		when(preliminaryCountStub.isApproved()).thenReturn(true);

		assertThat(ctrl.isApproved()).isTrue();
	}

	@Test
    public void hasProtocolCounts_withNoProtocolCounts_returnsFalse() {
		when(getCountsMock().hasProtocolCounts()).thenReturn(false);

		assertThat(ctrl.hasProtocolCounts()).isFalse();
	}

	@Test
    public void getProtocolCount_withSelectedIndex_returnsIndexCount() {
		ProtocolCount stub1 = mock(ProtocolCount.class, RETURNS_DEEP_STUBS);
		ProtocolCount stub2 = mock(ProtocolCount.class, RETURNS_DEEP_STUBS);
		when(getCountsMock().getProtocolCounts()).thenReturn(Arrays.asList(stub1, stub2));
		ctrl.selectProtocolCount(1);

		assertThat(ctrl.getProtocolCount().getId()).isEqualTo(stub2.getId());
	}

	@Test
    public void getProtocolCount_withNoSelectedIndex_returnsFirstCount() {
		ProtocolCount stub1 = mock(ProtocolCount.class, RETURNS_DEEP_STUBS);
		ProtocolCount stub2 = mock(ProtocolCount.class, RETURNS_DEEP_STUBS);
		when(getCountsMock().getProtocolCounts()).thenReturn(Arrays.asList(stub1, stub2));
		ctrl.selectProtocolCount(null);

		assertThat(ctrl.getProtocolCount().getId()).isEqualTo(stub1.getId());
	}

	@Test
    public void button_withSaveFalse_returnDisabled() {
		Button button = ctrl.button(ButtonType.SAVE);

		assertThat(button.isDisabled()).isTrue();
	}

	@Test
    public void button_withSaveTrue_returnEnabled() {
		hasAccess(Opptelling_Valgting_Rediger);
		when(getCountsMock().getPreliminaryCount().isAntallStemmesedlerLagtTilSideLagret()).thenReturn(true);
		when(getCountsMock().getPreliminaryCount().isEditable()).thenReturn(true);

		Button button = ctrl.button(ButtonType.SAVE);

		assertThat(button.isDisabled()).isFalse();
	}

	@Test
    public void button_withApproveFalse_returnDisabled() {
		Button button = ctrl.button(ButtonType.APPROVE);

		assertThat(button.isDisabled()).isTrue();
	}

	@Test
    public void button_withApproveTrue_returnEnabled() {
		hasAccess(Opptelling_Forhånd_Rediger);
		when(getCountsMock().getPreliminaryCount().isAntallStemmesedlerLagtTilSideLagret()).thenReturn(true);
		when(getCountsMock().getPreliminaryCount().isEditable()).thenReturn(true);

		Button button = ctrl.button(ButtonType.APPROVE);

		assertThat(button.isDisabled()).isFalse();
	}

	@Test
    public void button_withRevokeFalse_returnDisabled() {
		Button button = ctrl.button(ButtonType.REVOKE);

		assertThat(button.isDisabled()).isTrue();
	}

	@Test
    public void button_withRevokeTrue_returnEnabled() {
		hasAccess(Opptelling_Opphev_Foreløpig_Telling);
		when(preliminaryCountStub.isApproved()).thenReturn(true);

		Button button = ctrl.button(ButtonType.REVOKE);

		assertThat(button.isDisabled()).isFalse();
	}

	@Test
    public void isIncludeProtocolCount_withNoProtocolCounts_returnsFalse() {
		when(getCountsMock().hasProtocolCounts()).thenReturn(false);

		assertThat(ctrl.isIncludeProtocolCount()).isFalse();
	}

	@Test
    public void isIncludeProtocolCount_withProtocolCounts_returnsTrue() {
		when(getCountsMock().hasProtocolCounts()).thenReturn(true);

		assertThat(ctrl.isIncludeProtocolCount()).isTrue();
	}

	@Test
    public void getCount_withPrelimCount_isTheSame() {
		assertThat(ctrl.getCount()).isSameAs(preliminaryCountStub);
	}

	@Test
    public void getTotalBallotCountDifferenceFromPreviousCount_withProtocolCounts_returnsTotalBallotDifference() {
		when(getCountsMock().hasProtocolCounts()).thenReturn(true);
		when(getCountsMock().getTotalBallotCountDifferenceBetween(CountQualifier.PROTOCOL, CountQualifier.PRELIMINARY)).thenReturn(10);

		assertThat(ctrl.getTotalBallotCountDifferenceFromPreviousCount()).isEqualTo(10);
	}

	@Test
    public void getTotalBallotCountDifferenceFromPreviousCount_withNoProtocolCounts_returnsBallotMinusMarkoff() {
		when(getCountsMock().hasProtocolCounts()).thenReturn(false);
		when(getCountsMock().getMarkOffCount()).thenReturn(3);
		when(getCountsMock().getPreliminaryCount().getExpectedBallotCount()).thenReturn(null);
		when(preliminaryCountStub.getTotalBallotCount()).thenReturn(10);
		stub_countCategory(VO);

		assertThat(ctrl.getTotalBallotCountDifferenceFromPreviousCount()).isEqualTo(7);
	}

	@Test
    public void getTotalBallotCountDifferenceFromPreviousCount_withExpectedBallotCount_returnCorrectDifference() {
		when(getCountsMock().getPreliminaryCount().getTotalBallotCount()).thenReturn(2);
		when(getCountsMock().getPreliminaryCount().getExpectedBallotCount()).thenReturn(3);
		when(getCountsMock().getPreliminaryCount().getMarkOffCount()).thenReturn(4);

		int totalBallotCountDifferenceFromPreviousCount = ctrl.getTotalBallotCountDifferenceFromPreviousCount();

		assertThat(totalBallotCountDifferenceFromPreviousCount).isEqualTo(-1);
	}

	@Test
    public void getTotalMarkOffCount_withCategoryFO_checkResult() {
		when(getCountsMock().getMarkOffCount()).thenReturn(10);
		when(preliminaryCountStub.getLateValidationCovers()).thenReturn(6);
		stub_countCategory(FO);

		assertThat(ctrl.getTotalMarkOffCount()).isEqualTo(4);
	}

	@Test
    public void getTotalMarkOffCount_withCategoryFS_checkResult() {
		when(getCountsMock().getMarkOffCount()).thenReturn(10);
		when(preliminaryCountStub.getLateValidationCovers()).thenReturn(6);
		stub_countCategory(FS);

		assertThat(ctrl.getTotalMarkOffCount()).isEqualTo(16);
	}

	@Test
    public void getTotalMarkOffCount_withNoMarkOffCount_returnsNull() {
		when(getCountsMock().getMarkOffCount()).thenReturn(null);

		Integer totalMarkOffCount = ctrl.getTotalMarkOffCount();

		assertThat(totalMarkOffCount).isNull();
	}

	@Test
    public void getTotalMarkOffCount_withDailyMarkoffs_returnsDailyMarkoffs() {
		DailyMarkOffCounts dailyMarkOffCounts = createMock(DailyMarkOffCounts.class);
		when(dailyMarkOffCounts.getMarkOffCount()).thenReturn(20);
		when(getCountsMock().getPreliminaryCount().getDailyMarkOffCounts()).thenReturn(dailyMarkOffCounts);
		stub_countCategory(VO);

		Integer totalMarkOffCount = ctrl.getTotalMarkOffCount();

		assertThat(totalMarkOffCount).isEqualTo(20);
	}

	@Test
    public void renderMultipleProtocolCountsView_withNoProtocolCounts_returnsFalse() {
		when(getCountsMock().hasProtocolCounts()).thenReturn(false);

		assertThat(ctrl.renderMultipleProtocolCountsView()).isEqualTo(false);

	}

	@Test
    public void renderMultipleProtocolCountsView_withMultipleProtocolCounts_returnsTrue() {
		when(getCountsMock().hasProtocolCounts()).thenReturn(true);
		when(getCountsMock().getProtocolCounts()).thenReturn(mockList(2, ProtocolCount.class));

		assertThat(ctrl.renderMultipleProtocolCountsView()).isEqualTo(true);
	}

	@Test
    public void renderSingleProtocolCountView_withNoSelected_returnsFalse() {
		assertThat(ctrl.renderSingleProtocolCountView()).isEqualTo(false);
	}

	@Test
    public void renderSingleProtocolCountView_withSelected_returnsTrue() {
		ctrl.selectProtocolCount(1);

		assertThat(ctrl.renderSingleProtocolCountView()).isEqualTo(true);
	}

	@Test
    public void renderSingleProtocolCountView_withOneProtocolCount_returnsTrue() {
		when(getCountsMock().getProtocolCounts()).thenReturn(mockList(1, ProtocolCount.class));

		assertThat(ctrl.renderSingleProtocolCountView()).isEqualTo(true);
	}

	@Test
    public void getMarkOffCountsModelForAllPollingDistricts_whenTotalBallotCountForOtherPollingDistricts_returnsModel() {
		MarkOffCountsModelForAllPollingDistricts model = ctrl.getMarkOffCountsModelForAllPollingDistricts();

		assertThat(model).isNotNull();
	}

	@Test
    public void getMarkOffCountsModelForAllPollingDistricts_whenNoTotalBallotCountForOtherPollingDistricts_returnsNull() {
		when(getCountsMock().getPreliminaryCount().getTotalBallotCountForOtherPollingDistricts()).thenReturn(null);

		MarkOffCountsModelForAllPollingDistricts model = ctrl.getMarkOffCountsModelForAllPollingDistricts();

		assertThat(model).isNull();
	}

	@Test
    public void isIncludeExpectedBallotCount_givenExpectedBallotCountAndMarkOffCount_returnsFalse() {
		when(getCountsMock().getPreliminaryCount().getExpectedBallotCount()).thenReturn(1);
		when(getCountsMock().getPreliminaryCount().getMarkOffCount()).thenReturn(1);

		boolean includeExpectedBallotCount = ctrl.isIncludeExpectedBallotCount();

		assertThat(includeExpectedBallotCount).isFalse();
	}

	@Test
    public void isIncludeExpectedBallotCount_givenNoExpectedBallotCount_returnsFalse() {
		boolean includeExpectedBallotCount = ctrl.isIncludeExpectedBallotCount();

		assertThat(includeExpectedBallotCount).isFalse();
	}

	@Test
    public void isIncludeExpectedBallotCount_givenExpectedBallotCountAndNoMarkOffCount_returnsTrue() {
		when(getCountsMock().getPreliminaryCount().getExpectedBallotCount()).thenReturn(1);

		boolean includeExpectedBallotCount = ctrl.isIncludeExpectedBallotCount();

		assertThat(includeExpectedBallotCount).isFalse();
	}

	@Test(dataProvider = "getAreaName")
    public void getAreaName_withDataProvider_verifyExpected(String path, String expected) {
		ProtocolCount protocolCount = new ProtocolCount("id", AreaPath.from(path), "areaName", "reportingUnitAreaName", true);

		assertThat(ctrl.getAreaName(protocolCount)).isEqualTo(expected);
	}

	@DataProvider(name = "getAreaName")
	public Object[][] getAreaName() {
		return new Object[][] {
				{ "111111.22", "areaName" },
				{ "111111.22.33.4444.555555.6666", "6666 areaName" },
		};
	}

	@Test
	public void isCommentRequired_withNonVOCategoryAndCountCommentRequired_returnsTrue() throws Exception {
		PreliminaryCountController ctrl = ctrl_withPreliminaryCount();
		when(ctrl.getPreliminaryCount().getCategory()).thenReturn(CountCategory.BF);
		when(ctrl.getPreliminaryCount().isCommentRequired()).thenReturn(true);

		assertThat(ctrl.isCommentRequired()).isTrue();
	}

	@Test
	public void isElectronicMarkOffs_withNoElectronicMarkoffOnCount_returnsFalse() throws Exception {
		PreliminaryCountController ctrl = ctrl_withPreliminaryCount();
		when(ctrl.getPreliminaryCount().isElectronicMarkOffs()).thenReturn(false);

		assertThat(ctrl.isElectronicMarkOffs()).isFalse();
	}

	@Test
	public void getDailyMarkOffCounts_withNoCount_returnsNull() throws Exception {
		PreliminaryCountController ctrl = initializeMocks(PreliminaryCountController.class);
		when(getCountsMock().getPreliminaryCount()).thenReturn(null);

		assertThat(ctrl.getDailyMarkOffCounts()).isNull();
	}

	@Test
	public void getDailyMarkOffCounts_withCount_returnsCountDailyMarkoffs() throws Exception {
		PreliminaryCountController ctrl = ctrl_withDailyMarkOffCounts();

		assertThat(ctrl.getDailyMarkOffCounts()).isNotNull();
	}

	@Test
	public void getDailyMarkOffCountsModel_withNoDailyMarkoffCounts_returnsNull() throws Exception {
		PreliminaryCountController ctrl = initializeMocks(PreliminaryCountController.class);
		when(getCountsMock().getPreliminaryCount()).thenReturn(null);

		assertThat(ctrl.getDailyMarkOffCountsModel()).isNull();
	}

	@Test
	public void getDailyMarkOffCountsModel_withDailyMarkoffCounts_returnsModel() throws Exception {
		PreliminaryCountController ctrl = ctrl_withDailyMarkOffCounts();

		assertThat(ctrl.getDailyMarkOffCountsModel()).isNotNull();
	}

	@Test(dataProvider = "isIncludeMarkOffCount")
	public void isIncludeMarkOffCount_withDataProvider_verifyExpected(boolean withMarkOffCount, boolean withDailyMarkoffs, boolean expected) throws Exception {
		PreliminaryCountController ctrl = ctrl_withPreliminaryCount();
		if (withDailyMarkoffs) {
			ctrl = ctrl_withDailyMarkOffCounts();
		} else {
			when(ctrl.getPreliminaryCount().getDailyMarkOffCounts()).thenReturn(null);
		}
		if (withMarkOffCount) {
			when(ctrl.getPreliminaryCount().getMarkOffCount()).thenReturn(20);
		} else {
			when(ctrl.getPreliminaryCount().getMarkOffCount()).thenReturn(null);
		}

		assertThat(ctrl.isIncludeMarkOffCount()).isEqualTo(expected);
	}

	@DataProvider(name = "isIncludeMarkOffCount")
	public Object[][] isIncludeMarkOffCount() {
		return new Object[][] {
				{ false, false, false },
				{ true, false, true },
				{ false, true, true },
				{ true, true, true }
		};
	}

	@Test
	public void getValidator_withCategoryVO_verifyResponse() throws Exception {
		PreliminaryCountController ctrl = ctrl_withPreliminaryCount();
		when(ctrl.getPreliminaryCount().getCategory()).thenReturn(VO);
		when(ctrl.getPreliminaryCount().getDailyMarkOffCounts()).thenReturn(null);

		ApprovePreliminaryCountValidator validator = ctrl.getValidator();

		verify(ctrl.getCounts()).getTotalBallotCountForProtocolCounts();
		assertThat(validator).isExactlyInstanceOf(ApprovePreliminaryCountValidatorForVo.class);
	}

	@Test
	public void getValidator_withCategoryVOAndDailyMarkoffs_verifyResponse() throws Exception {
		PreliminaryCountController ctrl = ctrl_withPreliminaryCount();
		when(ctrl.getPreliminaryCount().getCategory()).thenReturn(VO);
		when(ctrl.getPreliminaryCount().getDailyMarkOffCounts()).thenReturn(new DailyMarkOffCounts());

		ApprovePreliminaryCountValidator validator = ctrl.getValidator();

		verify(ctrl.getPreliminaryCount(), atLeastOnce()).getDailyMarkOffCounts();
		assertThat(validator).isExactlyInstanceOf(ApprovePreliminaryCountValidatorForVo.class);
	}

	@Test
	public void getValidator_withCategoryFS_verifyResponse() throws Exception {
		PreliminaryCountController ctrl = ctrl_withPreliminaryCount();
		when(ctrl.getPreliminaryCount().getCategory()).thenReturn(FS);

		ApprovePreliminaryCountValidator validator = ctrl.getValidator();

		assertThat(validator).isExactlyInstanceOf(ApprovePreliminaryCountValidatorForOtherCategories.class);
	}

	@Test
	public void isCountEditable_withUserOnCountyLevel_returnsFalse() {
		when(getStartCountingControllerMock().isUserOnCountyLevel()).thenReturn(true);
		when(getCountsMock().getPreliminaryCount().getCategory()).thenReturn(CountCategory.FO);
		when(getCountsMock().getPreliminaryCount().isEditable()).thenReturn(true);

		assertThat(ctrl.isCountEditable()).isFalse();
	}

	@Test
	public void isCountEditable_withoutAntallStemmesedlerLagtTilSideLagret_returnsFalse() {
		when(getCountsMock().getPreliminaryCount().isAntallStemmesedlerLagtTilSideLagret()).thenReturn(false);
		when(getCountsMock().getPreliminaryCount().getCategory()).thenReturn(CountCategory.FO);
		when(getCountsMock().getPreliminaryCount().isEditable()).thenReturn(true);

		assertThat(ctrl.isCountEditable()).isFalse();
	}

	private PreliminaryCountController ctrl_withDailyMarkOffCounts() throws Exception {
		PreliminaryCountController ctrl = ctrl_withPreliminaryCount();
		DailyMarkOffCounts dailyMarkOffCounts = createMock(DailyMarkOffCounts.class);
		when(ctrl.getPreliminaryCount().getDailyMarkOffCounts()).thenReturn(dailyMarkOffCounts);
		return ctrl;
	}

	private PreliminaryCountController ctrl_withPreliminaryCount() throws Exception {
		PreliminaryCountController ctrl = initializeMocks(PreliminaryCountController.class);
		when(getCountsMock().getPreliminaryCount()).thenReturn(createMock(PreliminaryCount.class));
		return ctrl;
	}

	private void stub_countCategory(CountCategory category) {
		when(getStartCountingControllerMock().getCountContext()).thenReturn(new CountContext(ElectionPath.from("111111.11.11.111111"), category));
	}

	@Test
    public void sjekkAntallStemmesedlerLagtTilSideLagret_withNotAntallStemmesedlerLagtTilSideLagretAndCountIsEditable_shouldDisplayErrorMessage() {
		when(getCountsMock().getPreliminaryCount().isAntallStemmesedlerLagtTilSideLagret()).thenReturn(false);
		when(getCountsMock().getPreliminaryCount().getCategory()).thenReturn(CountCategory.FO);
		when(getCountsMock().getPreliminaryCount().isEditable()).thenReturn(true);

		ctrl.sjekkAntallStemmesedlerLagtTilSideLagret();

		verify(getMessageProviderMock()).get("@opptelling.antallStemmesedlerLagtTilSide.ikkeLagret");
	}

}


