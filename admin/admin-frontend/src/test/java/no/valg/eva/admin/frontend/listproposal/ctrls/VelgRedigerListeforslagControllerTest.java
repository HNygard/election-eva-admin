package no.valg.eva.admin.frontend.listproposal.ctrls;

import no.evote.constants.EvoteConstants;
import no.evote.exception.ErrorCode;
import no.evote.security.UserData;
import no.evote.service.configuration.AffiliationService;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import org.primefaces.event.ReorderEvent;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class VelgRedigerListeforslagControllerTest extends BaseFrontendTest {

	@Test
	public void onRowReorder_withInvalidTargetReorder_shouldRollback() throws Exception {
		VelgRedigerListeforslagController ctrl = initializeMocks(VelgRedigerListeforslagController.class);
		ReorderEvent reorderEventStub = onRowReorder_setup(0, 4, 5, false);
		ctrl.getAffiliationList().get(4).setApproved(true);
		ctrl.onRowReorder(reorderEventStub);

		List<Affiliation> result = ctrl.getAffiliationList();
		assertThat(result.size()).isEqualTo(5);
		assertThat(result.get(0).getParty().getId()).isEqualTo("in4");
		assertThat(result.get(1).getParty().getId()).isEqualTo("in0");
		assertThat(result.get(2).getParty().getId()).isEqualTo("in1");
		assertThat(result.get(3).getParty().getId()).isEqualTo("in2");
		assertThat(result.get(4).getParty().getId()).isEqualTo("in3");

	}

	@Test
	public void onRowReorder_withInvalidSourceReorder_shouldRollback() throws Exception {
		VelgRedigerListeforslagController ctrl = initializeMocks(VelgRedigerListeforslagController.class);
		ReorderEvent reorderEventStub = onRowReorder_setup(0, 4, 5, false);
		ctrl.onRowReorder(reorderEventStub);

		List<Affiliation> result = ctrl.getAffiliationList();
		assertThat(result.size()).isEqualTo(5);
		assertThat(result.get(0).getParty().getId()).isEqualTo("in4");
		assertThat(result.get(1).getParty().getId()).isEqualTo("in0");
		assertThat(result.get(2).getParty().getId()).isEqualTo("in1");
		assertThat(result.get(3).getParty().getId()).isEqualTo("in2");
		assertThat(result.get(4).getParty().getId()).isEqualTo("in3");
	}

	@Test
	public void onRowReorder_withFullReorder_fullSwap() throws Exception {
		VelgRedigerListeforslagController ctrl = initializeMocks(VelgRedigerListeforslagController.class);
		ReorderEvent reorderEventStub = onRowReorder_setup(0, 4, 5, true);
		ctrl.onRowReorder(reorderEventStub);

		List<Affiliation> result = ctrl.getAffiliationList();
		assertThat(result.size()).isEqualTo(5);
		for (int i = 0; i < result.size(); i++) {
			assertThat(result.get(i).getParty().getId()).isEqualTo("out" + i);
		}

	}

	@Test
	public void onRowReorder_withPartialReorder_partialSwap() throws Exception {
		VelgRedigerListeforslagController ctrl = initializeMocks(VelgRedigerListeforslagController.class);
		ReorderEvent reorderEventStub = onRowReorder_setup(2, 3, 2, true);
		ctrl.onRowReorder(reorderEventStub);

		List<Affiliation> result = ctrl.getAffiliationList();
		assertThat(result.size()).isEqualTo(5);
		assertThat(result.get(0).getParty().getId()).isEqualTo("in0");
		assertThat(result.get(1).getParty().getId()).isEqualTo("in1");
		assertThat(result.get(2).getParty().getId()).isEqualTo("out0");
		assertThat(result.get(3).getParty().getId()).isEqualTo("out1");
		assertThat(result.get(4).getParty().getId()).isEqualTo("in4");

	}

	@Test
	public void onRowReorder_withException_returnsErrorMessage() throws Exception {
		VelgRedigerListeforslagController ctrl = initializeMocks(VelgRedigerListeforslagController.class);
		ReorderEvent event = onRowReorder_setup(0, 4, 5, true);
		evoteExceptionWhen(AffiliationService.class)
				.changeDisplayOrder(eq(getUserDataMock()), any(Affiliation.class), anyInt(), anyInt());

		ctrl.onRowReorder(event);

		assertFacesMessage(SEVERITY_ERROR, "[@common.error.unexpected, cb0e38f0]");
	}

	@Test
	public void onError_withEntityNotFoundError_verifyContestUpdatedAndReturnValue() throws Exception {
		VelgRedigerListeforslagController ctrl = initializeMocks(VelgRedigerListeforslagController.class);
		mockFieldValue("affiliationList", new ArrayList<>());
		mockFieldValue("kontekst", createMock(Kontekst.class));
		Contest contest = createMock(Contest.class);
		ElectionType electionType = new ElectionType();
		electionType.setId(EvoteConstants.ELECTION_TYPE_DIRECT);
		when(getInjectMock(MvElectionService.class).findSingleByPath(any(ElectionPath.class)).getContest()).thenReturn(contest);
		when(contest.getElection().getElectionType()).thenReturn(electionType);
        when(getInjectMock(AffiliationService.class).findByContest(eq(getUserDataMock()), anyLong())).thenReturn(new ArrayList<>());

		String result = ctrl.onError(ErrorCode.ERROR_CODE_0502_ENTITY_NOT_FOUND);

		assertThat(result).isEqualTo("@listProposal.save.optimisticLockingException");
	}

	private ReorderEvent onRowReorder_setup(int from, int to, int out, boolean approved) throws Exception {
		ReorderEvent reorderEventStub = mock(ReorderEvent.class);
		mockFieldValue("affiliationList", getAffiliationList("in", 5, approved));
		when(reorderEventStub.getFromIndex()).thenReturn(from);
		when(reorderEventStub.getToIndex()).thenReturn(to);
		when(getInjectMock(AffiliationService.class).changeDisplayOrder(any(UserData.class), any(Affiliation.class), anyInt(), anyInt())).thenReturn(
				getAffiliationList("out", out, approved));
		return reorderEventStub;
	}

	private List<Affiliation> getAffiliationList(String name, int num, boolean approved) {
		List<Affiliation> result = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			result.add(new Affiliation());
			result.get(i).setDisplayOrder(i + 1);
			result.get(i).setParty(new Party());
			result.get(i).setApproved(approved);
			result.get(i).getParty().setId(name + i);
		}
		return result;
	}

}

