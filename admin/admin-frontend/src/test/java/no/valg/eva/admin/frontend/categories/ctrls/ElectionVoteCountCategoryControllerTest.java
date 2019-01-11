package no.valg.eva.admin.frontend.categories.ctrls;

import no.evote.constants.ElectionLevelEnum;
import no.evote.presentation.config.counting.ElectionVoteCountCategoryElement;
import no.evote.security.UserData;
import no.evote.service.configuration.ElectionVoteCountCategoryService;
import no.evote.service.configuration.MvElectionService;
import no.evote.service.configuration.VoteCountCategoryService;
import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test cases for ElectionVoteCountCategoryController
 */

public class ElectionVoteCountCategoryControllerTest extends BaseFrontendTest {

	private ElectionVoteCountCategoryController ctrl;
	private Random randomGenerator = new Random(System.currentTimeMillis());

	@BeforeMethod
	public void initMocks() throws Exception {
		ctrl = initializeMocks(ElectionVoteCountCategoryController.class);
	}

	@Test
    public void init_withInvalidOperatorLevel_returnsNull() {
		stub_operatorElectionLevel(ElectionLevelEnum.CONTEST);

		ctrl.init();

		assertThat(ctrl.getElements()).isNull();
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "MvElection list size expected to be 1")
    public void init_withMoreThanOneElectionGroup_throwsException() {
		stub_operatorElectionLevel(ElectionLevelEnum.ELECTION_EVENT);
		stub_findByPathAndChildLevel(3);

		ctrl.init();
	}

	@Test
    public void init_withValidData_verifyState() {
		stub_operatorElectionLevel(ElectionLevelEnum.ELECTION_EVENT);
		stub_findByPathAndChildLevel(1);
		stub_findElectionVoteCountCategories(2);
		stub_editable(false);

		ctrl.init();

		assertThat(ctrl.getSelectedElectionGroup()).isNotNull();
		assertThat(ctrl.isEditable()).isFalse();
		assertThat(ctrl.isRequiredProtocolCount()).isFalse();
		assertThat(ctrl.getElements()).isNotNull();
	}

	@Test
	public void elementListIsPopulatedFromElectionVoteCountCategoryAndVoteCountCategoryTables() {

		ElectionGroup electionGroup = getElectionGroup("hello");
		List<ElectionVoteCountCategory> electionVoteCountCategories = new ArrayList<>();
		ElectionVoteCountCategory existingVoCat = createElectionVoteCountCategory(VO.getId());
		electionVoteCountCategories.add(existingVoCat);
		ElectionVoteCountCategory existingVfCat = createElectionVoteCountCategory(VF.getId());
		electionVoteCountCategories.add(existingVfCat);
		when(getInjectMock(ElectionVoteCountCategoryService.class).findElectionVoteCountCategories(getUserDataMock(),
				electionGroup, BF)).thenReturn(electionVoteCountCategories);

		List<VoteCountCategory> voteCountCategories = new ArrayList<>();
		voteCountCategories.add(createVoteCountCategory(VO.getId()));
		voteCountCategories.add(createVoteCountCategory(VF.getId()));
		VoteCountCategory newFoCat = createVoteCountCategory(FO.getId());
		voteCountCategories.add(newFoCat);
		when(getInjectMock(VoteCountCategoryService.class).findAll(getUserDataMock(), BF)).thenReturn(voteCountCategories);

		List<ElectionVoteCountCategoryElement> elements = ctrl.electionVoteCountCategoryElements(getUserDataMock(),
				electionGroup);
		assertThat(elements).hasSize(3);

		Set<ElectionVoteCountCategory> catSet = new HashSet<>();
		catSet.add(elements.get(0).getElectionVoteCountCategory());
		catSet.add(elements.get(1).getElectionVoteCountCategory());
		catSet.add(elements.get(2).getElectionVoteCountCategory());
		assertTrue("VO should be existing cat from election_vote_count_category table", catSet.remove(existingVoCat));
		assertTrue("VF should be existing cat from election_vote_count_category table", catSet.remove(existingVfCat));
		assertEquals("FO should be new cat based on row in vote_count_category table", FO.getId(), catSet.iterator().next().key());
	}

	@Test
	public void electionVoteCountCategoryElements_withNullForElectionGroup_returnsEmptyList() {
		List<ElectionVoteCountCategoryElement> elements = ctrl.electionVoteCountCategoryElements(getUserDataMock(), null);

		assertThat(elements).isEmpty();
	}

	@Test
	public void getPageTitleMeta_withElectionGroup_returnsGroupHeading() throws Exception {
		MockUtils.setPrivateField(ctrl, "selectedElectionGroup", getElectionGroup("Mitt valg"));

		List<PageTitleMetaModel> result = ctrl.getPageTitleMeta();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getLabel()).isEqualTo("@election_level[1].name");
		assertThat(result.get(0).getValue()).isEqualTo("Mitt valg");

	}

	private ElectionVoteCountCategory createElectionVoteCountCategory(String id) {
		ElectionVoteCountCategory cat = new ElectionVoteCountCategory();
		cat.setVoteCountCategory(createVoteCountCategory(id));
		cat.setPk(randomGenerator.nextLong());
		return cat;
	}

	private VoteCountCategory createVoteCountCategory(String id) {
		VoteCountCategory cat = new VoteCountCategory();
		cat.setId(id);
		return cat;
	}

	private void stub_operatorElectionLevel(ElectionLevelEnum level) {
		when(getUserDataMock().getOperatorMvElection().getElectionLevel()).thenReturn(level.getLevel());
	}

	private List<MvElection> stub_findByPathAndChildLevel(int size) {
		List<MvElection> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			result.add(createMock(MvElection.class));
		}
		when(getInjectMock(MvElectionService.class).findByPathAndChildLevel(any(UserData.class), any(MvElection.class))).thenReturn(result);
		return result;
	}

	private List<ElectionVoteCountCategory> stub_findElectionVoteCountCategories(int size) {
		List<ElectionVoteCountCategory> result = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			result.add(createMock(ElectionVoteCountCategory.class));
		}
		when(getInjectMock(ElectionVoteCountCategoryService.class).findElectionVoteCountCategories(eq(getUserDataMock()), any(ElectionGroup.class), eq(BF)))
				.thenReturn(result);
		return result;
	}

	private void stub_editable(boolean value) {
		ElectionEventStatusEnum status = value ? ElectionEventStatusEnum.CENTRAL_CONFIGURATION : ElectionEventStatusEnum.APPROVED_CONFIGURATION;
		when(getInjectMock(UserDataController.class).isOverrideAccess()).thenReturn(value);
		when(getInjectMock(UserDataController.class).getElectionEvent().getElectionEventStatus().getId()).thenReturn(status.id());
	}

	private ElectionGroup getElectionGroup(String name) {
		ElectionGroup result = new ElectionGroup();
		result.setName(name);
		return result;
	}

}

