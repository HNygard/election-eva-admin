package no.valg.eva.admin.frontend.contexteditor.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.model.election.GenericElectionType;
import no.valg.eva.admin.common.configuration.service.ElectionService;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.SaveElectionResponse;
import no.valg.eva.admin.frontend.election.ctrls.MvElectionPickerController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElectionControllerTest extends BaseFrontendTest {

	@Test
	public void prepareForCreate_withElection_verifyState() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);

		ctrl.prepareForCreate(ELECTION_PATH_ELECTION_GROUP, "Name");

		assertThat(ctrl.getCurrentElection()).isEqualToComparingFieldByField(getPreparedElection("Name"));
		assertThat(ctrl.getCurrentElection().isAutoGenerateContests()).isFalse();
		assertThat(ctrl.getSelectedAreaLevel()).isNull();
		assertThat(ctrl.getActiveIndex()).isEqualTo("0");
		assertThat(ctrl.isBrukStemmetillegg()).isFalse();
	}

	@Test
	public void doCreateElection_withInvalidRenumberLogic_returnsErrorMessage() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		Election election = election();
		election.setRenumber(false);
		election.setRenumberLimit(true);

		ctrl.doCreateElection(election);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@election.election.renumberConstraint");
	}

	@Test
	public void doCreateElection_withDuplicateId_returnsErrorMessage() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		Election election = election();
		ctrl.setSelectedAreaLevel("2");
		stub_save(saveElectionResponse(true, election));

		ctrl.doCreateElection(election);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "[@common.message.create.CHOOSE_UNIQUE_ID, @election_level[2].name, 11, Election Group Name 11]");
	}

	@Test
	public void doCreateElection_withValidElection_createsNewElection() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		Election election = election();
		ctrl.setSelectedAreaLevel("2");
		stub_save(saveElectionResponse(false, election));

		ctrl.doCreateElection(election);

		verify(getInjectMock(MvElectionPickerController.class)).update(ElectionLevelEnum.ELECTION.getLevel(), election.getElectionPath().path());
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.message.sub_create.successful, Name 11, Election Group Name 11]");
		assertThat(ctrl.getCurrentElection()).isEqualToComparingFieldByField(getPreparedElection("Election Group Name 11"));
		verify(getRequestContextMock()).execute("PF('createElectionLevel2Widget').hide()");
		verify(getRequestContextMock()).update("hierarchyEditor");
	}

	@Test
	public void prepareForUpdate_withElection_verifyState() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		Election election = getPreparedElection("Name");
		election.setAreaLevel(AreaLevelEnum.COUNTY.getLevel());
		election.setBaselineVoteFactor(BigDecimal.ONE);
		stub_electionService_get(election);

		ctrl.prepareForUpdate(createMock(MvElectionMinimal.class));

		assertThat(ctrl.getCurrentElection()).isNotNull();
		assertThat(ctrl.getSelectedAreaLevel()).isEqualTo(String.valueOf(AreaLevelEnum.COUNTY.getLevel()));
		assertThat(ctrl.isReadOnly()).isFalse();
		assertThat(ctrl.getActiveIndex()).isEqualTo("0");
		assertThat(ctrl.isBrukStemmetillegg()).isTrue();
	}

	@Test
	public void doUpdateElection_withValidElection_createsNewElection() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		Election election = election();
		ctrl.setSelectedAreaLevel("2");
		stub_save(saveElectionResponse(false, election));

		ctrl.doUpdateElection(election);

		verify(getInjectMock(MvElectionPickerController.class)).update(ElectionLevelEnum.ELECTION.getLevel(), null);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.update.successful");
		verify(getRequestContextMock()).execute("PF('editElectionLevel2Widget').hide()");
		verify(getRequestContextMock()).update("hierarchyEditor");
	}

	@Test
	public void doDeleteElection_withNoRemovable_deleteIsNotExecuted() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		Election election = election();
		stub_isCurrentRemovable(false);

		ctrl.doDeleteElection(election);

		verify(getInjectMock(ElectionService.class), never()).delete(eq(getUserDataMock()), any(ElectionPath.class));
	}

	@Test
	public void doDeleteElection_withRemovable_deletesElection() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		Election election = election();
		stub_isCurrentRemovable(true);

		ctrl.doDeleteElection(election);

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.message.sub_delete.successful, Name 11, Election Group Name 11]");
		verify(getInjectMock(ElectionService.class)).delete(eq(getUserDataMock()), any(ElectionPath.class));
		verify(getInjectMock(MvElectionPickerController.class)).update(ElectionLevelEnum.ELECTION.getLevel(), null);
	}

	@Test
	public void isInputDisabled_withWriteModeAndEventNotDisabled_returnsFalse() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		ctrl.setReadOnly(false);
		when(getInjectMock(UserDataController.class).isCurrentElectionEventDisabled()).thenReturn(false);

		assertThat(ctrl.isInputDisabled()).isFalse();
	}

	@Test
	public void getMinMaxCandidateTypes_returnsCorrectList() throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);

		
		assertThat(ctrl.getMinMaxCandidateTypes()).hasSize(3);
		assertThat(ctrl.getMinMaxCandidateTypes().get(0).getValue()).isSameAs(ElectionController.MinMaxCandidateType.MIN_MAX_ADDITION);
		assertThat(ctrl.getMinMaxCandidateTypes().get(1).getValue()).isSameAs(ElectionController.MinMaxCandidateType.MIN_MAX);
		assertThat(ctrl.getMinMaxCandidateTypes().get(2).getValue()).isSameAs(ElectionController.MinMaxCandidateType.OVERRIDE);
		
	}

	@Test(dataProvider = "correctMaxCandidates")
	public void correctMaxCandidates_withDataProvider_verifyExpected(ElectionController.MinMaxCandidateType type) throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		ctrl.setMaxCandidatesType(type);
		Election election = createMock(Election.class);

		ctrl.correctMaxCandidates(election);

		if (type == ElectionController.MinMaxCandidateType.MIN_MAX_ADDITION) {
			verify(election, atLeastOnce()).setMaxCandidates(null);
			verify(election, never()).setMaxCandidatesAddition(null);
		} else if (type == ElectionController.MinMaxCandidateType.MIN_MAX_ADDITION) {
			verify(election, never()).setMaxCandidates(null);
			verify(election, atLeastOnce()).setMaxCandidatesAddition(null);
		} else if (type == ElectionController.MinMaxCandidateType.OVERRIDE) {
			verify(election, atLeastOnce()).setMaxCandidates(null);
			verify(election, atLeastOnce()).setMaxCandidatesAddition(null);
		}
	}

	@Test(dataProvider = "correctMaxCandidates")
	public void correctMinCandidates_withDataProvider_verifyExpected(ElectionController.MinMaxCandidateType type) throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);
		ctrl.setMinCandidatesType(type);
		Election election = createMock(Election.class);

		ctrl.correctMinCandidates(election);

		if (type == ElectionController.MinMaxCandidateType.MIN_MAX_ADDITION) {
			verify(election, atLeastOnce()).setMinCandidates(null);
			verify(election, never()).setMinCandidatesAddition(null);
		} else if (type == ElectionController.MinMaxCandidateType.MIN_MAX_ADDITION) {
			verify(election, never()).setMinCandidates(null);
			verify(election, atLeastOnce()).setMinCandidatesAddition(null);
		} else if (type == ElectionController.MinMaxCandidateType.OVERRIDE) {
			verify(election, atLeastOnce()).setMinCandidates(null);
			verify(election, atLeastOnce()).setMinCandidatesAddition(null);
		}
	}

	@DataProvider
	public Object[][] correctMaxCandidates() {
		return new Object[][] {
				{ ElectionController.MinMaxCandidateType.MIN_MAX_ADDITION },
				{ ElectionController.MinMaxCandidateType.MIN_MAX },
				{ ElectionController.MinMaxCandidateType.OVERRIDE }
		};
	}

	@Test(dataProvider = "resolveMinMaxCandidateType")
	public void resolveMinMaxCandidateType_withDataProvider_verifyExpected(Integer minMaxCandidateAddition, Integer minMaxCandidate,
			ElectionController.MinMaxCandidateType expected) throws Exception {
		ElectionController ctrl = initializeMocks(ElectionController.class);

		ElectionController.MinMaxCandidateType result = ctrl.resolveMinMaxCandidateType(minMaxCandidateAddition, minMaxCandidate);

		assertThat(result).isSameAs(expected);
	}

	@DataProvider
	public Object[][] resolveMinMaxCandidateType() {
		return new Object[][] {
				{ 1, null, ElectionController.MinMaxCandidateType.MIN_MAX_ADDITION },
				{ null, 1, ElectionController.MinMaxCandidateType.MIN_MAX },
				{ null, null, ElectionController.MinMaxCandidateType.OVERRIDE }
		};
	}

	private Election getPreparedElection(String electionGroupName) {
		Election result = new Election(ELECTION_PATH_ELECTION_GROUP);
		result.setGenericElectionType(GenericElectionType.F);
		result.setElectionGroupName(electionGroupName);
		result.setId("");
		result.setName("");
		result.setPenultimateRecount(true);
		result.setCandidateRankVoteShareThreshold(BigDecimal.ZERO);
		result.setLevelingSeatsVoteShareThreshold(BigDecimal.ZERO);
		result.setSettlementFirstDivisor(ElectionController.DEFAULT_SETTLEMENT_FIRST_DIVISOR);
		result.setCandidatesInContestArea(true);
		result.setSingleArea(true);
		
		result.setMaxCandidateNameLength(25);
		result.setMaxCandidateResidenceProfessionLength(20);
		
		return result;
	}

	private SaveElectionResponse stub_save(SaveElectionResponse response) {
        when(getInjectMock(ElectionService.class).save(eq(getUserDataMock()), any())).thenReturn(response);
		return response;
	}

	private Election stub_electionService_get(Election election) {
		when(getInjectMock(ElectionService.class).get(eq(getUserDataMock()), any(ElectionPath.class))).thenReturn(election);
		return election;
	}

	private SaveElectionResponse saveElectionResponse(boolean withIdNotUniqueError, Election election) {
		if (withIdNotUniqueError) {
			return SaveElectionResponse.withIdNotUniqueError();
		}
		return SaveElectionResponse.ok().setVersionedObject(election);
	}

	private Election election() {
		Election election = new Election(ELECTION_PATH_ELECTION_GROUP);
		election.setId("11");
		election.setName("Name 11");
		election.setElectionGroupName("Election Group Name 11");
		return election;
	}

	private void stub_isCurrentRemovable(boolean isCurrentRemovable) {
		when(getInjectMock(UserDataController.class).getElectionEvent().getElectionEventStatus().getId()).thenReturn(isCurrentRemovable
				? ElectionEventStatusEnum.CENTRAL_CONFIGURATION.id() : ElectionEventStatusEnum.CLOSED.id());
	}

}
