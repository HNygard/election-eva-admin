package no.valg.eva.admin.frontend.counting.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.BallotCount;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.common.ValgdistriktOpptellingskategoriOgValggeografiHolder;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;
import static no.valg.eva.admin.frontend.common.Button.renderedAndEnabled;
import static no.valg.eva.admin.frontend.common.ButtonType.APPROVE;
import static no.valg.eva.admin.frontend.common.ButtonType.APPROVE_TO_SETTLEMENT;
import static no.valg.eva.admin.frontend.common.ButtonType.REGISTER_CORRECTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BaseApproveRejectedCountControllerTest extends BaseFrontendTest {

    private static final String CONTEST_PATH = "111111.22.33.444444";
    private static final String AREA_PATH = "111111.22.33.4444";

	@Test
	public void isReportingUnitOnContestLevel_withUserOnMunicipalityLevel_returnsFalse() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.COUNTY);

		assertThat(ctrl.isReportingUnitOnContestLevel()).isFalse();
	}

	@Test
	public void isReportingUnitOnContestLevel_withUserOnCountyLevel_returnsTrue() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.COUNTY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.COUNTY);

		assertThat(ctrl.isReportingUnitOnContestLevel()).isTrue();
	}

	@Test
	public void buildCountContext_withSelected_returnsCorrectCountContext() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		ValgdistriktOpptellingskategoriOgValggeografiHolder holder = getInjectMock(ValgdistriktOpptellingskategoriOgValggeografiHolder.class);
		when(holder.getSelectedCountCategory()).thenReturn(CountCategory.FO);
		when(holder.getSelectedContestInfo().getElectionPath()).thenReturn(ElectionPath.from(CONTEST_PATH));

		CountContext ctx = ctrl.buildCountContext();
		CountContext ctx2 = ctrl.getCountContext();

		assertThat(ctx.getCategory()).isEqualTo(CountCategory.FO);
		assertThat(ctx.getContestPath()).isEqualTo(ElectionPath.from(CONTEST_PATH));
		assertThat(ctx).isEqualTo(ctx2);
	}

	@Test
	public void getElectionName_withSelectedContest_returnsContest() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		when(getInjectMock(ValgdistriktOpptellingskategoriOgValggeografiHolder.class).getSelectedContestInfo().getElectionName()).thenReturn("Contest");

		assertThat(ctrl.getElectionName()).isEqualTo("Contest");
	}

	@Test
	public void getMunicipalityName_withSelectedArea_returnsMunicipality() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		when(getInjectMock(ValgdistriktOpptellingskategoriOgValggeografiHolder.class).getSelectedMvArea().getMunicipalityName()).thenReturn("Municipality");

		assertThat(ctrl.getMunicipalityName()).isEqualTo("Municipality");
	}

	@Test
	public void getAreaPageTitleMeta_withFinalCount_shouldReturn2Elements() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		List<PageTitleMetaModel> list = new ArrayList<>();
		list.add(createMock(PageTitleMetaModel.class));
		when(getInjectMock(PageTitleMetaBuilder.class).area(any(MvArea.class))).thenReturn(list);
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(finalCountMock.getStatus()).thenReturn(APPROVED);
		ctrl.finalCount = finalCountMock;

		List<PageTitleMetaModel> result = ctrl.getAreaPageTitleMeta();

		assertThat(result).hasSize(2);
		assertThat(result.get(1).getLabel()).isEqualTo("@statistic.column.countStatus");
		assertThat(result.get(1).getValue()).isEqualTo("@vote_count_status[APPROVED].name");
	}

	private void stub_userData_getOperatorAreaLevel(AreaLevelEnum areaLevelEnum) {
		when(getUserDataMock().getOperatorAreaLevel()).thenReturn(areaLevelEnum);
	}

	private void stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum areaLevelEnum) {
		when(getInjectMock(ValgdistriktOpptellingskategoriOgValggeografiHolder.class).getSelectedContestInfo().getAreaLevel()).thenReturn(areaLevelEnum);
	}

	private BaseApproveRejectedCountController create() throws Exception {
		return initializeMocks(new BaseApproveRejectedCountController() {
			@Override
			protected String buildFromUrlPart() {
				return "test=test";
			}

			@Override
			protected boolean resolveErrorStateAndMessages(FinalCount finalCount) {
				return false;
			}

			@Override
			public boolean isEditMode() {
				return false;
			}
			@Override
			public boolean isScanned() {
				return false;
			}
		});
	}

	@Test
	public void button_givenApproveAndReportingUnitOnContestLevel_returnNotRendered() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.MUNICIPALITY);

		Button button = ctrl.button(APPROVE);

		assertThat(button).isEqualTo(notRendered());
	}

	@Test
	public void button_givenApproveAndNotReportingUnitOnContestLevel_returnRenderedAndEnabled() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.COUNTY);
		ctrl.finalCount = createMock(FinalCount.class);

		Button button = ctrl.button(APPROVE);

		assertThat(button).isEqualTo(enabled(true));
	}

	@Test
	public void button_givenApproveAndNotReportingUnitOnContestLevelAndRejectedBallotsProcessed_returnRenderedAndDisabled() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.COUNTY);
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(finalCountMock.isRejectedBallotsProcessed()).thenReturn(true);
		ctrl.finalCount = finalCountMock;

		Button button = ctrl.button(APPROVE);

		assertThat(button).isEqualTo(enabled(false));
	}

	@Test
	public void button_givenApproveToSettlementAndNotReportingUnitOnContestLevel_returnNotRendered() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.COUNTY);

		Button button = ctrl.button(APPROVE_TO_SETTLEMENT);

		assertThat(button).isEqualTo(notRendered());
	}

	@Test
	public void button_givenApproveToSettlementAndReportingUnitOnContestLevelAndModifiedBallotsProcessedFalse_returnRenderedAndDisabled() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.MUNICIPALITY);
		mockNewBallotCountsField(singletonList(ballotCount(0, 0)));
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(finalCountMock.isModifiedBallotsProcessed()).thenReturn(false);
		ctrl.finalCount = finalCountMock;

		Button button = ctrl.button(APPROVE_TO_SETTLEMENT);

		assertThat(button).isEqualTo(enabled(false));
	}

	@Test
	public void button_givenApproveToSettlementAndReportingUnitOnContestLevel_returnRenderedAndEnabled() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.MUNICIPALITY);
		mockNewBallotCountsField(singletonList(ballotCount(0, 0)));
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(finalCountMock.getStatus()).thenReturn(APPROVED);
		when(finalCountMock.isModifiedBallotsProcessed()).thenReturn(true);
		ctrl.finalCount = finalCountMock;

		Button button = ctrl.button(APPROVE_TO_SETTLEMENT);

		assertThat(button).isEqualTo(renderedAndEnabled());
	}

	private void mockNewBallotCountsField(List<BallotCount> ballotCounts) throws NoSuchFieldException, IllegalAccessException {
		mockFieldValue("newBallotCounts", ballotCounts);
	}

	@Test
	public void button_givenApproveToSettlementAndReportingUnitOnContestLevel_returnButtonWithNameApproveAndToSettlement() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.MUNICIPALITY);
		mockNewBallotCountsField(singletonList(ballotCount(0, 0)));
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(finalCountMock.getStatus()).thenReturn(APPROVED);
		when(finalCountMock.isModifiedBallotsProcessed()).thenReturn(true);
		ctrl.finalCount = finalCountMock;

		Button button = ctrl.button(APPROVE_TO_SETTLEMENT);

		assertThat(button.getName()).isEqualTo("@count.ballot.approve.rejected.ApproveAndToSettlement");
	}

	@Test
	public void button_givenApproveToSettlementAndReportingUnitOnContestLevelAndRejectedBallotsProcessed_returnButtonWithNameToSettlement() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.MUNICIPALITY);
		mockNewBallotCountsField(singletonList(ballotCount(0, 0)));
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(finalCountMock.getStatus()).thenReturn(APPROVED);
		when(finalCountMock.isModifiedBallotsProcessed()).thenReturn(true);
		when(finalCountMock.isRejectedBallotsProcessed()).thenReturn(true);
		ctrl.finalCount = finalCountMock;

		Button button = ctrl.button(APPROVE_TO_SETTLEMENT);

		assertThat(button.getName()).isEqualTo("@count.ballot.approve.rejected.toSettlement");
	}

	@Test
	public void button_givenApproveToSettlementAndReportingUnitOnContestLevelAndToSettlement_returnRenderedAndDisabled() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.MUNICIPALITY);
		mockNewBallotCountsField(singletonList(ballotCount(0, 0)));
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(finalCountMock.getStatus()).thenReturn(TO_SETTLEMENT);
		when(finalCountMock.isModifiedBallotsProcessed()).thenReturn(true);
		ctrl.finalCount = finalCountMock;

		Button button = ctrl.button(APPROVE_TO_SETTLEMENT);

		assertThat(button).isEqualTo(enabled(false));
	}

	@Test
	public void button_givenRegisterCorrectionsAndNotReportingUnitOnContestLevel_returnNotRendered() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.COUNTY);

		Button button = ctrl.button(REGISTER_CORRECTIONS);

		assertThat(button).isEqualTo(notRendered());
	}

	@Test
	public void button_givenRegisterCorrectionsAndReportingUnitOnContestLevelAndHasNoModifiedBallotCounts_returnRenderedAndEnabled() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.MUNICIPALITY);
		mockNewBallotCountsField(singletonList(ballotCount(0, 0)));
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(finalCountMock.getStatus()).thenReturn(APPROVED);
		when(finalCountMock.isModifiedBallotsProcessed()).thenReturn(true);
		ctrl.finalCount = finalCountMock;

		Button button = ctrl.button(REGISTER_CORRECTIONS);

		assertThat(button).isEqualTo(renderedAndEnabled());
	}

	@Test
	public void button_givenRegisterCorrectionsAndReportingUnitOnContestLevelAndModifiedBallotsProcessedFalse_returnRenderedAndEnabled() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.MUNICIPALITY);
		mockNewBallotCountsField(singletonList(ballotCount(0, 0)));
		FinalCount finalCountMock = createMock(FinalCount.class);
		when(finalCountMock.getStatus()).thenReturn(APPROVED);
		when(finalCountMock.isModifiedBallotsProcessed()).thenReturn(false);
		ctrl.finalCount = finalCountMock;

		Button button = ctrl.button(REGISTER_CORRECTIONS);

		assertThat(button).isEqualTo(renderedAndEnabled());
	}

	private BallotCount ballotCount(int unmodifiedCount, int modifiedCount) {
		return new BallotCount("id", "name", unmodifiedCount, modifiedCount);
	}

	@Test
	public void button_givenRegisterCorrectionsAndReportingUnitOnContestLevelAndHasModifiedBallotCounts_returnRenderedAndEnabled() throws Exception {
		BaseApproveRejectedCountController ctrl = create();
		stub_userData_getOperatorAreaLevel(AreaLevelEnum.MUNICIPALITY);
		stub_picker_getSelectedContestInfoGetAreaLevel(AreaLevelEnum.MUNICIPALITY);
		mockNewBallotCountsField(singletonList(ballotCount(0, 1)));

		Button button = ctrl.button(REGISTER_CORRECTIONS);

		assertThat(button).isEqualTo(renderedAndEnabled());
	}
}
