package no.valg.eva.admin.frontend.delete.ctrls;

import no.evote.service.voting.VotingService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DeleteVotingsControllerTest extends BaseFrontendTest {

	@Test
	public void doInit_withServiceData_shouldHaveInitState() throws Exception {
		DeleteVotingsController ctrl = initializeMocks(DeleteVotingsController.class);
		when(getInjectMock(VotingService.class).findAllVotingCategories(getUserDataMock())).thenReturn(mockList(1, VotingCategory.class));
		setUserDataElection(ELECTION_PATH_ELECTION_GROUP);
		setUserDataArea(AREA_PATH_COUNTY);

		ctrl.init();

		assertThat(ctrl.getVotingCategoryList()).hasSize(1);
		assertThat(ctrl.getSelectedVotingCategoryPks()).isEmpty();
	}

	@Test
	public void deleteVotings_withEvoteException_shouldAddErrorMessage() throws Exception {
		DeleteVotingsController ctrl = initializeMocks(DeleteVotingsController.class);
		ctrl.setSelectedVotingCategoryPks(new ArrayList<>());
        evoteExceptionWhen(VotingService.class, "@evote").deleteVotings(eq(getUserDataMock()), any(), any(), any());

		ctrl.deleteVotings();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@evote");
	}

	@Test
	public void deleteVotings_withNoVotingCategoriesSelected_shouldDeleteAllVotings() throws Exception {
		DeleteVotingsController ctrl = initializeMocks(DeleteVotingsController.class);
		ctrl.setSelectedVotingCategoryPks(new ArrayList<>());
		setUserDataElection(ELECTION_PATH_ELECTION_GROUP);
		setUserDataArea(AREA_PATH_COUNTY);
		ctrl.init();

		ctrl.deleteVotings();

		verify_deleteVotings(null);
		verify(getInjectMock(VotingService.class)).deleteSeqVotingNumber(eq(getUserDataMock()), any(MvElection.class), any(MvArea.class));
	}

	@Test
	public void deleteVotings_withTwoVotingCategoriesSelected_shouldDeleteAllVotingsForAllVotingCategories() throws Exception {
		DeleteVotingsController ctrl = initializeMocks(DeleteVotingsController.class);
		ctrl.setSelectedVotingCategoryPks(Arrays.asList("1", "2"));
		MvElection mvElection = createMock(MvElection.class);
		when(mvElection.toString()).thenReturn("Valg");
		mockFieldValue("mvElection", mvElection);
		MvArea mvArea = new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue();
		when(mvArea.toString()).thenReturn("Oslo");
		mockFieldValue("mvArea", mvArea);

		ctrl.deleteVotings();

		verify_deleteVotings(1);
		verify_deleteVotings(2);
		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@delete.votings.confirmation, Valg, Oslo]");
	}

	private void verify_deleteVotings(Integer selectedVotingCategoryPk) {
		verify(getInjectMock(VotingService.class), times(1)).deleteVotings(eq(getUserDataMock()), any(MvElection.class), any(MvArea.class),
				eq(selectedVotingCategoryPk));
	}
}

